/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glossary;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Notification {

    public static enum Type {
        NEWMILESTONE, // Messages about new Milestones
        DUPMILESTONE, // Messages about duplicated milestones
        PROBLEMSOLVED, // Messages about problems being solved
        GOALMISSION, // Messages related to goals and missions
        PROBLEMS, // Checkin Checkout messages
        IDENTITY // Open & close problem messages
    };
}
