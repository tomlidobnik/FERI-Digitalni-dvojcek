package si.um.feri.copycats.api;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public class LocationRepository {

    public static ObjectMap<Integer, LocationDto> LOCATIONS = new ObjectMap<>();

    public static void load(Runnable onDone) {
        ApiService.get("/location/all", new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse res) {
                String json = res.getResultAsString();
                LocationDto[] locations = new Json().fromJson(LocationDto[].class, json);

                Gdx.app.postRunnable(() -> {
                    for (LocationDto l : locations) {
                        LOCATIONS.put(l.id, l);
                    }
                    onDone.run();
                });
            }

            @Override public void failed(Throwable t) {
                Gdx.app.error("API", "Failed to load locations", t);
            }

            @Override public void cancelled() {}
        });
    }
}
