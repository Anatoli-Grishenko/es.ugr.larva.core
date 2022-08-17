/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import static agents.LARVAFirstAgent.sd;
import crypto.Keygen;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import messaging.ACLMessageTools;
import static messaging.ACLMessageTools.isInitiator;
import messaging.DialogueManager;
import messaging.MessageBox;
import messaging.MessageBox.BoxQueue;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class LARVADialogicalAgent extends LARVAFirstAgent {

    protected DialogueManager DM;

    @Override
    public void setup() {
        super.setup();
        DM = new DialogueManager(getLocalName());
    }

    protected boolean LARVAcheckDialogue() {
        ACLMessage received;
        received = this.LARVAblockingReceive(100);
        while (received != null) {
            DM.addUtterance(received);
            received = this.LARVAblockingReceive(100);
        }
        Info("Checking Dialogue status: "+DM.getOpenConversations().length);
        return DM.getOpenConversations().length>0;
    }

    protected ACLMessage [] LARVAopenConversations() {
        Info("Retrieving all open conversations");
        return DM.getOpenConversations();
    }

    protected ACLMessage [] LARVAunexpectedRequests() {
        Info("Retrieving unexpected requests");
        return DM.getUnexpectedRequests();
    }

    protected ACLMessage [] LARVApendingRequest() {
        Info("Retrieving unanswered requests");
        return DM.getMyPendingRequests();
    }

    protected ACLMessage [] LARVAnewAnswers() {
        Info("Retrieving unread answers");
        return DM.getNewAnswers();
    }

    protected void LARVAstartDialogue(ACLMessage msg) {
        ACLMessage received;
        Info("Starting a new utterance");
        msg = ACLMessageTools.secureACLM(msg);
        LARVAsend(msg);
        DM.addUtterance(msg);
    }
    
    protected ACLMessage[] LARVAblockingDialogue(ACLMessage msg) {
        this.LARVAstartDialogue(msg);
        return this.LARVAwaitAnswersTo(msg);
    }
    
    protected ACLMessage[] LARVAwaitAnswersTo(ACLMessage msg) {
        Info("Waiting answers");
        while (!DM.getUtterance(msg).isOverDue()) {
            Info("Waiting answers");
            DM.addUtterance(LARVAblockingReceive());
        }
        return DM.getUtterance(msg).getAllAnswers();
    }

    protected void LARVAAnswerDialogue(ACLMessage msg) {
        ACLMessage received;
        Info("Continuing a previous utterance");
        msg = ACLMessageTools.secureACLM(msg);
        LARVAsend(msg);
        DM.addUtterance(msg);
    }

}
