/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools || Templates
 * and open the template in the editor.
 */
package messaging;

import JsonObject.Json;
import JsonObject.JsonObject;
import crypto.Cryptor;
import static crypto.Keygen.getHexaKey;
import static disk.Logger.trimString;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tools.TimeHandler;
import tools.emojis;
import zip.ZipTools;

/**
 *
 * @author lcv
 */
public class ACLMessageTools {

    public static final ArrayList<Integer> INITIATORS = new ArrayList(Stream.of(ACLMessage.REQUEST, ACLMessage.QUERY_IF,
            ACLMessage.QUERY_REF, ACLMessage.SUBSCRIBE, ACLMessage.CFP, ACLMessage.INFORM_REF,
            ACLMessage.CANCEL, ACLMessage.ACCEPT_PROPOSAL).collect(Collectors.toList()));

    public static final ArrayList<Integer> ERRORS = new ArrayList(Stream.of(ACLMessage.REFUSE, ACLMessage.FAILURE).collect(Collectors.toList()));

    public static final ArrayList<Integer> CONTINUERS = new ArrayList(Stream.of(ACLMessage.AGREE, ACLMessage.PROPOSE).collect(Collectors.toList()));
    public static final ArrayList<Integer> CLOSERS = new ArrayList(Stream.of(ACLMessage.REFUSE, ACLMessage.FAILURE, ACLMessage.INFORM,
            ACLMessage.CONFIRM, ACLMessage.DISCONFIRM, ACLMessage.REJECT_PROPOSAL,
            ACLMessage.NOT_UNDERSTOOD).collect(Collectors.toList()));

    // External fields
    public static final String ACLMID = "ACLMID", ACLMRCVDATE = "RECEIVEDDATE", ACLMSNDDATE = "SENDDATE",
            ACLMADMIN = "LARVAADMIN", ACLMROLE = "LARVAROLE", ACLMSTEALTH = "LARVASTEALTH";

//static final ArrayList<Integer> ERRORS = new ArrayList(Stream.of().collect(Collectors.toList()));
//            CONTINUERS[] = {ACLMessage.AGREE, ACLMessage.PROPOSE},
//            CLOSERS[] = {ACLMessage.REFUSE, ACLMessage.FAILURE, ACLMessage.INFORM, 
//                ACLMessage.CONFIRM, ACLMessage.DISCONFIRM, ACLMessage.REJECT_PROPOSAL,
//                ACLMessage.NOT_UNDERSTOOD},
//            ERRORS [] = {ACLMessage.REFUSE, ACLMessage.FAILURE};
//    static final int INITIATORS[] = {ACLMessage.REQUEST, ACLMessage.QUERY_IF, 
//        ACLMessage.QUERY_REF, ACLMessage.SUBSCRIBE, ACLMessage.CFP, ACLMessage.INFORM_REF, 
//        ACLMessage.CANCEL, 
//                ACLMessage.ACCEPT_PROPOSAL},
//            CONTINUERS[] = {ACLMessage.AGREE, ACLMessage.PROPOSE},
//            CLOSERS[] = {ACLMessage.REFUSE, ACLMessage.FAILURE, ACLMessage.INFORM, 
//                ACLMessage.CONFIRM, ACLMessage.DISCONFIRM, ACLMessage.REJECT_PROPOSAL,
//                ACLMessage.NOT_UNDERSTOOD},
//            ERRORS [] = {ACLMessage.REFUSE, ACLMessage.FAILURE};
    public static enum ACLMEncoding {
        NONE, JSON
    }

    public static final String _NULLVAL = "";

    public static String getAllReceivers(ACLMessage msg) {
        String res = "";
        for (Iterator iterator = msg.getAllReceiver();
                iterator.hasNext();) {
            AID r = (AID) iterator.next();
            res += r.getLocalName() + ",";
        }

        return res;
    }

    public static ArrayList<String> getReceiverList(ACLMessage msg) {
        ArrayList<String> res = new ArrayList();
        for (Iterator iterator = msg.getAllReceiver();
                iterator.hasNext();) {
            AID r = (AID) iterator.next();
            res.add(r.getLocalName());
        }
        return res;
    }

    public static ArrayList<String> getReplyToList(ACLMessage msg) {
        ArrayList<String> res = new ArrayList();
        for (Iterator iterator = msg.getAllReplyTo();
                iterator.hasNext();) {
            AID r = (AID) iterator.next();
            res.add(r.getLocalName());
        }
        return res;
    }

    public static JsonObject getJsonContentACLM(ACLMessage m) {
        JsonObject res = new JsonObject();
        if (isJsonACLM(m)) {
            res = Json.parse(m.getContent()).asObject();
        }
        return res;
    }

    public static boolean isJsonString(String s) {
        boolean res = false;

        if (s == null) {
            return false;
        }
        try {
            res = s.length() > 0 && s.charAt(0) == '{' && Json.parse(s) != null;
        } catch (Exception ex) {
            System.err.println("isJsonString() " + ex.toString() + " " + s);
        }
        return res;

    }

    public static boolean isJsonACLM(ACLMessage m) {
        boolean res = false;

        if (m == null || m.getContent() == null) {
            return false;
        }
        return isJsonString(m.getContent());
    }

    public static JsonObject toJsonACLM(ACLMessage msg) {
        JsonObject res = null;
        String receivers = getAllReceivers(msg);

//        ArrayList <AID> receivers = msg.getAllReceiver()
//        for (Iterator iterator = msg.getAllReceiver();
//                iterator.hasNext();) {
//            AID r = (AID) iterator.next();
//            receivers += r.getLocalName() + " ";
//        }
        res = new JsonObject().add(
                "performative", ACLMessage.getPerformative(msg.getPerformative())).add(
                "sender", (msg.getSender() == null ? _NULLVAL : msg.getSender().getLocalName())).add(
                "receiver", (!msg.getAllReceiver().hasNext() ? _NULLVAL : receivers)).add(
                "protocol", (msg.getProtocol() == null ? _NULLVAL : msg.getProtocol())).add(
                "encoding", (msg.getLanguage() == null ? _NULLVAL : msg.getLanguage())).add(
                //                "encoding", (msg.getLanguage() == null ? _NULLVAL : msg.getLanguage().substring(0, 5)+"...")).add(
                "conversation", (msg.getConversationId() == null ? _NULLVAL : msg.getConversationId())).add(
                "in-reply-to", (msg.getInReplyTo() == null ? _NULLVAL : msg.getInReplyTo())).add(
                "reply-with", (msg.getReplyWith() == null ? _NULLVAL : msg.getReplyWith()));
        if (isJsonACLM(msg)) {
            res.add("content", (msg.getContent() == null ? new JsonObject() : Json.parse(msg.getContent()).asObject()));
        } else {
            res.add("content", msg.getContent());
        }
        return res;
    }

    public static String fancyWriteACLM(ACLMessage original, boolean simple) {
        String res = "", sep = "|";
        if (original == null) {
            return emojis.WARNING + "NULL MESSAGE";
        }
        ACLMessage msg = (ACLMessage) original.clone();
        res += (msg.getSender() == null ? _NULLVAL : "||SND" + sep + msg.getSender().getLocalName());
        Iterator it;
        it = msg.getAllReceiver();
        res += "||RCV" + sep;
        while (it.hasNext()) {
            res += ((AID) it.next()).getLocalName() + " ";
        }
        String cnt = "||CNT" + sep;
        if (msg.getContent().startsWith("ZIPDATA")) {
            cnt += ZipTools.ZIPMARK;
        } else if (msg.getContent().startsWith(ZipTools.ZIPMARK)) {
            cnt += ZipTools.ZIPMARK;
        } else if (msg.getContent().startsWith(Cryptor.CRYPTOMARK)) {
            cnt += Cryptor.CRYPTOMARK;
        } else if (isJsonACLM(msg)) {
            cnt += trimString(msg.getContent(), 255);
        } else {
            cnt += msg.getContent();
        }
        res = res + cnt;
        if (!simple) {
            res = "||PFM" + sep + ACLMessage.getPerformative(msg.getPerformative()) + res;
            it = msg.getAllReplyTo();
            if (it.hasNext()) {
                res += "||RPT" + sep;
                while (it.hasNext()) {
                    res += ((AID) it.next()).getLocalName() + " ";
                }
            }
            res += (isNull(msg.getProtocol()) ? _NULLVAL : "||PRT" + sep + msg.getProtocol())
                    + (isNull(msg.getConversationId()) ? _NULLVAL : "||CID" + sep + msg.getConversationId())
                    + (isNull(msg.getEncoding()) ? _NULLVAL : "||ENC" + sep + msg.getEncoding())
                    + (isNull(msg.getReplyWith()) ? _NULLVAL : "||RPW" + sep + msg.getReplyWith())
                    + (isNull(msg.getInReplyTo()) ? _NULLVAL : "||IRT" + sep + msg.getInReplyTo())
                    + (msg.getReplyByDate() == null ? _NULLVAL : "||RPB" + sep + new TimeHandler().fromDate(msg.getReplyByDate()).toString())
                    + (isNull(msg.getLanguage()) ? _NULLVAL : "||LAN" + sep + trimString(msg.getLanguage(), 10))
                    + (isNull(msg.getOntology()) ? _NULLVAL : "||ONT" + sep + msg.getOntology());
            res += "||";
        }
        return res;
    }

    protected static boolean isNull(String s) {
        return s == null || s.length() == 0;
    }

    public static String fancyWriteACLM(ACLMessage original) {
        return fancyWriteACLM(original, false);
    }

    public static String getDetailsLARVA(ACLMessage msg) {
        String res = "";
        if (msg != null && msg.getContent() != null) {
            if (isJsonACLM(msg)) {
                res = getJsonContentACLM(msg).getString("details", msg.getContent());
            } else {
                res = msg.getContent();
            }
        }
        return res;
    }

    public static ACLMessage respondTo(ACLMessage incoming) {
        ACLMessage outgoing = new ACLMessage();
        outgoing.addReceiver(incoming.getSender());
        outgoing.setSender(((AID) incoming.getAllReceiver().next()));
        outgoing.setContent("");
        if (incoming.getConversationId() != null) {
            outgoing.setConversationId(incoming.getConversationId());
        }
        if (incoming.getReplyWith() != null) {
            outgoing.setInReplyTo(incoming.getReplyWith());
        }
        if (incoming.getProtocol() != null) {
            outgoing.setProtocol(incoming.getProtocol());
        }
        return outgoing;
    }

    public static boolean isAnswerTo(ACLMessage incoming, ACLMessage sent) {
        try {
            return incoming.getConversationId().equals(sent.getConversationId())
                    //                    && incoming.getProtocol().equals(sent.getProtocol())
                    && incoming.getInReplyTo().equals(sent.getReplyWith());
        } catch (Exception Ex) {
            return false;
        }
    }

    public static boolean isBadRecipient(ACLMessage msg) {
        String c = msg.getContent().toUpperCase();
        return (c.contains("MTS-ERROR") && c.contains("FAILED TO FIND AGENT"));
    }

    public static ACLMessage secureACLM(ACLMessage incoming) {
        if (incoming == null) {
            return null;
        }
        incoming.setContent((incoming.getContent() == null ? _NULLVAL : incoming.getContent()));
        incoming.setLanguage(incoming.getLanguage() == null ? _NULLVAL : incoming.getLanguage());
        incoming.setConversationId((incoming.getConversationId() == null ? _NULLVAL : incoming.getConversationId()));
        incoming.setOntology((incoming.getOntology() == null ? _NULLVAL : incoming.getOntology()));
        incoming.setReplyWith((incoming.getReplyWith() == null ? getHexaKey() : incoming.getReplyWith()));
        incoming.setInReplyTo((incoming.getInReplyTo() == null ? _NULLVAL : incoming.getInReplyTo()));
        incoming.setProtocol((incoming.getProtocol() == null ? _NULLVAL : incoming.getProtocol()));
        return incoming;
    }

//    public static boolean isDashACL(ACLMessage msg) {
//        return msg.getReplyWith() != null && msg.getReplyWith().contains(LARVADash.MARK);
//    }
//
//    public static ACLMessage cleanDashMark(ACLMessage msg) {
//        msg.setReplyWith(msg.getReplyWith().replace(LARVADash.MARK, ""));
//        return msg;
//    }
//
//    public static ACLMessage addDashMark(ACLMessage msg) {
//        if (msg.getReplyWith() == null) {
//            msg.setReplyWith("");
//        }
//        msg.setReplyWith(msg.getReplyWith() + " " + LARVADash.MARK);
//        return msg;
//    }
    public static AID getMainReceiver(ACLMessage msg) {
        Iterator it;
        it = msg.getAllReceiver();
        if (it.hasNext()) {
            return ((AID) it.next());
        } else {
            return null;
        }
    }

    public static boolean isConversational(ACLMessage msg) {
        if (msg.getConversationId().length() > 0
                && msg.getReplyWith().length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isInitiator(ACLMessage msg) {
//        return msg.getInReplyTo().length()==0;
        return INITIATORS.contains(msg.getPerformative());
//        for (int i : INITIATORS) {
//            if (i == msg.getPerformative()) {
//                return true;
//            }
//        }
//        return false;
    }

    public static boolean isInitiator(int performative) {
        return INITIATORS.contains(performative);
////        return msg.getInReplyTo().length()==0;
//        for (int i : INITIATORS) {
//            if (i == performative) {
//                return true;
//            }
//        }
//        return false;
    }

    public static boolean isContinuer(ACLMessage msg) {
        for (int i : CONTINUERS) {
            if (i == msg.getPerformative()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCloser(ACLMessage msg) {
        for (int i : CLOSERS) {
            if (i == msg.getPerformative()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isZipped(ACLMessage msg) {
        if (msg.getContent().startsWith("ZIPDATA")) {
            return true;
        } else {
            return false;
        }
    }

}
