/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import geometry.Point3D;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import static messaging.ACLMessageTools.ACLMSTEALTH;
import static messaging.ACLMessageTools.fancyWriteACLM;
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
        this.DFAddMyServices(new String[]{"TYPE " + myType});
        this.allowParking = true;
        logger.offEcho();
    }

    @Override
    protected Status processAsynchronousMessages() {
//        Info(this.DM.toString());
        String toWhom;
        super.processAsynchronousMessages();
        for (ACLMessage m : this.getInboundOpen()) {
            String tokens[] = m.getContent().split(" ");
            toWhom = m.getSender().getLocalName();
            if (isOnMission()) {
                if (m.getPerformative() == ACLMessage.CANCEL) {
                    Info("Received CANCEL");
                    this.offMission();
                    this.forget(m);
                    return Status.CHOOSEMISSION;
                } else {
                    this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Sorry, I am busy", null));
                }
            } else {
                if (!inNegotiation && allowREQUEST && m.getPerformative() == ACLMessage.REQUEST) {
                    if (m.getContent().toUpperCase().equals("REFILL")) {
                        this.forget(m);
                        return this.onDemandRefill(m);
//                        //logger.onEcho();
//                        InfoMessage("Received REFILL from " + toWhom + " asking Transponder");
//                        sTransponder = this.askTransponder(toWhom);
//                        if (sTransponder.length() == 0) {
////                            InfoMessage("Bad Transponder");
//                            this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Sorry, your position is not available in Transponder", null));
//                            return myStatus;
//                        }
////                        this.Dialogue(this.respondTo(m, ACLMessage.AGREE, "Going towards you ", null));
//                        InfoMessage("Transponder " + sTransponder);
//                        try {
//                            pTarget = new Point3D(this.getTransponderField(sTransponder, "GPS"));
//                            this.onMission(m, "REFILL " + toWhom,
//                                    new String[]{"MOVETO " + pTarget.getXInt() + " " + pTarget.getYInt(),
//                                        "REFILL " + toWhom});
//                            this.Dialogue(this.respondTo(m, ACLMessage.AGREE, "On the way", null));
//                            return Status.SOLVEMISSION;
//                        } catch (Exception ex) {
//                            InfoMessage("Bad transponder");
//                            this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Sorry, your position is not available in Transponder", null));
//                            return myStatus;
//                        }
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
