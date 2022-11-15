/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import agents.LARVAFirstAgent;
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
import static tools.TimeHandler.nextSecs;
import tools.emojis;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class BasicPlayer extends LARVAFirstAgent {
    protected int nmessages;
    protected ArrayList<String> Players;
    protected ArrayList<String> Receivers;
    protected String Service = "PLAYER", Word;
    protected boolean useDeadlines=false;
    protected int deadline=30;

    // Descripción de los estados del agente
    protected enum Status {
        WAIT, // Espera inicial o intermedia si hiciese falta
        SEND, // Enviar un mensaje a otros (pueden ser varios) para que nos respondan
        RECEIVE, // Recibir cualquier mensaje
        ANSWER, // En caso de que un mensaje recibido sea una petición, responder a ella
        EXIT // Salida, previa confirmación
    };
    protected Status myStatus;
    protected Dictionary Dict;

    @Override
    public void setup() {
        super.setup();
        openXUITTY();
        deactivateSequenceDiagrams();
        setFixedReceiver("CONTROLLER");
        myStatus = Status.WAIT;
        Dict = new Dictionary();
        // Vamos a utilizar el diccion aroi de español con +70K palabras
        // algunas un poco raras porque se han extraído de libros antiguos (EL Quixote, la Biblia)
        Dict.load("config/ES.words"); 
        getIn();
    }

    // Típico cuerpo de ejecución
    @Override
    public void Execute() {
        Info("Status: " + myStatus.name()+" "+nmessages+" pending answers");
        printScreen();
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
    
    public void printScreen() {
        xuitty.clearScreen();
        xuitty.setCursorXY(1,1);
        xuitty.textColor(White);
        xuitty.print("SENT : ");
        if (outbox == null) {
            xuitty.textColor(Red);
            xuitty.print("X");
        } else {
            xuitty.textColor(Green);
            xuitty.print(outbox.getContent()+"   pending answers: ");
            for (int i=0; i< nmessages; i++) {
              xuitty.print(emojis.BLACKSQUARE);
            }
        }
        xuitty.setCursorXY(1,3);
        xuitty.textColor(White);
        xuitty.print("RECEIVED: ");
        if (inbox==null) {
            xuitty.textColor(Red);
            xuitty.print("X");
        } else {
            xuitty.textColor(Green);
            xuitty.print(inbox.getContent());
        }
         xuitty.render();
    }

    // Me registra en el juego, para ello sólo tengo que apuntarme en el DF
    public void getIn() {
        if (!DFHasService(getLocalName(), Service)) {
            DFSetMyServices(new String[]{Service});
        }
    }

    // Me da de baja en el juego
    public void getOut() {
        DFRemoveAllMyServices();
    }

    // Gestor del estado WAIT
    public Status myWait() {
        return Status.SEND;
    }

    // Gestor del estado SEND
    // Por ahora no hace nada. Se instanciará en las clases inferiores
    public Status mySend() {
        return Status.RECEIVE;
    }

    // Gestor del estado RECEIVE
    // Se activa en cada mensaje recibido
    public Status myReceive() {
        inbox = LARVAblockingReceive();
        return Status.ANSWER;
    }

    // Gestor del estado ANSWER
    // Crea un a respuesta (pregunta la palabra encadenada) y la envía
    public Status myAnswer() {
        // Proceso el mensaje que se había quedado almacenado
        // en inbox y respondo
        ACLMessage aux = answerTo(inbox);
        // En caso de que hala fallado la construcción de la respuesta, se procede a salir.
        if (aux != null) {
            LARVAsend(aux);
            return Status.RECEIVE;
        } else {
            return Status.EXIT;
        }

    }

    // Rastrea el DF y encuentra a todos los agentes registrados
    public ArrayList<String> findPlayers() {
        ArrayList<String> res = DFGetAllProvidersOf(Service);
        if (res.contains(getLocalName())) {
            res.remove(getLocalName());
        }
        Collections.sort(res);
        return res;
    }

    // Presenta un diálogo para seleccionar, entre los Players, a los posibles destinatarios
    // pueden ser uno de ellos, dos de ellos o todos
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
            return null;
        }
    }

    // Ayuda para seleccionar una palabra. Si es una respuesta a una palabra anterior
    // muestra una sugerencia del diccionario a modo de ayuda
    public String selectWord(String word) {
        String w;
        if (word == null || word.length() == 0) {
            w = inputLine("SEND A WORD\n\n\nPlease intro a word in Spanish");

        } else {
            w = inputLine("ANSWER TO A WORD\n\n\nPlease intro a word in Spanish to answer to " + word + "\nSuggestions:" + Dict.findNextWords(word, 5).toString());
        }
        Info("Select word " + w);
        return w;
    }
    
    // Construye un mensaje de respuesta a otro mensaje recibido, listo para modificar algo
    // o enviarlo
    public ACLMessage answerTo(ACLMessage m) {
        ACLMessage answer = m.createReply();
        answer.setPerformative(ACLMessage.INFORM);
        Word = selectWord(m.getContent());
        if (Word != null) {
            answer.setContent(Word);
            answer.setReplyWith(Word);
            if (useDeadlines) {
                answer.setReplyByDate(getDeadline());
            }
            return answer;
        } else {
            return null;
        }
    }

    public Date getDeadline() {
        return nextSecs(deadline+(int)(Math.random()*deadline)).toDate();
    }
    // Tira los dados para generar comportamientos aleatorios
    public boolean rollDice(double threshold) {
        return Math.random() > threshold;
    }

}
