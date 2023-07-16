/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AutoConfiguration;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.WriterConfig;
import crypto.Cryptor;
import data.Ole;
import data.OleConfig;
import data.OleSerializer;
import data.Transform;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.imap.IMAPReply;
import org.glassfish.jersey.server.wadl.config.WadlGeneratorConfig;
import org.json.HTTP;
import swing.SwingTools;
import tools.ExceptionHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class AC {

    String configFolder, passwd;
    Class configType;
    boolean verbose, encrypted;
    Cryptor myCryptor;
    Ole properties, controllers;
    ArrayList<Field> fullFields;
    Object currentObject;
    BiConsumer <ActionEvent,Object> validator;

    public AC(String myConfigFolder, Class myConfigType) {
        SwingTools.initLookAndFeel("Dark");
        configFolder = myConfigFolder;
        configType = myConfigType;
        this.verbose = false;
        properties = new Ole();
        controllers = new Ole();
        fullFields = new ArrayList(Transform.toArrayList(myConfigType.getDeclaredFields()));
        if (myConfigType.isAnnotationPresent(OleSerializer.class)) {
            Ole oField = new Ole();
            for (Field f : fullFields) {
                if (f.isAnnotationPresent(OleSerializer.class)) {
                    oField = new Ole();
                    OleSerializer annotation = f.getAnnotation(OleSerializer.class);
                    boolean fromfile = annotation.FromFile(),
                            validate=annotation.Validate();
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
                    if (!fromfile) {
                        oField.set("file", fromfile);
                    }
                    if (!selectWith.toUpperCase().equals("NONE")) {
                        for (Method m : myConfigType.getDeclaredMethods()) {
                            if (m.getName().equals(selectWith)) {
                                String values[];
                                try {
                                    values = (String[]) m.invoke(null);
                                    oField.set("select", Transform.toJsonArray(values));
                                } catch (Exception ex) {
                                    new ExceptionHandler(ex);
//                                    SwingTools.Error("Call to method " + m.getName() + " failed:\n" + ex.toString());
                                }
                            }
                        }

                    }
//                    if (!validatewith.toUpperCase().equals("NONE")) {
//                        controllers.set(validationevent,new Ole().
//                                setField("label",validationlabel).
//                                setField("method",validatewith).
//                                setField("args", validationevent));
//                        if (validatorMethod == null) {
//                            try {
//                                validatorMethod = getConfigType().getMethod(validatewith, 
//                                        getConfigType(), ActionEvent.class);
//                            } catch (Exception ex) {
//                                new ExceptionHandler(ex);
//                            }
//                        }
//                    }
                    properties.set(f.getName(), oField);
                }
            }
            properties.set("control", controllers);
        }
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

    public AC onEncryption(String password) {
        setEncrypted(true);
        setMyCryptor(new Cryptor(getPasswd()));
        return this;
    }

    public AC offEncryption() {
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
    public boolean isConfiguration(String name) {
        String fullFileName = getFullFileName(name);
        if (new File(fullFileName).exists()) {
            return true;
        } else {
            return false;
        }
    }

    public Object loadConfiguration(String name) {
        OleConfig oleCfg = new OleConfig();
        try {
            currentObject = getConfigType().newInstance();
            String fullFileName = getFullFileName(name);
            try {
                if (isVerbose()) {
                    System.out.println("Loading configuration from " + fullFileName);
                }
                if (!isConfiguration(name)) {
                    if (!SwingTools.Confirm("The config filename " + fullFileName + " does not exist yet.\nDo you want to create it right now?")) {
                        return null;
                    }
                    currentObject = editConfiguration(currentObject, name);
                    if (isEncrypted()) {
                        oleCfg.onEncryption(myCryptor);
                    }
                    saveConfiguration(currentObject, name);
                }
                if (isEncrypted()) {
                    oleCfg.onEncryption(myCryptor);
                }
                oleCfg = (OleConfig) oleCfg.loadFile(fullFileName);
                currentObject = OleConfig.toObject(oleCfg, getConfigType());
                return currentObject;
            } catch (Exception ex) {
                if (verbose) {
                    SwingTools.Error("Faield to load configuration from " + fullFileName);
                }
                new ExceptionHandler(ex);
            }
        } catch (Exception ex) {
            new ExceptionHandler(ex);
            return null;
        }
        return null;
    }

    public boolean saveConfiguration(Object o, String name) {
        String fullFilename = this.getFullFileName(name);
        if (isVerbose()) {
            System.out.println("Saving configuration in " + fullFilename);
        }
        OleConfig owd = OleConfig.fromObject(o);
        owd.set("properties", properties);

//        if (useEncription) {
//            owd.onEncryption(myCryptor);
//        } else {
//            owd.offEncryption();
//        }
        if (isVerbose()) {
            System.out.println("Saving configuration into " + fullFilename);
        }
        if (isEncrypted()) {
            owd.onEncryption(myCryptor);
        }
        return owd.saveAsFile("./", fullFilename, true);
    }

    public Object editConfiguration(Object o, String name) {
//        JsonObject oole = Ole.objectToOle(o);
//        System.out.println(oole.toPlainJson().toString());
        OleConfig ooleC = OleConfig.fromObject(o);
        currentObject = o;
        ooleC.set("properties", properties);
//        if (!this.va) {
//            ooleC = ooleC.edit(null);
//        } else {
//            ooleC = ooleC.edit(null, (obj, ae) -> listener(obj,ae));
//        }
        if (ooleC != null) {
            currentObject = OleConfig.toObject(ooleC, getConfigType());
            saveConfiguration(currentObject, name);
            return currentObject;
        } else {
            return null;
        }
    }

    public static void listener(Object o, ActionEvent ae) {
//        try {
//            validatorMethod.invoke(null, o, ae);
//        } catch (Exception ex) {
//            new ExceptionHandler(ex);
//        }        
    }
//    private void hiddenValidator(ActionEvent e, OleConfig ocfg) {
//        try {
//            Object provobject = getConfigType().newInstance();
//            provobject = OleConfig.toObject(ocfg, getConfigType());
//            validatorMethod.invoke(provobject, e.getActionCommand());
//        } catch (Exception ex) {
//            new ExceptionHandler(ex);
//        }
//    }

    public String toString(Object o) {
//        return Ole.toOle3(o, true).toPlainJson().toString(WriterConfig.PRETTY_PRINT);
        if (isEncrypted()) {
            return myCryptor.enCrypt64(Ole.objectToOle(o).toPlainJson().toString(WriterConfig.PRETTY_PRINT));
        } else {
            return Ole.objectToOle(o).toPlainJson().toString(WriterConfig.PRETTY_PRINT);
        }
    }

    public Object fromString(String s) {
        Object o;
        try {
            o = getConfigType().newInstance();
            if (isVerbose()) {
                System.out.println("Deserializing ");
            }
//            o = getConfigType().newInstance();
//            o=Ole.oleToObject(new JsonObject().set(s), getConfigType());
            return o;
        } catch (Exception ex) {
            if (verbose) {
                System.out.println("Faield to deserailize");
            }
            new ExceptionHandler(ex);
        }

        return null;
    }

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
