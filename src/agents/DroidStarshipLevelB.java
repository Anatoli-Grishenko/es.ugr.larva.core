/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import geometry.Point3D;
import jade.lang.acl.ACLMessage;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DroidStarshipLevelB extends DroidStarshipLevelA {

    protected boolean CFP;

    @Override
    public void setup() {
        super.setup();
        CFP = false;
    }

    @Override
    public Status MyParking() {
        int time = PARKINGTIME + (int) (Math.random() * PARKINGTIME);
        boolean exit = false;
        TimeHandler tini, tend;
        long remaining;
        tini = new TimeHandler();
        tend = new TimeHandler();
        this.setTask("PARKING ");
        this.MyReadPerceptions();
        while (!exit) {
            inbox = fromSessionManager();
            if (this.isOnMission()) {
                return filterStatus(Status.OPENMISSION.name());
            }
            tend = new TimeHandler();
            exit = tini.elapsedTimeSecsUntil(tend) >= time;
        }
        // Message("Released");
        return filterStatus(Status.CHOOSEMISSION.name());
    }

    @Override
    protected ACLMessage fromSessionManager() {
        ACLMessage msg;
        while (true) {
            if (myStatus == Status.PARKING) {
                msg = LARVAblockingReceive(500);
            } else {
                msg = LARVAblockingReceive();
            }
            if (msg != null && msg.getSender().getLocalName().equals(sessionManager)) {
                counterSessionManager--;
                return msg;
            } else {
                if (myStatus == Status.PARKING) {
                    if (msg != null) {
                        processUnexpectedMessage(msg);
                    }
                    return null;
                } else {
                    if (msg != null) {
                        processUnexpectedMessage(msg);
                    }
                }

            }
        }
    }

    protected void processUnexpectedMessage(ACLMessage msg) {
        contentTokens = msg.getContent().split(" ");
        if (isOnMission()) {
            if (contentTokens[0].toUpperCase().equals("CANCEL")) {
                this.offMission();
                return;
            }
        } else {
            if (!CFP && contentTokens[0].toUpperCase().equals("CFP")) {
                if (contentTokens[1].equals("BYDISTANCE")) {
                    String city = contentTokens[2];
                    Point3D citypos = E.getCityPosition(city), mypos = E.getGPS();
                    outbox = msg.createReply();
                    outbox.setContent("Propose " + citypos.planeDistanceTo(mypos));
                    this.LARVAsend(outbox);
                    CFP = true;
                    return;
                } else if (contentTokens[1].equals("BYCARGO")) {
                    outbox = msg.createReply();
                    outbox.setContent("Propose " + E.getMaxcargo());
                    this.LARVAsend(outbox);
                    CFP = true;
                    return;
                }
            } else if (CFP && contentTokens[0].toUpperCase().equals("ACCEPT")) {
                if (contentTokens[1].toUpperCase().equals("MOVEIN")) {
                    outbox = msg.createReply();
                    outbox.setContent("Agree");
                    this.LARVAsend(outbox);
                    CFP = false;
                    onMission(msg, "MOVEIN " + contentTokens[2]);
                    myStatus = Status.OPENMISSION;
                    return;
                }
            } else if (CFP && contentTokens[0].toUpperCase().equals("REJECT")) {
                CFP = false;
                return;
            }
            outbox = msg.createReply();
            outbox.setContent("Refuse");
            this.LARVAsend(outbox);
        }
    }

    @Override
    public Status filterStatus(String s) {
        if (isChangeMission()) {
            return myStatus;
        } else {
            try {
                return Status.valueOf(s);
            } catch (Exception ex) {
                return Status.EXIT;
            }
        }
    }
}
