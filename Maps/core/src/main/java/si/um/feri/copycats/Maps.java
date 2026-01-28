package si.um.feri.copycats;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

import si.um.feri.copycats.api.EventRepository;
import si.um.feri.copycats.api.LocationRepository;
import si.um.feri.copycats.render.MapRenderer;
import si.um.feri.copycats.utils.EventMarker;
import si.um.feri.copycats.utils.MapRasterTiles;
import si.um.feri.copycats.utils.MarkerIconRegistry;
import si.um.feri.copycats.utils.ZoomXY;
import si.um.feri.copycats.utils.Constants;
import si.um.feri.copycats.utils.Marker;
import si.um.feri.copycats.utils.MarkerType;
import si.um.feri.copycats.utils.Geolocation;

public class Maps extends ApplicationAdapter implements GestureDetector.GestureListener {
    private SpriteBatch batch;
    private Texture placeholder;

    private OrthographicCamera camera;
    private MapRenderer renderer;
    private Viewport viewport;

    private ZoomXY beginTile;
    private ZoomXY[] tileZone;

    private final List<Marker> MARKERS = new ArrayList<>();

    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.561396, 15.643631);

    private final Vector3 tmp = new Vector3();

    private static final float MIN_ZOOM = 0.02f;
    private static final float MAX_ZOOM = 5.0f;
    private static final float WHEEL_ZOOM_FACTOR = 1.15f;

    private boolean zoomAnimating = false;
    private boolean zoomStarted = false;
    private float zoomStartValue = 1f;
    private float zoomTargetValue = 0.3f;
    private final float zoomDuration = 1.5f;
    private float zoomElapsed = 0f;
    private final float startupDelay = 0.6f;
    private float timeSinceStart = 0f;

    @Override
    public void create() {
        batch = new SpriteBatch();
        MarkerIconRegistry.load();
        LocationRepository.load(EventRepository::load);
        renderer = new MapRenderer();

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.apply();

        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.update();

        ZoomXY centerTile = MapRasterTiles.getTileNumber(
            CENTER_GEOLOCATION.lat,
            CENTER_GEOLOCATION.lng,
            Constants.ZOOM
        );

        int offsetX = (Constants.NUM_TILES_X - 1) / 2;
        int offsetY = (Constants.NUM_TILES_Y - 1) / 2;

        beginTile = new ZoomXY(Constants.ZOOM, centerTile.x - offsetX, centerTile.y - offsetY);
        tileZone = MapRasterTiles.getTileZoneCoords(centerTile, Constants.NUM_TILES_X, Constants.NUM_TILES_Y);

        Gdx.app.log("Maps", "placeholderLoaded=" + (placeholder != null));
        for (int i = 0; i < MARKERS.size(); i++) {
            Marker mm = MARKERS.get(i);
            Vector2 p = MapRasterTiles.getPixelPosition(mm.lokacija.lat, mm.lokacija.lng, MapRasterTiles.TILE_SIZE, Constants.ZOOM, beginTile.x, beginTile.y, Constants.MAP_HEIGHT);
            Gdx.app.log("Maps", "Startup Marker[" + i + "] geo=(" + mm.lokacija.lat + "," + mm.lokacija.lng + ") pixel=" + p + " iconNull=" + (mm.icon == null));
        }

        Vector2 centerPixel = MapRasterTiles.getPixelPosition(
            CENTER_GEOLOCATION.lat,
            CENTER_GEOLOCATION.lng,
            MapRasterTiles.TILE_SIZE,
            Constants.ZOOM,
            beginTile.x,
            beginTile.y,
            Constants.MAP_HEIGHT
        );

        camera.position.set(centerPixel.x, centerPixel.y, 0);
        camera.update();
        camera.zoom = 4f;
        Gdx.app.log("Maps", "Centered camera on centerPixel=" + centerPixel + " cameraPos=" + camera.position + " zoom=" + camera.zoom);

//        zoomStartValue = camera.zoom;
//        zoomTargetValue = Math.max(MIN_ZOOM, zoomStartValue * 0.5f);
//        zoomElapsed = 0f;
//        zoomAnimating = false;
//        zoomStarted = false;
//        timeSinceStart = 0f;

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                zoomTowardsMouse(amountY);
                return true;
            }
        });
        multiplexer.addProcessor(new GestureDetector(this));
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        timeSinceStart += delta;
        if (!zoomStarted && timeSinceStart >= startupDelay) {
            zoomStarted = true;
            zoomAnimating = true;
            zoomElapsed = 0f;
            zoomStartValue = camera.zoom;
            zoomTargetValue = Math.max(MIN_ZOOM, zoomStartValue * 0.5f);
        }

        if (zoomAnimating) {
            zoomElapsed += delta;
            float t = MathUtils.clamp(zoomElapsed / zoomDuration, 0f, 1f);
            float ease = t * t * (3 - 2 * t);
            camera.zoom = MathUtils.lerp(zoomStartValue, zoomTargetValue, ease);
            if (t >= 1f) {
                camera.zoom = zoomTargetValue;
                zoomAnimating = false;
            }
        }

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        clampCamera();
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderer.drawTiles(batch, camera, tileZone, beginTile, Constants.MAP_HEIGHT);

        for (EventMarker em : EventRepository.MARKERS) {
            Vector2 pos = MapRasterTiles.getPixelPosition(
                em.location.latitude,
                em.location.longitude,
                MapRasterTiles.TILE_SIZE,
                Constants.ZOOM,
                beginTile.x,
                beginTile.y,
                Constants.MAP_HEIGHT
            );

            renderer.drawMarker(batch, camera, em.icon, pos, 128f, false);
        }
        batch.end();
    }

    private void zoomTowardsMouse(float amountY) {
        tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(tmp);
        float beforeX = tmp.x;
        float beforeY = tmp.y;

        if (amountY > 0) camera.zoom *= WHEEL_ZOOM_FACTOR;
        else camera.zoom /= WHEEL_ZOOM_FACTOR;

        camera.zoom = MathUtils.clamp(camera.zoom, MIN_ZOOM, MAX_ZOOM);

        tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(tmp);
        float afterX = tmp.x;
        float afterY = tmp.y;

        camera.position.add(beforeX - afterX, beforeY - afterY, 0);

        clampCamera();
    }

    @Override public boolean touchDown(float x, float y, int pointer, int button) { return false; }
    @Override public boolean tap(float x, float y, int count, int button) { return false; }
    @Override public boolean longPress(float x, float y) { return false; }
    @Override public boolean fling(float velocityX, float velocityY, int button) { return false; }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom);
        clampCamera();
        return true;
    }


    @Override public boolean panStop(float x, float y, int pointer, int button) { return false; }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (initialDistance >= distance) camera.zoom *= 1.02f;
        else camera.zoom /= 1.02f;
        camera.zoom = MathUtils.clamp(camera.zoom, MIN_ZOOM, MAX_ZOOM);

        clampCamera();
        return true;
    }

    @Override public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) { return false; }
    @Override public void pinchStop() { }

    @Override
    public void dispose() {
        batch.dispose();

        if (placeholder != null) placeholder.dispose();
    }

    private void clampCamera() {
        float maxZoomByWidth = Constants.MAP_WIDTH / camera.viewportWidth;
        float maxZoomByHeight = Constants.MAP_HEIGHT / camera.viewportHeight;
        float maxAllowedZoom = Math.min(maxZoomByWidth, maxZoomByHeight);

        camera.zoom = MathUtils.clamp(camera.zoom, MIN_ZOOM, Math.min(MAX_ZOOM, maxAllowedZoom));

        float halfW = camera.viewportWidth * camera.zoom / 2f;
        float halfH = camera.viewportHeight * camera.zoom / 2f;

        if (halfW * 2 >= Constants.MAP_WIDTH) {
            camera.position.x = Constants.MAP_WIDTH / 2f;
        } else {
            camera.position.x = MathUtils.clamp(camera.position.x, halfW, Constants.MAP_WIDTH - halfW);
        }

        if (halfH * 2 >= Constants.MAP_HEIGHT) {
            camera.position.y = Constants.MAP_HEIGHT / 2f;
        } else {
            camera.position.y = MathUtils.clamp(camera.position.y, halfH, Constants.MAP_HEIGHT - halfH);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        clampCamera();
    }

}
