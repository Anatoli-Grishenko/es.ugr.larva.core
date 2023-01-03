/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import ai.Assignment;
import ai.Layout2D;
import ai.Mission;
import ai.MissionSet;
import ai.Scheduler;
import ai.SchedulerViewer;
import ai.TimeTable;
import crypto.Keygen;
import data.OleConfig;
import geometry.Point3D;
import jade.lang.acl.ACLMessage;
import java.awt.Color;
import static java.awt.Component.TOP_ALIGNMENT;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import swing.OleApplication;
import swing.OleDialog;
import swing.OleDrawPane;
import swing.OleScrollPane;
import swing.SwingTools;
import world.Thing;
import world.ThingSet;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class SSD extends DroidShip {

    protected String[] problems = new String[]{
        "CoruscantApr",
        "CoruscantNot",
        "CoruscantSob",
        "CoruscantHon",
        "CoruscantSingle",
        "Wobani.Apr1",
        "Wobani.Not1",
        "Wobani.Sob1",
        "Wobani.Hon1",
        "Endor.Sob1",
        "Endor.Sob2",
        "Endor.Hon1",
        "Endor.Hon2",
        "Dagobah.Apr1",
        "Dagobah.Apr2",
        "Dagobah.Not1",
        "Dagobah.Not2"
    };

    protected Scheduler aproblem = new Scheduler();
    protected TimeTable asolution = new TimeTable();
    protected Layout2D l = new Layout2D();
    protected ThingSet ts, Team, Missions;
    protected Assignment aux;
    protected SchedulerViewer view;
    protected ArrayList<String> viewIds = new ArrayList();

    protected OleDrawPane opDiagram;
    protected OleScrollPane osDiagram;
    protected Semaphore waitS = new Semaphore(0);
    protected Icon icon;

    protected ArrayList<String> names;
    protected HashMap<String, JProgressBar> Progress;
    protected HashMap<String, Mission> AssignedGoals;
    protected HashMap<String, MissionSet> AssignedMissions;
    protected HashMap<String, Mission> Assignments;
    protected boolean emergencyexit = false;
    protected boolean exit = false, mydebug=false;
    protected ArrayList<String> inCrew = new ArrayList(),
            outCrew = new ArrayList();
    protected HashMap<String, JLabel> MyLabel, MyHits;
    protected HashMap<String, JProgressBar> MyCaptures, Mytransfers;

    @Override
    public void setup() {
        Slave = false;
        super.setup();
        myType = "SSD";
        this.DFAddMyServices(new String[]{"TYPE " + myType});
        logger.onEcho(); 
        showPerceptions=false;
        deactivateSequenceDiagrams();
//        logger.offEcho();
        l.loadFromTSVFile("data/Cities.tsv");
        aproblem.setMap(l);
        ts = new ThingSet();
        ts.loadFromTSVFile("data/Census.tsv");
        aproblem.setDeliveries(ts);
        this.DFAddMyServices(new String[]{
            "OPENER",
            "CONTROLLER"});
        Progress = new HashMap();
        openGUI();
        mydebug = false;
        debugDroid=false;
    }

    @Override
    public Status MyCheckin() {
        try {
            setButtons(0);
            waitS.acquire();
            setButtons(10000);
        } catch (Exception ex) {
        }
        Info("Loading passport and checking-in to LARVA");
        if (!doLARVACheckin()) {
            Error("Unable to checkin");
            return changeStatus(Status.EXIT);
        }
        myApp.cleanStatus();
        myApp.addStatus(this.userName + "   |   ");
        try {
            setButtons(1);
            waitS.acquire();
            setButtons(10000);
        } catch (Exception ex) {
        }
        if (this.DFGetAllProvidersOf(service).isEmpty()) {
            Error("Service PMANAGER is down");
            return changeStatus(Status.CHECKOUT);
        }
        problemManager = this.DFGetAllProvidersOf(service).get(0);
        Info("Found problem manager " + problemManager);
        problem = this.inputSelect("Please select problem to solve", problems, problem);
        defSessionAlias(inputLine("Please type in session ALIAS"));
//        defSessionAlias(Keygen.getHexaKey());
//        problem = "CoruscantApr";
        if (problem == null) {
            return changeStatus(Status.CHECKOUT);
        }
        this.outbox = this.respondTo(null, ACLMessage.REQUEST,
                "Request open " + problem + " alias " + sessionAlias, problemManager);
        open = blockingDialogue(outbox).get(0);
        Info(problemManager + " says: " + open.getContent());
        content = open.getContent();
        contentTokens = content.split(" ");
        if (contentTokens[0].toUpperCase().equals("AGREE")) {
            sessionKey = contentTokens[4];
            session = blockingDialogue().get(0);
            sessionManager = session.getSender().getLocalName();
            Info(sessionManager + " says: " + session.getContent());
            this.DFAddMyServices(new String[]{
                getSessionAlias(), "OWNER " + sessionAlias});
            myApp.addStatus(this.problem + " alias " + getSessionAlias() + "  |   ");
            return changeStatus(Status.JOINSESSION);
        } else {
            Error(content);
            return changeStatus(Status.CHECKOUT);
        }
    }

    @Override
    public Status MyJoinSession() {
        if (DFGetAllProvidersOf("TEAM " + getSessionAlias()).isEmpty()) {
            LARVAwait(500);
            return myStatus;
        }
        try {
            setButtons(2);
            waitS.acquire();
            setButtons(3);
        } catch (Exception ex) {
        }
        ArrayList<String> jnames = DFGetAllProvidersOf("TEAM " + getSessionAlias());
        Collections.sort(jnames);
        Team = new ThingSet();
        Thing t;
        String message, name, type, city, gps;
        for (String s : jnames) {
            Info("Recruiting " + s);
            sTransponder = this.askTransponder(s);
            if (sTransponder.length() == 0) {
                message = "Sorry," + s + " your transponder is not available";
                this.Dialogue(this.respondTo(null, ACLMessage.REFUSE, message, s));
                Alert(message);
                continue;
            }
            try {
                name = getTransponderField(sTransponder, "NAME");
                gps = this.getTransponderField(sTransponder, "GPS");
                city = getTransponderField(sTransponder, "STATUS").replace("GROUNDED ", "");
                type = getTransponderField(sTransponder, "TYPE");
                if (name.length() == 0 || city.length() == 0 || gps.length() == 0 || type.length() == 0) {
                    message = "Sorry," + s + " your transponder is not correct. You are being ignored";
                    this.Dialogue(this.respondTo(null, ACLMessage.REFUSE, message, s));
                    Alert(message);
                    continue;
                }
                t = new Thing(name);
                t.setBelongsTo(city);
                pTarget = new Point3D(gps);
                t.setPosition(pTarget);
                t.setType(type);
                t.setCapacity(0);
                t.setMaxCapacity(5);
                t.setAvailable(true);
                Team.addThing(t);
                Info("Added new crew member " + t.toString());
            } catch (Exception ex) {
                message = "Sorry, " + s + " your transponder seems to be corrupted";
                this.Dialogue(this.respondTo(null, ACLMessage.FAILURE, message, s));
                Alert(message);
                return Status.CHOOSEMISSION;
            }
        }
        message = "This is the team I have found:\n" + Team.getAllNames().toString();
//        publish(message);
        if (mydebug && !Confirm(message)) {
            return myStatus;
        }

        Info("Checking list of cities with " + sessionManager);
//        this.doQueryCities();
        this.doQueryMissions();
        Missions = new ThingSet();
        for (String smission : E.getAllMissions()) {
            for (String sgoal : E.getMissionGoals(smission)) {
                String[] amission = sgoal.split(" ");
                if (amission[0].equals("CAPTURE")) {
                    t = new Thing(sgoal);
                    t.setCapacity(Integer.parseInt(amission[1]));
                    t.setType(amission[2]);
                    t.setBelongsTo(amission[3] + (amission.length > 4 ? (" " + amission[4]) : ""));
                    Missions.addThing(t);
                }
            }
        }
        message = "These are the missions I have found:\n" + Missions.getAllNames().toString();
//        if (mydebug && !Confirm(message)) {
//            return Status.CHECKOUT;
//        }
        outbox = this.respondTo(session, ACLMessage.REQUEST, "Request join session " + sessionKey + " in Lebesby", null);
        session = blockingDialogue(outbox).get(0);
        if (session.getPerformative() != ACLMessage.CONFIRM) {
            Error("Could not join session " + sessionKey + " due to " + session.getContent());
            return changeStatus(Status.CHECKOUT);
        }
        this.MyReadPerceptions();
        doNPCs();
        try {
            waitS.acquire();
            setButtons(4);
        } catch (Exception ex) {
        }
        aproblem = new Scheduler();
        l = new Layout2D();
        l.loadFromTSVFile("data/Cities.tsv");
        aproblem.setMap(l);
        ts = new ThingSet();
        ts.loadFromTSVFile("data/Census.tsv");
        aproblem.setDeliveries(ts);
        aproblem.setResources(Team);
        aproblem.setOrder(Missions);

        System.out.println(Team.toString());
        System.out.println(Missions.toString());
        asolution = aproblem.solveShortest();

//        for (String agentname : aproblem.getResources().getAllNames()) {
//            System.out.println("Plan completo para el agente " + agentname);
//            System.out.println(asolution.getSolution(agentname));
//        }
//        view = new SchedulerViewer(asolution);
//        view.Open();
//        view.View();
        Assignments = new HashMap();
        AssignedGoals = new HashMap();
        AssignedMissions = new HashMap<>();
        ArrayList<String> anames = new ArrayList(aproblem.getResources().getAllNames());
        Collections.sort(anames);
        for (String agentname : anames) {
            AssignedGoals.put(agentname, asolution.getFullMission(agentname));
            AssignedMissions.put(agentname, asolution.getMissionSet(agentname));
            int ncaptures = 0;
            for (String s : AssignedGoals.get(agentname).getAllGoals()) {
                String ss[] = s.split(" ");
                if (ss[0].equals("CAPTURE")) {
                    ncaptures += Integer.parseInt(ss[1]);
                }
            }
            message = "Capture briefing to " + agentname + "\n"
                    + AssignedMissions.get(agentname).toString().replace(";", "\n");
            MyCaptures.get(agentname).setMaximum(ncaptures);
            Mytransfers.get(agentname).setMaximum(ncaptures);
//            if (mydebug) {
//                Message(message);
//            }
        }
        inCrew = new ArrayList(aproblem.getResources().getAllNames());
        return Status.SOLVEMISSION;
    }

    @Override
    public Status MySolveProblem() {
        Mission amission;
        String message;
        for (String s : names) {
            if (!AMSIsConnected(s)) {
                MyLabel.get(s).setForeground(Color.RED);
                MyLabel.get(s).setText(MyLabel.get(s).getText());
            }
        }
        for (String sagent : inCrew) {
            outCrew.clear();
            if (Assignments.get(sagent) == null
                    && AssignedMissions.get(sagent).size() > 0) {
                ArrayList<String> aux = AssignedMissions.get(sagent).getAllMissionNames();
                Collections.sort(aux);
                String nextmission = aux.get(0);
                Assignments.put(sagent, AssignedMissions.get(sagent).getMission(nextmission));
                AssignedMissions.get(sagent).removeMission(nextmission);
                outbox = respondTo(null, ACLMessage.REQUEST, Assignments.get(sagent).toString(), sagent);
                outbox.setReplyWith(Assignments.get(sagent).getName());
                outbox.setProtocol(AssignedMissions.get(sagent).toString());
                Dialogue(outbox);
                message = "ON DUTY " + sagent + "\n" + Assignments.get(sagent).toString().replace(";", "\n");
                if (mydebug) {
                    Message(message);
                }
//                MyLabel.get(sagent).setText("<html>" + nextmission.toString().replace(";", "<br>") + "</html>");
//                MyHits.get(sagent).setText("<html></html>");
            } else {
                if (Assignments.get(sagent) == null) {
                    outCrew.add(sagent);
                }
            }
        }
        for (String sout : outCrew) {
            inCrew.remove(sout);
            outbox = respondTo(null, ACLMessage.CANCEL, "Bye" + sout + "!", sout);
            Dialogue(outbox);
            close(outbox);
            message = "OFF DUTY " + sout + "\nThanks for your service!";
            if (mydebug) {
                Message(message);
            }
        }
        outCrew.clear();
        for (ACLMessage answers : getOutboundDue()) {
            ACLMessage answer = getAnswersTo(answers).get(0);
            String aagent = answer.getSender().getLocalName();
            if (Assignments.get(aagent) != null) {
                amission = Assignments.get(aagent);
                if (answer.getPerformative() == ACLMessage.INFORM) {
                    if (answer.getInReplyTo().equals(amission.getName())) {
                        Assignments.remove(aagent);
                        message = "Congrats agent " + aagent + " you have completed mission " + amission.getName();
                        if (mydebug) {
                            Message(message);
                        }
                    }
                }
            } else {
                message = "Sorry, but agent " + aagent + " does not have any mission assigned";
                Dialogue(respondTo(answer, ACLMessage.NOT_UNDERSTOOD, message, aagent));
                Alert(message);
            }

        }

        for (ACLMessage iref : getInboundOpen()) {
            if (iref.getPerformative() == ACLMessage.INFORM_REF) {
                String aagent = iref.getSender().getLocalName();
                if (iref.getContent().startsWith("TRANSFER")) {
                    MyHits.get(aagent).setText(MyHits.get(aagent).getText().replace("</html>", "<b>" + "1x TRANSFER" + "</b>" + "<br></html>"));
                    Mytransfers.get(aagent).setValue(Mytransfers.get(aagent).getValue() + 1);
                    Mytransfers.get(aagent).setString(Mytransfers.get(aagent).getValue() + "x transfers");
                } else if (iref.getContent().startsWith("CAPTURE")) {
                    MyHits.get(aagent).setText(MyHits.get(aagent).getText().replace("</html>", "<u>" + "1x CAPTURE" + "</u>" + "<br></html>"));
                    MyCaptures.get(aagent).setValue(MyCaptures.get(aagent).getValue() + 1);
                    MyCaptures.get(aagent).setString(MyCaptures.get(aagent).getValue() + "x captures");
                } else if (iref.getContent().startsWith("MOVEIN")) {
                    MyHits.get(aagent).setText(MyHits.get(aagent).getText().replace("</html>", iref.getContent().replace("MOVEIN ", "") + "<br></html>"));
                }
            }
            forget(iref);

        }
        if (inCrew.isEmpty() || emergencyexit) {
            return Status.CLOSEMISSION;
        } else {
            return myStatus;
        }
    }

    public void doNPCs() {
//            DroidShip.Debug();
        this.doPrepareNPC(1, DESTL3.class);
//            this.doPrepareNPC(1, VAAT.class);
        this.doPrepareNPC(1, BB1F.class);
//        this.doPrepareNPC(names.size()*3/2, BB1F.class);
//        this.doPrepareNPC((int) (Math.max(1, names.size() - 1)), BB1F.class);
//        this.doPrepareNPC(1, YV.class);
        this.doPrepareNPC(1, MTT.class);
//        this.doPrepareNPC(names.size() / 2 + 1, MTT.class);
    }

    @Override
    protected Status MyCloseMission() {
        String message;
        for (String sout : inCrew) {
            outbox = respondTo(null, ACLMessage.CANCEL, "Bye " + sout + "!", sout);
            Dialogue(outbox);
            close(outbox);
            message = "OFF DUTY " + sout + "\nThanks for your service!";
            if (mydebug) {
                Message(message);
            }
        }
        inCrew.clear();
        this.doDestroyNPC();
        return Status.CHECKOUT;
    }

    @Override
    public Status MyCheckout() {
        if (!emergencyexit) {
            LARVAwait(500);
            return Status.CHECKOUT;
        }
        myApp.dispose();
        outbox = this.respondTo(open, ACLMessage.CANCEL, "Cancel session " + sessionKey, null);
        Info("Closing problem " + problem + ", session " + sessionKey);
        Info("PLAN: " + preplan);
//        this.LARVAsend(outbox);
//        inbox = LARVAblockingReceive();
        inbox = blockingDialogue(outbox).get(0);
        Info(problemManager + " says: " + inbox.getContent());
        this.doLARVACheckout();
        return Status.EXIT;
    }

    public boolean openGUI() {
        OleConfig myConfig = new OleConfig();
        myConfig.loadFile("/resources/config/ssd.app");
        if (myConfig.isEmpty()) {
            SwingTools.Error("Configuration file not found");
            return false;
        }
        myApp = new OleApplication(myConfig) {
            @Override
            public void myActionListener(ActionEvent e) {
                frameActionListener(e);
            }

            @Override
            public void myKeyListener(KeyEvent e) {
            }

            @Override
            public void Draw(Graphics2D g) {
//
            }
        };
//        JPanel p = new JPanel();
//        p.setLayout((new FlowLayout(FlowLayout.LEFT)));
        opDiagram = myApp.getDrawingPane();
        opDiagram.setLayout(new FlowLayout(FlowLayout.LEFT));
//        this.opDiagram.removeAll();
        osDiagram = myApp.getScollPane();
//        myApp.setResizable(false);
//        opDiagram.add(p);
//        Message(what, "/resources/images/" + myType + ".png");
//        SwingTools.toIcon(icon, 50, 50);
        setButtons(1000);
        return true;
    }

    public void frameActionListener(ActionEvent e) {
        if (e.getActionCommand().equals("Load Passport")) {
            String passportfile;
            passportfile = OleDialog.doSelectFile("./", "passport");
//            passportfile = "config/ANATOLI_GRISHENKO.passport";
            if (passportfile != null) {
                this.loadMyPassport(passportfile);
                waitS.release();
            }
        } else if (e.getActionCommand().equals("Open Problem")) {
            waitS.release();
        } else if (e.getActionCommand().equals("Join Session")) {
            waitS.release();
            names = DFGetAllProvidersOf("TEAM " + getSessionAlias());
            Collections.sort(names);
            JPanel jp;
            int i = 0;
            MyLabel = new HashMap<>();
            MyHits = new HashMap();
            MyCaptures = new HashMap();
            Mytransfers = new HashMap();
            int border = 32, gap = 20, fhhead = 14, fhbody = 10, w = (myApp.getWidth() - border) / (names.size());
            Font fhead, fbody;
            fhead = new Font("Monospaced", Font.BOLD, fhhead);
            fbody = new Font("Monospaced", Font.PLAIN, fhbody);
            for (String s : names) {
                JProgressBar jpb;
                jpb = new JProgressBar();
                jpb.setPreferredSize(new Dimension(w, fhhead));
                jpb.setMinimum(0);
                jpb.setMaximum(1);
                jpb.setValue(0);
                jpb.setStringPainted(true);
                jpb.setForeground(new Color(30, 144, 1)); /// Dodger
                opDiagram.add(jpb);
                MyCaptures.put(s, jpb);
            }
            for (String s : names) {
                JProgressBar jpb;
                jpb = new JProgressBar();
                jpb.setPreferredSize(new Dimension(w, fhhead));
                jpb.setMinimum(0);
                jpb.setMaximum(1);
                jpb.setValue(0);
                jpb.setStringPainted(true);
                jpb.setForeground(new Color(255, 127, 80));
                opDiagram.add(jpb);
                Mytransfers.put(s, jpb);
            }

            for (String s : names) {
                JLabel jl;
//                jl = new JLabel("ORDERS " + s);
//                jl.setFont(fhead);
//                jl.setVerticalAlignment(JLabel.TOP);
//                jl.setVerticalTextPosition(JLabel.TOP);
//                jl.setAlignmentY(TOP_ALIGNMENT);
//                jl.setPreferredSize(new Dimension((myApp.getWidth() - border) / (names.size() * 2), gap));
//                opDiagram.add(jl);
                jl = new JLabel(s);
                jl.setFont(fhead);
                jl.setVerticalAlignment(JLabel.TOP);
                jl.setVerticalTextPosition(JLabel.TOP);
                jl.setAlignmentY(TOP_ALIGNMENT);
                jl.setPreferredSize(new Dimension(w, fhhead));
                opDiagram.add(jl);
                jl.setForeground(Color.GREEN);
                MyLabel.put(s, jl);
            }

            for (String s : names) {
                JLabel jl;
//                jl = new JLabel("<html></html>");
//                jl.setFont(fbody);
//                jl.setVerticalAlignment(JLabel.TOP);
//                jl.setVerticalTextPosition(JLabel.TOP);
//                jl.setAlignmentY(TOP_ALIGNMENT);
//                jl.setPreferredSize(new Dimension((myApp.getWidth() - border) / (names.size() * 2), 300));
//                opDiagram.add(jl);

                jl = new JLabel("<html></html>");
                jl.setPreferredSize(new Dimension(w, 300));
                jl.setFont(fbody);
                jl.setVerticalAlignment(JLabel.TOP);
                jl.setVerticalTextPosition(JLabel.TOP);
                jl.setAlignmentY(TOP_ALIGNMENT);
                opDiagram.add(jl);
                MyHits.put(s, jl);
            }
            opDiagram.validate();
            myApp.addStatus(this.names.toString());
        } else if (e.getActionCommand().equals("Schedule Session")) {
            waitS.release();
            myApp.getToolBar().getButton("Schedule Session").setEnabled(false);
        } else if (e.getActionCommand().equals("End of mission")) {
            emergencyexit = true;
        }
    }

    protected Status changeStatus(Status s) {
        if (emergencyexit && s != Status.CHECKOUT) {
            return Status.CLOSEMISSION;
        } else {
            return s;
        }
    }

    protected void setButtons(int stage) {
        if (stage < 1) {
            myApp.getToolBar().getButton("Load Passport").setEnabled(true);
        } else if (stage == 1) {
            myApp.getToolBar().getButton("Load Passport").setEnabled(false);
            myApp.getToolBar().getButton("Open Problem").setEnabled(true);
        } else if (stage == 2) {
            myApp.getToolBar().getButton("Open Problem").setEnabled(false);
            myApp.getToolBar().getButton("Join Session").setEnabled(true);
        } else if (stage == 3) {
            myApp.getToolBar().getButton("Join Session").setEnabled(false);
            myApp.getToolBar().getButton("Schedule Session").setEnabled(true);
        } else if (stage == 4) {
            myApp.getToolBar().getButton("Schedule Session").setEnabled(false);
            myApp.getToolBar().getButton("End of mission").setEnabled(true);
        } else {
            myApp.getToolBar().getButton("Load Passport").setEnabled(false);
            myApp.getToolBar().getButton("Open Problem").setEnabled(false);
            myApp.getToolBar().getButton("Join Session").setEnabled(false);
            myApp.getToolBar().getButton("Schedule Session").setEnabled(false);
            myApp.getToolBar().getButton("End of mission").setEnabled(false);
        }
    }

    @Override
    public void takeDown() {
        super.takeDown();
    }
}
