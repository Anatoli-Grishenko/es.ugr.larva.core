/**
 * @file JsonTools.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

import JsonObject.JsonArray;
import JsonObject.JsonObject;
import JsonObject.JsonValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import data.Ole;
import data.Ole.oletype;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import javax.xml.bind.annotation.W3CDomHandler;
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
                res.add((JsonObject) s);    //////////////////////////////////////////
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

    public static ArrayList<String> toArrayListString(String what) {
        ArrayList<String> res = new ArrayList();
        res.add(what);
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

    public static String[] toArrayString(ArrayList<String> al) {
        String res[] = new String[al.size()];
        for (int i = 0; i < al.size(); i++) {
            res[i] = al.get(i);
        }
        return res;
    }

    public static double[] toArrayDouble(ArrayList<Double> al) {
        double res[] = new double[al.size()];
        for (int i = 0; i < al.size(); i++) {
            res[i] = al.get(i);
        }
        return res;
    }

    public static String[] toArrayString(JsonArray al) {
        String res[] = new String[al.size()];
        for (int i = 0; i < al.size(); i++) {
            res[i] = al.get(i).asString();
        }
        return res;
    }

    public static double[] toArrayDouble(JsonArray al) {
        double res[] = new double[al.size()];
        for (int i = 0; i < al.size(); i++) {
            res[i] = al.get(i).asDouble();
        }
        return res;
    }

//    public static Object[] toArrayString (ArrayList<Object> al) {
//       return al.toArrayString(new Object[al.size()]);
//    }
    public static ArrayList<String> getAllNames(Class<? extends Enum<?>> e) {
        String aux[] = Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
        return new ArrayList(Arrays.asList(aux));
    }

    public static <E extends Enum<E>> String getEnumField(E myenum, Field f) {
        for (Enum<E> enumVal : myenum.getClass().getEnumConstants()) {
            if (enumVal.toString().equalsIgnoreCase(f.toString())) {
                return enumVal.toString();
            }
        }
        return null;
//            sensors s= sensors.valueOf(jsv.asString());

    }

    public static <E extends Enum<E>> E getEnum(E myenum, String value) {
        for (Enum<E> enumVal : myenum.getClass().getEnumConstants()) {
            if (enumVal.toString().equalsIgnoreCase(value)) {
                return (E) enumVal;
            }
        }
        return null;
//            sensors s= sensors.valueOf(jsv.asString());

    }

    public static <E extends Enum<E>>
            String getEnumString(E clazz) {
        return clazz.name();
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

    public static JsonArray Matrix2JsonArray(int[][] raw) {
        int w = raw.length, h = raw[0].length;
        JsonArray jsamatrix = new JsonArray(), jsarow;
        for (int y = 0; y < h; y++) {
            jsarow = new JsonArray();
            for (int x = 0; x < w; x++) {
                jsarow.add(raw[x][y]);
            }
            jsamatrix.add(jsarow);
        }
        return jsamatrix;
    }

    public static JsonArray Matrix2JsonArray(int[] raw) {
        int w = raw.length;
        JsonArray jsamatrix = new JsonArray(), jsarow;
        for (int x = 0; x < w; x++) {
            jsamatrix.add(raw[x]);
        }
        return jsamatrix;
    }

    public static JsonArray Matrix2JsonArray(String[] raw) {
        int w = raw.length;
        JsonArray jsamatrix = new JsonArray(), jsarow;
        for (int x = 0; x < w; x++) {
            jsamatrix.add(raw[x]);
        }
        return jsamatrix;
    }

    public static JsonArray Matrix2JsonArray(double[][] raw) {
        int w = raw.length, h = raw[0].length;
        JsonArray jsamatrix = new JsonArray(), jsarow;
        for (int y = 0; y < h; y++) {
            jsarow = new JsonArray();
            for (int x = 0; x < w; x++) {
                jsarow.add(raw[x][y]);
            }
            jsamatrix.add(jsarow);
        }
        return jsamatrix;
    }

    public static JsonArray Matrix2JsonArray(double[] raw) {
        int w = raw.length;
        JsonArray jsamatrix = new JsonArray(), jsarow;
        for (int x = 0; x < w; x++) {
            jsamatrix.add(raw[x]);
        }
        return jsamatrix;
    }

    public static String outOf(String v[]) {
        return v[(int) (Math.random() * v.length)];
    }

    public static boolean isWithin(String v[], String value) {
        return Arrays.deepToString(v).matches(value);
    }

    public static JsonArray toJsonArray(Object o) {
        JsonArray res = null;
        Class arrayClass = o.getClass();
        Class itemType;
        Object item;
        if (isArrayObject(o)) {
            res = new JsonArray();
            itemType = arrayClass.getComponentType();
            for (int i = 0; i < Array.getLength(o); i++) {
                item = Array.get(o, i);
                switch (itemType.getSimpleName()) {
                    case "int":
                    case "Integer":
                        res.add((int) item);
                        break;
                    case "Double":
                    case "double":
                        res.add((int) item);
                        break;
                    case "bolean":
                        res.add((char) item);
                    case "char":
                        res.add((char) item);
                        break;
                    case "String":
                        res.add((String) item);
                        break;
                    default:
                        res.add((String) item.toString());
                        break;
                }
            }
        }
        if (isCollectionObject(o)) {
            res = new JsonArray();
            itemType = arrayClass.getComponentType();
            Collection cobject = ((Collection) o);
            ArrayList l = new ArrayList((Collection) o);
            for (int i = 0; i <cobject.size(); i++) {
                item = l.get(i);
                switch (item.getClass().getSimpleName()) {
                    case "int":
                    case "Integer":
                        res.add((int) item);
                        break;
                    case "Double":
                    case "double":
                        res.add((int) item);
                        break;
                    case "bolean":
                        res.add((char) item);
                    case "char":
                        res.add((char) item);
                        break;
                    case "String":
                        res.add((String) item);
                        break;
                    default:
                        res.add((String) item.toString());
                        break;
                }
            }
        }
        return res;
    }


    public static Object[] toArray(Object o) {
        Object res []= null;
        Class arrayClass = o.getClass();
        Class itemType;
        Object item;
        if (isCollectionObject(o)) {
            res = ((Collection)o).toArray(new String[((Collection)o).size()]);
        }
        if (isJsonArray(o)) {
            res = toArray(toArrayList((JsonArray)o));
        }
        return res;
    }
    
    public static List<Object> toList(Object o) {
        List res = null;
        Class arrayClass = o.getClass();
        Class itemType;
        Object item;
        if (isArrayObject(o)) {
            res = Arrays.asList(o);
        }
        if (isJsonArray(o)) {
            res = toArrayList((JsonArray)o);
        }
        return res;
    }
    
    


    public static boolean isPrimitiveObject(Object obj) {
//        return ;
//        return c == int.class || c == double.class || c == boolean.class || c == String.class;
        return (obj instanceof Integer
                || obj instanceof Character
                || obj instanceof Double
                || obj instanceof Boolean
                || obj instanceof String);

    }
    
    public static boolean isOleObject(Object o){
        Ole oo= new Ole();
        try {
            oo.set(o.toString());
            return true;
        } catch(Exception ex) {
            return false;
        }
//        if (jso.get(oletype.OLEMETA.name()) != null) {
//            return jso.get(oletype.OLEMETA.name()).asObject().getBoolean("ole", false);
//        } else {
//            return false;
//        }

    }


    public static boolean isArrayObject(Object obj) {
        return obj.getClass().isArray();
    }

    public static boolean isCollectionObject(Object obj) {
        return obj instanceof Collection<?>;
    }

    public static boolean isMapObject(Object obj) {
        return obj instanceof Map<?, ?>;
    }

    public static boolean isEnumObject(Object obj) {
        return obj.getClass().isEnum();
    }

    public static boolean isClassObject(Object obj) {
        return !isPrimitiveObject(obj) && !isArrayObject(obj) && !isEnumObject(obj);
    }
    
    public static boolean isJsonObject(Object obj) {
        return obj instanceof JsonObject;
    }
    
    public static boolean isJsonArray(Object obj) {
        return obj instanceof JsonArray;
    }
    
    

}
