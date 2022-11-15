/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import agents.LARVADialogicalAgent;
import data.Ole;
import data.OleConfig;
import glossary.Dictionary;
import java.util.ArrayList;
import java.util.Collections;
import swing.OleDialog;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DialogicPlayer extends LARVADialogicalAgent {

    protected ArrayList<String> Players;
    protected String Service = "PLAYER", AutoService = "PLAYER AUTO";

    protected enum Status {
        WAIT, SEND, RECEIVE, ANSWER, EXIT
    };
    protected Status myStatus;
    protected Dictionary Dict;

    @Override
    public void setup() {
        super.setup();
        deactivateSequenceDiagrams();
        setFixedReceiver("CONTROLLER");
        myStatus = Status.WAIT;
        Dict = new Dictionary();
        Dict.load("config/ES.words");
    }

    @Override
    public void Execute() {
        Info("Status: " + myStatus.name());
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
                if (Confirm("Do you want to exit?")) {
                    doExit();
                } else {
                    myStatus=Status.WAIT;
                }
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
        ///
        return Status.SEND;
    }

    public Status mySend() {
        ///
        return Status.RECEIVE;
    }

    public Status myReceive() {
        ///
        return Status.ANSWER;
    }

    public Status myAnswer() {
        return Status.SEND;
    }

    public ArrayList<String> findPlayers() {
        ArrayList<String> res = DFGetAllProvidersOf(Service);
        if (res.contains(getLocalName())) {
            res.remove(getLocalName());
        }
        Collections.sort(res);
        return res;
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
            w = inputLine("Please intro a word in Spanish");

        } else {
            w = inputLine("Please intro a word in Spanish to answer to " + word + "\nSuggestions:" + Dict.findNextWords(word, 5).toString());
        }
        Info("Select word " + w);
        return w;
    }

    public boolean rollDice(double threshold) {
        return Math.random() > threshold;
    }
    public String myMethod() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

}
