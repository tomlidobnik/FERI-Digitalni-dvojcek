package si.um.feri.copycats.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import si.um.feri.copycats.api.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class EventPopup extends Window {

    private EventDto currentEvent;

    private final Label nameValue;
    private final Label startTimeValue;
    private final Label endTimeValue;
    private final Label publicValue;
    private final Label tagValue;
    private final Label descriptionValue;
    private TextField titleField;
    private TextArea descField;
    private TextField startField;
    private TextField endField;


    public EventPopup(Skin skin) {
        super("Podrobnosti o dogodku", skin);

        setModal(true);
        setMovable(false);
        setResizable(false);

        defaults().pad(6).left();
        getTitleLabel().setAlignment(Align.center);

        float valueScale = 1.4f;
        float labelScale = 1.1f;

        Label nameLabel = new Label("Dogodek:", skin);
        nameLabel.setFontScale(labelScale);
        nameValue = new Label("", skin);
        nameValue.setFontScale(valueScale);

        Label startLabel = new Label("Od:", skin);
        startLabel.setFontScale(labelScale);
        startTimeValue = new Label("", skin);
        startTimeValue.setFontScale(valueScale);

        Label endLabel = new Label("Do:", skin);
        endLabel.setFontScale(labelScale);
        endTimeValue = new Label("", skin);
        endTimeValue.setFontScale(valueScale);

        Label publicLabel = new Label("Vidnost:", skin);
        publicLabel.setFontScale(labelScale);
        publicValue = new Label("", skin);
        publicValue.setFontScale(valueScale);

        Label tagLabel = new Label("Kategorija:", skin);
        tagLabel.setFontScale(labelScale);
        tagValue = new Label("", skin);
        tagValue.setFontScale(valueScale);

        Label descLabel = new Label("Opis:", skin);
        descLabel.setFontScale(labelScale);
        descriptionValue = new Label("", skin);
        descriptionValue.setFontScale(valueScale);
        descriptionValue.setWrap(true);

        TextButton editBtn = new TextButton("Uredi", skin);
        editBtn.pad(10, 20, 10, 20);
        editBtn.addListener(e -> {
            if (e.toString().equals("touchDown")) {
                openEditDialog();
                return true;
            }
            return false;
        });

        TextButton closeBtn = new TextButton("Zapri", skin);
        closeBtn.pad(10, 20, 10, 20);
        closeBtn.addListener(e -> {
            if (e.toString().equals("touchDown")) {
                hide();
                return true;
            }
            return false;
        });

        add(nameLabel); add(nameValue).growX().row();
        add(startLabel); add(startTimeValue).row();
        add(endLabel); add(endTimeValue).row();
        add(publicLabel); add(publicValue).row();
        add(tagLabel); add(tagValue).row();
        add(descLabel).top(); add(descriptionValue).width(420).row();

        Table buttons = new Table();
        buttons.add(editBtn).padRight(10);
        buttons.add(closeBtn);

        add(buttons).colspan(2).center().padTop(15);

        pack();
        setVisible(false);
    }

    public void show(EventDto event) {
        this.currentEvent = event;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm:ss");
        nameValue.setText(event.title);
        startTimeValue.setText(LocalDateTime.parse(event.start_date).format(fmt));
        endTimeValue.setText(LocalDateTime.parse(event.end_date).format(fmt));
        publicValue.setText(event.isPublic ? "Javen" : "Zaseben");

        if (Objects.equals(event.tag, "education")) tagValue.setText("Izobrazba");
        else if (Objects.equals(event.tag, "fun")) tagValue.setText("Zabava");
        else if (Objects.equals(event.tag, "sports")) tagValue.setText("Sport");
        else tagValue.setText("/");

        descriptionValue.setText(event.description);

        pack();
        setPosition(
            (Gdx.graphics.getWidth() - getWidth()) / 2f,
            (Gdx.graphics.getHeight() - getHeight()) / 2f
        );

        getColor().a = 0f;
        setVisible(true);
        toFront();
        addAction(Actions.fadeIn(0.2f));
    }

    private void openEditDialog() {
        Dialog d = new Dialog("Uredi dogodek", getSkin()) {
            @Override
            protected void result(Object object) {
                if (Boolean.TRUE.equals(object)) {
                    updateEvent(
                        titleField.getText(),
                        descField.getText(),
                        startField.getText(),
                        endField.getText()
                    );
                }
            }
        };

        titleField = new TextField(currentEvent.title, getSkin());
        descField = new TextArea(currentEvent.description, getSkin());
        startField = new TextField(currentEvent.start_date, getSkin());
        endField = new TextField(currentEvent.end_date, getSkin());

        d.getContentTable().add("Naslov").left().row();
        d.getContentTable().add(titleField).width(420).row();

        d.getContentTable().add("Opis").left().padTop(8).row();
        d.getContentTable().add(descField).width(420).height(120).row();

        d.getContentTable().add("Začetek").left().padTop(8).row();
        d.getContentTable().add(startField).width(420).row();

        d.getContentTable().add("Konec").left().padTop(8).row();
        d.getContentTable().add(endField).width(420).row();

        d.button("Shrani", true);
        d.button("Prekliči", false);

        d.show(getStage());
    }

    private void updateEvent(String title, String desc, String start, String end) {
        AuthService.getToken(() -> {

            EventUpdateRequest req = new EventUpdateRequest();
            req.id = currentEvent.id;
            req.title = title;
            req.description = desc;
            req.start_date = start;
            req.end_date = end;
            req.location_fk = currentEvent.location_fk;
            req.tag = currentEvent.tag;

            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);

            String payload = json.toJson(req);
            Gdx.app.log("EVENT", "Sending JSON: " + payload);

            Net.HttpRequest http = new HttpRequestBuilder()
                .newRequest()
                .method(Net.HttpMethods.PUT)
                .url("http://0.0.0.0:8000/api/event/update")
                .header("Authorization", AuthService.getBearer())
                .header("Content-Type", "application/json")
                .content(payload)
                .build();

            Gdx.net.sendHttpRequest(http, new Net.HttpResponseListener() {
                @Override
                public void handleHttpResponse(Net.HttpResponse res) {
                    Gdx.app.log("EVENT", "Raw response: " + res.getResultAsString());
                    Gdx.app.log("EVENT", "Event updated successfully");

                    currentEvent.title = title;
                    currentEvent.description = desc;
                    currentEvent.start_date = start;
                    currentEvent.end_date = end;

                    show(currentEvent);
                }

                @Override
                public void failed(Throwable t) {
                    Gdx.app.error("EVENT", "Update failed", t);
                }

                @Override public void cancelled() {}
            });
        });
    }



    public void hide() {
        addAction(Actions.sequence(
            Actions.fadeOut(0.15f),
            Actions.visible(false)
        ));
    }
}
