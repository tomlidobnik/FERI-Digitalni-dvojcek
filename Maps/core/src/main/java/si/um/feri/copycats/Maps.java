package si.um.feri.copycats;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;
import java.util.stream.Collectors;

import si.um.feri.copycats.api.EventDto;
import si.um.feri.copycats.api.EventRepository;
import si.um.feri.copycats.api.LocationRepository;
import si.um.feri.copycats.render.MapRenderer;
import si.um.feri.copycats.ui.EventFilterPanel;
import si.um.feri.copycats.ui.EventPopup;
import si.um.feri.copycats.utils.Constants;
import si.um.feri.copycats.utils.EventMarker;
import si.um.feri.copycats.utils.Geolocation;
import si.um.feri.copycats.utils.MapRasterTiles;
import si.um.feri.copycats.utils.MarkerIconRegistry;
import si.um.feri.copycats.utils.ZoomXY;

public class Maps extends ApplicationAdapter implements GestureDetector.GestureListener {

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private MapRenderer renderer;

    private Stage stage;
    private Skin skin;
    private Skin skin2;
    private EventPopup eventPopup;

    private ZoomXY beginTile;
    private ZoomXY[] tileZone;

    private final Vector3 tmp = new Vector3();

    private static final float MIN_ZOOM = 0.05f;
    private static final float MAX_ZOOM = 5.0f;
    private static final float WHEEL_ZOOM_FACTOR = 1.15f;

    private final Geolocation CENTER = new Geolocation(46.561396, 15.643631);
    private EventFilterPanel filterPanel;
    private Array<EventMarker> visibleMarkers;


    @Override
    public void create() {
        batch = new SpriteBatch();
        renderer = new MapRenderer();
        MarkerIconRegistry.load();

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.apply();

        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("skin/cloud-form-ui.json"));
        skin2 = new Skin(Gdx.files.internal("skin2/expee-ui.json"));
        eventPopup = new EventPopup(skin2);
        stage.addActor(eventPopup);

        visibleMarkers = new Array<>();

        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(stage);
        mux.addProcessor(new GestureDetector(this));
        mux.addProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                zoomTowardsMouse(amountY);
                return true;
            }
        });
        Gdx.input.setInputProcessor(mux);

        // Tile setup
        ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER.lat, CENTER.lng, Constants.ZOOM);
        int offsetX = (Constants.NUM_TILES_X - 1) / 2;
        int offsetY = (Constants.NUM_TILES_Y - 1) / 2;

        beginTile = new ZoomXY(Constants.ZOOM, centerTile.x - offsetX, centerTile.y - offsetY);
        tileZone = MapRasterTiles.getTileZoneCoords(centerTile, Constants.NUM_TILES_X, Constants.NUM_TILES_Y);

        Vector2 centerPixel = MapRasterTiles.getPixelPosition(
            CENTER.lat, CENTER.lng, MapRasterTiles.TILE_SIZE, Constants.ZOOM, beginTile.x, beginTile.y, Constants.MAP_HEIGHT
        );

        camera.position.set(centerPixel.x, centerPixel.y, 0);
        camera.zoom = 3.5f;
        camera.update();

        LocationRepository.load(() -> {
            EventRepository.load(() -> {
                Gdx.app.postRunnable(() -> {
                    visibleMarkers.clear();
                    visibleMarkers.addAll(EventRepository.MARKERS);

                    Array<EventDto> allEvents = new Array<>();
                    for (EventMarker em : EventRepository.MARKERS) {
                        allEvents.add(em.event);
                    }

                    filterPanel = new EventFilterPanel(
                        skin2,
                        stage,
                        allEvents,
                        this::updateMarkers
                    );

                    Gdx.app.log("MAP", "Markers loaded: " + visibleMarkers.size);
                });
            });
        });
    }



    @Override
    public void render() {

        float delta = Gdx.graphics.getDeltaTime();

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        clampCamera();
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        renderer.drawTiles(batch, camera, tileZone, beginTile, Constants.MAP_HEIGHT);

        for (EventMarker em : visibleMarkers) {
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

        stage.act(delta);
        stage.draw();
    }

    // Marker click
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        tmp.set(x, y, 0);
        camera.unproject(tmp);

        for (EventMarker em : visibleMarkers) {
            Vector2 pos = MapRasterTiles.getPixelPosition(
                em.location.latitude,
                em.location.longitude,
                MapRasterTiles.TILE_SIZE,
                Constants.ZOOM,
                beginTile.x,
                beginTile.y,
                Constants.MAP_HEIGHT
            );

            float half = 64f;
            if (tmp.x >= pos.x - half && tmp.x <= pos.x + half &&
                tmp.y >= pos.y - half && tmp.y <= pos.y + half) {

                eventPopup.show(em.event);
                return true;
            }
        }
        return false;
    }

    //Filtering
    private void updateMarkers(Array<EventDto> filteredEvents) {
        visibleMarkers.clear();

        for (EventMarker em : EventRepository.MARKERS) {
            if (filteredEvents.contains(em.event, false)) {
                visibleMarkers.add(em);
            }
        }
    }


    // Camera
    private void clampCamera() {
        float halfW = camera.viewportWidth * camera.zoom / 2f;
        float halfH = camera.viewportHeight * camera.zoom / 2f;

        camera.position.x = MathUtils.clamp(
            camera.position.x, halfW, Constants.MAP_WIDTH - halfW
        );
        camera.position.y = MathUtils.clamp(
            camera.position.y, halfH, Constants.MAP_HEIGHT - halfH
        );
    }

    private void zoomTowardsMouse(float amountY) {
        tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(tmp);

        if (amountY > 0) camera.zoom *= WHEEL_ZOOM_FACTOR;
        else camera.zoom /= WHEEL_ZOOM_FACTOR;

        camera.zoom = MathUtils.clamp(camera.zoom, MIN_ZOOM, MAX_ZOOM);
    }

    // Gestures
    @Override public boolean pan(float x, float y, float dx, float dy) {
        camera.translate(-dx * camera.zoom, dy * camera.zoom);
        return true;
    }

    @Override public boolean zoom(float initialDistance, float distance) {
        camera.zoom *= initialDistance > distance ? 1.02f : 0.98f;
        camera.zoom = MathUtils.clamp(camera.zoom, MIN_ZOOM, MAX_ZOOM);
        return true;
    }

    @Override public boolean tap(float x, float y, int count, int button) { return false; }
    @Override public boolean longPress(float x, float y) { return false; }
    @Override public boolean fling(float vx, float vy, int button) { return false; }
    @Override public boolean panStop(float x, float y, int p, int b) { return false; }
    @Override public boolean pinch(Vector2 a, Vector2 b, Vector2 c, Vector2 d) { return false; }
    @Override public void pinchStop() {}

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        skin.dispose();
    }
}
