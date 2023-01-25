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

    protected boolean isOpen(ACLMessage msg) {
        if (msg != null && DM.getMyUtterance(msg) != null) {
            return DM.getMyUtterance(msg).getMyStatus() == Utterance.Status.OPEN;
        } else {
            return false;
        }
    }

    protected boolean isOverdue(ACLMessage msg) {
        if (DM.getMyUtterance(msg) != null) {
            return DM.getMyUtterance(msg).getMyStatus() == Utterance.Status.OVERDUE;
        } else {
            return false;
        }
    }

    protected boolean isComplete(ACLMessage msg) {
        if (DM.getMyUtterance(msg) != null) {
            return DM.getMyUtterance(msg).getMyStatus() == Utterance.Status.COMPLETE;
        } else {
            return false;
        }
    }

    protected boolean isAlive(ACLMessage msg) {
        if (DM.getMyUtterance(msg) != null) {
            return DM.getMyUtterance(msg).isAlive();
        } else {
            return false;
        }
    }

    protected boolean isClosed(ACLMessage msg) {
        if (DM.getMyUtterance(msg) != null) {
            return !DM.getMyUtterance(msg).isAlive();
        } else {
            return false;
        }
    }

    protected void checkDialogues() {
        ACLMessage received;
        received = super.LARVAreceive(); //super.LARVAblockingReceive(5);
        while (received != null) {
            DM.addUtterance(received);
//            Info("\n\n\n"+DM.toString()+"\n\n\n");
            received = super.LARVAreceive(); //super.LARVAblockingReceive(5);
        }
    }

//    protected boolean isDueUtterance(ACLMessage m) {
//        checkDialogues();
//        Utterance u = DM.getPrevUtterance(inbox);
//        if (u == null) {
//            return false;
//        }
//        return u.getMyStatus() != Utterance.Status.OPEN;
//    }
    protected boolean hasDue() {
        checkDialogues();
        return !DM.getAllDue().isEmpty();
    }

    protected ArrayList<ACLMessage> getAllDue() {
        this.checkDialogues();
        return DM.getAllDue();
    }

    protected ArrayList<ACLMessage> getInboundDue() {
        this.checkDialogues();
        return DM.getInboundDue();
    }

    protected ArrayList<ACLMessage> getOutboundDue() {
        this.checkDialogues();
        return DM.getOutboundDue();
    }

    protected ArrayList<ACLMessage> getAllInbound() {
        this.checkDialogues();
        return DM.getAllInbound();
    }

    protected ArrayList<ACLMessage> getAllOutbound() {
        this.checkDialogues();
        return DM.getAllOutbound();
    }

    protected boolean hasOpen() {
        checkDialogues();
        return !DM.getAllOpen().isEmpty();
    }

    protected ArrayList<ACLMessage> getAllOpen() {
        this.checkDialogues();
        return DM.getAllOpen();
    }

//    protected ArrayList<ACLMessage> getInboundOpen() {
//        this.checkDialogues();
//        return DM.getInboundOpen();
//    }
    protected ArrayList<ACLMessage> getOutboundOpen() {
        this.checkDialogues();
        return DM.getOutboundOpen();
    }

    protected boolean hasInboundOpen() {
        checkDialogues();
        return !DM.getInboundOpen().isEmpty();
    }

    protected ArrayList<ACLMessage> getInboundOpen() {
        this.checkDialogues();
        return DM.getInboundOpen();
    }

    protected ArrayList<ACLMessage> getInboundIgnored() {
        this.checkDialogues();
        ArrayList<ACLMessage> res = DM.queryIgnoredMessages();
        DM.clearIgnoredMessages();
        return res;
    }

    protected boolean hasIgnoredMessages() {
        checkDialogues();
        return !DM.queryIgnoredMessages().isEmpty();
    }

    protected boolean hasAnswersTo(ACLMessage msg) {
        return msg != null && DM.getMyUtterance(msg).isAlive() && !DM.getAllAnswersTo(msg).isEmpty();
    }

    protected ArrayList<ACLMessage> getAnswersTo(ACLMessage msg) {
        this.checkDialogues();
        ArrayList<ACLMessage> received=DM.getAllAnswersTo(msg);
        close(msg);
        return received;
    }

    protected Utterance.Status getUtteranceStatus(ACLMessage msg) {
        checkDialogues();
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

    protected void forget(ACLMessage msg) {
        close(msg);
        DM.removeUtterance(msg);
    }

    protected void close(ACLMessage msg) {
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
//        this.checkDialogues();
    }

    private void waitOpenUtterance(ACLMessage msg) {
        ACLMessage received = null;
        boolean still;
        Info("Sleeping until new open utterances");
//        Info(toString());
        do {
            received = super.LARVAblockingReceive();
            DM.addUtterance(received);
//            still = !this.hasInboundOpen() && !this.hasDue(); //(msg == null ? !this.hasInboundOpen() : this.isOpen(msg));
            still = (msg == null ? !this.hasInboundOpen() : this.isOpen(msg));
        } while (still);
//        still = (msg == null ? !this.hasInboundOpen() : this.isOpen(msg));
//        while (still) {
//            received = super.LARVAblockingReceive();
//            DM.addUtterance(received);
//            still = (msg == null ? !this.hasInboundOpen() : this.isOpen(msg));
//        }
        Info("Wake up!. I have received " + fancyWriteACLM(received, false));
        if (msg != null) {
            this.close(msg);
        }
    }

    protected ArrayList<ACLMessage> blockingDialogue() {
        this.waitOpenUtterance(null);
        return this.getInboundOpen();
    }

    protected ArrayList<ACLMessage> blockingDialogue(ACLMessage msg) {
        ArrayList<ACLMessage> res = new ArrayList();
        Info("Opening uttterance ");
        this.Dialogue(msg);
        if (isInitiator(msg.getPerformative())) {
            while (this.isOpen(msg) || this.DM.getAllAnswersTo(msg).size() == 0) {
                Info("Waiting to close utterance");
                this.waitOpenUtterance(msg);
            }
            res = this.getAnswersTo(msg);
//            this.close(msg);
            this.forget(msg);
        }
        if (DM.getPrevUtterance(msg) != null) {
            DM.getPrevUtterance(msg).close();
//            this.forget(DM.getPrevUtterance(msg).getStarter());
        }
        return res;

    }

//    protected ArrayList<ACLMessage> waitAnswersTo(ACLMessage msg) {
//        while (this.isOpen(msg)) {
//            Info("Waiting to close utterance");
//            this.waitOpenUtterance(msg);
//        }
//        this.close(msg);
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
        return LARVAcreateReply(m);
    }

    public ACLMessage LARVAreplySender(ACLMessage m) {
        ACLMessage res = LARVAcreateReply(m);
        res.clearAllReceiver();
        res.addReceiver(m.getSender());
        return res;
    }

    public void ignoreMessage(ACLMessage msg) {

    }

    //Legacy
    @Override
    protected boolean doLARVACheckin() {
        Info("Checking-in to LARVA");
        if (DFGetAllProvidersOf("IDENTITY").isEmpty()) {
            Error("Unable to checkin at LARVA no identity manager service has been found");
        } else {
            if (mypassport == null || mypassport.length() == 0) {
                this.Error("Please load the passport first");
                return false;
            }
            this.addMilestone("MILES16");
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
                checkout = LARVAcreateReply(checkin);
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

    @Override
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

//    protected String askTransponder(String toWhom) {
//        outbox = new ACLMessage(ACLMessage.QUERY_REF);
//        outbox.setSender(getAID());
//        outbox.addReceiver(new AID(toWhom, AID.ISLOCALNAME));
//        outbox.setConversationId("TRANSPONDER"+getHexaKey());
//        outbox.setReplyWith(outbox.getConversationId());
//        outbox.setContent("TRANSPONDER");
//        outbox.setProtocol("DROIDSHIP");
//        this.LARVAsend(outbox);
//        inbox = LARVAblockingReceive();
//        if (inbox.getPerformative() == ACLMessage.INFORM) {
//            return inbox.getContent();
//        } else {
//            return "";
//        }
//    }//    protected String askTransponder(String toWhom) {
////        outbox = new ACLMessage(ACLMessage.QUERY_REF);
////        outbox.setSender(getAID());
////        outbox.addReceiver(new AID(toWhom, AID.ISLOCALNAME));
////        outbox.setConversationId("TRANSPONDER");
////        outbox.setReplyWith("TRANSPONDER");
////        outbox.setContent("TRANSPONDER");
////        outbox.setReplyByDate(nextSecs(45).toDate());
////        inBoxes = blockingDialogue(outbox);
////        if (inBoxes.size() > 0 && inBoxes.get(0).getPerformative() == ACLMessage.INFORM) {
////            return inBoxes.get(0).getContent();
////        } else {
////            Alert("No answer to Transponder of " + toWhom);
////            return "";
////        }
////    }
////
    @Override
    public String toString() {
        return DM.toString();
    }
}
