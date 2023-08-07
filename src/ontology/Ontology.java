/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ontology;

import JsonObject.JsonObject;
import JsonObject.WriterConfig;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author lcv
 */
public class Ontology {

    protected HashMap<String, String> _ontology;
    public static final String ROOT = "";

    public Ontology() {
        _ontology = new HashMap<>();
    }

    public Ontology add(String type, String subtype) {
        type = type.toUpperCase();
        subtype = subtype.toUpperCase();
        if (isType(subtype) || subtype.equals(ROOT)) {
            _ontology.put(type, subtype);
        }
        return this;
    }

    public boolean isType(String c) {
        return _ontology.containsKey(c.toUpperCase());
    }

    public String getRootType() {
        String res = "";
        for (Entry<String, String> e : _ontology.entrySet()) {
            if (e.getValue().equals(ROOT)) {
                res = e.getKey();
            }
        }
        return res;
    }

    public String getType(String c) {
        return _ontology.get(c.toUpperCase());
    }

    public boolean isSubTypeOf(String subtype, String type) {
        try {
            type = type.toUpperCase();
            subtype = subtype.toUpperCase();
//        String immediate=getType(subtype);
            if (subtype.equals(type)) {
                return true;
            } else if (subtype.equals(ROOT)) {
                return false;
            } else {
                return isSubTypeOf(getType(subtype), type);
            }
        } catch (Exception ex) {
            System.err.println("Exception between types " + subtype + " and " + type);
            return false;
        }
    }

    public boolean matchTypes(String first, String second) {
        return first.equals(second) || isSubTypeOf(first, second);
    }

    public JsonObject toJson() {
        JsonObject res = new JsonObject();
        for (String s : _ontology.keySet()) {
            res.add(s, _ontology.get(s));
        }
        return new JsonObject().add("ontology", res);
    }

    @Override
    public String toString() {
        return toJson().toString(WriterConfig.PRETTY_PRINT);
    }

}
