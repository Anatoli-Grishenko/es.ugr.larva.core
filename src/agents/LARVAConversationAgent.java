/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import messaging.ACLMessageTools;
import messaging.MessageBox;
import messaging.MessageBox.BoxQueue;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class LARVAConversationAgent extends LARVAFirstAgent {

    protected MessageBox mailbox;

    @Override
    public void setup() {
        super.setup();
        mailbox = new MessageBox();
    }

//    public void activateMessageQueue() {
//
//        this.addBehaviour(new Behaviour() {
//            @Override
//            public void action() {
//                ACLMessage pushMessage = LARVAblockingReceive();
//                Info("Queuing " + ACLMessageTools.fancyWriteACLM(pushMessage));
//                mailbox.pushMessage(pushMessage);
//            }
//
//            @Override
//            public boolean done() {
//                return isExit();
//            }
//        });
//    }
//
    public int queueSize() {
        return mailbox.size();
    }

    public int queueSize(BoxQueue q) {
        return mailbox.size(q);
    }

    public int queueSize(BoxQueue q, String value) {
        return mailbox.size(q, value);
    }

    public ACLMessage queueQuery() {
        return mailbox.queryMessage();
    }

    public ACLMessage queueQuery(BoxQueue q, String value) {
        return mailbox.queryMessage(q, value);
    }

    public ACLMessage queueQuery(BoxQueue q, int value) {
        return mailbox.queryMessage(q, ACLMessage.getPerformative(value));
    }

    public void Push(ACLMessage m) {
        mailbox.pushMessage(m);
    }

    public ACLMessage Pop() {
        return mailbox.popMessage();
    }

    public ACLMessage Pop(BoxQueue q, String value) {
        return mailbox.popMessage(q, value);
    }

    protected ACLMessage blockingPop(BoxQueue q, String value) {
        ACLMessage res = null, aux;
        do {
            if (this.queueSize(q, value) > 0) {
                Info("Found message in " + q.name() + " " + value);
                res = Pop(q, value);
            } else {
                Info("Waiting for message in " + q.name() + " " + value);
                if (queueSize() > 0) {
                    checkBackgroundACLMessages();
                } else {
                    aux = this.LARVAblockingReceive();
                    this.Push(aux);
                }
            }
        } while (res == null);
        return res;
    }

//    protected ACLMessage blockingPop(BoxQueue q, String value) {
//        ACLMessage res = null, aux;
//        do {
//            if (this.queueSize(q, value) > 0) {
//                res = Pop(q, value);
//            } else {
//                Info("Waiting for message in "+q.name()+" "+value);
//                checkBackgroundACLMessages();
//            }
//        } while (res == null);
//        return res;
//    }
//
    protected void checkBackgroundACLMessages() {
    }
}
