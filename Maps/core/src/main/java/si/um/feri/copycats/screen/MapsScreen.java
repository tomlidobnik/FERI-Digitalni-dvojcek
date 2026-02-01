package si.um.feri.copycats.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import si.um.feri.copycats.Maps;
public class MapsScreen implements Screen {

    private final Maps maps;
    private final Game game;

    public MapsScreen(Game game) {
        this.game = game;
        this.maps = new Maps();
    }

    @Override
    public void show() {
        maps.create();
    }

    @Override
    public void render(float delta) {
        maps.render();
    }

    @Override
    public void resize(int width, int height) {
        maps.resize(width, height);
    }

    @Override
    public void pause() {
        maps.pause();
    }

    @Override
    public void resume() {
        maps.resume();
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        maps.dispose();
    }
}
