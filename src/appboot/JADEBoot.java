/**
 *
 * @author lcv
 */
package appboot;

import data.Ole;
import data.OleConfig;
import disk.Logger;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.System.in;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import static tools.Internet.getExtIPAddress;
import static tools.Internet.getLocalIPAddress;

/**
 * A basic application launcher that abstracts the launch of Jade and the stop
 * of the associated container
 */
public class JADEBoot {

    protected boolean _connected = false, _echo = false;
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
    protected final String _lockShutDownFilename = ".DeleteThisToReset.lock", 
            _lockRebootFilename = ".Reboot.lock", _lockWaitFilename = ".Wait.lock";
    protected FileWriter _lockCloseSession, _lockReboot;
    protected int _port;
    protected double _progress;
    protected OleConfig config;
    protected String configfilename;
    protected Logger logger;

    protected enum PLATFORM {
        MAGENTIX, JADE, MICROJADE
    }
    PLATFORM _platformType;

    public JADEBoot() {
        _firstContainer = null;
        _containerName = "";
        _controllers = new HashMap<>();
        _agentNames = new ArrayList<>();
        logger = new Logger();
        logger.setEcho(true);
        logger.setOwner("Jade BOOT");
        _host = "localhost";
        _port = 1099;
        initBoot(new String[0]);
    }

    public JADEBoot(String[] args) {
        String arguments[];
        if (args.length == 0) {
            arguments = new String[]{"-config", "./LARVA/config/config.json"};
        } else {
            arguments = args;
        }
        _firstContainer = null;
        _containerName = "";
        _controllers = new HashMap<>();
        _agentNames = new ArrayList<>();
        logger = new Logger();
        logger.setEcho(true);
        logger.setOwner("Jade BOOT");
        _host = "localhost";
        _port = 1099;
        initBoot(arguments);
    }

    protected void initBoot(String[] args) {
        _args = args;
        _tasks = new ArrayList<>();
        _tasks.add("ARGUMENTS");
        _tasks.add("CONFIGURE");
        _tasks.add("CONNECT");
        _tasks.add("LAUNCH");
        _achieved = new ArrayList<>();
    }

    public JADEBoot Boot(String host, int port) {
        return this.selectConnection(host, port);
    }

//    public JADEBoot Boot(String cfgfile) {
//        configfilename=cfgfile;
//        return this.selectConnection();
//    }
    public JADEBoot Boot() {
        return this.selectConnection();
    }

    public JADEBoot Reboot() {
        this.activateLocks();
        return this;
    }

    protected JADEBoot Progress() {
        _progress = _achieved.size() * 1.0 / _tasks.size();
        Info("AppBoot v" + _version + "  " + (int) (_progress * 100) + "%% Completed");
        return this;
    }

    protected JADEBoot doCompleted(String task) {
        if (_tasks.contains(task) && !isCompleted(task)) {
            _achieved.add(task);
            Progress();
        }
        if (task.equals("CONNECT")) {
            this.activateLocks();
        }
//        Info("Press INTRO to continue");
//        new Scanner(in).nextLine();
        return this;
    }

    protected boolean isCompleted(String task) {
        return _achieved.contains(task);
    }

    protected JADEBoot processArguments() {
        Info("Processing arguments:");
        if (_args.length > 0) {
            for (int i = 0; i < _args.length; i++) {
                switch ((String)_args[i]) {
                    case "-config":
                        if (i + 1 < _args.length) {
                            configfilename = (String)_args[++i];
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
        config = new OleConfig();
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

    protected JADEBoot Configure() {
        if (!isCompleted("ARGUMENTS")) {
            processArguments();
        }
        Info("Configuring boot:");
        if (configfilename != null) {
            OleConfig cfgbasic = new OleConfig(config.getOle("basic"));
            if (cfgbasic.getFieldList().contains("savelog") && cfgbasic.getBoolean("savelog")) {
                if (cfgbasic.getFieldList().contains("logfile")) {
                    logger.setLoggerFileName(cfgbasic.getString("logfile"));
                } else {
                    logger.setLoggerFileName("default_log.json");
                }
                if (cfgbasic.getFieldList().contains("host")) {
                    _host = cfgbasic.getString("host");
                }
                if (cfgbasic.getFieldList().contains("port")) {
                    _port = cfgbasic.getInt("port");
                }
                if (cfgbasic.getFieldList().contains("containername")) {
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
    protected JADEBoot setupJadeConnection(String host, int port) {
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
            Info("Connected to Jade");
            _connected = true;
        } catch (Exception ex) {
            Abort("Unable to connect:" + ex.toString());
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
    protected JADEBoot setupMicroJadeConnection(String host, int port) {
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
     * Analyzes the inet connection and sets upa a JADEBoot or jade.Microboot
 conection, the most appropriate one
     *
     * @param host The target host
     * @param port The target port
     * @return A reference to the same instance
     */
    protected JADEBoot selectConnection(String host, int port) {

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

    protected JADEBoot selectConnection() {

        if (!isCompleted("CONFIGURE")) {
            Configure();
        }

        return selectConnection(_host, _port);
//        if (isBehindRouter()) {
//            return setupMicroJadeConnection(_host, _port);
//        } else {
//            return setupJadeConnection(_host, _port);
//        }
    }

    /**
     * This is a method to launch an Agent and pass it the command line
     * arguments. Its use has been abandoned and kept only for compatibility
     * with old agents. Pleas use ConsoleBoot.launchagent instead
     *
     * @param name The name that will be given to the agent
     * @param c The class name wihch it belongs to
     * @return A reference to the same instance
     */
    public JADEBoot launchAgent(String name, Class c) {
        Info("Launching agent " + name);
        if (!isCompleted("CONNECT")) {
            Abort("Please configure the connection first");
        }
        AgentController ag;
        _agentNames.add(name);
        if (configfilename != null) {
            _args = new String[]{config.toString()};
        } else {
            _args = new String[0];
        }
        if (isMicroBoot()) {
            try {
                MicroRuntime.startAgent(name, c.getName(), _args);
                ag = MicroRuntime.getAgent(name);
                _controllers.put(name, ag);
            } catch (Exception ex) {
                Error("ERROR CREATING AGENT " + name);
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

    public JADEBoot launchAgentContainer(String containerName, String agentName, Class c) {
        Info("Launching agent " + agentName + " in container " + containerName);
        if (!isCompleted("CONNECT")) {
            Abort("Please configure the connection first");
        }
        if (_secondContainer == null) {
            _runtime = jade.core.Runtime.instance();
            _profile = new ProfileImpl();
            if (!_host.equals("")) {
                _profile.setParameter(Profile.MAIN_HOST, _host);
            }
            if (_port != -1) {
                _profile.setParameter(Profile.MAIN_PORT, "" + _port);
            }
            if (!_containerName.equals("")) {
                _profile.setParameter(Profile.CONTAINER_NAME, containerName);
            }
            _secondContainer = _runtime.createAgentContainer(_profile);
        }
        AgentController ag;
        if (configfilename != null) {
            _args = new String[]{config.toString()};
        }
        if (isMicroBoot()) {
            try {
                MicroRuntime.startAgent(agentName, c.getName(), _args);
                ag = MicroRuntime.getAgent(agentName);
                _controllers.put(agentName, ag);
            } catch (Exception ex) {
                Error("ERROR CREATING AGENT " + agentName);
                Exception(ex);
            }
        } else {
            try {
                ag = _secondContainer.createNewAgent(agentName, c.getName(), _args);
                ag.start();
//                _controllers.put(agentName, ag);
            } catch (Exception e) {
                Error("Error creating Agent " + agentName);
                Exception(e);
                ag = null;
            }
        }

        doCompleted("LAUNCH");
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
        return this;
    }

    public JADEBoot WaitToClose() {
        boolean somealive;
        String alive;
        Info("Waiting for agents to close");
        do {
            alive = "";
            somealive = false;
            for (String sname : _agentNames) {
                String name = "" + sname;
                try {
                    if (isMicroBoot()) {
                        somealive = MicroRuntime.size() > 0;
                    } else {
                        _firstContainer.getAgent(name);
                        somealive = true;
                    }
                    alive += name + ". ";
                } catch (Exception ex) {
                    _controllers.remove(name);
//                    _agentNames.remove(name);
                }
            }
            if (somealive) {
                try {
                    Thread.sleep(2500);
                } catch (Exception e) {
                }
            }
        } while (somealive && !this.isShutDown());
        return this;
    }

    public JADEBoot Close() {
        // Kill all agents
        Info("Shutting down");
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
//        try {
//            Thread.sleep(5000);
//        } catch (Exception e) {
//        }
        return this;
    }

    public JADEBoot WaitToShutDown() {
        WaitToClose();
        ShutDown();
        return this;
    }
    public JADEBoot ShutDown() {
        Close();
        Info("Turning off JadeBoot");
        turnOff(_firstContainer);
//        try {
//            Thread.sleep(2500);
//        } catch (Exception e) {
//        }
        //turnOff(_secondContainer);
        System.exit(0);
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

    protected boolean isJade() {
        return (_platformType == PLATFORM.JADE
                || _platformType == PLATFORM.MICROJADE);
    }

    protected boolean isMicroBoot() {
        return _platformType == PLATFORM.MICROJADE;
    }

    protected void Info(String s) {
        logger.logMessage(s);
    }

    protected void Error(String s) {
        logger.logError(s);
    }

    protected void Exception(Exception ex) {
        logger.logException(ex);
    }

    protected void Abort(String s) {
        Error(s);
        Exit();
    }

//    protected JADEBoot close() {
//        try{
//            Info("AppBoot waiting to close");
//            Thread.sleep(2000);
//        }catch (Exception ex) {
//            
//        }
//        return this;
//    }
    protected void Exit() {
        Info("AppBoot exiting");
        System.exit(0);
    }

    protected boolean isBehindRouter() {
        return !_host.equals("localhost")
                && !getExtIPAddress().equals(getLocalIPAddress());
    }

    public boolean activateLocks() {
        try {
            this.deactivateLocks();
            _lockCloseSession = new FileWriter(new File(_lockShutDownFilename));
            _lockCloseSession.close();
//            _lockReboot = new FileWriter(new File(_lockRebootFilename));
//            _lockReboot.close();
            return true;
        } catch (IOException ex) {
            System.err.println(ex.toString());
            return false;
        }
    }

    public boolean isShutDown() {
        return !(new File(_lockShutDownFilename).exists());
    }

    public boolean isReboot() {
        return new File(_lockRebootFilename).exists();
    }

    public boolean mustWait() {
        return new File(_lockWaitFilename).exists();
    }

    public boolean deactivateLocks() {
        File f;
        f = new File(_lockShutDownFilename);
        if (f.exists()) {
            f.delete();
        }
        f = new File(_lockRebootFilename);
        if (f.exists()) {
            f.delete();
        }
        f = new File(_lockWaitFilename);
        if (f.exists()) {
            f.delete();
        }
        return true;
    }
}
