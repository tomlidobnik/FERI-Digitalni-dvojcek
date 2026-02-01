package si.um.feri.copycats.api;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public class ApiService {

    private static final String BASE_URL = "http://0.0.0.0:8000/api";

    public static void get(String endpoint, Net.HttpResponseListener listener) {
        Net.HttpRequest req = new Net.HttpRequest(Net.HttpMethods.GET);
        req.setUrl(BASE_URL + endpoint);
        req.setHeader("Accept", "application/json");
        Gdx.net.sendHttpRequest(req, listener);
    }
}

