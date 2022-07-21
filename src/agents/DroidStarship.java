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
public class DroidStarship extends LARVAFirstAgent {

    protected enum Status {
        START, CHECKIN, CHECKOUT, JOINSESSION, CHOOSEMISSION, OPENMISSION, SOLVEMISSION, CLOSEMISSION, EXIT, WAIT, PARKING
    }
    protected Status myStatus;
    protected String service = "PMANAGER", problem = "",
            problemManager = "", content, sessionKey, sessionManager, gobackHome = "GOBACKHOME";
    protected String problems[], plan[], actions[];
    protected ACLMessage open, session;
    protected String[] contentTokens, cities;
    protected String action = "", preplan = "", baseCity, currentCity, nextCity, myMission;
    protected Point3D gpsBase;
    protected int indexplan = 0, myEnergy = 0;
    protected boolean showPerceptions, onMission, changeMission;

    String whichWall, nextWhichwall;
    double distance, nextdistance;
    protected ACLMessage msgMission;

    @Override
    public void setup() {
        super.setup();
        logger.onTabular();
        logger.onEcho();
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
        this.saveSequenceDiagram("./" + getLocalName() + ".seqd");
        this.closeRemote();
        super.takeDown();
    }

    public Status MyCheckin() {
        Info("Loading passport and checking-in to LARVA");
//        String mytruepassport = this.mypassport;
        OlePassport op = new OlePassport();
//        op.set(mytruepassport);
//        userName=op.getName();
        op.setPassport("TlBDIEFnZW50cw== ZXlKUFRFVk5SVlJCSWpwN0ltbGtJam9pVHpJNWRXNWlOblZxVTNCbVp6TlVSaUlzSW5SNWNHVWlPaUpQVEVWUVFWTlRVRTlTVkNJc0ltWnBaV3hrY3lJNmV5SnlZWGRRWVhOemNHOXlkQ0k2SWlJc0luVnpaWEpKUkNJNklpSXNJbU5wWkNJNklpSXNJbUZzYVdGeklqb2lJaXdpWlcxaGFXd2lPaUlpTENKdVlXMWxJam9pSW4wc0ltUmhkR1VpT2lJeU1ESXlMVEEyTFRJeUlERTFPalE1T2pNMU9qVXlNeUlzSW1SbGMyTnlhWEIwYVc5dUlqb2lTbE5QVGlCUFltcGxZM1FnVEdsdWEyVmtJR0Z1WkNCRmJXSmxaR1ZrSWl3aWIyeGxJanAwY25WbExDSmpjbmx3ZEc4aU9pSWlmU3dpZFhObGNrbEVJam94TURBd0xDSmphV1FpT2lJaUxDSmhiR2xoY3lJNklrbGpaVzFoYmlJc0ltVnRZV2xzSWpvaWJDNWpZWE4wYVd4c2IwQmtaV056WVdrdWRXZHlMbVZ6SWl3aWJtRnRaU0k2SWs1UVF5QkJaMlZ1ZEhNaWZRPT0=");
        //this.loadMyPassport("config/ANATOLI_GRISHENKO.passport");
        if (!doLARVACheckin()) {
            Error("Unable to checkin");
            return Status.EXIT;
        }

        return Status.JOINSESSION;
    }

    public Status MyCheckout() {
        this.doLARVACheckout();
        return Status.EXIT;
    }

    public Status MyChooseMission() {
        myMission=chooseMission();
        return filterStatus(Status.OPENMISSION.name());
    }

    public Status MyOpenMission() {
        nextWhichwall = whichWall = "NONE";
        nextdistance = distance = Choice.MAX_UTILITY;
        this.MyReadPerceptions();
        return filterStatus(activateMission(myMission));
    }

    public Status MyCloseMission() {
        return filterStatus(Status.CHOOSEMISSION.name());
    }

    public Status MyWait() {
//        this.nextTask("TO_RELEASE");
// Message("My Wait to be released");
//        this.MyReadPerceptions();
        inbox = LARVAblockingReceive(5000);
        if (inbox == null) {
            this.offMission();
        }
        // Message("Released");
       return filterStatus(Status.CHOOSEMISSION.name());
    }

    public Status MyParking() {
        int time = 3000 + (int) (Math.random() * 3000);
//        this.nextTask("PARKING");
//        this.MyReadPerceptions();
        LARVAblockingReceive(time);
        // Message("Released");
        return filterStatus(Status.CHOOSEMISSION.name());
    }

    protected boolean MyExecuteAction(String action) {
        Info("Executing action " + action);
        outbox = session.createReply();
        outbox.setContent("Request execute " + action + " session " + sessionKey);
        toSessionManager(outbox);
        this.myEnergy++;
        inbox = this.LARVAblockingReceive(2000);
        if (inbox != null) {
            session = inbox;
        }
        return true;
    }

    protected boolean MyReadPerceptions() {
        Info("Reading perceptions");
        outbox = session.createReply();
        outbox.setContent("Query sensors session " + sessionKey);
        toSessionManager(outbox);
        this.myEnergy++;
        inbox = this.LARVAblockingReceive(2000); //this.LARVAblockingReceive(MessageTemplate.MatchSender(new AID(sessionManager, AID.ISLOCALNAME)));
        if (inbox != null) {
            Info("Read perceptions ok");
            session = inbox;
            getEnvironment().setExternalPerceptions(session.getContent());
        } else {
            Info("Failed to read perceptions");
        }
        return true;
    }

    public String myMethod() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    @Override
    protected String activateNextTask() {
        String parameters[];
        if (this.isOverCurrentMission()) {
            return Status.CLOSEMISSION.name();
        }
        nextTask();
        parameters = taskName.split(" ");
        if (parameters[0].equals("MOVEIN")) {
            outbox = session.createReply();
            outbox.setContent("Request course into " + parameters[1] + " Session " + sessionKey);
            this.LARVAsend(outbox);
            inbox= this.LARVAblockingReceive(2000);
            if (inbox == null)
                return Status.CHOOSEMISSION.name();
            session = inbox;
            if (!session.getContent().toUpperCase().startsWith("FAILURE")) {
                E.setExternalPerceptions(session.getContent());
                Info("Successfully found a rote in " + taskName);
                return Status.SOLVEMISSION.name();
            } else {
                Info("Failed to find a rote to " + taskName);
                return Status.CHOOSEMISSION.name();
            }
        } else if (parameters[0].equals("MOVETO")) {
            outbox = session.createReply();
            outbox.setContent("Request course into " + Integer.parseInt(parameters[1]) + " "
                    + Integer.parseInt(parameters[2]) + " Session " + sessionKey);
            this.LARVAsend(outbox);
            inbox= this.LARVAblockingReceive(2000);
            if (inbox == null)
                return Status.CHOOSEMISSION.name();
            session = inbox;
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
                    // Message("Done");
                    this.LARVAsend(outbox);
                    return filterStatus(Status.WAIT.name());
                } else {
                    return filterStatus(Status.PARKING.name());
                }
            } else {
                Info("Moving on to the next tasl");
                nextWhichwall = whichWall = "NONE";
                nextdistance = distance = Choice.MAX_UTILITY;
                return filterStatus(activateNextTask());
            }
        }
        Choice a = Ag(E, A);
            System.out.println("Alternatives " + A.toString());
        if (a == null) {
            Alert("Found no action to execute");
            return filterStatus(Status.CHECKOUT.name());
        } else {// Execute
            Info("Excuting " + a);
//            System.out.println("Excuting " + a);
            if (!Ve(E)) {
                this.Error("The agent is not alive: " + E.getStatus());
                return filterStatus(Status.CHECKOUT.name());
            }
            this.MyExecuteAction(a.getName());
            this.MyReadPerceptions();
            return filterStatus(myStatus.name());
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
        this.doQueryCities();
        cities = E.getCityList();
        if (cities.length == 0) {
            Error("Sorry this agent can only join worlds with cities");
            return Status.CHECKOUT;
        }
        baseCity = cities[(int) (Math.random() * cities.length)];
        currentCity = baseCity;
        Info("Joining session with base in  " + baseCity);
//        outbox = new ACLMessage();
//        outbox.setSender(this.getAID());;
//        outbox.addReceiver(new AID(sessionManager, AID.ISLOCALNAME));
        outbox = session.createReply();
        outbox.setContent("Request join session " + sessionKey + " in " + baseCity);
        this.LARVAsend(outbox);
        session = this.LARVAblockingReceive();
        if (!session.getContent().startsWith("Confirm")) {
            Error("Could not join session " + sessionKey + " due to " + session.getContent());
            return Status.CHECKOUT;
        }
        gpsBase = E.getGPS();
        return Status.CHOOSEMISSION;
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

    Status doQueryCities() {
        Info("Querying CITIES");
        outbox = new ACLMessage();
        outbox.setSender(this.getAID());;
        outbox.addReceiver(new AID(sessionManager, AID.ISLOCALNAME));
        outbox.setContent("Query CITIES session " + sessionKey);
        this.LARVAsend(outbox);
        session = LARVAblockingReceive();
        E.setExternalPerceptions(session.getContent());
        return myStatus;
    }

    @Override
    public ACLMessage LARVAblockingReceive() {
        return LARVAblockingReceive(-1);
    }

    @Override
    public ACLMessage LARVAblockingReceive(long milis) {
        TimeHandler tini, tend;
        ACLMessage res;
        boolean exit = false;
        tini = new TimeHandler();
        while (true) {
            if (milis < 0) {
                res = super.LARVAblockingReceive();
            } else {
                res = super.LARVAblockingReceive(milis);
            }
            if (milis > 0 && res == null) {
                return null;
            } else {
                if (res.getSender().getLocalName().equals(sessionManager)) {
                    return res;
                } else {
                    contentTokens = res.getContent().split(" ");
                    if (myStatus == Status.WAIT) {
                        if (contentTokens[0].toUpperCase().equals("CANCEL")) {
                            onMission = false;
                            return res;
                        }
                    } else if (!onMission) {
                        if (contentTokens[0].toUpperCase().equals("CFP")) {
                            if (contentTokens[1].equals("BYDISTANCE")) {
                                String city = contentTokens[2];
                                Point3D citypos = E.getCityPosition(city), mypos = E.getGPS();
                                outbox = res.createReply();
                                outbox.setContent("Propose " + citypos.planeDistanceTo(mypos));
                                this.LARVAsend(outbox);
                                continue;
                            } else if (contentTokens[1].equals("BYCARGO")) {
                                outbox = res.createReply();
                                outbox.setContent("Propose " + E.getMaxcargo());
                                this.LARVAsend(outbox);
                                continue;
                            }
                        }
                        if (contentTokens[0].toUpperCase().equals("ACCEPT")) {
                            if (contentTokens[1].toUpperCase().equals("MOVEIN")) {
                                onMission(res, "MOVEIN " + contentTokens[2]);
                                myStatus = Status.OPENMISSION;
                                outbox = msgMission.createReply();
                                outbox.setContent("Agree");
                                this.LARVAsend(outbox);
                                if (myStatus == Status.PARKING) {
                                    return null;
                                }
                                continue;
                            }
                        }
                    }
                }
            }
            outbox = res.createReply();
            outbox.setContent("Refuse");
            this.LARVAsend(res);
        }
    }

    protected void toSessionManager(ACLMessage msg) {
        msg.setReplyWith(missionName);
        this.LARVAsend(msg);
    }

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
}

//    public double goTakeOff(Environment E, Choice a) {
//        if (a.getName().equals("UP")) {
//            a.setAnnotation(this.myMethod());
//            return Choice.ANY_VALUE;
//        }
//        return Choice.MAX_UTILITY;
//    }
//
//    public double goLanding(Environment E, Choice a) {
//        if (a.getName().equals("DOWN")) {
//            a.setAnnotation(this.myMethod());
//            return Choice.ANY_VALUE;
//        }
//        return Choice.MAX_UTILITY;
//    }
//
//    protected double goAhead(Environment E, Choice a) {
//        if (a.getName().equals("MOVE")) {
//            a.setAnnotation(this.myMethod());
//            return U(S(E, a));
//        } else if (a.getName().equals("LEFT") || a.getName().equals("RIGHT")) {
//            a.setAnnotation(this.myMethod());
//            return U(S(E, a), new Choice("MOVE"));
//        }
//        return Choice.MAX_UTILITY;
//
//    }
//
//    @Override
//    protected Choice Ag(Environment E, DecisionSet A) {
//        if (G(E)) {
//            return null;
//        } else if (A.isEmpty()) {
//            return null;
//        } else {
//            A = Prioritize(E, A);
//            whichWall = nextWhichwall;
//            distance = nextdistance;
//            return A.BestChoice();
//        }
//    }
//
//    public double goKeepOnWall(Environment E, Choice a) {
//        if (a.getName().equals("MOVE")) {
//            a.setAnnotation(this.myMethod());
//            return Choice.ANY_VALUE;
//        }
//        return Choice.MAX_UTILITY;
//    }
//
//    public double goTurnOnWallLeft(Environment E, Choice a) {
//        if (a.getName().equals("LEFT")) {
//            a.setAnnotation(this.myMethod());
//            return Choice.ANY_VALUE;
//        }
//        return Choice.MAX_UTILITY;
//
//    }
//
//    public double goRevolveWallLeft(Environment E, Choice a) {
//        if (a.getName().equals("RIGHT")) {
//            a.setAnnotation(this.myMethod());
//            return Choice.ANY_VALUE;
//        }
//        return Choice.MAX_UTILITY;
//    }
//
//    public double goStopWallLeft(Environment E, Choice a) {
//        if (a.getName().equals("RIGHT")) {
//            nextWhichwall = "NONE";
//            distance = Integer.MAX_VALUE;
//            a.setAnnotation(this.myMethod());
//            return Choice.ANY_VALUE;
//        }
//        return Choice.MAX_UTILITY;
//    }
//
//    public double goFollowWallLeft(Environment E, Choice a) {
//        if (E.isFreeFrontLeft()) {
//            return goTurnOnWallLeft(E, a);
//        } else if (E.isTargetFrontRight()
//                && E.isFreeFrontRight()
//                && E.getDistance() < distance) {
//            return goStopWallLeft(E, a);
//        } else if (E.isFreeFront()) {
//            return goKeepOnWall(E, a);
//        } else {
//            return goRevolveWallLeft(E, a);
//        }
//    }
//
//    public double goAvoid(Environment E, Choice a) {
//        if (E.isTargetLeft()) {
//            if (a.getName().equals("LEFT")) {
//                nextWhichwall = "RIGHT";
//                nextdistance = E.getDistance();
//                a.setAnnotation(this.myMethod());
//
//                return Choice.ANY_VALUE;
//            }
//        } else if (E.isTargetRight()) {
//            if (a.getName().equals("RIGHT")) {
//                nextWhichwall = "LEFT";
//                nextdistance = E.getDistance();
//                a.setAnnotation(this.myMethod());
//
//                return Choice.ANY_VALUE;
//            }
//        }
//        return Choice.MAX_UTILITY;
//
//    }
//
//    public double goTurnOnWallRight(Environment E, Choice a) {
//        if (a.getName().equals("RIGHT")) {
//            a.setAnnotation(this.myMethod());
//
//            return Choice.ANY_VALUE;
//        }
//        return Choice.MAX_UTILITY;
//
//    }
//
//    public double goStopWallRight(Environment E, Choice a) {
//        if (a.getName().equals("LEFT")) {
//            nextWhichwall = "NONE";
//            distance = Integer.MAX_VALUE;
//            a.setAnnotation(this.myMethod());
//
//            return Choice.ANY_VALUE;
//        }
//        return Choice.MAX_UTILITY;
//    }
//
//    public double goRevolveWallRight(Environment E, Choice a) {
//        if (a.getName().equals("LEFT")) {
//            a.setAnnotation(this.myMethod());
//
//            return Choice.ANY_VALUE;
//        }
//        return Choice.MAX_UTILITY;
//    }
//
//    public double goFollowWallRight(Environment E, Choice a) {
//        if (E.isTargetLeft()
//                && E.isFreeFrontLeft()
//                && E.getDistance() < distance) {
//            return goStopWallRight(E, a);
//        } else if (E.isFreeFrontRight()) {
//            return goTurnOnWallRight(E, a);
//        } else if (E.isFreeFront()) {
//            return goKeepOnWall(E, a);
//        } else {
//            return goRevolveWallRight(E, a);
//        }
//    }
//
//    public double goFollowWall(Environment E, Choice a) {
//        if (whichWall.equals("LEFT")) {
//            return goFollowWallLeft(E, a);
//        } else if (whichWall.equals("RIGHT")) {
//            return goFollowWallRight(E, a);
//        }
//        return Choice.MAX_UTILITY;
//    }
//
//    protected double goTurnBack(Environment E, Choice a) {
//        if (E.isTargetLeft()) {
//            if (a.getName().equals("LEFT")) {
//                a.setAnnotation(this.myMethod());
//
//                return Choice.ANY_VALUE;
//            }
//        } else if (E.isTargetRight()) {
//            if (a.getName().equals("RIGHT")) {
//                a.setAnnotation(this.myMethod());
//
//                return Choice.ANY_VALUE;
//            }
//        }
//        return Choice.MAX_UTILITY;
//    }
//
//    protected double goLowEnergy(Environment E, Choice a) {
//        if (E.getGround() > 0) {
//            return goLanding(E, a);
//        } else if (a.getName().equals("RECHARGE")) {
//            return Choice.ANY_VALUE;
//        }
//        return Choice.MAX_UTILITY;
//    }
//
//    @Override
//    protected double U(Environment E, Choice a) {
//        if (E.getEnergy() * 100 / E.getAutonomy() < 15) {
//            return goLowEnergy(E, a);
//        }
//        if (E.getDistance() > 0
//                //                                && E.getGPS().getZ() < E.getMaxlevel()) {
//                && E.getGPS().getZ() < Math.min(E.getVisualFront() + 15, E.getMaxlevel())) {
//            return goTakeOff(E, a);
//        } else if (E.getDistance() == 0 && E.getGround() > 0) {
//            return goLanding(E, a);
//        } else if (!whichWall.equals("NONE")) {
//            return goFollowWall(E, a);
//        } else if (E.isTargetBack()) {
//            return goTurnBack(E, a);
//        } else if (!E.isFreeFront()) {
//            return goAvoid(E, a);
//        } else {
//            return goAhead(E, a);
//        }
//    }
