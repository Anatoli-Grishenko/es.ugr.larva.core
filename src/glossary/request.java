/**
 * @file requests.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package glossary;

/**
 * Main commands that an agent can request in every session with the server. The syntax will be defined in a separate document
 * <ul>
 * <li> <b>login</b> Open a session. All the members of the team can join it later.
 * <li> <b>join</b> Connects to a previously open session.
 * <li> <b>logout</b> Close a session
 * <li> <b>execute</b> Ask for the execution of any of the agent's capabilities
 * <li> <b>read</b> Query the state of a sensor connected to the agent
 * </ul>
 * 
 */
public enum request{LOGIN,LOGOUT,EXECUTE,READ};

//public class request {
//    public static final String LOGIN="LOGIN", LOGOUT="LOGOUT", EXECUTE="EXECUTE",
//            READ="READ";
//    
//}
