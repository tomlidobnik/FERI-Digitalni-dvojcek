package si.um.feri.copycats.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import si.um.feri.copycats.api.EventDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class EventPopup extends Window {

    private final Label nameValue;
    private final Label startTimeValue;
    private final Label endTimeValue;
    private final Label publicValue;
    private final Label tagValue;
    private final Label descriptionValue;

    public EventPopup(Skin skin) {
        super("Podrobnosti o dogodkušžŠČ", skin);

        setModal(true);
        setMovable(false);
        setResizable(false);

        pad(20);
        defaults().pad(6).left();

        getTitleLabel().setAlignment(Align.center);
        getTitleTable().getCell(getTitleLabel()).padBottom(5);


        // Labels
        Label nameLabel = new Label("Dogodek:", skin);
        nameValue = new Label("", skin);

        Label startTimeLabel = new Label("Od:", skin);
        startTimeValue = new Label("", skin);

        Label endTimeLabel = new Label("Do:", skin);
        endTimeValue = new Label("", skin);

        Label publicLabel = new Label("Vidnost:", skin);
        publicValue = new Label("", skin);

        Label tagLabel = new Label("Kategorija:", skin);
        tagValue = new Label("", skin);

        Label descLabel = new Label("Opis:", skin);
        descriptionValue = new Label("", skin);
        descriptionValue.setWrap(true);

        TextButton closeBtn = new TextButton("Zapri", skin);
        closeBtn.addListener(e -> {
            if (e.toString().equals("touchDown")) {
                hide();
                return true;
            }
            return false;
        });

        // Layout
        add(nameLabel);
        add(nameValue).growX().row();

        add(startTimeLabel);
        add(startTimeValue).row();

        add(endTimeLabel);
        add(endTimeValue).row();

        add(publicLabel);
        add(publicValue).row();

        add(tagLabel);
        add(tagValue).row();

        add(descLabel).top();
        add(descriptionValue).width(420).row();

        add(closeBtn).colspan(2).center().padTop(15);

        pack();
        setVisible(false);
    }

    public void show(EventDto event) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M.yyyy', 'HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(event.start_date);
        LocalDateTime end = LocalDateTime.parse(event.end_date);

        nameValue.setText(event.title);
        startTimeValue.setText(start.format(formatter));
        endTimeValue.setText(end.format(formatter));
        publicValue.setText(event.isPublic ? "Javen" : "Zaseben");
        if (Objects.equals(event.tag, "education")) {
            tagValue.setText("Izobrazba");
        } else if (Objects.equals(event.tag, "fun")) {
            tagValue.setText("Zabava");
        } else if (Objects.equals(event.tag, "sports")){
            tagValue.setText("Sport");
        } else {
            tagValue.setText("/");
        }
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

    public void hide() {
        addAction(Actions.sequence(
            Actions.fadeOut(0.15f),
            Actions.visible(false)
        ));
    }
}
