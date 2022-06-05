/**
 * @file LARVAFirstAgent.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package agents;

import Environment.Environment;
import ai.Choice;
import ai.DecisionSet;
import console.Console;
import data.Ole;
import data.OleConfig;
import data.OleFile;
import data.OleSet;
import data.Transform;
import disk.Logger;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import messaging.ACLMessageTools;
import messaging.SequenceDiagram;
import swing.OleAgentTile;
import swing.OleApplication;
import swing.OleButton;
import swing.OleToolBar;

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
 * and an associated boolean variable to control the exit and, therefore, the
 * death of the agent.
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
    private ACLMessage checkin, checkout;
    private String IdentityManager;

    protected int userID = -1;
    protected String userName = "";

    // Its known sequence diagram
    SequenceDiagram sd;
    //
    protected String title, mySessionmanager = "", problemName;

    protected OleSet stepsDone, stepsSent;
    protected boolean traceRunSteps;
    protected OleConfig oleConfig;
    protected AgentReport myReport;
    protected LARVAPayload payload;

    protected Environment E;
    protected DecisionSet A;
    protected String myMission[], missionName;

    protected Semaphore SWaitButtons;
    protected boolean cont = true, each = true, remote = false;
    OleAgentTile externalTile;
    OleToolBar externalTB;
    OleButton olbContinue, olbPause, olbNext, olbUntil;
    protected int nUntil, iUntil = 0, frameDelay = 0;
    protected boolean showConsole = false, showRemote = false;

    public double Reward(Environment E) {
        return E.getDistance();
    }

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
            if (Va(E, a)) {
                a.setUtility(U(T(E, a)));
            } else {
                a.setUtility(Choice.MAX_UTILITY);
            }
        }
        A.sort();
        return A;
    }

    protected Environment T(Environment E, Choice a) {
        if (!Ve(E)) {
            return null;
        } else {
            return E.simmulate(a);
        }
    }

    protected double U(Environment E) {
        if (!Ve(E)) {
            return Choice.MAX_UTILITY;
        } else if (E.getOntarget()) {
            return -1000;
        } else {
            return Reward(E);
        }
    }

    protected boolean Va(Environment E, Choice a) {
        return true;
    }

    protected boolean Ve(Environment E) {
        if (E == null || E.isCrahsed() || E.getStuck()>3) {
            return false;
        }
        return true;

    }

    protected boolean G(Environment E) {
        if (!Ve(E)) {
            return false;
        }
        return E.isOverMission();
    }

    /**
     * Main JADE setup
     */
    @Override
    public void setup() {
        stepsDone = new OleSet();
        stepsSent = new OleSet();
        sd = new SequenceDiagram();
        traceRunSteps = false;
        super.setup();
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
        }
        SWaitButtons = new Semaphore(0);
        E = new Environment();
        if (showRemote) {
            openRemote();
        }
    }

    @Override
    public void postExecute() {
        myReport.tick();
        if (this.frameDelay > 0 && (!remote || cont)) {
            try {
                Thread.sleep(this.frameDelay);
            } catch (InterruptedException ex) {
            }
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
        if (traceRunSteps) {
            addRunStep("MILES03");
        }
        if (this.SWaitButtons.availablePermits() == 0) {
            this.SWaitButtons.release();
        }
        if (remote) {
            closeRemote();
        }
        if (problemName != null) {
            this.saveSequenceDiagram(problemName + ".seqd");
        } 
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
        if (traceRunSteps) {
            addRunStep("MILES02");
        }
        logger.logError(message);
        if (isSwing()) {
            myText.append(logger.getLastlog());
            myText.setCaretPosition(Math.max(myText.getText().lastIndexOf("\n"), 0));
            myApp.Error(message);
        }
    }

    /**
     * Log a common message. It is sent of Stdout. When the echo is not active,
     * it does not show anything on screen.
     *
     * @param message The informative message
     */
    protected void Info(String message) {
        if (traceRunSteps) {
            addRunStep("MILES02");
        }
        logger.logMessage(message);
        if (isSwing() && logger.isEcho()) {
            myText.append(logger.getLastlog() + "");
//            myText.setCaretPosition(Math.max(myText.getText().lastIndexOf("\n"), 0));
            myText.setCaretPosition(Math.max(myText.getText().length(), 0));
        }
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
            if (traceRunSteps) {
                addRunStep("MILES20");
            }
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
            Error("Sorry, no identity manager service has been found");
        } else {
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
                if (traceRunSteps) {
                    addRunStep("MILES20");
                }
                checkout = checkin.createReply();
                if (checkin.getPerformative() == ACLMessage.CONFIRM) {
                    checkedin = true;
                    Info(checkin.getContent());
                    this.getUserData(checkin.getContent());
                    return true;
                } else if (checkin.getPerformative() == ACLMessage.REFUSE) {
                    Error("Check in to LARVA refused.\nDetails: " + checkin.getContent());
                } else {
                    Error("Could not check in to LARVA.\nDetails: " + checkin.getContent());
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
        if (traceRunSteps) {
            addRunStep("MILES10");
        }
//        if (myDashboard != null && msg.getContent() != null
        if (msg.getOntology() != null && msg.getOntology().toUpperCase().equals("COMMITMENT")) {
            String skey = msg.getConversationId(), sman;
            if (skey != null && !this.DFGetAllProvidersOf("SESSION MANAGER " + skey).isEmpty()) {
//                ACLMessage aux = new ACLMessage(msg.getPerformative());
//                aux.setSender(msg.getSender());
//                aux.setConversationId(msg.getConversationId());
//                aux.setReplyWith(msg.getReplyWith());
//                aux.setInReplyTo(msg.getInReplyTo());
//                aux.setOntology(msg.getOntology());
//                aux.setProtocol(msg.getProtocol());
                sman = this.DFGetAllProvidersOf("SESSION MANAGER " + skey).get(0);
                msg.addReceiver(new AID(sman, AID.ISLOCALNAME));
//                aux.addReceiver(new AID(sman, AID.ISLOCALNAME));
//                this.send(aux);
//                Info("⬜ Sending ACLM " + ACLMessageTools.fancyWriteACLM(aux, false));
//                sd.addSequence(aux);
            }
        }
//        if (msg.getContent() != null
//                && (msg.getContent().toUpperCase().contains("REQUEST JOIN")
//                || (msg.getContent().toUpperCase().contains("QUERY SENSOR")))) {
//            msg = ACLMessageTools.addDashMark(msg);
//        }
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
            if (res != null && res.getContent().contains("filedata")) {
                Ole ocontent = new Ole().set(res.getContent());
                OleFile ofile = new OleFile(ocontent.getOle("surface"));
                int maxlevel = ocontent.getInt("maxflight");
                E.setWorldMap(ofile.toString(), maxlevel);
                if (!getLocalName().startsWith("XUI")) {
                    repeat = true;
                }
            }
            if (res != null && res.getContent().contains("perceptions")) {
                E.feedPerception(res.getContent());
                repeat = false;
            }
        } while (repeat);
        Info("⭕< Received ACLM " + ACLMessageTools.fancyWriteACLM(res, false));

        sd.addSequence(res);

        this.checkReceivedMessage(res);

        myReport.setInBox(myReport.getInBox() + 1);
        return res;
    }

    private ACLMessage LARVAblockingReceive(long milis) {
        ACLMessage res;
        boolean repeat = false;
        if (traceRunSteps) {
            addRunStep("MILES13");
        }
        do {
            repeat = false;
            res = blockingReceive();
            if (res != null && res.getContent().contains("filedata")) {
                Ole ocontent = new Ole().set(res.getContent());
                OleFile ofile = new OleFile(ocontent.getOle("surface"));
                int maxlevel = ocontent.getInt("maxflight");
                E.setWorldMap(ofile.toString(), maxlevel);
                if (!getLocalName().startsWith("XUI")) {
                    repeat = true;
                }
            }
            if (res != null && res.getContent().contains("perceptions")) {
                E.feedPerception(res.getContent());
                repeat = false;
            }
        } while (repeat);
        if (res != null) {
//            Info("⬛ Received ACLM " + ACLMessageTools.fancyWriteACLM(res, true));
        }
        this.checkReceivedMessage(res);
        return res;
    }

    public ACLMessage LARVAblockingReceive(MessageTemplate t) {
        ACLMessage res;
        boolean repeat = false;
        if (traceRunSteps) {
            addRunStep("MILES13");
        }
        do {
            repeat = false;
            res = blockingReceive();
            if (res != null && res.getContent().contains("filedata")) {
                Ole ocontent = new Ole().set(res.getContent());
                OleFile ofile = new OleFile(ocontent.getOle("surface"));
                int maxlevel = ocontent.getInt("maxflight");
                E.setWorldMap(ofile.toString(), maxlevel);
                if (!getLocalName().startsWith("XUI")) {
                    repeat = true;
                }
            }
            if (res != null && res.getContent().contains("perceptions")) {
                E.feedPerception(res.getContent());
                repeat = false;
            }
        } while (repeat);
        if (res != null) {
            Info("⭕< Received ACLM " + ACLMessageTools.fancyWriteACLM(res, true));
        }
        this.checkReceivedMessage(res);
        sd.addSequence(res);
        return res;
    }

    protected ACLMessage LARVAblockingReceive(MessageTemplate t, long milis) {
        ACLMessage res;
        boolean repeat = false;
        if (traceRunSteps) {
            addRunStep("MILES13");
        }
        do {
            repeat = false;
            res = blockingReceive();
            if (res != null && res.getContent().contains("filedata")) {
                Ole ocontent = new Ole().set(res.getContent());
                OleFile ofile = new OleFile(ocontent.getOle("surface"));
                int maxlevel = ocontent.getInt("maxflight");
                E.setWorldMap(ofile.toString(), maxlevel);
                if (!getLocalName().startsWith("XUI")) {
                    repeat = true;
                }
            }
            if (res != null && res.getContent().contains("perceptions")) {
                E.feedPerception(res.getContent());
                repeat = false;
            }
        } while (repeat);
        if (res != null) {
            Info("⭕< Received ACLM " + ACLMessageTools.fancyWriteACLM(res, true));
            sd.addSequence(res);
        }
        this.checkReceivedMessage(res);
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
        if (traceRunSteps) {
            addRunStep("MILES21");
        }
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
        if (traceRunSteps) {
            addRunStep("MILES21");
        }

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
        if (traceRunSteps) {
            addRunStep("MILES21");
        }
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
        if (traceRunSteps) {
            addRunStep("MILES21");
        }
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
        if (traceRunSteps) {
            addRunStep("MILES21");
        }
        return super.DFHasService(agentName, service);
    }

    private void addRunStep(String step) {
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

    private String getSequenceDiagram() {
        return sd.printSequenceDiagram();
    }

    public void saveSequenceDiagram(String filename) {
        try {
            PrintStream out = new PrintStream(new File(filename));
            out.println(getSequenceDiagram());
            JTextArea taSeq = (JTextArea) this.payload.getGuiComponents().get("Sequence");
            if (taSeq != null) {
                taSeq.append(filename);
                taSeq.append(getSequenceDiagram());
                taSeq.validate();
            }
        } catch (FileNotFoundException ex) {
            Error("Unable to save Sequence Diagram into file " + filename);
        }
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
    }

    protected void closeRemote() {
        externalTB.removeAll();
    }

    protected void openRemote() {
        this.cont = false;
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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "CONTINUE":
                this.SWaitButtons.release();
                this.cont = true;
                this.olbContinue.setEnabled(false);
                this.olbNext.setEnabled(false);
                this.olbPause.setEnabled(true);
                this.olbUntil.setEnabled(false);
                break;
            case "PAUSE":
                this.SWaitButtons.release();
                this.cont = false;
                this.olbContinue.setEnabled(true);
                this.olbNext.setEnabled(true);
                this.olbPause.setEnabled(false);
                this.olbUntil.setEnabled(true);
                break;
            case "NEXT":
                this.olbContinue.setEnabled(true);
                this.olbNext.setEnabled(true);
                this.olbPause.setEnabled(false);
                this.olbUntil.setEnabled(true);
                this.SWaitButtons.release();
                break;
            case "UNTIL":
                try {
                nUntil = Integer.parseInt(this.inputLine("Please execute the agent until it reachesthe following steps"));
            } catch (Exception ex) {
                nUntil = -1;
            }
            this.cont = true;
            this.SWaitButtons.release();
            break;
        }
    }

}
