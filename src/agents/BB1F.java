/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.lang.acl.ACLMessage;
import tools.emojis;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class BB1F extends DroidShip {

    @Override
    public void setup() {
        super.setup();
        myType = "BB1F";
        this.LARVADFAddMyServices(new String[]{"TYPE " + myType});
        getMyCPUProfiler().setTsvFileName("./"+myType+".tsv");
        //logger.onEcho(); showPerceptions=true;
//        logger.offEcho();

    }

    @Override
    protected Status processAsynchronousMessages() {
        super.processAsynchronousMessages();
        for (ACLMessage m : this.getInboundOpen()) {
            toWhom = m.getSender().getLocalName();
            if (isRecruitedMission()) {
                if (m.getPerformative() == ACLMessage.CANCEL) {
                    Info("Received CANCEL");
                    this.offRecruitedMission();
                    this.forget(m);
                    return Status.CHOOSEMISSION;
                } else {
                    this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Sorry, I am busy", null));
                    return myStatus;
                }
            } else {
                if (allowREQUEST && m.getPerformative() == ACLMessage.REQUEST) {
//                    if (Math.random() <= 0.5) {
//                        this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Sorry, but I decline your request", null));
//                        forget(m);
//                        return myStatus;
//                    } else 
                    if (m.getContent().toUpperCase().equals("REFILL")) {
                        this.forget(m);
                        return this.onDemandRefill(m);
                    } else {
                        InfoMessage("Unknown request"
                                + "\n" + emojis.ROBOT + " From: " + m.getSender().getLocalName()
                                + "\n" + emojis.FOLDER + " Content: " + m.getContent());
                        this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Unknown request " + m.getContent(), null));
                        return myStatus;
                    }
                }
            }
            InfoMessage("Unknown request"
                    + "\n" + emojis.ROBOT + " From: " + m.getSender().getLocalName()
                    + "\n" + emojis.FOLDER + " Content: " + m.getContent());
            this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Unknown request " + m.getContent(), null));
            return myStatus;
        }
        return myStatus;
    }

}
