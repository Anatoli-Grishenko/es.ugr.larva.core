/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import com.eclipsesource.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class ThingIndex {

    HashMap<String, ArrayList<Thing>> index;
    String field;

    public ThingIndex() {
        index = new HashMap();
        field = "";
    }

    public int size() {
        return index.size();
    }

    public ThingIndex clear() {
        index.clear();
        return this;
    }

    public ArrayList<String> getKeys() {
        return new ArrayList(index.keySet());
    }

    public ArrayList<Thing> getValues(String key) {
        ArrayList<Thing> res = new ArrayList();
        if (index.get(key) != null) {
            for (Thing t : index.get(key)) {
                res.add(t);
            }
        }
        return res;
    }

    public ArrayList<Thing> getAllValues() {
        ArrayList<Thing> res = new ArrayList();
        for (String key : this.getKeys()) {
            for (Thing t : getValues(key)) {
                res.add(t);
            }
        }
        return res;
    }

    public String getField() {
        return field;
    }

    public ThingIndex setField(String field) {
        this.field = field;
        return this;
    }

    public ThingIndex addIndexTo(Thing t) {
        JsonObject jsot = t.toJson();
        if (jsot.get(getField()) != null) {
            String toindex;
            if (jsot.get(getField()).isArray()) {
                toindex = jsot.get(getField()).toString();
            } else {
                toindex = jsot.get(getField()).asString();
            }
            if (!index.containsKey(toindex)) {
                index.put(toindex, new ArrayList());
            }
            index.get(toindex).add(t);
        }
        return this;
    }

    public ThingIndex removeIndexTo(Thing t) {
        JsonObject jsot = t.toJson();
        if (jsot.get(getField()) != null) {
            String toindex = jsot.get(getField()).toString();
            if (index.get(toindex) != null && index.get(toindex).contains(t)) {
                index.get(toindex).remove(t);
            }
        }
        return this;
    }

}
