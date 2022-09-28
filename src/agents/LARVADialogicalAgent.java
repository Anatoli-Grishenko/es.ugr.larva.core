/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import static agents.LARVAFirstAgent.sd;
import crypto.Keygen;
import static crypto.Keygen.getHexaKey;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import messaging.ACLMessageTools;
import static messaging.ACLMessageTools.fancyWriteACLM;
import static messaging.ACLMessageTools.isInitiator;
import messaging.DialogueManager;
import messaging.Utterance;
import static messaging.Utterance.Status.OPEN;
import tools.TimeHandler;
import static tools.TimeHandler.nextSecs;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
// Bag para mensajes ignorados
// bag para errores ams
public class LARVADialogicalAgent extends LARVAFirstAgent {

    protected DialogueManager DM;
    protected ArrayList<ACLMessage> inBoxes;

    @Override
    public void setup() {
        super.setup();
        DM = new DialogueManager(getLocalName());
    }

    protected boolean isOpenUtterance(ACLMessage msg) {
        return DM.getMyUtterance(msg).getMyStatus() == Utterance.Status.OPEN;
    }

    protected boolean isOverdueUtterance(ACLMessage msg) {
        return DM.getMyUtterance(msg).getMyStatus() == Utterance.Status.OVERDUE;
    }

    protected boolean isCompleteUtterance(ACLMessage msg) {
        return DM.getMyUtterance(msg).getMyStatus() == Utterance.Status.COMPLETE;
    }

    protected boolean isAliveUtterance(ACLMessage msg) {
        return DM.getMyUtterance(msg).isAlive();
    }

    protected boolean isClosedUtterance(ACLMessage msg) {
        return !DM.getMyUtterance(msg).isAlive();
    }

    protected void checkOpenUtterances() {
        ACLMessage received;
        received = super.LARVAreceive(); //super.LARVAblockingReceive(5);
        while (received != null) {
            DM.addUtterance(received);
//            Info("\n\n\n"+DM.toString()+"\n\n\n");
            received = super.LARVAreceive(); //super.LARVAblockingReceive(5);
        }
    }

//    protected boolean isDueUtterance(ACLMessage m) {
//        checkOpenUtterances();
//        Utterance u = DM.getPrevUtterance(inbox);
//        if (u == null) {
//            return false;
//        }
//        return u.getMyStatus() != Utterance.Status.OPEN;
//    }
    protected boolean hasDueUtterances() {
        checkOpenUtterances();
        return !DM.queryAllDueUtterances().isEmpty();
    }

    protected ArrayList<ACLMessage> getAllDueUtterances() {
        this.checkOpenUtterances();
        return DM.queryAllDueUtterances();
    }

    protected boolean hasOpenUtterances() {
        checkOpenUtterances();
        return !DM.getAllOpenUtterances().isEmpty();
    }

    protected ArrayList<ACLMessage> getAllOpenUtterances() {
        this.checkOpenUtterances();
        return DM.getAllOpenUtterances();
    }

    protected boolean hasExtRequests() {
        checkOpenUtterances();
        return !DM.getExtOpenUtterances().isEmpty();
    }

    protected ArrayList<ACLMessage> getExtRequests() {
        this.checkOpenUtterances();
        return DM.getExtOpenUtterances();
    }

    protected ArrayList<ACLMessage> getIgnoredMessages() {
        this.checkOpenUtterances();
        ArrayList<ACLMessage> res = DM.queryIgnoredMessages();
        DM.clearIgnoredMessages();
        return res;
    }

    protected boolean hasIgnoredMessages() {
        checkOpenUtterances();
        return !DM.queryIgnoredMessages().isEmpty();
    }

    protected boolean hasAnswersTo(ACLMessage msg) {
        return msg != null && DM.getMyUtterance(msg).isAlive() && !DM.getAllAnswersTo(msg).isEmpty();
    }

    protected ArrayList<ACLMessage> getAnswersTo(ACLMessage msg) {
        this.checkOpenUtterances();
        return DM.getAllAnswersTo(msg);
    }

    protected Utterance.Status getUtteranceStatus(ACLMessage msg) {
        checkOpenUtterances();
        Utterance u;
        u = DM.getPrevUtterance(msg);
        if (u == null) {
            u = DM.getMyUtterance(msg);
        }
        if (u != null) {
            return u.getMyStatus();
        } else {
            return null;
        }
    }

    protected int getUtteranceDepth(ACLMessage msg) {
        Utterance u = DM.getRawUtterance(msg);
        int res = 1;
        while (u.getParent() != null) {
            res++;
            u = u.getParent();
        }
        return res;
    }

    protected int getUtterancesOf(ACLMessage msg, String agent) {
        Utterance u = DM.getRawUtterance(msg);
        int res = 1;
        while (u != null) {
            if (u.getInitiator().equals(agent)) {
                res++;
            }
            res++;
            u = u.getParent();
        }
        return res;
    }

    protected String getUtteranceStarter(ACLMessage msg) {
        Utterance u = DM.getRawUtterance(msg);
        int res = 1;
        while (u.getParent() != null) {
            u = u.getParent();
        }
        return u.getInitiator();
    }

    protected void closeUtterance(ACLMessage msg) {
        if (msg == null || DM.getMyUtterance(msg) == null) {
            return;
        }
        DM.getMyUtterance(msg).close();
        if (isInitiator(msg.getPerformative())) {
            if (DM.getPrevUtterance(msg) != null
                    && DM.getPrevUtterance(msg).getMyStatus() != OPEN
                    && DM.getPrevUtterance(msg).isAlive()) {
                DM.getPrevUtterance(msg).close();
            }
        }
    }

    private ACLMessage secureMessage(ACLMessage msg) {
        msg = ACLMessageTools.secureACLM(msg);
        if (msg.getConversationId() == null || msg.getConversationId().length() == 0) {
            msg.setConversationId("CID" + getHexaKey(6));
        }
        if (msg.getReplyWith() == null || msg.getReplyWith().length() == 0) {
            msg.setReplyWith("RW" + getHexaKey(6));
        }
        return msg;
    }

    protected void Dialogue(ACLMessage msg) {
        super.LARVAsend(secureMessage(msg));
        DM.addUtterance(msg);
//        this.checkOpenUtterances();
    }

    private void waitOpenUtterance(ACLMessage msg) {
        ACLMessage received = null;
        boolean still;
        Info("Sleeping until new open utterances");
//        Info(toString());
        do{
            received = super.LARVAblockingReceive();
            DM.addUtterance(received);
            still = (msg == null ? !this.hasExtRequests() : this.isOpenUtterance(msg));
        } while(still);
//        still = (msg == null ? !this.hasExtRequests() : this.isOpenUtterance(msg));
//        while (still) {
//            received = super.LARVAblockingReceive();
//            DM.addUtterance(received);
//            still = (msg == null ? !this.hasExtRequests() : this.isOpenUtterance(msg));
//        }
        Info("Wake up!. I have received " + fancyWriteACLM(received, true));
        if (msg != null) {
            this.closeUtterance(msg);
        }
    }

    protected ArrayList<ACLMessage> blockingDialogue() {
        this.waitOpenUtterance(null);
        return this.getExtRequests();
    }

    protected ArrayList<ACLMessage> blockingDialogue(ACLMessage msg) {
        ArrayList<ACLMessage> res = new ArrayList();
        Info("Opening uttterance ");
        this.Dialogue(msg);
        if (isInitiator(msg.getPerformative())) {
            while (this.isOpenUtterance(msg) || this.getAnswersTo(msg).size()==0) {
                Info("Waiting to close utterance");
                this.waitOpenUtterance(msg);
            }
            res = this.getAnswersTo(msg);
            this.closeUtterance(msg);
        }
        if (DM.getPrevUtterance(msg) != null) {
            DM.getPrevUtterance(msg).close();
        }
        return res;

    }

//    protected ArrayList<ACLMessage> waitAnswersTo(ACLMessage msg) {
//        while (this.isOpenUtterance(msg)) {
//            Info("Waiting to close utterance");
//            this.waitOpenUtterance(msg);
//        }
//        this.closeUtterance(msg);
//        return this.getAnswersTo(msg);
//    }
//
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

    //Legacy
    protected boolean doLARVACheckin() {
        Info("Checking-in to LARVA");
        if (DFGetAllProvidersOf("IDENTITY").isEmpty()) {
            Error("Unable to checkin at LARVA no identity manager service has been found");
        } else {
            if (mypassport == null || mypassport.length() == 0) {
                this.Error("Please load the passport first");
                return false;
            }
            ACLMessage outbox = new ACLMessage(ACLMessage.SUBSCRIBE);
            IdentityManager = DFGetAllProvidersOf("IDENTITY").get(0);
            Info("Found agent " + IdentityManager + " as Identity Manager");
            AID IM = new AID(IdentityManager, AID.ISLOCALNAME);
            outbox.setSender(getAID());
            outbox.addReceiver(IM);
            outbox.setContent(mypassport);
            outbox.setConversationId("checkin");
            outbox.setReplyWith("checkin");
//            outbox.setReplyByDate(nextSecs(WAITANSWERMS/1000).toDate());
            Info("Sending passport to " + IdentityManager);
            inBoxes = this.blockingDialogue(outbox);
            if (inBoxes.size() == 0) {
                Error("Agent " + IdentityManager + " does not answer. Not checked in");
            } else {
                checkin = inBoxes.get(0);
                addMilestone("MILES20");
                checkout = checkin.createReply();
                if (checkin.getPerformative() == ACLMessage.CONFIRM) {
                    checkedin = true;
                    Info(checkin.getContent());
                    this.getUserData(checkin.getContent());
                    return true;
                } else if (checkin.getPerformative() == ACLMessage.REFUSE) {
                    Error("Checkin at LARVA refused.\nDetails: " + checkin.getContent());
                } else {
                    Error("Could not checkin at LARVA.\nDetails: " + checkin.getContent());
                }
            }
            return false;
        }

        return false;
    }

    protected boolean doLARVACheckout() {
        Info("Checking-out from LARVA");
        if (checkout == null) {
            return false;
        }
        checkout.setPerformative(ACLMessage.CANCEL);
        checkout.setContent("Request checkout");
        outbox.setConversationId("checkin");
        outbox.setReplyWith("checkin");
//        checkout.setReplyByDate(nextSecs(WAITANSWERMS/1000).toDate());
        inBoxes = this.blockingDialogue(checkout);
        if (inBoxes.size() == 0) {
            Error("Agent " + IdentityManager + " does not answer. Not checked out");
        } else {
            if (inBoxes.get(0).getPerformative() == ACLMessage.CONFIRM) {
                Info(inBoxes.get(0).getContent());
                checkedin = false;
                return true;
            } else {
                Error(inBoxes.get(0).getContent());
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return DM.toString();
    }
}
