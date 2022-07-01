/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import crypto.Keygen;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class MessageBox {

    public static enum BoxQueue {
        CONVERSATIONID, SENDER, INREPLYTO, PERFORMATIVE
    };
    Semaphore smMaster;
    HashMap<BoxQueue, HashMap<String, Semaphore>> smQueue;
    protected ArrayList<String> masterID;
    protected HashMap<String, ACLMessage> masterMessage;
    protected HashMap<BoxQueue, HashMap<String, ArrayList<String>>> masterQueue;
//    protected HashMap <String, ArrayList<String>> conversationQueue;
//    protected HashMap <String, ArrayList<String>> senderQueue;
//    protected HashMap <String, ArrayList<String>> replyQueue;
//    protected HashMap <String, ArrayList<String>> performativeQueue;

    public MessageBox() {
        masterMessage = new HashMap();
        masterID = new ArrayList();
        masterQueue = new HashMap();
        smMaster = new Semaphore(0);
        smQueue = new HashMap();
        for (BoxQueue bq : BoxQueue.values()) {
            masterQueue.put(bq, new HashMap());
            smQueue.put(bq, new HashMap());
        }

//        senderQueue = new HashMap();
//        replyQueue = new HashMap();
//        performativeQueue = new HashMap();
    }

    public void pushMessage(ACLMessage m) {
        String mid, tag = "ACLMID";
        if (m.getUserDefinedParameter(tag) == null) {
            mid = Keygen.getAlphaNumKey(20);
            m.addUserDefinedParameter(tag, mid);
        }
        mid = m.getUserDefinedParameter(tag);
        masterID.add(mid);
        masterMessage.put(mid, m);
        smMaster.release(1);
        for (BoxQueue q : BoxQueue.values()) {
            tag = getTag(q, m);
            if (masterQueue.get(q).get(tag) == null) {
                masterQueue.get(q).put(tag, new ArrayList());
            }
//            System.out.println("Pushing "+q.name()+" "+ACLMessageTools.fancyWriteACLM(m));
            masterQueue.get(q).get(tag).add(mid);
            if (smQueue.get(q).keySet().contains(tag)) {
                smQueue.get(q).get(tag).release(1);
//                System.out.println("Releasing  "+q.name()+" "+ACLMessageTools.fancyWriteACLM(m));
            }
        }
    }

    public ACLMessage popMessage() {
        ACLMessage res;

        res = masterMessage.get(masterID.get(0));
        removeMessage(res.getUserDefinedParameter("ACLMID"));
        return res;
    }

    public ACLMessage popMessage(BoxQueue q, String value) {
        ACLMessage res;

        res = queryMessage(q, value);
        if (res != null) {
            removeMessage(res.getUserDefinedParameter("ACLMID"));
        }
        return res;
    }

    public int size() {
        return masterID.size();
    }

    public int size(BoxQueue q) {
        return masterQueue.get(q).size();
    }

    public int size(BoxQueue q, String value) {
        if (masterQueue.get(q).keySet().contains(value)) {
            return masterQueue.get(q).size();
        }
        return -1;
    }

    public ArrayList<String> getQueueKeySet(BoxQueue q) {
        return new ArrayList(masterQueue.get(q).keySet());
    }

    public ACLMessage queryMessage() {
        return masterMessage.get(masterID.get(0));
    }

    public ACLMessage queryMessage(BoxQueue q, String value) {
        if (masterQueue.get(q).get(value) == null || masterQueue.get(q).get(value).isEmpty()) {
            return null;
        } else {
            return masterMessage.get(masterQueue.get(q).get(value).get(0));
        }
    }

    protected void removeMessage(String mid) {
        if (masterID.contains(mid)) {
            masterID.remove(mid);
            if (masterMessage.get(mid) != null) {
                masterMessage.remove(mid);
            }
            for (BoxQueue q : BoxQueue.values()) {
                for (String skey : getQueueKeySet(q)) {
                    if (masterQueue.get(q).get(skey).contains(mid)) {
                        masterQueue.get(q).get(skey).remove(mid);
                    }
                }
            }

        }
    }

    protected String getTag(BoxQueue bq, ACLMessage m) {
        switch (bq) {
            case CONVERSATIONID:
                if (m.getConversationId() == null) {
                    return "";
                } else {
                    return m.getConversationId();
                }
            case SENDER:
                return m.getSender().getLocalName();
            case INREPLYTO:
                if (m.getInReplyTo() == null) {
                    return "";
                } else {
                    return m.getInReplyTo();
                }
            case PERFORMATIVE:
                return ACLMessage.getPerformative(m.getPerformative());
            default:
                return "";
        }
    }

//    public void waitMessage() {
//        try {
//            System.out.println("Acquiring in MASTER");
//            this.smMaster.acquire();
//        } catch (Exception ex) {
//            System.err.println("MessageBox :" + ex.toString());
//        }
//    }
//
//    public void waitMessage(BoxQueue q, String value) {
//        if (!smQueue.get(q).keySet().contains(value)) {
//            smQueue.get(q).put(value, new Semaphore(0));
//        }
//        try {
//            System.out.println("Acquiring in "+q+" "+value);
//            this.smQueue.get(q).get(value).acquire();
//        } catch (Exception ex) {
//            System.err.println("MessageBox :" + ex.toString());
//        }
//    }

    @Override
    public String toString() {
        String res = "";
        ACLMessage m;
        res = "MESSAGE BOX " + this.size() + " messages\n";
        for (BoxQueue q : BoxQueue.values()) {
            res += "   QUEUE " + q.name() + " " + this.size(q) + "\n";
            for (String k : this.getQueueKeySet(q)) {
                ArrayList<String> mq = this.masterQueue.get(q).get(k);
                res += "      " + k + " " + mq.size() + " messages\n";
                for (String mid : mq) {
                    res += "         " + ACLMessageTools.fancyWriteACLM(masterMessage.get(mid)) + "\n";
                }
            }
        }
        return res;
    }
}
