package si.um.feri.maprri.vector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import si.um.feri.maprri.api.EventDto;
import si.um.feri.maprri.api.LocationDto;

public class VectorMapRenderer {

    private SpriteBatch batch;
    private Texture markerTexture;

    private float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
    private float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

    public float minScale;
    public float maxScale;

    public final ShapeRenderer shapeRenderer;

    private final List<float[]> roads = new ArrayList<>();
    private final List<float[]> buildings = new ArrayList<>();

    private final Map<Integer, LocationDto> locationById = new HashMap<>();
    private final List<EventDto> events = new ArrayList<>();

    public OrthographicCamera camera;
    public Viewport viewport;

    public float scale = 0.5f;
    public float offsetX = 0f, offsetY = 0f;

    public VectorMapRenderer(OrthographicCamera camera, float screenWidth, float screenHeight) {
        this.camera = camera;
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        markerTexture = new Texture(Gdx.files.internal("images/marker.png"));

        viewport = new FitViewport(screenWidth, screenHeight, camera);
        viewport.apply();

        loadGeoJSON("maribor_center.geojson");
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
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Buildings
        shapeRenderer.setColor(Color.RED);
        for (float[] verts : buildings) draw(verts);

        // Roads
        shapeRenderer.setColor(Color.DARK_GRAY);
        for (float[] verts : roads) draw(verts);

        // Events
        shapeRenderer.end();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (EventDto e : events) {
            if (e.location_fk == null) continue;

            LocationDto loc = locationById.get(e.location_fk);
            if (loc == null) continue;

            Vector2 p = lonLatToMeters(loc.longitude, loc.latitude);

            float x = (p.x - offsetX) * scale;
            float y = (p.y - offsetY) * scale;

            float size = 48f; // marker size
            batch.draw(
                    markerTexture,
                    x - size / 2f,
                    y,
                    size,
                    size
            );
        }

        batch.end();

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

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        markerTexture.dispose();
    }
}
