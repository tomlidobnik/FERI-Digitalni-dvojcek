package si.um.feri.copycats.utils;

import com.badlogic.gdx.math.MathUtils;

public class BreathingAnimation {
    private float time = 0f;
    private float speed = 3f; // Animation speed
    private float minScale = 0.8f; // Minimum scale
    private float maxScale = 1.2f; // Maximum scale
    private float pulseOffset = 0f;

    public BreathingAnimation() {
        this.pulseOffset = MathUtils.random(0f, MathUtils.PI2);
    }

    public BreathingAnimation(float speed, float minScale, float maxScale) {
        this.speed = speed;
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.pulseOffset = MathUtils.random(0f, MathUtils.PI2);
    }

    public void update(float delta) {
        time += delta;
    }

    public float getCurrentScale() {
        float sineValue = (MathUtils.sin(time * speed + pulseOffset) + 1f) / 2f; // Convert to 0-1 range
        return minScale + sineValue * (maxScale - minScale);
    }

    public void reset() {
        time = 0f;
    }
}
