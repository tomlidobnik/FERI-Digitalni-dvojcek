package si.um.feri.copycats.utils;

import com.badlogic.gdx.graphics.Texture;
import si.um.feri.copycats.api.EventDto;
import si.um.feri.copycats.api.LocationDto;

public class EventMarker {
    public EventDto event;
    public LocationDto location;
    public MarkerType type;
    public Texture icon;
    public BreathingAnimation breathingAnimation;
    public boolean isCurrentlyActive = false;

    public EventMarker() {
        this.breathingAnimation = new BreathingAnimation(2.5f, 0.85f, 1.15f);
    }
}
