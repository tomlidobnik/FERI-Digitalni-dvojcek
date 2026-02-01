package si.um.feri.copycats.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import si.um.feri.copycats.api.AuthService;
import si.um.feri.copycats.api.LocationDto;

import java.util.function.Consumer;

public class CreateEventDialog {

    public final Table dialogTable;
    public final TextField titleField;
    public final TextArea descriptionField;
    public final TextField startDateField;
    public final TextField endDateField;
    public final SelectBox<String> visibilitySelectBox;
    public final SelectBox<String> tagSelectBox;
    public final TextButton createEventButton;
    public final TextButton cancelEventButton;

    private float pickedLat;
    private float pickedLng;

    private Consumer<Void> onEventCreated;

    public CreateEventDialog(Skin skin, Stage stage, Consumer<Void> onEventCreated) {
        this.onEventCreated = onEventCreated;

        dialogTable = new Table(skin);
        dialogTable.setBackground("window");
        dialogTable.pad(20);
        dialogTable.top();

        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.get("default", Label.LabelStyle.class));
        labelStyle.fontColor = Color.WHITE;
        Label dialogTitle = new Label("Dodajanje dogodka", labelStyle);
        dialogTitle.setFontScale(1.5f);
        dialogTitle.setColor(Color.WHITE);

        dialogTable.add(dialogTitle).padTop(-12).padBottom(30).row();

        // Title field
        Label titleLabel = new Label("Naslov:", skin);
        titleLabel.setFontScale(1.1f);
        titleField = new TextField("", skin);
        dialogTable.add(titleLabel).left().padBottom(5).row();
        dialogTable.add(titleField).width(300).padBottom(15).row();

        // Description
        Label descLabel = new Label("Opis:", skin);
        descLabel.setFontScale(1.1f);
        descriptionField = new TextArea("", skin);
        dialogTable.add(descLabel).left().padBottom(5).row();
        dialogTable.add(descriptionField).width(300).height(100).padBottom(15).row();

        // Start and End dates
        Label startLabel = new Label("Začetek (YYYY-MM-DDTHH:MM:SS):", skin);
        startDateField = new TextField("", skin);
        Label endLabel = new Label("Konec (YYYY-MM-DDTHH:MM:SS):", skin);
        endDateField = new TextField("", skin);

        dialogTable.add(startLabel).left().padBottom(5).row();
        dialogTable.add(startDateField).width(300).padBottom(15).row();
        dialogTable.add(endLabel).left().padBottom(5).row();
        dialogTable.add(endDateField).width(300).padBottom(15).row();

        // Visibility select box
        Label visLabel = new Label("Vidnost:", skin);
        visibilitySelectBox = new SelectBox<>(skin);
        visibilitySelectBox.setItems("Javno", "Zasebno");
        dialogTable.add(visLabel).left().padBottom(5).row();
        dialogTable.add(visibilitySelectBox).width(200).padBottom(15).row();

        // Tag select box
        Label tagLabel = new Label("Kategorija:", skin);
        tagSelectBox = new SelectBox<>(skin);
        tagSelectBox.setItems("None", "Sport", "Izobrazba", "Zabava");

        dialogTable.add(tagLabel).left().padBottom(5).row();
        dialogTable.add(tagSelectBox).width(200).padBottom(25).row();

        // Buttons
        Table buttonTable = new Table();
        createEventButton = new TextButton("Ustvari", skin);
        cancelEventButton = new TextButton("Prekliči", skin);
        buttonTable.add(createEventButton).padRight(20);
        buttonTable.add(cancelEventButton);

        dialogTable.add(buttonTable).center().padBottom(10).row();

        dialogTable.pack();
        dialogTable.setPosition(
                stage.getViewport().getWorldWidth() / 2 - dialogTable.getWidth() / 2,
                stage.getViewport().getWorldHeight() / 2 - dialogTable.getHeight() / 2
        );

        dialogTable.setVisible(false);
        stage.addActor(dialogTable);

        cancelEventButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        createEventButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                createEventFlow();
            }
        });
    }

    public void show(float lat, float lng) {
        this.pickedLat = lat;
        this.pickedLng = lng;

        dialogTable.setVisible(true);

        titleField.setText("");
        descriptionField.setText("");
        startDateField.setText("");
        endDateField.setText("");
        visibilitySelectBox.setSelected("Javno");
        tagSelectBox.setSelected("None");
    }

    public void hide() {
        dialogTable.setVisible(false);
    }

    private void createEventFlow() {
        if (titleField.getText().trim().isEmpty() ||
            startDateField.getText().trim().isEmpty() ||
            endDateField.getText().trim().isEmpty()) {
            Gdx.app.log("EVENT", "Please fill title/start/end");
            return;
        }

        AuthService.getToken(() -> Gdx.app.postRunnable(this::createLocation));
    }

    private void createLocation() {
        try {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);

            String locationPayload = json.toJson(new LocationPayload(
                "User generated [RRI]",
                pickedLng,
                pickedLat
            ));

            Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
            request.setUrl("http://0.0.0.0:8000/api/location/create");
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", AuthService.getBearer());
            request.setContent(locationPayload);

            Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
                @Override
                public void handleHttpResponse(Net.HttpResponse response) {
                    Gdx.app.log("LOCATION", "Created location, fetching ID next");
                    fetchLocationId();
                }

                @Override
                public void failed(Throwable t) {
                    Gdx.app.error("LOCATION", "Failed to create location", t);
                }

                @Override
                public void cancelled() {}
            });

        } catch (Exception e) {
            Gdx.app.error("LOCATION", "Exception", e);
        }
    }

    private void fetchLocationId() {
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl("http://0.0.0.0:8000/api/location/all");
        request.setHeader("Authorization", AuthService.getBearer());

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse response) {
                String raw = response.getResultAsString();
                Json json = new Json();

                LocationDto[] locations = json.fromJson(LocationDto[].class, raw);

                int locationId = -1;
                for (LocationDto loc : locations) {
                    if (Math.abs(loc.latitude - pickedLat) < 0.00001 &&
                        Math.abs(loc.longitude - pickedLng) < 0.00001) {
                        locationId = loc.id;
                        break;
                    }
                }

                if (locationId == -1) {
                    Gdx.app.error("EVENT", "Could not find newly created location!");
                    return;
                }

                Gdx.app.log("EVENT", "Location ID found: " + locationId);
                createEvent(locationId);
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("EVENT", "Failed to fetch locations", t);
            }

            @Override
            public void cancelled() {}
        });
    }

    private void createEvent(int locationId) {
        try {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            json.setUsePrototypes(false);

            String selectedTag = tagSelectBox.getSelected();
            String tag = null;
            if ("Sport".equals(selectedTag)) tag = "sports";
            else if ("Izobrazba".equals(selectedTag)) tag = "education";
            else if ("Zabava".equals(selectedTag)) tag = "fun";
            // "None" -> null

            boolean isPublic = "Javno".equals(visibilitySelectBox.getSelected());

            EventPayload payload = new EventPayload(
                titleField.getText(),
                descriptionField.getText(),
                startDateField.getText(),
                endDateField.getText(),
                locationId,
                isPublic,
                tag
            );

            String body = "{"
                + "\"title\":\"" + payload.title + "\","
                + "\"description\":\"" + payload.description + "\","
                + "\"start_date\":\"" + payload.start_date + "\","
                + "\"end_date\":\"" + payload.end_date + "\","
                + "\"location_fk\":" + payload.location_fk + ","
                + "\"public\":" + payload.isPublic + ","
                + (payload.tag != null ? "\"tag\":\"" + payload.tag + "\"" : "\"tag\":null")
                + "}";

            Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.POST);
            request.setUrl("http://0.0.0.0:8000/api/event/create");
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", AuthService.getBearer());
            request.setContent(body);

            Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
                @Override
                public void handleHttpResponse(Net.HttpResponse response) {
                    Gdx.app.log("EVENT", "Event created successfully!");
                    Gdx.app.postRunnable(CreateEventDialog.this::hide);

                    if (onEventCreated != null) {
                        Gdx.app.postRunnable(() -> onEventCreated.accept(null));
                    }
                }

                @Override
                public void failed(Throwable t) {
                    Gdx.app.error("EVENT", "Failed to create event", t);
                }

                @Override
                public void cancelled() {}
            });

        } catch (Exception e) {
            Gdx.app.error("EVENT", "Exception while creating event", e);
        }
    }



    private static class LocationPayload {
        public String info;
        public double longitude;
        public double latitude;
        public LocationPayload(String info, double lon, double lat) { this.info=info; this.longitude=lon; this.latitude=lat; }
    }

    private static class EventPayload {
        public String title;
        public String description;
        public String start_date;
        public String end_date;
        public int location_fk;
        public boolean isPublic;
        public String tag;

        public EventPayload(String title, String desc, String start, String end, int locId, boolean pub, String tag) {
            this.title = title;
            this.description = desc;
            this.start_date = start;
            this.end_date = end;
            this.location_fk = locId;
            this.isPublic = pub;
            this.tag = tag;
        }
    }

}
