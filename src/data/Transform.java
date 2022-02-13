/**
 * @file JsonTools.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import glossary.ole;
import static glossary.ole.SENSOR;
import static java.lang.Enum.valueOf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// JsonArray <--> ArrayList <--> Array <-- Enum
/**
 * Class of static methods for transforming some objects into
 * JsonObject/jsonArray
 */
public class Transform {

    /**
     * Transform a generic ArrayList into a JsonArray of the same types. It
     * supports String, Integer, Double, Boolean a,d Ole
     *
     * @param l An arrayList of objects
     * @return A JsonArray of the same objects. Any complex object is ignored
     * and it does not try to serialize them. The only complex object supported
     * is {@link Ole}
     */
    public static JsonArray toJsonArray(ArrayList<Object> l) {
        JsonArray res = new JsonArray();
        l.forEach(s -> {
            if (s instanceof String) {
                res.add((String) s);
            } else if (s instanceof Integer) {
                res.add((Integer) s);
            } else if (s instanceof Double) {
                res.add((Double) s);
            } else if (s instanceof Boolean) {
                res.add((Boolean) s);
            } else if (s instanceof Ole) {
                res.add(((Ole) s).toJson());
            } else {
                res.add((String) s.toString());
            }
        });
        return res;
    }

    public static List<Ole> toArrayListOle(JsonArray jsa) {
        ArrayList<Ole> res = new ArrayList();
        for (JsonValue jsv : jsa) {
            Ole o = new Ole(jsv.toString());
            if (o.getType().equals(ole.SENSOR.name())) {
                res.add(new OleSensor(o));
            } else if (o.getType().equals(ole.OLEFILE.name())) {
                res.add(new OleFile(o));
            } else {
                res.add(o);
            }
        }
        return res;
    }

    public static List<Double> toArrayListDouble(JsonArray jsa) {
        ArrayList<Double> res = new ArrayList();
        for (JsonValue jsv : jsa) {
            if (jsv.isNumber()) {
                res.add(jsv.asDouble());
            }
        }
        return res;
    }

    public static List<String> toArrayListString(JsonArray jsa) {
        ArrayList<String> res = new ArrayList();
        for (JsonValue jsv : jsa) {
            if (jsv.isString()) {
                res.add(jsv.asString());
            }
        }
        return res;
    }

    public static List<Object> toArrayList(JsonArray jsa) {
        ArrayList<Object> res = new ArrayList();
        for (JsonValue jsv : jsa) {
            if (jsv.isString()) {
                res.add(jsv.asString());
            } else if (jsv.isNumber()) {
                res.add(jsv.asDouble());
            } else if (jsv.isBoolean()) {
                res.add(jsv.asBoolean());
            } else {
                Ole o = new Ole(jsv.toString());
                if (!o.isEmpty()) {
                    if (o.getType().equals(ole.SENSOR.name())) {
                        res.add(new OleSensor(o));
                    } else if (o.getType().equals(ole.OLEFILE.name())) {
                        res.add(new OleFile(o));
                    } else {
                        res.add(o);
                    }
                } else {
                    res.add(jsv.toString());
                }
            }
        }
        return res;
    }

    public static List<Object> toArrayList(Object o[]) {
        return Arrays.asList(o);
    }

    public static String[] toArray(ArrayList<String> al) {
        return al.toArray(new String[al.size()]);
    }

//    public static Object[] toArray (ArrayList<Object> al) {
//       return al.toArray(new Object[al.size()]);
//    }
    public static ArrayList<String> getAllNames(Class<? extends Enum<?>> e) {
        String aux[] = Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
        return new ArrayList(Arrays.asList(aux));
    }

    protected <E extends Enum<E>> E getEnum(E myenum, String value) {
        for (Enum<E> enumVal : myenum.getClass().getEnumConstants()) {
            if (enumVal.toString().equalsIgnoreCase(value)) {
                return (E) enumVal;
            }
        }
        return null;
//            sensors s= sensors.valueOf(jsv.asString());

    }
    
    public static JsonObject OleToJson(Ole odata) {
        JsonObject res = new JsonObject();
        for (String f : odata.getFieldList()) {
            String type = odata.getFieldType(f);
            if (type.equals(ole.INTEGER.name())) {
                res.set(f,odata.getInt(f));
            } else if (type.equals(ole.DOUBLE.name())) {
                res.set(f, odata.getDouble(f));
            } else if (type.equals(ole.STRING.name())) {
                res.set(f, odata.getString(f));
            } else if (type.equals(ole.BOOLEAN.name())) {
                res.set(f, odata.getBoolean(f));
            } else if (type.startsWith(ole.OLE.name())) {
                res.set(f, odata.getOle(f).exportToJson());
            } else if (type.equals(ole.ARRAY.name())) {
            }
        }
        return res;
    }    

    public static Ole JsonToOle(JsonObject jsole) {
        Ole res  = new Ole();
        for (String jsf : jsole.names()) {
            if (jsole.get(jsf).isBoolean()) {
               res.setField(jsf, jsole.get(jsf).asBoolean());
            } else if (jsole.get(jsf).isNumber()) {
                if (jsole.get(jsf).toString().contains(".") || jsole.get(jsf).toString().contains(",")) {
                    res.setField(jsf, jsole.get(jsf).asDouble());
                } else {
                    res.setField(jsf, jsole.get(jsf).asInt());
                }
            } else if (jsole.get(jsf).isString()) {
                res.setField(jsf, jsole.get(jsf).asString());
            } else if (jsole.get(jsf).isObject()) {
                res.setField(jsf, new Ole(jsole.get(jsf).asObject()));
            }
        }
        return res;
    }    

}
