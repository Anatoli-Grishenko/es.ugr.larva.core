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
    protected double radiusPercentage = 0.2;
    protected Point3D mapCenter;
    protected int nbackup = 0, nrefill = 0, myCity;

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
    protected String problems[], plan[], actions[], around[];
    protected ACLMessage open, session;
    protected String[] contentTokens, cities, goalTokens;
    protected String action = "", preplan = "", baseCity, currentCity, nextCity, myMission, Message;
    protected Point3D gpsBase, pTarget, pSource;
    protected int indexplan = 0, myEnergy = 0, counterSessionManager = 0;
    protected boolean showPerceptions, recruitedMission, localMission;

    protected String whichWall, nextWhichwall;
    protected double distance, nextdistance;
    protected ACLMessage msgMission, msgBackup;

    protected boolean allowREQUEST, allowCFP, allowParking;
    protected boolean inNegotiation;
    protected String rw = "", toWhom, fromWho, who, sTransponder, myType, employer = "";

    protected Environment Ei, Ef;
    protected Choice a;
    protected TimeHandler tini, tend, tinibkp;
    protected int tminbkpout = 5000, ////> Minimo backup time miliecs si no isconnected()
            tmaxbkpout = 30000; ///> Maximo si connected, milisecs
    protected int realParkingTime;
    protected boolean needCourse = true;
    protected ThingSet population;
    protected boolean waitReport = false;
    protected boolean Slave = true;

    @Override
    public void setup() {
        this.allowExceptionShield = false;
        this.disableDeepLARVAMonitoring();
        super.setup();
        logger.setLoggerFileName(null);
        citizenOf = new HashMap();
        myCitizens = new HashMap();
        logger.onEcho();
//        logger.offEcho();
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
        myStatus = Status.START;
        myText = null;
        recruitedMission = false;
        localMission = false;
        allowCFP = true;
        allowREQUEST = true;
        allowParking = false;
        inNegotiation = false;
        usePerformatives = true;
        this.frameDelay = 10;
        this.DFSetMyServices(new String[]{"DROIDSHIP"});
        if (Slave) {
            handleAlias();
        }
        logger.offSaveDisk();
        logger.offEcho();
//        logger.onEcho();
    }

    protected void handleAlias() {
        loadSessionAlias();
        getSharedSession(getSessionAlias());
        Info("This is what I know:"
                + "\nController " + Controller
                + "\nSession Key " + sessionKey
                + "\nSession Alias " + getSessionAlias()
                + "\nSession Manager " + sessionManager
        );
        this.DFAddMyServices(new String[]{
            getSessionAlias(),
            sessionKey});
    }

    protected void defController() {
        this.DFAddMyServices(new String[]{
            "OPENER",
            "CONTROLLER"});
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
                myStatus = autoChooseLocalMission();
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
                Message("Exit");
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

//    public Status endBackup() {
//        InfoMessage("Backup to " + toWhom + " has just finished\nGood luck!");
//        this.offRecruitedMission();
//        return Status.CHOOSEMISSION;
//    }
    public Status MyWait() {
//        if (tini == null) {
//            realParkingTime = PARKINGTIME + (int) (Math.random() * PARKINGTIME);
//            tini = new TimeHandler();
//            E.setCurrentMission("BACKUP", new String[]{"BACKUP"});
//        }
//        if (isOnMission()) {
//            TimeHandler tnowbkp = new TimeHandler();
//            if (tinibkp.elapsedTimeMilisecsUntil(tnowbkp) > tminbkpout && !AMSIsConnected(toWhom)) {
//                return endBackup();
//            }
//            if (tinibkp.elapsedTimeMilisecsUntil(tnowbkp) > tmaxbkpout) {
//                return endBackup();
//            }
//        }
        this.defaultBehaviour.block(1000);
        this.checkDialogues();
        return myStatus;
    }

    public Status MyCheckin() {
        boolean cin;
        String fakepassport = "TlBDIEFHRU5U ZXlKUFRFVk5SVlJCSWpwN0ltbGtJam9pV0UxUk56SlBSVzVPWjBaS1VqSXpjaUlzSW5SNWNHVWlPaUpQVEVWUVFWTlRVRTlTVkNJc0ltWnBaV3hrY3lJNmV5SnlZWGRRWVhOemNHOXlkQ0k2SWlJc0luVnpaWEpKUkNJNklpSXNJbU5wWkNJNklpSXNJbUZzYVdGeklqb2lJaXdpWlcxaGFXd2lPaUlpTENKdVlXMWxJam9pSW4wc0ltUmhkR1VpT2lJeU1ESXlMVEE1TFRJeUlEQTNPakF5T2pJM09qTXdPQ0lzSW1SbGMyTnlhWEIwYVc5dUlqb2lTbE5QVGlCUFltcGxZM1FnVEdsdWEyVmtJR0Z1WkNCRmJXSmxaR1ZrSWl3aWIyeGxJanAwY25WbExDSmpjbmx3ZEc4aU9pSWlmU3dpZFhObGNrbEVJam94TURBd0xDSmphV1FpT2lJaUxDSmhiR2xoY3lJNklrbGpaVzFoYmlJc0ltVnRZV2xzSWpvaWJDNWpZWE4wYVd4c2IwQmtaV056WVdrdWRXZHlMbVZ6SWl3aWJtRnRaU0k2SWs1UVF5QkJSMFZPVkNKOQ==",
                realpassport = mypassport;

        OlePassport op = new OlePassport();
        op.loadPassport(oleConfig.getTab("Identity").getString("Passport file", ""));
        Info("Checking-in to LARVA");
        IdentityManager = DFGetAllProvidersOf("IDENTITY").get(0);
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

    public String autoSelectCity() {
        String city; // = {"Fort Babine", "Bakerville", "Port Alexander"};
        double sx = 0, sy = 0, x = 0, y = 0, n = 0, minx = 1000000, maxx = -minx, miny = minx, maxy = -maxx;
        Point3D p;
        doQueryCities();

//        for (String s : E.getCityList()) {
//            try {
//                p = E.getCityPosition(s);
//                x = p.getX();
//                y = p.getY();
//                sx += x;
//                sy += y;
//                if (maxx < x) {
//                    maxx = x;
//                }
//                if (minx > x) {
//                    minx = x;
//                }
//                if (maxy < y) {
//                    maxy = y;
//                }
//                if (miny > y) {
//                    miny = y;
//                }
//                n++;
//            } catch (Exception ex) {
//                System.out.println("EX " + ex.toString());
//            }
//        }
//        mapCenter = new Point3D(518, 603, 0);
//        mapCenter = new Point3D((maxx-minx)/n, (maxy-miny)/n, 0);
        
        mapCenter = E.getCityPosition(E.getCityList()[0]);
        around = E.getCitiesAround(mapCenter, 160);
        myCity = 0;
        city = around[myCity];
        return city;
    }

    public String autoSelectNextCity() {
        myCity = (myCity+1)%around.length;
        return around[myCity];
    }

//    public String mySelectCityCourse() {
//        String city;
//        if (!this.MyReadPerceptions()) {
//            Info("Communication error");
//            return "";
//        }
//        do {
//            city = cities[(int) (Math.random() * cities.length)];
//        } while (city.equals(E.getCurrentCity()) && !this.doFindCourseIn(city));
//        return city;
//    }
    public Status autoChooseLocalMission() {
        nextCity = this.autoSelectNextCity();
        this.onLocalMission("AUTOMODE", new String[]{"MOVEIN " + nextCity});
        return Status.SOLVEMISSION;
    }

    public Status MyBackup() {
        return Status.CHOOSEMISSION;
    }

    public Status MyTransferIn() {
        return Status.CHOOSEMISSION;
    }

    public Status MyParking() {
//        this.offRecruitedMission();
        if (!allowParking) {
            return Status.CHOOSEMISSION;
        }
        if (tini == null) {
            realParkingTime = PARKINGTIME + (int) (Math.random() * PARKINGTIME);
            tini = new TimeHandler();
        }
        tend = new TimeHandler();
        E.setCurrentMission("WAITING", new String[]{"WAITING "});
        this.defaultBehaviour.block();
//        tend = new TimeHandler();
////        this.sendStealthTransponder();
//        if (tini.elapsedTimeSecsUntil(tend) >= realParkingTime) {
//            tini = null;
//            return Status.CHOOSEMISSION;
//        }
//        return myStatus;
        return Status.CHOOSEMISSION;
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
            return Status.SOLVEMISSION;
        }
        Info(easyPrintPerceptions());
        Choice a = Ag(E, A);
        if (a == null) {
            Alert("Found no action to execute");
            return Status.CLOSEMISSION;
        } else {// Execute
            Info("Excuting " + a);
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
            needCourse = true;
            return Status.PARKING;
        }
        goalTokens = E.getCurrentGoal().toUpperCase().split(" ");
        switch (goalTokens[0]) {
            case "PARKING":
                return Status.PARKING;
            case "MOVETO":
                return doGoalMoveTo();
            case "MOVEIN":
                return doGoalMoveIn();
            case "REFILL":
                return doGoalRefill();
            case "BACKUP":
                return doGoalBackup();
            case "TRANSFERIN":
                return MyTransferIn();
            case "EXIT":
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
        Info("Searching a course in " + destination);
        Point3D p = E.getCityPosition(destination);
        return doFindCourseTo(p.getXInt(), p.getYInt());
    }

    public boolean doFindCourseTo(int x, int y) {
        Info("Searching a course to " + x + " " + y);
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
        Info("Joining session " + getSessionAlias());
        if (sessionKey.length() == 0) {
            Error("Sorry service SESSION MANAGER not found for alias " + sessionAlias);
            return Status.CHECKOUT;
        }
        if (!this.doQueryCities()) {
            return Status.EXIT;
        }
        cities = E.getCityList();
        if (cities.length == 0) {
            Error("Sorry this agent can only join worlds with cities");
            return Status.CHECKOUT;
        }
        baseCity = this.autoSelectCity();
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
        publish("Hi I am a Droidship of type " + myType);
        return Status.CHOOSEMISSION;
    }

    boolean doQueryCities() {
        Info("Querying CITIES");
        outbox = respondTo(session, ACLMessage.QUERY_REF, "Query CITIES session " + sessionKey, sessionManager);
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
        outbox = respondTo(session, ACLMessage.QUERY_REF, "Query MISSIONS session " + sessionKey, sessionManager);
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

    public void onLocalMission(String mission, String[] goals) {
        localMission = true;
        this.needCourse = true;
        E.setCurrentMission(mission, goals);
        Info("On local mission " + E.getCurrentMission().toString());
    }

    public void offLocalMission() {
        localMission = false;
        Info("Off local mission ");
    }

    public boolean isLocalMission() {
        return localMission;
    }

    public void onRecruitedMission(ACLMessage msg, String mission, String[] goals) {
        recruitedMission = true;
        msgMission = msg;
        this.needCourse = true;
        E.setCurrentMission(mission, goals);
        Info("On recruited mission " + E.getCurrentMission().toString());
    }

    public void offRecruitedMission() {
        recruitedMission = false;
    }

    public boolean isRecruitedMission() {
        return recruitedMission;
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

    protected Status processAsynchronousMessages() {
        for (ACLMessage m : this.getInboundOpen()) {
            if (!m.getContent().startsWith("REPORT")) {
                if (!m.getProtocol().equals("DROIDSHIP")) {
                    forget(m);
                } else if (m.getPerformative() == ACLMessage.QUERY_REF && m.getContent().toUpperCase().equals("TRANSPONDER")) {
                    outbox = respondTo(m, ACLMessage.INFORM, this.Transponder(), null);
                    this.Dialogue(outbox);
                    this.forget(m);
                }
            } 
        }
        return myStatus;
    }

    protected ACLMessage respondTo(ACLMessage incoming, int Performative, String what, String toWhom) {
        ACLMessage res;
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
            res.setReplyWith(getHexaKey(6));
            res.addUserDefinedParameter(ACLMROLE, "DROIDSHIP");
        }
        res.setContent(what);
        return res;
    }

    protected void InfoMessage(String what) {
        Info(what);
        if (debugDroid) {
            Message(what, "/resources/images/" + myType + ".png");
        }
    }

    protected Status MyCloseMission() {
        if (isLocalMission()) {
            offLocalMission();
        }
        if (isRecruitedMission()) {
            offRecruitedMission();
        }
        return Status.CHOOSEMISSION;
    }

    protected Status doGoalBackup() {
        goalTokens = E.getCurrentGoal().split(" ");
        if (!goalTokens[0].equals("BACKUP")) {
            return Status.CHECKOUT;
        }
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
                publish("BACKUP to " + toWhom + " ongoing!");
                tinibkp = new TimeHandler();
                this.forget(outbox);
//                logger.offEcho();
                return Status.WAIT;
            } else {
                outbox = this.respondTo(null, ACLMessage.FAILURE, "Sorry, " + toWhom + " your position is not valid", toWhom);
                this.Dialogue(outbox);
//                logger.offEcho();
                return Status.CHOOSEMISSION;
            }
        } catch (Exception ex) {
            this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, "Sorry, " + toWhom + " your transponder seems to be corrupted", toWhom));
//            logger.offEcho();
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
                publish("Recharge to" + toWhom + " done!");
                this.forget(outbox);
                System.out.println("NREFILL: " + (nrefill++));
                this.offRecruitedMission();
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
            nextCity = E.getCurrentGoal().replace("MOVEIN ", "");
            if (E.getCurrentCity().equals(nextCity)) {
                E.setNextGoal();
                return Status.SOLVEMISSION;
            }
            if (!this.doFindCourseIn(nextCity)) {
                Alert("Could not find a course towards " + nextCity);
                return Status.EXIT;
            }
            needCourse = false;
        }
        return MyMoveProblem();
    }

    protected Status doGoalMoveTo() {
        int x, y;
        if (needCourse) {
            goalTokens = E.getCurrentGoal().split(" ");
            if (E.getGPS().getX() == Integer.parseInt(goalTokens[1])
                    && E.getGPS().getY() == Integer.parseInt(goalTokens[2])
                    && E.getGround() == 0) {
                E.setNextGoal();
                needCourse = false;
                return Status.SOLVEMISSION;
            }
            x = Integer.parseInt(goalTokens[1]);
            y = Integer.parseInt(goalTokens[2]);
            if (!this.doFindCourseTo(x, y)) {
                Alert("Could not find a course towards ");
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
        outbox.setConversationId("TRANSPONDER" + getHexaKey());
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
//            InfoMessage("Received TRANSFER  " + who + " from " + fromWho);
            sTransponder = this.askTransponder(fromWho);
            if (sTransponder.length() == 0) {
                this.Dialogue(this.respondTo(null, ACLMessage.REFUSE, "Sorry," + fromWho + " your position is not available in Transponder", toWhom));
                return myStatus;
            }
//            InfoMessage("Transponder received ok");
            pTarget = new Point3D(this.getTransponderField(sTransponder, "GPS"));
            if (pTarget.isEqualTo(E.getGPS())) {
                if (this.MyExecuteAction("TRANSFER " + fromWho + " " + who)) {
                    outbox = respondTo(m, ACLMessage.INFORM, "DONE", null);
                    this.Dialogue(outbox);
                    logger.offEcho();
//                    InfoMessage("Transfer " + who + " ok");
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
            this.forget(m);
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
            this.onRecruitedMission(m, "BACKUP" + toWhom,
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
            this.onRecruitedMission(m, "REFILL " + toWhom,
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
        outbox = respondTo(session, ACLMessage.QUERY_REF, "Query " + type.toUpperCase() + " session " + sessionKey, sessionManager);
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

    // A new method just to show the information of sensors in console
    public String easyPrintPerceptions() {
        String res;
        int matrix[][];
        if (!logger.isEcho()) {
            return "";
        }
        if (getEnvironment() == null) {
            Error("Environment is unacessible, please setupEnvironment() first");
            return "";
        }
        if (!showPerceptions) {
            return "";
        }
        res = "\n\nReading of sensors\n";
        if (getEnvironment().getName() == null) {
            res += emojis.WARNING + " UNKNOWN AGENT";
            return res;
        } else {
            res += emojis.ROBOT + " " + getEnvironment().getName();
        }
        res += "\n";
        res += String.format("%10s: %05d W %05d W %05d W\n", "ENERGY",
                getEnvironment().getEnergy(), getEnvironment().getEnergyburnt(), myEnergy);
        res += String.format("%10s: %15s\n", "POSITION", getEnvironment().getGPS().toString());
//        res += "PAYLOAD "+getEnvironment().getPayload()+" m"+"\n";
        res += String.format("%10s: %05d m\n", "X", getEnvironment().getGPS().getXInt())
                + String.format("%10s: %05d m\n", "Y", getEnvironment().getGPS().getYInt())
                + String.format("%10s: %05d m\n", "Z", getEnvironment().getGPS().getZInt())
                + String.format("%10s: %05d m\n", "MAXLEVEL", getEnvironment().getMaxlevel())
                + String.format("%10s: %05d m\n", "MAXSLOPE", getEnvironment().getMaxslope());
        res += String.format("%10s: %05d m\n", "GROUND", getEnvironment().getGround());
        res += String.format("%10s: %05d º\n", "COMPASS", getEnvironment().getCompass());
        if (getEnvironment().getTarget() == null) {
            res += String.format("%10s: " + "!", "TARGET");
        } else {
            res += String.format("%10s: %05.2f m\n", "DISTANCE", getEnvironment().getDistance());
            res += String.format("%10s: %05.2f º\n", "ABS ALPHA", getEnvironment().getAngular());
            res += String.format("%10s: %05.2f º\n", "REL ALPHA", getEnvironment().getRelativeAngular());
        }
//        res += "\nVISUAL ABSOLUTE\n";
//        matrix = getEnvironment().getAbsoluteVisual();
//        for (int y = 0; y < matrix[0].length; y++) {
//            for (int x = 0; x < matrix.length; x++) {
//                res += printValue(matrix[x][y]);
//            }
//            res += "\n";
//        }
//        for (int x = 0; x < matrix.length; x++) {
//            if (x != matrix.length / 2) {
//                res += "----";
//            } else {
//                res += "[  ]-";
//            }
//        }
        res += "\nVISUAL RELATIVE\n";
        matrix = getEnvironment().getRelativeVisual();
        for (int y = 0; y < matrix[0].length; y++) {
            for (int x = 0; x < matrix.length; x++) {
                res += printValue(matrix[x][y]);
            }
            res += "\n";
        }
        for (int x = 0; x < matrix.length; x++) {
            if (x != matrix.length / 2) {
                res += "----";
            } else {
                res += "[  ]-";
            }
        }
        res += "VISUAL POLAR\n";
        matrix = getEnvironment().getPolarVisual();
        for (int y = 0; y < matrix[0].length; y++) {
            for (int x = 0; x < matrix.length; x++) {
                res += printValue(matrix[x][y]);
            }
            res += "\n";
        }
        res += "\n";
        res += "\nLIDAR RELATIVE\n";
        matrix = getEnvironment().getRelativeLidar();
        for (int y = 0; y < matrix[0].length; y++) {
            for (int x = 0; x < matrix.length; x++) {
                res += printValue(matrix[x][y]);
            }
            res += "\n";
        }
        for (int x = 0; x < matrix.length; x++) {
            if (x != matrix.length / 2) {
                res += "----";
            } else {
                res += "-^^-";
            }
        }
        res += "\n";
        return res;
    }

    protected String printValue(int v) {
        if (v == Perceptor.NULLREAD) {
            return "XXX ";
        } else {
            return String.format("%03d ", v);
        }
    }

    protected String printValue(double v) {
        if (v == Perceptor.NULLREAD) {
            return "XXX ";
        } else {
            return String.format("%05.2f ", v);
        }
    }

    @Override
    protected String Transponder() {
        String goal;
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
        goal = "GOAL " + E.getCurrentGoal();
        answer += sep + goal + sep + "MISSION " + E.getCurrentMission();
        return answer;
    }

    public void publish(String msg) {
        outbox = respondTo(session, ACLMessage.REQUEST,
                "REQUEST PUBLISH " + msg, sessionManager);
        outbox.setConversationId(sessionKey);
        LARVAsend(outbox);
    }

    public synchronized boolean getSharedSession(String prefix) {
        if (DFGetAllProvidersOf("CONTROLLER").isEmpty()) {
            sessionManager = DFGetAllProvidersOf("SESSION MANAGER " + getSessionAlias()).get(0);
            for (String key : DFGetAllServicesProvidedBy(sessionManager)) {
                if (key.startsWith("SESSION::")) {
                    sessionKey = key;
                    return true;
                }
            }
            return false;
        } else {
            for (String controller : DFGetAllProvidersOf("CONTROLLER")) {
                for (String service : DFGetAllServicesProvidedBy(controller)) {
                    if (service.startsWith(prefix)) {
                        Controller = controller;
//                    defSessionAlias(service);
                        sessionManager = DFGetAllProvidersOf("SESSION MANAGER " + getSessionAlias()).get(0);
                        for (String key : DFGetAllServicesProvidedBy(sessionManager)) {
                            if (key.startsWith("SESSION::")) {
                                sessionKey = key;
                                return true;
                            }
                        }

                    }
                }
            }
            return false;
        }
    }
}
