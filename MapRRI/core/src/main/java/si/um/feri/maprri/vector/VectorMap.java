package si.um.feri.maprri.vector;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;

import si.um.feri.maprri.api.ApiClient;

public class VectorMap extends ApplicationAdapter {

    private VectorMapRenderer map;
    OrthographicCamera camera;

    float zoomSpeed = 0.02f;

    @Override
    public void create() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0, 0, 0);
        camera.update();

        map = new VectorMapRenderer(camera, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        ApiClient.fetchLocations(locs -> {
            map.setLocations(locs);

            ApiClient.fetchEvents(evs -> {
                map.setEvents(evs);
                System.out.println("Loaded " + evs.size() + " events");
            });
        });
    }

    @Override
    public void render() {
        handleInput();
        map.clampScale();
        map.clampOffset();

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        map.render();
        if (Gdx.input.justTouched()) {
            map.handleClick(Gdx.input.getX(), Gdx.input.getY());
        }

    }

    private void handleInput() {
        float moveSpeed = 50f / map.scale;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  map.offsetX -= moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) map.offsetX += moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    map.offsetY += moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  map.offsetY -= moveSpeed;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) map.scale *= (1 + zoomSpeed);
        if (Gdx.input.isKeyPressed(Input.Keys.S)) map.scale *= (1 - zoomSpeed);
    }

    @Override
    public void resize(int width, int height) {
        map.viewport.update(width, height);
    }

    @Override
    public void dispose() {
        map.dispose();
    }
}
