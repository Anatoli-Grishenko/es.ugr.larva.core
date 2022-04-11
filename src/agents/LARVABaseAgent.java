/**
 * @file LARVABaseAgent.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package agents;

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
     * It controls the exit of the default behaviour and the consequent death of
     * the agent
     */
    protected boolean exit;

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
    protected String mypassport;

    /**
     * Counter of cycles of the method Execute()
     */
    protected long ncycles;

  
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
        exit = true;
        logger.setOwner(this.getLocalName());
        this.BehaviourDefaultSetup();
    }

    @Override
    public void takeDown() {
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

    public void preExecute() { }

    public void postExecute() { }

    public void doExit() {
        exit= true;
    }
    public void doNotExit() {
        exit= false;
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
    public ArrayList<String> DFGetProviderList() {
        return DFGetAllProvidersOf("");
    }

    /**
     * Provides the list of all known services in the platform
     *
     * @return A list of service names
     */
    public ArrayList<String> DFGetServiceList() {
        return DFGetAllServicesProvidedBy("");
    }

    /**
     * It gives the list of all agent who provide a certain service
     *
     * @param service The service being queried
     * @return A list of agent names, each of whom is a registered provider of
     * that service
     */
    public ArrayList<String> DFGetAllProvidersOf(String service) {
        ArrayList<String> res = new ArrayList<>();
        DFAgentDescription list[];
        list = this.DFQueryAllProviders(service);
        if (list != null && list.length > 0) {
            for (DFAgentDescription list1 : list) {
                if (!res.contains(list1.getName().getLocalName())) {
                    res.add(list1.getName().getLocalName());
                }
            }
        }
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
    public ArrayList<String> DFGetAllServicesProvidedBy(String agentName) {
        ArrayList<String> res = new ArrayList<>();
        DFAgentDescription list[];
        list = this.DFQueryAllServicesProvided(agentName);
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
    public boolean DFHasService(String agentName, String service) {
        return DFGetAllProvidersOf(service).contains(agentName);
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
    public boolean DFSetMyServices(String[] services) {
        if (this.DFGetAllServicesProvidedBy(getLocalName()).size() > 0) {
            DFRemoveAllMyServices();
        }
        if (this.DFSetServices(getLocalName(), services)) {
            Info("Registering services " + Transform.toArrayList(services).toString());
            return true;
        }

        return false;
    }

    /**
     * It allows the de-registration of all services.
     */
    public void DFRemoveAllMyServices() {
        try {
            DFService.deregister(this);
        } catch (FIPAException ex) {

        }
    }

    //
    // DF Private
    //
    private boolean DFSetServices(String agentname, String services[]) {
        DFAgentDescription dfd;
        ServiceDescription sd;
        boolean res = false;

        dfd = new DFAgentDescription();
        dfd.setName(new AID(agentname, AID.ISLOCALNAME));
        for (String s : services) {
            sd = new ServiceDescription();
            sd.setName(s.toUpperCase());
            sd.setType(s.toUpperCase());
            dfd.addServices(sd);
        }
        try {
            DFService.register(this, dfd);
            res = true;
        } catch (FIPAException ex) {
            MinorException(ex);
        }
        return res;
    }

    private DFAgentDescription[] DFQueryAllServicesProvided(String agentname) {
        DFAgentDescription dfd;
        ServiceDescription sd;
        DFAgentDescription services[] = new DFAgentDescription[0];

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
        return services;
    }

    private DFAgentDescription[] DFQueryAllProviders(String service) {
        DFAgentDescription dfd;
        ServiceDescription sd;
        DFAgentDescription agents[] = new DFAgentDescription[0];
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
    public boolean AMSIsConnected(String agentName) {
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
    public ArrayList<String> AMSGetAllConnectedAgents() {
        ArrayList<String> res = new ArrayList<>();
        AMSAgentDescription list[];
        list = this.AMSQuery("");
        for (AMSAgentDescription list1 : list) {
            res.add(list1.getName().getLocalName());
        }
        return res;
    }

    private AMSAgentDescription[] AMSQuery(String agentname) {
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
                ncycles++;
                if (isExit()) {
                    doDelete();
                }
            }

            @Override
            public boolean done() {
                return exit;
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
     * @return The number of iterations of the agent
     */
    public long getNCycles() {
        return ncycles;
    }

    public void setNcycles(int ncycles) {
        this.ncycles = ncycles;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

}
