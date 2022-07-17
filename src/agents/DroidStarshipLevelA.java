/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import Environment.Environment;
import ai.Choice;
import ai.DecisionSet;
import ai.MissionSet;
import data.OlePassport;
import geometry.Point3D;
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
public class DroidStarshipLevelA extends LARVAFirstAgent {

    protected enum Status {
        START, CHECKIN, CHECKOUT, JOINSESSION, CHOOSEMISSION, OPENMISSION, SOLVEMISSION, CLOSEMISSION, EXIT, WAIT, PARKING
    }
    final int PARKINGTIME = 5, WAITIME = 2;

    protected Status myStatus;
    protected String service = "PMANAGER", problem = "",
            problemManager = "", content, sessionKey, sessionManager, gobackHome = "GOBACKHOME";
    protected String problems[], plan[], actions[];
    protected ACLMessage open, session;
    protected String[] contentTokens, cities;
    protected String action = "", preplan = "", baseCity, currentCity, nextCity, myMission;
    protected Point3D gpsBase;
    protected int indexplan = 0, myEnergy = 0, counterSessionManager=0;
    protected boolean showPerceptions, onMission, changeMission;

    String whichWall, nextWhichwall;
    double distance, nextdistance;
    protected ACLMessage msgMission;

    @Override
    public void setup() {
        super.setup();
        logger.onTabular();
        logger.offEcho();
        this.setFrameDelay(0);
        showPerceptions = false;
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
        onMission = false;
        myText = null;
//        openRemote();
    }

    @Override
    public void Execute() {
        Info("Status: " + myStatus.name());
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
            case OPENMISSION:
                myStatus = MyOpenMission();
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
                doExit();
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
        return filterStatus(Status.EXIT.name());
    }

    public Status MyCheckin() {
        Info("Loading passport and checking-in to LARVA");
        OlePassport op = new OlePassport();
        op.setPassport("TlBDIEFnZW50cw== ZXlKUFRFVk5SVlJCSWpwN0ltbGtJam9pVHpJNWRXNWlOblZxVTNCbVp6TlVSaUlzSW5SNWNHVWlPaUpQVEVWUVFWTlRVRTlTVkNJc0ltWnBaV3hrY3lJNmV5SnlZWGRRWVhOemNHOXlkQ0k2SWlJc0luVnpaWEpKUkNJNklpSXNJbU5wWkNJNklpSXNJbUZzYVdGeklqb2lJaXdpWlcxaGFXd2lPaUlpTENKdVlXMWxJam9pSW4wc0ltUmhkR1VpT2lJeU1ESXlMVEEyTFRJeUlERTFPalE1T2pNMU9qVXlNeUlzSW1SbGMyTnlhWEIwYVc5dUlqb2lTbE5QVGlCUFltcGxZM1FnVEdsdWEyVmtJR0Z1WkNCRmJXSmxaR1ZrSWl3aWIyeGxJanAwY25WbExDSmpjbmx3ZEc4aU9pSWlmU3dpZFhObGNrbEVJam94TURBd0xDSmphV1FpT2lJaUxDSmhiR2xoY3lJNklrbGpaVzFoYmlJc0ltVnRZV2xzSWpvaWJDNWpZWE4wYVd4c2IwQmtaV056WVdrdWRXZHlMbVZ6SWl3aWJtRnRaU0k2SWs1UVF5QkJaMlZ1ZEhNaWZRPT0=");
        if (!doLARVACheckin()) {
            Alert("Unable to checkin");
            return Status.EXIT;
        }
        return Status.JOINSESSION;
    }

    public Status MyCheckout() {
        this.doLARVACheckout();
        return Status.EXIT;
    }

    public Status MyChooseMission() {
        myMission = chooseMission();
        return filterStatus(Status.OPENMISSION.name());
    }

    public Status MyOpenMission() {
        nextWhichwall = whichWall = "NONE";
        nextdistance = distance = Choice.MAX_UTILITY;
        return filterStatus(activateMission(myMission));
    }

    public Status MyCloseMission() {
        return filterStatus(Status.CHOOSEMISSION.name());
    }

    public Status MyParking() {
        int time = PARKINGTIME + (int) (Math.random() * PARKINGTIME);
        boolean CFP, exit = false;
        TimeHandler tini, tend;
        long remaining;
        tini = new TimeHandler();
        tend = new TimeHandler();
        CFP = false;
        while (!exit) {
            this.setTask("PARKING " + (time - tini.elapsedTimeSecsUntil(tend)));
            this.MyReadPerceptions();
            inbox = LARVAblockingReceive(500);
            if (inbox != null) {
                contentTokens = inbox.getContent().split(" ");
                if (isOnMission()) {
                    if (contentTokens[0].toUpperCase().equals("CANCEL")) {
                        this.offMission();
                        continue;
                    } 
                } else {
                    if (!CFP && contentTokens[0].toUpperCase().equals("CFP")) {
                        if (contentTokens[1].equals("BYDISTANCE")) {
                            String city = contentTokens[2];
                            Point3D citypos = E.getCityPosition(city), mypos = E.getGPS();
                            outbox = inbox.createReply();
                            outbox.setContent("Propose " + citypos.planeDistanceTo(mypos));
                            this.LARVAsend(outbox);
                            CFP = true;
                            continue;
                        } else if (contentTokens[1].equals("BYCARGO")) {
                            outbox = inbox.createReply();
                            outbox.setContent("Propose " + E.getMaxcargo());
                            this.LARVAsend(outbox);
                            CFP = true;
                            continue;
                        }
                    } else if (CFP && contentTokens[0].toUpperCase().equals("ACCEPT")) {
                        if (contentTokens[1].toUpperCase().equals("MOVEIN")) {
                            onMission(inbox, "MOVEIN " + contentTokens[2]);
                            outbox = msgMission.createReply();
                            outbox.setContent("Agree");
                            this.LARVAsend(outbox);
                            return filterStatus(Status.OPENMISSION.name());
                        }
                    } else if (CFP && contentTokens[0].toUpperCase().equals("REJECT")) {
                        CFP = false;
                        continue;
                    }
                }
                outbox = inbox.createReply();
                outbox.setContent("Refuse");
                this.LARVAsend(outbox);
            }
            tend = new TimeHandler();
            exit = tini.elapsedTimeSecsUntil(tend) >= time && !CFP;
        }
        // Message("Released");
        return filterStatus(Status.CHOOSEMISSION.name());
    }

    protected boolean MyExecuteAction(String action) {
        Info("Executing action " + action);
        outbox = session.createReply();
        outbox.setContent("Request execute " + action + " session " + sessionKey);
        toSessionManager(outbox);
        this.myEnergy++;
        session = this.fromSessionManager();
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
        toSessionManager(outbox);
        this.myEnergy++;
        session = this.LARVAblockingReceive(MessageTemplate.MatchSender(new AID(sessionManager, AID.ISLOCALNAME)));
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

    @Override
    protected String activateTask() {
        String parameters[];
        if (this.isOverCurrentMission()) {
            return Status.CLOSEMISSION.name();
        }
        setTask();
        parameters = taskName.split(" ");
        if (parameters[0].equals("MOVEIN")) {
            outbox = session.createReply();
            outbox.setContent("Request course into " + parameters[1] + " Session " + sessionKey);
            toSessionManager(outbox);
            session = this.fromSessionManager();
            if (!session.getContent().toUpperCase().startsWith("FAILURE")) {
                E.setExternalPerceptions(session.getContent());
                Info("Successfully found a route in " + taskName);
                return Status.SOLVEMISSION.name();
            } else {
                Info("Failed to find a route to " + taskName);
                return Status.CHOOSEMISSION.name();
            }
        } else if (parameters[0].equals("MOVETO")) {
            outbox = session.createReply();
            outbox.setContent("Request course into " + Integer.parseInt(parameters[1]) + " "
                    + Integer.parseInt(parameters[2]) + " Session " + sessionKey);
            this.toSessionManager(outbox);
            session = this.fromSessionManager();
            if (!session.getContent().toUpperCase().startsWith("FAILURE")) {
                E.setExternalPerceptions(session.getContent());
                Info("Successfully found a rote to " + taskName);
                return Status.SOLVEMISSION.name();
            } else {
                Info("Failed to find a rote to " + taskName);
                return Status.CHOOSEMISSION.name();
            }
        } else {
            return Status.CHOOSEMISSION.name();
        }
    }

    public Status MySolveProblem() {
        // Analizar objetivo
        if (G(E)) {
            Info("Target reached");
            if (isOverCurrentMission()) {
                Info("The problem is over");
                if (onMission) {
                    outbox = msgMission.createReply();
                    outbox.setPerformative(ACLMessage.INFORM);
                    outbox.setContent("INFORM OK");
                    this.LARVAsend(outbox);
                }
                return filterStatus(Status.PARKING.name());
            }
        }
        Choice a = Ag(E, A);
//        System.out.println("Alternatives " + A.toString());
        if (a == null) {
            Alert("Found no action to execute");
            return filterStatus(Status.CHECKOUT.name());
        } else {// Execute
//            Info("Excuting " + a);
//            System.out.println("Excuting " + a);
            if (!Ve(E)) {
                this.Error("The agent is not alive: " + E.getStatus());
                return filterStatus(Status.CHECKOUT.name());
            }
            if (!this.MyExecuteAction(a.getName())) {
                return filterStatus(Status.EXIT.name());
            }
            if (!this.MyReadPerceptions()) {
                return filterStatus(Status.EXIT.name());
            }
            return filterStatus(Status.SOLVEMISSION.name());
        }
    }

    protected String chooseMission() {
        do {
            nextCity = cities[(int) (Math.random() * cities.length)];
        } while (nextCity.equals(currentCity));
        missionName = "MOVEIN " + nextCity;
        defMission(missionName);
        return missionName;
    }

    protected void defMission(String mission) {
        Missions.clear();
        Missions.addMission(mission);
        Missions.addTask(mission, mission);
    }

    protected void defMission(String mission, String tasks[]) {
        Missions.clear();
        Missions.addMission(mission);
        for (String task : tasks) {
            Missions.addTask(mission, task);
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
            return filterStatus(Status.EXIT.name());
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
        outbox.setContent("Request join session " + sessionKey + " in " + baseCity);
        this.toSessionManager(outbox);
        session = this.fromSessionManager();
        if (!session.getContent().toUpperCase().startsWith("CONFIRM")) {
            Error("Could not join session " + sessionKey + " due to " + session.getContent());
            return Status.CHECKOUT;
        }
        if (!this.MyReadPerceptions()) {
            return filterStatus(Status.EXIT.name());
        }
        gpsBase = E.getGPS();
        return Status.CHOOSEMISSION;
    }

    boolean doQueryCities() {
        Info("Querying CITIES");
        outbox = new ACLMessage();
        outbox.setSender(this.getAID());;
        outbox.addReceiver(new AID(sessionManager, AID.ISLOCALNAME));
        outbox.setContent("Query CITIES session " + sessionKey);
        this.toSessionManager(outbox);
        session = fromSessionManager();
        if (session.getContent().toUpperCase().startsWith("FAILURE")) {
            return false;
        } else {
            E.setExternalPerceptions(session.getContent());
            return true;
        }
    }

    protected void toSessionManager(ACLMessage msg) {
        if (missionName == null || missionName.length() == 0) {
            msg.setReplyWith("NOMISSION");
        } else {
            msg.setReplyWith(missionName);
        }
        counterSessionManager++;
        this.LARVAsend(msg);
    }

    protected ACLMessage fromSessionManager() {
        counterSessionManager--;
        return this.LARVAblockingReceive(MessageTemplate.MatchSender(new AID(sessionManager, AID.ISLOCALNAME)));
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

    public void onMission(ACLMessage msg, String mission) {
        onMission = true;
        changeMission = true;
        msgMission = msg;
        myMission = mission;
        defMission(myMission);
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

}
