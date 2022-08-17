/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.HashMap;
import static messaging.ACLMessageTools.isInitiator;
import static messaging.ACLMessageTools.secureACLM;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DialogueManager extends HashMap<String, HashMap<String, Utterance>> {

    protected ACLMessage currentMessage;
    protected String AgentOwner;
    protected ArrayList<ACLMessage> queue;

    public DialogueManager(String agentName) {
        super();
        queue = new ArrayList();
        AgentOwner = agentName;
    }

    public DialogueManager start(ACLMessage msg) {
        addUtterance(msg);
        currentMessage = msg;
        return this;
    }

    public ACLMessage getCurrentMessage() {
        return currentMessage;
    }

//    public boolean isNewUtterance(ACLMessage msg) {
//        msg = secureACLM(msg);
//        if (get(msg.getConversationId()) == null) {
//            return true;
//        }
//        
//        if (get(msg.getConversationId()).get(msg.getInReplyTo()) == null) {
//            return true;
//        }
//        return false;
//    }
    public boolean isClosedUtterance(ACLMessage answer) {
        answer = secureACLM(answer);
        Utterance utt = this.getUtterance(answer);
        if (utt == null) {
            return true;
        } else {
            return utt.isClosed();
        }
//        if (get(answer.getConversationId()) == null) {
//            return true;
//        }

//        if (isInitiator(answer)) {
//            if (get(answer.getConversationId()).get(answer.getReplyWith()) == null) {
//                return true;
//            }
//            return get(answer.getConversationId()).get(answer.getReplyWith()).isOverDue();
//        } else {
//            if (get(answer.getConversationId()).get(answer.getInReplyTo()) == null) {
//                return true;
//            }
//            return get(answer.getConversationId()).get(answer.getInReplyTo()).isOverDue();
//        }
    }

    public DialogueManager addUtterance(ACLMessage msg) {
        Utterance previous, current;
        this.checkDialogue();
        this.checkinUtterance(msg);
        previous = this.getUtterance(msg);
        if (isInitiator(msg)) {
            if (previous == null) {
                if (get(msg.getConversationId()) == null) {
                    put(msg.getConversationId(), new HashMap());
                }
                if (get(msg.getConversationId()).get(msg.getReplyWith()) == null) {
                    current = new Utterance(AgentOwner, msg);
                    get(msg.getConversationId()).put(msg.getReplyWith(), current);
                }
            } else {
                if (get(msg.getConversationId()) == null) {
                    put(msg.getConversationId(), new HashMap());
                }
                if (get(msg.getConversationId()).get(msg.getReplyWith()) == null) {
                    current = new Utterance(AgentOwner, msg);
                    get(msg.getConversationId()).put(msg.getReplyWith(), current);
                    previous.process(msg);
                    current.setParent(previous);
                }
            }
        } else {
            if (!isClosedUtterance(msg)) {
                getUtterance(msg).process(msg);
            }
        }
        this.checkDialogue();
        if (!msg.getSender().getLocalName().equals(this.AgentOwner)) {
            queue.add(msg);
        }
        System.out.println("DIALOGUE STATUS:" + this.getOpenConversations().length + " pending answers\n" + toString());
        return this;
    }

    public void checkinUtterance(ACLMessage answer) {
        Utterance u = this.getUtterance(answer);
        System.out.println("%%% " + this.AgentOwner + " CHECKIN Message " + Utterance.shorten(answer) + (u == null ? "NEW"
                : "--->" + u.toString()));
    }

    public Utterance getUtterance(ACLMessage answer) {
        try {
            if (isInitiator(answer)) {
                if (answer.getInReplyTo().length()==0)
                return get(answer.getConversationId()).get(answer.getReplyWith());
                else 
                return get(answer.getConversationId()).get(answer.getInReplyTo());                    
            } else {
                return get(answer.getConversationId()).get(answer.getInReplyTo());
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public DialogueManager checkDialogue() {
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                this.get(cid).get(rw).check();
            }
        }
        return this;
    }

    public ACLMessage[] getNewAnswers() {
        ACLMessage res[] = queue.toArray(new ACLMessage[queue.size()]);
        queue.clear();
        return res;
    }

    public ACLMessage[] getUnexpectedRequests() {
        this.checkDialogue();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (!get(cid).get(rw).getInitiator().equals(AgentOwner)) {
                    if (!get(cid).get(rw).isOverDue()) {
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
                    if (get(cid).get(rw).isOverDue()) {
                        pending.add(get(cid).get(rw).getStarter());
                    }
                }
            }
        }
        return pending.toArray(new ACLMessage[pending.size()]);
    }

    public ACLMessage[] getOpenConversations() {
        this.checkDialogue();
        ArrayList<ACLMessage> pending = new ArrayList();
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                if (get(cid).get(rw).isOpen()) {
                    pending.add(get(cid).get(rw).getStarter());
                }

            }
        }
        return pending.toArray(new ACLMessage[pending.size()]);
    }

    @Override
    public String toString() {
        Utterance u;
        String res = "CONVERSATIONS\n";
        for (String cid : this.keySet()) {
            for (String rw : this.get(cid).keySet()) {
                u = get(cid).get(rw);
                if (u.getParent() == null) {
                    res += toString(u, 0);
                }
            }
        }
        res += "\n-------------------------------\n";
        res += "OPEN CONVERSATIONS: " + this.getOpenConversations().length + "\n";
        res += "\tUnexpected: " + this.getUnexpectedRequests().length + "\n";
        res += "\tAnswers: " + this.getMyPendingRequests().length + "\n";
        return res;

    }

    public String toString(Utterance u, int nest) {
        String res = "";
        if (nest == 0) {
            res += "|-- CID:" + u.ConversationID + "\n";
        }
        res += "|"+nesting(nest + 1);
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
