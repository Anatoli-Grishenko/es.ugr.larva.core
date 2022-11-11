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
import static messaging.ACLMessageTools.ACLMID;
import static messaging.ACLMessageTools.ACLMRCVDATE;
import static messaging.ACLMessageTools.ACLMSNDDATE;
import static messaging.ACLMessageTools.getReceiverList;
import static messaging.ACLMessageTools.isInitiator;
import static messaging.ACLMessageTools.secureACLM;
import tools.TimeHandler;

/**
 *
 * @author7 Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DialogueManager extends HashMap<String, HashMap<String, Utterance>> {

    protected String AgentOwner;
    protected ArrayList<ACLMessage> fullQueue, ignoredQueue, amsQueue;
    protected HashMap<String, Utterance> belongsTo;

    public DialogueManager(String agentName) {
        super();
        fullQueue = new ArrayList();
        ignoredQueue = new ArrayList();
        amsQueue = new ArrayList();
        belongsTo = new HashMap();
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
        String mID = msg.getUserDefinedParameter(ACLMID);

        Utterance previous, current;
        this.checkAllUtterances();
//        System.out.println(this.AgentOwner + ">>>>>> Start processing " + ACLMessageTools.fancyWriteACLM(msg) + "\n" + this.toString());
        if (msg.getSender().getLocalName().equals("ams")) {
            return false;
        }
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
                belongsTo.put(mID, current);
                result = true;
            } else {
                result = false;
            }
        }
        if (!isInitiator(msg) || (result && previous != null)) {
            if (previous == null) {
                result = false;
            } else if (previous.getMyStatus() == Utterance.Status.OPEN) {
                previous.process(msg);
                if (belongsTo.get(mID) == null) {
                    belongsTo.put(mID, previous);
                }
                result = true;
            } else {
                if (msg.getPerformative() == ACLMessage.NOT_UNDERSTOOD) {
                    result = true;
                }
            }
        }
        this.checkAllUtterances();
        if (!msg.getSender().getLocalName().equals(this.AgentOwner)
                || getReceiverList(msg).contains(this.AgentOwner)) {
            fullQueue.add(msg);
        }
//        if (!msg.getSender().getLocalName().equals(this.AgentOwner)
//                || ACLMessageTools.getMainReceiver(msg).getLocalName().equals(this.AgentOwner)) {
//            fullQueue.add(msg);
//        }
//        if (previous != null && previous.isAlive()) {
//            previous.close();
//        }
//        System.out.println(this.AgentOwner + "<<<<<<< End processing " + ACLMessageTools.fancyWriteACLM(msg) + "\n" + this.toString());
//        if (!msg.getSender().getLocalName().equals(this.AgentOwner)) {
//            fullQueue.add(msg);
//        }
//        System.out.println("DIALOGUE STATUS:" + this.getOpenConversations().length + " pending answers\n" + toString());
        if (!result) {
            this.ignoredQueue.add(msg);
        }
        return result;
    }

    public DialogueManager removeUtterance(ACLMessage answer) {
        if (get(answer.getConversationId()) != null) {
            Utterance ut = getMyUtterance(answer);
//            if (ut.getParent() != null) {
//                ut.getParent().Children.remove(ut);
//                ut.setParent(null);
//            }
            if (ut != null) {
                get(answer.getConversationId()).remove(ut.getStarter().getReplyWith());
            }
//            if (get(answer.getConversationId()).get(answer.getReplyWith()) != null) {
//                get(answer.getConversationId()).remove(answer.getReplyWith());
//            }
        }
        return this;
    }

    public Utterance getMyUtterance(ACLMessage answer) {
        if (get(answer.getConversationId()) != null) {
            return get(answer.getConversationId()).get(answer.getReplyWith());
        } else {
            return null;
        }
    }

    public Utterance getRawUtterance(ACLMessage answer) {
        if (answer == null) {
            return null;
        }
        return belongsTo.get(answer.getUserDefinedParameter(ACLMID));
    }

    public Utterance getPrevUtterance(ACLMessage answer) {
        if (answer != null && get(answer.getConversationId()) != null) {
            return get(answer.getConversationId()).get(answer.getInReplyTo());
        } else {
            return null;
        }
    }

    public DialogueManager checkAllUtterances() {
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                this.get(cid).get(rw).check();
            }
        }
        return this;
    }

    public ArrayList<ACLMessage> getAllOpen() {
        this.checkAllUtterances();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (get(cid).get(rw).getMyStatus() == Utterance.Status.OPEN) {
//                    pending.add(get(cid).get(rw).getStarter());
                    this.pushBack(pending, get(cid).get(rw).getStarter());
                }
            }
        }
        return pending;
    }

    public ArrayList<ACLMessage> getInboundOpen() {
        this.checkAllUtterances();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (!get(cid).get(rw).getInitiator().equals(AgentOwner)) {
                    if (get(cid).get(rw).getMyStatus() == Utterance.Status.OPEN) {
//                        pending.add(get(cid).get(rw).getStarter());
                        this.pushBack(pending, get(cid).get(rw).getStarter());
                    }
                }
            }
        }
        return pending;
    }

    public ArrayList<ACLMessage> getOutboundOpen() {
        this.checkAllUtterances();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (get(cid).get(rw).getInitiator().equals(AgentOwner)) {
                    if (get(cid).get(rw).getMyStatus() == Utterance.Status.OPEN) {
//                        pending.add(get(cid).get(rw).getStarter());
                        this.pushBack(pending, get(cid).get(rw).getStarter());
                    }
                }
            }
        }
        return pending;
    }

    public ArrayList<ACLMessage> getAllInbound() {
        this.checkAllUtterances();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (!get(cid).get(rw).getInitiator().equals(AgentOwner)) {
                    this.pushBack(pending, get(cid).get(rw).getStarter());
                }
            }
        }
        return pending;
    }

    public ArrayList<ACLMessage> getAllOutbound() {
        this.checkAllUtterances();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (get(cid).get(rw).getInitiator().equals(AgentOwner)) {
                    this.pushBack(pending, get(cid).get(rw).getStarter());
                }
            }
        }
        return pending;
    }

    public ArrayList<ACLMessage> getAllDue() {
        this.checkAllUtterances();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (get(cid).get(rw).getMyStatus() != Utterance.Status.OPEN
                        && get(cid).get(rw).isAlive()) {

//                    pending.add(get(cid).get(rw).getStarter());
                    this.pushBack(pending, get(cid).get(rw).getStarter());
                }
            }
        }
        return pending;
    }

    public ArrayList<ACLMessage> getOutboundDue() {
        this.checkAllUtterances();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (get(cid).get(rw).getInitiator().equals(AgentOwner)) {
                    if (get(cid).get(rw).getMyStatus() != Utterance.Status.OPEN
                            && get(cid).get(rw).isAlive()) {

//                    pending.add(get(cid).get(rw).getStarter());
                        this.pushBack(pending, get(cid).get(rw).getStarter());
                    }
                }
            }
        }
        return pending;
    }

    public ArrayList<ACLMessage> getInboundDue() {
        this.checkAllUtterances();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (!get(cid).get(rw).getInitiator().equals(AgentOwner)) {
                    if (get(cid).get(rw).getMyStatus() != Utterance.Status.OPEN
                            && get(cid).get(rw).isAlive()) {

//                    pending.add(get(cid).get(rw).getStarter());
                        this.pushBack(pending, get(cid).get(rw).getStarter());
                    }
                }
            }
        }
        return pending;
    }

//    public ArrayList<ACLMessage> getAllDue() {
//        this.checkAllUtterances();
//        ArrayList<ACLMessage> pending = new ArrayList();
//        for (String cid : this.keySet()) {
//            for (String rw : this.get(cid).keySet()) {
//                if (get(cid).get(rw).getMyStatus() != Utterance.Status.OPEN
//                        && get(cid).get(rw).isAlive()) {
//                    pending.add(get(cid).get(rw).getStarter());
//                }
//            }
//        }
//        return pending;
//    }
//
    public ArrayList<ACLMessage> getAllAnswersTo(ACLMessage msg) {
        ArrayList<ACLMessage> res = new ArrayList();
        if (msg == null) {
            return res;
        }
        this.checkAllUtterances();
        Utterance u = this.getMyUtterance(msg);
        if (u != null && !u.isOpen()) {
            res = u.getAllAnswers();
        }
        return res;
    }

//    public ArrayList<ACLMessage> getAllAnswersTo(ACLMessage msg) {
//        return this.queryAllAnswersTo(msg);
//        ArrayList<ACLMessage> res = new ArrayList();
//        if (msg == null) {
//            return res;
//        }
//        this.checkAllUtterances();
//        Utterance u = this.getMyUtterance(msg);
//        if (u != null && !u.isOpen() && u.isAlive()) {
//            res = u.getAllAnswers();
////            u.close();
//        }
//        return res;
//    }

    public ArrayList<ACLMessage> queryIgnoredMessages() {
        this.checkAllUtterances();
        return this.ignoredQueue;
    }

    public void clearIgnoredMessages() {
        ignoredQueue.clear();
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
            res += "├─── CID:" + cid + "\n";
            for (String rw : this.get(cid).keySet()) {
                u = get(cid).get(rw);
                if (u.getParent() == null) {
                    res += toString(u, 0);
                }
            }
        }
        res += "\n-------------------------------\n";
        res += "OPEN CONVERSATIONS: " + this.getAllOpen().size() + "\n";
        res += "\tMine: " + this.getOutboundOpen().size() + "\n";
        res += "DUE CONVERSATIONS: " + this.getAllDue().size() + "\n";
        return res;

    }

    public String toString(Utterance u, int nest) {
        String res = "";
//        if (nest == 0) {
//            res += "|-- CID:" + u.ConversationID + "\n";
//        }
        res += "│" + nesting(nest);
        res += "├───> " + u.minimalToString() + "\n";
        if (u.getChildren() != null) {
            for (Utterance child : u.getChildren()) {
                res += toString(child, nest + 1);
            }
        } else {
            if (u.Answers != null) {
                for (ACLMessage m : u.Answers) {
                    res += "│" + nesting(nest + 1) + "├···>" + ACLMessageTools.fancyWriteACLM(m, false) + "\n";
                }
            }
        }
        return res;
    }
//    public String toString(Utterance u, int nest) {
//        String res = "";
////        if (nest == 0) {
////            res += "|-- CID:" + u.ConversationID + "\n";
////        }
//        res += "│" + nesting(nest);
//        res += "├───> RW:" + u.ReplyWith + " " + u.minimalToString(u) + "\n";
//        if (u.getChildren() != null) {
//            for (Utterance child : u.getChildren()) {
//                res += toString(child, nest + 1);
//            }
//        } else {
//            if (u.Answers != null) {
//                for (ACLMessage m : u.Answers) {
//                    res += "│" +nesting(nest + 1) + "├···>" + ACLMessageTools.fancyWriteACLM(m,true) + "\n";
//                }
//            }
//        }
//        return res;
//    }

    protected String nesting(int n) {
        String res = "";
        for (int i = 0; i < n + 1; i++) {
            if (i == 0) {
                res += "     ";
            } else {
                res += "│    ";
            }
        }
        return res;
    }

    protected void pushBack(ArrayList<ACLMessage> list, ACLMessage msg) {
        int i;
        i = 0;
        while (i < list.size() && ltACLMessage(list.get(i), msg)) {
            i++;
        }
        list.add(i, msg);
    }

    protected boolean ltACLMessage(ACLMessage A, ACLMessage B) {
        long lA, lB;
        try {
            if (A.getUserDefinedParameter(ACLMRCVDATE) != null) {
                lA = Long.parseLong(A.getUserDefinedParameter(ACLMRCVDATE));
                lB = Long.parseLong(B.getUserDefinedParameter(ACLMRCVDATE));
            } else {
                lA = Long.parseLong(A.getUserDefinedParameter(ACLMSNDDATE));
                lB = Long.parseLong(B.getUserDefinedParameter(ACLMSNDDATE));
            }
            return lA < lB;
        } catch (Exception ex) {
            return false;
        }
    }
}
