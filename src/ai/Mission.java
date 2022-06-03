/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import com.eclipsesource.json.JsonArray;
import java.util.ArrayList;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Mission extends ArrayList<String>{
    String name;
    public Mission(String name) {
        super();
        setName(name);
    }

    public Mission(JsonArray jsa) {
        setName(jsa.get(0).asString());
        for (int i=1; i<jsa.size(); i++) {
            this.add(jsa.get(i).asString());
        }
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public JsonArray toJson() {
        JsonArray res = new JsonArray();
        res.add(getName());
        for (String sm : this) {
            res.add(sm);
        }
        return res;
    }
}
