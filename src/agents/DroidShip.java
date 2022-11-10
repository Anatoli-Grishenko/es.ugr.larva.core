/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import Environment.Environment;
import ai.Choice;
import ai.DecisionSet;
import ai.Mission;
import ai.MissionSet;
import ai.Plan;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import static crypto.Keygen.getHexaKey;
import data.OlePassport;
import static disk.Logger.trimFullString;
import geometry.Point3D;
import geometry.SimpleVector3D;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import static messaging.ACLMessageTools.ACLMROLE;
import static messaging.ACLMessageTools.ACLMSTEALTH;
import static messaging.ACLMessageTools.fancyWriteACLM;
import tools.TimeHandler;
import static tools.TimeHandler.nextSecs;
import tools.emojis;
import world.Perceptor;
import world.Thing;
import world.ThingSet;
import static zip.ZipTools.unzipString;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DroidShip extends LARVADialogicalAgent {

    protected static boolean debugDroid = false;
    protected HashMap<String, ArrayList<String>> myCitizens;
    protected HashMap<String, String> citizenOf;

    public static void Debug() {
        debugDroid = true;
    }

    protected enum Status {
        START, CHECKIN, CHECKOUT, JOINSESSION, CHOOSEMISSION,
        SOLVEMISSION, CLOSEMISSION, EXIT, WAIT, PARKING, REFILL, BACKUP, TRANSFERIN,
    }
    final int PARKINGTIME = 5, WAITIME = 1;

    protected Status myStatus;
    protected String service = "PMANAGER", problem = "",
            problemManager = "", content, sessionKey, sessionManager, gobackHome = "GOBACKHOME";
    protected String problems[], plan[], actions[];
    protected ACLMessage open, session;
    protected String[] contentTokens, cities, goalTokens;
    protected String action = "", preplan = "", baseCity, currentCity, nextCity, myMission, Message;
    protected Point3D gpsBase, pTarget, pSource;
    protected int indexplan = 0, myEnergy = 0, counterSessionManager = 0;
    protected boolean showPerceptions, onMission, changeMission;

    protected String whichWall, nextWhichwall;
    protected double distance, nextdistance;
    protected ACLMessage msgMission;

    protected boolean allowREQUEST, allowCFP, allowParking;
    protected boolean inNegotiation;
    protected String rw = "", toWhom, fromWho, who, sTransponder, myType, myCity;
    protected Status reaction;

    protected Plan behaviour = null;
    protected Environment Ei, Ef;
    protected Choice a;
    protected TimeHandler tini, tend;
    protected int realParkingTime;
    protected boolean needCourse = true;
    protected ThingSet population;
    protected boolean waitReport = false;

    @Override
    protected String Transponder() {
        String goal = "";
        String sep = this.sepTransponder, answer = "TRANSPONDER" + sep;

        answer += "NAME " + getLocalName() + sep + "TYPE " + E.getType();
        if (E.getGround() > 0) {
            answer += sep + "STATUS MOVING";
        } else {
            answer += sep + "STATUS GROUNDED " + getEnvironment().getCurrentCity();
        }
        answer += sep + "GPS " + E.getGPS().toString()
                + sep + "COURSE " + SimpleVector3D.Dir[E.getGPSVector().getsOrient()]
                + sep + "PAYLOAD " + E.getPayload();
//        if (myStatus == Status.PARKING) {
//            if (E.getType().equals("DEST")) {
////                goalTokens = " WAITING REPORT";
////            } else {
////                if (this.allowParking) {
////                    goalTokens = " PARKING " + (realParkingTime - tini.elapsedTimeSecsUntil(tend));
////                }
////            }
//        } else {
//            goalTokens = "GOAL " + E.getCurrentGoal();
//        }
        goal = "GOAL " + E.getCurrentGoal();
        answer += sep + goal + sep + "MISSION " + E.getCurrentMission();
        return answer;
    }

    @Override
    public void setup() {
        this.allowExceptionShield = false;
        this.disableDeepLARVAMonitoring();
        super.setup();
        logger.setLoggerFileName(null);
        citizenOf = new HashMap();
        myCitizens = new HashMap();
//        logger.onEcho();
        logger.offEcho();
        this.setSecuredMessages(true);
        this.deactivateSequenceDiagrams();
        this.setFrameDelay(0);
        showPerceptions = false;
//        this.openRemote();
        this.closeRemote();
        this.setupEnvironment();
        A = new DecisionSet();
        A.
                addChoice(new Choice("RECHARGE")).
                addChoice(new Choice("DOWN")).
                addChoice(new Choice("UP")).
                addChoice(new Choice("MOVE")).
                addChoice(new Choice("LEFT")).
                addChoice(new Choice("RIGHT"));
        Info("Searching who is ProblemManager");
        if (this.DFGetAllProvidersOf(service).isEmpty()) {
            Error("Service " + service + " is down");
            myStatus = Status.EXIT;
            return;
        }
        problemManager = this.DFGetAllProvidersOf("PManager").get(0);
        Info("Found problem manager " + problemManager);
        myStatus = Status.START;
        myText = null;
        onMission = false;
        allowCFP = true;
        allowREQUEST = true;
        allowParking = false;
        inNegotiation = false;
        usePerformatives = true;
        this.frameDelay = 10;
        this.DFAddMyServices(new String[]{"DROIDSHIP"});
    }

    @Override
    public void Execute() {
        myStatus = this.processAsynchronousMessages();
        switch (myStatus) {
            case START:
                myStatus = Status.CHECKIN;
                break;
            case CHECKIN:
                myStatus = MyCheckin();
                break;
            case JOINSESSION:
                myStatus = MyJoinSession();
                break;
            case CHOOSEMISSION:
                myStatus = MyChooseMission();
                break;
            case SOLVEMISSION:
                myStatus = MySolveProblem();
                break;
            case WAIT:
                myStatus = MyWait();
                break;
            case PARKING:
                myStatus = MyParking();
                break;
            case CLOSEMISSION:
                myStatus = MyCloseMission();
                break;
            case CHECKOUT:
                myStatus = MyCheckout();
                break;
            case EXIT:
            default:
                this.LARVAexit = true;
                break;
        }
    }

    @Override
    public void takeDown() {
        Info("Taking down...");
        this.closeRemote();
        super.takeDown();
    }

    public Status MyWait() {
        if (tini == null) {
            realParkingTime = PARKINGTIME + (int) (Math.random() * PARKINGTIME);
            tini = new TimeHandler();
            E.setCurrentMission("BACKUP", new String[]{"BACKUP"});
        }
        this.defaultBehaviour.block(1000);
        this.checkOpenUtterances();
        return myStatus;
    }

    public Status MyCheckin() {
        boolean cin;
        String fakepassport = "TlBDIEFHRU5U ZXlKUFRFVk5SVlJCSWpwN0ltbGtJam9pV0UxUk56SlBSVzVPWjBaS1VqSXpjaUlzSW5SNWNHVWlPaUpQVEVWUVFWTlRVRTlTVkNJc0ltWnBaV3hrY3lJNmV5SnlZWGRRWVhOemNHOXlkQ0k2SWlJc0luVnpaWEpKUkNJNklpSXNJbU5wWkNJNklpSXNJbUZzYVdGeklqb2lJaXdpWlcxaGFXd2lPaUlpTENKdVlXMWxJam9pSW4wc0ltUmhkR1VpT2lJeU1ESXlMVEE1TFRJeUlEQTNPakF5T2pJM09qTXdPQ0lzSW1SbGMyTnlhWEIwYVc5dUlqb2lTbE5QVGlCUFltcGxZM1FnVEdsdWEyVmtJR0Z1WkNCRmJXSmxaR1ZrSWl3aWIyeGxJanAwY25WbExDSmpjbmx3ZEc4aU9pSWlmU3dpZFhObGNrbEVJam94TURBd0xDSmphV1FpT2lJaUxDSmhiR2xoY3lJNklrbGpaVzFoYmlJc0ltVnRZV2xzSWpvaWJDNWpZWE4wYVd4c2IwQmtaV056WVdrdWRXZHlMbVZ6SWl3aWJtRnRaU0k2SWs1UVF5QkJSMFZPVkNKOQ==",
                realpassport = mypassport;

        OlePassport op = new OlePassport();
        op.loadPassport(oleConfig.getTab("Identity").getString("Passport file", ""));
        sessionAlias = trimFullString(op.getName());
        IdentityManager = DFGetAllProvidersOf("IDENTITY").get(0);
        Info("Checking-in to LARVA");
        outbox = new ACLMessage(ACLMessage.SUBSCRIBE);
        AID IM = new AID(IdentityManager, AID.ISLOCALNAME);
        outbox.setSender(getAID());
        outbox.addReceiver(IM);
        outbox.setContent(fakepassport);
        checkin = this.blockingDialogue(outbox).get(0);
        if (checkin.getPerformative() == ACLMessage.CONFIRM) {
            checkedin = true;
            Info(checkin.getContent());
            checkout = checkin.createReply();
        } else if (checkin.getPerformative() == ACLMessage.REFUSE) {
            Alert("Checkin at LARVA refused.\nDetails: " + checkin.getContent());
            return Status.EXIT;
        } else {
            Alert("Could not checkin at LARVA.\nDetails: " + checkin.getContent());
            return Status.EXIT;
        }
        return Status.JOINSESSION;
    }

    public Status MyCheckout() {
        this.doLARVACheckout();
        return Status.EXIT;
    }

    public String mySelectCity() {
        String city;
        city = cities[(int) (Math.random() * cities.length)];
        return city;
    }

    public String mySelectNextCity() {
        String city, around[] = E.getCitiesAround(200);

        city = around[(int) (Math.random() * around.length)];
        return city;
    }

    public String mySelectCityCourse() {
        String city;
        if (!this.MyReadPerceptions()) {
            Info("Communication error");
            return "";
        }
        do {
            city = cities[(int) (Math.random() * cities.length)];
        } while (city.equals(E.getCurrentCity()) && !this.doFindCourseIn(city));
        return city;
    }

    public Status MyChooseMission() {
        nextCity = this.mySelectNextCity();
        this.onMyMission("AUTOMODE", new String[]{"MOVEIN " + nextCity});
//        E.setCurrentMission("AUTOMODE", new String[]{"MOVEIN " + nextCity});pp
//        this.
        this.needCourse = true;
        return Status.SOLVEMISSION;
    }

    public Status MyCloseMission() {
        return Status.CHOOSEMISSION;
    }

    public Status MyBackup() {
        return Status.CHOOSEMISSION;
    }

    public Status MyTransferIn() {
        return Status.CHOOSEMISSION;
    }

    public Status MyParking() {
        this.offMission();
        if (!allowParking) {
            return Status.CHOOSEMISSION;
        }
        if (tini == null) {
            realParkingTime = PARKINGTIME + (int) (Math.random() * PARKINGTIME);
            tini = new TimeHandler();
        }
        tend = new TimeHandler();
        E.setCurrentMission("WAITING", new String[]{"WAITING " + (realParkingTime - (tini.elapsedTimeSecsUntil(tend)))});
        this.defaultBehaviour.block(1000);
        tend = new TimeHandler();
//        this.sendStealthTransponder();
        if (tini.elapsedTimeSecsUntil(tend) >= realParkingTime) {
            tini = null;
            return Status.CHOOSEMISSION;
        }
        return myStatus;
    }

    protected boolean MyExecuteAction(String action) {
        Info("Executing action " + action);
        outbox = session.createReply();
        outbox.addUserDefinedParameter("DROIDSHIP", "true");
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setContent("Request execute " + action + " session " + sessionKey);
        outbox.setReplyWith(getHexaKey());
        this.myEnergy++;
        session = this.blockingDialogue(outbox).get(0);
        if (session.getContent().toUpperCase().startsWith("INFORM")) {
            return true;
        } else {
            Alert("Execution of action " + action + " failed due to " + session.getContent());
            return false;
        }
    }

    protected boolean MyReadPerceptions() {
        Info("Reading perceptions");
        outbox = session.createReply();
        outbox.setContent("Query sensors session " + sessionKey);
        outbox.setPerformative(ACLMessage.QUERY_REF);
        outbox.setReplyWith(getHexaKey());
        outbox.addUserDefinedParameter("DROIDSHIP", "true");
        this.myEnergy++;
        session = this.blockingDialogue(outbox).get(0);
//        session = this.fromSessionManager(); //this.LARVAblockingReceive(MessageTemplate.MatchSender(new AID(sessionManager, AID.ISLOCALNAME)));
        if (session.getContent().toUpperCase().startsWith("FAILURE")) {
            Alert("Failed to read perceptions");
            return false;
        } else {
            Info("Read perceptions ok");
            getEnvironment().setExternalPerceptions(session.getContent());
            return true;
        }

    }

    public String myMethod() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    public Status MyMoveProblem() {
        // Analizar objetivo
        if (G(E)) {
            Info("Target reached");
            E.setNextGoal();
//            if (!E.getCurrentCity().equals("MOVING")) {
//                this.doQueryPeople("People");
//            }
            return Status.SOLVEMISSION;
        }
        Choice a = Ag(E, A);
        if (a == null) {
            Alert("Found no action to execute");
            return Status.CLOSEMISSION;
        } else {// Execute
            Info("Excuting " + a);
//            System.out.println("Alternatives " + A.toString());
//            System.out.println("Excuting " + a);
            if (!this.MyExecuteAction(a.getName())) {
                Info("Communication error");
                return Status.CHECKOUT;
            }
            if (!this.MyReadPerceptions()) {
                Info("Communication error");
                return Status.CHECKOUT;
            }
            if (!Ve(E)) {
                this.Error("The agent is not alive: " + E.getStatus());
                return Status.CLOSEMISSION;
            }
            return Status.SOLVEMISSION;
        }
    }

    public Status MySolveProblem() {
        if (E.getCurrentMission().isOver()) {
            this.offMission();
            needCourse = true;
            return Status.PARKING;
        }
        goalTokens = E.getCurrentGoal().toUpperCase().split(" ");
        switch (goalTokens[0]) {
            case "MOVETO":
                return doGoalMoveTo();
//                if (needCourse) {
//                    if (E.getGPS().getX() == Integer.parseInt(goalTokens[1])
//                            && E.getGPS().getY() == Integer.parseInt(goalTokens[2])
//                            && E.getGround() == 0) {
//                        E.setNextGoal();
//                        needCourse = false;
//                        return Status.SOLVEMISSION;
//                    }
//                    if (!this.doFindCourseTo(Integer.parseInt(goalTokens[1]), Integer.parseInt(goalTokens[2]))) {
//                        this.LARVAwait(1000);
//                        E.setNextGoal();
//                        return Status.EXIT;
//                    }
//                    needCourse = false;
//                }
//                return MyMoveProblem();
            case "MOVEIN":
                return doGoalMoveIn();
//                nextCity = getEnvironment().getCurrentGoal().replace("MOVEIN ", "");
//                if (needCourse) {
//                    if (E.getCurrentCity().equals(nextCity)) {
//                        E.setNextGoal();
//                        return Status.SOLVEMISSION;
//                    }
//                    if (!this.doFindCourseIn(nextCity)) {
//                        this.LARVAwait(1000);
//                        E.setNextGoal();
//                        return Status.EXIT;
//                    }
//                    needCourse = false;
//                }
//                return MyMoveProblem();
            case "REFILL":
                return doGoalRefill();
//                goalTokens = E.getCurrentGoal().split(" ");
//                toWhom = goalTokens[1];
//                sTransponder = this.askTransponder(toWhom);
//                if (sTransponder.length() == 0) {
//                    this.Dialogue(this.respondTo(null, ACLMessage.REFUSE, "Sorry, your position is not available in Transponder", toWhom));
//                    return myStatus;
//                }
//                InfoMessage("Transponder " + sTransponder);
//                try {
//                    pTarget = new Point3D(this.getTransponderField(sTransponder, "GPS"));
//                    if (pTarget.isEqualTo(E.getGPS())) {
//                        if (!this.MyExecuteAction(E.getCurrentGoal())) {
//                            Info("Communication error");
//                            return Status.CHECKOUT;
//                        }
//                        outbox = this.respondTo(null, ACLMessage.INFORM, "DONE", goalTokens[1]);
//                        Dialogue(outbox);
//                        this.forgetUtterance(outbox);
//                        this.offMission();
//                        logger.offEcho();
//                        return Status.CHOOSEMISSION;
//                    } else {
//                        this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, your position is not valid", toWhom));
//                        return Status.CHOOSEMISSION;
//                    }
//                } catch (Exception ex) {
//                    this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, your position is not valid", toWhom));
//                    return Status.CHOOSEMISSION;
//                }
            case "BACKUP":
                return doGoalBackup();
//                goalTokens = E.getCurrentGoal().split(" ");
//                toWhom = goalTokens[1];
//                sTransponder = this.askTransponder(toWhom);
//                if (sTransponder.length() == 0) {
//                    this.Dialogue(this.respondTo(null, ACLMessage.REFUSE, "Sorry, your position is not available in Transponder", toWhom));
//                    return myStatus;
//                }
//                InfoMessage("Transponder " + sTransponder);
//                try {
//                    pTarget = new Point3D(this.getTransponderField(sTransponder, "GPS"));
//                    if (pTarget.isEqualTo(E.getGPS())) {
//                        outbox = this.respondTo(null, ACLMessage.INFORM, "DONE", goalTokens[1]);
//                        Dialogue(outbox);
//                        this.forgetUtterance(outbox);
//                        logger.offEcho();
//                        return Status.WAIT;
//                    } else {
//                        this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, your position is not valid", toWhom));
//                        logger.offEcho();
//                        return Status.CHOOSEMISSION;
//                    }
//                } catch (Exception ex) {
//                    this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, your position is not valid", toWhom));
//                    logger.offEcho();
//                    return Status.CHOOSEMISSION;
//                }
            case "TRANSFERIN":
                return MyTransferIn();
            case "EXIT":
//                this.
                return Status.EXIT;
            default:
                Alert("Sorry I do not know how to reach goal " + E.getCurrentGoal());
                return Status.CLOSEMISSION;
        }
    }

    public boolean doFindCourseIn(String destination) {
        if (E.getDestinationCity().equals(destination)) {
            return true;
        }
        if (E.getCurrentCity().equals(destination)) {
            Message("I am already there");
            return true;
        }
        Info("Searching a route in " + destination);
        Point3D p = E.getCityPosition(destination);
        return doFindCourseTo(p.getXInt(), p.getYInt());
    }

    public boolean doFindCourseTo(int x, int y) {
        Info("Searching a route to " + x + " " + y);
        outbox = session.createReply();
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setContent("Request course to " + x + " " + y + " Session " + sessionKey);
        outbox.setReplyWith(getHexaKey());
        Info("Request course to " + x + " " + y + " Session " + sessionKey);
        outbox.addUserDefinedParameter("DROIDSHIP", "true");
        session = this.blockingDialogue(outbox).get(0);
        if (!session.getContent().toUpperCase().startsWith("FAILURE")) {
            E.setExternalPerceptions(session.getContent());
            Info("Successfully found a route");
            return true;
        } else {
            Info("Failed to find a route");
            return false;
        }
    }

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
        this.doQueryMissions();
        if (cities.length == 0) {
            Error("Sorry this agent can only join worlds with cities");
            return Status.CHECKOUT;
        }
        baseCity = this.mySelectCity();
//cities[(int) (Math.random() * cities.length)];
        currentCity = baseCity;
        Info("Joining session with base in  " + baseCity);
        outbox = session.createReply();
        outbox.setContent("Request join session " + sessionKey + " in " + baseCity);
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setConversationId(sessionKey);
        outbox.setReplyWith(getHexaKey());
        session = this.blockingDialogue(outbox).get(0);
        if (!session.getContent().toUpperCase().startsWith("CONFIRM")) {
            Error("Could not join session " + sessionKey + " due to " + session.getContent());
            return Status.CHECKOUT;
        }
        if (!this.MyReadPerceptions()) {
            return Status.EXIT;
        }
        gpsBase = E.getGPS();
        Info("I am joining session " + sessionKey);
        return Status.CHOOSEMISSION;
    }

    boolean doQueryCities() {
        Info("Querying CITIES");
        outbox = new ACLMessage(ACLMessage.QUERY_REF);
        outbox.setSender(this.getAID());;
        outbox.addReceiver(new AID(sessionManager, AID.ISLOCALNAME));
        outbox.setContent("Query CITIES session " + sessionKey);
        outbox.setConversationId(sessionKey);
        outbox.setReplyWith(getHexaKey());
        session = this.blockingDialogue(outbox).get(0);
        if (session.getContent().toUpperCase().startsWith("FAILURE")) {
            return false;
        } else {
            E.setExternalPerceptions(session.getContent());
            return true;
        }
    }

    protected Status doQueryMissions() {
        Info("Querying MISSIONS");
        outbox = new ACLMessage(ACLMessage.QUERY_REF);
        outbox.setSender(this.getAID());;
        outbox.addReceiver(new AID(sessionManager, AID.ISLOCALNAME));
        outbox.setContent("Query MISSIONS session " + sessionKey);
        outbox.setConversationId(sessionKey);
        outbox.setReplyWith(getHexaKey());
        session = this.blockingDialogue(outbox).get(0);
        E.setExternalPerceptions(session.getContent());
        return myStatus;
    }

    public Status filterStatus(String s) {
        try {
            return Status.valueOf(s);
        } catch (Exception ex) {
            return Status.EXIT;
        }
    }

    public boolean isChangeMission() {
        boolean res = changeMission;
        changeMission = false;
        return res;
    }

    public boolean isOnMission() {
        return onMission;
    }

    public void onMyMission(String mission, String[] goals) {
        this.needCourse = true;
        E.setCurrentMission(mission, goals);
        Info("On mission " + mission);
    }

    public void onMission(ACLMessage msg, String mission, String[] goals) {
        onMission = true;
        changeMission = true;
        msgMission = msg;
        this.needCourse = true;
        E.setCurrentMission(mission, goals);
        Info("On mission " + mission);
    }

    public void offMission() {
        onMission = false;
    }

    protected double goTurnBack(Environment E, Choice a) {
        if (E.isTargetLeft()) {
            if (a.getName().equals("LEFT")) {
                a.setAnnotation(this.myMethod());

                return Choice.ANY_VALUE;
            }
        } else if (E.isTargetRight()) {
            if (a.getName().equals("RIGHT")) {
                a.setAnnotation(this.myMethod());

                return Choice.ANY_VALUE;
            }
        }
        return Choice.MAX_UTILITY;
    }

    public double goTakeOff(Environment E, Choice a) {
        if (a.getName().equals("UP")) {
            a.setAnnotation(this.myMethod());
            return Choice.ANY_VALUE;
        }
        return Choice.MAX_UTILITY;
    }

    public double goLanding(Environment E, Choice a) {
        if (a.getName().equals("DOWN")) {
            a.setAnnotation(this.myMethod());
            return Choice.ANY_VALUE;
        }
        return Choice.MAX_UTILITY;
    }

    protected double goAhead(Environment E, Choice a) {
        if (a.getName().equals("MOVE")) {
            a.setAnnotation(this.myMethod());
            return U(S(E, a));
        } else if (a.getName().equals("LEFT") || a.getName().equals("RIGHT")) {
            a.setAnnotation(this.myMethod());
            return U(S(E, a), new Choice("MOVE"));
        }
        return Choice.MAX_UTILITY;

    }

    protected double goLowEnergy(Environment E, Choice a) {
        if (E.getGround() > 0) {
            return goLanding(E, a);
        } else if (a.getName().equals("RECHARGE")) {
            return Choice.ANY_VALUE;
        }
        return Choice.MAX_UTILITY;
    }

    @Override
    protected double U(Environment E, Choice a) {
        if (E.getEnergy() * 100 / E.getAutonomy() < 20) {
            return goLowEnergy(E, a);
        }
        if (E.getDistance() > 0
                && E.getGPS().getZ() < E.getMaxlevel()) {
//                && E.getGPS().getZ() < Math.min(E.getVisualFront() + 15, E.getMaxlevel())) {
            return goTakeOff(E, a);
        } else if (E.getDistance() == 0 && E.getGround() > 0) {
            return goLanding(E, a);
        } else if (E.isTargetBack()) {
            return goTurnBack(E, a);
        } else {
            return goAhead(E, a);
        }
    }

//    protected ACLMessage droidReply(ACLMessage msg) {
//        ACLMessage out;
//        if (msg != null) {
//            out = msg.createReply();
//        } else {
//            out = new ACLMessage();
//        }
//        out.addUserDefinedParameter(ACLMROLE, "DROIDSHIP");
//        return out;
//    }
//
    protected Status processAsynchronousMessages() {
        for (ACLMessage m : this.getInboundOpen()) {
//            String tokens[] = m.getContent().split(",")[0].split(" ");
//            InfoMessage("Received request from " + m.getSender().getLocalName());
            if (!m.getContent().startsWith("REPORT")) {
                if (!m.getProtocol().equals("DROIDSHIP")) {
                    forgetUtterance(m);
                } else if (m.getPerformative() == ACLMessage.QUERY_REF && m.getContent().toUpperCase().equals("TRANSPONDER")
                        && m.getPerformative() == ACLMessage.QUERY_REF) {
                    outbox = respondTo(m, ACLMessage.INFORM, this.Transponder(), null);
                    this.Dialogue(outbox);
                    this.forgetUtterance(m);
                }
            }
        }
        return myStatus;
    }

    protected ACLMessage respondTo(ACLMessage incoming, int Performative, String what, String toWhom) {
        ACLMessage res;
        InfoMessage(what);
        if (incoming == null) {
            res = new ACLMessage(Performative);
            res.setConversationId(this.sessionKey);
            res.setReplyWith(getHexaKey(6));
            res.setSender(getAID());
            res.addReceiver(new AID(toWhom, AID.ISLOCALNAME));
            res.addUserDefinedParameter(ACLMROLE, "DROIDSHIP");
        } else {
            res = incoming.createReply();
            res.setPerformative(Performative);
            res.setConversationId(this.sessionKey);
            res.setReplyWith(getHexaKey(6));
            res.addUserDefinedParameter(ACLMROLE, "DROIDSHIP");
        }
        res.setContent(what);
        return res;
    }
//    protected String askTransponder(String toWhom) {
//        outbox = new ACLMessage(ACLMessage.QUERY_REF);
//        outbox.setSender(getAID());
//        outbox.addReceiver(new AID(toWhom, AID.ISLOCALNAME));
//        outbox.setConversationId("TRANSPONDER"+getHexaKey());
//        outbox.setReplyWith(outbox.getConversationId());
//        outbox.setContent("TRANSPONDER");
//        this.LARVAsend(outbox);
//        inbox = LARVAblockingReceive();
//        if (inbox.getPerformative() == ACLMessage.INFORM) {
//            return inbox.getContent();
//        } else {
//            return "";
//        }
//    }
    protected void InfoMessage(String what) {
        Info(what);
        if (debugDroid) {
            Message(what, "/resources/images/" + myType + ".png");
        }
    }

    protected Status doGoalBackup() {
        goalTokens = E.getCurrentGoal().split(" ");
        toWhom = goalTokens[1];
        sTransponder = this.askTransponder(toWhom);
        if (sTransponder.length() == 0) {
            this.Dialogue(this.respondTo(null, ACLMessage.REFUSE, "Sorry," + toWhom + " your position is not available in Transponder", toWhom));
            return myStatus;
        }
        try {
            pTarget = new Point3D(this.getTransponderField(sTransponder, "GPS"));
            if (pTarget.isEqualTo(E.getGPS())) {
                outbox = this.respondTo(null, ACLMessage.INFORM, "Backup to " + toWhom + " starts now!\nRoger! Roger!", goalTokens[1]);
                Dialogue(outbox);
                this.forgetUtterance(outbox);
//                logger.offEcho();
                return Status.WAIT;
            } else {
                this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, " + toWhom + " your position is not valid", toWhom));
                logger.offEcho();
                return Status.CHOOSEMISSION;
            }
        } catch (Exception ex) {
            this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, " + toWhom + " your transponder seems to be corrupted", toWhom));
            logger.offEcho();
            return Status.CHOOSEMISSION;
        }

    }

    protected Status doGoalRefill() {
        try {
            goalTokens = E.getCurrentGoal().split(" ");
            toWhom = goalTokens[1];
            sTransponder = this.askTransponder(toWhom);
            if (sTransponder.length() == 0) {
                this.Dialogue(this.respondTo(null, ACLMessage.REFUSE, "Sorry," + toWhom + " your position is not available in Transponder", toWhom));
                return myStatus;
            }
//            InfoMessage("Transponder received ok");
            pTarget = new Point3D(this.getTransponderField(sTransponder, "GPS"));
            if (pTarget.isEqualTo(E.getGPS())) {
                if (!this.MyExecuteAction(E.getCurrentGoal())) {
                    Info("Communication error");
                    return Status.CHECKOUT;
                }
                outbox = this.respondTo(null, ACLMessage.INFORM, "Recharge completed!", goalTokens[1]);
                Dialogue(outbox);
                this.forgetUtterance(outbox);
                this.offMission();
                logger.offEcho();
                return Status.CHOOSEMISSION;
            } else {
                this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, your position is not valid", toWhom));
                return Status.CHOOSEMISSION;
            }
        } catch (Exception ex) {
            this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, " + toWhom + " your transponder seems to be corrupted", toWhom));
            logger.offEcho();
            return Status.CHOOSEMISSION;
        }

    }

    protected Status doGoalMoveIn() {
        if (needCourse) {
            nextCity = getEnvironment().getCurrentGoal().replace("MOVEIN ", "");
            if (E.getCurrentCity().equals(nextCity)) {
                E.setNextGoal();
                return Status.SOLVEMISSION;
            }
            if (!this.doFindCourseIn(nextCity)) {
                this.LARVAwait(1000);
                E.setNextGoal();
                return Status.EXIT;
            }
            needCourse = false;
        }
        return MyMoveProblem();
    }

    protected Status doGoalMoveTo() {
        if (needCourse) {
            goalTokens = E.getCurrentGoal().split(" ");
            if (E.getGPS().getX() == Integer.parseInt(goalTokens[1])
                    && E.getGPS().getY() == Integer.parseInt(goalTokens[2])
                    && E.getGround() == 0) {
                E.setNextGoal();
                needCourse = false;
                return Status.SOLVEMISSION;
            }
            if (!this.doFindCourseTo(Integer.parseInt(goalTokens[1]), Integer.parseInt(goalTokens[2]))) {
                this.LARVAwait(1000);
                E.setNextGoal();
                return Status.EXIT;
            }
            needCourse = false;
        }
        return MyMoveProblem();

    }
    protected String askTransponder(String toWhom) {
        outbox = new ACLMessage(ACLMessage.QUERY_REF);
        outbox.setSender(getAID());
        outbox.addReceiver(new AID(toWhom, AID.ISLOCALNAME));
        outbox.setConversationId("TRANSPONDER"+getHexaKey());
        outbox.setReplyWith(outbox.getConversationId());
        outbox.setContent("TRANSPONDER");
        outbox.setReplyByDate(nextSecs(45).toDate());
        inBoxes = blockingDialogue(outbox);
        if (inBoxes.size() > 0 && inBoxes.get(0).getPerformative() == ACLMessage.INFORM) {
            return inBoxes.get(0).getContent();
        } else {
            Alert("No answer to Transponder of " + toWhom);
            return "";
        }
    }


    protected Status onDemandTransfer(ACLMessage m) {
        try {
            fromWho = m.getSender().getLocalName();
            who = m.getContent().replace("TRANSFER ", "");
            InfoMessage("Received TRANSFER  " + who + " from " + fromWho);
            sTransponder = this.askTransponder(fromWho);
            if (sTransponder.length() == 0) {
                this.Dialogue(this.respondTo(null, ACLMessage.REFUSE, "Sorry," + fromWho + " your position is not available in Transponder", toWhom));
                return myStatus;
            }
            InfoMessage("Transponder received ok");
            pTarget = new Point3D(this.getTransponderField(sTransponder, "GPS"));
            if (pTarget.isEqualTo(E.getGPS())) {
                if (this.MyExecuteAction("TRANSFER " + fromWho + " " + who)) {
                    outbox = respondTo(m, ACLMessage.INFORM, "DONE", null);
                    this.Dialogue(outbox);
                    logger.offEcho();
                    InfoMessage("Transfer " + who + " ok");
                    return myStatus;
                } else {
                    InfoMessage("Sorry, TRANSFER " + who + " from " + fromWho + " has failed");
                    this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, TRANSFER " + who + " from " + fromWho + " has failed", fromWho));
                    logger.offEcho();
                    return myStatus;
                }
            } else {
                InfoMessage("Sorry, your position is not valid");
                this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, your position is not valid", fromWho));
                logger.offEcho();
                return Status.CHOOSEMISSION;
            }
        } catch (Exception ex) {
            this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, " + fromWho + " your transponder seems to be corrupted", toWhom));
            logger.offEcho();
            return Status.CHOOSEMISSION;
        }
    }

    protected Status onDemandReport(ACLMessage m) {
//                droidReply(m);
        toWhom = m.getSender().getLocalName();
//        InfoMessage("Received REPORT from " + toWhom + " ... checking report");
        String census = checkCensus(m);
        if (census.length() == 0) {
            ACLMessage auxOutbox, auxInbox;
            auxOutbox = newStealthMessage();
//            auxOutbox = new ACLMessage(ACLMessage.REQUEST);
//            auxOutbox.setSender(getAID());
//            auxOutbox.addReceiver(new AID(this.mySessionmanager, AID.ISLOCALNAME));
//            auxOutbox.setConversationId(this.mySessionID);
//            auxOutbox.setPerformative(ACLMessage.REQUEST);
//            auxOutbox.addUserDefinedParameter(ACLMSTEALTH, "TRUE");
            auxOutbox.setContent("Confirm");
            auxOutbox.setPerformative(ACLMessage.INFORM_REF);
            auxOutbox.setReplyWith("CONFIRM MISSION " + m.getSender().getLocalName());
            session = this.blockingDialogue(auxOutbox).get(0);
            outbox = respondTo(m, ACLMessage.CONFIRM, "Ok. Your report is right"
                    + "\n" + emojis.ROBOT + " From: " + m.getSender().getLocalName(), null);
            outbox.setContent("Confirm");
            outbox.setReplyWith("Confirm");
            this.Dialogue(outbox);
            this.waitReport = false;
            return Status.CHOOSEMISSION;
        } else {
            InfoMessage(emojis.WARNING + " Sorry. Your report is not valid"
                    + "\n" + emojis.ROBOT + " From: " + m.getSender().getLocalName());
            outbox.setPerformative(ACLMessage.DISCONFIRM);
            outbox.setContent("Disconfirm " + census);
            outbox.setReplyWith("Disconfirm");
            this.Dialogue(outbox);
            this.forgetUtterance(m);
            return myStatus;
        }
    }

    protected Status onDemandBackup(ACLMessage m) {
        try {
            toWhom = m.getSender().getLocalName();
            InfoMessage("Received BACKUP request"
                    + "\n" + emojis.ROBOT + " From: " + m.getSender().getLocalName());
            sTransponder = this.askTransponder(toWhom);
            if (sTransponder.length() == 0) {
                this.Dialogue(this.respondTo(null, ACLMessage.REFUSE, "Sorry," + toWhom + " your position is not available in Transponder", toWhom));
                return myStatus;
            }
            pTarget = new Point3D(this.getTransponderField(sTransponder, "GPS"));
            this.onMission(m, "BACKUP" + toWhom,
                    new String[]{"MOVETO " + pTarget.getXInt() + " " + pTarget.getYInt(),
                        "BACKUP " + toWhom});
            this.Dialogue(this.respondTo(m, ACLMessage.AGREE, "Received transponder ok\non my way to your position"
                    + "\n" + emojis.ROBOT + " From: " + m.getSender().getLocalName(), null));
            return Status.SOLVEMISSION;
        } catch (Exception ex) {
            this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, your transponder seems to be corrupted"
                    + "\n" + emojis.ROBOT + " From: " + m.getSender().getLocalName()
                    + "\n" + emojis.FOLDER + " Content: " + m.getContent(), sTransponder));
            logger.offEcho();
            return Status.CHOOSEMISSION;
        }

    }

    protected Status onDemandRefill(ACLMessage m) {
        try {
            toWhom = m.getSender().getLocalName();
            InfoMessage("Received REFILL request"
                    + "\n" + emojis.ROBOT + " From: " + m.getSender().getLocalName());
            sTransponder = this.askTransponder(toWhom);
            if (sTransponder.length() == 0) {
                this.Dialogue(this.respondTo(m, ACLMessage.REFUSE, "Sorry, your position is not available in Transponder"
                        + "\n" + emojis.ROBOT + " From: " + m.getSender().getLocalName()
                        + "\n" + emojis.FOLDER + " Content: " + sTransponder, null));
                return myStatus;
            }
            pTarget = new Point3D(this.getTransponderField(sTransponder, "GPS"));
            this.onMission(m, "REFILL " + toWhom,
                    new String[]{"MOVETO " + pTarget.getXInt() + " " + pTarget.getYInt(),
                        "REFILL " + toWhom});
            this.Dialogue(this.respondTo(m, ACLMessage.AGREE, "Received transponder ok\non my way to your position"
                    + "\n" + emojis.ROBOT + " From: " + m.getSender().getLocalName(), null));
            return Status.SOLVEMISSION;
        } catch (Exception ex) {
            this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, your transponder seems to be corrupted"
                    + "\n" + emojis.ROBOT + " From: " + m.getSender().getLocalName()
                    + "\n" + emojis.FOLDER + " Content: " + sTransponder, toWhom));
            logger.offEcho();
            return Status.CHOOSEMISSION;
        }
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

    protected Status doQueryPeople(String type) {
        Info("Querying people " + type);
        outbox = session.createReply();
        outbox.setContent("Query " + type.toUpperCase() + " session " + sessionKey);
        outbox.setPerformative(ACLMessage.QUERY_REF);
        session = this.blockingDialogue(outbox).get(0);
        population = new ThingSet();
        String unzipedcontent = unzipString(session.getContent().replace("ZIPDATA", ""));
        JsonObject jspeople = Json.parse(unzipedcontent).asObject().get("thingset").asObject();
//        System.out.println("CENSUS:"+jspeople.toString(WriterConfig.PRETTY_PRINT));
        population.fromJson(jspeople.get("people").asArray());
//        for (Thing t : population.getAllThings()) {
//            if (citizenOf.keySet().contains(t.getName())) {
//                if (citizenOf.get(t.getName()).equals())
//            }
//        }
        return myStatus;
    }

    protected ACLMessage newStealthMessage() {
        ACLMessage auxOutbox;
        auxOutbox = new ACLMessage(ACLMessage.REQUEST);
        auxOutbox.setSender(getAID());
        auxOutbox.addReceiver(new AID(this.mySessionmanager, AID.ISLOCALNAME));
        auxOutbox.setConversationId(this.mySessionID);
        auxOutbox.addUserDefinedParameter(ACLMSTEALTH, "TRUE");
        auxOutbox.setContent("STEALTH");
        auxOutbox.setReplyWith("STEALTH");
        return auxOutbox;
    }
}
