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

    protected JsonArray getList() {
        return get("items").asArray();
    }

    public int size() {
        return getList().size();
    }

    public String getItem(int i) {
        if (0 <= i && i < size()) {
            return getList().get(i).asString();
        } else {
            return null;
        }
    }

    public OleList addUniqueItem(String item) {
        if (!findItem(item)) {
            this.addToField("items", item);
        }
        return this;
    }

    public OleList addUniqueItem(String[] items) {
        for (int i = 0; i < items.length; i++) {
            if (!findItem(items[i])) {
                this.addToField("items", items[i]);
            }
        }
        return this;
    }

    public OleList addDupItem(String[] items) {
        for (int i = 0; i < items.length; i++) {
            this.addToField("items", items[i]);
        }
        return this;
    }

    public OleList addDupItem(String item) {
        this.addToField("items", item);
        return this;
    }

    public OleList removeItem(String item) {
        ArrayList<String> items = this.getArray("items");
        items.remove(item);
        this.setField("items", new ArrayList(items));
        return this;
    }

    public OleList removeALlItems(String item) {
        ArrayList<String> items = this.getArray("items");
        while (items.contains(item)) {
            items.remove(item);
        }
        this.setField("items", new ArrayList(items));
        return this;
    }

    public boolean findItem(String item) {
        for (JsonValue jsv : getList().values()) {
            if (jsv.asString().equals(item)) {
                return true;
            }
        }
        return false;
    }

    public OleList getIntersection(OleList other) {
        OleList res = new OleList();
        for (JsonValue jsv : getList().values()) {
            if (other.findItem(jsv.asString())) {
                res.addUniqueItem(jsv.asString());
            }
        }
        return res;
    }

    public OleList getUnion(OleList other) {
        OleList res = new OleList();
        for (JsonValue jsv : getList().values()) {
            res.addUniqueItem(jsv.asString());
        }
        for (JsonValue jsv : other.getList().values()) {
            res.addUniqueItem(jsv.asString());
        }
        return res;
    }

    public OleList getConcatenation(OleList other) {
        OleList res = new OleList();
        for (JsonValue jsv : getList().values()) {
            res.addDupItem(jsv.asString());
        }
        for (JsonValue jsv : other.getList().values()) {
            res.addDupItem(jsv.asString());
        }
        return res;
    }

    public OleList getDifference(OleList other) {
        OleList res = new OleList();
        for (JsonValue jsv : getList().values()) {
            if (!other.findItem(jsv.asString())) {
                res.addUniqueItem(jsv.asString());
            }
        }
        return res;
    }

    public String prettyprint() {
        String res = "";
        for (JsonValue jsv : getList().values()) {
            res += jsv.asString() + " ";
        }
        return res;
    }

}
