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
import glossary.ole;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import tools.TimeHandler;

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
    public static enum type {
        BADVALUE, OLEMETA,
        INTEGER, DOUBLE, STRING, ARRAY, BOOLEAN,
        OLELIST,
        OLEBITMAP, OLEFILE, OLEACLM, OLEPASSPORT, OLEREPORT, OLETABLE,
        ADMINPASSPORT, DBQUERY, NOTIFICATION,
        SENSOR, REQUEST, ANSWER, RECORD, POINT, VECTOR, ENTITY,
        OLE, QUERY, DIALOG
    };

//////////////////////////////////////////// Static methods    
    public static boolean isOle(JsonObject jso) {
        if (jso.get(type.OLEMETA.name()) != null) {
            return jso.get(type.OLEMETA.name()).asObject().getBoolean("ole", false);
        } else {
            return false;
        }

    }

    public static Ole Json2Ole(JsonObject jsole) {
        return new Ole(jsole);
    }

    public static JsonObject Ole2PlainJson(Ole odata) {
        JsonObject res = new JsonObject();
        for (String f : odata.getFieldList()) {
            String type = odata.getFieldType(f);
            if (type.equals(ole.INTEGER.name())) {
                res.set(f, odata.getInt(f, -1));
            } else if (type.equals(ole.DOUBLE.name())) {
                res.set(f, odata.getDouble(f, -1));
            } else if (type.equals(ole.STRING.name())) {
                res.set(f, odata.getString(f, ""));
            } else if (type.equals(ole.BOOLEAN.name())) {
                res.set(f, odata.getBoolean(f, false));
            }
            if (type.equals(ole.ARRAY.name())) {
                res.set(f, odata.get(f).asArray());
            } else if (type.startsWith(ole.OLE.name())) {
                res.set(f, Ole2PlainJson(odata.getOle(f)));
            }
        }
        return res;
    }

    //////////////////////////////////////////// Constructors
    /**
     * Basic constructor.
     */
    public Ole() {
        super();
        Init();
    }

    public Ole(JsonObject jsole) {
        fromJson(jsole);
    }

    public Ole(String s) {
        parse(s);
    }

    protected JsonObject meta() {
        if (get(type.OLEMETA.name()) != null) {
            return get(type.OLEMETA.name()).asObject();
        } else {
            return new JsonObject();
        }
    }

    public Ole clear() {
        ArrayList<String> names = new ArrayList(this.names());
        for (String s : names) {
            this.remove(s);
        }
        return this;
    }

    protected void Init() {
        clear();
        set(type.OLEMETA.name(), new JsonObject());
        meta().set("id", Keygen.getAlphaNumKey(16));
        meta().set("type", type.OLE.name());
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

    public Ole set(String s){
        parse(s);
        return this;
    }
    public Ole fromJson(JsonObject jsole) {
        if (jsole.get(type.OLEMETA.name()) != null) {
            fromFullJson(jsole);
        } else {
            fromPlainJson(jsole);
        }
        return this;
    }

    protected Ole fromPlainJson(JsonObject jsole) {
        Init();
        for (String jsf : jsole.names()) {
            if (jsf.equals(type.OLEMETA.name())) {
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
        set(type.OLEMETA.name(), jsole.get(type.OLEMETA.name()).asObject());
        return fromPlainJson(jsole);
    }

    public JsonObject toPlainJson() {
        return Ole2PlainJson(this);
    }

    @Override
    public String toString(WriterConfig wcon) {
        if (this.isEncrypted()) {
            Cryptor myc = new Cryptor(meta().getString("crypto", ""));
            return myc.enCrypt(super.toString(wcon));
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
                definit = new Cryptor(meta().getString("crypto", "")).deCrypt(s);
            } else {
                definit = s;
            }

            jsole = Json.parse(definit).asObject();
            if (jsole.get(type.OLEMETA.name()) != null) {
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
            return meta().getString("id", type.BADVALUE.name());
        } else {
            return type.BADVALUE.name();
        }
    }

    public String getType() {
        if (isOle()) {
            return meta().getString("type", type.BADVALUE.name());
        } else {
            return type.BADVALUE.name();
        }
    }

    public String getDate() {
        if (isOle()) {
            return meta().getString("date", type.BADVALUE.name());
        } else {
            return type.BADVALUE.name();
        }
    }

    public String getDescription() {
        if (isOle()) {
            return meta().getString("description", type.BADVALUE.name());
        } else {
            return type.BADVALUE.name();
        }
    }

    public List<String> getFieldList() {
        if (get(type.OLEMETA.name()) != null) {
            return meta().get("fields").asObject().names();
        } else {
            return new ArrayList<String>();
        }
    }

    public String getFieldType(String field) {
        if (checkField(field)) {
            return getValueType(get(field));
        } else {
            return type.BADVALUE.name();
        }
    }

    public String getValueType(JsonValue jsv) {
        if (jsv.isBoolean()) {
            return type.BOOLEAN.name();
        } else if (jsv.isString()) {
            return type.STRING.name();
        } else if (jsv.isNumber()) {
            if (jsv.toString().contains(".")) {
                return type.DOUBLE.name();
            } else {
                return type.INTEGER.name();
            }
        } else if (jsv.isArray()) {
            return type.ARRAY.name();
        } else if (jsv.isObject()) {
            return type.OLE.name();
        } else {
            return type.BADVALUE.name();
        }
    }

    public final Ole getOle(String field) {
        if (get(field).isObject() && isOle(get(field).asObject())) {
            return (Ole) get(field).asObject();
        } else {
            return (Ole) new JsonObject();
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
        return meta().get("crypto").asString().length() > 0;
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
        meta().set("crypto", myc.getCryptoKey());
        return this;
    }

    /**
     * Deactivate the encryptioin of transactions
     *
     * @return
     */
    public Ole offEncryption() {
        meta().set("crypto", "");
        return this;
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
        try {
            String str = new Scanner(new File(fullfilename)).useDelimiter("\\Z").next();
            parse(str);
        } catch (Exception ex) {
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
            res = type.BADVALUE.name();
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
        return new ArrayList(Transform.toArrayList(get(field).asArray()));
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
}
