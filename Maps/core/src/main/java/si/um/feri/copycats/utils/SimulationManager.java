package si.um.feri.copycats.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;
import java.util.Map;

public class SimulationManager implements Disposable {
    private Array<Character> characters;
    private Map<Integer, Integer> eventPopularity;
    private boolean isRunning = false;

    public SimulationManager() {
        characters = new Array<>();
        eventPopularity = new HashMap<>();
    }

    public void startSimulation(Array<EventMarker> activeEvents) {
        isRunning = true;
        characters.clear();
        eventPopularity.clear();

        // Random popularity for each event
        for (EventMarker em : activeEvents) {
            if (em.isCurrentlyActive) {
                int popularity = MathUtils.random(1, 20);
                eventPopularity.put(em.event.id, popularity);
            }
        }
    }

    public void stopSimulation() {
        isRunning = false;
        characters.clear();
    }

    public void update(float delta) {
        if (!isRunning) return;

        // Update existing characters
        for (int i = characters.size - 1; i >= 0; i--) {
            Character c = characters.get(i);
            c.update(delta);

            if (c.shouldRemove()) {
                characters.removeIndex(i);
            }
        }
    }

    public Array<Character> getCharacters() {
        return characters;
    }

    public Map<Integer, Integer> getEventPopularity() {
        return eventPopularity;
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void dispose() {
        characters.clear();
    }
}
