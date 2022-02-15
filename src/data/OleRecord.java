/**
 * @file OleOptions.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

/**
 * It is just a simple subclass of Ole with the generic access methods. Please refer to 
 * {@link Ole} to get to know the different options that may be represented.
 * 
 */
public class OleRecord extends Ole {

    public OleRecord() {
        super();
        setType(oletype.OLERECORD.name());
    }
    
    public OleRecord(Ole o) {
        super(o);
        setType(oletype.OLERECORD.name());
    }

}
