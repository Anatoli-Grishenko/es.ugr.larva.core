/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.lang.acl.ACLMessage;
import tools.TimeHandler;
import tools.emojis;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DEST extends DroidShip {

    @Override
    public void setup() {
        super.setup();
        myType = "DEST";
        this.DFAddMyServices(new String[]{"TYPE " + myType});
        allowParking = true;
        //logger.onEcho(); showPerceptions=true;
        logger.offEcho();
    }

    @Override
    public Status MyJoinSession() {
        Status res = super.MyJoinSession();
        this.doQueryPeople("people");
        return res;
    }

    @Override
    protected Status processAsynchronousMessages() {
//        Info(this.DM.toString());
        super.processAsynchronousMessages();
        for (ACLMessage m : this.getInboundOpen()) {
            if (m.getContent().startsWith("REPORT")
                    && myStatus == Status.PARKING) {
                InfoMessage("I have received a report."
                        + "\n" + emojis.ROBOT + " From: " + m.getSender().getLocalName()
                        + "\n" + emojis.FOLDER + " Content: " + m.getContent());
                this.forget(m);
                Status sAux = this.onDemandReport(m);
                return sAux;
            } else if (m.getContent().startsWith("TRANSFER")) {
                return this.onDemandTransfer(m);
            }
        }
        return myStatus;
    }

    @Override
    public Status MyParking() {
        if (!allowParking) {
            return Status.CHOOSEMISSION;
        }
//        if (!waitReport) {
//            tini = new TimeHandler();
//            waitReport = true;
//        }
//        tend = new TimeHandler();
//        E.setCurrentMission("WAITING", new String[]{"WAITING REPORT "});
        defaultBehaviour.block();
        return myStatus;
    }

}
