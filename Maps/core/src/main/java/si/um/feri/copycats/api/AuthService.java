package si.um.feri.copycats.api;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class AuthService {

    private static String token;

    public static void getToken(Runnable onSuccess) {
        if (token != null) {
            onSuccess.run();
            return;
        }

        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        LoginRequest body = new LoginRequest();
        body.username = "janez";
        body.password = "1234";

        String payload = json.toJson(body);
        Gdx.app.log("AUTH", "Sending JSON: " + payload);

        Net.HttpRequest request = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url("http://0.0.0.0:8000/api/user/token")
            .header("Content-Type", "application/json")
            .content(payload)
            .build();

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                String raw = response.getResultAsString();
                Gdx.app.log("AUTH", "Raw response: " + raw);

                if (!raw.contains("\"token\"")) {
                    Gdx.app.error("AUTH", "Token missing in response");
                    return;
                }

                TokenResponse tr = json.fromJson(TokenResponse.class, raw);
                token = tr.token;

                Gdx.app.log("AUTH", "Token OK");
                onSuccess.run();
            }

            @Override public void failed(Throwable t) {
                Gdx.app.error("AUTH", "Token request failed", t);
            }

            @Override public void cancelled() {}
        });
    }

    public static String getBearer() {
        return "Bearer " + token;
    }
}
