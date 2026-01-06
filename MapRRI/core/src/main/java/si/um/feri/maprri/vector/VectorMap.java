package si.um.feri.maprri.vector;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class VectorMap extends ApplicationAdapter {
    private VectorMapRenderer map;
    OrthographicCamera camera;
    ShapeRenderer shapeRenderer;

    float zoomSpeed = 0.02f;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        map = new VectorMapRenderer(camera, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0, 0, 0);
        camera.update();
    }

    @Override
    public void render() {
        handleInput();

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        map.shapeRenderer.setProjectionMatrix(camera.combined);
        map.render();
    }

    private void handleInput() {
        float moveSpeed = 5 / map.scale;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  map.offsetX -= moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) map.offsetX += moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    map.offsetY += moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  map.offsetY -= moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.A))  map.scale *= (1 + zoomSpeed);
        if (Gdx.input.isKeyPressed(Input.Keys.S)) map.scale *= (1 - zoomSpeed);
    }

    @Override
    public void resize(int width, int height) {
        map.viewport.update(width, height);
    }

    @Override
    public void dispose() {
        map.dispose();
        shapeRenderer.dispose();
    }
}
