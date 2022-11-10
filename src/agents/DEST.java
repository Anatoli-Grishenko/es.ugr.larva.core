/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import ai.Mission;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.WriterConfig;
import geometry.Point3D;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import static messaging.ACLMessageTools.ACLMSTEALTH;
import static messaging.ACLMessageTools.fancyWriteACLM;
import tools.TimeHandler;
import tools.emojis;
import world.Thing;
import world.ThingSet;
import static zip.ZipTools.unzipString;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DEST extends DroidShip {

    @Override
    public void setup() {
        super.setup();
        myType = "DEST";
        this.DFAddMyServices(new String[]{"TYPE DEST"});
        this.allowParking = true;
//        logger.offEcho();
//        logger.onEcho();
    }

    @Override
    protected Status processAsynchronousMessages() {
//        Info(this.DM.toString());
        super.processAsynchronousMessages();
        for (ACLMessage m : this.getInboundOpen()) {
            if (m.getContent().startsWith("REPORT") 
                    //&& m.getPerformative() == ACLMessage.INFORM_REF
                    && myStatus == Status.PARKING) {
                InfoMessage("I have received a report." 
                        +"\n"+emojis.ROBOT+" From: "+m.getSender().getLocalName()+
                        "\n"+emojis.FOLDER+" Content: "+m.getContent());
                this.forgetUtterance(m);
                Status sAux=this.onDemandReport(m);
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
        if (!waitReport) {
            tini = new TimeHandler();
            waitReport = true;
        }
        tend = new TimeHandler();
        E.setCurrentMission("WAITING", new String[]{"WAITING REPORT " + tini.elapsedTimeSecsUntil(tend)});
        this.checkOpenUtterances();
        this.LARVAwait(1000);
//        System.out.println("\n\n"+this.DM.toString()+"\n\n");
        return myStatus;
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
        baseCity = this.mySelectCity();
        currentCity = baseCity;
        Info("Joining session with base at 1 1");
        outbox = session.createReply();
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setContent("Request join session " + sessionKey + " in " + baseCity);
        session = this.blockingDialogue(outbox).get(0);
        if (!session.getContent().toUpperCase().startsWith("CONFIRM")) {
            Error("Could not join session " + sessionKey + " due to " + session.getContent());
            return Status.CHECKOUT;
        }
        this.doQueryPeople("people");
        this.waitReport = false;
        nextCity = this.mySelectCityCourse();
        if (nextCity.length() == 0) {
            Info("Error reading cities list");
            return Status.CHECKOUT;
        }
        this.onMission(null, "FINAL", new String[]{"MOVEIN " + nextCity + ""});
        return Status.SOLVEMISSION;
    }

    @Override
    public String mySelectCityCourse() {
        if (!this.MyReadPerceptions()) {
            Info("Communication error");
            return "";
        }
        return E.getCurrentCity();

//        String city;
//        this.MyReadPerceptions();
//        do {
//            city = cities[(int) (Math.random() * cities.length)];
//        } while (city.equals(E.getCurrentCity()) && !this.doFindCourseIn(city));
//        return city;
    }

//    @Override
//    protected void processAsynchronousMessages(ACLMessage msg) {
//        String sep = ";";
//        String tokens[] = msg.getContent().split(LARVAFirstAgent.sepTransponder)[0].split(" ");
//        Info("Unexpected " + msg.getContent());
//        if (msg.getContent().toUpperCase().equals("TRANSPOND")) {
//            outbox = msg.createReply();
//            outbox.setContent(Transponder());
//            LARVAsend(outbox);
//            System.out.println(getLocalName() + " TRANSPONDER: " + Transponder());
//            return;
//        }
//        if (isOnMission()) {
//        } else {
//            if (tokens[0].toUpperCase().equals("REPORT")) {
//                if (checkReport(msg)) {
//                    Message("Received report " + inbox.getContent());
//                    onMission(msg, "THEEND", new String[]{"MOVETO 0 0", "EXIT"});
//                }
//            }
//        }
//        Info("Refuse " + msg.getContent());
//        outbox = msg.createReply();
//        outbox.setContent("Refuse");
//        this.LARVAsend(outbox);
//    }
}
