package si.um.feri.copycats.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;

public class IntroScreen implements Screen {

    private final Game game;
    private SpriteBatch batch;
    private Texture splashTexture;
    private float elapsedTime = 0f;
    private static final float FADE_IN_DURATION = 0.5f;
    private static final float HOLD_DURATION = 0.5f;
    private static final float FADE_OUT_DURATION = 0.3f;
    private static final float TOTAL_DURATION = FADE_IN_DURATION + HOLD_DURATION + FADE_OUT_DURATION;
    private static final float MIN_DISPLAY_TIME = 0.8f;

    private float alpha = 0f;
    private float scale = 0.25f;

    private MapsScreen mapsScreen = null;
    private boolean isTransitioning = false;
    private boolean mapsScreenReady = false;

    public IntroScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        try {
            splashTexture = new Texture("copycats.png");
        } catch (Exception e) {
            Gdx.app.error("IntroScreen", "Could not load splash texture", e);
            switchToMainScreen();
            return;
        }

        Gdx.app.postRunnable(() -> {
            try {
                Gdx.app.log("IntroScreen", "Pre-loading MapsScreen...");
                mapsScreen = new MapsScreen(game);
                mapsScreenReady = true;
                Gdx.app.log("IntroScreen", "MapsScreen loaded and ready!");
            } catch (Exception e) {
                Gdx.app.error("IntroScreen", "Error loading MapsScreen", e);
            }
        });
    }

    private void switchToMainScreen() {
        if (isTransitioning) return;
        isTransitioning = true;

        Gdx.app.log("IntroScreen", "Switching to main application screen.");

        if (mapsScreen == null) {
            mapsScreen = new MapsScreen(game);
        }
        game.setScreen(mapsScreen);
    }

    @Override
    public void render(float delta) {
        elapsedTime += delta;

        if (mapsScreenReady && elapsedTime >= MIN_DISPLAY_TIME && !isTransitioning) {
            switchToMainScreen();
            return;
        }

        if (elapsedTime < FADE_IN_DURATION) {
            float progress = elapsedTime / FADE_IN_DURATION;
            alpha = Interpolation.fade.apply(progress);
            scale = Interpolation.pow2Out.apply(0.1f, 0.25f, progress);
        } else if (elapsedTime < FADE_IN_DURATION + HOLD_DURATION) {
            alpha = 1.0f;
            scale = 0.25f;
        } else if (elapsedTime < TOTAL_DURATION) {
            float progress = (elapsedTime - FADE_IN_DURATION - HOLD_DURATION) / FADE_OUT_DURATION;
            alpha = Interpolation.fade.apply(1.0f - progress);
            scale = Interpolation.pow2In.apply(0.25f, 0.4f, progress);
        } else {
            alpha = 0f;
            if (!isTransitioning) {
                switchToMainScreen();
                return;
            }
        }

        Gdx.gl.glClearColor(254f/255f, 1.0f, 217f/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (splashTexture != null && alpha > 0) {
            batch.begin();

            batch.setColor(1, 1, 1, alpha);

            float width = Gdx.graphics.getWidth();
            float height = Gdx.graphics.getHeight();
            float imgWidth = width * scale;
            float imgHeight = height * scale;
            float x = (width - imgWidth) / 2;
            float y = (height - imgHeight) / 2;

            batch.draw(splashTexture, x, y/1.5f , imgWidth, imgHeight*2);

            batch.setColor(1, 1, 1, 1);
            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (splashTexture != null) {
            splashTexture.dispose();
        }
    }
}
