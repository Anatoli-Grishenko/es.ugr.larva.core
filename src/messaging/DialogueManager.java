/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.HashMap;
import javax.net.ssl.SSLEngineResult;
import static messaging.ACLMessageTools.isInitiator;
import static messaging.ACLMessageTools.secureACLM;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DialogueManager extends HashMap<String, HashMap<String, Utterance>> {

    protected String AgentOwner;
    protected ArrayList<ACLMessage> fullQueue;

    public DialogueManager(String agentName) {
        super();
        fullQueue = new ArrayList();
        this.setAgentOwner(agentName);
    }

    public String getAgentOwner() {
        return AgentOwner;
    }

    public void setAgentOwner(String AgentOwner) {
        this.AgentOwner = AgentOwner;
    }

    public DialogueManager start(ACLMessage msg) {
        addUtterance(msg);
        return this;
    }

//    public boolean isClosedUtterance(ACLMessage answer) {
//        answer = secureACLM(answer);
//        Utterance utt = this.getPrevUtterance(answer);
//        if (utt == null) {
//            return true;
//        } else {
//            return utt.getMyStatus() == Utterance.Status.CLOSED;
//        }
//    }
//    public DialogueManager closeUtterance(ACLMessage answer) {
//        answer = secureACLM(answer);
//        Utterance utt = this.getPrevUtterance(answer);
//        utt.close();
//        return this;
//    }
//    public DialogueManager closeConversation(ACLMessage answer) {
//        for (String cid : this.keySet()) {
//            if (cid.equals(answer.getConversationId())) {
//                for (String rw : this.get(cid).keySet()) {
//                    get(cid).get(rw).close();
//                }
//            }
//        }
//        return this;
//    }
    public boolean addUtterance(ACLMessage msg) {
        boolean result = false;
        Utterance previous, current;
        this.checkDialogue();
//        System.out.println(this.AgentOwner + ">>>>>> Start processing " + ACLMessageTools.fancyWriteACLM(msg) + "\n" + this.toString());
        previous = this.getPrevUtterance(msg);
        if (isInitiator(msg)) {
            if (previous == null) {
                if (get(msg.getConversationId()) == null) {
                    put(msg.getConversationId(), new HashMap());
                }
            }
            if (get(msg.getConversationId()).get(msg.getReplyWith()) == null) {
                current = new Utterance(AgentOwner, msg);
                current.setParent(previous);
                get(msg.getConversationId()).put(msg.getReplyWith(), current);
                result = true;
            } else {
                result = false;
            }
        }
        if (!isInitiator(msg) || (result && previous != null)) {
            if (previous.getMyStatus() == Utterance.Status.OPEN) {
                getPrevUtterance(msg).process(msg);
                result = true;
            } else {
                if (msg.getPerformative() == ACLMessage.NOT_UNDERSTOOD) {
                    result = true;
                }
            }
        }
        this.checkDialogue();
        if (!msg.getSender().getLocalName().equals(this.AgentOwner)
                || ACLMessageTools.getMainReceiver(msg).getLocalName().equals(this.AgentOwner)) {
            fullQueue.add(msg);
        }
        if (previous != null && (previous.getMyStatus() == Utterance.Status.COMPLETED
                || previous.getMyStatus() == Utterance.Status.OVERDUE)) {
            previous.close();
        }
//        System.out.println(this.AgentOwner + "<<<<<<< End processing " + ACLMessageTools.fancyWriteACLM(msg) + "\n" + this.toString());
//        if (!msg.getSender().getLocalName().equals(this.AgentOwner)) {
//            fullQueue.add(msg);
//        }
//        System.out.println("DIALOGUE STATUS:" + this.getOpenConversations().length + " pending answers\n" + toString());
        return result;
    }

    public Utterance getMyUtterance(ACLMessage answer) {
        if (get(answer.getConversationId()) != null) {
            return get(answer.getConversationId()).get(answer.getReplyWith());
        } else {
            return null;
        }
//        try {
//            if (isInitiator(answer)) {
//                return get(answer.getConversationId()).get(answer.getReplyWith());
//            } else {
//                return null;
//            }
//        } catch (Exception ex) {
//            return null;
//        }
    }

    public Utterance getPrevUtterance(ACLMessage answer) {
        if (get(answer.getConversationId()) != null) {
            return get(answer.getConversationId()).get(answer.getInReplyTo());
        } else {
            return null;
        }
//        try {
//            if (isInitiator(answer)) {
//                if (answer.getInReplyTo().length() == 0) {
//                    return null;
//                } else {
//                    return get(answer.getConversationId()).get(answer.getInReplyTo());
//                }
//            } else {
//                return get(answer.getConversationId()).get(answer.getInReplyTo());
//            }
//        } catch (Exception ex) {
//            return null;
//        }
    }

    public DialogueManager checkDialogue() {
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                this.get(cid).get(rw).check();
            }
        }
        return this;
    }

//    public ACLMessage[] getNewAnswers() {
//        ArrayList<ACLMessage> pending = new ArrayList();
//        for (ACLMessage msg : fullQueue) {
//            if (!msg.getSender().getLocalName().equals(this.AgentOwner)
//                    || ACLMessageTools.getMainReceiver(msg).getLocalName().equals(this.AgentOwner)) {
//                pending.add(msg);
//            }
//        }
//        return pending.toArray(new ACLMessage[pending.pendingReceptions()]);
//    }
//
//    public int sizeNewAnswers() {
//        int res = 0;
//        for (ACLMessage msg : fullQueue) {
//            if (!msg.getSender().getLocalName().equals(this.AgentOwner)
//                    || ACLMessageTools.getMainReceiver(msg).getLocalName().equals(this.AgentOwner)) {
//                res++;
//            }
//        }
//        return res;
//    }
    public ACLMessage[] getUnexpectedRequests() {
        this.checkDialogue();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (!get(cid).get(rw).getInitiator().equals(AgentOwner)) {
                    if (get(cid).get(rw).getMyStatus() != Utterance.Status.CLOSED) {
                        pending.add(get(cid).get(rw).getStarter());
                    }
                }
            }
        }
        return pending.toArray(new ACLMessage[pending.size()]);
    }

    public ACLMessage[] getMyPendingRequests() {
        this.checkDialogue();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (get(cid).get(rw).getInitiator().equals(AgentOwner)) {
                    if (get(cid).get(rw).getMyStatus() != Utterance.Status.CLOSED) {
                        pending.add(get(cid).get(rw).getStarter());
                    }
                }
            }
        }
        return pending.toArray(new ACLMessage[pending.size()]);
    }

    public ACLMessage[] getOpenDialogues() {
        this.checkDialogue();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (get(cid).get(rw).getMyStatus() != Utterance.Status.CLOSED) {
                    pending.add(get(cid).get(rw).getStarter());
                }

            }
        }
        return pending.toArray(new ACLMessage[pending.size()]);
    }

    public ACLMessage[] getAllAnswersTo(ACLMessage msg) {
        this.checkDialogue();
        ArrayList<ACLMessage> pending = new ArrayList();
        Utterance u = this.getPrevUtterance(msg);
        if (u != null) {
            return u.getAllAnswers();
        } else {
            return new ACLMessage[0];
        }
    }

//    public void dismissACLMessage(ACLMessage msg) {
//        int res = 0;
//        ArrayList aux = new ArrayList();
//        for (ACLMessage m : fullQueue) {
//            if (!m.getUserDefinedParameter("ACLMID").equals(msg.getUserDefinedParameter("ACLMID"))) {
//                aux.add(m);
//            }
//        }
//        fullQueue = aux;
//    }
//    public ArrayList<ACLMessage> getQueue() {
//        return fullQueue;
//    }
    @Override
    public String toString() {
        Utterance u;
        String res = "CONVERSATIONS\n";
        for (String cid : this.keySet()) {
            res += "|-- CID:" + cid + "\n";
            for (String rw : this.get(cid).keySet()) {
                u = get(cid).get(rw);
                if (u.getParent() == null) {
                    res += toString(u, 0);
                }
            }
        }
        res += "\n-------------------------------\n";
        res += "OPEN CONVERSATIONS: " + this.getOpenDialogues().length + "\n";
        res += "\tUnexpected: " + this.getUnexpectedRequests().length + "\n";
        res += "\tAnswers: " + this.getMyPendingRequests().length + "\n";
        return res;

    }

    public String toString(Utterance u, int nest) {
        String res = "";
//        if (nest == 0) {
//            res += "|-- CID:" + u.ConversationID + "\n";
//        }
        res += "|" + nesting(nest + 1);
        res += "|--RW:" + u.ReplyWith + " " + u.toString() + "\n";
        if (u.getChildren() != null) {
            for (Utterance child : u.getChildren()) {
                res += toString(child, nest + 1);
            }
        } else {
            if (u.Answers != null) {
                for (ACLMessage m : u.Answers) {
                    res += nesting(nest + 2) + "|---" + Utterance.shorten(m) + "\n";
                }
            }
        }
        return res;
    }

    protected String nesting(int n) {
        String res = "";
        for (int i = 0; i < n + 1; i++) {
            res += "   ";
        }
        return res;
    }
}
