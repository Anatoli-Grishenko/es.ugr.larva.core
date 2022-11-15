/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import agents.BasicPlayer;
import agents.DialogicPlayer;
import data.OleConfig;
import glossary.Dictionary;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Collections;
import swing.OleDialog;
import tools.TimeHandler;
import static tools.TimeHandler.nextSecs;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class NPCDialogicPlayer extends DialogicPlayer {

    static int ninstances = 0;
    static int secs = 2500, deadline = 15, points = 2;
    static double psilent = 0, pdeadline = 0;
    static boolean sentToAll = true;
    static OleConfig oCfg;

    @Override
    public void setup() {
        super.setup();
        openRemote();
    }

    @Override
    public void takeDown() {
        ninstances--;
        super.takeDown();
    }
    @Override
    public void Execute() {
        Info("METHOD: "+myMethod());
        super.Execute();
//        Info("DIALOGUES"+DM.toString());
        this.LARVAwait(secs+(int)(Math.random()*secs)+secs);
    }

    @Override
    public Status myWait() {
        Info("METHOD: "+myMethod());
        if (ninstances == 0) {
            oCfg = new OleConfig();
            oCfg.setField("Send to all", sentToAll);
            oCfg.setField("Prob. Deadline", pdeadline);
            oCfg.setField("Deadline secs", deadline);
            oCfg.setField("Prob. Silent", psilent);
            oCfg.setField("NPC Delay", secs);
            oCfg.setField("Max. Points", points);
            OleDialog oOptions = new OleDialog(null, "NPC Options");
            if (oOptions.run(oCfg)) {
                oCfg = oOptions.getResult();
                pdeadline = oCfg.getDouble("Prob. Deadline", pdeadline);
                deadline = oCfg.getInt("Deadline secs", deadline);
                sentToAll = oCfg.getBoolean("Send to all", sentToAll);
                psilent = oCfg.getDouble("Prob. Silent", psilent);
                points = oCfg.getInt("Max. Points", points);
                secs = oCfg.getInt("NPC Delay", points);
                String controller = DFGetAllProvidersOf("CONTROLLER").get(0);
                outbox = new ACLMessage(ACLMessage.REQUEST);
                outbox.setSender(getAID());
                outbox.addReceiver(new AID(controller, AID.ISLOCALNAME));
                outbox.setContent(oCfg.toPlainJson().toString());
                Dialogue(outbox);
                forget(outbox);
                outbox=null;
            }
        }
        ninstances++;
        getIn();
        outbox = null;
        return Status.SEND;
    }

    @Override
    public void getIn() {
        getOut();
        DFSetMyServices(new String[]{Service, AutoService});
    }

    @Override
    public String selectWord(String word) {
        String w;
        if (word == null || word.length() == 0) {
            w = Dict.findFirstWord();
        } else {
            w = Dict.findNextWord(word);
        }
        Info("Select word " + w);
        return w;
    }

    @Override
    public Status mySend() {
        Info("METHOD: "+myMethod());
        if (outbox != null) {
            Info("Cannot send yet.");
            return Status.RECEIVE;
        }
        Players = findPlayers();
        ArrayList<String> receivers = selectReceivers(Players, true);
        if (receivers.size() > 0) {
            String word = selectWord(null);
            outbox = new ACLMessage(ACLMessage.QUERY_IF);
            outbox.setSender(getAID());
            outbox.setConversationId("DBA");
            outbox.setReplyWith(word);
            outbox.setContent(word);
            if (rollDice(1 - pdeadline)) {
                outbox.setReplyByDate(nextSecs(deadline + (int) (Math.random() * deadline)).toDate());
            }
            for (String name : selectReceivers(Players, true)) {
                outbox.addReceiver(new AID(name, AID.ISLOCALNAME));
            }
            Info("Send " + word + " to " + receivers.size());
            Dialogue(outbox);
            return Status.RECEIVE;
        } else {
            Info("No receivers found");
            outbox = null;
            return Status.RECEIVE;
        }
    }

    @Override
    public Status myReceive() {
        Info("METHOD: "+myMethod());
        if (outbox != null && (isComplete(outbox) || isOverdue(outbox))) {
            outbox = null;
            for (ACLMessage m : getAnswersTo(outbox)) {
                Info("Received " + m.getContent());
            }
            return Status.SEND;
        } else {
            Info("Waiting for answers yet. No message read.");
            return Status.ANSWER;
        }
    }

    @Override
    public Status myAnswer() {
        Info("METHOD: "+myMethod());
        if (hasInboundOpen()) {
            Info("Reading pending request");
            for (ACLMessage m : getInboundOpen()) {
                if (rollDice(1 - psilent)) {
                    Info("Skipping answer to " + m.getContent());
                    forget(m);
                } else {
                    String word = selectWord(m.getContent());
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

    @Override
    public ArrayList<String> selectReceivers(ArrayList<String> values, boolean multiple) {
        ArrayList<String> res = new ArrayList();
        if (!values.isEmpty()) {
            Collections.shuffle(values);
            if (sentToAll) {
                res.addAll(values);
            } else {
                res.add(values.get(0));
                if (values.size() > 1 && rollDice(0.5)) {
                    res.add(values.get(1));
                }
                if (values.size() > 2 && rollDice(0.5)) {
                    res.add(values.get(2));
                }
                if (values.size() > 3 && rollDice(0.5)) {
                    res.add(values.get(3));
                }
                if (values.size() > 4 && rollDice(0.5)) {
                    res.add(values.get(3));
                }
            }
        }
        if (res.contains(getLocalName())) {
            res.remove(getLocalName());
        }
        return res;
    }

}
