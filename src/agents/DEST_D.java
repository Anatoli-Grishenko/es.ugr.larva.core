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
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import static messaging.ACLMessageTools.ACLMSTEALTH;
import tools.TimeHandler;
import world.Thing;
import world.ThingSet;
import static zip.ZipTools.unzipString;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DEST_D extends DroidStarshipLevelD {

    ThingSet population;

    @Override
    public void setup() {
        super.setup();
        this.DFAddMyServices(new String[]{"TYPE DEST"});
        this.allowParking = true;
    }

    @Override
    public Status MyParking() {
        if (!allowParking) {
            return Status.CHOOSEMISSION;
        }
        if (tini == null) {
//            realParkingTime = 30000;
            tini = new TimeHandler();
        }
        tend = new TimeHandler();
        E.setCurrentMission("WAITING", new String[]{"WAITING REPORT"});
        tend = new TimeHandler();
        ACLMessage m = this.blockingDialogue().get(0);
//        this.closeUtterance(m);
        if (m.getContent().startsWith("REPORT")) {
            outbox = m.createReply();
            String census = checkCensus(m);
            if (census.length() == 0) {
                ACLMessage auxOutbox, auxInbox;
                auxOutbox=session.createReply();
//                auxOutbox = new ACLMessage(ACLMessage.REQUEST);
//                auxOutbox.setSender(getAID());
//                auxOutbox.addReceiver(new AID(this.mySessionmanager, AID.ISLOCALNAME));
//                auxOutbox.setConversationId(this.mySessionID);
                auxOutbox.setPerformative(ACLMessage.REQUEST);
                auxOutbox.setContent("Confirm");
                auxOutbox.addUserDefinedParameter(ACLMSTEALTH, "TRUE");
                auxOutbox.setReplyWith("CONFIRM MISSION " + m.getSender().getLocalName());
                session = this.blockingDialogue(auxOutbox).get(0);
                outbox.setPerformative(ACLMessage.CONFIRM);
                outbox.setContent("Confirm");
                this.Dialogue(outbox);
                this.closeUtterance(outbox);
                return Status.CHOOSEMISSION;
            } else {
                outbox.setPerformative(ACLMessage.DISCONFIRM);
                outbox.setContent("Disconfirm " + census);
                this.Dialogue(outbox);
                this.closeUtterance(outbox);
            }
        }
//        if (m.getPerformative() == ACLMessage.INFORM_REF) {
//            if (m.getContent().startsWith("REPORT")) {
//                outbox = session.createReply();
//                outbox.setPerformative(ACLMessage.INFORM_REF);
//                m.setReplyWith(m.getSender().getLocalName());
//                outbox.setContent(m.getContent());
//                inbox = this.blockingDialogue(outbox).get(0);
//                outbox = m.createReply();
//                if (inbox.getPerformative() == ACLMessage.CONFIRM) {
//                    outbox.setPerformative(ACLMessage.CONFIRM);
//                    outbox.setContent("Confirm");
//                    tini = null;
//                    return Status.CHOOSEMISSION;
//                } else {
//                    outbox.setPerformative(ACLMessage.DISCONFIRM);
//                    outbox.setContent("Disconfirm");
//                }
//                this.Dialogue(m);
//            }
//        }
        return myStatus;
    }

    public String checkCensus(ACLMessage msg) {
        String census[] = msg.getContent().split(Mission.sepMissions);
        if (census.length < 2) {
            return "Error processing report. Please follow the instructions given by teachers";
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
                        if (t.getType().toUpperCase().equals(stype.toUpperCase()) 
                                && t.getBelongsTo().toUpperCase().equals(scity.toUpperCase())) {
                            slives++;
                        }
                    }
                    if (slives != snumber) {
                        return "Error counting people " + stype + " at " + scity
                                + "\nThere should be " + slives + " " + stype + "\n"
                                + "but " + snumber + "have been reported";
                    }
                } catch (Exception ex) {
                    return "Error processing report. Please follow the instructions given by teachers";
                }
            }

        }
        return "";
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
        nextCity = this.mySelectCityCourse();
        this.onMission(null, "FINAL", new String[]{"MOVEIN " + nextCity});
        return Status.SOLVEMISSION;
    }

    @Override
    protected void processUnexpectedMessage(ACLMessage msg) {
        String sep = ";";
        String tokens[] = msg.getContent().split(LARVAFirstAgent.sepTransponder)[0].split(" ");
        Info("Unexpected " + msg.getContent());
        if (msg.getContent().toUpperCase().equals("TRANSPOND")) {
            outbox = msg.createReply();
            outbox.setContent(Transponder());
            LARVAsend(outbox);
            System.out.println(getLocalName() + " TRANSPONDER: " + Transponder());
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
        outbox.setPerformative(ACLMessage.QUERY_REF);
        session = this.blockingDialogue(outbox).get(0);
        population = new ThingSet();
        String unzipedcontent = unzipString(session.getContent());

        JsonObject jspeople = Json.parse(unzipedcontent).asObject().get("thingset").asObject();
//        System.out.println("CENSUS:"+jspeople.toString(WriterConfig.PRETTY_PRINT));
        population.fromJson(jspeople.get("people").asArray());
        return myStatus;
    }

}
