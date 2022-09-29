/**
 * @file LARVAFirstAgent.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package agents;

import Environment.Environment;
import ai.Choice;
import ai.DecisionSet;
import ai.Mission;
import ai.MissionSet;
import appboot.XUITTY;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import console.Console;
import crypto.Keygen;
import static crypto.Keygen.getHexaKey;
import data.Ole;
import data.OleConfig;
import data.OlePassport;
import data.OleSet;
import data.Transform;
import disk.Logger;
import static disk.Logger.trimFullString;
import geometry.SimpleVector3D;
import glossary.Signals;
import jade.core.AID;
import jade.core.MicroRuntime;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
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
import static messaging.ACLMessageTools.ACLMID;
import messaging.SequenceDiagram;
import swing.OleAgentTile;
import swing.OleApplication;
import swing.OleButton;
import swing.OleToolBar;
import static tools.Internet.getExtIPAddress;
import static tools.Internet.getLocalIPAddress;
import tools.TimeHandler;
import tools.emojis;
import static messaging.ACLMessageTools.ACLMRCVDATE;
import static messaging.ACLMessageTools.ACLMROLE;
import static messaging.ACLMessageTools.ACLMSNDDATE;
import static messaging.ACLMessageTools.ACLMSTEALTH;

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
    protected String IdentityManager = "";

    protected int userID = -1;
    protected String userName = "";

    // Its known sequence diagram
    protected static SequenceDiagram sd;
    //
    protected String title, problemName;

    protected OleSet stepsDone, stepsSent;
    protected boolean DeepMonitor = false;
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

//    protected MissionSet Missions;
    protected Mission currentMission;
//    protected int iTask, nTasks;
    protected String sessionAlias = "";

    protected boolean securedMessages;
    protected String lastSentMsg = "", lastRecMsg = "", lastSentACLMID = "";
    protected static String sepTransponder = "/", sepGoals = ";";
    protected int nlastSentMsg = 0, nlastSentACLMID = 0, nlastRecMsg = 0, nrecErrors = 0;
    private ACLMessage lastSend, lastReceive;
    protected List<String> errortags = Stream.of("FAILURE", "REFUSE", "NOT-UNDERSTOOD", "BAD ", "ERROR").collect(Collectors.toList());
    protected BaseFactoryAgent baseFactory;
    protected boolean allowExceptionShield = true, allowSequenceDiagrams = true,
            continuousSequence = true,
            ignoreExceptions = false, allowEaryWarning = true;

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
        if (E == null || E.isCrahsed()) {
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
//        Missions = new MissionSet();
        stepsDone = new OleSet();
        stepsSent = new OleSet();
        if (sd == null) {
            sd = new SequenceDiagram(this.getLocalName());
        } else {
            if (sd.getOwner().equals(getLocalName())) {
                sd.clear();
            }
        }
        addMilestone("MILES00");
        addMilestone("MILES01");
        this.logger.setEcho(true);
        // create a new frame to store text field and button
        if (this.getArguments() != null && this.getArguments().length > 0) {
            payload = (LARVAPayload) this.getArguments()[0];
            if (payload.getGuiComponents() != null) {
                myText = (JTextArea) payload.getGuiComponents().get("Activity log");
                myApp = payload.getParent();
            }
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
        E = new Environment(this);
        if (showRemote) {
            openRemote();
        }
        this.setSecuredMessages(true);
        this.activateSequenceDiagrams();
        this.setContinuousSequenceDiagram(true);
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
            if (!ignoreExceptions) {
                LARVAexit = true;
            }
        }
    }

    public void doExit() {
        if (this.allowExceptionShield) {
            throw new ExitRequestedException(Signals.EXITREQUESTED.name(), new IOException());
        } else {
            doSoftExit();
        }
    }

    public void doSoftExit() {
        LARVAexit = true;
    }

    public void doNotExit() {
        LARVAexit = false;
    }

    @Override
    protected void BehaviourDefaultSetup() {
        if (this.allowExceptionShield) {
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
        } else {
            defaultBehaviour = new Behaviour() {
                @Override
                public void action() {
                    preExecute();
                    Execute();
                    postExecute();
                    ncycles++;
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
    }

    @Override
    public void postExecute() {
        myReport.tick();
        if (this.frameDelay > 0 && (!remote || cont)) {
//            LARVAwait(frameDelay);
            this.doWait(frameDelay);
//            this.defaultBehaviour.block(frameDelay);
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
        this.DeepMonitor = true;
    }

    /**
     * It allows a deep analysis of the changes of state of the agents that
     * inherit from this class. It produces a hihgh traffic of messages,
     * therefore, it must be used only upon teacher instruction.
     */
    protected void disableDeepLARVAMonitoring() {
        this.DeepMonitor = false;
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
        addMilestone("MILES03");
        if (remote) {
            closeRemote();
        }
        if (this.isActiveSequenceDiagrams()) {
            this.addSequenceDiagram(null);
            this.saveSequenceDiagram(getName() + ".seqd");
            this.drawSequenceDiagram();
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
        addMilestone("MILES02");
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
        addMilestone("MILES02");
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

    protected void InfoACLM(String message, ACLMessage msg) {
        if (logger.isEcho()) {
            Info(message + " " + ACLMessageTools.fancyWriteACLM(msg, false));
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
            addMilestone("MILES16");
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
            this.addMilestone("MILES16");
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
                addMilestone("MILES17");
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

//    @Override
//    protected ACLMessage LARVAprocessAnyMessage(ACLMessage msg) {
//        return super.LARVAprocessAnyMessage(msg);
//    }
    protected void checkDeepMilestones(ACLMessage msg) {
        if (this.DeepMonitor && msg != null) {
            if (msg.getUserDefinedParameter(ACLMROLE) != null
                    && (msg.getUserDefinedParameter(ACLMROLE).equals("SESSION MANAGER"))) {
                if (mySessionmanager == null || mySessionmanager.length() == 0) {
                    this.mySessionmanager = msg.getSender().getLocalName();
                }
                this.addMilestone("MILES24");
            }
            if (!this.hasMilestone("MILES22")) {
                if (msg.getUserDefinedParameter(ACLMROLE) != null
                        && msg.getUserDefinedParameter(ACLMROLE).equals("IDENTITY")) {
                    this.addMilestone("MILES22");
                }
            }
            if (!this.hasMilestone("MILES23")) {
                if (msg.getUserDefinedParameter(ACLMROLE) != null
                        && msg.getUserDefinedParameter(ACLMROLE).equals("PMANAGER")) {
                    this.addMilestone("MILES23");
                }
            }
        }
        if (msg.getContent().startsWith("Agree to open")) {
            addMilestone("MILES19");
        }
        if (msg.getContent().startsWith("Confirm check-in")) {
            addMilestone("MILES17");
        }
        if (msg.getContent().startsWith("Confirm check-out")) {
            addMilestone("MILES18");
        }
        if (msg.getContent().contains("has been closed")) {
            addMilestone("MILES20");
        }
    }

    @Override
    protected ACLMessage LARVAprocessSendMessage(ACLMessage msg) {
        msg = super.LARVAprocessSendMessage(msg);
        this.addMilestone("MILES04");
        if (this.isSecuredMessages()) {
            this.secureSend(msg);
        }
//        myReport.setOutBox(myReport.getOutBox() + 1);
        if (msg.getContent().startsWith("REPORT;") && msg.getPerformative() == ACLMessage.NOT_UNDERSTOOD) {
            msg.setPerformative(ACLMessage.INFORM_REF);
            msg.setConversationId(this.mySessionID);
            msg.setReplyWith("MyReport");
        }
        this.addSequenceDiagram(msg);
        checkDeepMilestones(msg);
        return msg;
    }

    @Override
    protected ACLMessage LARVAprocessReceiveMessage(ACLMessage msg) {
        msg = super.LARVAprocessReceiveMessage(msg);
        checkDeepMilestones(msg);
        this.addMilestone("MILES07");
        if (this.allowEaryWarning && this.isErrorMessage(msg)) {
            Alert(emojis.WARNING + "EARLY WARNING SYSTEM\n" + "Error found " + msg.getContent());
        }
        if (this.isSecuredMessages()) {
            this.secureReceive(msg);
        }
        this.addSequenceDiagram(msg);
        return msg;
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
        this.LARVAprocessSendMessage(msg);
        this.send(msg);
        lastSend = msg;
        InfoACLM("⭕> Sending ACLM ", msg);
    }

    /**
     * For the same purposes that LARVAsend, LARVAblocking receive()
     * encapsulates Agent.blockingReceive() and to ease the reception of extra
     * information from the server in order to make the UI more usable and easy
     * to understand
     *
     * @return
     */
    protected ACLMessage LARVAreceive() {
        ACLMessage msg;
        msg = receive();
        if (msg != null && !msg.getSender().getLocalName().equals("ams")) {
            InfoACLM("⭕< Received ACLM ", msg);
            msg = this.LARVAprocessReceiveMessage(msg);
        }
        return msg;
    }

    protected ACLMessage LARVAblockingReceive() {
        ACLMessage msg;
        boolean repeat;
        msg = blockingReceive();
        if (msg != null) {
            InfoACLM("⭕< Received ACLM ", msg);
            msg = this.LARVAprocessReceiveMessage(msg);
        }
        return msg;
    }

    protected ACLMessage LARVAblockingReceive(long milis) {
        ACLMessage msg;
        boolean repeat = false;
        msg = blockingReceive(milis);
        if (msg != null) {
            InfoACLM("⭕< Received ACLM ", msg);
            msg = this.LARVAprocessReceiveMessage(msg);
        }
        return msg;
    }

    public ACLMessage LARVAblockingReceive(MessageTemplate t) {
        ACLMessage msg;
        boolean repeat = false;
        msg = blockingReceive(t);
        if (msg != null) {
            InfoACLM("⭕< Received ACLM ", msg);
            msg = this.LARVAprocessReceiveMessage(msg);
        }
        return msg;
    }

    protected ACLMessage LARVAblockingReceive(MessageTemplate t, long milis) {
        ACLMessage msg;
        boolean repeat = false;
        msg = blockingReceive(t, milis);
        if (msg != null) {
            InfoACLM("⭕< Received ACLM ", msg);
            msg = this.LARVAprocessReceiveMessage(msg);
        }
        return msg;
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
//        if (isSwing()) {
        JOptionPane.showMessageDialog(null,
                message, "Agent " + getLocalName(), JOptionPane.WARNING_MESSAGE);
//        } else {
//            Info(message);
//        }
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
            OleApplication app = payload.getParent();
            JOptionPane.showMessageDialog(app,
                    message, "Agent " + getLocalName(), JOptionPane.INFORMATION_MESSAGE);
//            Info(message);
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
            OleApplication app = payload.getParent();
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
        addMilestone("MILES25");
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
        addMilestone("MILES25");
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
        addMilestone("MILES25");
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
        addMilestone("MILES25");
        return super.DFGetAllServicesProvidedBy(agentName);
    }

    public ArrayList<String> DFGetAllMyServices() {
        addMilestone("MILES25");
        return super.DFGetAllServicesProvidedBy(getLocalName());
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
        addMilestone("MILES25");
        return super.DFHasService(agentName, service);
    }

    @Override
    public boolean DFSetMyServices(String[] services) {
        addMilestone("MILES26");
        return super.DFSetMyServices(services);
    }

    @Override
    public boolean DFAddMyServices(String[] services) {
        addMilestone("MILES26");
        return super.DFAddMyServices(services);
    }

    @Override
    public boolean DFRemoveMyServices(String[] services) {
        addMilestone("MILES26");
        return super.DFRemoveMyServices(services);
    }

    /**
     * It allows the de-registration of all services.
     */
    @Override
    public void DFRemoveAllMyServices() {
        addMilestone("MILES26");
        super.DFRemoveAllMyServices();
    }

    protected void addMilestone(String step) {
        if (!DeepMonitor) {
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
            this.send(outgoing); // send it hidden
            stepsSent = new OleSet();
        }
    }

    protected boolean hasMilestone(String milestone) {
        return stepsDone.findItem(milestone);
    }

    public void addSequenceDiagram(ACLMessage msg) {
        if (this.isActiveSequenceDiagrams()) {
            sd.addSequence(msg, getLocalName());
            this.getSequenceDiagram();
        }
    }

    public String getSequenceDiagram() {
        String res = "";
        if (sd.getOwner().equals(this.getLocalName()) && this.isActiveSequenceDiagrams()) {
            try {
                res = sd.printSequenceDiagram();
                if (this.isContinuousSequenceDiagrams()) {
                    this.drawSequenceDiagram();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    public String drawSequenceDiagram() {
        String res = "";
        if (sd.getOwner().equals(this.getLocalName()) && this.isActiveSequenceDiagrams()) {
            try {
                res = sd.printSequenceDiagram();
                if (this.payload != null) {
                    JTextArea taSeq = (JTextArea) this.payload.getGuiComponents().get("Sequence");
                    if (taSeq != null) {
//                    taSeq.append(filename);
//                    taSeq.setText(getLocalName());
                        taSeq.setText(res);
//                    taSeq.append(res);
                        taSeq.validate();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    public void saveSequenceDiagram(String filename) {
        if (this.isActiveSequenceDiagrams()) {
            try {
                PrintStream out = new PrintStream(new File(filename));
                out.println(getSequenceDiagram());
            } catch (FileNotFoundException ex) {
                Error("Unable to save Sequence Diagram into file " + filename);
            }
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
        E = new Environment(this);
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
    protected String chooseMission() {
        Info("Choosing a mission");
        String m = "";
        if (getEnvironment().getAllMissions().length == 1) {
            m = getEnvironment().getAllMissions()[0];
        } else {
            m = this.inputSelect("Please chhoose a mission", getEnvironment().getAllMissions(), "");
        }
        Info("Selected mission " + m);
        return m;
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

    //////////////////////
    protected String Transponder() {
        String goal = "";
        String sep = this.sepTransponder, answer = "TRANSPONDER" + sep;

        answer += "NAME " + getLocalName() + sep + "TYPE " + E.getType();
        if (E.getGround() > 0) {
            answer += sep + "STATUS MOVING";
        } else {
            answer += sep + "STATUS GROUNDED " + getEnvironment().getCurrentCity();
        }
        answer += sep + "GPS " + E.getGPS().toString() + sep + "COURSE " + SimpleVector3D.Dir[E.getGPSVector().getsOrient()] + sep + "PAYLOAD " + E.getPayload();
        answer += sep + "GOAL " + E.getCurrentGoal() + sep + "MISSION " + E.getCurrentMission();
        return answer;
//    String sep = Mission.sepMissions, answer = "TRANSPONDER" + sep;
//        answer += "NAME " + getLocalName() + sep + "TYPE " + E.getType();
//        if (E.getGround() > 0) {
//            answer += sep + "ONAIR";
//        } else {
//            answer += sep + "GROUND " + getEnvironment().getCurrentCity();
//        }
//        answer += sep + "GPS " + E.getGPS().toString() + sep + "COURSE " + SimpleVector3D.Dir[E.getGPSVector().getsOrient()] + sep + "PAYLOAD " + E.getPayload();
//        answer += sep + "MISSION " + E.getCurrentMission() + sep + "GOAL " + E.getCurrentGoal();
//        return answer;
    }

    public String getTransponderField(String transponder, String field) {
        String fields[] = transponder.split(this.sepTransponder);
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].startsWith(field)) {
                return fields[i];
            }
        }
        return "";
    }

//    public void replyTransponder(ACLMessage request) {
//        outbox = request.createReply();
//        outbox.setPerformative(ACLMessage.INFORM);
//        outbox.setContent(Transponder());
//        this.LARVAsend(outbox);
//    }
//
    public void sendStealthTransponder() {
        ACLMessage out = new ACLMessage(ACLMessage.INFORM_REF);
        out.setSender(getAID());
        out.addReceiver(new AID(this.mySessionmanager, AID.ISLOCALNAME));
        out.setContent(Transponder());
        out.addUserDefinedParameter(ACLMSTEALTH, "TRUE");
        send(out);
    }

    public boolean isSecuredMessages() {
        return securedMessages;
    }

    public void setSecuredMessages(boolean securedMessages) {
        this.securedMessages = securedMessages;
    }

    public void secureExit(String why) {
        this.Alert(emojis.WARNING + "SECURE MESSAGING SYSTEM\n" + "Secure messaging has stopped agent " + getLocalName() + " due to " + why);
        this.doExit();
    }

    public void secureReceive(ACLMessage msg) {
        if (msg.getUserDefinedParameter(ACLMID) == null) {
            secureExit(emojis.WARNING + "SECURE MESSAGING SYSTEM\n" + "Irregular message received. Not a LARVA secure message");
        }
        if (dupReceive(msg)) {
            secureExit(emojis.WARNING + "SECURE MESSAGING SYSTEM\n" + "Too many messages received");
        }
        if (msg == null) {
            return;
        }
        if (this.isErrorMessage(msg)) {
            this.nrecErrors++;
        } else {
            nrecErrors = 0;
        }
        if (nrecErrors > 5) {
            this.secureExit(emojis.WARNING + "SECURE MESSAGING SYSTEM\n" + "Too many consecutive errors");
        }
    }

    public void secureSend(ACLMessage msg) {
        if (msg.getUserDefinedParameter(ACLMID) == null) {
            secureExit(emojis.WARNING + "SECURE MESSAGING SYSTEM\n" + "Irregular message sent. Not a LARVA secure message");
        }
        if (dupSend(msg)) {
            secureExit(emojis.WARNING + "SECURE MESSAGING SYSTEM\n" + "Too many consecutive messages sent");
        }
        if (msg.getContent() == null) {
            secureExit(emojis.WARNING + "SECURE MESSAGING SYSTEM\n" + "You are sending a message with an empty content. Please use .setContent() properly");
        }
        if (ACLMessageTools.getAllReceivers(msg) == null || ACLMessageTools.getAllReceivers(msg).length() == 0) {
            secureExit(emojis.WARNING + "SECURE MESSAGING SYSTEM\n" + "You are sending a message with an empty receiver. Please use .addReceiver() properly");
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
        if (msg.getUserDefinedParameter(ACLMID).equals(this.lastSentACLMID)) {
            nlastSentACLMID++;
            if (nlastSentACLMID > 2) {
                return true;
            }
        } else if (lastSend != null && msg.getConversationId().equals(lastSend.getConversationId())
                && msg.getContent().equals(lastSend.getContent())
                && msg.getInReplyTo().equals(lastSend.getInReplyTo())) {
            nlastSentACLMID++;
            if (nlastSentACLMID > 2) {
                return true;
            }
        } else {
            nlastSentACLMID = 0;
        }
        lastSentACLMID = msg.getUserDefinedParameter(ACLMID);
        return false;
    }

    public boolean dupReceive(ACLMessage msg) {
        if (msg.getContent().equals(this.lastRecMsg)) {
            nlastRecMsg++;
            if (nlastRecMsg > 10) {
                return true;
            }
        } else {
            if (lastReceive != null && msg.getConversationId().equals(lastReceive.getConversationId())
                    && msg.getContent().equals(lastReceive.getContent())
                    && msg.getReplyWith().equals(lastReceive.getReplyWith())) {
                return true;
            }
            nlastRecMsg = 0;
            lastRecMsg = msg.getContent();
        }
        return false;
    }

    public boolean isErrorMessage(ACLMessage msg) {
//        if (this.errortags.contains(msg.getContent().split(" ")[0].toUpperCase())) {
//            return true;
//        }
        if (ACLMessageTools.ERRORS.contains(msg.getPerformative())) {
            return true;
        }
//        String c = msg.getContent().toUpperCase();
//        for (String s : errortags) {
//            if (c.startsWith(s)) {
//                return true;
//            }
//        }
        return false;
    }

    public boolean doLaunchSubagent(Class c) {
        OlePassport oPassport = new OlePassport();
        oPassport.loadPassport(oleConfig.getTab("Identity").getString("Passport file", ""));
        LARVAPayload payload = new LARVAPayload();
        payload.setOlecfg(oleConfig);
        payload.setoPassport(oPassport);
        Object _args[] = new Object[1];
        _args[0] = payload;
        if (oleConfig.getOptions().getOle("Connection").getString("boot", "").equals("microjade")) {
            return baseFactory.fastMicroBirth(c.getSimpleName().substring(0, 1) + getHexaKey(3), c, _args);
        } else {
            return baseFactory.fastBirth(c.getSimpleName().substring(0, 1) + getHexaKey(3), c, _args);
        }

    }

    public boolean doPrepareNPC(int n, Class c) {
        if (baseFactory == null) {
            baseFactory = new BaseFactoryAgent(this);
        }
        boolean res = true;
        for (int i = 0; i < n & res; i++) {
            res = doLaunchSubagent(c);
        }
        return res;
    }

    public void doDestroyNPC() {
//        if (baseFactory == null) {
//            return;
//        }
////        for (String sagent: baseFactory.getAllAgentNames()) {
////            baseFactory.killAgent(sagent);
////        }
////        for (String scontainer: baseFactory.getAllContainerNames()) {
////            baseFactory.killContainer(scontainer);
////        }
        if (baseFactory != null) {
            baseFactory.killAllExit();
            baseFactory = null;
        }
    }

    public void activateSequenceDiagrams() {
        this.allowSequenceDiagrams = true;
    }

    public void deactivateSequenceDiagrams() {
        this.allowSequenceDiagrams = false;
    }

    public boolean isActiveSequenceDiagrams() {
        return this.allowSequenceDiagrams;
    }

    public boolean isContinuousSequenceDiagrams() {
        return this.continuousSequence;
    }

    public void setContinuousSequenceDiagram(Boolean t) {
        this.continuousSequence = t;
    }
}
