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
import ai.Plan;
import static crypto.Keygen.getHexaKey;
import data.OlePassport;
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
public class DroidStarshipLevelA extends LARVAFirstAgent {

    protected enum Status {
        START, CHECKIN, CHECKOUT, JOINSESSION, CHOOSEMISSION, OPENMISSION, SOLVEMISSION, CLOSEMISSION, EXIT, WAIT, PARKING
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

    protected Plan AgPlan(Environment E, DecisionSet A) {
        Plan result;
        Ei = E.clone();
        Plan p = new Plan();
        for (int i = 0; i < Ei.getRange() / 2 - 2; i++) {
            Ei.cache();
            if (!Ve(Ei)) {
                return null;
            } else if (G(Ei)) {
                return p;
            } else {
                a = super.Ag(Ei, A);
                if (a != null) {
                    p.add(a);
                    Ef = S(Ei, a);
                    Ei = Ef;
                } else {
                    return null;
                }
            }
        }
        return p;
    }

    @Override
    public void setup() {
        super.setup();
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
        myText = null;
    }

    @Override
    public void Execute() {
        if (reaction != null) {
            myStatus = reaction;
            reaction = null;
            Info("Reacting to " + this.getCurrentTask());
            Info("Status: " + myStatus.name() + "(" + taskName + ")");
        } else {
            Info("Status: " + myStatus.name());

        }
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
        return Status.EXIT;
    }

    public Status MyCheckin() {
        boolean cin;
        String fakepassport = "TlBDIEFnZW50cw== ZXlKUFRFVk5SVlJCSWpwN0ltbGtJam9pVHpJNWRXNWlOblZxVTNCbVp6TlVSaUlzSW5SNWNHVWlPaUpQVEVWUVFWTlRVRTlTVkNJc0ltWnBaV3hrY3lJNmV5SnlZWGRRWVhOemNHOXlkQ0k2SWlJc0luVnpaWEpKUkNJNklpSXNJbU5wWkNJNklpSXNJbUZzYVdGeklqb2lJaXdpWlcxaGFXd2lPaUlpTENKdVlXMWxJam9pSW4wc0ltUmhkR1VpT2lJeU1ESXlMVEEyTFRJeUlERTFPalE1T2pNMU9qVXlNeUlzSW1SbGMyTnlhWEIwYVc5dUlqb2lTbE5QVGlCUFltcGxZM1FnVEdsdWEyVmtJR0Z1WkNCRmJXSmxaR1ZrSWl3aWIyeGxJanAwY25WbExDSmpjbmx3ZEc4aU9pSWlmU3dpZFhObGNrbEVJam94TURBd0xDSmphV1FpT2lJaUxDSmhiR2xoY3lJNklrbGpaVzFoYmlJc0ltVnRZV2xzSWpvaWJDNWpZWE4wYVd4c2IwQmtaV056WVdrdWRXZHlMbVZ6SWl3aWJtRnRaU0k2SWs1UVF5QkJaMlZ1ZEhNaWZRPT0=",
                realpassport = mypassport;
        if (this.doLARVACheckin()) {
            doLARVACheckout();
        }
        Info("Checking-in to LARVA");
        ACLMessage outbox = new ACLMessage(ACLMessage.SUBSCRIBE);
        AID IM = new AID(IdentityManager, AID.ISLOCALNAME);
        outbox.setSender(getAID());
        outbox.addReceiver(IM);
        outbox.setContent(fakepassport);
        this.LARVAsend(outbox);
        checkin = this.LARVAblockingReceive(MessageTemplate.MatchSender(IM), WAITANSWERMS);
        if (checkin == null) {
            Alert("Agent " + IdentityManager + " does not answer. Not checked in");
            return Status.EXIT;
        } else {
            checkout = checkin.createReply();
            if (checkin.getPerformative() == ACLMessage.CONFIRM) {
                checkedin = true;
                Info(checkin.getContent());
            } else if (checkin.getPerformative() == ACLMessage.REFUSE) {
                Alert("Checkin at LARVA refused.\nDetails: " + checkin.getContent());
                return Status.EXIT;
            } else {
                Alert("Could not checkin at LARVA.\nDetails: " + checkin.getContent());
                return Status.EXIT;
            }
        }
        return Status.JOINSESSION;
    }

    public Status MyCheckout() {
        this.doLARVACheckout();
        return Status.EXIT;
    }

    public Status MyChooseMission() {
        myMission = chooseMission();
        Info("Selected mission: " + myMission);
        return Status.OPENMISSION;
    }

    public Status MyOpenMission() {
        nextWhichwall = whichWall = "NONE";
        nextdistance = distance = Choice.MAX_UTILITY;
        Info("Activating mission " + myMission);
        return filterStatus(activateMission(myMission));
    }

    public Status MyCloseMission() {
        return Status.CHOOSEMISSION;
    }

    public Status MyParking() {
        if (!allowParking) {
            return Status.CHOOSEMISSION;
        }
        int time = PARKINGTIME + (int) (Math.random() * PARKINGTIME);
        boolean exit = false;
        TimeHandler tini, tend;
        long remaining;
        tini = new TimeHandler();
        tend = new TimeHandler();
        while (!exit) {
            this.setTaskName("PARKING " + (time - tini.elapsedTimeSecsUntil(tend)));
            this.MyReadPerceptions();
            inbox = this.LARVAblockingReceive(500);
            if (this.isOnMission()) {
                Info("Skip to mission mission " + this.getCurrentTask());
                return Status.OPENMISSION;
            }
            tend = new TimeHandler();
            exit = tini.elapsedTimeSecsUntil(tend) >= time;
        }
        return Status.CHOOSEMISSION;
    }

    protected boolean MyExecuteAction(String action) {
        Info("Executing action " + action);
        outbox = session.createReply();
        outbox.setContent("Request execute " + action + " session " + sessionKey);
        LARVAsend(outbox);
        this.myEnergy++;
        session = LARVAblockingReceive();
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
        LARVAsend(outbox);
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
    protected String activateNextTask() {
        String parameters[];
        if (this.isOverCurrentMission()) {
            return Status.CLOSEMISSION.name();
        }
        nextTask();
        if (isOnMission()) {
            taskName = getCurrentTask() + " (" + msgMission.getSender().getLocalName() + ")";
            setTaskName(taskName);
        }
        parameters = taskName.split(" ");
        if (parameters[0].equals("MOVEIN")) {
            if (parameters[1].equals(getEnvironment().getCurrentCity())) {
                return Status.SOLVEMISSION.name();
            }
            outbox = session.createReply();
            outbox.setContent("Request course into " + parameters[1] + " Session " + sessionKey);
            LARVAsend(outbox);
            session = LARVAblockingReceive();
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
            LARVAsend(outbox);
            session = LARVAblockingReceive();
            if (!session.getContent().toUpperCase().startsWith("FAILURE")) {
                E.setExternalPerceptions(session.getContent());
                Info("Successfully found a rote to " + taskName);
                return Status.SOLVEMISSION.name();
            } else {
                Info("Failed to find a rote to " + taskName);
                return Status.CHOOSEMISSION.name();
            }
        } else if (parameters[0].equals("EXIT")) {
            return Status.CHECKOUT.name();
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
                return Status.PARKING;
            }
        }
        Choice a = Ag(E, A);
//        System.out.println("Alternatives " + A.toString());
        if (a == null) {
            Alert("Found no action to execute");
            return Status.CHECKOUT;
        } else {// Execute
//            Info("Excuting " + a);
//            System.out.println("Excuting " + a);
            if (!Ve(E)) {
                this.Error("The agent is not alive: " + E.getStatus());
                return Status.CHECKOUT;
            }
            if (!this.MyExecuteAction(a.getName())) {
                return Status.EXIT;
            }
            if (!this.MyReadPerceptions()) {
                return Status.EXIT;
            }
            return Status.SOLVEMISSION;
        }

//        behaviour = AgPlan(E, A);
//        if (behaviour == null || behaviour.isEmpty()) {
//            Alert("Found no plan to execute");
//            return Status.CHECKOUT;
//        } else {// Execute
//            Info("Found plan: " + behaviour.toString());
//            while (!behaviour.isEmpty()) {
//                a = behaviour.get(0);
//                behaviour.remove(0);
//                Info("Excuting " + a);
//                this.MyExecuteAction(a.getName());
//                if (!Ve(E)) {
//                    this.Error("The agent is not alive: " + E.getStatus());
//                    return Status.CHECKOUT;
//                }
//            }
//            this.MyReadPerceptions();
//            return Status.SOLVEMISSION;
//        }
    }

    protected String chooseMission() {
        do {
            nextCity = cities[(int) (Math.random() * cities.length)];
        } while (nextCity.equals(currentCity));
        missionName = "MOVEIN " + nextCity;
        defMission(missionName);
        return missionName;
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
        if (cities.length == 0) {
            Error("Sorry this agent can only join worlds with cities");
            return Status.CHECKOUT;
        }
        baseCity = cities[(int) (Math.random() * cities.length)];
        currentCity = baseCity;
        Info("Joining session with base in  " + baseCity);
        outbox = session.createReply();
        outbox.setContent("Request join session " + sessionKey + " in " + baseCity);
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
        return Status.CHOOSEMISSION;
    }

    boolean doQueryCities() {
        Info("Querying CITIES");
        outbox = new ACLMessage();
        outbox.setSender(this.getAID());;
        outbox.addReceiver(new AID(sessionManager, AID.ISLOCALNAME));
        outbox.setContent("Query CITIES session " + sessionKey);
        LARVAsend(outbox);
        session = LARVAblockingReceive();
        if (session.getContent().toUpperCase().startsWith("FAILURE")) {
            return false;
        } else {
            E.setExternalPerceptions(session.getContent());
            return true;
        }
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
        defMission(mission);
        myMission = mission;
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

    protected boolean isUnexpected(ACLMessage msg) {
        return !msg.getSender().getLocalName().equals(sessionManager);
    }

    @Override
    protected void LARVAsend(ACLMessage msg) {
        rw = getHexaKey(16);
        msg.setReplyWith(rw);
        super.LARVAsend(msg);
    }

    @Override
    protected ACLMessage LARVAblockingReceive() {
        ACLMessage res;
        while (true) {
            res = super.LARVAblockingReceive();
            if (isUnexpected(res)) {
                this.processUnexpectedMessage(res);
            } else {
                return res;
            }
        }
    }

    @Override
    protected ACLMessage LARVAblockingReceive(long milis) {
        ACLMessage res;
        TimeHandler tini = new TimeHandler(), tend = tini;
        while (tini.elapsedTimeMilisecsUntil(tend) < milis) {
            res = super.LARVAblockingReceive(milis);
            if (res != null) {
                if (isUnexpected(res)) {
                    this.processUnexpectedMessage(res);
                } else {
                    return res;
                }
            }
            tend = new TimeHandler();
        }
        return null;
    }

    protected void processUnexpectedMessage(ACLMessage msg) {
        logger.onEcho();
        String tokens[] = msg.getContent().split(",")[0].split(" ");
        Info("Unexpected " + msg.getContent());
        if (msg.getContent().toUpperCase().equals("TRANSPOND")) {
            outbox = msg.createReply();
            String answer = "";
            answer += "NAME " + getLocalName() + "TYPE, " + E.getType() + ",CITY " + getEnvironment().getCurrentCity();
            answer += "GPS" + E.getGPS().toString() + ",COURSE "+SimpleVector3D.Dir[E.getGPSVector().getsOrient()]+", PAYLOAD " + E.getPayload();
            LARVAsend(outbox);
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
                    onMission(msg, tokens[2] + " " + tokens[3]);
                    Info("new task " + this.getCurrentTask());
                    reaction = Status.OPENMISSION;
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
                    String newMission = tokens[2] + " " + tokens[3];
                    onMission(msg, newMission);
                    Info("On mission " + newMission);
                    reaction = Status.OPENMISSION;
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
