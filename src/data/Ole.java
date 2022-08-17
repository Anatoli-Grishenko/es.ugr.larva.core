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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        }
        return this;
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
            if (jsv.toString().contains(".")) {
                return oletype.DOUBLE.name();
            } else {
                return oletype.INTEGER.name();
            }
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
            loadFile(getFileResource(fullfilename));
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
        } else if (s instanceof Ole) {
            setField(field, (Ole) s);
        } else {
            setField(field, (String) s.toString());
        }
        return this;
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
}
