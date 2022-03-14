/**
 * @file OleCardID.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

import crypto.Cryptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class devoted to store, load and save a CardID just to handle it and being
 * able to transfer it, without possibly decrypt it, but it can be shared.
 */
public class OlePassport extends Ole {
    protected final String separator=" ";

    /**
     * Base constructor
     */
    public OlePassport() {
        super();
        InitPassport();
    }

    /**
     * Copy constructor
     *
     * @param o The Ole object to clone
     */
    public OlePassport(Ole o) {
        super(o);
        InitPassport();
    }

    public Ole loadPassport(String fullfilename) {
        String sload="", publicPassport;
        Scanner reader;
        try {
             reader = new Scanner(new File(fullfilename));
//             sload = reader.nextLine();
             sload = new Scanner(new File(fullfilename)).useDelimiter("\\Z").next();
             setField("rawPassport",sload);
             publicPassport=sload.split(separator)[0];
             setField("name",this.getCryptor().deCrypt64(publicPassport));
        } catch (FileNotFoundException ex) {
            System.err.println("Error reading passport "+fullfilename);
        }
        return this;
    }


    private final void InitPassport() {
        setType(oletype.OLEPASSPORT.name());
        this.addField("rawPassport");
        this.addField("userID");
        this.addField("cid");
        this.addField("alias");
        this.addField("email");
        this.addField("name");
        this.onEncryption(new Cryptor(Cryptor._defaultKey));
    }

    public int getUserID() {
        return getInt("userID");
    }

    public String getCid() {
        return getField("cid");
    }

    public String getAlias() {
        return getField("alias");
    }

    public String getEmail() {
        return getField("email");
    }

    public String getName() {
        return getField("name");
    }

    public OlePassport setUserID(String uid) {
        setField("userID", uid);
        return this;
    }

    public OlePassport setCid(String ucid) {
        setField("cid", ucid);
        return this;
    }

    public OlePassport setAlias(String ualias) {
        setField("alias", ualias);
        return this;
    }

    public OlePassport setEmail(String uemail) {
        setField("email", uemail);
        return this;
    }

    public OlePassport setName(String uname) {
        setField("name", uname);
        return this;
    }

}
