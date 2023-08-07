/**
 * @file OleConfig.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;


import JsonObject.JsonArray;
import JsonObject.JsonObject;
import JsonObject.WriterConfig;
import static data.Ole.classToOle;
import static data.Transform.isPrimitiveObject;
import java.awt.event.ActionEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import swing.OleApplication;
import swing.OleDialog;

public class OleConfig extends Ole {

    public OleConfig() {
        super();
        setType(oletype.OLECONFIG.name());
        init();
    }

    public OleConfig(Ole o) {
        super(o);
        setType(oletype.OLECONFIG.name());
        init();
    }

    public OleConfig(JsonObject o) {
        super(o);
        setType(oletype.OLECONFIG.name());
        init();
    }

    private OleConfig init() {
//        setField("options", new Ole());
//        setField("properties", new Ole());
//        setField("metadata", new Ole());
        return this;
    }

    //--------------------
    public Ole getProperties() {
        if (!getOle("properties").isEmpty()) { //.getFieldList().size() != 0) {
            return getOle("properties");
        } else {
            return new Ole();
        }
    }

    public Ole getProperties(String sfield) {
        return getProperties().getOle(sfield);
    }

    public Ole getOptions() {
        if (!getOle("options").isEmpty()) { //.getFieldList().size() != 0) {
            return getOle("options");
        } else {
            return this;
        }
    }

    public Ole getMetadata() {
        if (getOle("metadata").getFieldList().size() != 0) {
            return getOle("metadata");
        } else {
            return this;
        }
    }

    public Ole addMetadata(String field, Object o) {
//        this.getOle(oletype.OLEMETA.name()).setField(field, o);
        return this;
    }

    protected List<String> getTabList() {
        Ole options = getOptions();
        ArrayList<String> res = new ArrayList();
        for (String s : options.getFieldList()) {
            if (options.getFieldType(s).equals(oletype.OLE.name())) {
                res.add(s);
            }
        }
        return res;
    }
//--------------------------------

    public int numTabs() {
        return getTabList().size();
    }

    public List<String> getAllTabNames() {
        ArrayList<String> res = new ArrayList();
        if (numTabs() == 0) {
            res.add("Options");
        } else {
            res = new ArrayList(this.getTabList());
        }
        return res;
    }

    public Ole getTab(String stab) {
        if (numTabs() == 0) {
            return getOptions();
        } else if (getAllTabNames().contains(stab)) {
            return getOptions().getOle(stab);
        } else {
            return new Ole();
        }
    }

    public List<String> getAllTabFields(String stab) {
        ArrayList<String> res = new ArrayList();
        return getTab(stab).getFieldList();
    }

    //--------------------------
    public static OleConfig fromSimple(Object obj) {
        OleConfig res = new OleConfig();

        if (isPrimitiveObject(obj)) {
            res.setDescription(obj.getClass().getName());
            if (obj instanceof Character) {
                res.setField("options", "" + (char) obj + "");
            } else if (obj instanceof Integer) {
                res.setField("options", (int) obj);
            } else if (obj instanceof Double) {
                res.setField("options", (double) obj);
            } else if (obj instanceof Boolean) {
                res.setField("options", (boolean) obj);
            } else if (obj instanceof String) {
                res.setField("options", (String) obj);
            }
        }
        return res;
    }

    public static OleConfig fromArray(Object obj) {
        JsonArray jsares = Transform.toJsonArray(obj);
        OleConfig res = new OleConfig();
        res.set("options", jsares);
        return res;
    }

//        if (isArrayObject(obj)) {
////            for (int i = 0; i < Array.getLength(obj); i++) {
////                jsares.add(objectToOle(Array.get(obj, i)).get("options"));
////            }
//            Class arrayClass = obj.getClass();
//            Class itemType = arrayClass.getComponentType();
//            for (int i = 0; i < Array.getLength(obj); i++) {
//                Object item = Array.get(obj, i);
//                if (Array.getLength(obj) > 0) {
//                    switch (itemType.getSimpleName()) {
//                        case "int":
//                        case "Integer":
//                            jsares.add((int) item);
//                            break;
//                        case "Double":
//                        case "double":
//                            jsares.add((int) item);
//                            break;
//                        case "bolean":
//                            jsares.add((char) item);
//                        case "char":
//                            jsares.add((char) item);
//                            break;
//                        case "String":
//                            jsares.add((String) item);
//                            break;
//                        default:
//                            jsares.add((String) item.toString());
//                            break;
//                    }
//                }
//                Transform.toArrayList(jsares)
//                res.setField("options", jsares);
//                if (Array.getLength(obj) > 0) {
//                    res.setDescription(itemType + "[]");
//                }
//            }
//        }
    public static OleConfig fromEnum(Object obj) {
        OleConfig res = new OleConfig();
        if (Transform.isEnumObject(obj)) {
            res.set("options", (String) obj.toString());
            res.setDescription("enum " + obj.getClass().getName());
        }
        return res;
    }

    public static OleConfig fromObject(Object obj) {
        return fromObject(obj, 0);
    }

    public static OleConfig fromObject(Object obj, int maxdepth) {
        OleConfig res = null;
        if (isPrimitiveObject(obj)) {
            res = fromSimple(obj);
//            res.addMetadata("type", obj.getClass().getSimpleName());
        } else if (Transform.isArrayObject(obj)) {
            res = fromArray(obj);
//            res.addMetadata("type", obj.getClass().getSimpleName());
        } else if (Transform.isEnumObject(obj)) {
            res = fromEnum(obj);
//            res.addMetadata("type", obj.getClass().getSimpleName());
        } else if (Transform.isClassObject(obj)) {
            res = fromClass(obj);
//            res.addMetadata("type", obj.getClass().getSimpleName());
        } else if (Transform.isOleObject(obj)) {
            res = OleConfig.fromObject(obj);
//            res.addMetadata("type", obj.getClass().getSimpleName());
        }
        return res;
    }

//    public static OleConfig ToOle(Object obj) {
//        return classToOle(obj, 0);
//    }
//
    public static OleConfig fromClass(Object obj) {
        OleConfig res = new OleConfig();
        OleConfig oOptions = new OleConfig(), oProp = new OleConfig();

        Class c = obj.getClass();
        Field fullFields[];
//        ArrayList<Field> fullFields = new ArrayList();
        fullFields = c.getDeclaredFields();
//        fullFields = getAllFields(fullFields, c, 0);
        for (Field f : fullFields) {
            try {
                f.setAccessible(true);
                Object otmp = f.get(obj);
                if (otmp == null) {
                    continue;
                }
                Class otype = obj.getClass();
                if (isPrimitiveObject(otmp)) {
                    oOptions.setField(f.getName(), otmp);
                } else if (f.getType().isEnum()) {
                    Object oenum = f.get(obj);
                    oOptions.setField(f.getName(), (String) oenum.toString());
                } else if (f.getType().isArray()) {
                    if (f.get(obj) != null) {
                        oOptions.setField(f.getName(), Transform.toJsonArray(f.get(obj)));
                    }
                } else {
                    if (!f.getType().getTypeName().startsWith("java")) {
                        if (f.get(obj) != null) {
                            Ole onested = classToOle(f.get(obj));
                            oOptions.set(f.getName(), onested.get("options").asObject());
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println(ex.toString());
            }
        }
        res = new OleConfig();

        res.set("options", oOptions.toPlainJson());
        return res;
    }

    public static Object toSimple(OleConfig o, Class c) {
        Object obj;
        try {
            obj = c.newInstance();
        } catch (Exception ex) {
            System.err.println(ex.toString());
            return null;
        }
        if (c == int.class) {
            int ires = o.get("options").asInt();
            return ires;
        } else if (obj instanceof Double) {
            double dres = o.get("options").asDouble();
            return dres;
        } else if (obj instanceof Boolean) {
            boolean bres = o.get("options").asBoolean();
            return bres;
        } else if (c == String.class) {
            String sres = o.get("options").asString();
            return sres;
        }
        return new Object();
    }

    public static Object toObject(OleConfig ole, Class c) {
        Object obj;
        try {
            obj = c.newInstance();
        } catch (Exception ex) {
            System.err.println(ex.toString());
            return null;
        }
        if (isPrimitiveObject(obj)) {
            return toSimple(ole, c);
        } else if (Transform.isArrayObject(obj)) {
            return toArray(ole, "options");
        } else if (Transform.isEnumObject(obj)) {
            return toEnum(ole, c);
        } else if (Transform.isClassObject(obj)) {
            return toClass(ole, c);
        } else if (Transform.isOleObject(obj)) {
            OleConfig.toObject(ole, c);
        }
        return null;
    }

    public static Object toClass(OleConfig ocfg, Class c) {
        Object o;
        try {
            o = c.newInstance();
        } catch (Exception ex) {
            System.err.println(ex.toString());
            return null;
        }
        ArrayList<Field> fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
        Field f;
        for (String s : ocfg.getOptions().names()) {
            System.out.println(s + "  ");
            if (!s.equals(oletype.OLEMETA.name())) {
                try {
                    f = getField(c, s);
                    f.setAccessible(true);
                    if (f.getType() == boolean.class) {
                        f.setBoolean(o, ocfg.getOptions().getBoolean(f.getName()));
                    } else if (f.getType() == int.class) {
                        f.setInt(o, ocfg.getOptions().get(f.getName()).asInt());
                    } else if (f.getType() == double.class) {
                        f.setDouble(o, ocfg.getOptions().getDouble(f.getName()));
                    } else if (f.getType() == String.class) {
                        f.set(o, ocfg.getOptions().getField(f.getName()));
                    } else if (f.getType().isEnum()) {
                        Class enc = f.getType();
                        f.set(o, Enum.valueOf(enc, ocfg.getOptions().getField(f.getName())));
                    } else if (f.getType().isArray()) {
                        Object a = toArray(ocfg, f.getName());
                        if (a != null) {
                            f.set(o, a);
                        } else {
                            f.set(o, Array.newInstance(String.class, 0));
                        }
                    }
                } catch (Exception ex) {
                    System.err.println(ex.toString());
                    return null;
                }
            }

        }
        return o;
    }

    public static Object toArray(OleConfig ocfg, String key) {
        JsonArray jsa;
        if (ocfg.get(key) != null && ocfg.get(key).isArray()) {
            jsa = ocfg.get(key).asArray();
        } else {
            jsa = ocfg.getOptions().get(key).asArray();
        }
//       if (ocfg.getOptions() != null && ocfg.get("options").asObject().get(key).isArray()) {
//           jsa = ocfg.getOptions().get(key).asArray();
//       } else
//           jsa = ocfg.get(key).asArray();
//           
        int size = jsa.size();
        if (size == 0) {
            return null;
        }
        if (jsa.get(0).isNumber() && !jsa.get(0).toString().contains(".")) {
            Object mv = Array.newInstance(Integer.class, size);
            for (int i = 0; i < size; i++) {
                Array.set(mv, i, jsa.get(i).asInt());
            }
            return mv;
        } else if (jsa.get(0).isNumber() && jsa.get(0).toString().contains(".")) {
            Object mv = Array.newInstance(Double.class, size);
            for (int i = 0; i < size; i++) {
                Array.set(mv, i, jsa.get(i).asDouble());
            }
            return mv;
        } else if (jsa.get(0).isString()) {
            Object mv = Array.newInstance(String.class, size);
            for (int i = 0; i < size; i++) {
                Array.set(mv, i, jsa.get(i).asString());
            }
            return mv;
        } else if (jsa.get(0).isBoolean()) {
            Object mv = Array.newInstance(Boolean.class, size);
            for (int i = 0; i < size; i++) {
                Array.set(mv, i, jsa.get(i).asInt());
            }
            return mv;
        }
        return null;
    }

    public static Object toEnum(OleConfig ole, Class c) {
        Object obj;
        try {
            obj = c.newInstance();
        } catch (Exception ex) {
            System.err.println(ex.toString());
            return null;
        }
        if (Transform.isEnumObject(obj)) {
            obj = Enum.valueOf(c, ole.get("options").asString());
            return Enum.valueOf(c, ole.get("options").asString());
        } else {
            return null;
        }
    }

    public static ArrayList<Field> getAllFields(ArrayList<Field> fields, Class<?> type, int maxlevel) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null && maxlevel >= 0) {
            getAllFields(fields, type.getSuperclass(), maxlevel - 1);
        }

        return fields;
    }
//

    private static HashMap<String, OleConfig> retunn = new HashMap();

    private static Field getField(Class clazz, String fieldName)
            throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            } else {
                return getField(superClass, fieldName);
            }
        }
    }

    public OleConfig edit(OleApplication parent) {
        OleDialog oDlg = new OleDialog(parent, "Edit ");
        oDlg.setEdit(true);
//        System.out.println(this.toPlainJson().toString(WriterConfig.PRETTY_PRINT));
        if (oDlg.run(this)) {
            return oDlg.getResult();
        }
        return null;
    }

    public OleConfig edit(OleApplication parent, BiConsumer<Object , ActionEvent> validator) {
        OleDialog oDlg = new OleDialog(parent, "Edit ");
        oDlg.setEdit(true);
        oDlg.addObjectListener(validator);
//        System.out.println(this.toPlainJson().toString(WriterConfig.PRETTY_PRINT));
        if (oDlg.run(this)) {
            return oDlg.getResult();
        }
        return null;
    }

    public void view(OleApplication parent) {
        OleDialog oDlg = new OleDialog(parent, "Edit ");
        oDlg.setEdit(false);
        System.out.println(this.toPlainJson().toString(WriterConfig.PRETTY_PRINT));
        oDlg.run(this);
    }

//    public static Object editDialog(Object o,
//            OleApplication parent,
//            BiConsumer<Object, ActionEvent> validator,
//            boolean readonly) {
//        Class c = o.getClass();
//        OleConfig objectCfg = OleConfig.fromObject(o);
//        
//        return
//    }

//        OleConfig res = new OleConfig();
//        String myrun = Keygen.getHexaKey(8);
//        retunn.put(myrun, res);
//
//        SwingTools.doSwingWait(() -> {
//            OleDialog oDlg = new OleDialog(parent, "Edit ");
//            oDlg.addActionListener(validator);
//            oDlg.setEdit(true);
//            if (oDlg.run(this)) {
//                this.set(oDlg.getResult().toPlainJson().toString());
//                retunn.put(myrun, this);
//            }
//        });
//        res = retunn.get(myrun);
//        retunn.remove(myrun);
//--------------------------------
//
//    public int numTabs() {
//        return getTabList().size();
//    }
//
//    public List<String> getAllTabNames() {
//        ArrayList<String> res = new ArrayList();
//        if (numTabs() == 0) {
//            res.add("Options");
//        } else {
//            res = new ArrayList(this.getTabList());
//        }
//        return res;
//    }
//
//    public Ole getTab(String stab) {
//        if (numTabs() == 0) {
//            return getOptions();
//        } else if (getAllTabNames().contains(stab)) {
//            return getOptions().getOle(stab);
//        } else {
//            return new Ole();
//        }
//    }
//
//    public List<String> getAllTabFields(String stab) {
//        ArrayList<String> res = new ArrayList();
//        return getTab(stab).getFieldList();
//    }
//    @Override
//    public OleConfig edit(OleApplication parent) {
//        SwingTools.doSwingWait(() -> {
//            OleDialog oDlg = new OleDialog(parent, "Edit ");
//            oDlg.setEdit(true);
//            if (oDlg.run(this)) {
//                this.set(oDlg.getResult().toPlainJson().toString());
//            }
//        });
//        return this;
//    }
//
//    @Override
//    public void view(OleApplication parent) {
//        OleDialog oDlg = new OleDialog(parent, "View ");
//        oDlg.setEdit(false);
//        oDlg.run(new OleConfig(this));
//    }
}
///**
// * @file OleConfig.java
// * @author Anatoli.Grishenko@gmail.com
// *
// */
//package data;
//
//import JsonObject.JsonObject;
//import JsonObject.JsonValue;
//import java.util.ArrayList;
//import java.util.List;
//import javax.swing.JFrame;
//import swing.OleApplication;
//import swing.OleDialog;
//import swing.SwingTools;
//
//public class OleConfig extends Ole {
//
//    public OleConfig() {
//        super();
//        setType(oletype.OLECONFIG.name());
//    }
//
//    public OleConfig(Ole o) {
//        super(o);
//        setType(oletype.OLECONFIG.name());
//    }
//
//    //--------------------
//    public Ole getProperties() {
//        if (getOle("properties").getFieldList().size() != 0) {
//            return getOle("properties");
//        } else {
//            return new Ole();
//        }
//    }
//
//    public Ole getProperties(String sfield) {
//        return getProperties().getOle(sfield);
//    }
//
//    public Ole getOptions() {
//        if (getOle("options").getFieldList().size() != 0) {
//            return getOle("options");
//        } else {
//            return this;
//        }
//    }
//
//    protected List<String> getTabList() {
//        Ole options = getOptions();
//        ArrayList<String> res = new ArrayList();
//        for (String s : options.getFieldList()) {
//            if (options.getFieldType(s).equals(oletype.OLE.name())) {
//                res.add(s);
//            }
//        }
//        return res;
//    }
////--------------------------------
//
//    public int numTabs() {
//        return getTabList().size();
//    }
//
//    public List<String> getAllTabNames() {
//        ArrayList<String> res = new ArrayList();
//        if (numTabs() == 0) {
//            res.add("Options");
//        } else {
//            res = new ArrayList(this.getTabList());
//        }
//        return res;
//    }
//
//    public Ole getTab(String stab) {
//        if (numTabs() == 0) {
//            return getOptions();
//        } else if (getAllTabNames().contains(stab)) {
//            return getOptions().getOle(stab);
//        } else {
//            return new Ole();
//        }
//    }
//
//    public List<String> getAllTabFields(String stab) {
//        ArrayList<String> res = new ArrayList();
//        return getTab(stab).getFieldList();
//    }
//
//    @Override
//    public OleConfig edit(OleApplication parent) {
//        SwingTools.doSwingWait(() -> {
//            OleDialog oDlg = new OleDialog(parent, "Edit ");
//            oDlg.setEdit(true);
//            if (oDlg.run(this)) {
//                this.set(oDlg.getResult().toPlainJson().toString());
//            }
//        });
//        return this;
//    }
//
//    @Override
//    public void view(OleApplication parent) {
//        OleDialog oDlg = new OleDialog(parent, "View ");
//        oDlg.setEdit(false);
//        oDlg.run(new OleConfig(this));
//    }
//
//}
