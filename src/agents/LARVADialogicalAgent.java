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
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import messaging.ACLMessageTools;
import static messaging.ACLMessageTools.isInitiator;
import messaging.DialogueManager;
import messaging.MessageBox;
import messaging.MessageBox.BoxQueue;
import messaging.Utterance;
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

    protected void LARVAcheckOpenDialogs() {
        ACLMessage received;
        received = super.LARVAreceive(); //super.LARVAblockingReceive(5);
        while (received != null) {
            if (!DM.addUtterance(received)) {
                ignoreMessage(received);
            }
            received = super.LARVAreceive(); //super.LARVAblockingReceive(5);
        }
    }

    protected boolean LARVAhasOpenDialogs() {
        LARVAcheckOpenDialogs();
        return this.LARVAsizeOpenDialogues() > 0;
    }

    protected int LARVAsizeOpenDialogues() {
        LARVAcheckOpenDialogs();
        return this.LARVAqueryOpenDialogues().length;
    }

    protected ACLMessage[] LARVAqueryOpenDialogues() {
        this.LARVAcheckOpenDialogs();
        return DM.getOpenDialogues();
    }

    protected boolean LARVAhasUnexpectedRequests() {
        LARVAcheckOpenDialogs();
        return this.LARVAsizeUnexpectedRequests() > 0;
    }

    protected ACLMessage[] LARVAqueryUnexpectedRequests() {
        this.LARVAcheckOpenDialogs();
        return DM.getUnexpectedRequests();
    }

    protected int LARVAsizeUnexpectedRequests() {
        LARVAcheckOpenDialogs();
        return this.LARVAqueryUnexpectedRequests().length;
    }

    protected ACLMessage[] LARVAqueryPendingRequests() {
        this.LARVAcheckOpenDialogs();
        return DM.getMyPendingRequests();
    }

    protected boolean LARVAhasPendingRequests() {
        LARVAcheckOpenDialogs();
        return this.LARVASizePendingRequests() > 0;
    }

    protected int LARVASizePendingRequests() {
        LARVAcheckOpenDialogs();
        return this.LARVAqueryPendingRequests().length;
    }

    protected ACLMessage[] LARVAqueryAnswersTo(ACLMessage msg) {
        this.LARVAcheckOpenDialogs();
        return DM.getAllAnswersTo(msg);
    }

    protected boolean LARVAhasAnswersTo(ACLMessage msg) {
        LARVAcheckOpenDialogs();
        return this.LARVASizeAnswersTo(msg) > 0;
    }

    protected int LARVASizeAnswersTo(ACLMessage msg) {
        LARVAcheckOpenDialogs();
        return this.LARVAqueryAnswersTo(msg).length;
    }

    protected Utterance.Status LARVAgetDialogueStatus(ACLMessage msg) {
        LARVAcheckOpenDialogs();
        Utterance u;
        u=DM.getMyUtterance(msg);
        if (u==null) {
            u=DM.getPrevUtterance(msg);
        }
        if (u != null) {
            return u.getMyStatus();
        } else {
            return Utterance.Status.CLOSED;
        }
    }

    protected void LARVAcloseUtterance(ACLMessage msg) {
//        Info("\n\n"+" ::Closing utterance about " + msg.getContent());
//        Info("::DIALOGIC>>>>>>>>>>> Closing " + msg.getContent()
//                + "\n" + DM.toString());
//        System.out.println("\n\n"+getLocalName()+" ::Closing utterance about " + msg.getContent());
//        System.out.println(getLocalName()+" ::DIALOGIC>>>>>>>>>>> Closing " + msg.getContent()
//                + "\n" + DM.toString());
        DM.getMyUtterance(msg).close();
//        Info(" ::DIALOGIC<<<<<<<<<<\n\n\n" + DM.toString());
//        System.out.println(getLocalName()+" ::DIALOGIC<<<<<<<<<<\n\n\n" + DM.toString());
    }

//    protected void LARVAcloseConversation(ACLMessage msg) {
//        DM.closeConversation(msg);
//    }
//
    protected void LARVADialogue(ACLMessage msg) {
        super.LARVAsend(msg);
        DM.addUtterance(msg);
        this.LARVAcheckOpenDialogs();
    }

    protected ACLMessage[] LARVAblockingDialogue(ACLMessage msg) {
        this.LARVADialogue(msg);
        return this.LARVAwaitAnswersTo(msg);
    }

    protected ACLMessage[] LARVAwaitAnswersTo(ACLMessage msg) {
        LARVAcheckOpenDialogs();
        while (DM.getMyUtterance(msg).getMyStatus() == Utterance.Status.OPEN) {
            this.LARVAcheckOpenDialogs();
        }
        return DM.getMyUtterance(msg).getAllAnswers();
    }

    @Deprecated
    @Override
    protected void LARVAsend(ACLMessage msg) {
    }

    @Deprecated
    @Override
    protected ACLMessage LARVAreceive() {
        return null;
    }

    @Deprecated
    @Override
    protected ACLMessage LARVAblockingReceive() {
        return null;
    }

    @Deprecated
    @Override
    protected ACLMessage LARVAblockingReceive(long milis) {
        return null;
    }

    @Deprecated
    @Override
    public ACLMessage LARVAblockingReceive(MessageTemplate t) {
        return null;
    }

    @Deprecated
    @Override
    protected ACLMessage LARVAblockingReceive(MessageTemplate t, long milis) {
        return null;
    }

    public ACLMessage LARVAreplyAll(ACLMessage m) {
        return m.createReply();
    }

    public ACLMessage LARVAreplySender(ACLMessage m) {
        ACLMessage res = m.createReply();
        res.clearAllReceiver();
        res.addReceiver(m.getSender());
        return res;
    }

    public void ignoreMessage(ACLMessage msg) {

    }
}
