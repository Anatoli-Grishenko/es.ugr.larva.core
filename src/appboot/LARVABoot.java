/**
 * @author Anatoli.Grishenko@gmail.com
 */
package appboot;

import agents.AgentReport;
import agents.LARVAPayload;
import agents.XUIAgent;
import com.eclipsesource.json.JsonObject;
import static crypto.Keygen.getHexaKey;
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import swing.LARVAFrame;
import swing.OleAgentTile;
import swing.OleApplication;
import swing.OleButton;
import swing.OleDialog;
import swing.OleDrawPane;
import swing.OleFoldableList;
import swing.OleToolBar;
import swing.SwingTools;
import static tools.Internet.getExtIPAddress;
import static tools.Internet.getLocalIPAddress;
import tools.emojis;

/**
 * A basic application launcher that abstracts the launch of Jade and the stop
 * of the associated container
 */
public class LARVABoot {

    public static final int METAL = 0;
    public static final int LIGHT = 1;
    public static final int CLASSIC = 2;

    protected boolean _connected = false, _echo = false, _debug = false, _quickshutdown = false;
    protected String _title, _subtitile, _version = "1.0", _xuiName;
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
    protected final String _lockShutDownFilename = ".DeleteThisToReset.lock", _lockRebootFilename = ".Reboot.lock", _lockWaitFilename = ".Wait.lock";
    String _configFileName,
            _appConfiguration, _background, _logoUgr;
    protected FileWriter _lockCloseSession, _lockReboot;
    protected int _port, _style;
    protected double _progress;
    protected Logger logger;

    protected enum PLATFORM {
        MAGENTIX, JADE, MICROJADE
    }
    PLATFORM _platformType;

    enum Buttons {
        Start, Shutdown, Configure
    };

    protected JScrollPane pScroll;

    protected OleApplication appMain;
    OleConfig app;
    OlePassport oPassport;
    OleFoldableList pTiles;
    JPanel _pServerTiles, _XUI, _seqDiagram;
    JScrollPane psTiles;
    HashMap<String, Component> shareableGUI;
    protected JTextArea taMessages, taSequence;
    protected JTabbedPane tabbedPane;

    protected int nlog;
    protected String who, name;
    protected String sResult;
    protected boolean bResult, showPerformance;
    protected String sMessages;
    protected Semaphore sShutdown, doSwing, doJade;
    protected OleConfig oleConfig;
    private String markdowns = "*_`#~";

    /**
     * Main constructor. Initializes the variables and prepare the list of task
     * to complete: Parsing arguments (if any), configure options (if any),
     * connect to JADE and launch agents
     */
    public LARVABoot() {
        initGUI(METAL);
    }

    public LARVABoot(int s) {
        initGUI(s);
    }

    protected void initGUI(int style) {
        shareableGUI = new HashMap();
        _style = style % 3;
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
        doSwing = new Semaphore(1);
        doJade = new Semaphore(1);
        String saux;
        switch (_style) {
            case METAL: {
//                String saux2 = "";
//                try {
//                    JarFile jf = new JarFile("dist/lib/es.ugr.larva.core.jar");
//                    JarEntry je = jf.getJarEntry("resources/config/MetalBoot.app");
//                    if (je == null) {
//                        System.out.println("NO ");
//                    } else {
//                        InputStream is = jf.getInputStream(je);
//                        System.out.println("YES "+je.getName());
//                    }
//                } catch (IOException ex) {
//                    System.out.println("ABSOLUTE NO");
//                }
//                String saux3="jar:"+getClass().getResource("/resources/config/MetalBoot.app").toString();
//                System.out.println("--->"+saux3);
//                saux2=getClass().getResource("/resources/config/MetalBoot.app").toString().replace("file:", "");
                this._appConfiguration = "/resources/config/MetalBoot.app";
            }
            break;

            case LIGHT:
                this._appConfiguration = "/resources/config/LightBoot.app";
                break;
            case CLASSIC:
            default:
                this._appConfiguration = "/resources/config/ClassicBoot.app";

        }
        this._configFileName = "./config/Configuration.conf";
        app = new OleConfig();
        app.loadFile(this._appConfiguration);
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
        appMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appMain.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                sShutdown.release();
            }
        });
        pTiles = new OleFoldableList(appMain);
//        pTiles.setMinimumSize(new Dimension(200,120));
        pTiles.setPreferredSize(new Dimension(150, 1000));
        psTiles = new JScrollPane(pTiles);
        psTiles.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        psTiles.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Local agents", psTiles);
        appMain.getMainPanel().add(tabbedPane, BorderLayout.LINE_START);

        tabbedPane = new JTabbedPane();
        taMessages = new JTextArea();
        taMessages.setEditable(false);
        taMessages.setWrapStyleWord(true);
        Font f = appMain.getFont();
        f = new Font(Font.MONOSPACED, Font.PLAIN, f.getSize());
        taMessages.setFont(f);
        pScroll = new JScrollPane(taMessages);
        pScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        OleToolBar otbAux = new OleToolBar(this.appMain, 12);
        OleButton obAux;
        JPanel jpAux;
        obAux = new OleButton(this.appMain, "Save activity", "save_alt");
        obAux.setFlat();
        obAux.setIcon();
//        obAux.setIcon(new Dimension(20, 20));
//        obAux.setExtraFlat();
        otbAux.addButton(obAux);
        obAux = new OleButton(this.appMain, "Clean activity", "delete");
        obAux.setFlat();
        obAux.setIcon();
//        obAux.setIcon(new Dimension(20, 20));
//        obAux.setExtraFlat();
        otbAux.addButton(obAux);

        jpAux = new JPanel();
        jpAux.setLayout(new BorderLayout());
        jpAux.add(otbAux, BorderLayout.NORTH);
        jpAux.add(pScroll, BorderLayout.CENTER);

        tabbedPane.addTab("Activity log", jpAux);
        shareableGUI.put("Activity log", taMessages);
        _pServerTiles = new JPanel();
        _pServerTiles.setLayout(new FlowLayout(FlowLayout.LEFT));
        pScroll = new JScrollPane(_pServerTiles);
        pScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tabbedPane.addTab("Server agents", pScroll);
        shareableGUI.put("Server agents", _pServerTiles);

        taSequence = new JTextArea();
        taSequence.setEditable(false);
        taSequence.setWrapStyleWord(true);
        f = appMain.getFont();
        f = new Font(Font.MONOSPACED, Font.PLAIN, f.getSize());
        taSequence.setFont(f);
        pScroll = new JScrollPane(taSequence);
        pScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        otbAux = new OleToolBar(this.appMain, 12);
        obAux = new OleButton(this.appMain, "Save sequence", "save_alt");
        obAux.setFlat();
        obAux.setIcon();
//        obAux.setIcon(new Dimension(20, 20));
//        obAux.setExtraFlat();
        otbAux.addButton(obAux);
        obAux = new OleButton(this.appMain, "Clean sequence", "delete");
        obAux.setFlat();
        obAux.setIcon();
//        obAux.setIcon(new Dimension(20, 20));
//        obAux.setExtraFlat();
        otbAux.addButton(obAux);
        jpAux = new JPanel();
        jpAux.setLayout(new BorderLayout());
        jpAux.add(otbAux, BorderLayout.NORTH);
        jpAux.add(pScroll, BorderLayout.CENTER);

        tabbedPane.addTab("Sequence", jpAux);
        shareableGUI.put("Sequence", taSequence);

        _XUI = new JPanel();
        _XUI.setLayout(new BorderLayout());
        pScroll = new JScrollPane(_XUI);
        pScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tabbedPane.addTab("XUI", pScroll);
        shareableGUI.put("XUI", _XUI);
        appMain.getMainPanel().add(tabbedPane, BorderLayout.CENTER);
        appMain.setSize(new Dimension(app.getOptions().getOle("FrameSize").getInt("width", 640),
                app.getOptions().getOle("FrameSize").getInt("height", 480)));
        showStatus();
        if (new File(_configFileName).exists()) {
            oleConfig = new OleConfig();

            if (oleConfig.loadFile(_configFileName).isEmpty()) {
                appMain.Error("Error loading configuration file " + _configFileName);
                Exit();
            }
            if (oleConfig.getTab("Identity").getBoolean("Autoload", false)) {
                loadPassport();
            }
            if (oleConfig.getTab("Connection").getBoolean("Autoconnect", false)) {
                Boot();
            }
            if (this.oleConfig.getTab("Identity").getBoolean("Open XUI", false)) {
                tabbedPane.setSelectedIndex(3);
            } else {
                tabbedPane.setSelectedIndex(0);
            }

        } else {
            for (String s : appMain.getToolBar().getButtonList()) {
                if (!s.toUpperCase().equals("EXIT")) {
                    appMain.getToolBar().getButton(s).setVisible(false);
                }
            }
        }
        showStatus();
    }

    void appListener(ActionEvent e) {
//        System.out.println("Listener  waiting");
        try {
            this.doJade.acquire(1);
        } catch (Exception ex) {

        }
//        System.out.println("Listener  working");
        if (e.getActionCommand().equals("Exit")) {
            if (appMain.Confirm("Kill all agents and exit?")) {
//                this.Exit();
                this.sShutdown.release();
            }
        }
        if (e.getActionCommand().equals("Passport")) {
            loadPassport();
        }
        if (e.getActionCommand().equals("Clean activity")) {
            this.taMessages.setText("");
            this.taMessages.validate();
        }
        if (e.getActionCommand().equals("Save activity")) {
            String toSave = taMessages.getText(), filename;
            filename = OleDialog.doSelectFile("./", "txt");
            if (filename != null) {
                try {
                    PrintStream outP = new PrintStream(new File(filename));
                    outP.println(toSave);
                    SwingTools.Info("Sequence diagram saved");
                } catch (FileNotFoundException ex) {
                    this.Error("Error saving activity log");
                }
            }
        }
        if (e.getActionCommand().equals("Clean sequence")) {
            this.taSequence.setText("");
            this.taSequence.validate();
        }
        if (e.getActionCommand().equals("Save sequence")) {
            String toSave = taSequence.getText(), filename;
            filename = OleDialog.doSelectFile("./", "txt");
            if (filename != null) {
                try {
                    PrintStream outP = new PrintStream(new File(filename));
                    outP.println(toSave);
                    SwingTools.Info("Activity log saved");
                } catch (FileNotFoundException ex) {
                    this.Error("Error saving activity log");
                }
            }
        }
        if (e.getActionCommand().equals("Connect")) {
            this.Boot();
        }
        if (e.getActionCommand().equals("Options")) {
            OleDialog oOptions = new OleDialog(appMain, "Boot options");
            if (oOptions.run(oleConfig)) {
                oleConfig = oOptions.getResult();
                oleConfig.saveAsFile("./", this._configFileName, true);
                if (oleConfig.getTab("Identity").getString("Passport file", "").length() > 0) {
                    loadPassport();
                }
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
        if (e.getActionCommand().startsWith("Configure ")) {
            OleConfig problemCfg = new OleConfig();
            problemCfg.loadFile("config/Problems.conf");
            SwingTools.doSwingWait(() -> {
                OleDialog odOptions = new OleDialog(this.appMain, "Please choose problem options");
                if (odOptions.run(problemCfg)) {
                    odOptions.getResult().saveAsFile("./config/", "Problems.conf", true);
                }
            });
            problemCfg.loadFile("config/Problems.conf");

        }
//        System.out.println("Listener  ending");
        this.doJade.release(1);
    }

    protected void loadPassport() {
        oPassport = new OlePassport();
        oPassport.loadPassport(oleConfig.getTab("Identity").getString("Passport file", ""));
        if (oPassport.isEmpty()) {
            appMain.Error("Error loading passport file " + oleConfig.getTab("Identity").getString("Pasport file", ""));
        } else {
            _xuiName = "XUI" + oPassport.getName(); //+getHexaKey(4);
//            _xuiName = _xuiName.substring(0, 10);
            for (String s : _tiles.keySet()) {
                if (_tiles.get(s) != null) {
                    _tiles.get(s).getMyReport().setOwnerName(oPassport.getName());
                    _tiles.get(s).updateReport();
                }
            }
        }
        showStatus();
    }

    protected void showStatus() {
        appMain.cleanStatus();
        if (_connected) {
//            appMain.addStatus(emojis.PLUG, OleApplication.DodgerBlue);
            appMain.addStatus(appMain.getIconSet().getHighlightIcon("link", 20, 20));
            appMain.addStatus(_host);
            appMain.addStatus("(" + _port + ")");
        } else {
            appMain.addStatus(appMain.getIconSet().getInactiveIcon("link_off", 20, 20));
//            appMain.addStatus(emojis.PLUG, OleApplication.Maroon);
            appMain.addStatus(" No connection yet");
        }
        if (oPassport != null && !oPassport.isEmpty()) {
            appMain.addStatus(appMain.getIconSet().getHighlightIcon("account_circle", 20, 20));
//            appMain.addStatus(emojis.ID, OleApplication.DodgerBlue);
            appMain.addStatus(oPassport.getName());
        } else {
            appMain.addStatus(appMain.getIconSet().getInactiveIcon("account_circle", 20, 20));
//            appMain.addStatus(emojis.ID, OleApplication.Maroon);
            appMain.addStatus(" No passport yet");
        }
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
        appMain.showProgress("Booting Jade", 0, 10);
        this.selectConnection(host, port);
        showStatus();
        if (_connected) {
//            appMain.getToolBar().getButton("Passport").setEnabled(false);
            appMain.getToolBar().getButton("Connect").setEnabled(false);
        }
        return this;
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
        appMain.showProgress("Booting Jade", 0, 10);
        this.selectConnection();
        showStatus();
        if (_connected) {
//            appMain.getToolBar().getButton("Passport").setEnabled(false);
            appMain.getToolBar().getButton("Connect").setEnabled(false);
//            appMain.getToolBar().getButton("Options").setEnabled(false);
        }
        return this;
    }

    protected LARVABoot doCompleted(String task) {
        if (_tasks.contains(task) && !isCompleted(task)) {
            _achieved.add(task);
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
        doCompleted("ARGUMENTS");
        return this;
    }

    protected LARVABoot Configure() {
        if (!isCompleted("ARGUMENTS")) {
            processArguments();
        }
        appMain.showProgress("Applying configuration");
        appMain.showProgress("Configuring log activity");
        if (oleConfig != null) {
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
            Info("Applied config " + this._configFileName);
        }
        Info("%% BOOTING %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
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
        Ole oconnect = new Ole();
        oconnect.setField("host",_host);
        oconnect.setField("port",_port);
        oconnect.setField("boot", "jade");
        oleConfig.getOptions().add("Connection", oconnect);
        oleConfig.saveAsFile("config/", "Configuration.conf", true);

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
//        System.out.println("Trying to connecto to Jade (MicroBoot) @" + host + ":" + port);
        _platformType = PLATFORM.MICROJADE;
        _host = host;
        _port = port;
        _controllers = new HashMap<>();
        _runningAgents = new ArrayList<>();
        Ole oconnect = new Ole();
        oconnect.setField("host",_host);
        oconnect.setField("port",_port);
        oconnect.setField("boot", "microjade");
        oleConfig.getOptions().add("Connection", oconnect);
        oleConfig.saveAsFile("config/", "Configuration.conf", true);

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
        _connected = true;
        Info("Connected to Jade");
        appMain.showProgress("Connected to JADE");
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
        if (oleConfig == null) {
            this.Abort("Sorry, method Boot() without argumentes requires a configuration fle");
        }
        if (!isCompleted("CONFIGURE")) {
            Configure();
        }
        selectConnection(_host, _port);
        appMain.closeProgress(who);
        return this;
    }

    public LARVABoot loadAgent(String name, Class c) {
        AgentReport ag = new AgentReport(name, c, 100);
        if (oPassport != null) {
            ag.setOwnerName(oPassport.getName());
        }
        OleAgentTile otAux;
        if (new File("config/Problems.conf").exists()) {
            otAux = new OleAgentTile(appMain, ag, true);
        } else {
            otAux = new OleAgentTile(appMain, ag, false);
        }
        otAux.updateReport();
        pTiles.addFoldable(otAux);
        otAux.showSummary();
        pTiles.validate();
        _tiles.put(name, otAux);
        shareableGUI.put("TILE " + name, otAux);
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
            _tiles.get(name).doDeactivateAgent();
            Info("Agent " + name + " deactivated");
        } catch (Exception ex) {
//            appMain.Error("Error stoping agent " + name);
        }
        return this;
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
    synchronized public LARVABoot launchAgent(String name, Class c) {
        /// Issue #2 https://github.com/Anatoli-Grishenko/es.ugr.larva.core/issues/2
        name = this.clearMarkDowns(name);
        Info("Launching agent " + name + " " + c.getName());
        if (!isCompleted("CONNECT")) {
            Boot();
        }
        if (_tiles.get(name) == null) {
            loadAgent(name, c);
        }

        AgentController ag;
        LARVAPayload payload = new LARVAPayload();
        payload.setOlecfg(oleConfig);
        payload.setParent(appMain);
        payload.setoPassport(oPassport);
        payload.setMyReport(_tiles.get(name).getMyReport());
        payload.setGuiComponents(shareableGUI);
        _args = new Object[1];
        _args[0] = payload;
        if (isMicroBoot()) {
            try {
                MicroRuntime.startAgent(name, c.getName(), _args);
                ag = MicroRuntime.getAgent(name);
                _controllers.put(name, ag);
                _runningAgents.add(name);
                _tiles.get(name).doActivateAgent();
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
                _runningAgents.add(name);
                _tiles.get(name).doActivateAgent();
                showStatus();
            } catch (Exception e) {
                Error("Error creating Agent " + name);
                Exception(e);
                ag = null;
                _tiles.get(name).doActivate();
            }
        }
        if (oPassport != null && _tiles.get(_xuiName) == null && this.oleConfig.getTab("Identity").getBoolean("Open XUI", false)) {
            launchAgent(_xuiName, XUIAgent.class);
        }
        return this;
    }

    protected void Info(String s) {
        logger.logMessage(s);
        taMessages.append(logger.getLastlog()); //logger.getLastlog());
        taMessages.setCaretPosition(Math.max(taMessages.getText().lastIndexOf("\n"), 0));
    }

    protected void Error(String s) {
        logger.logError(s);
        taMessages.append(logger.getLastlog()); //logger.getLastlog());
        appMain.Error(s);
    }

    protected void Exception(Exception ex) {
        logger.logException(ex);
        taMessages.append(logger.getLastlog());
    }

    public void WaitToShutDown() {
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
//                    System.out.println(s+"**"+_tiles.get(s).getMyReport().getLastCycle());
                    _tiles.get(s).updateReportReadings();
                } catch (Exception ex) {
                    Info("Agent " + s + " has died");
                    _tiles.get(s).doDeactivateAgent();
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
//        System.out.println("Manager exiting");
        ShutDown();
        System.exit(0);
    }

    /**
     * The container of the agent is killed and the application exits
     *
     * @return The own instance
     */
    public LARVABoot ShutDown() {
        Info("Shutting down");
        Info("Turning off JadeBoot");
//        if (!this._quickshutdown) {
//            try {
//                Thread.sleep(5000);
//            } catch (Exception e) {
//            }
//        }
        turnOff(_firstContainer);
        appMain.dispose();
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
                }
            }
        } catch (Exception ex) {
        }
        Info("Container " + _containerName + " shut down");
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

//    public JScrollPane getMyPane() {
//        return pScroll;
//    }
    protected JTextArea getMessages() {
        return taMessages;
    }

    protected boolean isDebug() {
        return _debug;
    }

    protected void setDebug(boolean _debug) {
        this._debug = _debug;
    }

    public void Alert(String message) {
        appMain.Warning(message);
    }

    public String inputLine(String message) {
        return appMain.inputLine(message);
    }

    public String inputSelect(String message, String[] options, String value) {
        return appMain.inputSelect(message, options, value);
    }

    public boolean Confirm(String message) {
        return appMain.Confirm(message);
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
