package si.um.feri.maprri.vector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import si.um.feri.maprri.api.EventDto;
import si.um.feri.maprri.api.LocationDto;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.badlogic.gdx.utils.viewport.ScreenViewport;
public class VectorMapRenderer {
    private float time = 0f;
    private float eventWindowAlpha = 0f;
    private boolean eventWindowVisible = false;
    private float targetZoom = 1f;
    private EventDto hoveredEvent = null;
    private float targetScale = 1f;


    private SpriteBatch batch;
    private Texture markerTexture;
    private BitmapFont font;

    private Stage stage;
    private Skin skin;
    private Window eventWindow;

    private Label titleLabel;
    private Label descLabel;
    private Label timeLabel;
    private Label tagLabel;
    private float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
    private float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

    public float minScale;
    public float maxScale;

    public final ShapeRenderer shapeRenderer;


    private final List<float[]> roads = new ArrayList<>();
    private final List<float[]> buildings = new ArrayList<>();

    private final Map<Integer, LocationDto> locationById = new HashMap<>();
    private final List<EventDto> events = new ArrayList<>();

    private final Map<EventDto, Rectangle> eventHitboxes = new HashMap<>();
    private EventDto selectedEvent = null;

    public OrthographicCamera camera;
    public Viewport viewport;

    public float scale = 0.5f;
    public float offsetX = 0f, offsetY = 0f;

    private final Color activeColorA = new Color(0.2f, 0.6f, 1f, 1f);
    private final Color activeColorB = new Color(0.2f, 0f, 0.6f, 1f);
    private final Color tempColor = new Color();

    private String activeTagFilter = "ALL";

    private SelectBox<String> tagSelectBox;
    private Window filterWindow;

    public VectorMapRenderer(OrthographicCamera camera, float screenWidth, float screenHeight) {
        this.camera = camera;
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        markerTexture = new Texture(Gdx.files.internal("images/marker.png"));
        font = new BitmapFont();
        font.setColor(Color.WHITE);


        viewport = new FitViewport(screenWidth, screenHeight, camera);
        viewport.apply();

        loadGeoJSON("maribor_center.geojson");
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        createEventWindow();
        createFilterUI();

    }

    public void setLocations(List<LocationDto> locations) {
        locationById.clear();
        for (LocationDto l : locations) {
            locationById.put(l.id, l);
        }
    }

    public void setEvents(List<EventDto> evs) {
        events.clear();
        events.addAll(evs);
    }

    private void loadGeoJSON(String filename) {
        try {
            InputStream is = Gdx.files.internal(filename).read();
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            JSONObject geojson = new JSONObject(text);
            JSONArray features = geojson.getJSONArray("features");

            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                String type = geometry.getString("type");

                if (type.equals("LineString")) {
                    roads.add(parseCoordinates(geometry.getJSONArray("coordinates")));
                } else if (type.equals("Polygon")) {
                    buildings.add(parsePolygon(geometry.getJSONArray("coordinates")));
                } else if (type.equals("MultiPolygon")) {
                    JSONArray polys = geometry.getJSONArray("coordinates");
                    for (int j = 0; j < polys.length(); j++) {
                        buildings.add(parsePolygon(polys.getJSONArray(j)));
                    }
                }
            }

            offsetX = (minX + maxX) / 2f;
            offsetY = (minY + maxY) / 2f;

            float mapWidth  = maxX - minX;
            float mapHeight = maxY - minY;

            float scaleX = viewport.getWorldWidth() / mapWidth;
            float scaleY = viewport.getWorldHeight() / mapHeight;

            minScale = Math.min(scaleX, scaleY);
            maxScale = minScale * 20f;
            scale = minScale * 1.2f;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float[] parseCoordinates(JSONArray coords) {
        float[] verts = new float[coords.length() * 2];

        for (int i = 0; i < coords.length(); i++) {
            JSONArray c = coords.getJSONArray(i);
            Vector2 p = lonLatToMeters(c.getDouble(0), c.getDouble(1));

            verts[i * 2] = p.x;
            verts[i * 2 + 1] = p.y;

            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }
        return verts;
    }

    private float[] parsePolygon(JSONArray polyCoords) {
        return parseCoordinates(polyCoords.getJSONArray(0));
    }

    public static Vector2 lonLatToMeters(double lon, double lat) {
        double x = lon * 20037508.34 / 180.0;
        double y = Math.log(Math.tan((90.0 + lat) * Math.PI / 360.0)) / (Math.PI / 180.0);
        y = y * 20037508.34 / 180.0;
        return new Vector2((float) x, (float) y);
    }

    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        time += delta;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.RED);
        for (float[] verts : buildings) draw(verts);

        shapeRenderer.setColor(Color.DARK_GRAY);
        for (float[] verts : roads) draw(verts);

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        eventHitboxes.clear();
        float baseSize = 48f;
        for (EventDto e : events) {
            if (!shouldRenderEvent(e)) continue;
            if (e.location_fk == null) continue;

            LocationDto loc = locationById.get(e.location_fk);
            if (loc == null) continue;

            Vector2 p = lonLatToMeters(loc.longitude, loc.latitude);
            float x = (p.x - offsetX) * scale;
            float y = (p.y - offsetY) * scale;

            float size = baseSize;
            boolean active = isEventActive(e);

            Color drawColor = Color.WHITE;
            if (active) {
                float pulse = (float) (Math.sin(TimeUtils.nanoTime() / 1_000_000_000.0 * 3f) * 0.15f + 1f);
                size *= pulse;
                float lerp = (float) (Math.sin(TimeUtils.nanoTime() / 1_000_000_000.0 * 2f) * 0.5f + 0.5f);
                tempColor.set(activeColorA).lerp(activeColorB, lerp);
                drawColor = tempColor;
            }

            eventHitboxes.put(e, new Rectangle(x - size / 2f, y, size, size));
        }

        Vector2 mouseWorld = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        hoveredEvent = null;
        for (Map.Entry<EventDto, Rectangle> entry : eventHitboxes.entrySet()) {
            if (entry.getValue().contains(mouseWorld)) {
                hoveredEvent = entry.getKey();
                break;
            }
        }

        for (EventDto e : events) {
            if (!shouldRenderEvent(e)) continue;
            if (e.location_fk == null) continue;

            LocationDto loc = locationById.get(e.location_fk);
            if (loc == null) continue;

            Vector2 p = lonLatToMeters(loc.longitude, loc.latitude);
            float x = (p.x - offsetX) * scale;
            float y = (p.y - offsetY) * scale;

            float size = baseSize;
            boolean active = isEventActive(e);
            Color drawColor = Color.WHITE;

            if (active) {
                float pulse = (float) (Math.sin(TimeUtils.nanoTime() / 1_000_000_000.0 * 3f) * 0.15f + 1f);
                size *= pulse;
                float lerp = (float) (Math.sin(TimeUtils.nanoTime() / 1_000_000_000.0 * 2f) * 0.5f + 0.5f);
                tempColor.set(activeColorA).lerp(activeColorB, lerp);
                drawColor = tempColor;
            }

            if (e == hoveredEvent) {
                size *= 1.3f;
                drawColor = Color.YELLOW;
            }

            batch.setColor(drawColor);
            batch.draw(markerTexture, x - size / 2f, y, size, size);
            batch.setColor(Color.WHITE);
        }

        batch.end();

        stage.act(delta);
        if (eventWindowVisible) {
            eventWindowAlpha = Math.min(1f, eventWindowAlpha + delta * 3f);
            eventWindow.getColor().a = eventWindowAlpha;
        }
        stage.draw();
    }



    private boolean shouldRenderEvent(EventDto e) {
        if (activeTagFilter.equals("ALL")) return true;
        if (e.tag == null) return false;
        return e.tag.equalsIgnoreCase(activeTagFilter);
    }

    private void draw(float[] verts) {
        for (int i = 0; i < verts.length - 2; i += 2) {
            shapeRenderer.line(
                    (verts[i] - offsetX) * scale,
                    (verts[i + 1] - offsetY) * scale,
                    (verts[i + 2] - offsetX) * scale,
                    (verts[i + 3] - offsetY) * scale
            );
        }
    }

    public void clampOffset() {
        float halfW = viewport.getWorldWidth() / 2f / scale;
        float halfH = viewport.getWorldHeight() / 2f / scale;

        offsetX = Math.max(minX + halfW, Math.min(maxX - halfW, offsetX));
        offsetY = Math.max(minY + halfH, Math.min(maxY - halfH, offsetY));
    }

    public void clampScale() {
        scale = Math.max(minScale, Math.min(maxScale, scale));
    }

    public void handleClick(float screenX, float screenY) {
        if (hoveredEvent != null) {
            selectedEvent = hoveredEvent;
            showEvent(selectedEvent);
        }
    }

    public void handleHover(float screenX, float screenY) {
        Vector2 world = viewport.unproject(new Vector2(screenX, screenY));

        hoveredEvent = null;

        for (Map.Entry<EventDto, Rectangle> entry : eventHitboxes.entrySet()) {
            if (entry.getValue().contains(world)) {
                hoveredEvent = entry.getKey();
                break;
            }
        }
    }

    private void showEvent(EventDto e) {
        titleLabel.setText("Title: " + e.title);
        descLabel.setText("Description: " + e.description);
        timeLabel.setText("From: " + e.start_date + "  To: " + e.end_date);
        tagLabel.setText("Tag: " + (e.tag != null ? e.tag : "-"));

        eventWindow.pack();
        eventWindow.setVisible(true);
        eventWindowAlpha = 0f;
        eventWindowVisible = true;
    }


    private void createEventWindow() {
        eventWindow = new Window("Event", skin);
        eventWindow.setSize(420, 200);
        eventWindow.setPosition(20, 20);
        eventWindow.setVisible(false);
        eventWindow.setMovable(true);

        titleLabel = new Label("", skin);
        descLabel = new Label("", skin);
        timeLabel = new Label("", skin);
        tagLabel = new Label("", skin);

        descLabel.setWrap(true);

        Table content = new Table(skin);
        content.left().top();
        content.add(titleLabel).left().row();
        content.add(descLabel).width(380).left().padTop(5).row();
        content.add(timeLabel).left().padTop(5).row();
        content.add(tagLabel).left().padTop(5).row();

        eventWindow.add(content).expand().fill();
        stage.addActor(eventWindow);
    }
    private void createFilterUI() {
        filterWindow = new Window("Filter", skin);
        filterWindow.setSize(200, 80);
        filterWindow.setMovable(false);

        filterWindow.setPosition(
                stage.getViewport().getWorldWidth() - 220,
                stage.getViewport().getWorldHeight() - 100
        );

        tagSelectBox = new SelectBox<>(skin);
        tagSelectBox.setItems("ALL", "fun", "education", "sports");
        tagSelectBox.setSelected("ALL");

        tagSelectBox.addListener(event -> {
            activeTagFilter = tagSelectBox.getSelected();
            return false;
        });

        filterWindow.add(new Label("Show events:", skin)).left().row();
        filterWindow.add(tagSelectBox).width(160);

        stage.addActor(filterWindow);
    }


    private boolean isEventActive(EventDto e) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime start = LocalDateTime.parse(e.start_date);
        LocalDateTime end   = LocalDateTime.parse(e.end_date);

        return !now.isBefore(start) && !now.isAfter(end);
    }


    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        markerTexture.dispose();
        font.dispose();

    }
}
