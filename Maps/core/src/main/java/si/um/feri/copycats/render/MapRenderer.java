package si.um.feri.copycats.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import si.um.feri.copycats.utils.MapRasterTiles;
import si.um.feri.copycats.utils.ZoomXY;

public class MapRenderer {

    public MapRenderer() { }

    public void drawTiles(SpriteBatch spriteBatch, OrthographicCamera camera, ZoomXY[] tileZone, ZoomXY beginTile, int mapHeight) {
        if (spriteBatch == null || camera == null || tileZone == null || beginTile == null) return;

        spriteBatch.setProjectionMatrix(camera.combined);

        int available = 0;
        int missing = 0;

        for (ZoomXY tz : tileZone) {
            if (tz == null) continue;
            Texture t = MapRasterTiles.getRasterTileCachedAsync(tz.zoom, tz.x, tz.y);
            float px = (tz.x - beginTile.x) * MapRasterTiles.TILE_SIZE;
            float py = mapHeight - ((tz.y - beginTile.y + 1) * MapRasterTiles.TILE_SIZE);

            if (t != null) {
                available++;
                spriteBatch.draw(t, px, py, MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE);
            }
        }

        if (available == 0) {
            Gdx.app.log("MapRenderer", "No tiles available yet, drawing fallback for all tiles (missing=" + missing + ")");
        }
    }

    public void drawMarker(SpriteBatch spriteBatch, OrthographicCamera camera, Texture icon, Vector2 pos, float baseSize, boolean scaleWithCamera) {
        if (icon == null || pos == null || spriteBatch == null || camera == null) return;

        float size = baseSize;
        if (scaleWithCamera) size /= camera.zoom;

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.draw(icon, pos.x - size / 2f, pos.y - size / 2f, size, size);
    }

    public void drawMarker(SpriteBatch spriteBatch, OrthographicCamera camera, Texture icon, Vector2 pos, float baseSize, boolean scaleWithCamera, float scale) {
        if (icon == null || pos == null || spriteBatch == null || camera == null) return;

        float size = baseSize * scale;
        if (scaleWithCamera) size /= camera.zoom;

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.draw(icon, pos.x - size / 2f, pos.y - size / 2f, size, size);
    }

}
