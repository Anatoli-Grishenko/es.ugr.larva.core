/**
 * @file DialogicPlayer.java
 */
package agents;

import static agents.BasicPlayer.AutoService;
import static agents.BasicPlayer.Service;
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
public class DialogicPlayer extends LARVADialogicalAgent {
    public static boolean Interactive = false;

    protected int nmessages; //> Message counter
    protected ArrayList<String> Players; //> Set of registered players in the game
    protected ArrayList<String> Receivers; //> Set of all receivers of the same message
    protected ArrayList<String> Unanswered; //> Agents who have not answered yet
    protected boolean useDeadlines = false; //> If true, outgoing messges will have an expiration date, after which, ay answer will be ignored
    protected int secs = 30; //> Baseline for the deadlines system. Deadlines would be in the range [secs,2*secs]

    /**
     * Different states of the agent
     */
    protected enum Status {
        WAIT, //> Espera inicial o intermedia si hiciese falta
        SEND, //> Enviar un mensaje a otros (pueden ser varios) para que nos respondan
        RECEIVE, //> Recibir cualquier mensaje
        ANSWER, //> En caso de que un mensaje recibido sea una petición, responder a ella
        EXIT //> Salida, previa confirmación
    };
    protected Status myStatus;
    protected Dictionary Dict;
    protected String whoController;

    @Override
    /**
     * Configuration of regular parameters
     */
    public void setup() {
        super.setup();
        deactivateSequenceDiagrams();
        whoController = DFGetAllProvidersOf("CONTROLLER").get(0);
        setFixedReceiver(whoController);
        myStatus = Status.WAIT;
        Dict = new Dictionary();
        Unanswered = new ArrayList();
        Dict.load("config/ES.words"); ///> We are using the Spanish dictionary with 70K+ words
        logger.offEcho();
    }

    /**
     * Typical execution body
     */
    @Override
    public void Execute() {
        if (getNCycles()%25 == 0) {
            reset();
        }
        Info("Status: " + myStatus.name() + " outbound open=" + this.getOutboundOpen().size()
                + "   inbound open=" + getInboundOpen().size());
        this.checkDialogues();
        Players = findPlayers();
        Receivers = new ArrayList();
        for (String s : Players) {
            if (!Unanswered.contains(s)) {
                Receivers.add(s);
            }
        }
        ArrayList<String> filter = new ArrayList();
        for (String s : Unanswered) {
            if (!Players.contains(s)) {
                filter.add(s);
            }
        }
        Unanswered.removeAll(filter);

//        Info("DIALOGUE"+DM.toString());
//        Info("Status: " + myStatus.name() + " " + nmessages + " pending answers");
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
        getOut(); //> Before takinf the agent down, it gets out the game
        super.takeDown();
    }

    /**
     * Introduces the agent inn the play, just by registering it in DF
     */
    public void getIn() {
        if (!DFHasService(getLocalName(), Service)) {
            DFAddMyServices(new String[]{Service});
        }
        if (!DFHasService(getLocalName(), AutoService)) {
            DFAddMyServices(new String[]{AutoService});
        }
    }

    /**
     * Excludes the agent from the play, simply by removing all services
     */
    public void getOut() {
        if (DFHasService(getLocalName(), Service)) {
            DFRemoveMyServices(new String[]{Service});
        }
        if (DFHasService(getLocalName(), AutoService)) {
            DFRemoveMyServices(new String[]{AutoService});
        }
    }

    /**
     * Empty WAIT status controller
     *
     * @return By default, the next status
     */
    public Status myWait() {
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

    protected void reset() {
        Info("Reseting ...");
//        getOut();
        outbox = new ACLMessage(ACLMessage.CANCEL);
        outbox.setSender(getAID());
        outbox.addReceiver(new AID(whoController, AID.ISLOCALNAME));
        outbox.setContent("");
        Dialogue(outbox);
        forget(outbox);
        for (ACLMessage op : getOutboundOpen()) {
            forget(op);
        }
        LARVAwait(500);
        Unanswered = new ArrayList();
//        getIn();

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
        } else {
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
            if (Interactive) {
                w = SwingTools.inputLine("SEND A WORD\n\n\nPlease intro a word in Spanish");
            } else {
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
    public ACLMessage answerTo(ACLMessage m, Dictionary dict, boolean interactive) {
        ACLMessage answer = m.createReply();
        String word = selectWord(dict, m.getContent());
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


//
//    /**
//     * It allows to select a set of names (multiple) from a wide list of names
//     *
//     * @param values The original set of names
//     * @param multiple If true, multiple selection isi allowed, if false, only a
//     * single selection is allowed.
//     * @return A subset of @a values
//     */
//    public ArrayList<String> selectReceivers(ArrayList<String> values, boolean multiple) {
//        ArrayList<String> res = new ArrayList();
//        OleConfig ocfg = new OleConfig(), oList = new OleConfig();
//        oList.setField("Players", new ArrayList(values));
//        Ole options = new Ole();
//        options.setField("Players", oList);
//        ocfg.set("options", options);
//        Ole properties = new Ole();
//        properties.setField("Players", new Ole().setField("multiple", multiple).setField("tooltip", "Please select your rival(s)"));
//        ocfg.set("properties", properties);
//        OleDialog odlg = new OleDialog(null, "Select Player");
//        if (odlg.run(ocfg)) {
//            ocfg = odlg.getResult();
//            return ocfg.getProperties().getOle("Players").getArray("selected");
//        } else {
//            return null;
//        }
//    }
//
//    /**
//     * This method select the most appropriate word.If previous is null, then
// it selects a starting word. Otherwise it selects a chained word.
//     *
//     * @param previous The previous word
//     * @param interactive When interactive, it asks the user for the word
//     * otherwise it selects a word automatically
//     * @return The next word
//     */
//    public String selectWord(String previous, boolean interactive) {
//        String w;
//        if (previous == null || previous.length() == 0) {
//            if (interactive) {
//                w = inputLine("SEND A WORD\n\n\nPlease intro a word in Spanish");
//            } else {
//                w = Dict.findFirstWord();
//            }
//
//        } else {
//            if (interactive) {
//                w = inputLine("ANSWER TO A WORD\n\n\nPlease intro a word in Spanish to answer to " + previous + "\nSuggestions:" + Dict.findNextWords(previous, 5).toString());
//            } else {
//                w = Dict.findNextWord(previous);
//            }
//        }
//        Info("Select word " + w);
//        return w;
//    }
//
//    /**
//     * Given a message that has been received with a request, it ellaborates an
//     * answer to it, but does not send it yet
//     *
//     * @param m The message received
//     * @return The prepared answer
//     */
//    public ACLMessage answerTo(ACLMessage m) {
//        ACLMessage answer = m.createReply();
//        String word = selectWord(m.getContent(), false);
//        if (word != null) {
//            answer.setContent(word);
//            return answer;
//        } else {
//            return null;
//        }
//    }
//
//    /**
//     * It gives a random deadline between the required limists (both included)
//     *
//     * @param minsecs Minimum number of seconds
//     * @param maxsecs Maximum number of seconds
//     * @return A deadline in [minsecs. maxsecs]
//     */
//    public Date getDeadline(int minsecs, int maxsecs) {
//        return nextSecs(secs + (int) (Math.random() * secs)).toDate();
//    }
//
//    /**
//     * It randomly rolls a dice and returns true if the dices is good, false
//     * otherwise
//     *
//     * @param threshold A dice is good if its probability is equal or greater to
//     * this value
//     * @return True or false as explained above
//     */
//    public boolean rollDice(double threshold) {
//        return Math.random() > threshold;
//    }
    public String myMethod() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

}
