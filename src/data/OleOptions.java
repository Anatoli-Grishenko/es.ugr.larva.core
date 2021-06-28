/**
 * @file OleOptions.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

import glossary.ole;

/**
 * It is just a simple subclass of Ole with the generic access methods. Please refer to 
 * {@link Ole} to get to know the different options that may be represented.
 * 
 */
public class OleOptions extends Ole {

    public OleOptions() {
        super();
        setType(ole.OPTIONS.name());
    }
    
    public OleOptions(Ole o) {
        super(o);
        setType(ole.OPTIONS.name());
    }

}
