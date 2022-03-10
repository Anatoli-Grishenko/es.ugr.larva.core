/**
 * @author Anatoli.Grishenko@gmail.com
 */
package appboot;

import com.formdev.flatlaf.FlatDarkLaf;
import data.Ole;
import data.OleConfig;
import data.OlePassport;
import disk.Logger;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
import swing.OleAgentTile;
import swing.OleApplication;
import swing.OleDialog;
import swing.OleDrawPane;
import swing.OleScrollPane;
import swing.SwingTools;
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
    protected HashMap<String, OleAgentTile> _tiles;
    protected ArrayList<String> _runningAgents, _launchingAgents, _stoppingAgents;
    protected String _host, _virtualhost, _containerName, _platformId, _username, _password;
    protected final String _lockShutDownFilename = ".DeleteThisToReset.lock", _lockRebootFilename = ".Reboot.lock", _lockWaitFilename = ".Wait.lock",
            _configFileName = "./config/Configuration.conf",
            _appConfiguration = "./config/LARVABoot.app";
    protected FileWriter _lockCloseSession, _lockReboot;
    protected int _port;
    protected double _progress;
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
    protected String title;
    protected JPanel pControl;
    protected JPanel pMain;
    protected JButton bStart, bExit, bConfig;

    protected OleApplication appMain;
    OleConfig app;
    OlePassport oPassport;
    JPanel pTiles;
    JScrollPane psTiles;
    protected JTextArea taMessages;

    protected int nlog;
    protected String who, name;
    protected String sResult;
    protected boolean bResult;
    protected String sMessages;
    protected Semaphore sShutdown, sStart, doSwing, doJade;
    protected OleConfig oleConfig;
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
        _tiles = new HashMap<>();
        _runningAgents = new ArrayList<>();
        _launchingAgents = new ArrayList<>();
        _stoppingAgents = new ArrayList<>();
        logger = new Logger();
        logger.setEcho(true);
        logger.setOwner("Jade BOOT");
        logger.onTabular();
        _tasks = new ArrayList<>();
        _tasks.add("ARGUMENTS");
        _tasks.add("CONFIGURE");
        _tasks.add("CONNECT");
        _tasks.add("LAUNCH");
        _achieved = new ArrayList<>();
        _args = new Component[0];
        sShutdown = new Semaphore(0);
        sStart = new Semaphore(0);
        doSwing = new Semaphore(1);
        doJade = new Semaphore(1);
        initGUI();
    }

    protected void initGUI() {
        app = new OleConfig();
        app.loadFile(_appConfiguration);
        if (app.isEmpty()) {
            System.err.println("Missing configuration file " + _appConfiguration);
            System.exit(1);
        }
        appMain = new OleApplication(app) {
            @Override
            public void Draw(Graphics2D g) {
            }

            @Override
            public void myActionListener(ActionEvent e) {
                appListener(e);
            }

            @Override
            public void myKeyListener(KeyEvent e) {
            }
        };
        appMain.getMainPanel().removeAll();
        appMain.getMainPanel().setLayout(new BorderLayout());
        pTiles = new JPanel();
        pTiles.setPreferredSize(new Dimension(100, 100));
        pTiles.setLayout(new FlowLayout(FlowLayout.CENTER));
//        pTiles.setLayout(new BoxLayout(pTiles,BoxLayout.Y_AXIS));
        psTiles = new JScrollPane(pTiles);
        psTiles.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        taMessages = new JTextArea();
        taMessages.setEditable(false);
        taMessages.setWrapStyleWord(true);
        pScroll = new JScrollPane(taMessages);
        pScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        appMain.getMainPanel().add(psTiles, BorderLayout.LINE_START);
        appMain.getMainPanel().add(pScroll, BorderLayout.CENTER);
        showStatus();
        if (new File(_configFileName).exists()) {
            oleConfig = new OleConfig();

            if (oleConfig.loadFile(_configFileName).isEmpty()) {
                appMain.Error("Error loading configuration file " + _configFileName);
                Exit();
            }
        }
        if (oleConfig.getTab("Identity").getBoolean("Autoload", false)) {
            loadPassport();
        }
        if (oleConfig.getTab("Connection").getBoolean("Autoconnect", false)) {
            Boot();
        }
        showStatus();
    }

    void appListener(ActionEvent e) {
//        System.out.println("Listener  waiting");
        try {
            Thread.sleep(1000);
            this.doJade.acquire(1);
        } catch (Exception ex) {

        }
//        System.out.println("Listener  working");
        if (e.getActionCommand().equals("Exit")) {
            if (appMain.Confirm("Kill all agents and exit?")) {
                this.sShutdown.release();
            }
        }
        if (e.getActionCommand().equals("Passport")) {
            loadPassport();
        }
        if (e.getActionCommand().equals("Connect")) {
            this.Boot();
            showStatus();
        }
        if (e.getActionCommand().equals("Options")) {
            OleDialog oOptions = new OleDialog(appMain, "Boot options");
            if (oOptions.run(oleConfig)) {
                oleConfig = oOptions.getResult();
                oleConfig.saveAsFile("./", this._configFileName, true);
            }
        }
        if (e.getActionCommand().startsWith("Activate ")) {
            String name = e.getActionCommand().split((" "))[1];
            _launchingAgents.add(name);
//            launchAgent(name, _tiles.get(name).getClass());
        }
        if (e.getActionCommand().startsWith("Deactivate ")) {
            String name = e.getActionCommand().split((" "))[1];
            _stoppingAgents.add(name);
//            stopAgent(name);
        }
//        System.out.println("Listener  ending");
        this.doJade.release(1);
    }

    protected void loadPassport() {
        oPassport = new OlePassport();
        oPassport.loadPassport(oleConfig.getTab("Identity").getString("Passport file", ""));
        if (oPassport.isEmpty()) {
            appMain.Error("Error loading passport file " + oleConfig.getTab("Identity").getString("Pasport file", ""));
        }
        showStatus();
    }

    protected void showStatus() {
        appMain.cleanStatus();
        if (_connected) {
            appMain.addStatus(emojis.PLUG, OleApplication.DodgerBlue);
            appMain.addStatus(oleConfig.getTab("Connection").getString("Hostname", ""));
            appMain.addStatus("(" + oleConfig.getTab("Connection").getInt("Port", -1) + ")");
        } else {
            appMain.addStatus(emojis.PLUG, OleApplication.Maroon);
            appMain.addStatus(" No connection yet");
        }
        if (oPassport != null && !oPassport.isEmpty()) {
            appMain.addStatus(emojis.ID, OleApplication.DodgerBlue);
            appMain.addStatus(oPassport.getName());
        } else {
            appMain.addStatus(emojis.ID, OleApplication.Maroon);
            appMain.addStatus(" No passport yet");
        }
//        String status=emojis.PLUG;
//        if (_connected) {
//            status += " "+oleConfig.getTab("Connection").getString("Hostname", "");
//            status += "("+oleConfig.getTab("Connection").getInt("Port", -1)+")";
//        } else {
//            status +=" NO CONNECTION";
//        }
//        status += "     "+emojis.ID;
//        if (oPassport != null && !oPassport.isEmpty()) {
//            status += " "+oPassport.getName();
//        } else {
//            status += " NO PASSPORT";
//        }
//        appMain.showStatus(status);
    }

    //    protected void initGUI() {
    //        try {
    //            UIManager.setLookAndFeel(new FlatDarkLaf());
    //        } catch (Exception ex) {
    //            System.err.println("Failed to initialize LaF");
    //        }
    //
    //        fMain = new LARVAFrame(e -> this.jadebootListener(e));
    //        pMain = new JPanel();
    //        BoxLayout pHBox = new BoxLayout(pMain, BoxLayout.Y_AXIS);
    //        pMain.setLayout(pHBox);
    //        pMain.setBorder(new EmptyBorder(new Insets(4, 4, 4, 4)));
    //
    //        taMessages = new JTextArea(100, 100);
    //        taMessages.setEditable(false);
    //        taMessages.setWrapStyleWord(true);
    //
    //        pControl = new JPanel(new FlowLayout(FlowLayout.CENTER));
    //        pControl.setPreferredSize(new Dimension(width, 32));
    //        bExit = new JButton(Buttons.Shutdown.name());
    //        bExit.addActionListener(fMain);
    //        bStart = new JButton(Buttons.Start.name());
    //        bStart.addActionListener(fMain);
    //        bConfig = new JButton(Buttons.Configure.name());
    //        bConfig.addActionListener(fMain);
    //        if (this.oleConfig != null) {
    //            pControl.add(bConfig);
    //            pControl.add(bStart);
    //            sStart.drainPermits();
    //        } else {
    //            sStart.release();
    //        }
    //        pControl.add(bExit);
    //        taMessages = new JTextArea(100, 100);
    //        taMessages.setEditable(false);
    //        taMessages.setWrapStyleWord(true);
    //        pScroll = new JScrollPane(taMessages);
    //        pScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    //        pScroll.setPreferredSize(new Dimension(width, height - pScroll.getHeight()));
    //        pMain.add(pScroll);
    //        pMain.add(pControl);
    //        fMain.add(pMain);
    //        fMain.setSize(width, height);
    //        fMain.setVisible(true);
    //        fMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //        fMain.show();
    ////        Settings=new OleDialog("Configuration");
    //    }
    protected void refreshGUI() {
//        title = "LARVA Launcher";
//        if (!_achieved.contains("ARGUMENTS")) {
//            title = emojis.BLACKCIRCLE + " " + (int) (_progress * 100) + "% " + title + " processing arguments";
//        } else if (!_achieved.contains("CONFIGURE")) {
//            title = emojis.BLACKCIRCLE + " " + (int) (_progress * 100) + "% " + title + " loading configuration";
//        } else if (!_connected) {
//            title = emojis.BLACKCIRCLE + " " + (int) (_progress * 100) + "% " + title + " Connecting to JADE";
//        } else {
//            title = emojis.WHITECIRCLE + " " + title + "[" + this._platformType + "] " + this._host + ":" + this._port;
//        }
//        fMain.setTitle(title);
//
////        fMain.revalidate();
//        fMain.repaint();
////        taMessages.revalidate();
////        taMessages.repaint();
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
//        try {
//            sStart.acquire();
//        } catch (InterruptedException ex) {
//        }
        appMain.showProgress("Booting Jade", 0, 10);
        return this.selectConnection();
//        return this.selectConnection(oleConfig.getTab("Connection").getField("Hostname"),
//                oleConfig.getTab("Conection").getInt("Port"));
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
        appMain.showProgress("Processing arguments");
        if (_args.length > 0) {
            for (int i = 0; i < _args.length; i++) {
                switch ((String) _args[i]) {
                    case "-silent":
                        logger.setEcho(false);
                        break;
                    default:
                        Abort("Error, missing argument in call");
                }
            }
        } else {

        }
        oleConfig.setField("rebootjade", this._lockRebootFilename);
        oleConfig.setField("shutdownjade", this._lockShutDownFilename);
        doCompleted("ARGUMENTS");
        return this;
    }

    protected LARVABoot Configure() {
        if (!isCompleted("ARGUMENTS")) {
            processArguments();
        }
        appMain.showProgress("Applying configuration");
        appMain.showProgress("Configuring log activity");
        Ole cfgbasic = oleConfig.getTab("Log activity");
        if (cfgbasic.getBoolean("Overwrite log", false)) {
            logger.onOverwrite();
        } else {
            logger.onAppend();
        }
        if (cfgbasic.getBoolean("Silent", false)) {
            logger.offEcho();
        } else {
            logger.onEcho();
        }
        if (cfgbasic.getBoolean("Save log", false)) {
            logger.setLoggerFileName(cfgbasic.getString("File log", "./default.json"));
        }
        appMain.showProgress("Configuring JADE host");
        _host = oleConfig.getTab("Connection").getField("Hostname");
        _port = oleConfig.getTab("Connection").getInt("Port");
        Info("%% BOOTING %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        Info("Applied config " + this._configFileName);
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
            appMain.showProgress("Connected to JADE");
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
        _runningAgents = new ArrayList<>();

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

        selectConnection(_host, _port);
        appMain.closeProgress(who);
        return this;
    }

    public LARVABoot loadAgent(String name, Class c) {
        OleAgentTile otAux = new OleAgentTile(appMain, name, c);
        pTiles.add(otAux);
        otAux.showSummary();
        pTiles.validate();
        _tiles.put(name, otAux);
        return this;
    }

    public LARVABoot stopAgent(String name) {
        AgentController agc;
        try {
            if (isMicroBoot()) {
                agc = MicroRuntime.getAgent(name);
            } else {
                agc = _firstContainer.getAgent(name);
            }
            agc.kill();
            _controllers.remove(name);
            _tiles.get(name).doDeactivate();
            Info("Agent " + name + " deactivated");
        } catch (Exception ex) {
//            appMain.Error("Error stoping agent " + name);
        }
        return this;
    }

//    public LARVABoot checkAgent(String name) {
//        AgentController agc;
//        try {
//            if (isMicroBoot()) {
//                agc = MicroRuntime.getAgent(name);
//            } else {
//                agc = _firstContainer.getAgent(name);
//            }
//        } catch (Exception ex) {
//            appMain.Info("Agent " + name + " has died");
//            stopAgent(name);
////            _controllers.remove(name);
////            this._runningAgents.remove(name);
////            _tiles.get(name).doDeactivate();
//        }
//        return this;
//    }

    /**
     * Given a class c which inherits, either directly or not, from JADE Agent
     * it opens a container (the same for all launched agents) and launch the
     * agent
     *
     * @param name Name of the agent. It must be unique
     * @param c Class which extends Agent
     * @return The own instance
     */
    synchronized public LARVABoot launchAgent(String name, Class c) {
        /// Issue #2 https://github.com/Anatoli-Grishenko/es.ugr.larva.core/issues/2
        name = this.clearMarkDowns(name);
        Info("Launching agent " + name+" "+c.getName());
        if (!isCompleted("CONNECT")) {
            Boot();
        }
        if (_tiles.get(name) == null) {
            loadAgent(name, c);
        }
        AgentController ag;
        _args = new Object[3];
        _args[0] = appMain;
        _args[1] = this.pScroll;
        _args[2] = this.taMessages;
        if (isMicroBoot()) {
            try {
                MicroRuntime.startAgent(name, c.getName(), _args);
                ag = MicroRuntime.getAgent(name);
                _controllers.put(name, ag);
                _tiles.get(name).doActivate();
                showStatus();
            } catch (Exception ex) {
                Error("Error creating Agent " + name);
                Exception(ex);
            }
        } else {
            try {
                ag = _firstContainer.createNewAgent(name, c.getName(), _args);
                ag.start();
                _controllers.put(name, ag);
                _tiles.get(name).doActivate();
                showStatus();
            } catch (Exception e) {
                Error("Error creating Agent " + name);
                Exception(e);
                ag = null;
                _tiles.get(name).doActivate();
            }
        }
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
//        taMessages.append(logger.getLastlog()); //logger.getLastlog());
        appMain.Error(s);
        refreshGUI();
    }

    protected void Exception(Exception ex) {
        logger.logException(ex);
//        taMessages.append(logger.getLastlog());
        refreshGUI();
    }

    /**
     * This method waits until all launched agents are dead.
     *
     * @return The own instance
     */
//    public LARVABoot WaitToClose() {
//        while (!isEmpty() && !isShutDown()) {
//            for (String name : _runningAgents) {
//                checkAgent(name);
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (Exception e) {
//            }
//        }
//        return this;
//    }
//    public LARVABoot WaitToClose() {
//        boolean somealive;
//        String alive;
//        Info("Waiting for agents to close");
//        do {
//            alive = "";
//            somealive = false;
//            for (String sname : _runningAgents) {
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
////                    _runningAgents.remove(name);
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

    public void waitToClose() {
        boolean exit = false;
        do {
//            System.out.println("Manager waiting");
            try {
                this.doJade.acquire(1);
            } catch (Exception ex) {

            }
//            System.out.println("Manager working");
            String name;
            while (!_launchingAgents.isEmpty()) {
                name = _launchingAgents.get(0);
                _runningAgents.add(name);
                _launchingAgents.remove(0);
                launchAgent(name, _tiles.get(name).getMyClass());
            }
            AgentController agc;
            ArrayList<String> diedAgents = new ArrayList();
            for (String s : _runningAgents) {
                try {
                    if (isMicroBoot()) {
                        agc = MicroRuntime.getAgent(s);
                    } else {
                        agc = _firstContainer.getAgent(s);
                    }
                } catch (Exception ex) {
                    Info("Agent " + s + " has died");
                    _tiles.get(s).doDeactivate();
                    diedAgents.add(s);
                }
            }
            _runningAgents.removeAll(diedAgents);
            while (!_stoppingAgents.isEmpty()) {
                name = _stoppingAgents.get(0);
                _stoppingAgents.remove(0);
                stopAgent(name);
            }
            if (isShutDown()) {
                _stoppingAgents.addAll(_runningAgents);
                _runningAgents.clear();
                _launchingAgents.clear();
            }
            if (isShutDown() && _launchingAgents.isEmpty() && _runningAgents.isEmpty() && _stoppingAgents.isEmpty()) {
                exit = true;
            }
            this.doJade.release(1);
//            System.out.println("Manager ending");
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {

            }
        } while (!exit);
        System.out.println("Manager exiting");
        ShutDown();
    }

    /**
     * It immediately kills all agents which could have been running in the
     * predefined container without waiting for them to exit properly
     *
     * @return The own instance
     */
//    protected LARVABoot Close() {
//        // Kill all agents
//        try {
//            this.sShutdown.acquire();
//        } catch (Exception ex) {
//        };
//        Info("Killing all remaining agents");
//        this._achieved.remove("LAUNCH");
//        AgentController agc;
//        for (String name : _runningAgents) {
//            stopAgent(name);
//        }
//        return this;
//    }

    /**
     * The container of the agent is killed and the application exits
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
        appMain.dispose();
        return this;
    }

    /**
     * It kills all agents which could have been running in the predefined
     * container. Once all agents have been terminated, the container is killed
     * and the application exits
     *
     * @return The own instance
     */
//    public LARVABoot WaitToShutDown() {
//        this._quickshutdown = false; //true;
//        Close();
//        ShutDown();
//        return this;
//    }

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

    protected void jadebootListener(ActionEvent e) {
//        if (e.getActionCommand().equals(Buttons.Shutdown.name())) {
//            if (Confirm("Kill all agents and exit?")) {
//                this.sShutdown.release();
//            }
//        }
//        if (e.getActionCommand().equals(Buttons.Start.name())) {
//            sStart.release();
//            this.bConfig.setEnabled(false);
//            this.bStart.setEnabled(false);
//        }
//        if (e.getActionCommand().equals(Buttons.Configure.name())) {
//            Settings = new OleDialog(this.fMain, "Settings");
//            if (Settings.run(oleConfig)) {
//                oleConfig.saveAsFile("./config/", "config.json", true);
//            }
//
//        }
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
            for (String sname : _runningAgents) {
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
        appMain.Error(s);
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
        String res = "";
        for (int i = 0; i < original.length(); i++) {
            if (markdowns.contains("" + original.charAt(i))) {
                res += ".";
            } else {
                res += "" + original.charAt(i);
            }
        }
        return res;
    }
}
