/**
 * @file OleOptions.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;
import glossary.ole;
import java.util.ArrayList;

/**
 * It is just a simple subclass of Ole with the generic access methods. Please
 * refer to {@link Ole} to get to know the different options that may be
 * represented.
 *
 */
public class OleList extends Ole {

    public OleList() {
        super();
        setType(ole.LIST.name());
        this.setField("items", new ArrayList());
    }

    public OleList(Ole o) {
        super(o);
        setType(ole.LIST.name());
    }

    public OleList addItem(String item) {
        JsonArray jslist = data.get("items").asArray();
        
        jslist.add(item);
        return this;
    }
    
    public boolean findItem(String item) {
        JsonArray jslist = data.get("items").asArray();

        for (JsonValue jsv : jslist.values()) {
            if (jsv.asString().equals(item))
                return true;
        }
        return false;
    }
}
