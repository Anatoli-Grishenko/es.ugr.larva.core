/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class MissionSet extends HashMap<String, Mission> {

    public MissionSet() {
        super();
    }

    public MissionSet(JsonArray jsa) {
        Mission m;
        for (JsonValue jsva : jsa) {
            if (jsva.isArray()) {
                m = new Mission(jsva.asArray());
                put(m.getName(), m);
            }
        }
    }
    public JsonArray toJson(){
        JsonObject res = new JsonObject(); 
        JsonArray jsares= new JsonArray();
        Mission m;
        for (String smission:this.keySet()) {
            m = get(smission);
            jsares.add(m.toJson());
        }
        return jsares;
    }
    public Mission getMission(int i) {
        if (0<=i && i<size()) {
            return this.get(new ArrayList(this.keySet()).get(i));
        }else
            return null;
    }
    public Mission getMission(String name) {
        if (this.keySet().contains(name)) {
            return this.get(name);
        }else
            return null;
    }
}
