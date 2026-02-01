package si.um.feri.copycats;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.badlogic.gdx.graphics.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import si.um.feri.copycats.api.AuthService;
import si.um.feri.copycats.api.EventDto;
import si.um.feri.copycats.api.EventRepository;
import si.um.feri.copycats.api.LocationDto;
import si.um.feri.copycats.api.LocationRepository;
import si.um.feri.copycats.render.MapRenderer;
import si.um.feri.copycats.ui.CreateEventDialog;
import si.um.feri.copycats.ui.EventFilterPanel;
import si.um.feri.copycats.ui.EventPopup;
import si.um.feri.copycats.utils.Constants;
import si.um.feri.copycats.utils.EventMarker;
import si.um.feri.copycats.utils.Geolocation;
import si.um.feri.copycats.utils.MapRasterTiles;
import si.um.feri.copycats.utils.MarkerIconRegistry;
import si.um.feri.copycats.utils.MarkerType;
import si.um.feri.copycats.utils.SimulationManager;
import si.um.feri.copycats.utils.ZoomXY;
import si.um.feri.copycats.utils.Character;
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

    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 10.0f;
    private static final float WHEEL_ZOOM_FACTOR = 1.1f;

    private final Geolocation CENTER = new Geolocation(46.561396, 15.643631);
    private EventFilterPanel filterPanel;
    private Array<EventMarker> visibleMarkers;
    private TextButton addEventButton;

    private boolean pickingLocation = false;
    private Table pickLocationBanner;
    private Vector2 pickedLatLng = null;
    private Vector2 pickedPixel = null;

    private final Map<EventDto, Rectangle> eventHitboxes = new HashMap<>();
    private final Array<EventMarker> markers = new Array<>();

    private CreateEventDialog createEventDialog;
    private SimulationManager simulationManager;
    private TextButton simulationButton;
    private boolean simulationActive = false;
    private float simulationSpawnTimer = 0f;
    private Texture characterPlaceholderTexture;
    private Array<Texture> characterTextures;


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
        updateSkinWithSlovenianFonts();
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
        mux.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (pickingLocation && keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
                    exitPickLocationMode();
                    Gdx.app.log("PICK", "Pick-location cancelled via ESC");
                    return true;
                }
                return false;
            }
        });

        Gdx.input.setInputProcessor(mux);

        addEventButton = new TextButton("+", skin2);
        addEventButton.getLabel().setFontScale(2f);
        addEventButton.setSize(60, 60);

        addEventButton.setPosition(
            stage.getViewport().getWorldWidth() - 80,
            20
        );

        addEventButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                enterPickLocationMode();
            }
        });


        stage.addActor(addEventButton);

        pickLocationBanner = new Table(skin2);
        pickLocationBanner.setBackground("window");

        Label bannerLabel = new Label("Izberi lokacijo", skin2);
        bannerLabel.setFontScale(1f);

        pickLocationBanner.add(bannerLabel).pad(10, 20, 10, 20);
        pickLocationBanner.pack();

        pickLocationBanner.setPosition(
            20,
            stage.getViewport().getWorldHeight() - pickLocationBanner.getHeight() - 20
        );

        pickLocationBanner.setVisible(false);
        stage.addActor(pickLocationBanner);

        simulationManager = new SimulationManager();

        // Sim button
        simulationButton = new TextButton("Simulacija", skin2);
        simulationButton.setSize(120, 60);
        simulationButton.setPosition(
            stage.getViewport().getWorldWidth() - 210,
            20
        );

        simulationButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleSimulation();
            }
        });

        stage.addActor(simulationButton);
        loadCharacterTextures();


        createEventDialog = new CreateEventDialog(
            skin2,
            stage,
            event -> ((Maps) Gdx.app.getApplicationListener()).reloadEverything()
        );

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

        updateMarkerAnimations(delta);

        // Update sim
        if (simulationActive) {
            updateSimulation(delta);
            drawSimulationDebug();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        renderer.drawTiles(batch, camera, tileZone, beginTile, Constants.MAP_HEIGHT);

        // Draw events
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

            if (em.isCurrentlyActive) {
                float scale = em.breathingAnimation.getCurrentScale();
                renderer.drawMarker(batch, camera, em.icon, pos, 128f, false, scale);
            } else {
                renderer.drawMarker(batch, camera, em.icon, pos, 128f, false);
            }
        }

        // Draw simulation characters
        if (simulationActive) {
            drawSimulationCharacters();
            drawEventDebugPositions();

        }

        batch.end();

        stage.act(delta);
        stage.draw();
    }

    private void drawSimulationCharacters() {
        for (Character character : simulationManager.getCharacters()) {
            if (character.texture != null) {
                float size = 24f;
                batch.draw(
                    character.texture,
                    character.position.x - size/2,
                    character.position.y - size/2,
                    size,
                    size
                );
            }
        }
    }

    private void loadCharacterTextures() {
        characterTextures = new Array<>();

        try {
            for (int i = 1; i <= 3; i++) {
                String path = "characters/character" + i + ".png";
                if (Gdx.files.internal(path).exists()) {
                    Texture texture = new Texture(Gdx.files.internal(path));
                    characterTextures.add(texture);
                    Gdx.app.log("SIM", "Loaded character texture: " + path);
                } else {
                    Gdx.app.log("SIM", "Character texture not found: " + path);
                }
            }

            if (characterTextures.size == 0) {
                Gdx.app.log("SIM", "No character images found, generating default");
                createCharacterTextures();
            } else {
                characterPlaceholderTexture = characterTextures.first();
            }

        } catch (Exception e) {
            Gdx.app.error("SIM", "Failed to load character textures", e);
            createCharacterTextures();
        }
    }

    private void createCharacterTextures() {
        characterTextures = new Array<>();

        for (int i = 0; i < 3; i++) {
            characterTextures.add(createCharacterTexture(i));
        }

        characterPlaceholderTexture = characterTextures.first();
    }

    private Texture createCharacterTexture(int variant) {
        int size = 32;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();

        Color[] colors = {
            new Color(0.9f, 0.3f, 0.3f, 1f),
            new Color(0.3f, 0.9f, 0.3f, 1f),
            new Color(0.3f, 0.3f, 0.9f, 1f)
        };

        Color bodyColor = colors[variant % colors.length];

        pixmap.setColor(bodyColor);
        pixmap.fillCircle(size/2, size/4, size/8);
        pixmap.drawLine(size/2, size/4 + size/8, size/2, size * 3/4);
        pixmap.drawLine(size/2 - 6, size/2, size/2 + 6, size/2);
        pixmap.drawLine(size/2, size * 3/4, size/2 - 5, size - 4);
        pixmap.drawLine(size/2, size * 3/4, size/2 + 5, size - 4);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void updateSimulation(float delta) {
        simulationManager.update(delta);

        simulationSpawnTimer += delta;
        if (simulationSpawnTimer >= 0.5f) {
            simulationSpawnTimer = 0f;

            int totalCharsSpawned = 0;

            for (EventMarker em : visibleMarkers) {
                if (!em.isCurrentlyActive) continue;

                Integer popularity = simulationManager.getEventPopularity().get(em.event.id);
                if (popularity == null) continue;
                int charsToSpawn = MathUtils.random(0, popularity / 4);
                totalCharsSpawned += charsToSpawn;

                for (int i = 0; i < charsToSpawn; i++) {
                    spawnCharacterForEvent(em);
                }
            }

            if (totalCharsSpawned > 0) {
                Gdx.app.debug("SIM", "Spawned " + totalCharsSpawned + " characters");
                Gdx.app.debug("SIM", "Total characters: " + simulationManager.getCharacters().size);
            }
        }
    }

    // Marker click
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        tmp.set(x, y, 0);
        camera.unproject(tmp);

        if (pickingLocation) {
            Geolocation geo = MapRasterTiles.getLatLngFromPixel(
                tmp.x,
                tmp.y,
                MapRasterTiles.TILE_SIZE,
                Constants.ZOOM,
                beginTile.x,
                beginTile.y,
                Constants.MAP_HEIGHT
            );

            pickedLatLng = new Vector2((float) geo.lat, (float) geo.lng);

            Gdx.app.log("PICK",
                "Picked location -> lat=" + geo.lat + ", lng=" + geo.lng
            );

            exitPickLocationMode();

            eventPopup.hide();
            createEventDialog.show((float) geo.lat, (float) geo.lng);

            camera.position.set(tmp.x, tmp.y, 0);
            camera.update();

            return true;
        }

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

    // Pick location mode
    private void enterPickLocationMode() {
        pickingLocation = true;
        pickLocationBanner.setVisible(true);
        pickLocationBanner.toFront();

        Gdx.graphics.setSystemCursor(
            com.badlogic.gdx.graphics.Cursor.SystemCursor.Crosshair
        );

        Gdx.app.log("MAP", "Entered pick-location mode");
    }


    private void exitPickLocationMode() {
        pickingLocation = false;
        pickLocationBanner.setVisible(false);

        Gdx.graphics.setSystemCursor(
            com.badlogic.gdx.graphics.Cursor.SystemCursor.Arrow
        );

        Gdx.app.log("MAP", "Exited pick-location mode");
    }

    // refresh after addition
    public void reloadEverything() {
        LocationRepository.load(() -> {
            EventRepository.load(() -> {
                Gdx.app.postRunnable(() -> {
                    visibleMarkers.clear();
                    visibleMarkers.addAll(EventRepository.MARKERS);

                    Array<EventDto> allEvents = new Array<>();
                    for (EventMarker em : EventRepository.MARKERS) {
                        allEvents.add(em.event);
                    }

                    if (filterPanel != null) {
                        filterPanel.setEvents(allEvents);
                    }

                    Gdx.app.log("MAP", "Everything reloaded: " + visibleMarkers.size);
                });
            });
        });
    }

    // Currently active animation
    private boolean isEventCurrentlyActive(EventDto event) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime start = LocalDateTime.parse(event.start_date, formatter);
            LocalDateTime end = LocalDateTime.parse(event.end_date, formatter);
            LocalDateTime now = LocalDateTime.now();

            return !now.isBefore(start) && !now.isAfter(end);
        } catch (Exception e) {
            Gdx.app.error("Maps", "Error parsing dates for event: " + event.title, e);
            return false;
        }
    }
    private void updateMarkerAnimations(float delta) {
        for (EventMarker em : visibleMarkers) {
            em.isCurrentlyActive = isEventCurrentlyActive(em.event);
            if (em.isCurrentlyActive) {
                em.breathingAnimation.update(delta);
            }
        }
    }

    // Simulation
    private void toggleSimulation() {
        simulationActive = !simulationActive;

        if (simulationActive) {
            Array<EventMarker> activeEvents = new Array<>();
            for (EventMarker em : visibleMarkers) {
                if (em.isCurrentlyActive) {
                    activeEvents.add(em);
                }
            }
            simulationManager.startSimulation(activeEvents);
            simulationButton.setText("Zaustavi");
            simulationSpawnTimer = 0f;

            for (EventMarker em : activeEvents) {
                Integer pop = simulationManager.getEventPopularity().get(em.event.id);
                Gdx.app.debug("SIM", "Event '" + em.event.title + "' popularity: " + pop);

                Vector2 eventPos = MapRasterTiles.getPixelPosition(
                    em.location.latitude,
                    em.location.longitude,
                    MapRasterTiles.TILE_SIZE,
                    Constants.ZOOM,
                    beginTile.x,
                    beginTile.y,
                    Constants.MAP_HEIGHT
                );
                Gdx.app.debug("SIM", "Event '" + em.event.title + "' position: " + eventPos);
                Gdx.app.debug("SIM", "Event location: lat=" + em.location.latitude + ", lng=" + em.location.longitude);
            }

            Gdx.app.debug("SIM", "MAP_WIDTH: " + Constants.MAP_WIDTH + ", MAP_HEIGHT: " + Constants.MAP_HEIGHT);
            Gdx.app.debug("SIM", "Camera position: " + camera.position);
            Gdx.app.debug("SIM", "Camera zoom: " + camera.zoom);
            Gdx.app.log("SIM", "Simulation started with " + activeEvents.size + " active events");
        } else {
            simulationManager.stopSimulation();
            simulationButton.setText("Simulacija");
            simulationSpawnTimer = 0f;
            Gdx.app.log("SIM", "Simulation stopped");
        }
    }

    private void drawSimulationDebug() {
        int charCount = simulationManager.getCharacters().size;

        if (charCount > 0) {
            Gdx.app.debug("SIM", "Drawing " + charCount + " characters");

            if (charCount > 0) {
                Character firstChar = simulationManager.getCharacters().get(0);
                Gdx.app.debug("SIM", "First char at: " + firstChar.position +
                    " Target: " + firstChar.targetPosition +
                    " Arrived: " + firstChar.arrived);
            }
        }
    }
    private void spawnCharacterForEvent(EventMarker eventMarker) {
        Vector2 eventPos = MapRasterTiles.getPixelPosition(
            eventMarker.location.latitude,
            eventMarker.location.longitude,
            MapRasterTiles.TILE_SIZE,
            Constants.ZOOM,
            beginTile.x,
            beginTile.y,
            Constants.MAP_HEIGHT
        );

        float spawnDistance = MathUtils.random(200f, 400f);
        float spawnAngle = MathUtils.random(0f, 360f) * MathUtils.degRad;

        float spawnX = eventPos.x + (float)Math.cos(spawnAngle) * spawnDistance;
        float spawnY = eventPos.y + (float)Math.sin(spawnAngle) * spawnDistance;

        spawnX = MathUtils.clamp(spawnX, 0f, Constants.MAP_WIDTH);
        spawnY = MathUtils.clamp(spawnY, 0f, Constants.MAP_HEIGHT);

        Vector2 spawnPos = new Vector2(spawnX, spawnY);

        Texture characterTex;
        if (characterTextures != null && characterTextures.size > 0) {
            characterTex = characterTextures.get(MathUtils.random(characterTextures.size - 1));
        } else {
            characterTex = characterPlaceholderTexture;
        }

        Character character = new Character(spawnPos, eventPos, characterTex);
        simulationManager.getCharacters().add(character);
    }

    private void drawEventDebugPositions() {
        Color originalColor = batch.getColor();

        for (EventMarker em : visibleMarkers) {
            if (!em.isCurrentlyActive) continue;

            Vector2 pos = MapRasterTiles.getPixelPosition(
                em.location.latitude,
                em.location.longitude,
                MapRasterTiles.TILE_SIZE,
                Constants.ZOOM,
                beginTile.x,
                beginTile.y,
                Constants.MAP_HEIGHT
            );

//            batch.setColor(Color.RED);
//
//            float crossSize = 20f;
//            batch.draw(characterPlaceholderTexture, pos.x - crossSize/2, pos.y - 2, crossSize, 4);
//            batch.draw(characterPlaceholderTexture, pos.x - 2, pos.y - crossSize/2, 4, crossSize);

            Gdx.app.debug("SIM-DEBUG", "Event '" + em.event.title + "' at screen pos: " + pos);
        }

        batch.setColor(Color.WHITE);
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

    // Slo font
    private void updateSkinWithSlovenianFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/OpenSans-Regular.ttf"));

            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
            param.size = 16;
            param.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ščžŠČŽ";

            BitmapFont sloFont = generator.generateFont(param);

            Label.LabelStyle defaultStyle = skin2.get(Label.LabelStyle.class);
            if (defaultStyle != null) {
                if (defaultStyle.font != null) {
                    defaultStyle.font.dispose();
                }
                defaultStyle.font = sloFont;
            }

            skin2.add("font", sloFont, BitmapFont.class);

            FreeTypeFontGenerator.FreeTypeFontParameter titleParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
            titleParam.size = 24;
            titleParam.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ščžŠČŽ";

            BitmapFont titleFont = generator.generateFont(titleParam);
            skin2.add("title", titleFont, BitmapFont.class);

            generator.dispose();

            Gdx.app.log("FONT", "Updated skin with Slovenian fonts");

        } catch (Exception e) {
            Gdx.app.error("FONT", "Failed to update fonts", e);
        }
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

        if (addEventButton != null) {
            addEventButton.setPosition(
                stage.getViewport().getWorldWidth() - 80,
                20
            );
        }

        if (simulationButton != null) {
            simulationButton.setPosition(
                stage.getViewport().getWorldWidth() - 210,
                20
            );
        }

        if (pickLocationBanner != null) {
            pickLocationBanner.setPosition(
                20,
                stage.getViewport().getWorldHeight()
                    - pickLocationBanner.getHeight() - 20
            );
        }

        if (filterPanel != null && filterPanel.getTable() != null) {
            Table t = filterPanel.getTable();
            t.setPosition(
                stage.getViewport().getWorldWidth() - t.getWidth() - 20,
                stage.getViewport().getWorldHeight() - t.getHeight() - 20
            );
        }

        if (createEventDialog != null) {
            Table t = createEventDialog.dialogTable;
            t.setPosition(
                stage.getViewport().getWorldWidth()/2 - t.getWidth()/2,
                stage.getViewport().getWorldHeight()/2 - t.getHeight()/2
            );
        }

    }


    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        skin.dispose();
        skin2.dispose();
        if (simulationManager != null) {
            simulationManager.dispose();
        }
        if (characterTextures != null) {
            for (Texture tex : characterTextures) {
                tex.dispose();
            }
            characterTextures.clear();
        }
        if (characterPlaceholderTexture != null) {
            characterPlaceholderTexture.dispose();
        }
    }
}
