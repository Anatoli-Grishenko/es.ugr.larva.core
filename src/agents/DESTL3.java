/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.lang.acl.ACLMessage;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DESTL3 extends DEST {

    @Override
    public Status MyJoinSession() {
        super.MyJoinSession();
        this.onLocalMission("JUST PARK", new String[]{"PARKING"});
        return Status.CHOOSEMISSION;
    }

    @Override
    public String autoSelectCity() {
        return "Hartley";
    }

    public String autoSelectNextCity() {
        return "Hartley";
    }

}
