/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import data.Ole;
import data.OleConfig;
import glossary.Dictionary;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import swing.OleDialog;
import swing.SwingTools;
import static tools.TimeHandler.nextSecs;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class NPCDP extends NPCDialogic {

    public static boolean Interactive = true;
    static int ninstances = 0;
    static int latency = 2500, deadline = 15, points = 2;
    static double psilent = 0, pdeadline = 0;
    static boolean sentToAll = true;
    static OleConfig oCfg;

    @Override
    public void setup() {
        super.setup();
        Unanswered = new ArrayList();
        openRemote();
    }

    @Override
    public void takeDown() {
        ninstances--;
        getOut();
        super.takeDown();
    }

    @Override
    public void Execute() {
        Info("METHOD: " + myMethod());
        super.Execute();
        this.LARVAwait(latency + (int) (Math.random() * latency));
    }

    @Override
    public Status myWait() {
        Info("METHOD: " + myMethod());
        if (ninstances == 0) {
            oCfg = new OleConfig();
//            oCfg.setField("Send to all", sentToAll);
//            oCfg.setField("Prob. Deadline", pdeadline);
//            oCfg.setField("Deadline secs", deadline);
//            oCfg.setField("Prob. Silent", psilent);
//            oCfg.setField("NPC Delay", latency);
//            oCfg.setField("Max. Points", points);
            oCfg.loadFile("config/Controller.conf");
            OleDialog oOptions = new OleDialog(null, "NPC Options");
            if (oOptions.run(oCfg)) {
                oCfg = oOptions.getResult();
                oCfg.saveAsFile("config/", "Controller.conf", true);
                pdeadline = oCfg.getDouble("Prob. Deadline", pdeadline);
                deadline = oCfg.getInt("Deadline secs", deadline);
                sentToAll = oCfg.getBoolean("Send to all", sentToAll);
                psilent = oCfg.getDouble("Prob. Silent", psilent);
                points = oCfg.getInt("Max. Points", points);
                latency = oCfg.getInt("NPC Delay", latency);
                String controller = LARVADFGetAllProvidersOf("CONTROLLER").get(0);
                outbox = new ACLMessage(ACLMessage.REQUEST);
                outbox.setSender(getAID());
                outbox.addReceiver(new AID(controller, AID.ISLOCALNAME));
                outbox.setContent(oCfg.toPlainJson().toString());
                Dialogue(outbox);
                forget(outbox);
                outbox = null;
            }
        }
        ninstances++;
        getIn();
        outbox = null;
        return Status.SEND;
    }

    @Override
    public Status mySend() {
        Info("METHOD: " + myMethod());
        if (Receivers == null) {
            return Status.WAIT;
        }
        if (!sentToAll) {
            Receivers = selectReceivers(Receivers, true);
        }
        if (Receivers.size() > 0) {
            String word = selectWord(Dict, (String) null);
            outbox = new ACLMessage(ACLMessage.QUERY_IF);
            outbox.setSender(getAID());
            outbox.setConversationId(BasicPlayer.Conversation);
            outbox.setProtocol(BasicPlayer.Protocol);
            outbox.setReplyWith(word);
            outbox.setContent(word);
            if (rollDice(1 - pdeadline)) {
                outbox.setReplyByDate(nextSecs(deadline + (int) (Math.random() * deadline)).toDate());
            }
            for (String name : Receivers) {
                outbox.addReceiver(new AID(name, AID.ISLOCALNAME));
            }
            Info("Send " + word + " to " + Receivers.size());
            Dialogue(outbox);
            Unanswered.addAll(Receivers);
            return Status.RECEIVE;
        } else {
            Info("No receivers found");
            outbox = null;
            return Status.RECEIVE;
        }
    }

    @Override
    public Status myReceive() {
        Info("METHOD: " + myMethod());
        for (ACLMessage ob : getOutboundOpen()) {
            for (ACLMessage m : getAnswersTo(ob)) {
                if (Unanswered.contains(m.getSender().getLocalName())) {
                    Info("Received (open)" + m.getContent() + " from " + m.getSender().getLocalName());
                    Unanswered.remove(m.getSender().getLocalName());
                }
            }
        }
        for (ACLMessage ob : getOutboundDue()) {
            for (ACLMessage m : getAnswersTo(ob)) {
                if (Unanswered.contains(m.getSender().getLocalName())) {
                    Info("Received (due)" + m.getContent() + " from " + m.getSender().getLocalName());
                    Unanswered.remove(m.getSender().getLocalName());
                }
            }
            close(ob);
        }
        return Status.ANSWER;
    }

    @Override
    public Status myAnswer() {
        Info("METHOD: " + myMethod());
        if (hasInboundOpen()) {
            Info("Reading pending request");
            for (ACLMessage m : getInboundOpen()) {
                if (rollDice(1 - psilent)) {
                    Info("Skipping answer to " + m.getContent());
                    forget(m);
                } else {
                    String word = selectWord(Dict, m.getContent());
                    ACLMessage aux = m.createReply();
                    aux.setPerformative(ACLMessage.INFORM);
                    aux.setContent(word);
                    aux.setReplyWith(word);
                    Dialogue(aux);
                    Info("Answering toto " + m.getContent());
                }
            }
            return myStatus;
        } else {
            return Status.SEND;
        }
    }

//    @Override
//    public ArrayList<String> selectReceivers(ArrayList<String> values, boolean multiple) {
//        ArrayList<String> res = new ArrayList();
//        if (!values.isEmpty()) {
//            Collections.shuffle(values);
//            if (sentToAll) {
//                res.addAll(values);
//            } else {
//                res.add(values.get(0));
//                if (values.size() > 1 && rollDice(0.5)) {
//                    res.add(values.get(1));
//                }
//                if (values.size() > 2 && rollDice(0.5)) {
//                    res.add(values.get(2));
//                }
//                if (values.size() > 3 && rollDice(0.5)) {
//                    res.add(values.get(3));
//                }
//                if (values.size() > 4 && rollDice(0.5)) {
//                    res.add(values.get(3));
//                }
//            }
//        }
//        if (res.contains(getLocalName())) {
//            res.remove(getLocalName());
//        }
//        return res;
//    }
}
