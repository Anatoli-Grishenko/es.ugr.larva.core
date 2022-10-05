/**
 * @file capabilities.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package glossary;

/**
 * List of all the capabilities an agent might have.<p>
 */

public enum capability{
    MOVE, RIGHT, LEFT, UP, DOWN, // Movement
    CAPTURE,  BOARD, DEBARK, TRANSFER, // Payload
    RECHARGE, // Energy, Oneself
    REFILL, // Energy, to others
    QUERY, REPORT, INFORM, // Dialogue
    MOVEIN,MOVETO,MOVEINTO, MOVEBY,// Movement
    RESCUE}


