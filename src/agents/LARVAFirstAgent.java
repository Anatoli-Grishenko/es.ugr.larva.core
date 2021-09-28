/**
 * @file LARVAFirstAgent.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package agents;

import data.Ole;
import data.OleList;
import data.Transform;
import swing.LARVAFrame;
import disk.Logger;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import messaging.ACLMessageTools;
import static messaging.ACLMessageTools.getAllReceivers;
import messaging.SequenceDiagram;
import swing.LARVADash;
import swing.LARVADash.Layout;

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
public class LARVAFirstAgent extends LARVABaseAgent {

    // JFrame from launcher
    protected LARVAFrame myFrame;
    protected JPanel myPane, myMap;
    protected JScrollPane myScrPane;
    protected JTextArea myText;
    protected LARVADash myDashboard;
    private ACLMessage checkin, checkout;
    private String IdentityManager;

    // Its known sequence diagram
    SequenceDiagram sd;
    //
    protected String title, mySessionmanager = "", problemName;

    protected OleList stepsDone, stepsSent;
    protected boolean traceRunSteps;
    protected Ole oleConfig;


    /**
     * Main JADE setup
     */
    @Override
    public void setup() {
        stepsDone = new OleList();
        stepsSent = new OleList();
        sd = new SequenceDiagram();
        traceRunSteps = false;
        super.setup();
        addRunStep("MILES00");
        addRunStep("MILES01");
        this.logger.setEcho(true);
        // create a new frame to store text field and button
        if (this.getArguments() != null && this.getArguments().length > 1) {
            doSwingWait(() -> {
                myText = (JTextArea) this.getArguments()[2];
                myScrPane = (JScrollPane) this.getArguments()[1];
                myFrame = (LARVAFrame) this.getArguments()[0];
            });
            doSwingLater(() -> {
                myFrame.show();
                this.refreshGUI();
            });
        }
        if (new File("./config/config.json").exists()) {
            Info("Found a configuration file");
            oleConfig = new Ole();
            if (oleConfig.loadFile("./config/config.json").isEmpty()) {
                oleConfig = null;
            } else {
//                problemName = new OleList(oleConfig.getOle("LARVA").getOle("Problem")).getItem(0);
                problemName = (String) oleConfig.getOle("LARVA")
                        .getOle("Problem")
                        .getArray("items").get(0);
                Ole record = oleConfig.getOle("Jade");
                logger.setEcho(!record.getBoolean("Silent execution"));
                if (record.getBoolean("Save log")) {
                    String logfile = record.getField("Log file");
                    if (logfile.length() == 0) {
                        logfile = "./auditLog.json";
                    }
                    logger.setLoggerFileName(logfile);
                }
            }
        }
        myDashboard = new LARVADash(Layout.DASHBOARD, this);
    }

    /**
     * It detects if the ouput is graphical (swing) or text (console)
     *
     * @return true when SWING is active, false otherwise
     */
    protected boolean isSwing() {
        return this.myFrame != null;
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
    protected String[] getConfiguredSensors() {
        String res[] = new String[0];
        ArrayList<String> sensorList = new ArrayList();
        if (oleConfig != null && !oleConfig.isEmpty()) {
            Ole sensors = oleConfig.getOle("LARVA").getOle("Sensors");
            for (String sensor : sensors.getNetFieldList()) {
                if (sensors.getBoolean(sensor)) {
                    sensorList.add(sensor.toUpperCase());
                }
            }
            res = Transform.toArray(sensorList);
        }
        return res;
    }

    @Override
    public void takeDown() {
        if (traceRunSteps) {
            addRunStep("MILES03");
        }
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
            refreshGUI();
            JOptionPane.showMessageDialog(null,
                    logger.getLastlog(), "Agent " + getLocalName(),
                    JOptionPane.ERROR_MESSAGE);
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
        if (traceRunSteps) {
            addRunStep("MILES02");
        }
        logger.logMessage(message);
        if (isSwing() && logger.isEcho()) {
            myText.append(logger.getLastlog());
            myText.setCaretPosition(Math.max(myText.getText().lastIndexOf("\n"), 0));
            refreshGUI();
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
                checkout = checkin.createReply();
                if (checkin.getPerformative() == ACLMessage.CONFIRM) {
                    checkedin = true;
                    Info(checkin.getContent());
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
        if (myDashboard != null && msg.getContent() != null
                && (msg.getContent().toUpperCase().contains("REQUEST JOIN")
                || (msg.getContent().toUpperCase().contains("QUERY SENSOR")))) {
            msg = ACLMessageTools.addDashMark(msg);
        }
        this.send(msg);
        Info("⬜ Sending ACLM " + ACLMessageTools.fancyWriteACLM(msg, true));
        sd.addSequence(msg.getSender().getLocalName(),
                getAllReceivers(msg), msg.getContent());
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
        boolean repeat = false;
        do {
            res = blockingReceive();
            if (res != null && ACLMessageTools.isDashACL(res)) {
                res = ACLMessageTools.cleanDashMark(res);
                repeat = myDashboard.preProcessACLM(res);
            } else {
                repeat = false;
            }
        } while (repeat);
        Info("⬛ Received ACLM " + ACLMessageTools.fancyWriteACLM(res, true));
        sd.addSequence(res.getSender().getLocalName(),
                getAllReceivers(res), res.getContent());
        this.checkReceivedMessage(res);
        return res;
    }

    private ACLMessage LARVAblockingReceive(long milis) {
        ACLMessage res;
        boolean repeat = false;
        if (traceRunSteps) {
            addRunStep("MILES13");
        }
        do {
            res = blockingReceive(milis);
            if (res != null && ACLMessageTools.isDashACL(res)) {
                repeat = myDashboard.preProcessACLM(res);
            } else {
                repeat = false;
            }
        } while (repeat);
        if (res != null) {
//            Info("⬛ Received ACLM " + ACLMessageTools.fancyWriteACLM(res, true));
        }
        this.checkReceivedMessage(res);
        return res;
    }

    private ACLMessage LARVAblockingReceive(MessageTemplate t) {
        ACLMessage res;
        boolean repeat = false;
        if (traceRunSteps) {
            addRunStep("MILES13");
        }
        do {
            res = blockingReceive(t);
            if (res != null && ACLMessageTools.isDashACL(res)) {
                repeat = myDashboard.preProcessACLM(res);
            } else {
                repeat = false;
            }
        } while (repeat);
        if (res != null) {
//            Info("⬛ Received ACLM " + ACLMessageTools.fancyWriteACLM(res, true));
        }
        this.checkReceivedMessage(res);
        return res;
    }

    protected ACLMessage LARVAblockingReceive(MessageTemplate t, long milis) {
        ACLMessage res;
        boolean repeat = false;
        if (traceRunSteps) {
            addRunStep("MILES13");
        }
        do {
            res = blockingReceive(t, milis);
            if (res != null && ACLMessageTools.isDashACL(res)) {
                repeat = myDashboard.preProcessACLM(res);
            } else {
                repeat = false;
            }
        } while (repeat);
        if (res != null) {
            Info("⬛ Received ACLM " + ACLMessageTools.fancyWriteACLM(res, true));
            sd.addSequence(res.getSender().getLocalName(),
                    getAllReceivers(res), res.getContent());
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
            String res = JOptionPane.showInputDialog(null, message, "Agent " + getLocalName(), JOptionPane.QUESTION_MESSAGE);
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
            String res = (String) JOptionPane.showInputDialog(null, message, "Agent " + getLocalName(), JOptionPane.QUESTION_MESSAGE, null, options, value);
            return res;
        } else {
            return super.inputLine(message);
        }
    }

    protected void refreshGUI() {
        doSwingLater(() -> {
            myFrame.repaint();
        });
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
     * @param l the type of dashboard, initially only DASHBOARD is allowed
     */
    public void doActivateLARVADash() {
        myDashboard.setActivated(true);
        myDashboard.initGUI();
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
            stepsSent = new OleList();
        }
    }

    private String getSequenceDiagram() {
        return sd.printSequenceDiagram();
    }
    
    public void saveSequenceDiagram(String filename) {
        try {
            PrintStream out= new PrintStream(new File(filename));
            out.println(getSequenceDiagram());
        } catch (FileNotFoundException ex) {
            Error("Unable to save Sequence Diagram into file "+filename);
        }
    }

}
