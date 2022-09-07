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

    public static enum Status {
        OPEN, OVERDUE, COMPLETE 
    }

    Status myStatus;
    protected String ConversationID, ReplyWith, Initiator, Owner, Content;
    protected ArrayList<String> Receivers;
    protected TimeHandler Deadline, Start;
    protected Utterance Parent;
    protected ArrayList<Utterance> Children;

    protected ArrayList<ACLMessage> Answers;
    protected ACLMessage Starter;
    protected boolean alive, completed;

    public Utterance() {
        Receivers = new ArrayList();
        Answers = new ArrayList();
        alive = true;
        myStatus = Status.OPEN;
    }

    public Utterance(String AgentName, ACLMessage msg) {
        Receivers = new ArrayList();
        Answers = new ArrayList();
        Owner = AgentName;
        alive = true;
        start(msg);
    }

    public Status getMyStatus() {
        return myStatus;
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

    public int pendingReceptions() {
        return Receivers.size();
    }

    public Utterance close() {
        alive=false;
        return this;
    }

    public Utterance start(ACLMessage msg) {
        if (!isInitiator(msg)) {
            return this;
        }
        Starter = msg;
        Initiator = msg.getSender().getLocalName();
        if (Initiator.equals(this.Owner)) {
            for (String rcvrs : ACLMessageTools.getAllReceivers(msg).split(",")) {
                Receivers.add(rcvrs);
            }
        } else {
            Receivers.add(this.Owner);
        }
        try {
            Deadline = new TimeHandler().fromDate(msg.getReplyByDate());
        } catch (Exception ex) {
            Deadline = null;
        }
        Start = new TimeHandler();
        ConversationID = (msg.getConversationId() == null ? "" : msg.getConversationId());
        ReplyWith = (msg.getReplyWith() == null ? "" : msg.getReplyWith());
        Content = (msg.getContent()== null ? "" : msg.getContent());
        myStatus = Status.OPEN;
        return this;

    }

    public boolean isOverDue() {
        return (!this.isOnTime() && !isReady());
    }

    public boolean isAlive() {
        return alive;
    }

//    public boolean isCompleted() {
//        return myStatus==Status.COMPLETED;
//    }

    public boolean isOpen() {
        return pendingReceptions() > 0 && isOnTime();
    }

    public boolean isReady() {
        return pendingReceptions() == 0;
    }

    public boolean fits(ACLMessage answer) {
        return answer.getConversationId().equals(this.ConversationID) && 
                answer.getInReplyTo().equals(this.ReplyWith)
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

    public ArrayList<ACLMessage> getAllAnswers() {
        return new ArrayList(Answers);
    }

    public boolean isOnTime() {
        if (Deadline == null) {
            return true;
        } else {
            TimeHandler now = new TimeHandler();
//            if (now.isAfterEq(Deadline)) {
//                System.out.println("Ontime: " + now.toString() + " - " + Deadline.toString());
//            } else {
//                System.out.println("Ontime: " + now.toString()+ " >>> " + Deadline.toString());
//            }
            return now.isAfterEq(Deadline);
        }
    }

    public Utterance check() {
        if (myStatus == Status.OPEN) {
            if (isOverDue() ){
                myStatus = Status.OVERDUE;        
            }else if (isReady())
                myStatus=Status.COMPLETE;
        }
        return this;
    }

    public String minimalToString() {
        return ACLMessageTools.fancyWriteACLM(this.getStarter(), false) +
                "||ANSW " + this.getAllAnswers().size() + "/" + this.Receivers.size()+
                "||" + myStatus.name()+ " "+(alive?"(+)":"(-)")+"||";
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
        res +=(alive?"(+)":"(-)");
        res += "Message <" + this.getStarter().getContent() + ">"
                + (Initiator.equals(Owner)
                ? " sent to " + ACLMessageTools.getAllReceivers(getStarter())
                : " received from " + Initiator)
                + " RW: "
                + getStarter().getReplyWith() 
                + " IRT: "
                + getStarter().getInReplyTo()
                + " BY " + this.getDeadline() + " ";
        res += "\tReceived answers " + this.getAllAnswers().size() + "/" + this.Receivers.size();
        res += "\t" + myStatus.name();
        return res;
    }
}
