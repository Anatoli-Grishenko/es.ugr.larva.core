/**
 * @file JsonOle.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.WriterConfig;
import crypto.Cryptor;
import crypto.Keygen;
import data.Transform;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import swing.OleApplication;
import swing.OleDialog;
import swing.SwingTools;
import static swing.SwingTools.getFileResource;
import tools.TimeHandler;
import zip.ZipTools;

/**
 * Generic class for exchanging complex objects and disk files by using JSon as
 * the basic representation, while abstracting its use through an API that does
 * not require any Json background. It supports the following objects and
 * provides a general method for querying the content of these objects. Anyway,
 * every JsonOle subobject can override these methods to define more specific
 * access, or define new ones for its own purpose.
 * <p>
 *
 * <b>Supported fields</b><br>
 * <ul>
 * <li> String
 * <li> Double
 * <li> Integer
 * <li> Boolean
 * <li> Ole
 * <li> ArrayList Ole
 * </ul>
 *
 * <b>Supported subobjects</b><br>
 * <ul>
 * <li>{@link JsonOleFile} A wrapper for reading, writing and sending any file
 * through messages. Files are read as a byte sequence, encoded as a string and
 * transferred as such.
 * <li>{@link JsonOleRecord} A wrapper for managing configurations or a set of
 * options. Every option can be a number, a string, a boolean or another Ole
 * Object, or an array of all the previous types.
 * </ul>
 * <p>
 *
 * <b>Encryption</b><br>
 * Ole Object can also be encrypted, so they can be transferred privately. In
 * order to do that a {@link Cryptor} object is added to the class. This Cryptor
 * object already has its 128-bits key initialized from outside, although it
 * coud be changed within Ole objects.
 *
 * Encryption can be activated and deactivated with the appropriate methods. In
 * case of activation, and extrernal instance of Cryptor must be provided.
 */
public class Ole extends JsonObject {

    //////////////////////////////////////////// Static constants
    public static enum oletype {
        BADVALUE, OLEMETA,
        INTEGER, DOUBLE, STRING, ARRAY, BOOLEAN,
        OLELIST, OLEBITMAP, OLEFILE, OLEACLM, OLEPASSPORT, OLEREPORT,
        OLETABLE, OLEQUERY, OLERECORD, OLESENSOR, OLEPOINT, OLECONFIG, OLEDOT, OLEMENU,
        ADMINPASSPORT, DBQUERY, NOTIFICATION,
        REQUEST, ANSWER, VECTOR, ENTITY,
        OLE, DIALOG
    };

    Cryptor myCryptor;

    public static boolean isOle(String oString) {
        try {
            return isOle(Json.parse(oString).asObject());
        } catch (Exception ex) {
            return false;
        }
    }

//////////////////////////////////////////// Static methods    
    /**
     * @brief Determine whether a JsonObject is also a Ole object
     * @param jso a regular JsonObject
     * @return
     */
    public static boolean isOle(JsonObject jso) {
        if (jso.get(oletype.OLEMETA.name()) != null) {
            return jso.get(oletype.OLEMETA.name()).asObject().getBoolean("ole", false);
        } else {
            return false;
        }

    }

    /**
     * @brief Import a plain JsonObject into a valie Ole Object
     * @param jsole The JsonObject
     * @return A valid importation to a Ole Object
     */
    public static Ole Json2Ole(JsonObject jsole) {
        return new Ole(jsole);
    }

    protected static JsonValue Ole2JsonValue(JsonValue jsobject) {
        JsonValue jsvres;
        if (jsobject.isArray()) {
            jsvres = new JsonArray();
            for (int i = 0; i < jsobject.asArray().size(); i++) {
                jsvres.asArray().add(Ole2JsonValue(jsobject.asArray().get(i)));
            }
        } else if (jsobject.isObject()) { // es Ole
            jsvres = new JsonObject();
            for (String f : jsobject.asObject().names()) {
                if (f.equals(oletype.OLEMETA.name())) {
                    continue;
                }
                jsvres.asObject().set(f, Ole2JsonValue(jsobject.asObject().get(f)));
            }
        } else {
            jsvres = jsobject;
        }
        return jsvres;
    }

    /**
     * @brief Export a valid Ole Object into a plain JsonObject, removing any
     * META information
     * @param odata The Ole object to export
     * @return A valid JsonObject without any META information
     */
    public static JsonObject Ole2PlainJson(Ole odata) {
        return Ole2JsonValue(odata).asObject();
    }

    /**
     * @brief Gives all fieldnames included in a, poissible nested, JsonObject
     * @param jso the JsonObject
     * @return A list of all field names
     */
    public static List<String> allNames(JsonObject jso) {
        List<String> res = new ArrayList();
        for (String s : jso.names()) {
            if (!jso.get(s).isObject()) {
                res.add(s);
            } else {
                res.addAll(allNames(jso.get(s).asObject()));
            }
        }
        return res;
    }

    //////////////////////////////////////////// Constructors
    /**
     * @brief Basic constructor.
     */
    public Ole() {
        super();
        Init();
    }

    /**
     * @brief Import constructor
     * @param jsole
     */
    public Ole(JsonObject jsole) {
        fromJson(jsole);
    }

    /**
     * @brief Parse constructor
     * @param s
     */
    public Ole(String s) {
        parse(s);
    }

    protected JsonObject meta() {
        if (get(oletype.OLEMETA.name()) != null) {
            return get(oletype.OLEMETA.name()).asObject();
        } else {
            return new JsonObject();
        }
    }

    /**
     * @brief removes all fields
     * @return
     */
    public Ole clear() {
        ArrayList<String> names = new ArrayList(this.getFieldList());
        for (String s : names) {
            this.remove(s);
        }
        return this;
    }

    protected void Init() {
        clear();
        set(oletype.OLEMETA.name(), new JsonObject());
        meta().set("id", Keygen.getAlphaNumKey(16));
        meta().set("type", oletype.OLE.name());
        meta().set("fields", new JsonObject());
        meta().set("date", TimeHandler.Now());
        meta().set("description", "JSON Object Linked and Embeded");
        meta().set("ole", true);
        meta().set("crypto", "");
    }

//////////////////////////////////////////// Export/Import    
    public boolean isOle() {
        return isOle(this);
    }

    public boolean isEmpty() {
        return getFieldList().size() == 0;
    }

    public Ole set(String s) {
        parse(s);
        return this;
    }

    public Ole fromJson(JsonObject jsole) {
        if (jsole.get(oletype.OLEMETA.name()) != null) {
            fromFullJson(jsole);
        } else {
            fromPlainJson(jsole);
        }
        return this;
    }

    protected Ole fromPlainJson(JsonObject jsole) {
        Init();
        for (String jsf : jsole.names()) {
            if (jsf.equals(oletype.OLEMETA.name())) {
                continue;
            }
            if (jsole.get(jsf).isBoolean()) {
                set(jsf, jsole.get(jsf).asBoolean());
            } else if (jsole.get(jsf).isNumber()) {
                if (jsole.get(jsf).toString().contains(".") || jsole.get(jsf).toString().contains(",")) {
                    set(jsf, jsole.get(jsf).asDouble());
                } else {
                    set(jsf, jsole.get(jsf).asInt());
                }
            } else if (jsole.get(jsf).isString()) {
                set(jsf, jsole.get(jsf).asString());
            } else if (jsole.get(jsf).isObject()) {
                set(jsf, new Ole(jsole.get(jsf).asObject()));
            } else if (jsole.get(jsf).isArray()) {
                set(jsf, jsole.get(jsf).asArray());
            }
        }
        return this;
    }

    protected Ole fromFullJson(JsonObject jsole) {
        clear();
        fromPlainJson(jsole);
        set(oletype.OLEMETA.name(), jsole.get(oletype.OLEMETA.name()).asObject());
        return this;
    }

    public JsonObject toPlainJson() {
        return Ole2PlainJson(this);
    }

    @Override
    public String toString(WriterConfig wcon) {
        if (this.isEncrypted()) {
            return myCryptor.enCrypt64(super.toString(wcon));
        } else {
            return super.toString(wcon);
        }
    }

    @Override
    public String toString() {
        return toString(WriterConfig.MINIMAL);
    }

    public Ole parse(String s) {
        try {
            JsonObject jsole;
            String definit;
            if (this.isEncrypted()) {
                definit = myCryptor.deCrypt(s);
            } else {
                definit = s;
            }

            jsole = Json.parse(definit).asObject();
            if (jsole.get(oletype.OLEMETA.name()) != null) {
                return fromFullJson(jsole);
            } else {
                return fromPlainJson(jsole);
            }

        } catch (Exception ex) {
            System.err.println(ex.toString());
            return null;
        }
    }
//////////////////////////////////////////// Fields

    public boolean checkField(String fieldName) {
        return meta().get("fields").asObject().names().contains(fieldName);
    }

    public Ole addField(String fieldName) {
        if (!checkField(fieldName)) {
            meta().get("fields").asObject().set(fieldName, "");
        }
        return this;
    }

    public Ole setID(String id) {
        if (!isOle()) {
            Init();
        }
        meta().set("id", id);
        return this;
    }

    public Ole setType(String type) {
        if (!isOle()) {
            Init();
        }
        meta().set("type", type);
        return this;
    }

    public Ole setDate(String date) {
        if (!isOle()) {
            Init();
        }
        meta().set("date", date);
        return this;
    }

    public Ole setDescription(String description) {
        if (!isOle()) {
            Init();
        }
        meta().set("description", description);
        return this;
    }

    public Ole setOle() {
        if (!isOle()) {
            Init();
        }
        meta().set("ole", true);
        return this;
    }

    public String getID() {
        if (isOle()) {
            return meta().getString("id", oletype.BADVALUE.name());
        } else {
            return oletype.BADVALUE.name();
        }
    }

    public String getType() {
        if (isOle()) {
            return meta().getString("type", oletype.BADVALUE.name());
        } else {
            return oletype.BADVALUE.name();
        }
    }

    public String getDate() {
        if (isOle()) {
            return meta().getString("date", oletype.BADVALUE.name());
        } else {
            return oletype.BADVALUE.name();
        }
    }

    public String getDescription() {
        if (isOle()) {
            return meta().getString("description", oletype.BADVALUE.name());
        } else {
            return oletype.BADVALUE.name();
        }
    }

    public List<String> getFieldList() {
        if (get(oletype.OLEMETA.name()) != null) {
            return meta().get("fields").asObject().names();
        } else {
            return new ArrayList<String>();
        }
    }

    public String getFieldType(String field) {
        if (checkField(field)) {
            return getValueType(get(field));
        } else {
            return oletype.BADVALUE.name();
        }
    }

    public String getValueType(JsonValue jsv) {
        if (jsv.isBoolean()) {
            return oletype.BOOLEAN.name();
        } else if (jsv.isString()) {
            return oletype.STRING.name();
        } else if (jsv.isNumber()) {
            return oletype.DOUBLE.name();
//            if (jsv.toString().contains(".")) {
//                return oletype.DOUBLE.name();
//            } else {
//                return oletype.INTEGER.name();
//            }
        } else if (jsv.isArray()) {
            return oletype.ARRAY.name();
        } else if (jsv.isObject()) {
            return oletype.OLE.name();
        } else {
            return oletype.BADVALUE.name();
        }
    }

    public final Ole getOle(String field) {
        if (get(field) != null && get(field).isObject()) { // && isOle(get(field).asObject())) {
            return (Ole) get(field).asObject();
        } else {
            return new Ole(new JsonObject());
        }

    }

    @Override
    public Ole set(String field, boolean value) {
        super.set(field, value);
        addField(field);
        return this;
    }

    @Override
    public Ole set(String field, int value) {
        super.set(field, value);
        addField(field);
        return this;
    }

    @Override
    public Ole set(String field, double value) {
        super.set(field, value);
        addField(field);
        return this;
    }

    @Override
    public Ole set(String field, String value) {
        super.set(field, value);
        addField(field);
        return this;
    }

    public Ole set(String field, JsonArray value) {
        super.set(field, value);
        addField(field);
        return this;
    }

    public Ole set(String field, Ole value) {
        super.set(field, value);
        addField(field);
        return this;
    }

    //////////////////////////////////////////// Crypto  
    public boolean isEncrypted() {
        return myCryptor != null;
    }

    /**
     * Activates the encryption of the transactions. An extrernal
     * {@link crypto.Cryptor} must be provided, with its encryptio key already
     * set.
     *
     * @param myc The external instance of an Cryptor
     * @return A reference to the instance
     */
    public Ole onEncryption(Cryptor myc) {
        myCryptor = myc;
        return this;
    }

    /**
     * Deactivate the encryptioin of transactions
     *
     * @return
     */
    public Ole offEncryption() {
        myCryptor = null;
        return this;
    }

    public Cryptor getCryptor() {
        return this.myCryptor;
    }

    ////////////////////// File I/O
    /**
     * It reads the serialization, either encrypted or not, of a previous Ole
     * object saved into disk. That is, the kind of files supported by this
     * method are only those compatible with a serialziation of an Ole object,
     * that is to say, this is not the way to wrap a generic file, in order to
     * do that, an instance of {@link OleFile} should be used instead
     *
     * @param fullfilename of the serialization file to be read
     * @return A reference to the instance
     */
    public Ole loadFile(String fullfilename) {
        if (fullfilename.startsWith("/resources")) {
            loadFile(getFileResource(fullfilename.substring(1)));
            return this;
        } else {
            try {
                String str = new Scanner(new File(fullfilename)).useDelimiter("\\Z").next();
                if (str.contains("resource:/")) {
                    String base = getClass().getResource("/resources/").toString();
                    str = str.replaceAll("resource:/resources/", base);
                }
//                Pattern resourcePattern = Pattern.compile("@@.*@@");
//                Matcher resourcesID = resourcePattern.matcher(str);
//                if (resourcesID.find()) {
//                    for (int i = 0; i < resourcesID.groupCount(); i++) {
//                        String base = resourcesID.group(i).replaceAll("@@", "");
//                        PrintStream ops = new PrintStream(new File("./base"+i+".png"));
//                        ops.print(Arrays.toString(SwingTools.getBytesResource("resources/"+base)));
//                        str = str.replace(resourcesID.group(i),base);
//                    }
//                }
                parse(str);
            } catch (Exception ex) {
                System.err.println("(OLE) Error loading file " + fullfilename + " " + ex.toString());
            }
            return this;
        }
    }

    public Ole loadFile(InputStream is) {
        try {
            String str = new Scanner(is).useDelimiter("\\Z").next();
//            Pattern resourcePattern = Pattern.compile("@@.*@@");
//            Matcher resourcesID = resourcePattern.matcher(str);
//            int i = 0;
//            while (resourcesID.find()) {
//                String base, basefile;
//                base = resourcesID.group();
//                basefile = base.replaceAll("@@", "");
//                String replacement = "./base" + i++ + ".png";
//                PrintStream ops = new PrintStream(new File(replacement));
//                ops.print(Arrays.toString(SwingTools.getBytesResource("resources/" + base)));
//                ops.close();
//                str = str.replace(base, replacement);
//
//            }
//                if (resourcesID.find()) {
//                    for (int i = 0; i < resourcesID.groupCount(); i++) {
//                        String base = resourcesID.group(i).replaceAll("@@", "");
//                        PrintStream ops = new PrintStream(new File("./base"+i+".png"));
//                        ops.print(Arrays.toString(SwingTools.getBytesResource("resources/"+base)));
//                        str = str.replace(resourcesID.group(i),base);
//                    }
//                }
            if (str.contains("resource:/")) {
                String base = getClass().getResource("/resources/").toString();
                str = str.replaceAll("resource:/resources/", base);
            }
            parse(str);
        } catch (Exception ex) {
            System.err.println("(OLE) Error loading inputstream " + is.toString() + " " + ex.toString());
        }
        return this;
    }

    /**
     * It saves the serialization of the Ole object into a disk file. This is
     * usually a text file with a Json dump of the content of the Ole object. If
     * encryption is activated, then it is also stored as a text file, but it
     * only contains a sequence of digits as the ecnryption of the serialization
     *
     * @param outputfolder Path to the saved file. It must exist beforehand
     * @param newname Name of the file
     * @return A boolean indicating if the operation has been succesfull or not
     */
    public boolean saveAsFile(String outputfolder, String newname, boolean plainJson) {
        PrintWriter outfile;
        try {
            outfile = new PrintWriter(new BufferedWriter(new FileWriter(outputfolder + "/" + newname)));
        } catch (IOException ex) {
            return false;
        }
        String toRecord;
        if (isEncrypted()) {
            toRecord = toString(WriterConfig.PRETTY_PRINT);
        } else {
            if (plainJson) {
                toRecord = this.toPlainJson().toString(WriterConfig.PRETTY_PRINT);
            } else {
                toRecord = toString(WriterConfig.PRETTY_PRINT);
            }
        }
        outfile.print(toRecord);
        outfile.close();
        return true;
    }

    ///////////////////////////////// Backwards compat
    /**
     * Most generic method to return a field. It is returned as a String
     *
     * @param field
     * @return The serialization of the field
     */
    public final String getField(String field) {
        String res = "";
        if (checkField(field)) {
            if (get(field).isString()) {
                res = get(field).asString();
            } else {
                res = get(field).toString();
            }
        } else {
            res = oletype.BADVALUE.name();
        }
        return res;
    }

    /**
     * Gets the field as a boolean
     *
     * @param field The field to query
     * @return the value of the field. It throws an exception if the field is
     * not compatible
     */
    public final boolean getBoolean(String field) {
        return getBoolean(field, false);
//        return Boolean.parseBoolean(getField(field));
    }

    /**
     * Gets the field as an integer
     *
     * @param field The field to query
     * @return the value of the field. It throws an exception if the field is
     * not compatible
     */
    public final int getInt(String field) {
        return getInt(field, -1);
//        return Integer.parseInt(getField(field));
    }

    /**
     * Gets the field as a double
     *
     * @param field The field to query
     * @return the value of the field. It throws an exception if the field is
     * not compatible
     */
    public final double getDouble(String field) {
        return getDouble(field, -1);
    }

    /**
     * Gets the field as a String
     *
     * @param field The field to query
     * @return the value of the field. It throws an exception if the field is
     * not compatible
     */
    public final String getString(String field) {
        return getString(field, "");
    }

    /**
     * Gets the field as a generic ArrayList
     *
     * @param field The field to query
     * @return the value of the field. It throws an exception if the field is
     * not compatible
     */
    public final ArrayList getArray(String field) {
        if (get(field) != null && getFieldType(field).equals(oletype.ARRAY.name())) {
            return new ArrayList(Transform.toArrayList(get(field).asArray()));
        } else {
            return null;
        }
    }

    public Object getMetaField(String field) {
        return getField(field);
    }

    /**
     * It sets the value of the field
     *
     * @param fieldname The name of the field. If the field does not exist, it
     * adds it to the fields list
     * @param value Value of the field. It can be any of the types supported by
     * Ole
     * @return A reference to the instance
     */
    public final Ole setField(String fieldname, String value) {
        addField(fieldname);
        set(fieldname, value);
        return this;
    }

    /**
     * It sets the value of the field
     *
     * @param fieldname The name of the field. If the field does not exist, it
     * adds it to the fields list
     * @param value Value of the field. It can be any of the types supported by
     * Ole
     * @return A reference to the instance
     */
    public final Ole setField(String fieldname, int value) {
        addField(fieldname);
        set(fieldname, value);
        return this;
    }

    /**
     * It sets the value of the field
     *
     * @param fieldname The name of the field. If the field does not exist, it
     * adds it to the fields list
     * @param value Value of the field. It can be any of the types supported by
     * Ole
     * @return A reference to the instance
     */
    public final Ole setField(String fieldname, double value) {
        addField(fieldname);
        set(fieldname, value);
        return this;
    }

    /**
     * It sets the value of the field
     *
     * @param fieldname The name of the field. If the field does not exist, it
     * adds it to the fields list
     * @param value Value of the field. It can be any of the types supported by
     * Ole
     * @return A reference to the instance
     */
    public final Ole setField(String fieldname, boolean value) {
        addField(fieldname);
        set(fieldname, value);
        return this;
    }

    /**
     * It sets the value of the field
     *
     * @param fieldname The name of the field. If the field does not exist, it
     * adds it to the fields list. The elements of the array can only be those
     * supported by Ole, otherwise, they are stored as their toString().
     * @param value Value of the field. It can be any of the types supported by
     * Ole
     * @return A reference to the instance
     */
    public final Ole setField(String fieldname, ArrayList<Object> value) {
        addField(fieldname);
        set(fieldname, Transform.toJsonArray(value));
        return this;
    }

    /**
     * It sets the value of the field
     *
     * @param fieldname The name of the field. If the field does not exist, it
     * adds it to the fields list
     * @param value Value of the field. It can be any of the types supported by
     * Ole
     * @return A reference to the instance
     */
    public final Ole setField(String fieldname, Ole value) {
        addField(fieldname);
        set(fieldname, value);
        return this;
    }

    public final Ole addToField(String fieldname, String v) {
        if (get(fieldname).isArray()) {
            get(fieldname).asArray().add(v);
        }
        return this;
    }

    public final Ole addToField(String fieldname, int v) {
        if (get(fieldname).isArray()) {
            get(fieldname).asArray().add(v);
        }
        return this;
    }

    public final Ole addToField(String fieldname, double v) {
        if (get(fieldname).isArray()) {
            get(fieldname).asArray().add(v);
        }
        return this;
    }

    public final Ole addToField(String fieldname, boolean v) {
        if (get(fieldname).isArray()) {
            get(fieldname).asArray().add(v);
        }
        return this;
    }

    public final Ole addToField(String fieldname, Ole v) {
        if (get(fieldname).isArray()) {
            get(fieldname).asArray().add(v);
        }
        return this;
    }

    public Ole setFieldGeneric(String field, Object s) {
        if (s instanceof String) {
            setField(field, (String) s);
        } else if (s instanceof Integer) {
            setField(field, (Integer) s);
        } else if (s instanceof Double) {
            setField(field, (Double) s);
        } else if (s instanceof Boolean) {
            setField(field, (Boolean) s);
        } else if (s instanceof Enum) {
//            String sc = ((<E extends <Enum E>> Class <E>) s).;
//            setField(field, getEnumString(s));
        } else if (s instanceof Ole) {
            setField(field, (Ole) s);
        } else {
            setField(field, (String) s.toString());
        }
        return this;
    }

    public Ole setFieldGeneric2(String field, Object s, Class c) {
        if (String.class.isInstance(s)) {
            setField(field, (String) s);
        } else if (Integer.class.isInstance(s)) {
            setField(field, (Integer) s);
        } else if (Double.class.isInstance(s)) {
            setField(field, (Double) s);
        } else if (Boolean.class.isInstance(s)) {
            setField(field, (Boolean) s);
        } else if (Enum.class.isInstance(s)) {
            Object oo[] = c.getEnumConstants();
            String n = "";
            for (Object o : oo) {
                if (o.equals(s)) {
                    n = o.toString();
                }
            }
            setField(field, n);
        } else if (Ole.class.isInstance(s)) {
            setField(field, (Ole) s);
        } else {
            setField(field, (String) s.toString());
        }
        return this;
    }

//    public static 
//            String getEnumString(Object o) {
//        return ((<E extends Enum<E>>)o).
//    }
//
    public static <E extends Enum<E>>
            String getEnumString(Class<E> clazz, String s) {
        for (E en : EnumSet.allOf(clazz)) {
            if (en.name().equalsIgnoreCase(s)) {
                return en.name();
            }
        }
        return null;
    }

//    public void serialize(Object o, Class c) {
//        Ole res = new Ole();
//        String getterName;
//        ArrayList<Field> myFields,
//                fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
//        myFields = fullFields;
//        for (Field f : myFields) {
//            try {
//                getterName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
//                Method getter = c.getDeclaredMethod(getterName);
//                if (f.getType() == boolean.class
//                        || f.getType() == int.class
//                        || f.getType() == long.class
//                        || f.getType() == double.class
//                        || f.getType() == String.class) {
//                    res.setFieldGeneric(f.getName(), getter.invoke(this));
//                }
//            } catch (Exception ex) {
//            }
//        }
//        return res;
//
//    }
    public String forceFieldString(String field) {
        if (get(field) == null) {
            return null;
        } else if (get(field).isString()) {
            return get(field).asString();
        } else {
            return null;
        }
    }

    public int forceFieldInt(String field) {
        if (get(field) == null) {
            return -1;
        } else if (get(field).isNumber()) {
            return getInt(field, -1);
        } else if (get(field).isString()) {
            try {
                return Integer.parseInt(getField(field));
            } catch (Exception ex) {
                return -1;
            }
        }
        return -1;

    }

    public double forceFieldDouble(String field) {
        if (get(field) == null) {
            return -1;
        } else if (get(field).isNumber()) {
            return getDouble(field, -1);
        } else if (get(field).isString()) {
            try {
                return Double.parseDouble(getField(field));
            } catch (Exception ex) {
                return -1;
            }
        }
        return -1;

    }

    public boolean forceFieldBoolean(String field) {
        if (get(field) == null) {
            return false;
        } else if (get(field).isBoolean()) {
            return getBoolean(field, false);
        } else if (get(field).isString()) {
            try {
                return Boolean.parseBoolean(getField(field));
            } catch (Exception ex) {
                return false;
            }
        }
        return false;

    }

    public Ole zipMe() {
        Ole res = new Ole();
        String tozip = this.toString();
        JsonArray data = new JsonArray();
        try {
            byte[] bytedata = ZipTools.zipToByte(tozip);
            for (int i = 0; i < bytedata.length; i++) {
                data.add((int) bytedata[i]);
            }
        } catch (Exception ex) {
        }
        res.addField("zipdata");
        res.set("zipdata", data);
        return res;
    }

    public Ole zipThis(String tozip) {
        Ole res = new Ole();
        JsonArray data = new JsonArray();
        try {
            byte[] bytedata = ZipTools.zipToByte(tozip);
            for (int i = 0; i < bytedata.length; i++) {
                data.add((int) bytedata[i]);
            }
        } catch (Exception ex) {
        }
        res.addField("zipdata");
        res.set("zipdata", data);
        return res;
    }

    public Ole Unzip(Ole zipOle) {
        if (zipOle.get("zipdata") == null) {
            return this;
        }
        JsonArray content = zipOle.get("zipdata").asArray();
        byte[] bytedata = new byte[content.size()];
        for (int i = 0; i < bytedata.length; i++) {
            bytedata[i] = (byte) content.get(i).asInt();
        }
        try {
            String fromZip = ZipTools.unzipByte(bytedata);
            this.parse(fromZip);
        } catch (Exception ex) {
        }
        return this;
    }

    public String UnzipThis(Ole zipOle) {
        if (zipOle.get("zipdata") == null) {
            return "";
        }
        JsonArray content = zipOle.get("zipdata").asArray();
        byte[] bytedata = new byte[content.size()];
        for (int i = 0; i < bytedata.length; i++) {
            bytedata[i] = (byte) content.get(i).asInt();
        }
        try {
            String fromZip = ZipTools.unzipByte(bytedata);
            return fromZip;
        } catch (Exception ex) {
        }
        return "";
    }

//    public static Ole toOle(AutoOle obj) {
//        Ole res = new Ole();
//        Class c = obj.myClass();
//        ArrayList<Field> myFields, fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
//        myFields = fullFields;
//        for (Field f : myFields) {
//            String getterName;
//            if (f.getType() == boolean.class) {
//                getterName = "is" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
//            } else {
//                getterName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
//            }
//            try {
//                Method getter = c.getDeclaredMethod(getterName);
//                if (f.getType() == boolean.class
//                        || f.getType() == int.class
//                        || f.getType() == long.class
//                        || f.getType() == double.class
//                        || f.getType() == String.class) {
//                    res.setFieldGeneric(f.getName(), getter.invoke(obj));
//                }
//            } catch (Exception ex) {
//            }
//        }
//        return res;
//    }
    public static Ole toOle2(Object obj) {
        Ole res = new Ole();
        Class c = obj.getClass();
        ArrayList<Field> myFields,
                fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
        myFields = fullFields;
        for (Field f : myFields) {
            try {
                f.setAccessible(true);
                if (f.getType() == boolean.class) {
                    res.setField(f.getName(), f.getBoolean(obj));
                } else if (f.getType() == int.class) {
                    res.setField(f.getName(), f.getInt(obj));
                } else if (f.getType() == double.class) {
                    res.setField(f.getName(), f.getDouble(obj));
                } else if (f.getType() == String.class) {
                    res.setField(f.getName(), (String) f.get(obj));
                } else if (f.getType().isEnum()) {
                    String getterName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
                    Method getter = c.getDeclaredMethod(getterName);
                    Object oenum = getter.invoke(obj);
                    res.setField(f.getName(), (String) oenum.toString());
                }
            } catch (Exception ex) {
                System.err.println(ex.toString());
            }
        }
        return res;
    }

    public static void singleDataToOle(Object ob, Class c, Field f, Ole ol) {
        try {
            if (f.getType() == boolean.class) {
                ol.setField(f.getName(), f.getBoolean(ob));
            } else if (f.getType() == int.class) {
                ol.setField(f.getName(), f.getInt(ob));
            } else if (f.getType() == double.class) {
                ol.setField(f.getName(), f.getDouble(ob));
            } else if (f.getType() == String.class) {
                ol.setField(f.getName(), (String) f.get(ob));
            } else if (f.getType().isEnum()) {
                String getterName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
                Method getter = c.getDeclaredMethod(getterName);
                Object oenum = getter.invoke(ob);
                ol.setField(f.getName(), (String) oenum.toString());
//                    String labels[] = Transform.toArrayString(Transform.getAllNames(c));
                ArrayList<String> alValues = new ArrayList();
                for (Object ov : f.getType().getEnumConstants()) {
                    alValues.add(ov.toString());
                }
                ol.add(f.getName(), new JsonObject().add("select", Transform.toJsonArray(new ArrayList(alValues))));
            }
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
    }

//    public static JsonArray arrrayToOle(Object ob, Class c, Array a, Ole ol) {
//        JsonArray res = new JsonArray();
//        Ole oaux;
//        for (int i = 0; i < Array.getLength(a); i++) {
//            try {
//                if (Array.get(a, i).getClass() == boolean.class) {
//                    res.add(Array.getBoolean(ob, i));
//                } else if (Array.get(a, i).getClass() == int.class) {
//                    res.add(Array.getInt(ob, i));
//                } else  if (Array.get(a, i).getClass() == double.class) {
//                    res.add(Array.getDouble(ob, i));
//                } else if (Array.get(a, i).getClass() == String.class) {
//                    res.add((String)Array.get(ob, i));
//                } else if (f.getType().isEnum()) {
//                    String getterName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
//                    Method getter = c.getDeclaredMethod(getterName);
//                    Object oenum = getter.invoke(ob);
//                    ol.setField(f.getName(), (String) oenum.toString());
////                    String labels[] = Transform.toArrayString(Transform.getAllNames(c));
//                    ArrayList<String> alValues = new ArrayList();
//                    for (Object ov : f.getType().getEnumConstants()) {
//                        alValues.add(ov.toString());
//                    }
//                    ol.add(f.getName(), new JsonObject().add("select", Transform.toJsonArray(new ArrayList(alValues))));
//                }
//            } catch (Exception ex) {
//                System.err.println(ex.toString());
//            }
//        }
//    }
//
//    public static Ole toOle4(Object obj) {
//        Ole res = new Ole(), oOptions = new Ole(), oProp = new Ole();
//        Class c = obj.getClass();
//        ArrayList<Field> myFields,
//                fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
//        myFields = fullFields;
//        for (Field f : myFields) {
//            f.setAccessible(true);
//            if (f.getType() == boolean.class
//                    || f.getType() == int.class
//                    || f.getType() == double.class
//                    || f.getType() == String.class
//                    || f.getType().isEnum()) {
//                singleDataToOle(obj, c, f, oProp);
//            } else if (f.getType().isArray()) {
//                JsonArray jsa = new JsonArray();
//                for (int i = 0; i < Array.getLength(f); i++) {
//
//                }
//            }
//        }
//        res = new Ole();
//        res.add("options", new Ole(oOptions));
//        res.add("properties", new Ole(oProp));
//        return res;
//    }
    public static Ole toOle3(Object obj, boolean readonly) {
        Ole res = new Ole(), oOptions = new Ole(), oProp = new Ole();
        Class c = obj.getClass();
        ArrayList<Field> myFields,
                fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
        myFields = fullFields;
        for (Field f : myFields) {
            try {
                f.setAccessible(true);
                if (f.getType() == boolean.class) {
                    if (Modifier.isPrivate(f.getModifiers()) && readonly) {
                        oOptions.setField(f.getName(), "<html><b>" + f.getName() + "   </b> " + f.getBoolean(obj) + "</html>");
                    } else {
                        oOptions.setField(f.getName(), f.getBoolean(obj));
                    }
                } else if (f.getType() == int.class) {
                    if (Modifier.isPrivate(f.getModifiers()) && readonly) {
                        oOptions.setField(f.getName(), "<html><b>" + f.getName() + "   </b> " + f.getInt(obj) + "</html>");
                    } else {
                        oOptions.setField(f.getName(), f.getInt(obj));
                    }
                } else if (f.getType() == double.class) {
                    if (Modifier.isPrivate(f.getModifiers()) && readonly) {
                        oOptions.setField(f.getName(), "<html><b>" + f.getName() + "   </b> " + f.getDouble(obj) + "</html>");
                    } else {
                        oOptions.setField(f.getName(), f.getDouble(obj));
                    }
                } else if (f.getType() == String.class) {
                    if (Modifier.isPrivate(f.getModifiers()) && readonly) {
                        oOptions.setField(f.getName(), "<html><b>" + f.getName() + "   </b> " + ((String) f.get(obj)) + "</html>");
                    } else {
                        oOptions.setField(f.getName(), (String) f.get(obj));
                    }
                } else if (f.getType().isEnum()) {
                    String getterName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
                    Method getter = c.getDeclaredMethod(getterName);
                    Object oenum = getter.invoke(obj);
                    if (Modifier.isPrivate(f.getModifiers()) && readonly) {
                        oOptions.setField(f.getName(), "<html><b>" + f.getName() + "   </b> " + ((String) oenum.toString()) + "</html>");
                    } else {
                        oOptions.setField(f.getName(), (String) oenum.toString());
                        ArrayList<String> alValues = new ArrayList();
                        for (Object ov : f.getType().getEnumConstants()) {
                            alValues.add(ov.toString());
                        }
                        oProp.add(f.getName(), new JsonObject().add("select", Transform.toJsonArray(new ArrayList(alValues))));
                    }
//                    String labels[] = Transform.toArrayString(Transform.getAllNames(c));
                }
            } catch (Exception ex) {
                System.err.println(ex.toString());
            }
        }
        res = new Ole();
        res.add("options", oOptions.toPlainJson());
        res.add("properties", oProp.toPlainJson());
        return res;
    }

    public static Object fromOle3(Ole ole, Object obj, boolean readonly) {
        Class c = obj.getClass();
        ole = ole.getOle("options");
        ArrayList<Field> fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
        for (Field f : fullFields) {
            if (!Modifier.isPrivate(f.getModifiers()) || !readonly) {
                f.setAccessible(true);
                try {
                    if (f.getType() == boolean.class) {
                        f.setBoolean(obj, ole.getBoolean(f.getName()));
                    } else if (f.getType() == int.class) {
                        f.setInt(obj, ole.getInt(f.getName()));
                    } else if (f.getType() == double.class) {
                        f.setDouble(obj, ole.getDouble(f.getName()));
                    } else if (f.getType() == String.class) {
                        f.set(obj, ole.getField(f.getName()));
                    } else if (f.getType().isEnum()) {
                        String setterName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
                        Method setter = c.getDeclaredMethod(setterName, f.getType());
                        Class enc = f.getType();
//                    setter.invoke(obj, Enum.valueOf(enc, ole.getField(f.getName())));
                        f.set(obj, Enum.valueOf(enc, ole.getField(f.getName())));
                    } else if (f.getType().isArray()) {

                    }
                } catch (Exception ex) {
                    System.err.println(ex.toString());
                }
            }
        }
        return obj;
    }

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

    public static Ole simpleToOle(Object obj) {
        Ole res = new Ole();

        if (isSimpleObject(obj)) {
            res.setDescription(obj.getClass().getName());
            if (obj instanceof Character) {
                res.setField("value", "" + (char) obj + "");
            } else if (obj instanceof Integer) {
                res.set("value", (int) obj);
            } else if (obj instanceof Double) {
                res.set("value", (double) obj);
            } else if (obj instanceof Boolean) {
                res.set("value", (boolean) obj);
            } else if (obj instanceof String) {
                res.set("value", (String) obj);
            }
        }
        return res;
    }

    public static Ole arrayToOle(Object obj) {
        Ole res = new Ole();
        JsonArray jsares = new JsonArray();

        if (isArrayObject(obj)) {
            for (int i = 0; i < Array.getLength(obj); i++) {
                jsares.add(objectToOle(Array.get(obj, i)).get("value"));
            }
            res.set("value", jsares);
            res.setDescription(Array.get(obj, 0).getClass().getSimpleName() + "[]");
        }
        return res;
    }

    public static Ole enumToOle(Object obj) {
        Ole res = new Ole();
        if (isEnumObject(obj)) {
            res.set("value", (String) obj.toString());
            res.setDescription("enum " + obj.getClass().getName());
        }
        return res;
    }

    public static Ole objectToOle(Object obj) {
        return objectToOle(obj, 0);
    }

    public static Ole objectToOle(Object obj, int maxdepth) {
        if (isSimpleObject(obj)) {
            return simpleToOle(obj);
        } else if (isArrayObject(obj)) {
            return arrayToOle(obj);
        } else if (isEnumObject(obj)) {
            return enumToOle(obj);
        } else if (isClassObject(obj)) {
            return classToOle(obj, maxdepth);
        } else {
            return new Ole();
        }
    }

    public static Ole classToOle(Object obj) {
        return classToOle(obj, 0);
    }

    public static Ole classToOle(Object obj, int maxdepth) {
        Ole res = new Ole(), oOptions = new Ole(), oProp = new Ole();
        Class c = obj.getClass();
        res.setDescription(c.getSimpleName());
        ArrayList<Field> myFields,
                fullFields = new ArrayList();
//        fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
        fullFields = getAllFields(fullFields, c, 3);
        myFields = fullFields;
//        System.out.println("\n\nExploring class "+c.getSimpleName()+" @ level "+maxdepth+ "fields "+myFields.toString()+"\n\n"); 
        for (Field f : myFields) {
            try {
                f.setAccessible(true);
                if (isSimpleObject(f.get(obj))) {
                    if (f.get(obj) != null) {
                        oOptions.set(f.getName(), objectToOle(f.get(obj)).toPlainJson().get("value"));
                    }
                } else if (f.getType().isEnum()) {
//                    String getterName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
//                    Method getter = c.getDeclaredMethod(getterName);
//                    Object oenum = getter.invoke(obj);
                    Object oenum = f.get(obj);
                    oOptions.setField(f.getName(), (String) oenum.toString());
//                    ArrayList<String> alValues = new ArrayList();
//                    for (Object ov : f.getType().getEnumConstants()) {
//                        alValues.add(ov.toString());
//                    }
//                    oProp.add(f.getName(), new JsonObject().add("select", Transform.toJsonArray(new ArrayList(alValues))));
                } else if (f.getType().isArray()) {
                    if (f.get(obj) != null) {
                        oOptions.set(f.getName(), arrayToOle(f.get(obj)).get("value").asArray());
                    }
                } else {
                    if (maxdepth > 0 && !f.getType().getTypeName().startsWith("java")) {
                        if (f.get(obj) != null) {
                            Ole onested = classToOle(f.get(obj), maxdepth - 1);
                            oOptions.set(f.getName(), onested.get("value").asObject());
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println(ex.toString());
            }
        }
        res = new Ole();

        res.add(
                "value", oOptions.toPlainJson());
        return res;
    }

    public static Object oleToSimple(Ole o, Object obj, Class c) {
        if (c == int.class) {
            int ires = o.get("value").asInt();
            obj = ires;
            return ires;
        } else if (obj instanceof Double) {
            double dres = o.get("value").asDouble();
            obj = dres;
            return dres;
        } else if (obj instanceof Boolean) {
            boolean bres = o.get("value").asBoolean();
            obj = bres;
            return bres;
        } else if (c == String.class) {
            String sres = o.get("value").asString();
            obj = sres;
            return sres;
        }
        return new Object();
    }

//    public static Object oleToSimple(Ole o, Class c) {
//        if (isSimpleObject(c)) {
//            if (c == int.class) {
//                int ires = o.get("value").asInt();
//                return ires;
//            } else if (c == double.class) {
//                double dres = o.get("value").asDouble();
//                return dres;
//            } else if (c == boolean.class) {
//                boolean bres = o.get("value").asBoolean();
//                return bres;
//            } else if (c == String.class) {
//                String sres = o.get("value").asString();
//                return sres;
//            }
//        }
//        return new Object();
//    }
//    public static Object oleToEnum(Ole ole, Object obj) {
//        if (isEnumObject(obj)) {
//            Enum.valueOf(, o)));
//        }
//        return obj;
//    }
    public static void oleToObject(Ole ole, Object obj, Class c) {
        if (isSimpleObject(obj)) {
            obj = oleToSimple(ole, obj, c);
        } else if (isArrayObject(obj)) {
            oleToArray(ole, obj, c);
        } else if (isEnumObject(obj)) {
            oleToEnum(ole, obj, c);
        } else if (isClassObject(obj)) {
            oleToClass(ole, obj, c);
        }
    }

    public static void oleToClass(Ole ole, Object o, Class c) {
        try {
            ole = new Ole(ole.get("value").asObject());
            ArrayList<Field> fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
            Field f;
            for (String s : ole.getFieldList()) {
                try {
                    f = getField(c, s);
                    f.setAccessible(true);
//                    System.out.println("Processing member: " + f.getName());
                    if (f.getType() == boolean.class) {
                        f.setBoolean(o, ole.getBoolean(f.getName()));
                    } else if (f.getType() == int.class) {
                        f.setInt(o, ole.get(f.getName()).asInt());
                    } else if (f.getType() == double.class) {
                        f.setDouble(o, ole.getDouble(f.getName()));
                    } else if (f.getType() == String.class) {
                        f.set(o, ole.getField(f.getName()));
                    } else if (f.getType().isEnum()) {
                        Class enc = f.getType();
                        f.set(o, Enum.valueOf(enc, ole.getField(f.getName())));
                    } else if (f.getType().isArray()) {

//                        f.set(o, oleToArray(ole, o, c))
//                            Ole oaux = new Ole();
//                            oaux.add("value",ole.get(f.getName()).asArray());
//                            oleToArray(ole, o, c);
                    } else {

                    }
                } catch (Exception ex) {
                    System.err.println(ex.toString());
                }

            }

        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
    }
    //    public <T> T[] oleToArray(T[] obj) {
    //        JsonArray jsa = get("value").asArray();
    //        int size = jsa.size();
    //        T[] res;
    //        res = (T[])new Object[size];
    //        Object oelement=new Object();
    //        if (isArrayObject(obj)) {
    //            for (int i = 0; i < size; i++) {
    //                Ole ovalue = new Ole();
    //                ovalue.add("value", jsa.get(i));
    //                oelement = oleToSimple(ovalue, Array.get(obj, 0));
    //                Array.set(res, i, oelement);
    //            }
    //            obj = (T[]) new Object[size];
    //            System.arraycopy(res, 0, obj, 0, size);
    //        }
    //        return res;
    //    }

    public static void oleToArray(Ole ole, Object obj, Class c) {
        JsonArray jsa = ole.get("value").asArray();
        int size = jsa.size();
//            c = Array.get(obj, 0).getClass();
//            c = Array.get(obj, 0).getClass();
//            Object res = Array.newInstance(c, size);
//        obj = Array.newInstance(c, size);
        Object oelement = new Object();
        try {
            for (int i = 0; i < size; i++) {
                Ole ovalue = new Ole();
                ovalue.add("value", jsa.get(i));
                if (c == int.class) {
                    int iaux = 0;
                    iaux = (int) oleToSimple(ovalue, iaux, int.class);
                    Array.set(obj, i, iaux);
                } else if (c == double.class) {
                    double daux = 0;
                    daux = (double) oleToSimple(ovalue, daux, double.class);
                    Array.set(obj, i, daux);
                } else if (c == boolean.class) {
                    boolean baux = false;
                    baux = (boolean) oleToSimple(ovalue, baux, boolean.class);
                    Array.set(obj, i, baux);
                } else if (c == String.class) {
                    String saux = "";
                    saux = (String) oleToSimple(ovalue, saux, String.class);
                    Array.set(obj, i, saux);
                } else if (isClassObject(c)) {
                    Class ctemp = Class.forName(c.getCanonicalName());
                    Object cobj = ctemp.newInstance();
                    oleToClass(ovalue, cobj, c);
                    Array.set(obj, i, cobj);
                }
            }
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
//            obj = (Object[]) new Object[size];
//        System.arraycopy(res, 0, obj, 0, size);

    }

    public static Object oleToEnum(Ole ole, Object obj, Class c) {
        if (isEnumObject(obj)) {
            obj = Enum.valueOf(c, ole.get("value").asString());
            return Enum.valueOf(c, ole.get("value").asString());
        } else {
            return new Object();
        }
    }

    public static ArrayList<Field> getAllFields(ArrayList<Field> fields, Class<?> type, int maxlevel) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null && maxlevel >= 0) {
            getAllFields(fields, type.getSuperclass(), maxlevel - 1);
        }

        return fields;
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

//    public static Ole objectToOle(Object obj) {
//        if (isSimpleObject(obj)) {
//            return simpleToOle(obj);
//        } else if (isArrayObject(obj)) {
//            return arrayToOle(obj);
//        } else if (isEnumObject(obj)) {
//            return enumToOle(obj);
//        } else if (isClassObject(obj)) {
//            return classToOle(obj);
//        } else {
//            return new Ole();
//        }
//    }
//
//    public static Ole classToOle(Object obj) {
//        Ole res = new Ole(), oOptions = new Ole(), oProp = new Ole();
//        Class c = obj.getClass();
//        ArrayList<Field> myFields,
//                fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
//        myFields = fullFields;
//        for (Field f : myFields) {
//            try {
//                f.setAccessible(true);
//                if (f.get(obj) != null) {
//                    oOptions.set(f.getName(), objectToOle(f.get(obj)).toPlainJson().get("value"));
////                    if (f.getType().isEnum()) {
////                        ArrayList<String> alValues = new ArrayList();
////                        for (Object ov : f.getType().getEnumConstants()) {
////                            alValues.add(ov.toString());
////                        }
////                        oProp.add(f.getName(), new JsonObject().add("select", Transform.toJsonArray(new ArrayList(alValues))));
////                    }
//                }
//            } catch (Exception ex) {
//                System.err.println(ex.toString());
//            }
//        }
//        res = new Ole();
//
//        res.add(
//                "value", oOptions.toPlainJson());
//        return res;
//    }
//
//
//    public static Object oleToSimple(Ole ole, Object obj) {
//        Class c = obj.getClass();
//        ole = ole.getOle("options");
//        ArrayList<Field> fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
//        for (Field f : fullFields) {
//            if (!Modifier.isPrivate(f.getModifiers()) || !readonly) {
//                f.setAccessible(true);
//                try {
//                    if (f.getType() == boolean.class) {
//                        f.setBoolean(obj, ole.getBoolean(f.getName()));
//                    } else if (f.getType() == int.class) {
//                        f.setInt(obj, ole.getInt(f.getName()));
//                    } else if (f.getType() == double.class) {
//                        f.setDouble(obj, ole.getDouble(f.getName()));
//                    } else if (f.getType() == String.class) {
//                        f.set(obj, ole.getField(f.getName()));
//                    } else if (f.getType().isEnum()) {
//                        String setterName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
//                        Method setter = c.getDeclaredMethod(setterName, f.getType());
//                        Class enc = f.getType();
////                    setter.invoke(obj, Enum.valueOf(enc, ole.getField(f.getName())));
//                        f.set(obj, Enum.valueOf(enc, ole.getField(f.getName())));
//                    } else if (f.getType().isArray()) {
//
//                    }
//                } catch (Exception ex) {
//                    System.err.println(ex.toString());
//                }
//            }
//        }
//        return obj;
//    }
    public static Object fromOle2(Ole ole, Object obj) {
        Class c = obj.getClass();
        ArrayList<Field> fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
        for (Field f : fullFields) {
            f.setAccessible(true);
            try {
                if (f.getType() == boolean.class) {
                    f.setBoolean(obj, ole.getBoolean(f.getName()));
                } else if (f.getType() == int.class) {
                    f.setInt(obj, ole.getInt(f.getName()));
                } else if (f.getType() == double.class) {
                    f.setDouble(obj, ole.getDouble(f.getName()));
                } else if (f.getType() == String.class) {
                    f.set(obj, ole.getField(f.getName()));
                } else if (f.getType().isEnum()) {
                    String setterName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
                    Method setter = c.getDeclaredMethod(setterName, f.getType());
                    Class enc = f.getType();
//                    setter.invoke(obj, Enum.valueOf(enc, ole.getField(f.getName())));
                    f.set(obj, Enum.valueOf(enc, ole.getField(f.getName())));

                }
            } catch (Exception ex) {
                System.err.println(ex.toString());
            }
        }
        return obj;
    }

    public Ole edit(OleApplication parent) {
        SwingTools.doSwingWait(() -> {
            OleDialog oDlg = new OleDialog(parent, "Edit ");
            oDlg.setEdit(true);
            if (oDlg.run(new OleConfig(this))) {
                this.set(oDlg.getResult().toPlainJson().toString());
            }
        });
        return this;
    }

    public void view(OleApplication parent) {
        OleDialog oDlg = new OleDialog(parent, "View ");
        oDlg.setEdit(false);
        oDlg.run(new OleConfig(this));
    }

//    public static AutoOle fromOle(Ole ole, AutoOle obj) {
//        Class c = obj.myClass();
//        ArrayList<Field> fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
//        for (Field f : fullFields) {
//            String setterName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
//            Method setter;
//            try {
//                if (f.getType() == boolean.class) {
//                    setter = c.getDeclaredMethod(setterName, boolean.class);
//                    setter.invoke(obj, ole.getBoolean(f.getName()));
//                } else if (f.getType() == double.class) {
//                    setter = c.getDeclaredMethod(setterName, double.class);
//
//                    setter.invoke(obj, ole.getDouble(f.getName()));
//                } else if (f.getType() == int.class) {
//                    setter = c.getDeclaredMethod(setterName, int.class);
//
//                    setter.invoke(obj, ole.getInt(f.getName()));
//                } else if (f.getType().isInstance("")) {
//                    setter = c.getDeclaredMethod(setterName, String.class
//                    );
//                    setter.invoke(obj, ole.getField(f.getName()));
//                }
//            } catch (Exception ex) {
//            }
//        }
//        return obj;
//    }
//
//    public void edit(OleApplication parent) {
//    }
//    
//    public void view(OleApplication parent) {
//     }
}
