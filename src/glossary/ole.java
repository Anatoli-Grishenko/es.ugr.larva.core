/**
 * @file ole.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package glossary;

/**
 * Types of {@link data.Ole} objects
 */

public enum ole {BADVALUE, OLEMETA, 
                                INTEGER, DOUBLE, STRING, ARRAY, BOOLEAN,
                                OLEBITMAP, OLEFILE, OLEACLM, OLEPASSPORT, OLEREPORT, OLETABLE, 
            ADMINPASSPORT, DBQUERY, LIST, NOTIFICATION,
            SENSOR,REQUEST,ANSWER, RECORD, POINT, VECTOR, ENTITY,
            OLE, QUERY, DIALOG };

//public class ole {
//    public static final String OLEBITMAP="bitmap", OLEFILE="file", OLEACLM="ACLMessage", 
//            OLEPASSPORT="cardid", OLEREPORT="report",DBQUERY="dbquery",
//            SENSOR="sensor",REQUEST="request",ANSWER="answer", RECORD="options",
//            ARRAY="array";
//}
