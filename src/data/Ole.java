/**
 * @file Ole.java
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
 * every Ole subobject can override these methods to define more specific
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
 * <li>{@link OleFile} A wrapper for reading, writing and sending any file
 * through messages. Files are read as a byte sequence, encoded as a string and
 * transferred as such.
 * <li>{@link OleRecord} A wrapper for managing configurations or a set of
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
public class Ole {
//    /**
//     * Alphanumeric key for identifying each instance
//     */
//    protected String id;
//
//    /**
//     * Type of the instance, see {@link glossary.ole} constants
//     */
//    protected String type;
//
//    /**
//     * Set of fields that define this Ole object. Every Ole object has id, type,
//     * data and a common text field named description to hold additional
//     * documentation for this object
//     */
//    protected ArrayList<String> fields;

    /**
     * This is the container for the object, specified as a JsonObject
     */
    protected JsonObject data;

    /**
     * External Cryptor to encrypt/decrypt the content of any Ole object
     */
    protected Cryptor enigma;

    /**
     * Basic constructor.
     */
    public Ole() {
        Init();
    }

    /**
     * Copy constructor
     *
     * @param o The object to be cloned
     */
    public Ole(Ole o) {
        this.enigma = o.enigma;
        this.data = o.data;
    }

    /**
     * De-serialization constructor. From the serialization of any Ole object,
     * this method re-construct the object.
     *
     * @param s The String which contains the serialziation of any Ole object
     */
    public Ole(String s) {
        set(s);
    }

    public JsonObject exportToJson() {
        return Transform.OleToJson(this);
//        JsonObject res = new JsonObject();
//        for (String f : getFieldList()) {
//            String type = getFieldType(f);
//            if (type.equals(ole.INTEGER.name())) {
//                res.set(f, this.getInt(f));
//            } else if (type.equals(ole.DOUBLE.name())) {
//                res.set(f, this.getDouble(f));
//            } else if (type.equals(ole.STRING.name())) {
//                res.set(f, this.getString(f));
//            } else if (type.equals(ole.BOOLEAN.name())) {
//                res.set(f, this.getBoolean(f));
//            } else if (type.startsWith(ole.OLE.name())) {
//                res.set(f, this.getOle(f).exportToJson());
//            } else if (type.equals(ole.ARRAY.name())) {
//            }
//        }
//        return res;
    }

    public Ole(JsonObject jsole)  {
        if (jsole.get(ole.OLEMETA.name())!=null) {  // From plain OLE
            this.data = jsole;
        } else {                                                                  // from plain JSON
            this.data = Transform.JsonToOle(jsole).data;
        }
    }

    private void Init() {
        data = new JsonObject();
        data.set(ole.OLEMETA.name(), new JsonObject());
        enigma = null;
        setID(Keygen.getAlphaNumKey(16));
        setType(ole.OLE.name());
        meta().set("fields", new JsonArray());
        setDate(TimeHandler.Now());
        setDescription("Object Linked and Embeded");
        meta().set("ole", true);
    }

    private JsonObject meta() {
        if (data.get(ole.OLEMETA.name()) != null) {
            return data.get(ole.OLEMETA.name()).asObject();
        } else {
            return new JsonObject();
        }
    }

    public boolean checkField(String fieldName) {
        return data.get(fieldName) != null;
    }

    public Ole addField(String fieldName) {
        if (!checkField(fieldName)) {
            data.set(fieldName, "");
            meta().get("fields").asArray().add(fieldName);
        }
        return this;
    }

    public Ole setID(String id) {
        meta().set("id", id);
        return this;
    }

    public Ole setType(String type) {
        meta().set("type", type);
        return this;
    }

    public Ole setDate(String date) {
        meta().set("date", date);
        return this;
    }

    public Ole setDescription(String description) {
        meta().set("description", description);
        return this;
    }

    public Ole setOle() {
        meta().set("ole", true);
        return this;
    }

    public String getID() {
        return meta().getString("id", ole.BADVALUE.name());
    }

    public String getType() {
        return meta().getString("type", ole.BADVALUE.name());
    }

    public String getDate() {
        return meta().getString("date", ole.BADVALUE.name());
    }

    public String getDescription() {
        return meta().getString("description", ole.BADVALUE.name());
    }

    public boolean isOle() {
        return meta().getBoolean("ole", false);
    }

    /**
     * Gets the list of declared fields (members) of an object.
     *
     * @return A list of String with the names of the fields
     */
    public List<String> getFieldList() {
        return Transform.toArrayListString(meta().get("fields").asArray());
    }

    /**
     * Used to detect empty objects which could have returned from a method
     *
     * @return true if data is empty, false otherwise
     */
    public boolean isEmpty() {
        return this.getFieldList().isEmpty();
    }

    /**
     * Detects the activation or deactivation of encryption
     *
     * @return true if encryption is activated, false otherwise
     */
    public boolean isEncrypted() {
        return enigma != null;
    }

    public String getFieldType(String field) {
        if (checkField(field)) {
            JsonValue jsv = data.get(field);
            if (jsv.isBoolean()) {
                return ole.BOOLEAN.name();
            } else if (jsv.isString()) {
                return ole.STRING.name();
            } else if (jsv.isNumber()) {
                if (jsv.toString().contains(".")) {
                    return ole.DOUBLE.name();
                } else {
                    return ole.INTEGER.name();
                }
            } else if (jsv.isArray()) {
                return ole.ARRAY.name();
            } else {
                return getOle(field).getType();
            }
        } else {
            return ole.BADVALUE.name();
        }
    }

//vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv   
    /**
     * Most generic method to return a field. It is returned as a String
     *
     * @param field
     * @return The serialization of the field
     */
    public final String getField(String field) {
        String res = "";
        if (checkField(field)) {
            if (data.get(field).isString()) {
                res = data.get(field).asString();
            } else {
                res = data.get(field).toString();
            }
        } else {
            res = ole.BADVALUE.name();
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
        return data.getBoolean(field, false);
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
        return data.getInt(field, -1);
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
        return data.getDouble(field, -1);
    }

    /**
     * Gets the field as a String
     *
     * @param field The field to query
     * @return the value of the field. It throws an exception if the field is
     * not compatible
     */
    public final String getString(String field) {
        return data.getString(field, "");
    }

    public final Ole getOle(String field) {
        return new Ole(data.get(field).asObject());
    }

    /**
     * Gets the field as a generic ArrayList
     *
     * @param field The field to query
     * @return the value of the field. It throws an exception if the field is
     * not compatible
     */
    public final ArrayList getArray(String field) {
        return new ArrayList(Transform.toArrayList(data.get(field).asArray()));
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
        data.set(fieldname, value);
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
        data.set(fieldname, value);
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
        data.set(fieldname, value);
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
        data.set(fieldname, value);
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
        data.set(fieldname, Transform.toJsonArray(value));
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
        data.set(fieldname, value.toJson());
        return this;
    }

    public final Ole addToField(String fieldname, String v) {
        if (data.get(fieldname).isArray()) {
            data.get(fieldname).asArray().add(v);
        }
        return this;
    }

    public final Ole addToField(String fieldname, int v) {
        if (data.get(fieldname).isArray()) {
            data.get(fieldname).asArray().add(v);
        }
        return this;
    }

    public final Ole addToField(String fieldname, double v) {
        if (data.get(fieldname).isArray()) {
            data.get(fieldname).asArray().add(v);
        }
        return this;
    }

    public final Ole addToField(String fieldname, boolean v) {
        if (data.get(fieldname).isArray()) {
            data.get(fieldname).asArray().add(v);
        }
        return this;
    }

    public final Ole addToField(String fieldname, Ole v) {
        if (data.get(fieldname).isArray()) {
            data.get(fieldname).asArray().add(v.toJson());
        }
        return this;
    }

    //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    public final Ole set(JsonObject jsole) {
        Init();
        JsonObject ole = Json.parse(jsole.toString()).asObject();
        if (ole.getBoolean("ole", false)) {
            this.data = ole;
        } else {
            this.data = parseJson(jsole);
        }
        return this;
    }

    private JsonObject parseJson(JsonObject jsk) {
        Ole res = new Ole();
        for (String name : jsk.names()) {
            if (jsk.get(name).isBoolean()) {
                res.setField(name, jsk.getBoolean(name, true));
            } else if (jsk.get(name).isNumber()) {
                res.setField(name, jsk.getDouble(name, -1));
            } else if (jsk.get(name).isString()) {
                res.setField(name, jsk.getString(name, ""));
            } else if (jsk.get(name).isArray()) {
                res.setField(name, new ArrayList(Transform.toArrayList(jsk.get(name).asArray())));
            } else {
                res.setField(name, new Ole().set(jsk.get(name).asObject()));
            }
        }
        return res.toJson();
    }

    /**
     * Main method to re-construct serialialized Ole objects
     *
     * @param olestring The serialization, either encrypted or not, of the
     * object
     * @return It reconstructs the object and returns a reference to it. If the
     * string fails to be decrypted or the string does not contain a valid
     * JsonObject, then an empty Ole is returned (see {@link isEmpty}
     */
    public final Ole set(String olestring) {
        JsonObject res;
        String sdata;
        try {
            // If en
            if (this.isEncrypted()) {
                sdata = enigma.deCrypt(olestring);
            } else {
                sdata = olestring;
            }
            // Reconstruct the JsonObject main container
            res = Json.parse(sdata).asObject();
            set(res);
        } catch (Exception ex) {
        }
        return this;
    }

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
        JsonObject res = new JsonObject();
        data = new JsonObject();
        try {
            String str = new Scanner(new File(fullfilename)).useDelimiter("\\Z").next();
            set(str);
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
    public boolean saveAsFile(String outputfolder, String newname) {
        PrintWriter outfile;
        try {
            outfile = new PrintWriter(new BufferedWriter(new FileWriter(outputfolder + "/" + newname)));
        } catch (IOException ex) {
            return false;
        }
        String toRecord = toString();
        outfile.print(toRecord);
        outfile.close();
        return true;
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
        enigma = myc;
        return this;
    }

    /**
     * Deactivate the encryptioin of transactions
     *
     * @return
     */
    public Ole offEncryption() {
        enigma = null;
        return this;
    }

    /**
     * It produces the serialization of the Ole object as a String. It is usally
     * a Json dump of the object, but if encryption is activated, a sequence of
     * digits is produced instead.
     *
     * @return A String with the serialization of the object, either encrypted
     * or not
     */
    @Override
    public String toString() {
        if (enigma == null) {
            String aux = toJson().toString();
            return aux;
        } else {
            return enigma.enCrypt(toJson().toString());
        }
    }

    /**
     * Dumps the Json structure of the Ole object
     *
     * @return a JsonObject with the structure of the full Ole object.
     */
    public JsonObject toJson() {
        return data;
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
}
