///**
// * @file OleRequest.java
// * @author Anatoli.Grishenko@gmail.com
// *
// */
//package data;
//
//import glossary.ole;
//
///**
// * Class to represent complex requests sent to the server. Any request is composed of a command,
// * as a sequence of chars. Each command is described in {@link glossary} and the format
// * of this string depends on the DBA project each year, but may be like "load map1", "execute _action_"
// * Optionally, a request may also carry: <p>
// * <ul>
// * <li> Key. An alphanumerical key obtained from the server in previous requests
// * <li> Attach. A vector of objects that may act as parameters or complements to the main command
// * <li> CardID. SOme commands require the identification of the interlocutor, therefore, the CardID
// * must be provided.
// * </ul>
// */
//public class OleRequest extends Ole {
//
//    /**
//     * Basic constructor
//     */
//    public OleRequest() {
//        super();
//        Init();
//    }
//
//    public OleRequest(Ole o) {
//        super(o);
//    }
//
//    private void Init() {
//        checkField("command");
//        checkField("key");
//        checkField("attach");
//        checkField("cardid");        
//        setType(ole.REQUEST.name());
//    }
//    
//}
