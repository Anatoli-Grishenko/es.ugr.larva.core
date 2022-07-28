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
            m = new Mission(jsva.asString());
            put(m.getName(), m);
        }
    }

    public JsonArray toJson() {
        JsonArray jsares = new JsonArray();
        Mission m;
        for (String smission : this.keySet()) {
            m = get(smission);
            jsares.add(m.toString());
        }
        return jsares;
    }

    public Mission getMission(String name) {
        if (this.keySet().contains(name)) {
            return this.get(name);
        }else
            return null;
    }
    
    public MissionSet addMission(String mission) {
        if (this.get(mission) == null) {
            this.put(mission, new Mission(mission));
        }
        return this;
    }
    public MissionSet addGoal(String mission, String task) {
        if (this.get(mission) != null) {
            this.get(mission).addGoal(task);
        }
        return this;
    }
}
