package si.um.feri.copycats.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;


import java.util.function.Consumer;

import si.um.feri.copycats.api.EventDto;

public class EventFilterPanel {

    private final Table table;
    private final SelectBox<String> tagSelect;
    private final CheckBox publicOnlyCheck;

    private final Array<EventDto> allEvents;
    private final Consumer<Array<EventDto>> onFilterChanged;

    public EventFilterPanel(
        Skin skin,
        Stage stage,
        Array<EventDto> allEvents,
        Consumer<Array<EventDto>> onFilterChanged
    ) {
        this.allEvents = allEvents;
        this.onFilterChanged = onFilterChanged;

        table = new Table(skin);
        table.setBackground("window");
        table.pad(10);
        table.top().right();

        // Title
        Label.LabelStyle labelStyle = new Label.LabelStyle(skin.get("default", Label.LabelStyle.class));
        labelStyle.fontColor = Color.WHITE;

        Label title = new Label("Filtriranje", labelStyle);
        title.setFontScale(1.2f);
        table.add(title).center().padBottom(10).row();


        // Tag select
        tagSelect = new SelectBox<>(skin);
        tagSelect.setItems("Vsi", "Sport", "Zabava", "Izobrazba");
        Label categoryLabel = new Label("Kategorija:", skin);
        categoryLabel.setFontScale(1.3f);
        table.add(categoryLabel).left().padBottom(5).row();
        table.add(tagSelect).width(150).padBottom(10).row();

        // Public only check
        publicOnlyCheck = new CheckBox("Samo javni dogodki", skin);
        table.add(publicOnlyCheck).left().row();

        // Listeners
        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                applyFilter();
            }
        };
        tagSelect.addListener(listener);
        publicOnlyCheck.addListener(listener);

        stage.addActor(table);

        table.pack();
        table.setPosition(
            Gdx.graphics.getWidth() - table.getWidth() - 20,
            Gdx.graphics.getHeight() - table.getHeight() - 20
        );
        
        applyFilter();
    }

    private void applyFilter() {
        String selectedTag = tagSelect.getSelected();
        boolean publicOnly = publicOnlyCheck.isChecked();

        Array<EventDto> filtered = new Array<>();

        for (EventDto event : allEvents) {
            boolean tagMatch =
                selectedTag.equals("Vsi") ||
                    (selectedTag.equals("Sport") && "sports".equals(event.tag)) ||
                    (selectedTag.equals("Zabava") && "fun".equals(event.tag)) ||
                    (selectedTag.equals("Izobrazba") && "education".equals(event.tag));

            boolean publicMatch = !publicOnly || event.isPublic;

            if (tagMatch && publicMatch) {
                filtered.add(event);
            }
        }

        onFilterChanged.accept(filtered);
    }
}

