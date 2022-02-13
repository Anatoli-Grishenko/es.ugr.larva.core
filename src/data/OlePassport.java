/**
 * @file OleCardID.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

import glossary.ole;

/**
 * Class devoted to store, load and save a CardID just to handle it and 
 * being able to transfer it, without possibly decrypt it, but it can be shared. 
 */
public class OlePassport extends Ole {

    /**
     * Base constructor
     */
    public OlePassport() {
        super();
        Init();
    }
    
    /**
     * Copy constructor
     * @param o The Ole object to clone
     */
    public OlePassport(Ole o) {
        super(o);
        Init();
    }

    private final void Init() {
        setType(ole.OLEPASSPORT.name());
        this.checkField("userID");
        this.checkField("cid");
        this.checkField("alias");
        this.checkField("email");
        this.checkField("name");
    }
}
