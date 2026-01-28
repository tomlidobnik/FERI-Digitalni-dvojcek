package si.um.feri.copycats.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import java.util.EnumMap;

public class MarkerIconRegistry {

    private static final EnumMap<MarkerType, Texture> ICONS =
        new EnumMap<>(MarkerType.class);

    public static void load() {
        ICONS.put(MarkerType.FUN,
            new Texture(Gdx.files.internal("icons/fun.png")));

        ICONS.put(MarkerType.SPORTS,
            new Texture(Gdx.files.internal("icons/sports.png")));

        ICONS.put(MarkerType.EDUCATION,
            new Texture(Gdx.files.internal("icons/education.png")));

        ICONS.put(MarkerType.DEFAULT,
            new Texture(Gdx.files.internal("icons/default.png")));
    }

    public static Texture get(MarkerType type) {
        Texture t = ICONS.get(type);
        return t != null ? t : ICONS.get(MarkerType.DEFAULT);
    }

    public static void dispose() {
        for (Texture t : ICONS.values()) {
            t.dispose();
        }
        ICONS.clear();
    }
}
