/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.ReportableObject;

/**
 *
 * @author lcv
 */
public class BaseFactoryAgent implements ReportableObject {

    protected HashMap<String, AgentController> myAgents;
    protected HashMap<String, ContainerController> myContainers;
    protected ContainerController creatorContainer;
    protected String creatorContainerName;
//    protected PlatformController platform;
    protected jade.core.Runtime rt;
    protected LARVAFirstAgent creatorAgent;

    public BaseFactoryAgent(LARVAFirstAgent owner) {
        myAgents = new HashMap();
        myContainers = new HashMap();
        creatorAgent = owner;
        creatorContainer = creatorAgent.getContainerController();
        try {
            creatorContainerName = creatorContainer.getContainerName();
            myContainers.put(creatorContainerName, creatorContainer);
        } catch (ControllerException ex) {
            creatorContainerName = "UNKNOWN";
        }
        rt = jade.core.Runtime.instance();
    }

    public ArrayList<String> getAllAgentNames() {
        return new ArrayList(myAgents.keySet());
    }

    public ArrayList<String> getAllContainerNames() {
        return new ArrayList(myContainers.keySet());
    }

    public String getMainContainerName() {
        return this.creatorContainerName;
    }

    public boolean createContainer(String containername) {
        ProfileImpl profile = new ProfileImpl(true);
        profile.setParameter(Profile.CONTAINER_NAME, containername);
        ContainerController newContainer = rt.createAgentContainer(profile);
        myContainers.put(containername, newContainer);
        return true;
    }

    public boolean birthAgent(String agentname, Class c, String parameters[]) {
        return birthAgent(agentname, c, parameters, creatorContainerName);
    }

    public boolean birthAgent(String agentname, Class c, String parameters[], String containername) {
        try {
            ContainerController container = myContainers.get(containername);
            if (container == null) {
                return false;
            }
            AgentController ag = container.createNewAgent(agentname, c.getName(), parameters);
            ag.start();
            myAgents.put(agentname, ag);
        } catch (StaleProxyException ex) {
            return false;
        }
        return true;
    }

    public boolean fastMicroBirth(String name, Class c, Object[] args) {
        try {
            MicroRuntime.startAgent(name, c.getName(), args);
            AgentController ag = MicroRuntime.getAgent(name);
            myAgents.put(name, ag);
        } catch (StaleProxyException ex) {
            return false;
        } catch (Exception ex) {
        }
        return true;
    }

    public boolean fastBirth(String name, Class c, Object[] args) {
        try {
            AgentController ag = this.creatorContainer.createNewAgent(name, c.getName(), args);
            ag.start();
            myAgents.put(name, ag);
        } catch (StaleProxyException ex) {
            return false;
        }
        return true;
    }

    public boolean killAgent(String agentname) {
        if (myAgents.get(agentname) != null) {
            try {
                AgentController ag = myAgents.get(agentname);
                myAgents.put(agentname, null);
                myAgents.remove(agentname);
                ag.kill();
            } catch (StaleProxyException ex) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean killContainer(String containername) {
        if (containername.equals(creatorContainerName)) {
            return false;
        }
        ContainerController newContainer = myContainers.get(containername);
        myContainers.put(containername, null);
        myContainers.remove(containername);
        try {
            newContainer.kill();
        } catch (StaleProxyException ex) {
            return false;
        }
        return true;
    }

    public boolean killAllAgents() {
        boolean res = true;
        for (String s : this.getAllAgentNames()) {
            res = res & killAgent(s);
        }
        return res;
    }

    public boolean killAllContainers() {
        for (String who : getAllContainerNames()) {
            if (!who.equals(getMainContainerName())) {
                killContainer(who);
            }
        }
        return true;
    }

    public String purgeAgents() {
        String res = "";
        for (String s : this.getAllAgentNames()) {
            if (!creatorAgent.AMSIsConnected(s)) {
                res += s + " ";
                killAgent(s);
            }
        }
        return res;
    }

    public void killAllExit() {
        killAllAgents();
        killAllContainers();
    }

    @Override
    public String defReportType() {
        return "Agent Factory report";
    }

    @Override
    public String[] defReportableObjectList() {
        return new String[]{"agents", "containers"};
    }

    @Override
    public String reportObjectStatus(String objectid) {
        switch (objectid) {
            case "agents":
                return this.getAllAgentNames().toString();
            default:
                return this.getAllContainerNames().toString();
        }
    }

}
