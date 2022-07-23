/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import Environment.Environment;
import ai.Choice;
import ai.DecisionSet;
import ai.MissionSet;
import ai.Plan;
import static crypto.Keygen.getHexaKey;
import data.OlePassport;
import geometry.Point3D;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import tools.TimeHandler;
import tools.emojis;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DroidStarshipLevelB extends DroidStarshipLevelA {

    protected String lastAction;
    int topSteps = 20, nSteps=topSteps;

    protected boolean MyExecuteAction(String action) {
        Info("Executing action " + action);
        outbox = session.createReply();
        outbox.setContent("Request execute " + action + " session " + sessionKey);
        LARVAsend(outbox);
        this.myEnergy++;
        session = LARVAblockingReceive();
        if (session.getContent().toUpperCase().startsWith("INFORM")) {
            lastAction = action;
            nSteps++;
            distance++;
            System.out.println("DISTANCE "+distance);
            return true;
        } else {
            Alert("Execution of action " + action + " failed due to " + session.getContent());
            return false;
        }
    }

    protected boolean MyReadPerceptions() {
        if (nSteps >= topSteps) {
            nSteps=0;
            Info("Reading perceptions");
            outbox = session.createReply();
            outbox.setContent("Query sensors session " + sessionKey);
            LARVAsend(outbox);
            this.myEnergy++;
            session = this.LARVAblockingReceive(MessageTemplate.MatchSender(new AID(sessionManager, AID.ISLOCALNAME)));
//        session = this.fromSessionManager(); //this.LARVAblockingReceive(MessageTemplate.MatchSender(new AID(sessionManager, AID.ISLOCALNAME)));
            if (session.getContent().toUpperCase().startsWith("FAILURE")) {
                Alert("Failed to read perceptions");
                return false;
            } else {
                Info("Read perceptions ok");
                getEnvironment().setExternalPerceptions(session.getContent());
                return true;
            }
        } else {
            E = S(E,new Choice(lastAction));
            return true;
        }

    }

}
