/**
 * @file OleCardID.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

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
        InitPassport();
    }
    
    /**
     * Copy constructor
     * @param o The Ole object to clone
     */
    public OlePassport(Ole o) {
        super(o);
        InitPassport();
    }

    private final void InitPassport() {
        setType(oletype.OLEPASSPORT.name());
        this.addField("userID");
        this.addField("cid");
        this.addField("alias");
        this.addField("email");
        this.addField("name");
    }
}
