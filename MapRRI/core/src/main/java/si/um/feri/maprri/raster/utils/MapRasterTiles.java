package si.um.feri.maprri.raster.utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import si.um.feri.maprri.raster.utils.ZoomXY;

public class MapRasterTiles {
    //Mapbox
    //https://docs.mapbox.com/api/maps/raster-tiles/
    /*static String mapServiceUrl = "https://api.mapbox.com/v4/";
    static String token = "?access_token=" + Keys.MAPBOX;
    static String tilesetId = "mapbox.satellite";
    static String format = "@2x.jpg90";*/

    //Geoapify
    //https://www.geoapify.com/get-started-with-maps-api
    static String mapServiceUrl = "https://maps.geoapify.com/v1/tile/";
    static String token = "?&apiKey=" + si.um.feri.maprri.raster.utils.Keys.GEOAPIFY;
    static String tilesetId = "klokantech-basic";
    static String format = "@2x.png";

    //@2x in format means it returns higher DPI version of the image and the image size is 512px (otherwise it is 256px)
    final static public int TILE_SIZE = 512;

    /**
     * Get raster tile based on zoom and tile number.
     *
     * @param zoom
     * @param x
     * @param y
     * @return
     * @throws IOException
     */
    public static Texture getRasterTile(int zoom, int x, int y) throws IOException {
        URL url = new URL(mapServiceUrl + tilesetId + "/" + zoom + "/" + x + "/" + y + format + token);
        ByteArrayOutputStream bis = fetchTile(url);
        return getTexture(bis.toByteArray());
    }

    /**
     * Get raster tile based on zoom and tile number.
     *
     * @param zoomXY string should be in format zoom/x/y
     * @return
     * @throws IOException
     */
    public static Texture getRasterTile(String zoomXY) throws IOException {
        URL url = new URL(mapServiceUrl + tilesetId + "/" + zoomXY + format + token);
        ByteArrayOutputStream bis = fetchTile(url);
        return getTexture(bis.toByteArray());
    }

    /**
     * Get raster tile based on zoom and tile number.
     *
     * @param zoomXY
     * @return
     * @throws IOException
     */
    public static Texture getRasterTile(ZoomXY zoomXY) throws IOException {
        URL url = new URL(mapServiceUrl + tilesetId + "/" + zoomXY.toString() + format + token);
        ByteArrayOutputStream bis = fetchTile(url);
        return getTexture(bis.toByteArray());
    }

    /**
     * Returns tiles for the area of size * size of provided center tile.
     *
     * @param zoomXY center tile
     * @param size
     * @return
     * @throws IOException
     */
    public static Texture[] getRasterTileZone(si.um.feri.maprri.raster.utils.ZoomXY zoomXY, int size) throws IOException {
        Texture[] array = new Texture[size * size];
        int[] factorY = new int[size * size]; //if size is 3 {-1, -1, -1, 0, 0, 0, 1, 1, 1};
        int[] factorX = new int[size * size]; //if size is 3 {-1, 0, 1, -1, 0, 1, -1, 0, 1};

        int value = (size - 1) / -2;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                factorY[i * size + j] = value;
                factorX[i + j * size] = value;
            }
            value++;
        }

        for (int i = 0; i < size * size; i++) {
            array[i] = getRasterTile(zoomXY.zoom, zoomXY.x + factorX[i], zoomXY.y + factorY[i]);
            System.out.println(zoomXY.zoom + "/" + (zoomXY.x + factorX[i]) + "/" + (zoomXY.y + factorY[i]));
        }
        return array;
    }

    /**
     * Gets tile from provided URL and returns it as ByteArrayOutputStream.
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static ByteArrayOutputStream fetchTile(URL url) throws IOException {
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        InputStream is = url.openStream();
        byte[] bytebuff = new byte[4096];
        int n;

        while ((n = is.read(bytebuff)) > 0) {
            bis.write(bytebuff, 0, n);
        }
        return bis;
    }

    /**
     * Converts byte[] to Texture.
     *
     * @param array
     * @return
     */
    public static Texture getTexture(byte[] array) {
        return new Texture(new Pixmap(array, 0, array.length));
    }

    //https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Java

    /**
     * It converts to tile number based on the geolocation (latitude and longitude) and zoom
     *
     * @param lat  latitude
     * @param lon  longitude
     * @param zoom
     * @return
     */
    public static si.um.feri.maprri.raster.utils.ZoomXY getTileNumber(final double lat, final double lon, final int zoom) {
        int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        if (xtile < 0)
            xtile = 0;
        if (xtile >= (1 << zoom))
            xtile = ((1 << zoom) - 1);
        if (ytile < 0)
            ytile = 0;
        if (ytile >= (1 << zoom))
            ytile = ((1 << zoom) - 1);
        return new si.um.feri.maprri.raster.utils.ZoomXY(zoom, xtile, ytile);
    }

    //https://www.maptiler.com/google-maps-coordinates-tile-bounds-projection/#15/15.63/46.56
    //https://gis.stackexchange.com/questions/17278/calculate-lat-lon-bounds-for-individual-tile-generated-from-gdal2tiles
    public static double tile2long(int tileNumberX, int zoom) {
        return (tileNumberX / Math.pow(2, zoom) * 360 - 180);
    }

    public static double tile2lat(int tileNumberY, int zoom) {

        double n = Math.PI - 2 * Math.PI * tileNumberY / Math.pow(2, zoom);
        return (180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n))));
    }

    public static double[] project(double lat, double lng, int tileSize) {
        double siny = Math.sin((lat * Math.PI) / 180);

        // Truncating to 0.9999 effectively limits latitude to 89.189. This is
        // about a third of a tile past the edge of the world tile.
        siny = Math.min(Math.max(siny, -0.9999), 0.9999);

        return new double[]{
                tileSize * (0.5 + lng / 360),
                tileSize * (0.5 - Math.log((1 + siny) / (1 - siny)) / (4 * Math.PI))
        };
    }

    /**
     * Converts geolocation to pixel position.
     *
     * @param lat        latitude
     * @param lng        longitude
     * @param tileSize
     * @param zoom
     * @param beginTileX x (tile number) of top left tile
     * @param beginTileY y (tile number) of top left tile
     * @param height     viewport height
     * @return
     */
    public static Vector2 getPixelPosition(double lat, double lng, int tileSize, int zoom, int beginTileX, int beginTileY, int height) {
        double[] worldCoordinate = project(lat, lng, tileSize);
        // Scale to fit our image
        double scale = Math.pow(2, zoom);

        // Apply scale to world coordinates to get image coordinates
        return new Vector2(
                (int) (Math.floor(worldCoordinate[0] * scale) - (beginTileX * tileSize)),
                height - (int) (Math.floor(worldCoordinate[1] * scale) - (beginTileY * tileSize) - 1)
        );
    }

    public static Vector2 getPixelPosition(double lat, double lng, int beginTileX, int beginTileY) {
        double[] worldCoordinate = project(lat, lng, MapRasterTiles.TILE_SIZE);
        // Scale to fit our image
        double scale = Math.pow(2, si.um.feri.maprri.raster.utils.Constants.ZOOM);

        // Apply scale to world coordinates to get image coordinates
        return new Vector2(
                (int) (Math.floor(worldCoordinate[0] * scale) - (beginTileX * MapRasterTiles.TILE_SIZE)),
                si.um.feri.maprri.raster.utils.Constants.MAP_HEIGHT - (int) (Math.floor(worldCoordinate[1] * scale) - (beginTileY * MapRasterTiles.TILE_SIZE) - 1)
        );
    }

    public static si.um.feri.maprri.raster.utils.Geolocation[][] fetchPath(si.um.feri.maprri.raster.utils.Geolocation[] geolocations){
        // Example coordinates (longitude, latitude)
        double[][] coordinatesArray = {
                {-122.42, 37.78}, // San Francisco
                {-121.89, 37.33}, // San Jose
                {-122.08, 37.39}  // Palo Alto
        };

        double[][] coordinates = new double[geolocations.length][2];
        for(int i=0; i<geolocations.length; i++){
            coordinates[i] = new double[]{geolocations[i].lat, geolocations[i].lng};
        }

        try {
            return getRouteFromCoordinates(coordinates);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static si.um.feri.maprri.raster.utils.Geolocation[][] getRouteFromCoordinates(double[][] coordinates) throws Exception {
        // Build the coordinates string for the URL
        StringBuilder coordinatesPath = new StringBuilder();
        for (int i = 0; i < coordinates.length; i++) {
            coordinatesPath.append(coordinates[i][0]).append(",").append(coordinates[i][1]);
            if (i < coordinates.length - 1) coordinatesPath.append("|");
        }

        // Construct the URL
        String urlString = "https://api.geoapify.com/v1/routing?waypoints=" + coordinatesPath.toString() +
                "&mode=" + "drive" + "&apiKey=" + si.um.feri.maprri.raster.utils.Keys.GEOAPIFY;

        // Open connection
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Check the response code
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
        }

        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        si.um.feri.maprri.raster.utils.Geolocation[][] geolocations;

        // Parse the JSON response
        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray features = jsonResponse.getJSONArray("features");
        if (features.length() > 0) {
            JSONObject geometry = features.getJSONObject(0).getJSONObject("geometry");
            JSONArray coordinatesArray = geometry.getJSONArray("coordinates");

            geolocations = new si.um.feri.maprri.raster.utils.Geolocation[coordinatesArray.length()][];
            // Print each coordinate in the path
            for (int i = 0; i < coordinatesArray.length(); i++) {
                JSONArray coord = coordinatesArray.getJSONArray(i);

                si.um.feri.maprri.raster.utils.Geolocation[] geol = new si.um.feri.maprri.raster.utils.Geolocation[coord.length()];
                for (int j = 0; j < coord.length(); j++) {
                    JSONArray c = coord.getJSONArray(j);
                    double lon = c.getDouble(0);
                    double lat = c.getDouble(1);
                    System.out.println("Longitude: " + lon + ", Latitude: " + lat);
                    geol[j] = new si.um.feri.maprri.raster.utils.Geolocation(lat, lon);
                }
                geolocations[i] = geol;
            }

            return geolocations;
        } else {
            return null;
        }
    }
}
