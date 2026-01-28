package si.um.feri.copycats.api;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
public class EventDto implements Json.Serializable {

    public int id;
    public int user_fk;
    public String title;
    public String description;
    public String start_date;
    public String end_date;
    public int location_fk;
    public boolean isPublic;
    public String tag;

    @Override
    public void read(Json json, JsonValue jsonData) {
        id = jsonData.getInt("id");
        user_fk = jsonData.getInt("user_fk");
        title = jsonData.getString("title");
        description = jsonData.getString("description");
        start_date = jsonData.getString("start_date");
        end_date = jsonData.getString("end_date");
        location_fk = jsonData.getInt("location_fk");
        isPublic = jsonData.getBoolean("public");
        tag = jsonData.getString("tag");
    }

    @Override
    public void write(Json json) {

    }
}
