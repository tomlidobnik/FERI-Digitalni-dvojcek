package si.um.feri.copycats.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Color;

public class IconTestApp extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture icon;
    private OrthographicCamera camera;
    private AnimationSequence sequence;

    @Override
    public void create() {
        batch = new SpriteBatch();
        try {
            icon = new Texture(Gdx.files.internal("placeholder.png"));
        } catch (Exception e) {
            Gdx.app.error("IconTestApp", "Failed to load placeholder.png: " + e.getMessage());
            icon = null;
        }

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        camera.update();

        Texture[] people = new Texture[5];
        int texSize = 64;
        Color[] cols = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN };
        for (int i = 0; i < people.length; i++) {
            Pixmap pm = new Pixmap(texSize, texSize, Format.RGBA8888);
            pm.setColor(0f, 0f, 0f, 0f);
            pm.fill();
            pm.setColor(cols[i % cols.length]);
            pm.fillCircle(texSize/2, texSize/2, texSize/2 - 4);
            people[i] = new Texture(pm);
            pm.dispose();
        }

        sequence = AnimationSequence.createFromTextures(people, true);
        sequence.setSize(64f);
        sequence.setRadius(90f);
        sequence.setSpeed(0.5f);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                com.badlogic.gdx.math.Vector3 tmp = new com.badlogic.gdx.math.Vector3(screenX, screenY, 0);
                camera.unproject(tmp);
                sequence.setCenter(tmp.x, tmp.y);
                return true;
            }
        });
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.12f, 0.12f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (icon != null) batch.draw(icon, 8, 8, Math.min(64, icon.getWidth()), Math.min(64, icon.getHeight()));

        if (sequence != null) {
            sequence.update(Gdx.graphics.getDeltaTime());
            sequence.render(batch);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (icon != null) icon.dispose();
        if (sequence != null) sequence.dispose();
    }
}
