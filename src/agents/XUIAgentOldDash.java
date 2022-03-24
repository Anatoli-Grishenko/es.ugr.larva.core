/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import data.Ole;
import data.OleFile;
import java.awt.Component;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import messaging.ACLMessageTools;
import swing.LARVAAirTrafficControlTiles;
import swing.LARVAMiniDash;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class XUIAgentOldDash extends LARVAFirstAgent {

    enum Status {
        CHECKIN, CHECKOUT, IDLE, HASSESSION, UPDATE, EXIT
    }
    Status myStatus;

    protected HashMap<String, LARVAMiniDash> AgentDash;
    protected LARVAAirTrafficControlTiles TheMap;
    protected String sessionKey="";
    JPanel _XUI,_Server;

    @Override
    public void setup() {
        super.setup();
        logger.offEcho();
        logger.onTabular();
        myStatus = Status.CHECKIN;
        _XUI = (JPanel) this.payload.getGuiComponents().get("XUI");
        TheMap = new LARVAAirTrafficControlTiles(_XUI);
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
        TheMap.clear();
        return Status.IDLE;
    }

    public Status MyCheckout() {
        this.doLARVACheckout();
        return Status.EXIT;
    }

    public Status myIdle() {
        inbox = this.LARVAblockingReceive();
        Info("Received: "+ACLMessageTools.fancyWriteACLM(inbox, false));
        if (inbox.getContent().contains("filedata")) {
            this.sessionKey = inbox.getConversationId();
            Ole ocontent = new Ole(inbox.getContent());
            OleFile ofile = new OleFile(ocontent.getOle("surface"));
            int maxlevel = ocontent.getInt("maxflight");
            TheMap.clear();
            TheMap.setWorldMap(ofile.toString(), maxlevel, ocontent.getField("palette"));            
        }else
        if (inbox.getContent().contains("perceptions")) {
            TheMap.feedPerception(inbox.getContent());            
        }else
        if (inbox.getContent().contains("goals")) {
            TheMap.feedGoals(inbox.getContent());            
        }
        return Status.IDLE;
    }
    
 }
