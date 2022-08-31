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
import jade.lang.acl.ACLMessage;
import world.Thing;
import world.ThingSet;
import static zip.ZipTools.unzipString;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DEST extends DroidStarshipLevelA {

    ThingSet population;

    @Override
    public void setup() {
        super.setup();
        this.DFAddMyServices(new String[]{"TYPE DEST"});
    }

    @Override
    protected boolean isUnexpected(ACLMessage msg) {
        if (msg.getContent().startsWith("TRANSPOND")) {
            return true;
        }
        if (myStatus == Status.PARKING) {
            return false;
        }
        return !msg.getSender().getLocalName().equals(sessionManager);
    }

    @Override
    public Status MyParking() {
        this.replyTransponder(session);

        while (true) {
            inbox = LARVAblockingReceive(1000);
            if (inbox != null) {
                outbox = inbox.createReply();
                Message("Received " + inbox.getContent());
//                System.out.println(inbox.getContent());
                if (checkCensus(inbox)) {
                    outbox.setContent("Confirm");
                    this.LARVAsend(outbox);
                    nextCity = this.mySelectCityCourse();
                    //cities[(int) (Math.random() * cities.length)];
                    this.onMission(null, "EXIT", new String[]{"MOVEIN " + nextCity});
                }
            }
            if (this.isOnMission()) {
                return Status.SOLVEMISSION;
            }
        }
    }

    public boolean checkCensus(ACLMessage msg) {
        String census[] = msg.getContent().split(Mission.sepMissions);
        if (census.length < 2) {
            return false;
        }
        for (int i = 1; i < census.length; i++) {
            String cityCensus[] = census[i].split(" "),
                    scity = cityCensus[0];
            if (cityCensus[i].length() < 1) {
                continue;
            }
            for (int j = 1; j < cityCensus.length; j++) {
                String stype;
                int snumber, slives;
                try {
                    stype = cityCensus[1];
                    snumber = Integer.parseInt(cityCensus[2]);
                    slives = 0;
                    for (Thing t : population.getAllThings()) {
                        if (t.getType().toUpperCase().equals(stype) && t.getBelongsTo().equals(scity)) {
                            slives++;
                        }
                    }
                    if (slives != snumber) {
                        Message("Error counting people " + stype + " at " + scity
                                + "\nThere should be " + slives + " " + stype + "\n"
                                + "but " + snumber + "have been reported");
                    }
                } catch (Exception ex) {
                    return false;
                }
            }

        }
        return true;
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
//                cities[(int) (Math.random() * cities.length)];
        currentCity = baseCity;
        Info("Joining session with base at 1 1");
        outbox = session.createReply();
        outbox.setContent("Request join session " + sessionKey + " in " + baseCity);
        LARVAsend(outbox);
//        currentCity = baseCity;
//        Info("Joining session with base in  " + baseCity);
//        outbox = session.createReply();
//        outbox.setContent("Request join session " + sessionKey + " in " + baseCity);
//        LARVAsend(outbox);
        session = LARVAblockingReceive();
        if (!session.getContent().toUpperCase().startsWith("CONFIRM")) {
            Error("Could not join session " + sessionKey + " due to " + session.getContent());
            return Status.CHECKOUT;
        }
//        if (!this.MyReadPerceptions()) {
//            return Status.EXIT;
//        }
        this.doQueryPeople("people");
        nextCity = this.mySelectCityCourse();
        this.onMission(null, "FINAL", new String[]{"MOVEIN " + nextCity});
//        this.setCurrentMission("FINAL", new String[]{"MOVEIN " + baseCity, "EXIT"});
//        myMission = this.activateCurrentMission("FINAL");
//        return Status.CHOOSEMISSION;
        this.replyTransponder(session);
        return Status.SOLVEMISSION;
    }

    @Override
    protected void processUnexpectedMessage(ACLMessage msg) {
        String sep = ";";
        String tokens[] = msg.getContent().split(",")[0].split(" ");
        Info("Unexpected " + msg.getContent());
        if (msg.getContent().toUpperCase().equals("TRANSPOND")) {
            outbox = msg.createReply();
            outbox.setContent(Transponder());
            LARVAsend(outbox);
            System.out.println(getLocalName()+" TRANSPONDER: "+Transponder());
            return;
        }
        if (isOnMission()) {
        } else {
            if (tokens[0].toUpperCase().equals("REPORT")) {
                if (checkReport(msg)) {
                    Message("Received report " + inbox.getContent());
                    onMission(msg, "THEEND", new String[]{"MOVETO 0 0", "EXIT"});
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

    protected Status doQueryPeople(String type) {
        Info("Querying people " + type);
        outbox = session.createReply();
        outbox.setContent("Query " + type.toUpperCase() + " session " + sessionKey);
        this.LARVAsend(outbox);
        session = LARVAblockingReceive();
        population = new ThingSet();
        String unzipedcontent = unzipString(session.getContent());

        JsonObject jspeople = Json.parse(unzipedcontent).asObject().get("thingset").asObject();
//        System.out.println("CENSUS:"+jspeople.toString(WriterConfig.PRETTY_PRINT));
        population.fromJson(jspeople.get("people").asArray());
        return myStatus;
    }

}
