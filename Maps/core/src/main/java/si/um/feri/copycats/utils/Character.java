package si.um.feri.copycats.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;

public class Character {
    public Vector2 position;
    public Vector2 targetPosition;
    public Vector2 velocity;
    public Texture texture;
    public float speed;
    public boolean arrived = false;
    public float timeAlive = 0f;
    public float maxLifeTime = 10f;

    public Character(Vector2 startPos, Vector2 targetPos, Texture texture) {
        this.position = new Vector2(startPos);
        this.targetPosition = new Vector2(targetPos);
        this.texture = texture;
        this.speed = MathUtils.random(20f, 60f); // Random speed

        Vector2 direction = new Vector2(targetPos).sub(startPos).nor();
        this.velocity = new Vector2(direction).scl(speed);
    }

    public void update(float delta) {
        if (arrived) {
            timeAlive += delta;
            return;
        }

        // Move towards target
        Vector2 direction = new Vector2(targetPosition).sub(position).nor();
        velocity.set(direction).scl(speed);
        position.add(velocity.x * delta, velocity.y * delta);

        // Check if arrived
        if (position.dst2(targetPosition) < 100f) {
            arrived = true;
        }

        timeAlive += delta;
    }

    public boolean shouldRemove() {
        return arrived && timeAlive > 2f; // Stay visible for 2 seconds after arrival
    }
}
