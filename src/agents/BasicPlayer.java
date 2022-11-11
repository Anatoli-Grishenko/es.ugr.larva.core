/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import agents.LARVADialogicalAgent;
import agents.LARVAFirstAgent;
import appboot.XUITTY;
import appboot.XUITTY.HTMLColor;
import static appboot.XUITTY.HTMLColor.Blue;
import static appboot.XUITTY.HTMLColor.DodgerBlue;
import static appboot.XUITTY.HTMLColor.Gray;
import static appboot.XUITTY.HTMLColor.Green;
import static appboot.XUITTY.HTMLColor.Red;
import static appboot.XUITTY.HTMLColor.White;
import data.Ole;
import data.OleConfig;
import glossary.Dictionary;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import messaging.ACLMessageTools;
import swing.OleDialog;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class BasicPlayer extends LARVAFirstAgent {

    protected ArrayList<String> Players;
    protected String Service = "PLAYER", AutoService="PLAYER AUTO";

    protected enum Status {
        WAIT, SEND, RECEIVE, ANSWER, EXIT
    };
    protected Status myStatus;
    protected Dictionary Dict;

    @Override
    public void setup() {
        super.setup();
        setFixedReceiver("CONTROLLER");
        myStatus = Status.WAIT;
        Dict = new Dictionary();
        Dict.load("config/ES.words");
    }

    @Override
    public void Execute() {
        Info("Status: "+myStatus.name());
        switch (myStatus) {
            case WAIT:
                myStatus = myWait();
                break;
            case SEND:
                myStatus = mySend();
                break;
            case RECEIVE:
                myStatus = myReceive();
                break;
            case ANSWER:
                myStatus = myAnswer();
                break;
            case EXIT:
                doExit();
                break;
        }
    }

    public void getIn() {
        if (!DFHasService(getLocalName(), Service)) {
            DFSetMyServices(new String[]{Service});
        }
    }

    public void getOut() {
        DFRemoveAllMyServices();
    }

    public Status myWait() {
        return Status.SEND;
    }

    public Status mySend() {
//        findPlayers();
//        ArrayList<String> receivers = selectReceivers(Players, true);
//        if (receivers.size() > 0) {
//            outbox = new ACLMessage(ACLMessage.QUERY_IF);
//            outbox.setSender(getAID());
//            outbox.setConversationId("DBA");
//            outbox.setReplyWith(getLocalName());
//            outbox.setContent(selectWord(null));
//            for (String name : selectReceivers(Players, true)) {
//                outbox.addReceiver(new AID(name, AID.ISLOCALNAME));
//            }
//            LARVAsend(outbox);
//            return Status.RECEIVE;
//        } 
        return Status.RECEIVE;
    }

    public Status myReceive() {
        return Status.ANSWER;
    }

    public Status myAnswer() {
        return Status.SEND;
    }

    public void findPlayers() {
        Players = DFGetAllProvidersOf(AutoService);
        if (Players.contains(getLocalName())) {
            Players.remove(getLocalName());
        }
        Collections.sort(Players);
    }

    public ArrayList<String> selectReceivers(ArrayList<String> values, boolean multiple) {
        ArrayList<String> res = new ArrayList();
        OleConfig ocfg = new OleConfig(), oList = new OleConfig();
        oList.setField("Players", new ArrayList(values));
        Ole options = new Ole();
        options.setField("Players", oList);
        ocfg.set("options", options);
        Ole properties = new Ole();
        properties.setField("Players", new Ole().setField("multiple", multiple).setField("tooltip", "Please select your rival(s)"));
        ocfg.set("properties", properties);
        OleDialog odlg = new OleDialog(null, "Select Player");
        if (odlg.run(ocfg)) {
            ocfg = odlg.getResult();
            return ocfg.getProperties().getOle("Players").getArray("selected");
        } else {
            return new ArrayList<String>();
        }
    }

    public String selectWord(String word) {
        String w;
        if (word == null || word.length() == 0) {
            w = inputLine("PLease intro a word in Spanish");

        } else {
            w = Dict.findNextWord(word);
        }
        Info("Select word " + w);
        return w;
    }

    public boolean rollDice(double threshold) {
        return Math.random() > threshold;
    }

}
