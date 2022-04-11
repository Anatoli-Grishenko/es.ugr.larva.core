/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import data.Ole;
import data.OleFile;
import disk.Logger;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import messaging.ACLMessageTools;
import swing.OleDashBoard;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class XUIAgent extends LARVAFirstAgent {

    enum Status {
        CHECKIN, CHECKOUT, IDLE, HASSESSION, UPDATE, EXIT
    }
    Status myStatus;

    protected OleDashBoard myDashBoard;
    protected String sessionKey = "";
    JPanel _XUI, _Server;

    @Override
    public void setup() {
        super.setup();
        logger = new Logger();
        logger.offEcho();
        logger.onOverwrite();
        logger.onTabular();
        logger.setLoggerFileName(this.getLocalName() + ".json");
        myStatus = Status.CHECKIN;
        _XUI = (JPanel) this.payload.getGuiComponents().get("XUI");
        myDashBoard = new OleDashBoard(_XUI, "XUI");
        myDashBoard.setPreferredSize(new Dimension(1600,800));
        _XUI.add(myDashBoard, BorderLayout.WEST);
        Info("Setting Death Star up");
        exit = false;
    }

    @Override
    public void Execute() {
        Info("Status: " + myStatus.name());
        switch (myStatus) {
            case CHECKIN:
                myStatus = MyCheckin();
                break;
            case IDLE:
                myStatus = myIdle();
                break;
            case CHECKOUT:
                myStatus = MyCheckout();
                break;
            case EXIT:
            default:
                exit = true;
                break;
        }

    }

    @Override
    public void takeDown() {
        MyCheckout();
        Info("Taking down and deleting agent");
        super.takeDown();
    }

    public Status MyCheckin() {
        Info("Loading passport and checking-in to LARVA");
        if (!doLARVACheckin()) {
            Error("Unable to checkin");
            return Status.EXIT;
        }
        this.DFSetMyServices(new String[]{"XUI " + userID});
//        TheMap.clear();
        return Status.IDLE;
    }

    public Status MyCheckout() {
        this.doLARVACheckout();
        return Status.EXIT;
    }

    public Status myIdle() {
        inbox = this.LARVAblockingReceive();
//        Info("Received: " + ACLMessageTools.fancyWriteACLM(inbox, false));
//        System.out.println("Received: " + ACLMessageTools.fancyWriteACLM(inbox, false));
        if (inbox.getContent().contains("filedata")) {
            this.sessionKey = inbox.getConversationId();
            myDashBoard.preProcessACLM(inbox.getContent());
        } else if (inbox.getContent().contains("perceptions")) {
            myDashBoard.preProcessACLM(inbox.getContent());            
        } else if (inbox.getContent().contains("goals")) {
            myDashBoard.preProcessACLM(inbox.getContent());
//            TheMap.feedGoals(inbox.getContent());            
        }
        return Status.IDLE;
    }

}
