package si.um.feri.maprri.vector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VectorMapRenderer {
    private float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
    private float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

    public final ShapeRenderer shapeRenderer;
    private final List<float[]> roads = new ArrayList<>();
    private final List<float[]> buildings = new ArrayList<>();

    public OrthographicCamera camera;
    public Viewport viewport;

    // Camera offset & scale for zoom/pan
    public float scale = 0.5f;
    public float offsetX = 0f, offsetY = 0f;

    public VectorMapRenderer(OrthographicCamera camera, float screenWidth, float screenHeight) {
        shapeRenderer = new ShapeRenderer();
        // Setup camera + viewport
        this.camera = camera;
        viewport = new FitViewport(screenWidth, screenHeight, camera);
        viewport.apply();

        loadGeoJSON("maribor_center.geojson");
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

            // Center the map on load
            offsetX = (minX + maxX) / 2f;
            offsetY = (minY + maxY) / 2f;

            System.out.println("Map centered at offset (" + offsetX + ", " + offsetY + "), scale=" + scale);

            System.out.println("Loaded: " + roads.size() + " roads, " + buildings.size() + " buildings");

            System.out.println("Bounds:");
            System.out.println("X range: " + minX + " → " + maxX);
            System.out.println("Y range: " + minY + " → " + maxY);

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

            // track bounds
            if (p.x < minX) minX = p.x;
            if (p.x > maxX) maxX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.y > maxY) maxY = p.y;
        }
        return verts;
    }

    private float[] parsePolygon(JSONArray polyCoords) {
        // Polygons in GeoJSON are an array of linear rings
        return parseCoordinates(polyCoords.getJSONArray(0));
    }

    // Web Mercator projection (EPSG:3857)
    public static Vector2 lonLatToMeters(double lon, double lat) {
        double x = lon * 20037508.34 / 180.0;
        double y = Math.log(Math.tan((90.0 + lat) * Math.PI / 360.0)) / (Math.PI / 180.0);
        y = y * 20037508.34 / 180.0;
        return new Vector2((float) x, (float) y);
    }

    public void render() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Draw buildings
        shapeRenderer.setColor(Color.RED);
        for (float[] verts : buildings) {
            drawPolygon(verts);
        }

        // Draw roads
        shapeRenderer.setColor(Color.DARK_GRAY);
        for (float[] verts : roads) {
            drawLineString(verts);
        }

        shapeRenderer.end();
    }

    private void drawPolygon(float[] verts) {
        for (int i = 0; i < verts.length - 2; i += 2) {
            float x1 = (verts[i] - offsetX) * scale;
            float y1 = (verts[i + 1] - offsetY) * scale;
            float x2 = (verts[i + 2] - offsetX) * scale;
            float y2 = (verts[i + 3] - offsetY) * scale;
            shapeRenderer.line(x1, y1, x2, y2);
        }
    }

    private void drawLineString(float[] verts) {
        for (int i = 0; i < verts.length - 2; i += 2) {
            float x1 = (verts[i] - offsetX) * scale;
            float y1 = (verts[i + 1] - offsetY) * scale;
            float x2 = (verts[i + 2] - offsetX) * scale;
            float y2 = (verts[i + 3] - offsetY) * scale;
            shapeRenderer.line(x1, y1, x2, y2);
        }
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
