/**
 * @file LARVABaseAgent.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package agents;

import crypto.Keygen;
import data.Ole;
import data.OleFile;
import data.OlePassport;
import data.Transform;
import disk.Logger;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import messaging.ACLMPayload;
import messaging.ACLMessageTools;
import static messaging.ACLMessageTools.ACLMID;
import tools.TimeHandler;
import static messaging.ACLMessageTools.ACLMRCVDATE;
import static messaging.ACLMessageTools.ACLMROLE;
import static messaging.ACLMessageTools.ACLMSNDDATE;
import profiling.Profiler;
import tools.NetworkCookie;

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
public class LARVABaseAgent extends Agent {

    protected final long WAITANSWERMS = 5000;

    /**
     * Default, repetitive behaviour. It ends when the variable exit equals true
     */
    public Behaviour defaultBehaviour;
    /**
     * Supoprt for handling disk files. See {@link OleFile}
     */
    protected OleFile olef;
    /**
     * Logger of messages. By default it is active and does not save data on
     * disk. For more information {@link Logger}
     */
    protected Logger logger;

    /**
     * It controls the LARVAexit of the default behaviour and the consequent
     * death of the agent
     */
    protected boolean LARVAexit;

    /**
     * It is true only when the checkin has been succesful
     */
    protected boolean checkedin;

    /**
     * Geeneral variables for messaging
     */
    protected ACLMessage inbox, outbox;
    /**
     * To store the personal passport
     */
    protected String mypassport,
            // Remember the name of the Session Manager
            mySessionmanager = "", mySessionID = "";

    /**
     * Counter of cycles of the method Execute()
     */
    protected long ncycles;
    protected NetworkCookie lastCookie;

    protected Profiler MyCPUProfiler, MyNetworkProfiler;
    protected boolean isProfiling = false;
    protected static Semaphore smDF = new Semaphore(1);

    protected static ACLMessage attachPayload(ACLMessage msg, ACLMPayload payload) {
        Class c = payload.getClass();
        msg.addUserDefinedParameter(ACLMPayload.Payload.PAYLOADCLASS.name(), c.getName());
        msg.addUserDefinedParameter(ACLMPayload.Payload.PAYLOAD.name(), Ole.objectToOle(payload).toPlainJson().toString());
        return msg;
    }

    protected static Object dettachPayload(ACLMessage msg) {
        Object res = null;
//        if (msg.getUserDefinedParameter(ACLMPayload.Payload.PAYLOADCLASS.name()) != null) {
//            Class c;
//            c= Class.forName(msg.getUserDefinedParameter(ACLMPayload.Payload.PAYLOADCLASS.name()));
//            msg.addUserDefinedParameter("payload", Ole.objectToOle(payload).toPlainJson().toString());
//        }
        return res;
    }

    /**
     * Main constructor
     */
    public LARVABaseAgent() {
        super();
        logger = new Logger();
        logger.setEcho(true);
        logger.setOwner("");
        checkedin = false;
        ncycles = 0;
    }

    /**
     * Main JADE setup
     */
    @Override
    public void setup() {
        super.setup();
        LARVAexit = true;
        logger.setOwner(this.getLocalName());
        this.BehaviourDefaultSetup();
        MyCPUProfiler = new Profiler();
        MyCPUProfiler.setOwner(getLocalName());
        MyNetworkProfiler = new Profiler();
        MyNetworkProfiler.setOwner(getLocalName());
    }

    @Override
    public void takeDown() {
        LARVADFRemoveAllMyServices();
        super.takeDown();
    }

    //
    // Lifecycle
    //
    /**
     * Main body of the default behaviour. This method is repeatedly executed
     * until the variable exit is set to true
     */
    public void Execute() {

    }

    public void preExecute() {

    }

    public void postExecute() {
        ncycles++;
        lastCookie = null;

    }

    //
    // Messaging
    //
    protected ACLMessage LARVAprocessAnyMessage(ACLMessage msg) {
        if (msg != null) {
            if (msg.getUserDefinedParameter(ACLMID) == null) {
                msg.addUserDefinedParameter(ACLMID, Keygen.getHexaKey(20));
            }
            msg = ACLMessageTools.secureACLM(msg);
        }
        return msg;
    }

    protected ACLMessage LARVAprocessSendMessage(ACLMessage msg) {
        if (msg.getUserDefinedParameter(ACLMSNDDATE) == null) {
            msg.addUserDefinedParameter(ACLMSNDDATE, "" + new TimeHandler().elapsedTimeSecs());
        }
        msg = ACLMessageTools.secureACLM(msg);
        if (msg.getSender() == null) {
            msg.setSender(getAID());
        }
        return this.LARVAprocessAnyMessage(msg);
    }

    protected ACLMessage LARVAprocessReceiveMessage(ACLMessage msg) {
        if (msg != null) {
            if (msg.getUserDefinedParameter(ACLMRCVDATE) == null) {
                msg.addUserDefinedParameter(ACLMRCVDATE, "" + new TimeHandler().elapsedTimeSecs());
            }
            if (msg.getUserDefinedParameter(ACLMROLE) != null && msg.getUserDefinedParameter(ACLMROLE).equals("SESSION MANAGER")) {
                this.mySessionmanager = msg.getSender().getLocalName();
                this.mySessionID = msg.getConversationId();
            }
        }
        return this.LARVAprocessAnyMessage(msg);
    }

    public void LARVAwait(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ex) {
        }

    }

    public void LARVASleep(int milis) {
        TimeHandler th1 = new TimeHandler(), th2 = th1;
        while (th1.elapsedTimeMilisecsUntil(th2) < milis) {
            th2 = new TimeHandler();
        }

    }

    //
    // DF+
    //
    /**
     * Build the list of all agents in the platform who provide any service
     *
     * @return A list of agent names, each of whom provides one or more of the
     * services
     */
    public synchronized ArrayList<String> LARVADFGetProviderList() {
        return LARVADFGetAllProvidersOf("");
    }

    /**
     * Provides the list of all known services in the platform
     *
     * @return A list of service names
     */
    public synchronized ArrayList<String> LARVADFGetServiceList() {
        return LARVADFGetAllServicesProvidedBy("");
    }

    /**
     * It gives the list of all agent who provide a certain service
     *
     * @param service The service being queried
     * @return A list of agent names, each of whom is a registered provider of
     * that service
     */
//    public ArrayList<String> BDFGetAllProvidersOf(String service) {
//        ArrayList<String> res = new ArrayList<>();
//        DFAgentDescription list[];
//        list = this.LARVADFQueryAllProviders(service);
//        if (list != null && list.length > 0) {
//            for (DFAgentDescription list1 : list) {
//                if (!res.contains(list1.getName().getLocalName())) {
//                    res.add(list1.getName().getLocalName());
//                }
//            }
//        }
//        return res;
//    }
    public synchronized ArrayList<String> LARVADFGetAllProvidersOf(String service) {
        ArrayList<String> res = new ArrayList<>();
//        try {
//            smDF.acquire(1);
//        } catch (Exception ex) {
//            return res;
//        }
        DFAgentDescription list[];
        list = this.LB_DFQueryAllProviders(service);
        if (list != null && list.length > 0) {
            for (DFAgentDescription list1 : list) {
                if (!res.contains(list1.getName().getLocalName())) {
                    res.add(list1.getName().getLocalName());
                }
            }
        }
//        smDF.release(1);
        return res;
    }

    /**
     * It gives the set of services provided by a certain agent, if any.
     *
     * @param agentName A list (might be empty) of names of services provided by
     * the agent.
     * @return An array of Strings with the set of services, if any, provided by
     * the agent.
     */
    public synchronized ArrayList<String> LARVADFGetAllServicesProvidedBy(String agentName) {
        ArrayList<String> res = new ArrayList<>();
//        try {
//            smDF.acquire(1);
//        } catch (Exception ex) {
//            return res;
//        }
        DFAgentDescription list[];
        list = this.LB_DFQueryAllServicesProvided(agentName);
        if (list != null && list.length > 0) {
            for (DFAgentDescription list1 : list) {
                Iterator sdi = list1.getAllServices();
                while (sdi.hasNext()) {
                    ServiceDescription sd = (ServiceDescription) sdi.next();
                    if (!res.contains(sd.getType())) {
                        res.add(sd.getType());
                    }
                }
            }
        }
//        smDF.release(1);
        return res;
    }

    /**
     * *
     * Check whether an agent is proviedr of a service or not
     *
     * @param agentName The name of the agent
     * @param service The name of the service
     * @return true when the agent is registered as a known provider of the
     * service, false otherwise
     */
    public synchronized boolean LARVADFHasService(String agentName, String service) {
        return LARVADFGetAllProvidersOf(service).contains(agentName);
    }

    /**
     * Allows any LARVAAgent to register a set of services. This operation is
     * not cummulative, that is, all agents are only allowed one entry in the
     * Directory, therefore, in the case of more than one call to this method,
     * will end up with only the services of the last call. The previous
     * services are overwritten.
     *
     * @param services An array of String defining the set of services provided
     * by an agent.
     * @return
     */
    public synchronized boolean LARVADFSetMyServices(String[] services) {
        Info("Services registered " + Transform.toArrayList(services).toString());
        if (this.LARVADFGetAllServicesProvidedBy(getLocalName()).size() > 0) {
            LB_DFRemoveAllMyServices();
        }
        if (this.LB_DFSetServices(getLocalName(), services)) {
//            LARVAwait(500);
            return true;
        }
        return false;
    }

    public synchronized boolean LARVADFAddMyServices(String[] services) {
        ArrayList<String> prevServices;
        Info("Adding services " + new ArrayList(Transform.toArrayList(services)));
        prevServices = this.LARVADFGetAllServicesProvidedBy(getLocalName());
        prevServices.addAll(new ArrayList(Transform.toArrayList(services)));
        return LARVADFSetMyServices(Transform.toArrayString(prevServices));
    }

    public synchronized boolean LARVADFRemoveMyServices(String[] services) {
        ArrayList<String> prevServices;
        Info("Removing services " + new ArrayList(Transform.toArrayList(services)));
        prevServices = this.LARVADFGetAllServicesProvidedBy(getLocalName());
        prevServices.removeAll(new ArrayList(Transform.toArrayList(services)));
        return LARVADFSetMyServices(Transform.toArrayString(prevServices));
    }

    public synchronized boolean LARVADFRemoveAllMyServices() {
        ArrayList<String> prevServices;
        Info("Removing all services ");
        prevServices = this.LARVADFGetAllServicesProvidedBy(getLocalName());
        if (!prevServices.isEmpty()) {
            LB_DFRemoveAllMyServices();
        }
        return true;
    }

    public Profiler getMyCPUProfiler() {
        return MyCPUProfiler;
    }

    public Profiler getMyNetworkProfiler() {
        return MyNetworkProfiler;
    }

    public void activateMyCPUProfiler(String filename) {
        getMyCPUProfiler().setActive(true);
        isProfiling = true;
        getMyCPUProfiler().setOwner(getLocalName());
        getMyCPUProfiler().setTsvFileName(filename + ".tsv");
    }

    public void activateMyNetworkProfiler(String filename) {
        isProfiling = true;
        getMyNetworkProfiler().setActive(true);
        getMyNetworkProfiler().setOwner(getLocalName());
        getMyNetworkProfiler().setTsvFileName(filename + ".tsv");
    }

    public void deactivateMyCPUProfiler() {
        getMyCPUProfiler().setActive(false);
    }

    public void deactivateMyNetworkProfiler() {
        getMyNetworkProfiler().setActive(true);
    }

    public NetworkCookie getNewNetworkCookie() {
        NetworkCookie nc = new NetworkCookie();
        nc.setOwner(getLocalName());
        nc.setSerie((int) getNCycles());
        return nc;
    }

    protected ACLMessage LARVAcreateReply(ACLMessage incoming) {
        ACLMessage outgoing = incoming.createReply();
        outgoing.setSender(getAID());
        outgoing.setEncoding(incoming.getEncoding());
        if (Profiler.isProfiler(incoming)) {
            NetworkCookie nc = Profiler.extractProfiler(incoming);
            if (nc.gettReceive().length() != 0) {
                nc = getNewNetworkCookie();
            }

            outgoing = Profiler.injectProfiler(outgoing, nc);
        }
        return outgoing;
    }

    /**
     * It allows the de-registration of all services.
     */
    private void LB_DFRemoveAllMyServices() {
        try {
            smDF.acquire(1);
        } catch (Exception ex) {
            return;
        }
        //System.out.println(getLocalName() + ">>> REMOVEALLMYSERVICES");

        try {
            DFService.deregister(this);
        } catch (FIPAException ex) {
            System.err.println(getLocalName() + ":" + ex.toString());
        }
        //System.out.println(getLocalName() + "  < REMOVEALLMYSERVICES");
        smDF.release(1);
    }

    //
    // DF Private
    //
    private boolean LB_DFSetServices(String agentname, String services[]) {
        DFAgentDescription dfd;
        ServiceDescription sd;
        boolean res = false;
        try {
            smDF.acquire(1);
        } catch (Exception ex) {
            return res;
        }
        //System.out.println(getLocalName() + ">>> SetSERVICES ");
        dfd = new DFAgentDescription();
        dfd.setName(new AID(agentname, AID.ISLOCALNAME));
        for (String s : services) {
            sd = new ServiceDescription();
            sd.setName(s); //.toUpperCase());
            sd.setType(s); //.toUpperCase());
            dfd.addServices(sd);
        }
        try {
            DFService.register(this, dfd);
            res = true;
        } catch (FIPAException ex) {
            MinorException(ex);
        }
        //System.out.println(getLocalName() + "  < SetSERVICES");
        smDF.release(1);
        return res;
    }

    private DFAgentDescription[] LB_DFQueryAllServicesProvided(String agentname) {
        DFAgentDescription dfd;
        ServiceDescription sd;
        DFAgentDescription services[] = new DFAgentDescription[0];
        try {
            smDF.acquire(1);
        } catch (Exception ex) {
            return services;
        }
        //System.out.println(getLocalName() + ">>> QueryAgent " + agentname);
        dfd = new DFAgentDescription();
        if (!agentname.equals("")) {
            dfd.setName(new AID(agentname, AID.ISLOCALNAME));
        }
        sd = new ServiceDescription();
        dfd.addServices(sd);
        SearchConstraints c = new SearchConstraints();
        c.setMaxResults((long) -1);
        try {
            services = DFService.search(this, dfd, c);
        } catch (FIPAException ex) {
            MinorException(ex);
        }
        //System.out.println(getLocalName() + "  < QueryAgent");
        smDF.release(1);
        return services;
    }

    private DFAgentDescription[] LB_DFQueryAllProviders(String service) {
        DFAgentDescription dfd;
        ServiceDescription sd;
        DFAgentDescription agents[] = new DFAgentDescription[0];
        try {
            smDF.acquire(1);
        } catch (Exception ex) {
            return agents;
        }
        //System.out.println(getLocalName() + ">>> QueryService " + service);
        dfd = new DFAgentDescription();
        SearchConstraints c = new SearchConstraints();
        c.setMaxResults((long) -1);
        sd = new ServiceDescription();
        if (!service.equals("")) {
            sd.setName(service);
        }
        dfd.addServices(sd);
        try {
            agents = DFService.search(this, dfd, c);
        } catch (FIPAException ex) {
            MinorException(ex);
        }
        //System.out.println(getLocalName() + "  < QueryService");
        smDF.release(1);
        return agents;
    }

    //
    // AMS +
    //
    /**
     * Checks whether a certain agent is connected or not to the platform.
     * <b>Warning!</b>This is a very useful tool but an abusive use must be
     * avoided in order to restrict the data transfer through the network.
     *
     * @param agentName The name of the agent
     * @return true if the aggent is right now connected to the platform, false
     * otherwise.
     */
    public synchronized boolean AMSIsConnected(String agentName) {
        return AMSGetAllConnectedAgents().contains(agentName);
    }

    /**
     * It gives the list of all known agents in the platform
     * <b>Warning!</b>This is a very useful tool but an abusive use must be
     * avoided in order to restrict the data transfer through the network.
     *
     * @return The list of all agent names that seem to be connected to the
     * platform
     */
    public synchronized ArrayList<String> AMSGetAllConnectedAgents() {
        ArrayList<String> res = new ArrayList<>();
        AMSAgentDescription list[];
        list = this.AMSQuery("");
        for (AMSAgentDescription list1 : list) {
            res.add(list1.getName().getLocalName());
        }
        return res;
    }

    private synchronized AMSAgentDescription[] AMSQuery(String agentname) {
        AMSAgentDescription amsd = new AMSAgentDescription(), amsdlist[] = null;
        if (!agentname.equals("")) {
            amsd.setName(new AID(agentname, AID.ISLOCALNAME));
        }
        SearchConstraints sc = new SearchConstraints();
        sc.setMaxResults((long) -1);
        try {
            amsdlist = AMSService.search(this, amsd, sc);
        } catch (FIPAException ex) {
        }
        return amsdlist;
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
    protected void Error(String message) {
        logger.logError(message);
    }

    /**
     * Log a common message. It is sent of Stdout. When the echo is not active,
     * it does not show anything on screen.
     *
     * @param message The informative message
     */
    protected void Message(String message) {
        logger.logMessage(message);
    }

    protected void Info(String message) {
        logger.logMessage(message);
    }

    protected void Alert(String message) {
        Message(message);
    }

    /**
     * This method ask the user for confirmation (yes=true, no = false) in front
     * of a given message
     *
     * @param message The question asked to the user
     * @return true if the user select yes or false if the user selects no
     */
    protected boolean Confirm(String message) {
        String line = inputLine(message);
        if (line.length() == 0 || line.toUpperCase().charAt(0) == 'Y') {
            return true;
        } else {
            return false;
        }
    }

    /**
     * It asks the user to input a String
     *
     * @param message The message shown to the user
     * @return The string typed by the user
     */
    protected String inputLine(String message) {
        System.out.println("\n\n" + message + " ");
        return new Scanner(System.in).nextLine();
    }

    /**
     * Log an exception. It is shown on screen either the echo is active or not
     *
     * @param ex Thje exceptio nproduced
     */
    private void MinorException(Exception ex) {
        logger.logException(ex);
    }

    protected void BehaviourDefaultSetup() {
        defaultBehaviour = new Behaviour() {
            @Override
            public void action() {
                preExecute();
                Execute();
                postExecute();
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

    public boolean isCheckedin() {
        return checkedin;
    }

    public void setCheckedin(boolean checkedin) {
        this.checkedin = checkedin;
    }

    /**
     * It gives the number of consecutive executions of the method Execute()
     *
     * @return The number of iterations of the agent
     */
    public long getNCycles() {
        return ncycles;
    }

    public void setNcycles(int ncycles) {
        this.ncycles = ncycles;
    }

    public boolean isExit() {
        return LARVAexit;
    }

    public void setExit(boolean exit) {
        this.LARVAexit = exit;
    }

}
