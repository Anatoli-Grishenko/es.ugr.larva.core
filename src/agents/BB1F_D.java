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

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class BB1F_D extends DroidShip {

    @Override
    public void setup() {
        super.setup();
        this.DFAddMyServices(new String[]{"TYPE BB1F"});
        this.allowParking = true;
        logger.offEcho();
    }

    @Override
    protected Status processAsynchronousMessages() {
//        Info(this.DM.toString());
        String toWhom;
        logger.onEcho();
        super.processAsynchronousMessages();
        for (ACLMessage m : this.getExtRequests()) {
            InfoMessage("Processing request" + fancyWriteACLM(m));
            String tokens[] = m.getContent().split(" ");
            toWhom = m.getSender().getLocalName();
            if (isOnMission()) {
                if (m.getPerformative() == ACLMessage.CANCEL) {
                    Info("Received CANCEL");
                    this.offMission();
                    logger.offEcho();
                    this.forgetUtterance(m);
                    return Status.CHOOSEMISSION;
                } else {
                    this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Sorry, I am busy", null));
                }
            } else {
                if (!inNegotiation && allowCFP
                        && m.getPerformative() == ACLMessage.CFP) { // inNegotiation BYDISTANCE MOVEIN A,MOVEIN C,CAPTURE 5 JEDI,MOVEIN D, TRANSFERTO 5 JEDI TS_FULL
                    Info("Received CFP " + m.getContent());
                    if (tokens[1].equals("BYDISTANCE")) {
                        String city = tokens[3];
                        Point3D citypos = E.getCityPosition(city), mypos = E.getGPS();
                        outbox = m.createReply();
                        outbox.setContent("Propose " + citypos.planeDistanceTo(mypos));
                        Info("City: " + city + "->" + citypos.planeDistanceTo(mypos));
                        this.LARVAsend(outbox);
                        inNegotiation = true;
                        return myStatus;
                    } else if (tokens[1].toUpperCase().equals("BYCARGO")) {
                        outbox = m.createReply();
                        outbox.setContent("Propose " + E.getMaxcargo());
                        this.LARVAsend(outbox);
                        inNegotiation = true;
                        return myStatus;
                    }
                } else if (inNegotiation && allowCFP && tokens[0].toUpperCase().toUpperCase().equals("ACCEPT")) {
                    Info("Received ACCEPT " + m.getContent());
                    if (tokens[1].toUpperCase().equals("MOVEIN")) {
                        outbox = m.createReply();
                        outbox.setContent("Agree");
                        this.LARVAsend(outbox);
                        Message("Contrated by " + m.getContent());
                        inNegotiation = false;
                        onMission(m, "COMMITMENT", new String[]{tokens[2] + " " + tokens[3]});
                        Info("new task " + E.getCurrentGoal());
                        reaction = DroidShip.Status.SOLVEMISSION;
                        return myStatus;
                    }
                } else if (inNegotiation && allowCFP && tokens[0].toUpperCase().equals("REJECT")) {
                    Info("Received REJECT");
                    inNegotiation = false;
                    return myStatus;
                } else if (!inNegotiation && allowREQUEST && m.getPerformative() == ACLMessage.REQUEST) {
                    if (m.getContent().equals("REFILL")) {
                        String sTransponder;
                        InfoMessage("Received REFILL from " + toWhom + " asking Transponder");
                        sTransponder = this.askTransponder(toWhom);
                        if (sTransponder.length() == 0) {
                            InfoMessage("Bad Transponder");
                            this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Sorry, your position is not available in Transponder", null));
                            logger.offEcho();
                            return myStatus;
                        }
                        this.Dialogue(this.respondTo(m, ACLMessage.AGREE, "Going towards you ", null));
                        InfoMessage("Transponder " + sTransponder);
                        Point3D targetRefill;
                        try {
                            targetRefill = new Point3D(this.getTransponderField(sTransponder, "GPS"));
                            this.onMission(m, "REFILL " + toWhom,
                                    new String[]{"MOVETO " + targetRefill.getXInt() + " " + targetRefill.getYInt(),
                                        "REFILL " + toWhom});
                            logger.offEcho();
                            return Status.SOLVEMISSION;
                        } catch (Exception ex) {
                            InfoMessage("Bad transponder");
                            this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Sorry, your position is not available in Transponder", null));
                            logger.offEcho();
                            return myStatus;
                        }
                    } else {
                        InfoMessage("Unknown request");
                        this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Unknown request " + m.getContent(), null));
                        logger.offEcho();
                        return myStatus;
                    }
                }
            }
            InfoMessage("Unknown request");
            this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Unknown request " + m.getContent(), null));
            logger.offEcho();
            return myStatus;
        }
        logger.offEcho();
        return myStatus;
    }

}
