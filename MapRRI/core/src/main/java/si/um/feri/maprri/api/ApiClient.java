package si.um.feri.maprri.api;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ApiClient {

    private static final String BASE_URL = "http://0.0.0.0:8000/api";

    private static final Json json = new Json();

    public static void fetchEvents(Consumer<List<EventDto>> callback) {
        Net.HttpRequest req = new Net.HttpRequest(Net.HttpMethods.GET);
        req.setUrl(BASE_URL + "/event/all");

        Gdx.net.sendHttpRequest(req, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                String body = response.getResultAsString();
                JsonValue root = new JsonReader().parse(body);

                List<EventDto> events = new ArrayList<>();
                for (JsonValue e : root) {
                    EventDto ev = new EventDto();
                    ev.id = e.getInt("id");
                    ev.title = e.getString("title");
                    ev.description = e.getString("description");
                    ev.start_date = e.getString("start_date");
                    ev.end_date = e.getString("end_date");
                    ev.location_fk = e.get("location_fk").isNull() ? null : e.getInt("location_fk");
                    ev.publicEvent = e.getBoolean("public");
                    ev.tag = e.isNull() ? null : e.getString("tag");
                    events.add(ev);
                }

                Gdx.app.postRunnable(() -> callback.accept(events));
            }

            @Override
            public void failed(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void cancelled() {}
        });
    }

    public static void fetchLocations(Consumer<List<LocationDto>> callback) {
        Net.HttpRequest req = new Net.HttpRequest(Net.HttpMethods.GET);
        req.setUrl(BASE_URL + "/location/all");

        Gdx.net.sendHttpRequest(req, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                String body = response.getResultAsString();
                JsonValue root = new JsonReader().parse(body);

                List<LocationDto> locations = new ArrayList<>();
                for (JsonValue l : root) {
                    LocationDto loc = new LocationDto();
                    loc.id = l.getInt("id");
                    loc.info = l.getString("info");
                    loc.longitude = l.getDouble("longitude");
                    loc.latitude = l.getDouble("latitude");
                    locations.add(loc);
                }

                Gdx.app.postRunnable(() -> callback.accept(locations));
            }

            @Override
            public void failed(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void cancelled() {}
        });
    }
}
