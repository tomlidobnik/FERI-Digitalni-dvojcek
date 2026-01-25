package si.um.feri.copycats.utils;

import com.badlogic.gdx.graphics.Texture;

import java.util.Map;

import si.um.feri.copycats.utils.Geolocation;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;


public class Marker {
    public MarkerType type;
    public Geolocation lokacija;
    public Texture icon;


    public Marker(MarkerType type, double lat, double lng, Texture icon) {
        this.type = type;
        this.lokacija = new Geolocation(lat, lng);
        this.icon = icon;
    }
}
