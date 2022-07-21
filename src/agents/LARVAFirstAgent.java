/**
 * @file LARVAFirstAgent.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package agents;

import Environment.Environment;
import ai.Choice;
import ai.DecisionSet;
import ai.MissionSet;
import appboot.XUITTY;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import console.Console;
import crypto.Keygen;
import data.Ole;
import data.OleConfig;
import data.OleSet;
import data.Transform;
import disk.Logger;
import static disk.Logger.trimFullString;
import glossary.Signals;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import messaging.ACLMessageTools;
import messaging.SequenceDiagram;
import swing.OleAgentTile;
import swing.OleApplication;
import swing.OleButton;
import swing.OleToolBar;
import tools.emojis;

/**
 * This is the basic agent in LARVA. It extends a Jade Agent with an API of
 * abstracted services with enhanced functionality or simplified operation
 * regarding the core of Jade. These are the most important services
 * <p>
 * <ul>
 * <li> Simplified access to AMS
 * <li> Simplified access to DF with a pool of service agents and a pool of
 * services provided.
 * <li> Logging capabilitiy both on screen and on disk with deactivable echo on
 * screen. All the notifications are annotated with a timestamp and the own name
 * of the agent in order to identify clearly who, when and what. It also
 * differentiate three levels of information. For more information
 * {@link Logger}
 * <ul>
 * <li> General information
 * <li> Error information
 * <li> MinorException handling
 * </ul>
 * <li> Thanks to the use of OlePassport, it offers automatic operation to load,
 * store and transfer Passports. It supports encryption by the definition of an
 * appropriate instance of Cryptor
 * <li> Support for reading, writing and transmission of any file, of any type
 * and size.
 * <li> Provides a basic behaviour, which has to be acvivated nevertheless, in
 * order to start working without any background on Jade behaviours. This is a
 * repeatable behaviour (Execute()) which acts as the main body of most agents
 * and an associated boolean variable to control the LARVAexit and, therefore,
 * the death of the agent.
 * </ul>
 *
 */
public class LARVAFirstAgent extends LARVABaseAgent implements ActionListener {

    // JFrame from launcher
    protected OleApplication myApp;
    protected JPanel myPane, myMap;
    protected JScrollPane myScrPane;
    protected JTextArea myText;
//    protected SensorDecoder Deco;
    protected ACLMessage checkin, checkout;
    protected String IdentityManager;

    protected int userID = -1;
    protected String userName = "";

    // Its known sequence diagram
    protected static SequenceDiagram sd;
    //
    protected String title, mySessionmanager = "", problemName;

    protected OleSet stepsDone, stepsSent;
    protected boolean traceRunSteps = false;
    protected OleConfig oleConfig;
    protected AgentReport myReport;
    protected LARVAPayload payload;

    protected Environment E;
    protected DecisionSet A;

    protected Semaphore SWaitButtons;
    protected boolean cont = true, each = true, remote = false;
    OleAgentTile externalTile;
    OleToolBar externalTB;
    OleButton olbContinue, olbPause, olbNext, olbUntil;
    protected int nUntil, iUntil = 0, frameDelay = 0;
    protected boolean showConsole = false, showRemote = false;
    protected XUITTY xuitty;

    protected MissionSet Missions;
    protected int iTask, nTasks;
    protected String myMission[], missionName, taskName, sessionAlias = "";

    protected boolean securedMessages;
    protected String lastSentMsg = "", lastRecMsg = "", lastSentACLMID = "";
    protected int nlastSentMsg = 0, nlastSentACLMID = 0, nlastRecMsg = 0, nrecErrors = 0;
    protected final String ACLMTAG = "ACLMID";
    protected List<String> errortags = Stream.of("FAILURE", "REFUSE", "NOT-UNDERSTOOD", "BAD ", "ERROR").collect(Collectors.toList());

    protected Choice Ag(Environment E, DecisionSet A) {
        if (G(E)) {
            return null;
        } else if (A.isEmpty()) {
            return null;
        } else {
            A = Prioritize(E, A);
            return A.BestChoice();
        }
    }

    protected DecisionSet Prioritize(Environment E, DecisionSet A) {
        for (Choice a : A) {
            a.setUtility(U(E, a));
        }
        A.sort();
        return A;
    }

    protected Environment S(Environment E, Choice a) {
        if (!Ve(E)) {
            return null;
        } else {
            return E.simmulate(a);
        }
    }

    protected double U(Environment E) {
        if (!Ve(E)) {
            return Choice.MAX_UTILITY;
        } else {
            return E.getDistance();
        }
    }

    protected double U(Environment E, Choice a) {
        return U(S(E, a));
    }

    protected boolean Ve(Environment E) {
        if (E == null || E.isCrahsed() || E.getStuck() > 3) {
            return false;
        }
        return true;

    }

    protected boolean G(Environment E) {
        return E.getOntarget();
    }

    protected boolean Va(Environment E, Choice a) {
        return true;
    }

    /**
     * Main JADE setup
     */
    @Override
    public void setup() {
        super.setup();
        Missions = new MissionSet();
        stepsDone = new OleSet();
        stepsSent = new OleSet();
        if (sd == null) {
            sd = new SequenceDiagram();
        }
        addRunStep("MILES00");
        addRunStep("MILES01");
        this.logger.setEcho(true);
        // create a new frame to store text field and button
        if (this.getArguments() != null && this.getArguments().length > 0) {
            payload = (LARVAPayload) this.getArguments()[0];
            myText = (JTextArea) payload.getGuiComponents().get("Activity log");
            myApp = payload.getParent();
            if (payload.getoPassport() != null) {
                this.mypassport = payload.getoPassport().getField("rawPassport");
            }
            if (payload.getOlecfg() != null) {
                oleConfig = payload.getOlecfg();
                if (oleConfig.getTab("Log activity") != null) {
                    logger.setEcho(!oleConfig.getTab("Log activity").getBoolean("Silent", false));
                    if (oleConfig.getTab("Log activity").getBoolean("Save log", false)) {
                        String logfile = oleConfig.getTab("Log activity").getString("File log", "./default.log");
                        logger.setLoggerFileName(logfile);
                    }
                }
            }
            if (payload.getMyReport() != null) {
                myReport = payload.getMyReport();
            } else {
                myReport = new AgentReport(getName(), this.getClass(), 100);
            }
            this.frameDelay = oleConfig.getTab("Display").getInt("Frame delay", -1);
            showConsole = this.oleConfig.getTab("Display").getBoolean("Show console");
            showRemote = this.oleConfig.getTab("Display").getBoolean("Show remote");
        } else {
            myReport = new AgentReport(getName(), this.getClass(), 100);
        }
        SWaitButtons = new Semaphore(0);
        E = new Environment();
        if (showRemote) {
            openRemote();
        }
        this.setSecuredMessages(true);
        doNotExit();
    }

    public void LARVAwait(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ex) {
        }

    }

    public void LARVAblock() {
        this.defaultBehaviour.block();
    }

    /**
     * This method encapsulates code of the agent, usually the most external
     * layer, and catches every possible exception, informs the teacher and
     * moves to emergency mode: that is, only the ADMIN channel is open. Froom
     * here the agent might go back to work if it receives the clearance from
     * the teacher. The goal is catching the un caught exceptions in the main
     * body and resume the activity in the main body loop or to simple die;
     *
     * @param r The piece of code to be safely executed.
     */
    public void doShield(Runnable r) {
        try {
            r.run();
        } catch (ExitRequestedException ex) {
            LARVAexit = true;
//                doDelete();
        } catch (Exception ex) {
            JsonObject res = logger.logException(ex);
            String message = "";
            message += emojis.CALENDAR + " " + res.getString("date", "") + "\n";
            res = res.get("record").asObject();
            message += emojis.ROBOT + " " + res.getString("agent", "") + "\n";
            message += emojis.WARNING + " UNCAUGHT EXCEPTION\n" + res.getString("uncaught-exception", "") + "\n";
            message += emojis.INFO + " INFO\n" + res.getString("info", title) + "\n";
            this.Alert(message);
            LARVAexit = true;
        }
    }

    public void doExit() {
        throw new ExitRequestedException(Signals.EXITREQUESTED.name(), new IOException());
    }

    public void doNotExit() {
        LARVAexit = false;
    }

    @Override
    protected void BehaviourDefaultSetup() {
        defaultBehaviour = new Behaviour() {
            @Override
            public void action() {
                doShield(() -> {
                    preExecute();
                    Execute();
                    postExecute();
                    ncycles++;
                });
                if (isExit()) {
                    doDelete();
                }
            }

            @Override
            public boolean done() {
                return LARVAexit;
            }

        };
        this.addBehaviour(defaultBehaviour);
    }

    @Override
    public void postExecute() {
        myReport.tick();
        if (this.frameDelay > 0 && (!remote || cont)) {
            LARVAwait(frameDelay);
        }
    }

    @Override
    public void preExecute() {
        waitRemoteSemaphore();
    }

    protected void waitRemoteSemaphore() {
        if (remote) {
            iUntil++;
            if (cont && nUntil > 0 && nUntil == iUntil) {
                cont = false;
            }
            if (!cont) {
                try {
                    this.SWaitButtons.acquire();
                } catch (Exception ex) {
                }
            }
        }

    }

    /**
     * It detects if the ouput is graphical (swing) or text (console)
     *
     * @return true when SWING is active, false otherwise
     */
    protected boolean isSwing() {
        return this.myApp != null;
    }

    /**
     * It allows a deep analysis of the changes of state of the agents that
     * inherit from this class. It produces a hihgh traffic of messages,
     * therefore, it must be used only upon teacher instruction.
     */
    protected void enableDeepLARVAMonitoring() {
        this.traceRunSteps = true;
    }

    /**
     * Only for use with external configuration files-
     *
     * @return The set of sensors configured in the external configuration file
     */
    protected String[] getConfiguredSensors(OleConfig ocfg) {
        String res[] = new String[0];
        ArrayList<String> sensorList = new ArrayList();
        if (ocfg != null && !ocfg.isEmpty()) {
            Ole sensors = ocfg.getTab("LARVA").getOle("Sensors");
            for (String sensor : sensors.getFieldList()) {
                if (sensors.getBoolean(sensor)) {
                    sensorList.add(sensor.toUpperCase());
                }
            }
            res = Transform.toArrayString(sensorList);
        }
        return res;
    }

    @Override
    public void takeDown() {
        addRunStep("MILES03");
        if (remote) {
            closeRemote();
        }
//        if (problemName != null) {
//            this.saveSequenceDiagram(problemName + ".seqd");
//        }
//        else {
//            this.saveSequenceDiagram(getName() + ".seqd");
//        }
        super.takeDown();
    }

    //
    // Console output
    //
    /**
     * Log an error message. It is sent of Stderr. When the echo is not active,
     * it does not show anything on screen.
     *
     * @param message The error message
     */
    @Override
    protected void Error(String message) {
        addRunStep("MILES02");
        logger.logError(message);
        if (isSwing()) {
            if (myText != null) {
                myText.append(logger.getLastlog());
                myText.setCaretPosition(Math.max(myText.getText().lastIndexOf("\n"), 0));
            }
            Alert(message);
        }
    }

    /**
     * Log a common message. It is sent of Stdout. When the echo is not active,
     * it does not show anything on screen.
     *
     * @param message The informative message
     */
    @Override
    protected void Info(String message) {
        addRunStep("MILES02");
        logger.logMessage(message);
        try {
            if (isSwing() && logger.isEcho()) {
                if (myText != null) {
                    myText.append(logger.getLastlog() + "");
//            myText.setCaretPosition(Math.max(myText.getText().lastIndexOf("\n"), 0));
                    myText.setCaretPosition(Math.max(myText.getText().length(), 0));
                }
            }
        } catch (Exception ex) {

        }
    }

    protected void Print(String message) {
        logger.logMessage(message);
        System.out.println(logger.getLastlog());
    }

    //
    // LARVA
    //
    /**
     * It loads the passport from a disk file
     *
     * @param passportFileName The file that contains the passport
     * @return true if it has been loaded false otherwise
     */
    public boolean loadMyPassport(String passportFileName) {
        try {
            FileReader fmypassport = new FileReader(passportFileName);
            mypassport = new Scanner(fmypassport).useDelimiter("\\Z").next();
            addRunStep("MILES20");
            return true;
        } catch (Exception ex) {
            Error("Unable to load passport file " + passportFileName);
            mypassport = "";
            return false;
        }
    }

    /**
     * It directly sets the passport from a given String
     *
     * @param mypassport The passport to be assigned
     */
    public void setMypassport(String mypassport) {
        this.mypassport = mypassport;
    }

    /**
     * It returns the passport previoulsy set by setMyPassport() or
     * loadMyPassport()
     *
     * @return The string of the passport
     */
    public String getMypassport() {
        return this.mypassport;
    }

    /**
     * It connects to the identity manager and register its passport, previously
     * stored in the variable mypassport with either method loadMyPassport()
     * setMyPassport()
     *
     * @return true if it has suceeded, false otherwise
     */
    protected boolean doLARVACheckin() {
        Info("Checking-in to LARVA");
        if (DFGetAllProvidersOf("IDENTITY").isEmpty()) {
            Error("Unable to checkin at LARVA no identity manager service has been found");
        } else {
            if (mypassport == null || mypassport.length() == 0) {
                this.Error("Please load the passport first");
                return false;
            }
            ACLMessage outbox = new ACLMessage(ACLMessage.SUBSCRIBE);
            IdentityManager = DFGetAllProvidersOf("IDENTITY").get(0);
            Info("Found agent " + IdentityManager + " as Identity Manager");
            AID IM = new AID(IdentityManager, AID.ISLOCALNAME);
            outbox.setSender(getAID());
            outbox.addReceiver(IM);
            outbox.setContent(mypassport);
            Info("Sending passport to " + IdentityManager);
            this.LARVAsend(outbox);
//            checkin = this.blockingReceive(MessageTemplate.MatchSender(IM), WAITANSWERMS);
            checkin = this.LARVAblockingReceive(MessageTemplate.MatchSender(IM), WAITANSWERMS);
            if (checkin == null) {
                Error("Agent " + IdentityManager + " does not answer. Not checked in");
            } else {
                addRunStep("MILES20");
                checkout = checkin.createReply();
                if (checkin.getPerformative() == ACLMessage.CONFIRM) {
                    checkedin = true;
                    Info(checkin.getContent());
                    this.getUserData(checkin.getContent());
                    return true;
                } else if (checkin.getPerformative() == ACLMessage.REFUSE) {
                    Error("Checkin at LARVA refused.\nDetails: " + checkin.getContent());
                } else {
                    Error("Could not checkin at LARVA.\nDetails: " + checkin.getContent());
                }
            }
            return false;
        }

        return false;
    }

    /**
     * It contacts the Identity Manager and send a cancelation of the checkin,
     * that it it checks out the agent from the platform
     *
     * @return true if it has suceeded, false otherwise
     */
    protected boolean doLARVACheckout() {
        Info("Checking-out from LARVA");
        if (checkout == null) {
            return false;
        }
        checkout.setPerformative(ACLMessage.CANCEL);
        checkout.setContent("Request checkout");
        this.LARVAsend(checkout);
        inbox = this.LARVAblockingReceive(MessageTemplate.MatchSender(new AID(IdentityManager, AID.ISLOCALNAME)), WAITANSWERMS);
        if (inbox == null) {
            Error("Agent " + IdentityManager + " does not answer. Not checked out");
        } else {
            if (checkin.getPerformative() == ACLMessage.CONFIRM) {
                Info(inbox.getContent());
                checkedin = false;
                return true;
            } else {
                Error(inbox.getContent());
            }
        }
        return false;
    }

    /**
     * It is a substitute of Agent.send() taht encapsulates two extra
     * behaviours. On the one hand, it is linked with DeepLARVAMonitoring and
     * tells the server if a message is trying to be sent (milestone MILES10).
     * On the other hand, it monitorizes the sending of several messages to the
     * server so that the agent may their answer and ease the processing of
     * sensors through LARVADash
     *
     * @param msg The ACL message to be sent
     */
    protected void LARVAsend(ACLMessage msg) {
        if (!ACLMessageTools.getMainReceiver(msg).equals(this.IdentityManager)) {
            addRunStep("MILES10");
        }
        if (msg.getUserDefinedParameter(ACLMTAG) == null) {
            msg.addUserDefinedParameter(ACLMTAG, Keygen.getHexaKey(20));
        }
//        if (myDashboard != null && msg.getContent() != null
//        if (msg.getOntology() != null && msg.getOntology().toUpperCase().equals("COMMITMENT")) {
//            String skey = msg.getConversationId(), sman;
//            if (skey != null && !this.DFGetAllProvidersOf("SESSION MANAGER " + skey).isEmpty()) {
////                ACLMessage aux = new ACLMessage(msg.getPerformative());
////                aux.setSender(msg.getSender());
////                aux.setConversationId(msg.getConversationId());
////                aux.setReplyWith(msg.getReplyWith());
////                aux.setInReplyTo(msg.getInReplyTo());
////                aux.setOntology(msg.getOntology());
////                aux.setProtocol(msg.getProtocol());
//                sman = this.DFGetAllProvidersOf("SESSION MANAGER " + skey).get(0);
//                msg.addReceiver(new AID(sman, AID.ISLOCALNAME));
////                aux.addReceiver(new AID(sman, AID.ISLOCALNAME));
////                this.send(aux);
////                Info("⬜ Sending ACLM " + ACLMessageTools.fancyWriteACLM(aux, false));
////                sd.addSequence(aux);
//            }
//        }
//        if (msg.getContent() != null
//                && (msg.getContent().toUpperCase().contains("REQUEST JOIN")
//                || (msg.getContent().toUpperCase().contains("QUERY SENSOR")))) {
//            msg = ACLMessageTools.addDashMark(msg);
//        }
        if (this.isSecuredMessages()) {
            this.secureSend(msg);
        }
        this.send(msg);
        Info("⭕> Sending ACLM " + ACLMessageTools.fancyWriteACLM(msg, false) + Console.defText(Console.white));
        myReport.setOutBox(myReport.getOutBox() + 1);
        sd.addSequence(msg);
    }

    /**
     * For the same purposes that LARVAsend, LARVAblocking receive()
     * encapsulates Agent.blockingReceive() and to ease the reception of extra
     * information from the server in order to make the UI more usable and easy
     * to understand
     *
     * @return
     */
    protected ACLMessage LARVAblockingReceive() {
        ACLMessage res;
        boolean repeat;
        do {
            repeat = false;
            res = blockingReceive();
//            if (res != null && res.getContent().contains("filedata")) {
//                Ole ocontent = new Ole().set(res.getContent());
//                OleFile ofile = new OleFile(ocontent.getOle("surface"));
//                int maxlevel = ocontent.getInt("maxflight");
//                E.setWorldMap(ofile.toString(), maxlevel);
//                if (!getLocalName().startsWith("XUI")) {
//                    repeat = true;
//                }
//            }
//            if (res != null && res.getContent().contains("perceptions")) {
//                E.feedPerception(res.getContent());
//                repeat = false;
//            }
        } while (repeat);
        this.checkReceivedMessage(res);
        if (res != null) {
            Info("⭕< Received ACLM " + ACLMessageTools.fancyWriteACLM(res, false));
            sd.addSequence(res);
            myReport.setInBox(myReport.getInBox() + 1);
        }
        if (this.isSecuredMessages()) {
            this.secureReceive(res);
        }
        return res;
    }

    protected ACLMessage LARVAblockingReceive(long milis) {
        ACLMessage res;
        boolean repeat = false;
        addRunStep("MILES13");
        do {
            repeat = false;
            res = blockingReceive(milis);
//            if (res != null && res.getContent().contains("filedata")) {
//                Ole ocontent = new Ole().set(res.getContent());
//                OleFile ofile = new OleFile(ocontent.getOle("surface"));
//                int maxlevel = ocontent.getInt("maxflight");
//                E.setWorldMap(ofile.toString(), maxlevel);
//                if (!getLocalName().startsWith("XUI")) {
//                    repeat = true;
//                }
//            }
//            if (res != null && res.getContent().contains("perceptions")) {
//                E.feedPerception(res.getContent());
//                repeat = false;
//            }
        } while (repeat);
        this.checkReceivedMessage(res);
        if (res != null) {
            Info("⭕< Received ACLM " + ACLMessageTools.fancyWriteACLM(res, false));
            sd.addSequence(res);
            myReport.setInBox(myReport.getInBox() + 1);
        }
        if (this.isSecuredMessages()) {
            this.secureReceive(res);
        }
        return res;
    }

    public ACLMessage LARVAblockingReceive(MessageTemplate t) {
        ACLMessage res;
        boolean repeat = false;
        addRunStep("MILES13");
        do {
            repeat = false;
            res = blockingReceive(t);
//            if (res != null && res.getContent().contains("filedata")) {
//                Ole ocontent = new Ole().set(res.getContent());
//                OleFile ofile = new OleFile(ocontent.getOle("surface"));
//                int maxlevel = ocontent.getInt("maxflight");
//                E.setWorldMap(ofile.toString(), maxlevel);
//                if (!getLocalName().startsWith("XUI")) {
//                    repeat = true;
//                }
//            }
//            if (res != null && res.getContent().contains("perceptions")) {
//                E.feedPerception(res.getContent());
//                repeat = false;
//            }
        } while (repeat);
        this.checkReceivedMessage(res);
        if (res != null) {
            Info("⭕< Received ACLM " + ACLMessageTools.fancyWriteACLM(res, false));
            sd.addSequence(res);
            myReport.setInBox(myReport.getInBox() + 1);
        }
        if (this.isSecuredMessages()) {
            this.secureReceive(res);
        }
        return res;
    }

    protected ACLMessage LARVAblockingReceive(MessageTemplate t, long milis) {
        ACLMessage res;
        boolean repeat = false;
        addRunStep("MILES13");
        do {
            repeat = false;
            res = blockingReceive(t, milis);
//            if (res != null && res.getContent().contains("filedata")) {
//                Ole ocontent = new Ole().set(res.getContent());
//                OleFile ofile = new OleFile(ocontent.getOle("surface"));
//                int maxlevel = ocontent.getInt("maxflight");
//                E.setWorldMap(ofile.toString(), maxlevel);
//                if (!getLocalName().startsWith("XUI")) {
//                    repeat = true;
//                }
//            }
//            if (res != null && res.getContent().contains("perceptions")) {
//                E.feedPerception(res.getContent());
//                repeat = false;
//            }
        } while (repeat);
        this.checkReceivedMessage(res);
        if (res != null) {
            Info("⭕< Received ACLM " + ACLMessageTools.fancyWriteACLM(res, false));
            sd.addSequence(res);
            myReport.setInBox(myReport.getInBox() + 1);
        }
        if (this.isSecuredMessages()) {
            this.secureReceive(res);
        }
        return res;
    }

    /**
     * Internal to DeepLarvaMonitoring to check the achievement of several
     * internal milestones
     *
     * @param res The message received by the agent which is monitored to detect
     * several milestones
     */
    private void checkReceivedMessage(ACLMessage res) {
        if (traceRunSteps) {
            addRunStep("MILES13");
            if (res != null) {
                if (res.getContent().startsWith("Hello, my name is")) {
                    this.mySessionmanager = res.getSender().getLocalName();
                }
                if (res.getContent().startsWith("Agree to open")) {
                    addRunStep("MILES24");
                }
                if (res.getContent().startsWith("Confirm check-in")) {
                    addRunStep("MILES22");
                }
                if (res.getContent().startsWith("Confirm check-out")) {
                    addRunStep("MILES23");
                }
                if (res.getContent().contains("has been closed")) {
                    addRunStep("MILES25");
                }

            }
        }

    }

    /**
     * This method ask the user for confirmation (yes=true, no = false) in front
     * of a given message
     *
     * @param message The question asked to the user
     * @return true if the user select yes or false if the user selects no
     */
    @Override
    protected boolean Confirm(String message) {
        if (isSwing()) {
            int op = JOptionPane.showConfirmDialog(null,
                    message, "Agent " + getLocalName(), JOptionPane.YES_NO_OPTION);

            return op == JOptionPane.YES_OPTION;
        } else {
            return super.Confirm(message);
        }
    }

    /**
     * It shows a message to the user and waits until the user confirms it has
     * read it
     *
     * @param message
     */
    @Override
    public void Alert(String message) {
        if (isSwing()) {
            JOptionPane.showMessageDialog(null,
                    message, "Agent " + getLocalName(), JOptionPane.WARNING_MESSAGE);
        } else {
            Info(message);
        }
    }

    /**
     * It shows a message to the user and waits until the user confirms it has
     * read it
     *
     * @param message
     */
    @Override
    public void Message(String message) {
        if (isSwing()) {
            JOptionPane.showMessageDialog(null,
                    message, "Agent " + getLocalName(), JOptionPane.INFORMATION_MESSAGE);
        } else {
            Info(message);
        }
    }

    /**
     * It asks the user to input a String
     *
     * @param message The message shown to the user
     * @return The string typed by the user
     */
    @Override
    protected String inputLine(String message) {
        if (isSwing()) {
            String res = myApp.inputLine(message);
            return res;
        } else {
            return super.inputLine(message);
        }
    }

    /**
     * It asks the user to select an input String only from a set of allowed
     * options
     *
     * @param message The message shown to the user
     * @param options An array of Strings as the set of possible selections
     * @param value The default selection
     * @return The string selected by the user
     */
    protected String inputSelect(String message, String[] options, String value) {
        if (isSwing()) {
            String res = myApp.inputSelect(message, options, value);
            return res;
        } else {
            return super.inputLine(message);
        }
    }

    protected void doSwingLater(Runnable what) {
        SwingUtilities.invokeLater(() -> {
            what.run();
        });
    }

    protected void doSwingWait(Runnable what) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                what.run();
            });
        } catch (Exception ex) {
        }
    }

    /**
     * It activates the SWING dashboard
     *
     */
    public void doActivateLARVADash() {
//        myDashboard.setActivated(true);
//        myDashboard.initGUI();
    }

    /**
     * It reports the name of the Identity Manager found automatically by the
     * Checkin Method
     *
     * @return
     */
    public String getIdentityManager() {
        return IdentityManager;
    }

    /**
     * It sets the name of the Identity Manager
     *
     * @param IdentityManager The knwon name of the IM
     */
    public void setIdentityManager(String IdentityManager) {
        this.IdentityManager = IdentityManager;
    }

    /**
     * It looks the Df and returns the names of all agents that provide any type
     * of service at LARVA
     *
     * @return An ArrayList of agent names
     */
    @Override
    public ArrayList<String> DFGetProviderList() {
        addRunStep("MILES21");
        return super.DFGetProviderList();
    }

    /**
     * It provides the full list of services provided by all agents in the
     * platform
     *
     * @return An ArrayList of names of services
     */
    @Override
    public ArrayList<String> DFGetServiceList() {
        addRunStep("MILES21");
        return super.DFGetServiceList();
    }

    /**
     * It provides the names of all agents which are known to provide a given
     * service
     *
     * @param service The name of a service
     * @return An arrayList of agent names who provide that service
     */
    @Override
    public ArrayList<String> DFGetAllProvidersOf(String service) {
        addRunStep("MILES21");
        return super.DFGetAllProvidersOf(service);
    }

    /**
     * It provides the names of the services provided by an agent
     *
     * @param agentName The provider agent
     * @return An ArrayList of service names provided by that agent
     */
    @Override
    public ArrayList<String> DFGetAllServicesProvidedBy(String agentName) {
        addRunStep("MILES21");
        return super.DFGetAllServicesProvidedBy(agentName);
    }

    /**
     * It simply checks whether or not this agent provides this service
     *
     * @param agentName The agent name
     * @param service The service name
     * @return True if the agent provides that service, false otherwise
     */
    @Override
    public boolean DFHasService(String agentName, String service) {
        addRunStep("MILES21");
        return super.DFHasService(agentName, service);
    }

    private void addRunStep(String step) {
        if (!traceRunSteps) {
            return;
        }
        if (stepsSent.size() == 0 && stepsDone.findItem(step)) {
            return;
        }
        stepsDone.addUniqueItem(step);
        stepsSent.addUniqueItem(step);
        if (mySessionmanager.length() > 0) {
            ACLMessage outgoing = new ACLMessage(ACLMessage.PROXY);
            outgoing.setSender(getAID());
            outgoing.addReceiver(new AID(mySessionmanager, AID.ISLOCALNAME));
            outgoing.setContent(stepsSent.prettyprint());
            this.send(outgoing);
            stepsSent = new OleSet();
        }
    }

    public String getSequenceDiagram() {
        return sd.printSequenceDiagram();
    }

    public void saveSequenceDiagram(String filename) {
        try {
            PrintStream out = new PrintStream(new File(filename));
            out.println(getSequenceDiagram());
            try {
                JTextArea taSeq = (JTextArea) this.payload.getGuiComponents().get("Sequence");
                if (taSeq != null) {
//                    taSeq.append(filename);
                    taSeq.setText(filename);
                    taSeq.append(getSequenceDiagram());
                    taSeq.validate();
                }
            } catch (Exception ex) {
                //Info(getSequenceDiagram());
            }
        } catch (FileNotFoundException ex) {
            Error("Unable to save Sequence Diagram into file " + filename);
        }
    }

    public void clearSequenceDiagram() {
        sd.clear();
    }

    public void getUserData(String welcome) {
        userID = -1;
        userName = "";
        String tokens[] = welcome.split(" ");
        int i;
        for (i = 0; !tokens[i].equals("user") && i < tokens.length; i++);
        if (i < tokens.length) {
            i++;
            try {
                userID = Integer.parseInt(tokens[i]);
                i++;
                while (i < tokens.length) {
                    userName += tokens[i++] + " ";
                }
            } catch (Exception ex) {
            }
        }
        if (sessionAlias.length() == 0) {
            this.sessionAlias = trimFullString(userName);
        }
    }

    protected void setupEnvironment() {
        E = new Environment();
    }

    protected Environment getEnvironment() {
        return E;
    }

    protected void openXUITTY() {
        xuitty = new XUITTY();
        xuitty.init((JPanel) this.payload.getGuiComponents().get("XUI"));
        xuitty.clearScreen();
        xuitty.render();
    }

    protected void closeRemote() {
        if (remote) {
            if (externalTB != null) {
                externalTB.removeAll();
                externalTB.repaint();
            }
            if (this.SWaitButtons != null && this.SWaitButtons.availablePermits() == 0) {
                this.SWaitButtons.release();
            }
            remote = false;
        }
    }

    protected void openRemote() {
        if (remote) {
            return;
        }
        OleApplication parentApp = this.payload.getParent();
        externalTile = (OleAgentTile) this.payload.getGuiComponents().get("TILE " + this.getLocalName());
        externalTB = externalTile.getExternalToolBar();
        externalTB.removeAllButtons();
        int sizeButtons = 24;
        olbContinue = new OleButton(parentApp, "CONTINUE", "play_circle");
        olbContinue.setExtraFlat();
        olbContinue.setBorderPainted(true);
        olbContinue.setContentAreaFilled(true);
        olbContinue.setIcon(new Dimension(sizeButtons, sizeButtons));
        olbContinue.addActionListener(this);
        externalTB.addButton(olbContinue);

        olbPause = new OleButton(parentApp, "PAUSE", "pause_circle");
//        olbPause.setExtraFlat();
        olbPause.setExtraFlat();
        olbPause.setBorderPainted(true);
        olbPause.setContentAreaFilled(true);
        olbPause.setIcon(new Dimension(sizeButtons, sizeButtons));
        olbPause.addActionListener(this);
        externalTB.addButton(olbPause);

        olbNext = new OleButton(parentApp, "NEXT", "not_started");
        olbNext.setExtraFlat();
        olbNext.setBorderPainted(true);
        olbNext.setContentAreaFilled(true);
        olbNext.setIcon(new Dimension(sizeButtons, sizeButtons));
        olbNext.addActionListener(this);
        externalTB.addButton(olbNext);
        olbUntil = new OleButton(parentApp, "UNTIL", "history");
        olbUntil.setExtraFlat();
        olbUntil.setBorderPainted(true);
        olbUntil.setContentAreaFilled(true);
        olbUntil.setIcon(new Dimension(sizeButtons, sizeButtons));
        olbUntil.addActionListener(this);
        externalTB.addButton(olbUntil);
        this.olbContinue.setEnabled(true);
        this.olbNext.setEnabled(true);
        this.olbPause.setEnabled(false);
        this.olbUntil.setEnabled(true);
        remote = true;
        remotePause();
    }

    public void remotePlay() {
        if (this.remote) {
            this.SWaitButtons.release();
            this.cont = true;
            this.olbContinue.setEnabled(false);
            this.olbNext.setEnabled(false);
            this.olbPause.setEnabled(true);
            this.olbUntil.setEnabled(false);
        }
    }

    public void remotePause() {
        if (this.remote) {
            this.cont = false;
            this.SWaitButtons.drainPermits();
            this.olbContinue.setEnabled(true);
            this.olbNext.setEnabled(true);
            this.olbPause.setEnabled(false);
            this.olbUntil.setEnabled(true);
        }
    }

    public void remoteNextStep() {
        if (this.remote) {
            this.cont = false;
            this.olbContinue.setEnabled(true);
            this.olbNext.setEnabled(true);
            this.olbPause.setEnabled(false);
            this.olbUntil.setEnabled(true);
            this.SWaitButtons.release();
        }
    }

    public void remoteJumpSteps() {
        if (this.remote) {
            try {
                nUntil = Integer.parseInt(this.inputLine("Please execute the agent until it reachesthe following steps"));
            } catch (Exception ex) {
                nUntil = -1;
            }
            this.cont = true;
            this.SWaitButtons.release();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "CONTINUE":
                remotePlay();
                break;
            case "PAUSE":
                remotePause();
                break;
            case "NEXT":
                remoteNextStep();
                break;
            case "UNTIL":
                remoteJumpSteps();
                break;
        }
    }

    public void setFrameDelay(int milis) {
        this.frameDelay = milis;
    }

    /////////////////// MISSIONS ////////////////77
    protected void clearCourseSelection() {
        missionName = null;
        setTaskName(null);
        E.setTarget(null);
        E.setDestination(null);

    }

    protected void decodeMissions(String things) {
        clearCourseSelection();
        JsonObject jsoThings = Json.parse(things).asObject();
        JsonArray jsathings;
        if (jsoThings.get("missions") != null) {
            Missions = new MissionSet(jsoThings.get("missions").asArray());
        }
    }

    protected String getCurrentMission() {
        String agentName = getLocalName();
        return getCurrentMission(agentName);
    }

    protected String getCurrentTask() {
        String agentName = getLocalName();
        return getCurrentTask(agentName);
    }

    protected String getagentType(String agentName) {
        if (this.AMSIsConnected(agentName)) {
            for (String service : this.DFGetAllServicesProvidedBy(agentName)) {
                if (service.startsWith("TYPE")) {
                    return service.replaceAll("TYPE ", "");
                }
            }

        }
        return "";
    }

    protected String getCurrentMission(String agentName) {
        if (this.AMSIsConnected(agentName)) {
            for (String service : this.DFGetAllServicesProvidedBy(agentName)) {
                if (service.startsWith("MISSION")) {
                    return service.replaceAll("MISSION ", "");
                }
            }

        }
        return "";
    }

    protected String getCurrentTask(String agentName) {
        if (this.AMSIsConnected(agentName)) {
            for (String service : this.DFGetAllServicesProvidedBy(agentName)) {
                if (service.startsWith("TASK")) {
                    return service.replaceAll("TASK ", "");
                }
            }

        }
        return "";
    }

    protected boolean nextTask() {
        if (missionName != null && Missions.containsKey(missionName) && iTask < nTasks) {
            return setTaskName(Missions.get(missionName).get(iTask++));
        } else {
            return false;
        }
    }

    protected boolean setTaskName(String task) {
        if (missionName != null) { // && (Missions.get(missionName).contains(task) || task.equals("PARKING"))) {
            taskName = task;
            for (String service : this.DFGetAllServicesProvidedBy(getLocalName())) {
                if (service.startsWith("TASK")) {
                    this.DFRemoveMyServices(new String[]{service});
                }
            }
            this.DFAddMyServices(new String[]{"TASK " + task});
            return true;
        } else {
            return false;
        }
    }

    public String activateMission(String mission) {
        if (Missions.keySet().contains(mission)) {
            if (Missions.containsKey(mission)) {
                missionName = mission;
                for (String service : this.DFGetAllServicesProvidedBy(getLocalName())) {
                    if (service.startsWith("MISSION")) {
                        this.DFRemoveMyServices(new String[]{service});
                    }
                }
                iTask = 0;
                if (mission != null) {
                    nTasks = getMissionLength(missionName);
                    this.DFAddMyServices(new String[]{"MISSION " + missionName});
                    return activateNextTask();
                } else {
                    nTasks = 0;
                }
                return "ERROR";
            } else {
                return "ERROR";
            }
        }
        return "ERROR";
    }

    public int getNumMissions() {
        return Missions.size();
    }

    public String[] getAllMissions() {
        return Transform.toArrayString(new ArrayList(Missions.keySet()));
    }

    public String[] getMissionTasks(String mission) {
        if (Missions.keySet().contains(mission)) {
            return Transform.toArrayString(Missions.get(mission));
        } else {
            return null;
        }
    }

    public int getMissionLength(String mission) {
        if (Missions.keySet().contains(mission)) {
            return Missions.get(mission).size();
        } else {
            return -1;
        }
    }

    public boolean isOverCurrentMission() {
        return iTask == nTasks && isOverCurrentTask();
    }

    public boolean isOverCurrentTask() {
        if (missionName != null && Missions.get(missionName) != null) {
            String task = taskName;
            switch (task.split(" ")[0]) {
                case "MOVEIN":
                case "MOVETO":
                    return E.getGPS().isEqualTo(E.getTarget());
                default:
                    return false;
            }
        } else {
            return true;
        }
    }

    protected String activateNextTask() {
        Error("Method activateTask has not been defined yet.");
        System.exit(1);
        return "";
    }

    public boolean isSecuredMessages() {
        return securedMessages;
    }

    public void setSecuredMessages(boolean securedMessages) {
        this.securedMessages = securedMessages;
    }

    public void secureExit(String why) {
        this.Alert("Secure messaging has stopped agent " + getLocalName() + " due to " + why);
        this.doExit();
    }

    public void secureReceive(ACLMessage msg) {
//        if (dupReceive(msg)) {
//            secureExit("Too many messages received");
//        }
        if (msg == null) {
            return;
        }
        if (this.isErrorMessage(msg)) {
            this.nrecErrors++;
        } else {
            nrecErrors = 0;
        }
        if (nrecErrors > 5) {
            this.secureExit("Too many consecutive errors");
        }
    }

    public void secureSend(ACLMessage msg) {
        if (dupSend(msg)) {
            secureExit("Too many messages sent");
        }
    }

    public boolean dupSend(ACLMessage msg) {
//        if (msg.getContent().equals(this.lastSentMsg)) {
//            nlastSentMsg++;
//            if (nlastSentMsg > 10) {
//                return true;
//            }
//        } else {
//            nlastSentMsg = 0;
//            lastSentMsg = msg.getContent();
//        }
        if (msg.getUserDefinedParameter(ACLMTAG).equals(this.lastSentACLMID)) {
            nlastSentACLMID++;
            if (nlastSentACLMID > 1) {
                return true;
            }
        } else {
            lastSentACLMID = msg.getUserDefinedParameter(ACLMTAG);
            nlastSentACLMID = 0;
        }
        return false;
    }

    public boolean dupReceive(ACLMessage msg) {
        if (msg.getContent().equals(this.lastRecMsg)) {
            nlastRecMsg++;
            if (nlastRecMsg > 10) {
                return true;
            }
        } else {
            nlastRecMsg = 0;
            lastRecMsg = msg.getContent();
        }
        return false;
    }

    public boolean isErrorMessage(ACLMessage msg) {
        if (this.errortags.contains(ACLMessage.getPerformative(msg.getPerformative()))) {
            return true;
        }
        String c = msg.getContent().toUpperCase();
        for (String s : errortags) {
            if (c.startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
