/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import geometry.Point3D;
import jade.lang.acl.ACLMessage;
import static messaging.ACLMessageTools.fancyWriteACLM;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class MTT extends DroidShip {

    @Override
    public void setup() {
        super.setup();
        this.DFAddMyServices(new String[]{
            "TYPE MTT",
            "REQUEST BACKUP",
            "REQUEST CAPTURE <name>",
            "REQUEST CAPTURE <n> <type>",
            "REQUEST MOVETO <x> <y>",
            "REQUEST MOVEIN <city>",
            "REQUEST MOVEBY <agent>",
            "REQUEST TRANSFER <agent>"});
        logger.offEcho();
    }

    @Override
    protected Status processAsynchronousMessages() {
//        Info(this.DM.toString());
        super.processAsynchronousMessages();
        for (ACLMessage m : this.getExtRequests()) {
            InfoMessage("MTT:: Processing request" + fancyWriteACLM(m));
            contentTokens = m.getContent().split(" ");
            toWhom = m.getSender().getLocalName();
            if (isOnMission()) {
                if (m.getPerformative() == ACLMessage.CANCEL) {
                    Info("Received CANCEL");
                    this.offMission();
                    this.forgetUtterance(m);
                    return Status.CHOOSEMISSION;
                } else {
                    this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Sorry, I am busy", null));
                }
            } else {
                if (!inNegotiation && allowREQUEST && m.getPerformative() == ACLMessage.REQUEST) {
                    if (m.getContent().equals("BACKUP")) {
                        this.forgetUtterance(m);
                        return this.onDemandBackup(m);
                        //logger.onEcho();
//                        InfoMessage("Received BACKUP from " + toWhom + " asking Transponder");
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
//                            this.onMission(m, "BACKUP" + toWhom,
//                                    new String[]{"MOVETO " + pTarget.getXInt() + " " + pTarget.getYInt(),
//                                        "BACKUP " + toWhom});
//                            this.Dialogue(this.respondTo(m, ACLMessage.AGREE, "On the way", null));
//                            return Status.SOLVEMISSION;
//                        } catch (Exception ex) {
//                            InfoMessage("Bad transponder");
//                            this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Sorry, your position is not available in Transponder", null));
//                            return myStatus;
//                        }
                    } else {
                        InfoMessage("Unknown request");
                        this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Unknown request " + m.getContent(), null));
                        return myStatus;
                    }
                }
            }
            InfoMessage("Unknown request");
            this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Unknown request " + m.getContent(), null));
            return myStatus;
        }
        return myStatus;
    }
}
