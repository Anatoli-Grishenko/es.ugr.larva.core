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
import static crypto.Keygen.getHexaKey;
import data.OlePassport;
import static disk.Logger.trimFullString;
import geometry.Point3D;
import geometry.SimpleVector3D;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.TimeHandler;
import tools.emojis;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DroidStarshipLevelD extends LARVADialogicalAgent {

    protected enum Status {
        START, CHECKIN, CHECKOUT, JOINSESSION, CHOOSEMISSION, SOLVEMISSION, CLOSEMISSION, EXIT, WAIT, PARKING
    }
    final int PARKINGTIME = 5, WAITIME = 1;

    protected Status myStatus;
    protected String service = "PMANAGER", problem = "",
            problemManager = "", content, sessionKey, sessionManager, gobackHome = "GOBACKHOME";
    protected String problems[], plan[], actions[];
    protected ACLMessage open, session;
    protected String[] contentTokens, cities;
    protected String action = "", preplan = "", baseCity, currentCity, nextCity, myMission;
    protected Point3D gpsBase;
    protected int indexplan = 0, myEnergy = 0, counterSessionManager = 0;
    protected boolean showPerceptions, onMission, changeMission;

    protected String whichWall, nextWhichwall;
    protected double distance, nextdistance;
    protected ACLMessage msgMission;

    protected boolean allowREQUEST, allowCFP, allowParking;
    protected boolean inNegotiation;
    protected String rw = "";
    protected Status reaction;

    protected Plan behaviour = null;
    protected Environment Ei, Ef;
    protected Choice a;
    protected TimeHandler tini, tend;
    protected int realParkingTime;
    protected boolean needCourse = true;

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
////                goal = " WAITING REPORT";
////            } else {
////                if (this.allowParking) {
////                    goal = " PARKING " + (realParkingTime - tini.elapsedTimeSecsUntil(tend));
////                }
////            }
//        } else {
//            goal = "GOAL " + E.getCurrentGoal();
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
        this.frameDelay=10;
    }

    @Override
    public void Execute() {
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
        return Status.EXIT;
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
        this.MyReadPerceptions();
        do {
            city = cities[(int) (Math.random() * cities.length)];
        } while (city.equals(E.getCurrentCity()) && !this.doFindCourseIn(city));
        return city;
    }

    public Status MyChooseMission() {
        nextCity = this.mySelectNextCity();
        E.setCurrentMission("AUTOMODE", new String[]{"MOVEIN " + nextCity});
//        this.
        this.needCourse = true;
        return Status.SOLVEMISSION;
    }

    public Status MyCloseMission() {
        return Status.CHOOSEMISSION;
    }

    public Status MyParking() {
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
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setContent("Request execute " + action + " session " + sessionKey);
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
            needCourse = true;
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
            this.MyExecuteAction(a.getName());
            this.MyReadPerceptions();
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
        String goal[] = E.getCurrentGoal().split(" ");
        switch (goal[0]) {
            case "PARKING":
                return Status.PARKING;
            case "MOVETO":
                if (E.getGPS().getX() == Integer.parseInt(goal[1])
                        && E.getGPS().getY() == Integer.parseInt(goal[2])
                        && E.getGround() == 0) {
                    E.setNextGoal();
//                    this.
                    return Status.SOLVEMISSION;
                } else if (this.doFindCourseTo(Integer.parseInt(goal[1]), Integer.parseInt(goal[2]))) {
                    return MyMoveProblem();
                } else {
                    Alert("Sorry, I cannot find a route in " + goal[0]);
                }
                return Status.SOLVEMISSION;
            case "MOVEIN":
                String targetCity = getEnvironment().getCurrentGoal().replace("MOVEIN ", "");
                if (needCourse) {
                    if (E.getCurrentCity().equals(targetCity)) {
                        E.setNextGoal();
                        return Status.SOLVEMISSION;
                    }
                    if (!this.doFindCourseIn(targetCity)) {
                        this.LARVAwait(1000);
                        E.setNextGoal();
                        return Status.EXIT;
                    }
                    needCourse = false;
                }
                return MyMoveProblem();
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
        outbox.setPerformative(ACLMessage.QUERY_REF);
        outbox.setContent("Request course to " + x + " " + y + " Session " + sessionKey);
        Info("Request course to " + x + " " + y + " Session " + sessionKey);
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
        session = this.blockingDialogue(outbox).get(0);
        if (!session.getContent().toUpperCase().startsWith("CONFIRM")) {
            Error("Could not join session " + sessionKey + " due to " + session.getContent());
            return Status.CHECKOUT;
        }
        if (!this.MyReadPerceptions()) {
            return Status.EXIT;
        }
        gpsBase = E.getGPS();
        return Status.CHOOSEMISSION;
    }

    boolean doQueryCities() {
        Info("Querying CITIES");
        outbox = new ACLMessage(ACLMessage.QUERY_REF);
        outbox.setSender(this.getAID());;
        outbox.addReceiver(new AID(sessionManager, AID.ISLOCALNAME));
        outbox.setContent("Query CITIES session " + sessionKey);
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

    public void onMission(ACLMessage msg, String mission, String[] goals) {
        onMission = true;
        changeMission = true;
        msgMission = msg;
        E.setCurrentMission(mission, goals);
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

    protected void processUnexpectedMessage(ACLMessage msg) {
        logger.onEcho();
        String tokens[] = msg.getContent().split(",")[0].split(" ");
        Info("Unexpected " + msg.getContent());
        if (msg.getContent().toUpperCase().equals("TRANSPOND")) {
            outbox = msg.createReply();
            outbox.setPerformative(ACLMessage.INFORM);
            outbox.setContent(this.Transponder());
            this.Dialogue(msg);
            return;
        }
        if (isOnMission()) {
            if (tokens[0].toUpperCase().equals("CANCEL")) {
                Info("Received CANCEL");
                this.offMission();
                logger.offEcho();
                return;
            }
        } else {
            if (!inNegotiation && allowCFP && tokens[0].toUpperCase().equals("CFP")) { // inNegotiation BYDISTANCE MOVEIN A,MOVEIN C,CAPTURE 5 JEDI,MOVEIN D, TRANSFERTO 5 JEDI TS_FULL
                Info("Received CFP " + msg.getContent());
                if (tokens[1].equals("BYDISTANCE")) {
                    String city = tokens[3];
                    Point3D citypos = E.getCityPosition(city), mypos = E.getGPS();
                    outbox = msg.createReply();
                    outbox.setContent("Propose " + citypos.planeDistanceTo(mypos));
                    Info("City: " + city + "->" + citypos.planeDistanceTo(mypos));
                    this.LARVAsend(outbox);
                    inNegotiation = true;
                    return;
                } else if (tokens[1].toUpperCase().equals("BYCARGO")) {
                    outbox = msg.createReply();
                    outbox.setContent("Propose " + E.getMaxcargo());
                    this.LARVAsend(outbox);
                    inNegotiation = true;
                    return;
                }
            } else if (inNegotiation && allowCFP && tokens[0].toUpperCase().toUpperCase().equals("ACCEPT")) {
                Info("Received ACCEPT " + msg.getContent());
                if (tokens[1].toUpperCase().equals("MOVEIN")) {
                    outbox = msg.createReply();
                    outbox.setContent("Agree");
                    this.LARVAsend(outbox);
                    Message("Contrated by " + msg.getContent());
                    inNegotiation = false;
                    onMission(msg, "COMMITMENT", new String[]{tokens[2] + " " + tokens[3]});
                    Info("new task " + E.getCurrentGoal());
                    reaction = Status.SOLVEMISSION;
                    return;
                }
            } else if (inNegotiation && allowCFP && tokens[0].toUpperCase().equals("REJECT")) {
                Info("Received REJECT");
                inNegotiation = false;
                return;
            } else if (!inNegotiation && allowREQUEST && tokens[0].toUpperCase().equals("REQUEST")) {
                Info("Received REQUEST " + msg.getContent());
                if (tokens[2].toUpperCase().equals("MOVEIN")) {
                    outbox = msg.createReply();
                    outbox.setContent("Agree");
                    this.LARVAsend(outbox);
                    Message("Agree to " + msg.getContent());
                    inNegotiation = false;
                    onMission(msg, "COMMITMENT", new String[]{tokens[2] + " " + tokens[3]});
                    reaction = Status.SOLVEMISSION;
                    return;
                }
            }
        }
        Message("Refuse to " + msg.getContent());
        Info("Refuse " + msg.getContent());
        outbox = msg.createReply();
        outbox.setContent("Refuse");
        this.LARVAsend(outbox);
    }

}
