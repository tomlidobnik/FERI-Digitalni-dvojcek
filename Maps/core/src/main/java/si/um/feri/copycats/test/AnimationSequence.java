package si.um.feri.copycats.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class AnimationSequence {
    public float centerX;
    public float centerY;
    private Texture texture;

    private float radius = 60f;
    private float speed = 1.0f;
    private float size = 64f;

    private float angle = 0f;

    private final String folderName;
    private boolean ownsTexture = false;
    private Texture[] textures = null;
    private boolean ownsTextures = false;
    private int count = 0;

    private AnimationSequence(String folderName) {
        this.folderName = folderName;
    }

    public static AnimationSequence createFromTextures(Texture[] textures, boolean owns) {
        AnimationSequence seq = new AnimationSequence("runtime");
        if (textures != null && textures.length > 0) {
            seq.textures = textures;
            seq.count = textures.length;
        }
        seq.ownsTextures = owns;
        seq.centerX = Gdx.graphics.getWidth() / 2f;
        seq.centerY = Gdx.graphics.getHeight() / 2f;
        return seq;
    }

    public static AnimationSequence load(String folderName, Texture fallback) {
        AnimationSequence seq = new AnimationSequence(folderName);

        FileHandle fh = Gdx.files.internal("animations/" + folderName + "/");
        try {
            if (fh.exists()) {
                FileHandle[] files = fh.list();
                for (FileHandle f : files) {
                    if (f.name().toLowerCase().endsWith(".png") || f.name().toLowerCase().endsWith(".jpg")) {
                        seq.texture = new Texture(f);
                        seq.ownsTexture = true;
                        break;
                    }
                }

                FileHandle meta = Gdx.files.internal("animations/" + folderName + "/anim.json");
                if (meta.exists()) {
                    JsonValue root = new JsonReader().parse(meta);
                    seq.centerX = root.has("centerX") ? root.getFloat("centerX") : 0f;
                    seq.centerY = root.has("centerY") ? root.getFloat("centerY") : 0f;
                    seq.radius = root.has("radius") ? root.getFloat("radius") : seq.radius;
                    seq.speed = root.has("speed") ? root.getFloat("speed") : seq.speed;
                    seq.size = root.has("size") ? root.getFloat("size") : seq.size;
                }
            }
        } catch (Exception e) {
            Gdx.app.error("AnimationSequence", "Failed to load animation folder '" + folderName + "': " + e.getMessage());
        }

        if (seq.texture == null && fallback != null) {
            seq.texture = fallback;
            seq.ownsTexture = false;
            seq.textures = new Texture[] { fallback };
            seq.count = 1;
            seq.centerX = Gdx.graphics.getWidth() / 2f;
            seq.centerY = Gdx.graphics.getHeight() / 2f;
        }

        return seq;
    }

    public void update(float delta) {
        angle += delta * speed * (float)(Math.PI * 2.0);
    }

    public void render(SpriteBatch batch) {
        if ((textures == null || count == 0) && texture == null) return;

        if (textures != null && count > 0) {
            for (int i = 0; i < count; i++) {
                float phase = angle + (float)i * (2f * (float)Math.PI / (float)count);
                float x = centerX + (float)Math.cos(phase) * radius;
                float y = centerY + (float)Math.sin(phase) * radius;
                float half = size / 2f;
                Texture t = textures[i % count];
                if (t != null) batch.draw(t, x - half, y - half, size, size);
            }
            return;
        }

        float x = centerX + (float)Math.cos(angle) * radius;
        float y = centerY + (float)Math.sin(angle) * radius;
        float half = size / 2f;
        batch.draw(texture, x - half, y - half, size, size);
    }

    public void dispose() {
        if (textures != null && ownsTextures) {
            for (Texture t : textures) if (t != null) t.dispose();
        }
        if (texture != null && ownsTexture) texture.dispose();
    }

    public void setCenter(float cx, float cy) {
        this.centerX = cx;
        this.centerY = cy;
    }

    public void setSize(float s) { this.size = s; }
    public void setRadius(float r) { this.radius = r; }
    public void setSpeed(float s) { this.speed = s; }
}
