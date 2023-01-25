/**
 * @file BasicPlayer.java
 */
package agents;

import static appboot.XUITTY.HTMLColor.Green;
import static appboot.XUITTY.HTMLColor.Red;
import static appboot.XUITTY.HTMLColor.White;
import data.Ole;
import data.OleConfig;
import glossary.Dictionary;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import swing.OleDialog;
import swing.SwingTools;
import static tools.TimeHandler.nextSecs;
import tools.emojis;

/**
 *
 * Main Super Class. It contains the tools for doing everythig, send, receive
 * and asnwer, but it actually does not use anythig -
 */
public abstract class DialogicPlayer extends LARVADialogicalAgent {

    public static final String Service = "PLAYER", ///> Register in the DF for every player
            AutoService = "NPC" + Service, ///> Service for NPCs
            Protocol = "CHAINEDWORDS", ///> Name of the game
            Conversation = "GAME"; ///> The game
    public static boolean Interactive = true;

    protected int nmessages; ///> Message counter
    protected ArrayList<String> Players; ///> Set of registered players in the game
    protected ArrayList<String> Receivers; ///> Set of all receivers of the same message
    protected boolean useDeadlines = false; ///> If true, outgoing messges will have an expiration date, after which, ay answer will be ignored
    protected int secs = 30; ///> Baseline for the deadlines system. Deadlines would be in the range [secs,2*secs]
    protected ArrayList<ACLMessage> answers, requests;

    /**
     * Different states of the agent
     */
    protected enum Status {
        WAIT, ///> Espera inicial o intermedia si hiciese falta
        SEND, ///> Enviar un mensaje a otros (pueden ser varios) para que nos respondan
        RECEIVE, ///> Recibir cualquier mensaje
        ANSWER, ///> En caso de que un mensaje recibido sea una petición, responder a ella
        EXIT ///> Salida, previa confirmación
    };
    protected Status myStatus;
    protected Dictionary Dict;

    @Override
    /**
     * Configuration of regular parameters
     */
    public void setup() {
        super.setup();
        openXUITTY(); ///> Uses the XUI as a terminal
        deactivateSequenceDiagrams();
        String who = DFGetAllProvidersOf("CONTROLLER").get(0);
        setFixedReceiver(who); //> Always send copies of messages to this agent
        myStatus = Status.WAIT;
        Dict = new Dictionary(); ///> Load Spanish Dictionary
        Dict.load("config/ES.words"); ///> We are using the Spanish dictionary with 70K+ words
        getIn(); ///> Register as a known player
        answers = new ArrayList();
        requests = new ArrayList();
        onInteractive();
    }

    /**
     * Typical execution body
     */
    @Override
    public void Execute() {
        Info("Status: " + myStatus.name() + " " + nmessages + " pending answers");
        printXUI();
        switch (myStatus) {
            case WAIT:
                myStatus = myWait(); ///> 1st
                break;
            case SEND:
                myStatus = mySend(); ///> 2nd 
                break;
            case RECEIVE:
                myStatus = myReceive(); ///>  3rd
                break;
            case ANSWER:
                myStatus = myAnswer(); ///> 4th
                break;
            case EXIT: ///> emergency
                if (Confirm("Do you want to exit?")) {
                    doExit();
                } else {
                    myStatus = Status.WAIT;
                }
                break;
        }
    }

    @Override
    /**
     * Destroy the agent
     */
    public void takeDown() {
        getOut(); ///> Before taking the agent down, it gets out the game
        super.takeDown();
    }

    /**
     * Print the basic status on a sort of terminal in the XUI area
     */
    public void printXUI() {
        int w = 25, h = 13, xsent = 5, xrec = xsent + w + 5, ysent = 5;
        xuitty.clearScreen();
        xuitty.textColor(Green);
        xuitty.doFrameTitle("SENT", xsent - 1, ysent - 1, w, h);
        xuitty.setCursorXY(xsent, ysent);
        if (outbox != null) {
            xuitty.print(outbox.getContent());
        }
//        xuitty.doFrameTitle("RECEIVED ASNWERS", xsent - 1, ysent + h + 1, w, h);
//        xuitty.setCursorXY(xsent, ysent + h + 2);
//        for (ACLMessage m : getOutboundDue()) {
//            for (ACLMessage n : getAnswersTo(m)) {
//                xuitty.print(emojis.BLACKSQUARE);
//            }
//        }
        xuitty.textColor(Red);
        xuitty.doFrameTitle("RECEIVED", xrec - 1, ysent - 1, w, h);
        xuitty.setCursorXY(xrec, ysent);
        if (getInboundOpen().isEmpty() && getOutboundDue().isEmpty()) {
            xuitty.print("XXX");
        } else {
            int i = 0;
            for (ACLMessage m : getInboundOpen()) {
                xuitty.setCursorXY(xrec, ysent + i);
                xuitty.print(m.getContent());
                i++;
            }
            for (ACLMessage m : getOutboundDue()) {
                for (ACLMessage ms : getAnswersTo(m)) {
                    xuitty.setCursorXY(xrec, ysent + i);
                    xuitty.print(m.getContent());
                    i++;
                }
            }
        }
        xuitty.render();
    }

    /**
     * Introduces the agent inn the play, just by registering it in DF
     */
    public void getIn() {
        if (!DFHasService(getLocalName(), Service)) {
            DFAddMyServices(new String[]{Service});
        }
    }

    /**
     * Excludes the agent from the play, simply by removing all services
     */
    public void getOut() {
        if (DFHasService(getLocalName(), Service)) {
            DFRemoveMyServices(new String[]{Service});
        }

    }

    /**
     * Empty WAIT status controller
     *
     * @return By default, the next status
     */
    public Status myWait() {
        Players = findPlayers();
        return Status.SEND;
    }

    /**
     * Empty SEND status controller
     *
     * @return By default, the next status
     */
    public Status mySend() {
        return Status.RECEIVE;
    }

    /**
     * Empty RECEIVE status controller
     *
     * @return By default, the next status
     */
    public Status myReceive() {
        return Status.ANSWER;
    }

    /**
     * Empty ANSWER status controller
     *
     * @return By default, the next status
     */
    public Status myAnswer() {
        return Status.WAIT;
    }

    /**
     * It reads the full DF and makes a list with all agents registered, without
     * my own name
     *
     * @return A list of agent names registered in the game, ecluding my name
     */
    public ArrayList<String> findPlayers() {
        ArrayList<String> res = DFGetAllProvidersOf(Service);
        if (res.contains(getLocalName())) {
            res.remove(getLocalName());
        }
        Collections.sort(res);
        return res;
    }

    /**
     * It allows to select a set of names (multiple) from a wide list of names
     *
     * @param values The original set of names
     * @param multiple If true, multiple selection isi allowed, if false, only a
     * single selection is allowed.
     * @return A subset of @a values
     */
    public ArrayList<String> selectReceivers(ArrayList<String> values, boolean multiple) {
        ArrayList<String> res = new ArrayList();
        ///> If interactive, it opens a popup Dialogue with the user
        if (Interactive) {
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
                return null;
            }
        } else { ///> Otherwise it selects things automatically
            if (!values.isEmpty()) {
                Collections.shuffle(values);
                res.add(values.get(0));
                if (multiple) {
                    for (String s : values) {
                        if (!res.contains(s) && rollDice(0.5)) {
                            res.add(s);
                        }
                    }
                }
            }
            return res;

        }
    }

    /**
     * This method select the most appropriate word.If previous is null, then it
     * selects a starting word.Otherwise it selects a chained word.
     *
     * @param dict The dictionary from where to extract words
     * @param previous The previous word
     * @return The next word
     */
    public String selectWord(Dictionary dict, String previous) {
        String w;
        if (previous == null || previous.length() == 0) {
            ///> If interactive, it opens a popup Dialogue to ask user
            if (Interactive) {
                w = SwingTools.inputLine("SEND A WORD\n\n\nPlease intro a word in Spanish");
            } else { ///> Otherwise it reacts automatically
                w = dict.findFirstWord();
            }

        } else {
            if (Interactive) {
                w = SwingTools.inputLine("ANSWER TO A WORD\n\n\nPlease intro a word in Spanish to answer to " + previous + "\nSuggestions:" + dict.findNextWords(previous, 5).toString());
            } else {
                w = dict.findNextWord(previous);
            }
        }
        return w;
    }

    /**
     * Given a message that has been received with a request, it ellaborates an
     * answer to it, but does not send it yet
     *
     * @param m The message received
     * @param dict The dictionary from where to extract words
     * @return The prepared answer
     */
    public ACLMessage answerTo(ACLMessage m) {
        ACLMessage answer = LARVAcreateReply(m);
        String word = selectWord(Dict, m.getContent());
        if (word != null) {
            answer.setContent(word);
            return answer;
        } else {
            return null;
        }
    }

    /**
     * It gives a random deadline between the required limists (both included)
     *
     * @param minsecs Minimum number of seconds
     * @param maxsecs Maximum number of seconds
     * @return A deadline in [minsecs. maxsecs]
     */
    public Date getDeadline(int minsecs, int maxsecs) {
        return nextSecs(minsecs + (int) (Math.random() * maxsecs)).toDate();
    }

    /**
     * It randomly rolls a dice and returns true if the dices is good, false
     * otherwise
     *
     * @param threshold A dice is good if its probability is equal or greater to
     * this value
     * @return True or false as explained above
     */
    public boolean rollDice(double threshold) {
        return Math.random() > threshold;
    }
   public static void onInteractive(){
       Interactive=true;
   }
   public static void offInteractive(){
       Interactive=false;
   }
}
