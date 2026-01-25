package si.um.feri.copycats.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.net.HttpRequestHeader;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;


public class MapRasterTiles {

    private static final String MAP_SERVICE_URL = "https://maps.geoapify.com/v1/tile/";
    private static final String TILESET_ID = "osm-carto";

    private static final String FORMAT = "@2x.png";

    public static final int TILE_SIZE = 512;

    private static final String API_KEY;
    static {
        String k = Dotenv.get("GEOAPIFY_KEY");
        API_KEY = (k != null) ? k : "";
    }

    private static final String CACHE_DIR = "tilecache_geoapify";

    private static final int MAX_IN_FLIGHT = 4;

    private static final ObjectMap<String, Texture> RAM = new ObjectMap<>();

    private static final ObjectMap<String, Boolean> IN_PROGRESS = new ObjectMap<>();

    private static final Queue<TileReq> QUEUE = new Queue<>();

    private static int inFlight = 0;

    private static class TileReq {
        int z, x, y;
        String key;
        FileHandle file;
        String url;
        TileReq(int z, int x, int y, String key, FileHandle file, String url) {
            this.z = z; this.x = x; this.y = y;
            this.key = key; this.file = file; this.url = url;
        }
    }
    public static Texture getRasterTileCachedAsync(int zoom, int x, int y) {
        String key = zoom + "/" + x + "/" + y;

        Texture inRam = RAM.get(key);
        if (inRam != null) return inRam;

        FileHandle fh = tileFile(zoom, x, y);
        if (fh.exists()) {
            Texture t = tryLoadTextureFromFile(fh);
            if (t != null) {
                RAM.put(key, t);
                return t;
            } else {
                fh.delete();
            }
        }

        if (!IN_PROGRESS.containsKey(key)) {
            IN_PROGRESS.put(key, true);
            enqueue(zoom, x, y, key, fh);
        }
        pumpQueue();
        return null;
    }

    public static void clearRamCache() {
        for (Texture t : RAM.values()) {
            if (t != null) t.dispose();
        }
        RAM.clear();
    }

    public static ZoomXY[] getTileZoneCoords(ZoomXY centerTile, int sizeX, int sizeY) {
        ZoomXY[] coords = new ZoomXY[sizeX * sizeY];

        int offsetX = (sizeX - 1) / 2;
        int offsetY = (sizeY - 1) / 2;

        int startX = centerTile.x - offsetX;
        int startY = centerTile.y - offsetY;

        int idx = 0;
        for (int row = 0; row < sizeY; row++) {
            for (int col = 0; col < sizeX; col++) {
                coords[idx++] = new ZoomXY(centerTile.zoom, startX + col, startY + row);
            }
        }
        return coords;
    }


    private static void enqueue(int z, int x, int y, String key, FileHandle outFile) {
        String url = MAP_SERVICE_URL + TILESET_ID + "/" + z + "/" + x + "/" + y + FORMAT + "?apiKey=" + API_KEY;
        QUEUE.addLast(new TileReq(z, x, y, key, outFile, url));
    }

    private static void pumpQueue() {
        while (inFlight < MAX_IN_FLIGHT && QUEUE.notEmpty()) {
            TileReq r = QUEUE.removeFirst();
            downloadTile(r);
        }
    }

    private static void downloadTile(TileReq r) {
        inFlight++;

        HttpRequestBuilder b = new HttpRequestBuilder();
        Net.HttpRequest req = b.newRequest().method(Net.HttpMethods.GET).url(r.url).build();
        req.setHeader(HttpRequestHeader.UserAgent, "MapRRI-student-project/1.0");
        req.setHeader(HttpRequestHeader.Accept, "image/png,image/*;q=0.8,*/*;q=0.5");

        Gdx.net.sendHttpRequest(req, new Net.HttpResponseListener() {

            @Override public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    int code = httpResponse.getStatus().getStatusCode();
                    if (code != HttpStatus.SC_OK) {
                        cleanupFailure(r);
                        return;
                    }

                    String ct = httpResponse.getHeader("Content-Type");
                    if (ct == null || !ct.toLowerCase().startsWith("image/")) {
                        cleanupFailure(r);
                        return;
                    }

                    byte[] bytes = httpResponse.getResult();
                    if (!looksLikePng(bytes)) {
                        cleanupFailure(r);
                        return;
                    }

                    ensureCacheDir();
                    r.file.writeBytes(bytes, false);

                    Gdx.app.postRunnable(() -> {
                        Texture t = tryLoadTextureFromBytes(bytes);
                        if (t != null) {
                            RAM.put(r.key, t);
                        } else {
                            if (r.file.exists()) r.file.delete();
                        }
                        finishRequest(r.key);
                    });

                } catch (Exception e) {
                    cleanupFailure(r);
                }
            }

            @Override public void failed(Throwable t) {
                cleanupFailure(r);
            }

            @Override public void cancelled() {
                cleanupFailure(r);
            }
        });
    }

    private static void cleanupFailure(TileReq r) {
        Gdx.app.postRunnable(() -> {
            if (r.file.exists()) r.file.delete();
            finishRequest(r.key);
        });
    }

    private static void finishRequest(String key) {
        IN_PROGRESS.remove(key);
        inFlight = Math.max(0, inFlight - 1);
        pumpQueue();
    }

    private static void ensureCacheDir() {
        FileHandle dir = Gdx.files.local(CACHE_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    private static FileHandle tileFile(int z, int x, int y) {
        ensureCacheDir();
        String name = TILESET_ID + "_" + z + "_" + x + "_" + y + FORMAT;
        return Gdx.files.local(CACHE_DIR + "/" + name);
    }

    private static Texture tryLoadTextureFromFile(FileHandle fh) {
        try {
            byte[] bytes = fh.readBytes();
            return tryLoadTextureFromBytes(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static Texture tryLoadTextureFromBytes(byte[] bytes) {
        try {
            Pixmap pm = new Pixmap(bytes, 0, bytes.length);
            Texture t = new Texture(pm);
            pm.dispose();
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean looksLikePng(byte[] b) {
        if (b == null || b.length < 8) return false;
        return (b[0] == (byte)0x89 &&
            b[1] == 0x50 &&
            b[2] == 0x4E &&
            b[3] == 0x47 &&
            b[4] == 0x0D &&
            b[5] == 0x0A &&
            b[6] == 0x1A &&
            b[7] == 0x0A);
    }


    public static ZoomXY getTileNumber(final double lat, final double lon, final int zoom) {
        int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        if (xtile < 0) xtile = 0;
        if (xtile >= (1 << zoom)) xtile = ((1 << zoom) - 1);
        if (ytile < 0) ytile = 0;
        if (ytile >= (1 << zoom)) ytile = ((1 << zoom) - 1);
        return new ZoomXY(zoom, xtile, ytile);
    }

    public static double[] project(double lat, double lng, int tileSize) {
        double siny = Math.sin((lat * Math.PI) / 180);
        siny = Math.min(Math.max(siny, -0.9999), 0.9999);

        return new double[]{
            tileSize * (0.5 + lng / 360),
            tileSize * (0.5 - Math.log((1 + siny) / (1 - siny)) / (4 * Math.PI))
        };
    }

    public static Vector2 getPixelPosition(double lat, double lng, int tileSize, int zoom, int beginTileX, int beginTileY, int height) {
        double[] worldCoordinate = project(lat, lng, tileSize);
        double scale = Math.pow(2, zoom);

        return new Vector2(
            (int) (Math.floor(worldCoordinate[0] * scale) - (beginTileX * tileSize)),
            height - (int) (Math.floor(worldCoordinate[1] * scale) - (beginTileY * tileSize) - 1)
        );
    }

}
