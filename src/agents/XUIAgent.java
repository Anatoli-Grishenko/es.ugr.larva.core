/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import ai.Mission;
import data.Ole;
import data.OleFile;
import disk.Logger;
import jade.lang.acl.ACLMessage;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import messaging.ACLMessageTools;
import swing.OleDashBoard;
import tools.TimeHandler;
import static zip.ZipTools.unzipString;

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
    protected boolean showTrail = false;
    protected int trailSize = 0;
    JPanel _XUI, _Server;
    boolean profiler = false;
    int nmessages = 0, miliswait, milisnext, sizeraw, sizezip, nagents = 0,
            totaldistance = 0, totaltargets = 0, runSecs = 60;
    long Milis = 0;
    TimeHandler premesg, postmesg, start;

    @Override
    public void setup() {
        super.setup();
        logger = new Logger();
        logger.offEcho();
//        logger.onEcho();
//        this.activateSequenceDiagrams();
        logger.onOverwrite();
        logger.onTabular();
//        logger.setLoggerFileName(this.getLocalName() + ".json");
        myStatus = Status.CHECKIN;
        _XUI = (JPanel) this.payload.getGuiComponents().get("XUI");
        myDashBoard = new OleDashBoard(_XUI, "XUI");
        myDashBoard.setMyXUIAgent(this);
        myDashBoard.setPreferredSize(new Dimension(1600, 800));
        _XUI.add(myDashBoard, BorderLayout.WEST);
        _XUI.validate();
        if (oleConfig != null) {
            showTrail = oleConfig.getTab("Display").getBoolean("Show trail", false);
            trailSize = oleConfig.getTab("Display").getInt("Trail length", 0);
            myDashBoard.setTrailSize(trailSize);
            myDashBoard.setShowTrail(showTrail);
        }
        this.doNotExit();
        this.showConsole = false;
        this.frameDelay = 0;
        this.cont = true;
        start = new TimeHandler();
        System.out.println("XUI" + Mission.sepMissions + "Date" + Mission.sepMissions
                + "Milis" + Mission.sepMissions
                + "Num Messages" + Mission.sepMissions
                + "Msg Wait" + Mission.sepMissions
                + "Msg loop" + Mission.sepMissions
                + "Raw" + Mission.sepMissions
                + "zip" + Mission.sepMissions
                + "Num Agents" + Mission.sepMissions
                + "Distance" + Mission.sepMissions
                + "Targets" + Mission.sepMissions
        );
        this.ignoreExceptions=true;
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
                LARVAexit = true;
                break;
        }

    }

    @Override
    public void takeDown() {
        myDashBoard.removeAll();
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
        String buffer[];
        boolean zip = false;
        inbox = this.LARVAblockingReceive();
        Info(">>>>>>>>>>>>>>>>"+inbox.getContent().substring(0, 10));
        if (inbox.getContent().startsWith("ZIPDATA")) {
            buffer = inbox.getContent().replace("ZIPDATA", "").split(Mission.sepMissions + Mission.sepMissions);
            zip = true;
        } else {
            buffer = new String[]{inbox.getContent()};
            zip = false;
        }

        for (String rawcontent : buffer) {
            String content;
            if (zip) {
                content = unzipString(rawcontent);
            } else {
                content = rawcontent;
            }
            sizezip = inbox.getContent().length();
            sizeraw = content.length();
//            System.out.println("Received " + sizeraw + "/" + sizezip + " bytes");
//        String content = new Ole().UnzipThis(new Ole(inbox.getContent()));
//        Info("Received: " + ACLMessageTools.fancyWriteACLM(inbox, false));
//        System.out.println("Received: " + ACLMessageTools.fancyWriteACLM(inbox, false));
            if (content.contains("filedata")) {
//                System.out.println("Map received");
                oleConfig.loadFile("config/Configuration.conf");
                showTrail = oleConfig.getTab("Display").getBoolean("Show trail", false);
                trailSize = oleConfig.getTab("Display").getInt("Trail length", 0);
                myDashBoard.setTrailSize(trailSize);
                myDashBoard.setShowTrail(showTrail);
                this.sessionKey = inbox.getConversationId();
                myDashBoard.preProcessACLM(content);
            } else if (content.contains("perceptions")) {
//                System.out.println("Perceptions received");
                myDashBoard.preProcessACLM(content);
            } else if (content.contains("city")) {
//                System.out.println("Cities received");
                myDashBoard.preProcessACLM(content);
            } else if (content.contains("people")) {
//                System.out.println("People received");
                myDashBoard.preProcessACLM(content);
            }
            _XUI.repaint();
            nagents = myDashBoard.decoderSet.keySet().size();
            int distance = 0, goals = 0;
            for (String s : myDashBoard.decoderSet.keySet()) {
                distance += myDashBoard.decoderSet.get(s).getNSteps();
                goals += (myDashBoard.decoderSet.get(s).getOntarget() ? 1 : 0);
            }
            totaldistance = distance;
            totaltargets += goals;
            Milis = start.elapsedTimeMilisecsUntil(new TimeHandler());
//            System.out.println("XUI" + Mission.sepMissions + TimeHandler.Now() + Mission.sepMissions
//                    + Milis + Mission.sepMissions
//                    + nmessages + Mission.sepMissions
//                    + miliswait + Mission.sepMissions
//                    + milisnext + Mission.sepMissions
//                    + sizeraw + Mission.sepMissions
//                    + sizezip + Mission.sepMissions
//                    + nagents + Mission.sepMissions
//                    + totaldistance + Mission.sepMissions
//                    + totaltargets + Mission.sepMissions
//            );
        }
//        if (Milis / 1000 > runSecs) {
//            return Status.EXIT;
//        }
        return Status.IDLE;
    }

    @Override
    public ACLMessage LARVAblockingReceive() {
        premesg = new TimeHandler();
        if (postmesg != null) {
            this.milisnext = (int) postmesg.elapsedTimeMilisecsUntil(premesg);
        }
        ACLMessage res = null;
        while (res == null) {
            res = this.LARVAblockingReceive(100);
        }
        postmesg = new TimeHandler();
        miliswait = (int) premesg.elapsedTimeMilisecsUntil(postmesg);
        nmessages++;
        return res;
    }
}
