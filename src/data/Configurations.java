/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import JsonObject.JsonArray;
import JsonObject.JsonObject;
import JsonObject.JsonValue;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import swing.OleApplication;
import swing.OleDialog;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Configurations extends Ole {

    public Configurations() {
        super();
        cleanUp();
    }

    private void cleanUp() {
//        for (String n : new ArrayList<String>(names())) {
//            if (get(n) != null) {
//                if (n.equals(oletype.OLEMETA.name())) {
//                    remove(n);
//                }
//            }
//        }
    }

    public Configurations(Ole o) {
        super(o);
        cleanUp();
    }

    public Configurations(JsonObject jso) {
        super(jso);
        cleanUp();
    }

    public Configurations(JsonValue jso) {
        super(jso.asObject());
        cleanUp();
    }

    //--------------------
    public Configurations getProperties() {
        if (get("properties") != null) {
            return new Configurations(get("properties"));
        } else {
            return null;
        }
    }

    @Override
    public List<String> getFieldList() {
        return names();
    }

    public Configurations getOptions() {
        if (get("options") != null) {
            return new Configurations(get("options").asObject());
        } else {
            return null;
        }
    }

    protected List<String> getTabList() {
        Configurations options = getOptions();
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

    public Configurations getTab(String stab) {
        if (numTabs() == 0) {
            return getOptions();
        } else if (getAllTabNames().contains(stab)) {
            return new Configurations(getOptions().get(stab));
        } else {
            return null;
        }
    }

    public Configurations getArrayConf(String field) {
        return new Configurations(get(field).asArray());
    }

    public List<String> getAllTabFields(String stab) {
        ArrayList<String> res = new ArrayList();
        return getTab(stab).getFieldList();
    }

    //--------------------------
    public static boolean isSimpleObject(Object obj) {
//        return ;
//        return c == int.class || c == double.class || c == boolean.class || c == String.class;
        return (obj instanceof Integer
                || obj instanceof Character
                || obj instanceof Double
                || obj instanceof Boolean
                || obj instanceof String);

    }

    public static boolean isArrayObject(Object obj) {
        return obj.getClass().isArray();
    }

    public static boolean isEnumObject(Object obj) {
        return obj.getClass().isEnum();
    }

    public static boolean isClassObject(Object obj) {
        return !isSimpleObject(obj) && !isArrayObject(obj) && !isEnumObject(obj);
    }

    public static Configurations fromSimple(Object obj) {
        Configurations res = new Configurations();

        if (isSimpleObject(obj)) {
            res.setDescription(obj.getClass().getName());
            if (obj instanceof Character) {
                res.setField("options", "" + (char) obj + "");
            } else if (obj instanceof Integer) {
                res.set("options", (int) obj);
            } else if (obj instanceof Double) {
                res.set("options", (double) obj);
            } else if (obj instanceof Boolean) {
                res.set("options", (boolean) obj);
            } else if (obj instanceof String) {
                res.set("options", (String) obj);
            }
        }
        return res;
    }

    public static Configurations fromArray(Object obj) {
        Configurations res = new Configurations();
        JsonArray jsares = new JsonArray();

        if (isArrayObject(obj)) {
            for (int i = 0; i < Array.getLength(obj); i++) {
                jsares.add(objectToOle(Array.get(obj, i)).get("options"));
            }
            res.set("options", jsares);
            if (Array.getLength(obj) > 0) {
                res.setDescription(Array.get(obj, 0).getClass().getSimpleName() + "[]");
            }
        }
        return res;
    }

    public static Configurations fromEnum(Object obj) {
        Configurations res = new Configurations();
        if (isEnumObject(obj)) {
            res.set("options", (String) obj.toString());
            res.setDescription("enum " + obj.getClass().getName());
        }
        return res;
    }

    public static Configurations fromObject(Object obj) {
        return fromObject(obj, 0);
    }

    public static Configurations fromObject(Object obj, int maxdepth) {
        if (isSimpleObject(obj)) {
            return fromSimple(obj);
        } else if (isArrayObject(obj)) {
            return fromArray(obj);
        } else if (isEnumObject(obj)) {
            return fromEnum(obj);
        } else if (isClassObject(obj)) {
            return fromClass(obj);
        } else {
            return null;
        }
    }

//    public static Configurations ToOle(Object obj) {
//        return classToOle(obj, 0);
//    }
//
    public static Configurations fromClass(Object obj) {
        Configurations res = new Configurations();
        Configurations oOptions = new Configurations(), oProp = new Configurations();

        Class c = obj.getClass();
        ArrayList<Field> fullFields = new ArrayList();
        fullFields = getAllFields(fullFields, c, 3);
        for (Field f : fullFields) {
            try {
                f.setAccessible(true);
                if (isSimpleObject(f.get(obj))) {
                    if (f.get(obj) != null) {
                        oOptions.set(f.getName(), fromObject(f.get(obj)).toPlainJson().get("options"));
                    }
                } else if (f.getType().isEnum()) {
                    Object oenum = f.get(obj);
                    oOptions.setField(f.getName(), (String) oenum.toString());
                } else if (f.getType().isArray()) {
                    if (f.get(obj) != null) {
                        oOptions.set(f.getName(), fromArray(f.get(obj)).get("options").asArray());
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
        res = new Configurations();

        res.set("options", oOptions.toPlainJson());
        return res;
    }

    public static Object toSimple(Configurations o, Class c) {
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

    public static Object toObject(Configurations ole, Class c) {
        Object obj;
        try {
            obj = c.newInstance();
        } catch (Exception ex) {
            System.err.println(ex.toString());
            return null;
        }
        if (isSimpleObject(obj)) {
            return toSimple(ole, c);
        } else if (isArrayObject(obj)) {
            return toArray(ole);
        } else if (isEnumObject(obj)) {
            return toEnum(ole, c);
        } else if (isClassObject(obj)) {
            return toClass(ole, c);
        }
        return null;
    }

    public static Object toClass(Configurations ocfg, Class c) {
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
            System.out.println(s+"  ");
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
                        f.set(o, toArray(ocfg.getOptions().getArrayConf(s)));
                    }
                } catch (Exception ex) {
                    System.err.println(ex.toString());
                    return null;
                }
            }

        }
        return null;
    }

    public static Object toArray(Configurations ole) {
        JsonArray jsa = ole.get("options").asArray();
        int size = jsa.size();
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

    public static Object toEnum(Configurations ole, Class c) {
        Object obj;
        try {
            obj = c.newInstance();
        } catch (Exception ex) {
            System.err.println(ex.toString());
            return null;
        }
        if (isEnumObject(obj)) {
            obj = Enum.valueOf(c, ole.get("options").asString());
            return Enum.valueOf(c, ole.get("options").asString());
        } else {
            return null;
        }
    }

//    public static ArrayList<Field> getAllFields(ArrayList<Field> fields, Class<?> type, int maxlevel) {
//        fields.addAll(Arrays.asList(type.getDeclaredFields()));
//
//        if (type.getSuperclass() != null && maxlevel >= 0) {
//            getAllFields(fields, type.getSuperclass(), maxlevel - 1);
//        }
//
//        return fields;
//    }
//
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

//    public Configurations edit(OleApplication parent) {
////        SwingTools.doSwingWait(() -> {
//        OleDialog oDlg = new OleDialog(parent, "Edit ");
//        oDlg.setEdit(true);
//        if (oDlg.run(this)) {
//            this.set(oDlg.getResult().toPlainJson().toString());
//            return this;
//        }
////        });
//        return null;
//    }
//
//    public Configurations edit(OleApplication parent, BiConsumer<ActionEvent, Configurations> validator) {
////        SwingTools.doSwingWait(() -> {
//        OleDialog oDlg = new OleDialog(parent, "Edit ");
//        oDlg.addActionListener(validator);
//        oDlg.setEdit(true);
//        if (oDlg.run(this)) {
//            this.set(oDlg.getResult().toPlainJson().toString());
//            return this;
//        }
////        });
//        return null;
//    }
//
//    @Override
//    public void view(OleApplication parent) {
//        OleDialog oDlg = new OleDialog(parent, "View ");
//        oDlg.setEdit(false);
//        oDlg.run(this);
//    }

//}
}
