/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AutoConfiguration;

import JsonObject.JsonArray;
import JsonObject.JsonObject;
import JsonObject.WriterConfig;
import crypto.Cryptor;
import data.Ole;
import static data.Ole.classToOle;
import data.OleConfig;
import data.OleSerializer;
import data.Transform;
import static data.Transform.isPrimitiveObject;
import java.awt.Font;
import static java.awt.Font.PLAIN;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import swing.SwingTools;
import tools.ExceptionHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public abstract class AutoConfigurationHandler {

    String configFolder, passwd;
    Class configType;
    boolean verbose, encrypted, validate;
    Cryptor myCryptor;
    Ole properties, controllers;
    ArrayList<Field> fullFields;
//    Object currentObject;
    OleConfig currentObjectJSON;
    Method Validator;
    String name;

    public AutoConfigurationHandler(String myConfigFolder, Class myConfigType) {
        configFolder = myConfigFolder;
        configType = this.getClass(); //myConfigType;
        this.verbose = false;
        reset();
//        currentObject = this;
//        currentObjectJSON = this.toJson();
//        fullFields = new ArrayList(Transform.toArrayList(myConfigType.getDeclaredFields()));
//        if (myConfigType.isAnnotationPresent(OleSerializer.class)) {
//            Ole oField = new Ole();
//            for (Field f : fullFields) {
//                if (f.isAnnotationPresent(OleSerializer.class)) {
//                    oField = new Ole();
//                    OleSerializer annotation = f.getAnnotation(OleSerializer.class);
//                    boolean fromfile = annotation.FromFile();
//                    validate = annotation.Validate();
//                    String tooltip = annotation.ToolTip(),
//                            selectfrom[] = annotation.SelectFrom(),
//                            selectWith = annotation.SelectWith(),
//                            validationlabel = annotation.ValidateLabelGUI(),
//                            triggersto = annotation.TriggersTo();
//                    if (!tooltip.toUpperCase().equals("NONE")) {
//                        oField.setField("tooltip", tooltip);
//                    }
//                    if (selectfrom.length > 0) {
//                        oField.set("select", Transform.toJsonArray(selectfrom));
//                    }
//                    if (fromfile) {
//                        oField.set("file", fromfile);
//                    }
//                    if (!selectWith.toUpperCase().equals("NONE")) {
//                        for (Method m : myConfigType.getDeclaredMethods()) {
//                            if (m.getName().equals(selectWith)) {
//                                String values[];
//                                try {
//                                    values = (String[]) m.invoke(null);
//                                    oField.set("select", Transform.toJsonArray(values));
//                                } catch (Exception ex) {
//                                    new ExceptionHandler(ex);
////                                    SwingTools.Error("Call to method " + m.getName() + " failed:\n" + ex.toString());
//                                }
//                            }
//                        }
//
//                    }
//                    if (validate) {
//                        controllers.set(f.getName(), new Ole().
//                                setField("label", validationlabel).
//                                setField("class", getConfigType().getName()).
//                                setField("method", "validateDialog"));
////                                    getConfigType().
////                                            getMethod("validate", null, getConfigType(), ActionEvent.class).invoke(null, t, u);
////                                } catch (Exception ex) {
////                                    new ExceptionHandler(ex);
////                                }
//                    }
//                    properties.set(f.getName(), oField);
//                }
//            }
//            properties.set("control", controllers.toPlainJson());
//        }
    }

    protected AutoConfigurationHandler reset() {
        properties = new Ole();
        controllers = new Ole();
        try{
        initProperties();
        } catch (Exception ex) {
            new ExceptionHandler(ex);
        }
        return this;
    }

    protected void initProperties() throws Exception {
        fullFields = new ArrayList(Transform.toArrayList(getConfigType().getDeclaredFields()));
        if (getConfigType().isAnnotationPresent(OleSerializer.class)) {
            Ole oField = new Ole();
            for (Field f : fullFields) {
                if (f.isAnnotationPresent(OleSerializer.class)) {
                    oField = new Ole();
                    OleSerializer annotation = f.getAnnotation(OleSerializer.class);
                    boolean fromfile = annotation.FromFile();
                    validate = annotation.Validate();
                    String tooltip = annotation.ToolTip(),
                            selectfrom[] = annotation.SelectFrom(),
                            selectWith = annotation.SelectWith(),
                            validationlabel = annotation.ValidateLabelGUI(),
                            triggersto = annotation.TriggersTo();
                    if (!tooltip.toUpperCase().equals("NONE")) {
                        oField.setField("tooltip", tooltip);
                    }
                    if (selectfrom.length > 0) {
                        oField.set("select", Transform.toJsonArray(selectfrom));
                    }
                    if (fromfile) {
                        oField.set("file", fromfile);
                    }
                    if (!selectWith.toUpperCase().equals("NONE")) {
                        for (Method m : getConfigType().getDeclaredMethods()) {
                            if (m.getName().equals(selectWith)) {
                                String values[];
                                values = (String[]) m.invoke(null);
                                oField.set("select", Transform.toJsonArray(values));

                            }
                        }

                    }
                    if (validate) {
                        controllers.set(f.getName(), new Ole().
                                setField("label", validationlabel).
                                setField("class", getConfigType().getName()).
                                setField("method", "validateDialog"));
//                                    getConfigType().
//                                            getMethod("validate", null, getConfigType(), ActionEvent.class).invoke(null, t, u);
//                                } catch (Exception ex) {
//                                    new ExceptionHandler(ex);
//                                }
                    }
                    properties.set(f.getName(), oField);
                }
            }
            properties.set("control", controllers.toPlainJson());
        }
//        currentObjectJSON.set("properties", properties);
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    private void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    private Cryptor getMyCryptor() {
        return myCryptor;
    }

    private void setMyCryptor(Cryptor myCryptor) {
        this.myCryptor = myCryptor;
    }

    public AutoConfigurationHandler onEncryption(String password) {
        setEncrypted(true);
        setMyCryptor(new Cryptor(getPasswd()));
        return this;
    }

    public AutoConfigurationHandler offEncryption() {
        setEncrypted(false);
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Class getConfigType() {
        return configType;
    }

    public void setConfigType(Class configType) {
        this.configType = configType;
    }

    public String getConfigFolder() {
        return configFolder;
    }

    public void setConfigFolder(String configFolder) {
        this.configFolder = configFolder;
    }

    public String getFullFileName(String name) {
        return getConfigFolder() + "/" + name + ".cfg";
    }

    //
    // Configuration
    //
//    public Object newInstance() {
//        try {
//            //        getConfigType().newInstance();
//            return getConfigType().getDeclaredConstructor().newInstance();
//        } catch (Exception ex) {
//            new ExceptionHandler(ex);
//        }
//        return null;
//    }
    public boolean existsConfiguration(String name) {
        String fullFileName = getFullFileName(name);
        if (new File(fullFileName).exists()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean loadConfiguration(String name) {
        try {
            String fullFileName = getFullFileName(name);
            try {
                if (isVerbose()) {
                    System.out.println("Loading configuration from " + fullFileName);
                }
                if (!existsConfiguration(name)) {
                    if (!SwingTools.Confirm("The config filename " + fullFileName + " does not exist yet.\nDo you want to create it right now?")) {
                        return false;
                    }
                    if (!saveConfiguration(name)) {
                        SwingTools.Error("Error while saving configuration " + name + " to disk");
                    }
                }
                if (isEncrypted()) {
                    currentObjectJSON.onEncryption(myCryptor);
                }
                currentObjectJSON = new OleConfig();
                currentObjectJSON.loadFile(fullFileName);
//                currentObject = newInstance();
                setName(name);
                fromJson(currentObjectJSON);
//                currentObjectJSON.set("properties",this.properties);
                return true;
            } catch (Exception ex) {
                if (verbose) {
                    SwingTools.Error("Failed to load configuration from " + fullFileName);
                }
                new ExceptionHandler(ex);
            }
        } catch (Exception ex) {
            new ExceptionHandler(ex);
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean saveConfiguration(String name) {
        String fullFilename = this.getFullFileName(name);
        currentObjectJSON = toJson();
        if (isEncrypted()) {
            currentObjectJSON.onEncryption(myCryptor);
        }
        return currentObjectJSON.saveAsFile("./", fullFilename, true);
    }

    public void proxy(Object o, ActionEvent ae) {
        try {
            getConfigType().getDeclaredMethod("validateDialog", null, getConfigType(), ActionEvent.class);
        } catch (Exception ex) {
            new ExceptionHandler(ex);
        }
    }

    public boolean editConfiguration() {
        Ole oControls = null, ocfgAux = null;
        currentObjectJSON = toJson();
        if (currentObjectJSON.getProperties().get("control") != null) {
            oControls = currentObjectJSON.getOle("control");
        }
        if (oControls == null) {
            ocfgAux = currentObjectJSON.edit(null);
        } else {
            try {
                BiConsumer<Object, ActionEvent> consum = new BiConsumer<Object, ActionEvent>() {
                    @Override
                    public void accept(Object o, ActionEvent t) {
                        validateDialog(o, t);
                    }
                };
                ocfgAux = currentObjectJSON.edit(null, consum);
            } catch (Exception ex) {
                new ExceptionHandler(ex);
            }
        }
        if (ocfgAux != null) {
            fromJson(ocfgAux);
            return true;
        } else {
            return false;
        }

    }

    @Override

    public String toString() {
        if (isEncrypted()) {
            return myCryptor.enCrypt64(OleConfig.fromObject(this).toPlainJson().toString(WriterConfig.PRETTY_PRINT));
        } else {
            return OleConfig.fromObject(this).toPlainJson().toString(WriterConfig.PRETTY_PRINT);
        }
    }

    public void fromString(String s) {
        try {
            Ole os = new Ole(s);
            fromJson(os.toPlainJson());
//            o = getConfigType().newInstance();
//            o=Ole.oleToObject(new JsonObject().set(s), getConfigType());
//            this = (getConfigType())o;
        } catch (Exception ex) {
            if (verbose) {
                System.out.println("Faield to deserailize");
            }
            new ExceptionHandler(ex);
        }

    }

    public static OleConfig fromArray(Object obj) {
        JsonArray jsares = Transform.toJsonArray(obj);
        OleConfig res = new OleConfig();
        res.set("options", jsares);
        return res;
    }

    public static OleConfig fromEnum(Object obj) {
        OleConfig res = new OleConfig();
        if (Transform.isEnumObject(obj)) {
            res.set("options", (String) obj.toString());
            res.setDescription("enum " + obj.getClass().getName());
        }
        return res;
    }

    public OleConfig toJson() {

        OleConfig res;
        OleConfig oOptions = new OleConfig(), oProp = new OleConfig();

        Class c = this.getClass();
        Field fullFields[];
//        ArrayList<Field> fullFields = new ArrayList();
        fullFields = c.getDeclaredFields();
//        fullFields = getAllFields(fullFields, c, 0);
        for (Field f : fullFields) {
            try {
                f.setAccessible(true);
                Object otmp = f.get(this);
                if (otmp == null) {
                    continue;
                }
                Class otype = this.getClass();
                if (isPrimitiveObject(otmp)) {
                    oOptions.setField(f.getName(), otmp);
                } else if (f.getType().isEnum()) {
                    Object oenum = f.get(this);
                    oOptions.setField(f.getName(), (String) oenum.toString());
                } else if (f.getType().isArray()) {
                    JsonArray jsa;
                    if (f.get(this) != null) {
                        jsa = Transform.toJsonArray(f.get(this));
                    } else {
                        jsa = new JsonArray();
                    }
                    oOptions.setField(f.getName(), Transform.toJsonArray(f.get(this)));
                } else {
                    if (!f.getType().getTypeName().startsWith("java")) {
                        if (f.get(this) != null) {
                            Ole onested = classToOle(f.get(this));
                            oOptions.set(f.getName(), onested.get("options").asObject());
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println(ex.toString());
                new ExceptionHandler(ex);
            }
        }
        res = new OleConfig();
        res.set("options", oOptions.toPlainJson());
        res.set("properties", this.properties);
        return res;
    }

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

    public void fromJson(JsonObject jso) {
        OleConfig ocfg = new OleConfig(jso);
        ArrayList<Field> fullFields = new ArrayList(Transform.toArrayList(getClass().getDeclaredFields()));
        Field f;
        for (String s : ocfg.getOptions().names()) {
//            System.out.println(s + "  ");
            if (!s.equals(Ole.oletype.OLEMETA.name())) {
                try {
                    f = getField(getClass(), s);
                    f.setAccessible(true);
                    if (f.getType() == boolean.class) {
                        f.setBoolean(this, ocfg.getOptions().getBoolean(f.getName()));
                    } else if (f.getType() == int.class) {
                        f.setInt(this, ocfg.getOptions().get(f.getName()).asInt());
                    } else if (f.getType() == double.class) {
                        f.setDouble(this, ocfg.getOptions().getDouble(f.getName()));
                    } else if (f.getType() == String.class) {
                        f.set(this, ocfg.getOptions().getField(f.getName()));
                    } else if (f.getType().isEnum()) {
                        Class enc = f.getType();
                        f.set(this, Enum.valueOf(enc, ocfg.getOptions().getField(f.getName())));
                    } else if (f.getType().isArray()) {
                        Class arraytype = f.getType().getComponentType();
                        Object a = toArray(ocfg, f.getName());
                        if (a == null) {
                            a = Array.newInstance(arraytype, 0);
                        }
                        f.set(this, a);
                    }
                } catch (Exception ex) {
                    new ExceptionHandler(ex);
                }
            }

        }
    }

    protected Object toArray(OleConfig ocfg, String key) {
        JsonArray jsa = ocfg.getOptions().get(key).asArray();

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

    protected Object toEnum(OleConfig ole, Class c) {
        Object obj;
        try {
            obj = c.newInstance();
        } catch (Exception ex) {
            System.err.println(ex.toString());
            new ExceptionHandler(ex);
            return null;
        }
        if (Transform.isEnumObject(obj)) {
            obj = Enum.valueOf(c, ole.get("options").asString());
            return Enum.valueOf(c, ole.get("options").asString());
        } else {
            return null;
        }
    }

    public void validateDialog(Object o, ActionEvent ae) {
        System.out.println();
    }

//    protected ArrayList<Field> getAllFields(ArrayList<Field> fields, Class<?> type, int maxlevel) {
//        fields.addAll(Arrays.asList(type.getDeclaredFields()));
//
//        if (type.getSuperclass() != null && maxlevel >= 0) {
//            getAllFields(fields, type.getSuperclass(), maxlevel - 1);
//        }
//
//        return fields;
//    }
//    public Object fromString(String s) {
//        Object o;
//        try {
//            o = getConfigType().newInstance();
//            if (isVerbose()) {
//                System.out.println("Deserializing ");
//            }
////            o = getConfigType().newInstance();
////            o=Ole.oleToObject(new JsonObject().set(s), getConfigType());
//            return o;
//        } catch (Exception ex) {
//            if (verbose) {
//                System.out.println("Faield to deserailize");
//            }
//            new ExceptionHandler(ex);
//        }
//
//        return null;
//    }
//
//    public boolean reConfigure(WDConfiguration wdc) {
//        try {
//            Info("Recofiguring ");
//            myWDConfig = wdc;
//            return saveConfiguration();
//        } catch (Exception ex) {
////            HandleException(ex);
//        }
//        return fale;
//    }
}
