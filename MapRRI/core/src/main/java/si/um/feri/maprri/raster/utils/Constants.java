package si.um.feri.maprri.raster.utils;

import com.badlogic.gdx.Gdx;

import si.um.feri.maprri.raster.utils.MapRasterTiles;

public class Constants {
    public static final int NUM_TILES = 3;
    public static final int ZOOM = 15;
    public static final int MAP_WIDTH = MapRasterTiles.TILE_SIZE * NUM_TILES;
    public static final int MAP_HEIGHT = si.um.feri.maprri.raster.utils.MapRasterTiles.TILE_SIZE * NUM_TILES;
    public static final int HUD_WIDTH = Gdx.graphics.getWidth();
    public static final int HUD_HEIGHT = Gdx.graphics.getHeight();
}
