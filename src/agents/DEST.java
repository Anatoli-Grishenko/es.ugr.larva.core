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
public class DEST extends DroidStarshipLevelA {

    @Override
    public void setup() {
        super.setup();
        this.DFAddMyServices(new String[]{"TYPE DEST"});
        this.logger.offEcho();
        onMission = false;
        this.openRemote();
//        this.closeRemote();
        onMission = false;
        allowCFP = true;
        allowREQUEST = true;
        allowParking = false;
    }

    public Status MyParking() {
        this.setTaskName("WAITING");
        this.MyReadPerceptions();

        while (true) {
            inbox = this.LARVAblockingReceive(1000);
            if (this.isOnMission()) {
                return Status.OPENMISSION;
            }
        }
    }

    @Override
    public Status MyJoinSession() {
        sessionKey = "";
        Info("Checking session " + sessionAlias);
        String opener = this.DFGetAllProvidersOf("OPEN ALIAS " + sessionAlias).get(0);
        for (String service : this.DFGetAllServicesProvidedBy(opener)) {
            if (service.startsWith(sessionAlias)) {
                sessionKey = service.split(" ")[1];
                if (this.DFGetAllProvidersOf("SESSION MANAGER " + this.sessionKey).isEmpty()) {
                    Error("Sorry service SESSION MANAGER not found");
                    return Status.CHECKOUT;
                }
                this.sessionManager = this.DFGetAllProvidersOf("SESSION MANAGER " + this.sessionKey).get(0);
                Info("Assigned to " + sessionManager + " in problem " + problemName + " during session " + sessionKey);
            }
        }
        Info("Found session " + sessionAlias + " with SessionManager " + sessionManager);
        Info("Checking list of cities with " + sessionManager);
        if (sessionKey.length() == 0) {
            Error("Sorry service SESSION MANAGER not found for alias " + sessionAlias);
            return Status.CHECKOUT;
        }
        this.DFAddMyServices(new String[]{"DROIDSHIP", sessionKey});
        if (!this.doQueryCities()) {
            return Status.EXIT;
        }
        cities = E.getCityList();
        if (cities.length == 0) {
            Error("Sorry this agent can only join worlds with cities");
            return Status.CHECKOUT;
        }
        baseCity = cities[(int) (Math.random() * cities.length)];
        currentCity = baseCity;
        Info("Joining session with base in  " + baseCity);
        outbox = session.createReply();
        outbox.setContent("Request join session " + sessionKey + " at 1 1");
        LARVAsend(outbox);
        session = LARVAblockingReceive();
        if (!session.getContent().toUpperCase().startsWith("CONFIRM")) {
            Error("Could not join session " + sessionKey + " due to " + session.getContent());
            return Status.CHECKOUT;
        }
        if (!this.MyReadPerceptions()) {
            return Status.EXIT;
        }
        gpsBase = E.getGPS();
        this.defMission("FINAL", new String[]{"MOVEIN " + baseCity, "EXIT"});
        myMission = this.activateMission("FINAL");
        return Status.CHOOSEMISSION;
    }

    @Override
    protected void processUnexpectedMessage(ACLMessage msg) {
        logger.onEcho();
        String tokens[] = msg.getContent().split(",")[0].split(" ");
        Info("Unexpected " + msg.getContent());
        if (isOnMission()) {
        } else {
            if (tokens[0].toUpperCase().equals("REPORT")) {
                if (checkReport(msg)) {
                    this.defMission("FINAL", new String[]{"MOVETO 0 0", "EXIT"});
                    onMission(msg, "FINAL");
                }
            }
        }
        Info("Refuse " + msg.getContent());
        outbox = msg.createReply();
        outbox.setContent("Refuse");
        this.LARVAsend(outbox);
    }

    protected boolean checkReport(ACLMessage msg) {
        return true;
    }
}
