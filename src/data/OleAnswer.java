///**
// * @file OleAnswer.java
// * @author Anatoli.Grishenko@gmail.com
// *
// */
//package data;
//
//import glossary.ole;
//import java.util.ArrayList;
//
///**
// * Class devoted to wrap the answers of the servers to any request coming from students
// */
//public class OleAnswer extends Ole {
//
//    /**
//     * Basic constructor
//     */
//    public OleAnswer() {
//        super();
//        Init();
//    }
//
//    /**
//     * Copy constructor
//     * @param o The object to be cloned
//     */
//    public OleAnswer(Ole o) {
//        super(o);
//    }
//
//    /**
//     * Rebuilder of the object from a serialization of tanother object.
//     * @param s A String that contains the serialzation of another object
//     */
//    public OleAnswer(String s) {
//        super(s);
//    }
//
//    /**
//     * Sets the default values of OleFile objects
//     */
//    private void Init() {
//        checkField("result");
//        checkField("details");
//        checkField("payload");
//        setType(ole.ANSWER.name());
//
//    }
//    
//    /**
//     * Wrapper method to query the value of a field, more specific than the generinc methods 
//     * of class Ole.
//     * @return The result of the action requested to the server
//     */
//    public String getResult() {
//        return getField("result");
//    }
//
//    /**
//     * The server ties to gve an explanation to the executuion of the action.
//     * @return 
//     */
//    public String getDetails() {
//        return getField("details");
//    }
//
//    /**
//     * Gets the payload of a message coming from the server.It is an 
//     * ArrayList of any of the types suupeorted by Ole
//     * @return 
//     */
//    public ArrayList getPayLoad() {
//        return getArray("payload");
//    }
//}
