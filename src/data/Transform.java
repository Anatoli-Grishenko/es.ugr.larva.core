/**
 * @file JsonTools.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;
import static java.lang.Enum.valueOf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import data.Ole;
import data.Ole.oletype;
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
                res.add((Ole) s);
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
            if (o.getType().equals(oletype.OLESENSOR.name())) {
//                res.add(new OleSensor(o));
            } else if (o.getType().equals(oletype.OLEFILE.name())) {
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
                    if (o.getType().equals(oletype.OLESENSOR.name())) {
//                        res.add(new OleSensor(o));
                    } else if (o.getType().equals(oletype.OLEFILE.name())) {
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

    public static int[][] shift(int original[][], int incrx, int incry, int badvalue) {
        int w = original.length, h = original[0].length, result[][] = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (0 <= x + incrx && x + incrx < w && 0 <= y + incry && y + incry < h) {
                    result[x][y] = original[x + incrx][y + incry];
                } else {
                    result[x][y] = badvalue;
                }
            }
        }
        return result;
    }

    public static int centroid(int original[][], int incrx, int incry, int badvalue) {
        int w = original.length, h = original[0].length, x = w / 2, y = h / 2;
        if (0 <= x + incrx && x + incrx < w && 0 <= y + incry && y + incry < h) {
            return original[x + incrx][y + incry];
        } else {
            return badvalue;
        }

    }
}
