/**
 * @author Anatoli.Grishenko@gmail.com
 */
package appboot;

import com.formdev.flatlaf.FlatDarkLaf;
import data.Ole;
import data.OleList;
import data.OleRecord;
import disk.Logger;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import swing.LARVAFrame;
import swing.OleDialog;
import static tools.Internet.getExtIPAddress;
import static tools.Internet.getLocalIPAddress;
import tools.emojis;

/**
 * A basic application launcher that abstracts the launch of Jade and the stop
 * of the associated container
 */
public class LARVABoot {

    protected boolean _connected = false, _echo = false, _debug = false, _quickshutdown = false;
    protected String _title, _subtitile, _version = "1.0";
    Object _args[];
    protected ArrayList<String> _tasks, _achieved;
    protected jade.core.Runtime _runtime;
    protected MicroRuntime _uruntime;
    protected ContainerController _firstContainer, _secondContainer;
    protected Profile _profile;
    protected HashMap<String, AgentController> _controllers;
    protected ArrayList<String> _agentNames;
    protected String _host, _virtualhost, _containerName, _platformId, _username, _password;
    protected final String _lockShutDownFilename = ".DeleteThisToReset.lock", _lockRebootFilename = ".Reboot.lock", _lockWaitFilename = ".Wait.lock", _configFileName = "./config/config.json";
    protected FileWriter _lockCloseSession, _lockReboot;
    protected int _port;
    protected double _progress;
    protected OleRecord config;
    protected String configfilename;
    protected Logger logger;

    protected enum PLATFORM {
        MAGENTIX, JADE, MICROJADE
    }
    PLATFORM _platformType;

    enum Buttons {
        Start, Shutdown, Configure
    };

    protected LARVAFrame fMain;
    protected JScrollPane pScroll;
    protected JTextArea taMessages;
    protected String title;
    protected JPanel pControl;
    protected JPanel pMain;
    protected JButton bStart, bExit, bConfig;
    protected int width = 800, height = 400;
    protected int nlog;
    protected String who, name;
    protected String sResult;
    protected boolean bResult;
    protected String sMessages;
    protected Semaphore sShutdown, sStart;
    protected Ole oleConfig;
    protected OleDialog Settings;
    private String markdowns = "*_`#~";

    /**
     * Main constructor. Initializes the variables and prepare the list of task
     * to complete: Parsing arguments (if any), configure options (if any),
     * connect to JADE and launch agents
     */
    public LARVABoot() {
        _firstContainer = null;
        _containerName = "";
        _controllers = new HashMap<>();
        _agentNames = new ArrayList<>();
        logger = new Logger();
        logger.setEcho(true);
        logger.setOwner("Jade BOOT");
        logger.onTabular();
        _host = "localhost";
        _port = 1099;
        _tasks = new ArrayList<>();
        _tasks.add("ARGUMENTS");
        _tasks.add("CONFIGURE");
        _tasks.add("CONNECT");
        _tasks.add("LAUNCH");
        _achieved = new ArrayList<>();
        _args = new Component[0];
        sShutdown = new Semaphore(0);
        sStart = new Semaphore(0);
        if (new File(_configFileName).exists()) {
            oleConfig = new Ole();
            if (oleConfig.loadFile(_configFileName).isEmpty()) {
                oleConfig = null;
            }
        }

        initGUI();
    }

    protected void initGUI() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        fMain = new LARVAFrame(e -> this.jadebootListener(e));
        pMain = new JPanel();
        BoxLayout pHBox = new BoxLayout(pMain, BoxLayout.Y_AXIS);
        pMain.setLayout(pHBox);
        pMain.setBorder(new EmptyBorder(new Insets(4, 4, 4, 4)));

        taMessages = new JTextArea(100, 100);
        taMessages.setEditable(false);
        taMessages.setWrapStyleWord(true);

        pControl = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pControl.setPreferredSize(new Dimension(width, 32));
        bExit = new JButton(Buttons.Shutdown.name());
        bExit.addActionListener(fMain);
        bStart = new JButton(Buttons.Start.name());
        bStart.addActionListener(fMain);
        bConfig = new JButton(Buttons.Configure.name());
        bConfig.addActionListener(fMain);
        if (this.oleConfig != null) {
            pControl.add(bConfig);
            pControl.add(bStart);
            sStart.drainPermits();
        } else {
            sStart.release();
        }
        pControl.add(bExit);
        pScroll = new JScrollPane(taMessages);
        pScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pScroll.setPreferredSize(new Dimension(width, height - pScroll.getHeight()));
        pMain.add(pScroll);
        pMain.add(pControl);
        fMain.add(pMain);
        fMain.setSize(width, height);
        fMain.setVisible(true);
        fMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fMain.show();

    }

    protected void refreshGUI() {
        title = "LARVA Launcher";
        if (!_achieved.contains("ARGUMENTS")) {
            title = emojis.BLACKCIRCLE + " " + (int) (_progress * 100) + "% " + title + " processing arguments";
        } else if (!_achieved.contains("CONFIGURE")) {
            title = emojis.BLACKCIRCLE + " " + (int) (_progress * 100) + "% " + title + " loading configuration";
        } else if (!_connected) {
            title = emojis.BLACKCIRCLE + " " + (int) (_progress * 100) + "% " + title + " Connecting to JADE";
        } else {
            title = emojis.WHITECIRCLE + " " + title + "[" + this._platformType + "] " + this._host + ":" + this._port;
        }
        fMain.setTitle(title);

//        fMain.revalidate();
        fMain.repaint();
//        taMessages.revalidate();
//        taMessages.repaint();
    }

    /**
     * Starts the booting process and connect to JADE according to the
     * paramenters
     *
     * @param host Where JADE plaform is running
     * @param port Where JADE plaform is running
     * @return The own instance
     */
    public LARVABoot Boot(String host, int port) {
        if (oleConfig == null) {
            return this.selectConnection(host, port);
        } else {
            return Boot();
        }
    }

    /**
     * Starts the booting process and connect to JADE according to the
     * paramenters given in a JSON config file
     *
     * @return The own instance
     */
    public LARVABoot Boot() {
        if (oleConfig == null) {
            this.Abort("Sorry, method Boot() without argumentes requires a configuration fle");
        }
        try {
            sStart.acquire();
        } catch (InterruptedException ex) {
        }
        return this.selectConnection(new OleList(oleConfig.getOle("Jade").getOle("Host")).getItem(0),
                oleConfig.getOle("Jade").getInt("Port"));
    }

    protected LARVABoot doCompleted(String task) {
        if (_tasks.contains(task) && !isCompleted(task)) {
            _achieved.add(task);
            Progress();
        }
        return this;
    }

    protected boolean isCompleted(String task) {
        return _achieved.contains(task);
    }

    protected LARVABoot processArguments() {
        Info("Processing arguments:");
        if (_args.length > 0) {
            for (int i = 0; i < _args.length; i++) {
                switch ((String) _args[i]) {
                    case "-config":
                        if (i + 1 < _args.length) {
                            configfilename = (String) _args[++i];
                        } else {
                            Abort("Error, missing argument in call");
                        }
                        break;
                    case "-silent":
                        logger.setEcho(false);
                        break;
                    default:
                        Abort("Error, missing argument in call");
                }
            }
        } else {

        }
        config = new OleRecord();
        if (configfilename != null && !new File(configfilename).exists()) {
            configfilename = null;
        }
        if (configfilename != null) {
            if (config.loadFile(configfilename).isEmpty()) {
                Abort("Error loading confg file " + configfilename);
            }
        }
        config.setField("rebootjade", this._lockRebootFilename);
        config.setField("shutdownjade", this._lockShutDownFilename);
        doCompleted("ARGUMENTS");
        return this;
    }

    protected LARVABoot Configure() {
        if (!isCompleted("ARGUMENTS")) {
            processArguments();
        }
        Info("Configuring boot:");
        if (configfilename != null) {
            OleRecord cfgbasic = new OleRecord(new Ole(config.getField("basic")));
            if (cfgbasic.getFullFieldList().contains("savelog") && cfgbasic.getBoolean("savelog")) {
                if (cfgbasic.getFullFieldList().contains("logfile")) {
                    logger.setLoggerFileName(cfgbasic.getString("logfile"));
                } else {
                    logger.setLoggerFileName("default_log.json");
                }
                if (cfgbasic.getFullFieldList().contains("host")) {
                    _host = cfgbasic.getString("host");
                }
                if (cfgbasic.getFullFieldList().contains("port")) {
                    _port = cfgbasic.getInt("port");
                }
                if (cfgbasic.getFullFieldList().contains("containername")) {
                    this._containerName = cfgbasic.getString("containername");
                }
            }
            Info("%% BOOTING %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            Info("Loaded config " + configfilename);
        }
        doCompleted("CONFIGURE");
        return this;
    }

    /**
     * Inner method to set a full-p2p Jade connection
     *
     * @param host Host that contains the main container
     * @param port Port
     * @return A reference to the same instance
     */
    protected LARVABoot setupJadeConnection(String host, int port) {
        if (!isCompleted("CONFIGURE")) {
            Configure();
        }
        Info("Trying to connecto to Jade (Boot) @" + host + ":" + port);
        _platformType = PLATFORM.JADE;
        _host = host;
        _port = port;

        try {
            Info("jade.Boot Host " + _host + "["
                    + _port + "] <"
                    + _platformId + ">");
            if (_firstContainer == null) {
                _runtime = jade.core.Runtime.instance();
                _profile = new ProfileImpl();
                if (!_host.equals("")) {
                    _profile.setParameter(Profile.MAIN_HOST, _host);
                }
                if (_port != -1) {
                    _profile.setParameter(Profile.MAIN_PORT, "" + _port);
                }
                if (!_containerName.equals("")) {
                    _profile.setParameter(Profile.CONTAINER_NAME, _containerName);
                }
                _firstContainer = _runtime.createAgentContainer(_profile);
                if (_containerName == null || _containerName.equals("")) {
                    _containerName = _firstContainer.getContainerName();
                }
            }
//            _runtime.setCloseVM(true);
            _connected = true;
            this.refreshGUI();
            Info("Connected to Jade");
        } catch (Exception ex) {
            Abort("Unable to connect:");
        }

        doCompleted("CONNECT");
        return this;
    }

    /**
     * Inner method to set a restricted-p2p Jade connection
     *
     * @param host Host that contains the main container
     * @param port Port
     * @return A reference to the same instance
     */
    protected LARVABoot setupMicroJadeConnection(String host, int port) {
        if (!isCompleted("CONFIGURE")) {
            Configure();
        }
        System.out.println("Trying to connecto to Jade (MicroBoot) @" + host + ":" + port);
        _platformType = PLATFORM.MICROJADE;
        _host = host;
        _port = port;
        _controllers = new HashMap<>();
        _agentNames = new ArrayList<>();

        Info("jade.MicroBoot Host: " + _host + "["
                + _port + "] <"
                + _platformId + ">");
        jade.util.leap.Properties pr = new jade.util.leap.Properties();
        if (!_host.equals("")) {
            pr.setProperty(Profile.MAIN_HOST, _host);
        }
        if (_port != -1) {
            pr.setProperty(Profile.MAIN_PORT, "" + _port);
        }

        MicroRuntime.startJADE(pr, null);
        _containerName = MicroRuntime.getContainerName();
        doCompleted("CONNECT");
        return this;
    }

    /**
     * Analyzes the inet connection and sets upa a LARVABoot or jade.Microboot
     * conection, the most appropriate one
     *
     * @param host The target host
     * @param port The target port
     * @return A reference to the same instance
     */
    protected LARVABoot selectConnection(String host, int port) {

        if (!isCompleted("CONFIGURE")) {
            Configure();
        }
        _host = host;
        _port = port;
        if (isBehindRouter()) {
            return setupMicroJadeConnection(host, port);
        } else {
            return setupJadeConnection(host, port);
        }
    }

    protected LARVABoot selectConnection() {

        if (!isCompleted("CONFIGURE")) {
            Configure();
        }

        return selectConnection(_host, _port);
    }

    /**
     * Given a class c which inherits, either directly or not, from JADE Agent
     * it opens a container (the same for all launched agents) and launch the
     * agent
     *
     * @param name Name of the agent. It must be unique
     * @param c Class which extends Agent
     * @return The own instance
     */
    public LARVABoot launchAgent(String name, Class c) {
        /// Issue #2 https://github.com/Anatoli-Grishenko/es.ugr.larva.core/issues/2
        name=this.clearMarkDowns(name);
        Info("Launching agent " + name);
        if (!isCompleted("CONNECT")) {
            Abort("Please configure the connection first");
        }
        AgentController ag;
        _agentNames.add(name);
        _args = new Object[3];
        _args[0] = fMain;
        _args[1] = this.pScroll;
        _args[2] = this.taMessages;
        if (isMicroBoot()) {
            try {
                MicroRuntime.startAgent(name, c.getName(), _args);
                ag = MicroRuntime.getAgent(name);
                _controllers.put(name, ag);
            } catch (Exception ex) {
                Error("Error creating Agent " + name);
                Exception(ex);
            }
        } else {
            try {
                ag = _firstContainer.createNewAgent(name, c.getName(), _args);
                ag.start();
                _controllers.put(name, ag);
            } catch (Exception e) {
                Error("Error creating Agent " + name);
                Exception(e);
                ag = null;
            }
        }

        doCompleted("LAUNCH");
        return this;
    }

    protected LARVABoot Progress() {
        _progress = _achieved.size() * 1.0 / _tasks.size();
        refreshGUI();
        return this;
    }

    protected void Info(String s) {
        logger.logMessage(s);
        taMessages.append(logger.getLastlog()); //logger.getLastlog());
        refreshGUI();
        if (_debug) {
            Alert(s);
        }
    }

    protected void Error(String s) {
        logger.logError(s);
        taMessages.append(logger.getLastlog()); //logger.getLastlog());
        Alert(s);
        refreshGUI();
    }

    protected void Exception(Exception ex) {
        logger.logException(ex);
        taMessages.append(logger.getLastlog());
        refreshGUI();
    }

    /**
     * This method waits until all launched agents are dead. 
     * @return The own instance
     */
    public LARVABoot WaitToClose() {
        this._quickshutdown=true;
        Info("Waiting for agents to close");
        while (!isEmpty() && !isShutDown()) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
        return this;
    }
//    public LARVABoot WaitToClose() {
//        boolean somealive;
//        String alive;
//        Info("Waiting for agents to close");
//        do {
//            alive = "";
//            somealive = false;
//            for (String sname : _agentNames) {
//                String name = "" + sname;
//                try {
//                    if (isMicroBoot()) {
//                        somealive = MicroRuntime.size() > 0;
//                    } else {
//                        _firstContainer.getAgent(name);
//                        somealive = true;
//                    }
//                    alive += name + ". ";
//                } catch (Exception ex) {
//                    _controllers.remove(name);
////                    _agentNames.remove(name);
//                }
//            }
//            if (somealive) {
//                try {
//                    Thread.sleep(2500);
//                } catch (Exception e) {
//                }
//            }
//        } while (somealive && !this.sShutdown.tryAcquire());
//        return this;
//    }

    /**
     * It immediately kills all agents which could have been running in the predefined
     * container without waiting for them to exit properly
     *
     * @return The own instance
     */
    protected LARVABoot Close() {
        // Kill all agents
        try {
            this.sShutdown.acquire();
        } catch (Exception ex) {
        };
        Info("Killing all remaining agents");
        this._achieved.remove("LAUNCH");
        AgentController agc;
        for (String name : _agentNames) {
            try {
                if (isMicroBoot()) {
                    agc = MicroRuntime.getAgent(name);
                } else {
                    agc = _firstContainer.getAgent(name);
                }
                agc.kill();
                _controllers.remove(name);
            } catch (Exception ex) {
            }
        }
        return this;
    }

    /**
     * The container of the agent is killed and the
     * application exits
     *
     * @return The own instance
     */
    public LARVABoot ShutDown() {
        Info("Shutting down");
        Info("Turning off JadeBoot");
        if (!this._quickshutdown) {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
            }
        }
        turnOff(_firstContainer);
        fMain.dispatchEvent(new WindowEvent(fMain, WindowEvent.WINDOW_CLOSING));
//        System.exit(0);
        return this;
    }

    /**
     * It kills all agents which could have been running in the predefined
     * container. Once all agents have been terminated, the container is killed
     * and the application exits
     *
     * @return The own instance
     */
    public LARVABoot WaitToShutDown() {
        this._quickshutdown = false; //true;
        Close();
        ShutDown();
        return this;
    }

    protected void turnOff(ContainerController container) {
        Info("Shutting down container " + _containerName);
        try {
            if (isMicroBoot()) {
                MicroRuntime.stopJADE();
            } else {
                try {
                    container.kill();
                } catch (Exception ex) {
//                Exception(ex);
                }
            }
        } catch (Exception ex) {
//                Exception(ex);
        }
        Info("Container " + _containerName + " shut down");
    }

    protected void doSwingLater(Runnable what) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                what.run();
            });
        } else {
            what.run();
        }
    }

    protected void doSwingWait(Runnable what) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    what.run();
                });
            } catch (Exception ex) {
            }
        } else {
            what.run();
        }
    }

    protected void jadebootListener(ActionEvent e) {
        if (e.getActionCommand().equals(Buttons.Shutdown.name())) {
            if (Confirm("Kill all agents and exit?")) {
                this.sShutdown.release();
            }
        }
        if (e.getActionCommand().equals(Buttons.Start.name())) {
            sStart.release();
            this.bConfig.setEnabled(false);
            this.bStart.setEnabled(false);
            if (Settings != null) {
                oleConfig = Settings.getDialogResult();
                if (oleConfig != null && !oleConfig.isEmpty()) {
                    oleConfig.saveAsFile("./config/", "config.json");
                }
            }
        }
        if (e.getActionCommand().equals(Buttons.Configure.name())) {
            Settings = new OleDialog(this.fMain, oleConfig);
        }
    }

    public void Alert(String message) {
        JOptionPane.showMessageDialog(this.fMain,
                message, "LARVA Boot", JOptionPane.INFORMATION_MESSAGE);
    }

    public String inputLine(String message) {
        sResult = JOptionPane.showInputDialog(this.fMain, message, "LARVA Boot", JOptionPane.QUESTION_MESSAGE);
        return sResult;
    }

    public String inputSelect(String message, String[] options, String value) {
        String res = (String) JOptionPane.showInputDialog(null, message, "LARVA Boot", JOptionPane.QUESTION_MESSAGE, null, options, value);
        return res;
    }

    public boolean Confirm(String message) {
        bResult = JOptionPane.showConfirmDialog(this.fMain,
                message, "LARVA Boot", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        return bResult;
    }

    protected boolean isJade() {
        return (_platformType == PLATFORM.JADE
                || _platformType == PLATFORM.MICROJADE);
    }

    protected boolean isMicroBoot() {
        return _platformType == PLATFORM.MICROJADE;
    }

    public boolean isShutDown() {
        return this.sShutdown.availablePermits() > 0;
    }

    public boolean isEmpty() {
        boolean somealive = false;

        if (isMicroBoot()) {
            try {
                somealive = MicroRuntime.size() > 0;
            } catch (Exception Ex) {
            }
        } else {
            for (String sname : _agentNames) {
                try {
                    _firstContainer.getAgent(sname);
                    somealive = true;
                } catch (Exception ex) {
                    _controllers.remove(name);
                }
                if (somealive) {
                    break;
                }
            }
        }
        return !somealive;
    }

    protected void Abort(String s) {
        Error(s);
        Exit();
    }

    protected void Exit() {
        Info("AppBoot exiting");
        ShutDown();
    }

    protected boolean isBehindRouter() {
        return !_host.equals("localhost")
                && !getExtIPAddress().equals(getLocalIPAddress());
    }

    public JFrame getMyFrame() {
        return fMain;
    }

    public JScrollPane getMyPane() {
        return pScroll;
    }

    protected JTextArea getMessages() {
        return taMessages;
    }

    protected boolean isDebug() {
        return _debug;
    }

    protected void setDebug(boolean _debug) {
        this._debug = _debug;
    }
    protected String clearMarkDowns(String original) {
        String res= "";
        for (int i=0; i<original.length(); i++) {
            if (markdowns.contains(""+original.charAt(i))) {
                res+=".";
            }
            else 
                res += ""+original.charAt(i);
        }
        return res;
    }
}
