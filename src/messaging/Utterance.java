/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import static messaging.ACLMessageTools.isInitiator;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Utterance {

    protected String ConversationID, ReplyWith, Initiator, Owner;
    protected ArrayList<String> Receivers;
    protected TimeHandler Deadline, Start;
    protected Utterance Parent;
    protected ArrayList<Utterance> Children;

    protected ArrayList<ACLMessage> Answers;
    protected ACLMessage Starter;
    protected boolean closed;

    public Utterance() {
        Receivers = new ArrayList();
        Answers = new ArrayList();
        closed = false;
    }

    public Utterance(String AgentName, ACLMessage msg) {
        Receivers = new ArrayList();
        Answers = new ArrayList();
        Owner = AgentName;
        start(msg);
    }

    
    public Utterance setParent(Utterance u) {
        Parent = u;
        if (u != null) {
            u.addChild(this);
        }
    return this;
    }

    public Utterance getParent() {
        return Parent;
    }

    public ArrayList<Utterance> getChildren() {
        return Children;
    }

    public Utterance addChild(Utterance u) {
        if (Children == null) {
            Children = new ArrayList();
        }
        Children.add(u);
        return this;
    }

    public String getInitiator() {
        return Initiator;
    }

    public TimeHandler getDeadline() {
        return Deadline;
    }

    public TimeHandler getStart() {
        return Start;
    }

    public ACLMessage getStarter() {
        return Starter;
    }

    public int size() {
        return Receivers.size();
    }

    public Utterance abandon() {
        Receivers.clear();
        return this;
    }

    public Utterance close() {
        abandon();
        closed = true;
        return this;
    }

    public Utterance start(ACLMessage msg) {
        if (!isInitiator(msg)) {
            return this;
        }
        Starter = msg;
        for (String rcvrs : ACLMessageTools.getAllReceivers(msg).split(",")) {
            Receivers.add(rcvrs);
        }
        try {
            Deadline = new TimeHandler().fromDate(msg.getReplyByDate());
        } catch (Exception ex) {
            Deadline = null;
        }
        Initiator = msg.getSender().getLocalName();
        Start = new TimeHandler();
        ConversationID = (msg.getConversationId() == null ? "" : msg.getConversationId());
        ReplyWith = (msg.getReplyWith() == null ? "" : msg.getReplyWith());
        closed = false;
        return this;

    }

    public boolean isOverDue() {
        return (!this.isOnTime() || !isOpen());
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isOpen() {
        return size() > 0;
    }

    public boolean fits(ACLMessage answer) {
        return answer.getConversationId() == this.ConversationID && answer.getInReplyTo() == this.ReplyWith
                && Receivers.contains(answer.getSender().getLocalName());
    }

    public Utterance process(ACLMessage msg) {
        if (!fits(msg) || !isOpen()) {
            return this;
        }
        Receivers.remove(msg.getSender().getLocalName());
        Answers.add(msg);
        return this;
    }

    public ACLMessage[] getAllAnswers() {
        return Answers.toArray(new ACLMessage[Answers.size()]);
    }

    public boolean isOnTime() {
        if (Deadline == null) {
            return true;
        } else {
            return new TimeHandler().isAfterEq(Deadline);
        }
    }

    public Utterance check() {
        if (!this.isOnTime()) {
            return this.abandon();
        } else {
            return this;
        }
    }

    public static String shorten(ACLMessage msg) {
        return "Message <" + msg.getContent() + ">"
                + " sent to " + ACLMessageTools.getAllReceivers(msg)
                + " received from " + msg.getSender().getLocalName()
                + " RW: "
                + msg.getReplyWith() + " IRT " + msg.getInReplyTo();
    }

    @Override
    public String toString() {
        String res = "";
        res += "Message <" + this.getStarter().getContent() + ">"
                + (Initiator.equals(Owner)
                ? " sent to " + ACLMessageTools.getAllReceivers(getStarter())
                : " received from " + Initiator)
                + " RW: "
                + getStarter().getReplyWith() + " BY " + this.getDeadline() + " ";
        res += "\tReceived answers " + this.getAllAnswers().length + "/" + this.Receivers.size();
        res += "\t" + (this.isOverDue() ? "CLOSED" : "OPEN");
        return res;
    }
}
