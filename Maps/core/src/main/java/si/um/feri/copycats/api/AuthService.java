package si.um.feri.copycats.api;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public class AuthService {

    private static String token;

    /** Fetch token if not already cached */
    public static void getToken(Runnable onSuccess) {
        if (token != null) {
            onSuccess.run();
            return;
        }

        Json json = new Json();

        // Build JSON body
        ObjectMap<String, String> body = new ObjectMap<>();
        body.put("username", "janez");
        body.put("password", "1234");

        // Build HTTP POST request
        Net.HttpRequest http = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url("http://0.0.0.0:8000/api/user/token")
            .header("Content-Type", "application/json")
            .content(json.toJson(body)) // Convert ObjectMap to proper JSON
            .build();

        Gdx.net.sendHttpRequest(http, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                String raw = response.getResultAsString();
                Gdx.app.log("AUTH", "Raw response: " + raw);

                try {
                    TokenResponse res = json.fromJson(TokenResponse.class, raw);
                    token = res.token; // Extract the "token" field
                    Gdx.app.log("AUTH", "Token fetched: " + token);
                    onSuccess.run();
                } catch (Exception e) {
                    Gdx.app.error("AUTH", "Token parse failed", e);
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("AUTH", "Token fetch failed", t);
            }

            @Override
            public void cancelled() {}
        });
    }

    /** Get current bearer string */
    public static String getBearer() {
        return "Bearer " + token;
    }
}
