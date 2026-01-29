package si.um.feri.copycats.api;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import si.um.feri.copycats.utils.EventMarker;
import si.um.feri.copycats.utils.MarkerIconRegistry;
import si.um.feri.copycats.utils.MarkerType;

public class EventRepository {

    public static final Array<EventMarker> MARKERS = new Array<>();

    public static void load(Runnable onDone) {
        ApiService.get("/event/all", new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse res) {
                String json = res.getResultAsString();
                EventDto[] events = new Json().fromJson(EventDto[].class, json);

                Gdx.app.postRunnable(() -> {
                    MARKERS.clear(); // clear old markers
                    for (EventDto e : events) {
                        LocationDto loc = LocationRepository.LOCATIONS.get(e.location_fk);
                        if (loc == null) continue;

                        EventMarker marker = new EventMarker();
                        marker.event = e;
                        marker.location = loc;
                        marker.type = MarkerType.fromTag(e.tag);
                        marker.icon = MarkerIconRegistry.get(marker.type);

                        MARKERS.add(marker);
                    }
                    if (onDone != null) onDone.run();
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("API", "Failed to load events", t);
            }

            @Override
            public void cancelled() {}
        });
    }

}
