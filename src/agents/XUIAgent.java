/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import ai.Mission;
import data.Ole;
import data.OleConfig;
import data.OleFile;
import disk.Logger;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import messaging.ACLMessageTools;
import profiling.Profiler;
import swing.OleDashBoard;
import swing.TelegramBackdoor;
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
    protected String sessionKey = "", telegramBot;
    protected boolean showTrail = false;
    protected int trailSize = 0;
    JPanel _XUI, _Server;
    boolean profiler = false, verbose = false;
    int nmessages = 0, miliswait, milisnext, sizeraw, sizezip, nagents = 0,
            totaldistance = 0, totaltargets = 0, runSecs = 60;
    long Milis = 0;
    TimeHandler premesg, postmesg, start;
    TelegramBackdoor tgb;
    boolean zip;
    String content;
    String Profilerprefix;

    @Override
    public void setup() {
        super.setup();
        logger = new Logger();
        if (verbose) {
            logger.onEcho();
            this.deactivateSequenceDiagrams();
            logger.onOverwrite();
            logger.onTabular();
        } else {
//            logger.offEcho();
            this.deactivateSequenceDiagrams();
        }
        E.setVerbose(verbose);
        setSecuredMessages(false);
//        logger.setLoggerFileName(this.getLocalName() + ".json");
        myStatus = Status.CHECKIN;
        _XUI = (JPanel) this.payload.getGuiComponents().get("XUI");
        myDashBoard = new OleDashBoard(_XUI, "XUI");
        myDashBoard.setMyXUIAgent(this);
        myDashBoard.setPreferredSize(new Dimension(1600, 800));
        myDashBoard.verbose = verbose;
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
        this.ignoreExceptions = false;
        this.allowEaryWarning = false;
//        if (this.DFGetAllProvidersOf("STUDENTS").size() > 0) {
//            telegramBot = this.DFGetAllProvidersOf("STUDENTS").get(0);
//        } else {
//            telegramBot = "";
//        }
//        tgb = new TelegramBackdoor("Telgram", (s) -> this.backSendTelegram(s));
//        tgb.setPreferredSize(new Dimension(100, 200));
        loadSessionAlias();
        // Profiling
        OleConfig oProfiler = new OleConfig();
        oProfiler.loadFile("config/profiler.json");
        Profilerprefix = "profiler/" + oProfiler.getString("C", "X") + oProfiler.getInt("S", 0) + "S" + oProfiler.getInt("K", 0) + "K" + oProfiler.getInt("NN", 0) + "NN";
        getMyCPUProfiler().setActive(true);
        getMyCPUProfiler().setOwner("XUI");
        activateMyCPUProfiler(Profilerprefix + "-" + "XUI-CPU");
        getMyNetworkProfiler().setActive(true);
        getMyNetworkProfiler().setOwner("XUI");
        activateMyNetworkProfiler(Profilerprefix + "-" + "XUI-NETWORK");

        myDashBoard.refProfiler.setOwner("DASHBOARD");
        myDashBoard.refProfiler.setTsvFileName(Profilerprefix + "-" + "DASHBOARD.tsv");
        myDashBoard.refProfiler.setActive(getMyCPUProfiler().isActive());
    }

    @Override
    public void Execute() {
        Info("Status: " + myStatus.name());
        getMyCPUProfiler().setSeries(myStatus.name());
        getMyNetworkProfiler().setSeries(myStatus.name());
        switch (myStatus) {
            case CHECKIN:
                myStatus = MyCheckin();
                break;
            case IDLE:
//                try {
                myStatus = myIdle();
//            } catch (Exception ex) {
//                myStatus = Status.IDLE;
//            }
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
        if (getMyCPUProfiler().isActive()) {
            getMyCPUProfiler().close();
        }
        if (getMyNetworkProfiler().isActive()) {
            getMyNetworkProfiler().close();
            getMyNetworkProfiler().saveAll(Profilerprefix + "-NETWORK.tsv");
            
        }
        myDashBoard.closeProfiler();
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
        zip = false;
        inbox = this.LARVAblockingReceive(5000);
        if (inbox != null) {
            if (!getMyNetworkProfiler().isActive()) {
                getMyNetworkProfiler().setActive(true);
                getMyNetworkProfiler().setOwner(inbox.getSender().getLocalName());
                getMyNetworkProfiler().setTsvFileName("XUI-SessionManager.tsv");
            }
//        Info(">>>>>>>>>>>>>>>>" + inbox.getContent().substring(0, 10));
            if (inbox.getInReplyTo().equals("BCKTLGRM")) {
//                this.tgb.write(inbox.getContent());
                return myStatus;
            } else if (inbox.getContent().startsWith("ZIPDATA")) {
                buffer = inbox.getContent().replace("ZIPDATA", "").split(Mission.sepMissions + Mission.sepMissions);
                zip = true;
            } else {
                buffer = new String[]{inbox.getContent()};
                zip = false;
            }

            for (String rawcontent : buffer) {
                getMyCPUProfiler().profileThis("PREUNZIP", "" + rawcontent.length(),
                        () -> {
                            if (zip) {
                                content = unzipString(rawcontent);
                            } else {
                                content = rawcontent;
                            }
                        });
                getMyCPUProfiler().profileThis("POSTUNZIP", "" + content.length(), () -> {
                    sizezip = inbox.getContent().length();
                    sizeraw = content.length();
                });

//            System.out.println("Received " + sizeraw + "/" + sizezip + " bytes");
//        String content = new Ole().UnzipThis(new Ole(inbox.getContent()));
//        Info("Received: " + ACLMessageTools.fancyWriteACLM(inbox, false));
//        System.out.println("Received: " + ACLMessageTools.fancyWriteACLM(inbox, false));
                if (content.contains("filedata")) {
                    getMyCPUProfiler().profileThis("filedata", "" + content.length(),
                            () -> {

//                System.out.println("Map received");
                                oleConfig.loadFile("config/Configuration.conf");
                                showTrail = oleConfig.getTab("Display").getBoolean("Show trail", false);
                                trailSize = oleConfig.getTab("Display").getInt("Trail length", 0);
                                myDashBoard.setTrailSize(trailSize);
                                myDashBoard.setShowTrail(showTrail);
                                this.sessionKey = inbox.getConversationId();
                                myDashBoard.preProcessACLM(content);
                            });
                } else if (content.contains("perceptions")) {
                    getMyCPUProfiler().profileThis("perceptions", "" + content.length(),
                            () -> {
                                if (verbose) {
                                    Info("\n\nXXXXXXXXXXXXXXXXXXXXXXX\nXUI Agent" + "Perceptions received");
                                }
                                myDashBoard.preProcessACLM(content);
                            });
                } else if (content.contains("city")) {
//                    getMyCPUProfiler().profileThis("cities", "" + content.length(),
//                            () -> {
                    if (verbose) {
                        Info("\n\nXXXXXXXXXXXXXXXXXXXXXXX\nXUI Agent" + "Cities received");
                    }
                    myDashBoard.preProcessACLM(content);
//                            });
                } else if (content.contains("people")) {
//                    getMyCPUProfiler().profileThis("people", "" + content.length(),
//                            () -> {

                    if (verbose) {
                        Info("\n\nXXXXXXXXXXXXXXXXXXXXXXX\nXUI Agent" + "People received");
                    }
                    myDashBoard.preProcessACLM(content);
//                            });
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
//            Milis = start.elapsedTimeMilisecsUntil(new TimeHandler());
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
//        outbox = inbox.createReply();
//        outbox.setContent("");
//        outbox.setPerformative(ACLMessage.INFORM);
//        this.LARVAsend(outbox);
            return Status.IDLE;
        } else {
//            return Status.IDLE;
            if (getMyCPUProfiler().isActive()) {
                return Status.EXIT;
            } else {
                return Status.IDLE;
            }
        }
    }

//    @Override
//    public ACLMessage LARVAblockingReceive() {
//        premesg = new TimeHandler();
//        if (postmesg != null) {
//            this.milisnext = (int) postmesg.elapsedTimeMilisecsUntil(premesg);
//        }
//        ACLMessage res = null;
//        res = super.LARVAblockingReceive();
////        while (res == null) {
////            res = this.LARVAblockingReceive(100);
////        }
//        postmesg = new TimeHandler();
//        miliswait = (int) premesg.elapsedTimeMilisecsUntil(postmesg);
//        nmessages++;
//        return res;
//    }
//
//    public void backSendTelegram(String what) {
//        if (telegramBot.length() > 0) {
//            outbox = new ACLMessage(ACLMessage.REQUEST);
//            outbox.setSender(getAID());
//            outbox.addReceiver(new AID(telegramBot, AID.ISLOCALNAME));
//            outbox.setContent(what);
//            outbox.setReplyWith("BCKTLGRM");
//            outbox.setProtocol("NOTIFICATION");
//            outbox.setEncoding(this.getMypassport());
//            this.LARVAsend(outbox);
////            this.tgb.write(what + "\n");
//        } else {
//            this.Alert("DBA Droid not found in DF services");
//        }
//    }
}
