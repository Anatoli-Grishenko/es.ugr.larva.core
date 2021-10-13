/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
 */
package swing;

import com.eclipsesource.json.JsonArray;
import data.Ole;
import data.OleFile;
import glossary.sensors;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import map2D.Map2DColor;
import map2D.Palette;
import messaging.Sentence;
import tools.TimeHandler;
import static world.Perceptor.NULLREAD;
import world.SensorDecoder;
import static world.liveBot.MAXENERGY;
import static world.liveBot.MAXFLIGHT;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class LARVADash {

    public static enum Layout {
        COMPACT, EXTENDED, DASHBOARD
    }
    public static final String MARK = "@DASH@";
    public static final Color cBad = new Color(100, 0, 0), cThing = Color.YELLOW;

    // Layout GUI
    protected LARVAFrame fDashboard;
    protected JPanel pMain, pMiddle, pButton, pRight, pLeft, pLog, pMap;
    protected MyDrawPane dpMyStatus, dpPalette, dpMap;
    protected JScrollPane spMap, spLog, spStatuses;
    protected MyPlainButton bZoomIn, bZoomOut, bPlay, bPause, bStop, bStep, bGame, bAgent;
    protected MyPlainButton bRecharge, bRescue, bUp, bDown, bLeft, bRight, bForward, bCapture;
    protected JTextArea taLog, taStatuses;
    protected JTextPane tpLog;
    protected int iLeft = 400, iRight = 650, iX, iY, iW, iH, iButton = 48,
            iLog = 350, iStatus = 250, iset = 0, iIter = 0, iTrace = 0, iMaxLevel, iMaxDistance, iMaxEnergy = MAXENERGY,
            iFlightw, iFlighth, worldw, worldh, iPalette, lastx = -1, lasty = -1;
    protected JCheckBox cbShowSplash;
    protected MyMapPalPane mpMap, mpVisual, mpLidar, mpThermal;
    protected Layout myLayout;
    protected double dZoom = 3.0;
    protected ImageIcon map;
    protected JLabel lSample;
    protected Map2DColor hFlight; // hVisual
    protected int baseFlight = 0;
    protected int[][] iVisual, iLidar, iThermal;
    protected double[] gps;
    protected RoundProgressBar rpbEnergy, rpbAltimeter, rpbDistance;
    protected JProgressBar pbDistance, pbAltitude, pbMaxlevel;
    protected Angular rpbCompass, rpbAngular;
    protected SensorDecoder lastPerception;
    protected TimeHandler tinit, tnow;
    protected Color cBackgr = new Color(25, 25, 25), cGreen = new Color(32, 178, 170),
            cTrace = new Color(0, 255, 0), cSky = new Color(100, 100, 100),
            cSoil = new Color(204, 102, 0), cDodgerB = new Color(0, 102, 204),
            cStatus = new Color(0, 50, 0), cTextStatus = new Color(0, 200, 0);
    Color[][] mMap, cVisual, cLidar, cThermal;
    HashMap<String, Palette> Palettes;
    protected Semaphore smContinue, smPlay, smStart, smReady;
    protected Semaphore smReadyData, smReadyFX, smAllowAgent;

    int factor = 24, space = 10, skip = 4, stringskip = 18, zoomSensors = 25;

    String family = "phos", splash1 = "TieFighterHelmet", splash2 = "RealTieFighter", name = "unknown",
            palMap, sperception = "";

    // Remote operation
    protected Consumer<String> externalExecutor;
    Agent myAgent;
    ACLMessage dashInbox;
    boolean simulation = false, showsplash, enablesimulation, fulllayout = false, exitdashboard = false, activated = false;
    String splashlock = "./dont.show";

    public LARVADash(Agent a) {
        myLayout = Layout.DASHBOARD;
        lastPerception = new SensorDecoder();
        Palettes = new HashMap();
        myAgent = a;
        smReady = new Semaphore(0);
        smContinue = new Semaphore(0);
        smStart = new Semaphore(0);
        smPlay = new Semaphore(1);
        smReadyData = new Semaphore(0);
        smReadyFX = new Semaphore(1);
        smAllowAgent = new Semaphore(0);
        smContinue = new Semaphore(0);
        File f = new File(splashlock);
        this.showsplash = !f.exists();

        this.whenExecute(s -> this.doExecute(s));
    }

    public boolean preProcessACLM(ACLMessage msg) {
        boolean res = false;
        if (msg.getContent().contains("filedata")) {
            Ole ocontent = new Ole().set(msg.getContent());
            OleFile ofile = new OleFile(ocontent.getOle("surface"));
            int maxlevel = ocontent.getInt("maxflight");
            enablesimulation = ocontent.getBoolean("simulator");
            if (isActivated()) {
                bGame.setEnabled(enablesimulation);
                try {
                    smStart.acquire();
                } catch (Exception ex) {
                }

                SwingTools.doSwingWait(() -> {
                    fullLayout();
                });
            }
            setWorldMap(ofile.toString(), maxlevel, ocontent.getField("palette"));
            res = true;
        }
        if (msg.getContent().contains("perceptions")) {
            if (isActivated()) {
                if (!exitdashboard) {
                    try {
                        smStart.acquire();
                    } catch (Exception ex) {
                    }
                    dashInbox = msg;
                }
            }
            this.feedPerception(msg.getContent());
            res = false;
        }
        return res;
    }

    protected boolean setWorldMap(String olefile, int maxlevel, String spalette) {
        this.lastPerception.setWorldMap(olefile, maxlevel);
        iMaxLevel = maxlevel;
        this.palMap = spalette;
        Palette p;

        worldw = lastPerception.getWorldMap().getWidth();
        worldh = lastPerception.getWorldMap().getHeight();
        if (isActivated()) {
            p = this.getPalette(this.palMap);
            mMap = new Color[worldw][worldh];
            for (int i = 0; i < worldw; i++) {
                for (int j = 0; j < worldh; j++) {
                    if (lastPerception.getWorldMap().getStepLevel(i, j) > maxlevel) {
                        mMap[i][j] = cBad;
                    } else {
                        mMap[i][j] = p.getColor(lastPerception.getWorldMap().getStepLevel(i, j));
                    }
                }
            }
            mpMap.setMap(mMap, p);
        }
        OleFile ofile = new OleFile();
        ofile.set(olefile);
        tinit = new TimeHandler();
        iMaxDistance = worldw + worldh;
        if (isActivated()) {
            this.fDashboard.setTitle("| MAP: " + ofile.getFileName() + "|");
            rpbDistance.setMaxValue(iMaxDistance);
        }
        iFlightw = iRight - factor;
        iFlighth = 125;
        hFlight = new Map2DColor(iFlightw, iFlighth, Color.BLACK);
        for (int i = 0; i < hFlight.getWidth(); i++) {
            for (int j = 0; j < hFlight.getHeight(); j++) {
                if (j % 10 == 0 || i % 10 == 0) {
                    hFlight.setColor(i, j, Color.DARK_GRAY);
                } else {
                    hFlight.setColor(i, j, Color.GRAY);
                }
            }
        }

        refresh();
        return true;
//        OleFile ofile = new OleFile();
//        ofile.set(olefile);
//        this.fDashboard.setTitle("| MAP: " + ofile.getFileName() + "|");
//        hFlight = new Map2DColor(iFlightw, iFlighth, Color.BLACK);
//        for (int i = 0; i < hFlight.getWidth(); i++) {
//            for (int j = 0; j < hFlight.getHeight(); j++) {
//                if (j % 10 == 0 || i % 10 == 0) {
//                    hFlight.setColor(i, j, Color.DARK_GRAY);
//                } else {
//                    hFlight.setColor(i, j, Color.GRAY);
//                }
//            }
//        }
//        tinit = new TimeHandler();
//        iMaxDistance = worldw + worldh;
//        rpbDistance.setMaxValue(iMaxDistance);
//        refresh();
//        return true;
    }

    public void feedPerception(String perception) {
        if (!perception.equals(sperception)) {
            feedPerceptionLocal(perception);
            sperception = perception;
            if (isActivated()) {
                try {
                    smAllowAgent.acquire(1);
                    smContinue.acquire(1);
                } catch (Exception ex) {
                }
            }
        }
    }

    protected void feedPerceptionLocal(String perception) {
        try {
            lastPerception.feedPerception(perception);
            String[] trace = lastPerception.getTrace();
            if (trace != null && isActivated()) { //&& iIter > trace.length && trace.length>0) {
                for (; iTrace < trace.length; iTrace++) {
                    this.addAction(trace[iTrace]);
                }
            }
            name = lastPerception.getName();

            if (lastPerception.hasSensor("GPS")) {
                gps = lastPerception.getGPS();
                lastx = (int) gps[0];
                lasty = (int) gps[1];
                if (lastx >= 0 && isActivated()) {
                    mpMap.setTrail(lastx, lasty, this.getAltitude());
                }

                int shft = 30;
                if (hFlight.getWidth() - shft - 1 == iIter - baseFlight) {
                    hFlight.shiftLeft(shft);
                    baseFlight += shft;
                }
                for (int ih = 0; ih < this.hFlight.getHeight(); ih++) {
                    int y1 = (ih * 256) / hFlight.getHeight(), y2 = (int) (gps[2] - lastPerception.getAltitude());
                    if (y1 < y2) {
                        if (this.myLayout == Layout.DASHBOARD) {
                            hFlight.setColor(iIter - baseFlight, hFlight.getHeight() - ih, cDodgerB);
                        } else {
                            hFlight.setColor(iIter - baseFlight, hFlight.getHeight() - ih, cSoil);
                        }
                    } else {
                        if (this.myLayout == Layout.DASHBOARD) {
                            hFlight.setColor(iIter - baseFlight, hFlight.getHeight() - ih, Color.BLACK);
                        } else {
                            hFlight.setColor(iIter - baseFlight, hFlight.getHeight() - ih, cSky);
                        }
                    }
                    if ((ih * 256) / hFlight.getHeight() > this.iMaxLevel) {
                        hFlight.setColor(iIter, hFlight.getHeight() - ih, cBad);
                    }
                    hFlight.setColor(iIter - baseFlight, hFlight.getHeight() - (int) (hFlight.getHeight() * gps[2] / 256), cTrace);

                }
            }

            if (getNsteps() == 1 && isActivated()) {
                this.fDashboard.setTitle("| Session: " + this.lastPerception.getSession()
                        + " |Agent: " + name + " " + fDashboard.getTitle());
            }
            if (isActivated()) {
                int[][] sensor;
                int rangex, rangey;
                Palette palette;
                if (lastPerception.hasSensor("VISUAL") || lastPerception.hasSensor("VISUALHQ")) {
                    iVisual = lastPerception.getVisualData();
                    sensor = iVisual;
                    rangex = sensor[0].length;
                    rangey = sensor.length;
                    palette = getPalette("Visual");
                    if (cVisual
                            == null) {
                        cVisual = new Color[rangex][rangey];
                        for (int i = 0; i < rangex; i++) {
                            for (int j = 0; j < rangey; j++) {
                                cVisual[i][j] = palette.getColor(0);
                            }
                        }
                        mpVisual.setMap(cVisual, palette);
                    }
                    for (int i = 0;
                            i < rangex;
                            i++) {
                        for (int j = 0; j < rangey; j++) {
                            if (sensor[j][i] > lastPerception.getMaxlevel()) {
                                cVisual[i][j] = cBad;
                            } else {
                                cVisual[i][j] = palette.getColor(sensor[j][i]);
                            }
                        }
                    }
                }
                if (lastPerception.hasSensor("LIDAR") || lastPerception.hasSensor("LIDARHQ")) {
                    sensor = lastPerception.getLidarData();
                    rangex = sensor[0].length;
                    rangey = sensor.length;
                    palette = getPalette("Lidar");
                    if (cLidar
                            == null) {
                        cLidar = new Color[rangex][rangey];
                        for (int i = 0; i < rangex; i++) {
                            for (int j = 0; j < rangey; j++) {
                                cLidar[i][j] = palette.getColor(0);
                            }
                        }
                        mpLidar.setMap(cLidar, palette);
                    }
                    for (int i = 0;
                            i < rangex;
                            i++) {
                        for (int j = 0; j < rangey; j++) {
                            if (sensor[j][i] < 0) {
                                cLidar[i][j] = cBad;
                            } else {
                                cLidar[i][j] = palette.getColor(sensor[j][i]);
                            }
                        }
                    }
                }
                if (lastPerception.hasSensor("THERMAL") || lastPerception.hasSensor("THERMALHQ")) {
                    sensor = lastPerception.getThermalData();
                    iThermal = lastPerception.getThermalData();
                    if (sensor.length
                            > 0) {
                        rangex = sensor[0].length;
                        rangey = sensor.length;
                        palette = getPalette("Thermal");
                        if (cThermal == null) {
                            cThermal = new Color[rangex][rangey];
                            for (int i = 0; i < rangex; i++) {
                                for (int j = 0; j < rangey; j++) {
                                    cThermal[i][j] = palette.getColor(0);
                                }
                            }
                            mpThermal.setMap(cThermal, palette);
                        }
                        for (int i = 0; i < rangex; i++) {
                            for (int j = 0; j < rangey; j++) {
                                cThermal[i][j] = palette.getColor(sensor[j][i]);
                            }
                        }
                    }
                }

                addStatus(lastPerception.getStatus());
                refresh();
            }
            iIter++;
        } catch (Exception ex) {
            System.err.println("Error processing perceptions");

        }
    }

    protected void whenExecute(Consumer<String> executor) {
        externalExecutor = executor;
    }

    protected void goSimulator() {
        simulation = true;
        pButton.removeAll();
        pButton.add(new JLabel(SwingTools.toIcon("./images/blue/blue-TieFighter.png", iButton, iButton)));
        pButton.add(bUp);
        pButton.add(bDown);
        pButton.add(bLeft);
        pButton.add(bRight);
        pButton.add(bCapture);
        pButton.add(bForward);
        pButton.add(bRecharge);
        pButton.add(bStop);
        pButton.validate();
        pButton.repaint();
        bUp.on();
        bDown.on();
        bLeft.on();
        bRight.on();
        bForward.on();
        this.bCapture.on();
        this.bRecharge.on();
        smAllowAgent.release(1); //drainPermits();
        smStart.release(2);
    }

    protected void goReal() {
        simulation = false;
        pButton.removeAll();
        pButton.add(new JLabel(SwingTools.toIcon("./images/red/red-TieFighter.png", iButton, iButton)));
        pButton.add(bPlay);
        pButton.add(bPlay);
        pButton.add(bStop);
        pButton.add(bPause);
        pButton.add(bStep);
        pButton.validate();
        pButton.repaint();
        bPlay.on();
        bPause.off();
        bStop.off();
        bStep.on();
        smAllowAgent.release(1);
        smContinue.release(1);
        smStart.release(2);
    }

    public void initGUI() {
        // Define panels
        fDashboard = new LARVAFrame(e -> this.DashListener(e));
        fDashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        pMain = new JPanel();
        pMain.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));

        pMiddle = new JPanel();
        pMiddle.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        pMiddle.setBackground(cBackgr);

        pLeft = new JPanel();
        pLeft.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        pLeft.setBackground(cBackgr);

        pLeft = new JPanel();
        pLeft.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        pLeft.setBackground(cBackgr);

        pRight = new JPanel();
        pRight.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        pRight.setBackground(cBackgr);

        dpMyStatus = new MyDrawPane(g -> showMyStatus(g));
        dpMyStatus.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        dpMyStatus.setBackground(cBackgr);

        dpMap = new MyDrawPane(null);
        dpMap.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        dpMap.setBackground(cBackgr);

        mpMap = new MyMapPalPane(null);
        mpMap.addRuler();
        mpMap.addTrail();
//        mpMap.addShadow(32);
        mpVisual = new MyMapPalPane(null);
        mpVisual.addRuler();
        mpVisual.addHotSpot();
        mpLidar = new MyMapPalPane(null);
        mpLidar.addRuler();
        mpLidar.addHotSpot();
        mpThermal = new MyMapPalPane(null);
        mpThermal.addRuler();
        mpThermal.addHotSpot();

        bZoomIn = new MyPlainButton("ZoomIn", "zoom_in.png", this.fDashboard);
        bZoomOut = new MyPlainButton("ZoomOut", "zoom_out.png", fDashboard);
        bPlay = new MyPlainButton("Play", "play.png", fDashboard);
        bPause = new MyPlainButton("Pause", "pause.png", fDashboard);
        bStep = new MyPlainButton("Step", "skip.png", fDashboard);
        bStop = new MyPlainButton("Stop", "stop.png", fDashboard);
        bUp = new MyPlainButton(glossary.capability.UP.name(), "moveUp.png", fDashboard);
        bCapture = new MyPlainButton(glossary.capability.CAPTURE.name(), "capture.png", fDashboard);
        bDown = new MyPlainButton(glossary.capability.DOWN.name(), "moveDown.png", fDashboard);
        bForward = new MyPlainButton(glossary.capability.MOVE.name(), "moveForward.png", fDashboard);
        bRight = new MyPlainButton(glossary.capability.RIGHT.name(), "rotateRight.png", fDashboard);
        bLeft = new MyPlainButton(glossary.capability.LEFT.name(), "rotateLeft.png", fDashboard);
        bRecharge = new MyPlainButton(glossary.capability.RECHARGE.name(), "recharge.png", fDashboard);
        bGame = new MyPlainButton("Game", "game.png", fDashboard);
        bAgent = new MyPlainButton("Agent", "Agent.png", fDashboard);

        pButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pButton.setBackground(cBackgr);

        tpLog = new JTextPane();
        tpLog.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        tpLog.setBackground(this.cStatus);
        tpLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        spLog = new JScrollPane(tpLog);
        spLog.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        spLog.setBackground(cBackgr);
        spLog.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        spLog.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        rpbEnergy = new RoundProgressBar(0, iMaxEnergy);
        rpbAltimeter = new RoundProgressBar(0, 256);
        rpbDistance = new RoundProgressBar(0, 256);
        rpbCompass = new Angular();
        rpbAngular = new Angular();

        fDashboard.setVisible(true);
        fDashboard.setResizable(false);
        fDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Palettes
        Palette pal = new Palette();
        pal.addWayPoint(0, new Color(0, 0, 0)); //Terrain
        pal.addWayPoint(10, new Color(0, 160, 0));
        pal.addWayPoint(40, new Color(51, 60, 0));
        pal.addWayPoint(80, new Color(153, 79, 0));
        pal.addWayPoint(100, Color.WHITE);
        pal.fillWayPoints(256);
        Palettes.put("Terrain", pal);

        pal = new Palette();
        pal.addWayPoint(0, new Color(0, 0, 200)); //Terrain
        pal.addWayPoint(10, new Color(0, 0, 160));
        pal.addWayPoint(40, new Color(51, 60, 0));
        pal.addWayPoint(80, new Color(153, 79, 0));
        pal.addWayPoint(100, Color.WHITE);
        pal.fillWayPoints(256);
        Palettes.put("Water", pal);

        pal = new Palette();
        pal.addWayPoint(0, new Color(255, 0, 0)); //Mostaphar
        pal.addWayPoint(40, new Color(35, 20, 0));
        pal.addWayPoint(90, new Color(153, 79, 0));
        pal.addWayPoint(100, Color.WHITE);
        pal.fillWayPoints(256);
        Palettes.put("Mostapahar", pal);

        pal = new Palette();
        pal.addWayPoint(0, new Color(0, 0, 0));    // Hoth
        pal.addWayPoint(10, new Color(0, 20, 51));
        pal.addWayPoint(90, new Color(179, 209, 255));
        pal.addWayPoint(100, Color.WHITE);
        pal.fillWayPoints(256);
        Palettes.put("Hoth", pal);

        pal = new Palette();
        pal.addWayPoint(0, new Color(0, 0, 0));    // Tatooine
        pal.addWayPoint(30, new Color(102, 61, 0));
        pal.addWayPoint(100, new Color(255, 235, 204));
        pal.fillWayPoints(256);
        Palettes.put("Tatooine", pal);

        pal = new Palette();
        pal.addWayPoint(0, new Color(0, 0, 255));    // Dagobah
        pal.addWayPoint(40, new Color(0, 10, 50));
        pal.addWayPoint(80, new Color(0, 120, 0));
        pal.addWayPoint(100, new Color(0, 255, 0));
        pal.fillWayPoints(256);
        Palettes.put("Dagobah", pal);

        pal = new Palette();
        pal.addWayPoint(0, Color.BLACK); // BW
        pal.addWayPoint(100, Color.WHITE);
        pal.fillWayPoints(256);
        Palettes.put("Visual", pal);

        pal = new Palette();
        pal.addWayPoint(0, Color.WHITE); // WB
        pal.addWayPoint(100, Color.BLACK);
        pal.fillWayPoints(256);
        Palettes.put("WB", pal);

//        pal = new Palette().intoThermal(256);
        pal = new Palette();
        pal.addWayPoint(0, Color.WHITE); // WB
        pal.addWayPoint(5, Color.RED);
        pal.addWayPoint(10, Color.YELLOW);
        pal.addWayPoint(20, Color.GREEN);
        pal.addWayPoint(40, Color.CYAN);
        pal.addWayPoint(60, Color.BLUE);
        pal.addWayPoint(80, Color.MAGENTA);
        pal.addWayPoint(100, Color.BLACK);
        pal.fillWayPoints(256);
        Palettes.put("Thermal", pal);

        pal = new Palette();
        pal.addWayPoint(0, new Color(0, 180, 0)); // WB
        pal.addWayPoint(100, new Color(0, 10, 0));
        pal.fillWayPoints(256);
        Palettes.put("Lidar", pal);

        preLayout();
        this.fDashboard.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                disableDashBoard();
            }
        });

    }

    protected void preLayout() {

        switch (myLayout) {
            case DASHBOARD:
                family = "blue";
                // Define sizes
                iLeft = 450;
                iLog = iLeft / 3;
                iRight = 600;
                iButton = 64;
                iStatus = iLeft;
                iFlightw = iRight - factor;
                iFlighth = 125;

                pMain.setPreferredSize(new Dimension(iLeft + iRight + iLog, iLeft + iButton));
                pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));

                pLeft.setPreferredSize(new Dimension(iLeft, iLeft));
                pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));

                pRight.setPreferredSize(new Dimension(iRight, iLeft));
                pRight.setLayout(new BoxLayout(pRight, BoxLayout.Y_AXIS));

                pMiddle.setPreferredSize(pMain.getPreferredSize());//new Dimension(iLeft + iRight + iLog, iLeft));
                pMiddle.setLayout(new FlowLayout(FlowLayout.LEFT, iset, iset));

                pButton.setPreferredSize(new Dimension(pMain.getPreferredSize().width, iButton)); //iLeft + iRight + iLog, iButton));
                pButton.setLayout(new FlowLayout(FlowLayout.CENTER));

                // Mount panels
                pButton.add(new JLabel(SwingTools.toIcon("./images/gray/gray-TieFighter.png", iButton, iButton)));

                pButton.add(bAgent);
                pButton.add(bGame);

                cbShowSplash = new JCheckBox();
                cbShowSplash.setText("Don't show this again");
                pButton.add(this.cbShowSplash);

                pMiddle.add(pLeft);
                pMiddle.add(pRight);

                pMain.add(pMiddle);
                pMain.add(pButton);
                fDashboard.add(pMain);
                fDashboard.pack();
                fDashboard.show();

                break;

            case COMPACT:
            default:
                // Define sizes
//                family = "phos";
//                iLeft = 300;
//                iRight = 300;
//                iButton = 64;
//                iStatus = 200;
//                iLog = iLeft + iButton;
//                iFlightw = iLeft + iRight - factor;
//                iFlighth = 101;
//
//                pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
//
//                pLeft.setPreferredSize(new Dimension(iLeft, iLeft + iButton));
//                pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));
//
//                pMiddle.setPreferredSize(new Dimension(iLeft + iRight, iLeft + iButton));
//                pMiddle.setLayout(new FlowLayout(FlowLayout.LEFT, iset, iset));
//
//                dpMap.setPreferredSize(new Dimension(iLeft, iLeft));
//
//                pButton.setPreferredSize(new Dimension(iLeft, iButton));
//                pButton.setLayout(new FlowLayout(FlowLayout.LEFT));
//
//                dpMyStatus.setPreferredSize(new Dimension(iLeft + iRight, iStatus));
//                dpMyStatus.setLayout(null);
//
//                tpLog.setPreferredSize(new Dimension(iRight, iLog));
//
//                spLog = new JScrollPane(tpLog);
//                spLog.setPreferredSize(new Dimension(iRight, iLog));
//
//                // Mount panels
//                pButton.add(bPlay);
//                pButton.add(bGame);
//
//                pLeft.add(dpMap);
//                pLeft.add(pButton);
//
//                pMiddle.add(pLeft);
//                pMiddle.add(spLog);
//
//                pMain.add(pMiddle);
//                pMain.add(dpMyStatus);
//                fDashboard.add(pMain);
//                fDashboard.pack();
//                fDashboard.show();
                break;
        }
        if (showsplash) {
//            this.pLeft.getGraphics().drawImage(SwingTools.toIcon("./images/black/" + splash1 + ".png", iStatus, iStatus).getImage(), iRight / 2 - iStatus / 2, 0, null);
            this.pLeft.getGraphics().drawImage(SwingTools.toIcon("./images/neg/neg-logougr.png", iLeft, iLeft).getImage(), 0, 0, null);
            this.fDashboard.setTitle("XUI: eXternal User Interface v 1.0");
            this.pRight.getGraphics().drawImage(SwingTools.toIcon("./images/black/" + splash2 + ".png", iLeft, iLeft).getImage(), 0, 0, null);
        } else {
            goReal();
            fullLayout();
        }
    }

    protected void fullLayout() {

        if (fulllayout) {
            return;
        }
        switch (myLayout) {
            case DASHBOARD:
                if (cbShowSplash.isSelected()) {
                    File f = new File(splashlock);
                    try {
                        f.createNewFile();
                        this.showsplash = false;
                    } catch (IOException ex) {
                    }
                }
                pLeft.removeAll();
                pRight.removeAll();
                dpMyStatus.setPreferredSize(new Dimension(iRight, iStatus));
                dpMyStatus.setLayout(null);
                mpVisual.setBounds(10, 150, 185, 150);
                dpMyStatus.add(mpVisual);
                mpLidar.setBounds(210, 150, 185, 150);
                dpMyStatus.add(mpLidar);
                mpThermal.setBounds(410, 150, 185, 150);
                dpMyStatus.add(mpThermal);

                // Mount panels
                spLog.setPreferredSize(new Dimension(iLog, iLeft));

                dpMap.add(mpMap);
                pLeft.add(dpMap);

                mpMap.setBounds(0, 0, iLeft - 4, iLeft - 4);///////
                pRight.add(dpMyStatus);

                pMiddle.add(pLeft);
                pMiddle.add(pRight);
                pMiddle.add(spLog);

                pMain.add(pMiddle);
                pMain.add(pButton);
                fDashboard.add(pMain);
                fDashboard.pack();
                fDashboard.show();
                break;

            case COMPACT:
            default:
                // Define sizes
//                family = "phos";
//                iLeft = 300;
//                iRight = 300;
//                iButton = 64;
//                iStatus = 200;
//                iLog = iLeft + iButton;
//                iFlightw = iLeft + iRight - factor;
//                iFlighth = 101;
//
//                pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
//
//                pLeft.setPreferredSize(new Dimension(iLeft, iLeft + iButton));
//                pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));
//
//                pMiddle.setPreferredSize(new Dimension(iLeft + iRight, iLeft + iButton));
//                pMiddle.setLayout(new FlowLayout(FlowLayout.LEFT, iset, iset));
//
//                dpMap.setPreferredSize(new Dimension(iLeft, iLeft));
//
//                pButton.setPreferredSize(new Dimension(iLeft, iButton));
//                pButton.setLayout(new FlowLayout(FlowLayout.LEFT));
//
//                dpMyStatus.setPreferredSize(new Dimension(iLeft + iRight, iStatus));
//                dpMyStatus.setLayout(null);
//
//                tpLog.setPreferredSize(new Dimension(iRight, iLog));
//
//                spLog = new JScrollPane(tpLog);
//                spLog.setPreferredSize(new Dimension(iRight, iLog));
//
//                // Mount panels
//                pButton.add(bPlay);
//                pButton.add(bGame);
//
//                pLeft.add(dpMap);
//                pLeft.add(pButton);
//
//                pMiddle.add(pLeft);
//                pMiddle.add(spLog);
//
//                pMain.add(pMiddle);
//                pMain.add(dpMyStatus);
//                fDashboard.add(pMain);
//                fDashboard.pack();
//                fDashboard.show();
                break;
        }
        refresh();
        fulllayout = true;
    }

    protected void initLayout() {

        switch (myLayout) {
            case DASHBOARD:
                family = "blue";
                // Define sizes
                iLeft = 450;
                iLog = iLeft / 3;
                iRight = 600;
                iButton = 64;
                iStatus = iLeft;
                iFlightw = iRight - factor;
                iFlighth = 100;

                pMain.setPreferredSize(new Dimension(iLeft + iRight + iLog, iLeft + iButton));
                pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));

                pLeft.setPreferredSize(new Dimension(iLeft, iLeft));
                pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));

                pRight.setPreferredSize(new Dimension(iRight, iLeft));
                pRight.setLayout(new BoxLayout(pRight, BoxLayout.Y_AXIS));

                pMiddle.setPreferredSize(pMain.getPreferredSize());//new Dimension(iLeft + iRight + iLog, iLeft));
                pMiddle.setLayout(new FlowLayout(FlowLayout.LEFT, iset, iset));

                pButton.setPreferredSize(new Dimension(pMain.getPreferredSize().width, iButton)); //iLeft + iRight + iLog, iButton));
                pButton.setLayout(new FlowLayout(FlowLayout.CENTER));

                dpMyStatus.setPreferredSize(new Dimension(iRight, iStatus));
                dpMyStatus.setLayout(null);
                mpVisual.setBounds(10, 150, 185, 150);
                dpMyStatus.add(mpVisual);
                mpLidar.setBounds(210, 150, 185, 150);
                dpMyStatus.add(mpLidar);
                mpThermal.setBounds(410, 150, 185, 150);
                dpMyStatus.add(mpThermal);

                // Mount panels
                spLog.setPreferredSize(new Dimension(iLog, iLeft));

                pButton.add(new JLabel(SwingTools.toIcon("./images/gray/gray-TieFighter.png", iButton, iButton)));

                pButton.add(bAgent);
                pButton.add(bGame);

                dpMap.add(mpMap);
                pLeft.add(dpMap);

                mpMap.setBounds(0, 0, iLeft - 2, iLeft - 2);
                pRight.add(dpMyStatus);

                pMiddle.add(pLeft);
                pMiddle.add(pRight);
                pMiddle.add(spLog);

                pMain.add(pMiddle);
                pMain.add(pButton);
                fDashboard.add(pMain);
                fDashboard.pack();
                fDashboard.show();
                break;

            case COMPACT:
            default:
                // Define sizes
                family = "phos";
                iLeft = 300;
                iRight = 300;
                iButton = 64;
                iStatus = 200;
                iLog = iLeft + iButton;
                iFlightw = iLeft + iRight - factor;
                iFlighth = 101;

                pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));

                pLeft.setPreferredSize(new Dimension(iLeft, iLeft + iButton));
                pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));

                pMiddle.setPreferredSize(new Dimension(iLeft + iRight, iLeft + iButton));
                pMiddle.setLayout(new FlowLayout(FlowLayout.LEFT, iset, iset));

                dpMap.setPreferredSize(new Dimension(iLeft, iLeft));

                pButton.setPreferredSize(new Dimension(iLeft, iButton));
                pButton.setLayout(new FlowLayout(FlowLayout.LEFT));

                dpMyStatus.setPreferredSize(new Dimension(iLeft + iRight, iStatus));
                dpMyStatus.setLayout(null);

                tpLog.setPreferredSize(new Dimension(iRight, iLog));

                spLog = new JScrollPane(tpLog);
                spLog.setPreferredSize(new Dimension(iRight, iLog));

                // Mount panels
                pButton.add(bPlay);
                pButton.add(bGame);

                pLeft.add(dpMap);
                pLeft.add(pButton);

                pMiddle.add(pLeft);
                pMiddle.add(spLog);

                pMain.add(pMiddle);
                pMain.add(dpMyStatus);
                fDashboard.add(pMain);
                fDashboard.pack();
                fDashboard.show();
                break;
        }

    }

    protected void DashListener(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Agent":
                goReal();
                break;
            case "Play":
                smContinue.release(1000);
                smPlay.release(1000);
                smAllowAgent.release(1000);
                smStart.release(1000);
                bPlay.off();
                bPause.on();
                bStop.on();
//                bStep.on();
                bStep.off();

                break;
            case "Game":
                goSimulator();
                break;
            case "Step":
                smContinue.release(1);
                bStop.on();
                break;
            case "Stop":
                smContinue.drainPermits();
                bPlay.off();
                bStop.off();
                bStep.off();
                bPause.off();
                this.disableDashBoard();
                break;
            case "Pause":
                smContinue.drainPermits();
                bPlay.on();
                bStop.on();
                bStep.on();
                bPause.off();
                break;
            default:
                externalExecutor.accept(e.getActionCommand());
                break;
        }
    }

    protected void showTerrain(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }

        g.drawImage(hFlight.getMap(), null, x, y);
    }

    protected void showMyStatus(Graphics2D g) {
        if (!lastPerception.isReady()) {
            return;
        }

        g.setBackground(Color.BLACK);
        g.setColor(Color.BLACK);
        switch (myLayout) {
            case DASHBOARD:
                DashBoardLayout(g);
                break;
            case COMPACT:
            default:
                CompactLayout(g);
        }
    }

    protected void CompactLayout(Graphics2D g) {
        if (lastPerception.isReady()) {
            showName(g, 0, 0);

            showAlive(g, 0, 1);
            showEnergy(g, 1, 1);
            showCompass(g, 2, 1);
            showAltimeter(g, 3, 1);
            showGPS(g, 4, 1);

            showOnTarget(g, 0, 2);
            showPayLoad(g, 1, 2);
            showAngular(g, 2, 2);
            showDistance(g, 3, 2);

            showNSteps(g, 4, 2);
            showTimer(g, 5, 2);

            showTerrain(g, 0, 3);
        }
    }

    protected void DashBoardLayout(Graphics2D g) {
        if (lastPerception.isReady()) {
            try {
                g.setColor(cDodgerB);
                showName(g, 0, 0);

                showAlive(g, 0, 1);
                showOnTarget(g, 0, 2);
                showPayLoad(g, 0, 3);
                showAltimeter(g, 0, 4);
                showGPS(g, 0, 5);
                showNSteps(g, 2, 5);
                showTimer(g, 4, 5);

                this.showEnergyPB(g, 1, 1, 4);
                this.showCompassPB(g, 2, 1, 4);
                this.showAltimeterPB(g, 3, 1, 4);
                this.showAngularPB(g, 4, 1, 4);
                this.showDistancePB(g, 5, 1, 4);

                showMiniVisual(g, 1, 6);
                showMiniLidar(g, 2, 6);
                showMiniThermal(g, 3, 6);
                this.showTerrain(g, 0, 13);
            } catch (Exception ex) {

            }
        }
    }

    protected void addAction(String action) {
        tpLog.setBackground(cStatus);
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset1 = sc.addAttribute(SimpleAttributeSet.EMPTY,
                StyleConstants.Foreground, Color.WHITE), aset2 = sc.addAttribute(SimpleAttributeSet.EMPTY,
                        StyleConstants.FontFamily, Font.SERIF);
        AttributeSet aset = sc.addAttributes(aset1, aset2);
        StyledDocument doc = tpLog.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), (this.getNsteps()) + ". " + action + "\n", aset);
        } catch (BadLocationException ex) {
        }
        tpLog.setCaretPosition(doc.getLength());
//        refresh();
    }

    protected void addStatus(String status) {
        if (status.length() < 1) {
            return;
        }
        tpLog.setBackground(cStatus);
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset1 = sc.addAttribute(SimpleAttributeSet.EMPTY,
                StyleConstants.Foreground, cTextStatus), aset2 = sc.addAttribute(SimpleAttributeSet.EMPTY,
                        StyleConstants.FontFamily, Font.SERIF);
        AttributeSet aset = sc.addAttributes(aset1, aset2);
        StyledDocument doc = tpLog.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), "\n" + status + "\n", aset);
        } catch (BadLocationException ex) {
        }
        tpLog.setCaretPosition(doc.getLength());

    }

    public boolean hasSensor(String sensor) {
        return this.lastPerception.hasSensor(sensor);
    }

    protected void refresh() {
        String trace = "";
        int n = 0, k = 2;

        SwingTools.doSwingWait(() -> {
            fDashboard.validate();
            this.fDashboard.repaint();
            smReadyFX.release(1);
        });
    }

    protected void showName(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        g.drawString(name, x, y + stringskip);
    }

    protected void showAlive(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.ALIVE.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        if (lastPerception.getAlive()) {
            g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-alive.png", factor, factor).getImage(), x, y, null);
            g.setColor(Color.WHITE);
            g.drawString("ALIVE", x + factor, y + stringskip);
        } else {
            g.drawImage(SwingTools.toIcon("./images/red/red-dead.png", factor, factor).getImage(), x, y, null);
            g.setColor(Color.WHITE);
            g.drawString("DEAD", x + factor, y + stringskip);
        }

    }

    protected void showOnTarget(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.ONTARGET.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        if (lastPerception.getOnTarget()) {
            g.drawImage(SwingTools.toIcon("./images/phos/phos-target.png", factor, factor).getImage(), x, y, null);
            g.setColor(Color.WHITE);
            g.drawString("TARGET", x + factor, y + stringskip);
        } else {
            g.drawImage(SwingTools.toIcon("./images/gray/gray-target.png", factor, factor).getImage(), x, y, null);
            g.setColor(Color.GRAY);
            g.drawString("UNLCK", x + factor, y + stringskip);
        }

    }

    protected void showEnergy(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.ENERGY.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        double energy = lastPerception.getEnergy();
        if (energy > 750) {
            g.drawImage(SwingTools.toIcon("./images/green/green-energy.png", factor, factor).getImage(), x, y, null);
        } else if (energy > 500) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-energy.png", factor, factor).getImage(), x, y, null);
        } else if (energy > 250) {
            g.drawImage(SwingTools.toIcon("./images/orange/orange-energy.png", factor, factor).getImage(), x, y, null);
        } else {
            g.drawImage(SwingTools.toIcon("./images/red/red-energy.png", factor, factor).getImage(), x, y, null);
        }
        g.setColor(Color.WHITE);

        g.drawString(String.format(" %04d W", (int) lastPerception.getEnergy()), x + factor, y + stringskip);

    }

    protected void showEnergyPB(Graphics2D g, int px, int py, int w) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.ENERGY.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        double energy = lastPerception.getEnergy();

        rpbEnergy.setPosition(x, y);
        rpbEnergy.setPreferredSize(w * factor);
        this.rpbEnergy.setBackground(Color.DARK_GRAY);
        this.rpbEnergy.setColor(cDodgerB);
        this.rpbEnergy.setThick(20);
        rpbAltimeter.setUnits("W");
        rpbEnergy.setValue((int) energy);
        rpbEnergy.showProgressBar(g);
        g.setColor(Color.WHITE);

        if (energy > 750) {
            g.drawImage(SwingTools.toIcon("./images/green/green-energy.png", factor, factor).getImage(),
                    x + w * factor / 2 - factor / 2, y + w * factor - factor, null);
        } else if (energy > 500) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-energy.png", factor, factor).getImage(),
                    x + w * factor / 2 - factor / 2, y + w * factor - factor, null);
        } else if (energy > 250) {
            g.drawImage(SwingTools.toIcon("./images/orange/orange-energy.png", factor, factor).getImage(),
                    x + w * factor / 2 - factor / 2, y + w * factor - factor, null);
        } else {
            g.drawImage(SwingTools.toIcon("./images/red/red-energy.png", factor, factor).getImage(),
                    x + w * factor / 2 - factor / 2, y + w * factor - factor, null);
        }

    }

    protected void showAngularPB(Graphics2D g, int px, int py, int w) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.ANGULAR.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        rpbAngular.setPosition(x, y);
        rpbAngular.setPreferredSize(w * factor);
        rpbAngular.setThick(15);
        rpbAngular.setValue((int) lastPerception.getAngular());
        this.rpbAngular.setColor(cTrace);
        this.rpbAngular.setBackground(Color.DARK_GRAY);
        rpbAngular.showAngle(g);
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-Angular_" + (int) (Math.round(lastPerception.getAngular() / 45) * 45) + ".png",
                2 * factor, 2 * factor).getImage(),
                x + w * factor / 2 - factor, y + w * factor / 2 - factor, null);
        g.setColor(Color.WHITE);

    }

    protected void showCompassPB(Graphics2D g, int px, int py, int w) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.COMPASS.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        rpbCompass.setPosition(x, y);
        rpbCompass.setPreferredSize(w * factor);
        rpbCompass.setThick(15);
        rpbCompass.setValue((int) lastPerception.getCompass());
        this.rpbCompass.setColor(cDodgerB);
        this.rpbCompass.setBackground(Color.DARK_GRAY);
        rpbCompass.showAngle(g);
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-Tie_" + lastPerception.getCompass() + ".png",
                2 * factor, 2 * factor).getImage(),
                x + w * factor / 2 - factor, y + w * factor / 2 - factor, null);
        g.setColor(Color.WHITE);

    }

    protected void showCompass(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.COMPASS.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        int compass = lastPerception.getCompass();
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-Tie_" + lastPerception.getCompass() + ".png", factor, factor).getImage(), x, y, null);
        g.setColor(Color.WHITE);
        g.drawString(String.format(" %03d º", lastPerception.getCompass()), x + factor, y + stringskip);
    }

    protected void showAltimeter(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.ALTITUDE.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-altitude.png", factor, factor).getImage(), x, y, null);
        g.setColor(Color.WHITE);
        g.drawString(String.format(" %03d m", lastPerception.getAltitude()), x + factor, y + stringskip);
    }

    protected void showAltimeterPB(Graphics2D g, int px, int py, int w) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.ALTITUDE.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        int realv = lastPerception.getAltitude(),
                maxv = MAXFLIGHT;
        double ratio = 1.0 * realv / maxv;
        rpbAltimeter.setPosition(x, y);
        rpbAltimeter.setPreferredSize(w * factor);
        this.rpbAltimeter.setBackground(Color.DARK_GRAY);
        this.rpbAltimeter.setThick(20);
        rpbAltimeter.setUnits("m");
        rpbAltimeter.setMaxValue(maxv);
        rpbAltimeter.setValue(realv);
        if (ratio <= 0.5) {
            this.rpbAltimeter.setColor(Color.GREEN);
        } else if (ratio <= 0.75) {
            this.rpbAltimeter.setColor(Color.YELLOW);
        } else if (ratio <= 0.90) {
            this.rpbAltimeter.setColor(Color.ORANGE);
        } else {
            this.rpbAltimeter.setColor(Color.RED);
        }
        rpbAltimeter.showProgressBar(g);
        g.setColor(Color.WHITE);

        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-altitude.png", factor, factor).getImage(),
                x + w * factor / 2 - factor / 2, y + w * factor - factor, null);
    }

    protected void showNSteps(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-nsteps.png", factor, factor).getImage(), x, y, null);
        g.setColor(Color.WHITE);
        g.drawString(String.format(" %03d ", getNsteps()), x + factor, y + stringskip);
    }

    protected void showAngular(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.ANGULAR.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-Angular_" + (int) (Math.round(lastPerception.getAngular() / 45) * 45) + ".png", factor, factor).getImage(), x, y, null);
        g.setColor(Color.WHITE);
        g.drawString(String.format(" %4.0f º", lastPerception.getAngular()), x + factor, y + stringskip);
    }

    protected void showDistance(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.DISTANCE.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-distance.png", factor, factor).getImage(), x, y, null);
        g.setColor(Color.WHITE);
        g.drawString(String.format(" %5.1f m", lastPerception.getDistance()), x + factor, y + stringskip);
    }

    protected void showDistancePB(Graphics2D g, int px, int py, int w) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.DISTANCE.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        int realv = (int) lastPerception.getDistance();
        rpbDistance.setPosition(x, y);
        rpbDistance.setPreferredSize(w * factor);
        this.rpbDistance.setBackground(Color.DARK_GRAY);
        this.rpbDistance.setThick(20);
        rpbDistance.setUnits("m");
        rpbDistance.setValue(realv);
        this.rpbDistance.setColor(this.cDodgerB);
        rpbDistance.showProgressBar(g);
        g.setColor(Color.WHITE);

        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-distance.png", factor, factor).getImage(),
                x + w * factor / 2 - factor / 2, y + w * factor - factor, null);
    }

    protected void showPayLoad(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.PAYLOAD.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-payload.png", factor, factor).getImage(), x, y, null);
        g.setColor(Color.WHITE);
        g.drawString(String.format(" %02d", lastPerception.getPayload()), x + factor, y + stringskip);
    }

    protected void showGPS(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.GPS.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
            return;
        }
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-gps.png", factor, factor).getImage(), x, y, null);
        double gps[] = lastPerception.getGPS();
        g.drawString(String.format(" %03d-%03d-%03d", (int) gps[0], (int) gps[1], (int) gps[2]), x + factor, y + stringskip);

    }

    protected void showTimer(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-timer.png", factor, factor).getImage(), x, y, null);
        g.setColor(Color.WHITE);
        g.drawString(String.format(" %03d s", getTimerSecs()), x + factor, y + stringskip);
    }

    protected void showMiniVisual(Graphics2D g, int px, int py) {
        if (!lastPerception.isReady() || cVisual == null) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.VISUAL.name()) && !lastPerception.hasSensor(sensors.VISUALHQ.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), px, py, null);
            return;
        }
        for (int y = 0; y < cVisual.length; y++) {
            for (int x = 0; x < cVisual[0].length; x++) {
                mpVisual.setColor(x, y, cVisual[x][y]);
            }
        }

        mpVisual.validate();
        mpVisual.repaint();
    }

    protected void showMiniLidar(Graphics2D g, int px, int py) {
        if (!lastPerception.isReady() || cLidar == null) {
            return;
        }

        if (!lastPerception.hasSensor(sensors.LIDAR.name()) && !lastPerception.hasSensor(sensors.LIDARHQ.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), px, py, null);
            return;
        }
        for (int y = 0; y < cLidar.length; y++) {
            for (int x = 0; x < cLidar[0].length; x++) {
                mpLidar.setColor(x, y, cLidar[x][y]);
            }
        }

        mpLidar.validate();
        mpLidar.repaint();
    }

    protected void showMiniThermal(Graphics2D g, int px, int py) {
        if (!lastPerception.isReady() || cThermal == null) {
            return;
        }
        if (!lastPerception.hasSensor(sensors.THERMAL.name()) && !lastPerception.hasSensor(sensors.THERMALHQ.name())) {
            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), px, py, null);
            return;
        }
        for (int y = 0; y < cThermal.length; y++) {
            for (int x = 0; x < cThermal[0].length; x++) {
                mpThermal.setColor(x, y, cThermal[x][y]);
            }
        }

        mpThermal.validate();
        mpThermal.repaint();
    }

    protected double[] fromJsonArray(JsonArray jsa) {
        double res[] = new double[jsa.size()];
        for (int i = 0; i < jsa.size(); i++) {
            res[i] = jsa.get(i).asDouble();
        }
        return res;
    }

    public boolean isTooHigh(int level) {
        return level >= getMaxlevel();
    }

    public boolean isNullread(int level) {
        return level == NULLREAD;
    }

    public int getNsteps() {
        if (lastPerception.isReady()) {
            return lastPerception.getNSteps();
        }
        return 0;

    }

    public int getTimerSecs() {
        if (lastPerception.isReady()) {
            return (int) tinit.elapsedTimeSecs(new TimeHandler());
        }
        return 0;
    }

    public int getMaxlevel() {
        if (lastPerception.isReady()) {
            return lastPerception.getMaxlevel();
        }
        return -1;
    }

    public String getStatus() {
        if (lastPerception.isReady()) {
            return lastPerception.getStatus();
        }
        return "";
    }

    public boolean getAlive() {
        if (lastPerception.isReady()) {
            return lastPerception.getAlive();
        }
        return false;
    }

    public boolean getOnTarget() {
        if (lastPerception.isReady()) {
            return lastPerception.getOnTarget();
        }
        return false;
    }

    public double[] getGPS() {
        if (lastPerception.isReady()) {
            return lastPerception.getGPS();
        }
        return new double[0];
    }

    public int getPayload() {
        if (lastPerception.isReady()) {
            return lastPerception.getPayload();
        }
        return -1;
    }

    public int getEnergyBurnt() {
        if (lastPerception.isReady()) {
            return (int) lastPerception.getEnergyBurnt();
        }
        return -1;
    }

    public int getCompass() {
        if (lastPerception.isReady()) {
            return lastPerception.getCompass();
        }
        return -1;
    }

    public int getAltitude() {
        if (lastPerception.isReady()) {
            return lastPerception.getAltitude();
        }
        return -1;
    }

    public double getDistance() {
        if (lastPerception.isReady()) {
            return lastPerception.getDistance();
        }
        return -1;

    }

    public double getAngular() {
        if (lastPerception.isReady()) {
            return lastPerception.getAngular();
        }
        return -1;
    }

    public double getEnergy() {
        if (lastPerception.isReady()) {
            return lastPerception.getEnergy();
        }
        return -1;
    }

    public int[][] getVisual() {
        return lastPerception.getVisualData();
    }

    public int[][] getLidar() {
        return lastPerception.getLidarData();
    }

    public int[][] getThermal() {
        return lastPerception.getThermalData();
    }

    public String getName() {
        if (lastPerception.isReady()) {
            return lastPerception.getName();
        }
        return "";
    }

    protected Palette getPalette(String name) {
        Palette res = Palettes.get(name);
        if (res == null) {
            res = Palettes.get(Palettes.keySet().toArray()[0]);
        }
        return res;
    }

    protected String doReadPerceptions() {
        ACLMessage outbox = dashInbox.createReply();
        outbox.setContent("Query sensors session " + dashInbox.getConversationId());
        myAgent.send(outbox);
        dashInbox = myAgent.blockingReceive();
        this.feedPerceptionLocal(dashInbox.getContent());
        return dashInbox.getContent();
    }

    protected void doExecute(String action) {
        addAction(action);
        ACLMessage outbox = dashInbox.createReply();
        outbox.setContent("Request execute " + action + " session " + dashInbox.getConversationId());
        myAgent.send(outbox);
        dashInbox = myAgent.blockingReceive();
        Sentence mySentence = new Sentence().parseSentence(dashInbox.getContent());
        if (mySentence.isNext("INFORM")) {
            doReadPerceptions();
            //
            //this.printSensors();
        } else {
            doReadPerceptions();
            JOptionPane.showMessageDialog(null,
                    mySentence.getSentence(), "Autopilot", JOptionPane.ERROR_MESSAGE);
            disableDashBoard();
        }
    }

    public boolean isOpen() {
        return !exitdashboard;
    }

    protected void disableDashBoard() {
        smAllowAgent.release(1);
        smContinue.release(1);
        this.smStart.release(1);
        this.pButton.removeAll();
        exitdashboard = true;
        refresh();
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String printSensors() {
        String message = "";
        double reading;
        double row[];
        int imatrix[][];
        double dmatrix[][];
        for (String s : this.lastPerception.getSensorList()) {
            message += "\n" + s + ":\n";
            switch (s.toUpperCase()) {
                case "ONTARGET":
                    if (getOnTarget()) {
                        message += "   TARGET";
                    } else {
                        message += "EMPTY";
                    }
                    break;
                case "GPS":
                    row = getGPS();
                    message += "   X=" + (int) row[0] + " Y=" + (int) row[1] + " Z=" + (int) row[2];
                    break;
                case "ALTITUDE":
                    reading = getAltitude();
                    message += "   " + (int) reading;
                    break;
                case "ENERGY":
                    reading = getEnergy();
                    message += "   " + (int) reading;
                    break;
                case "ALIVE":
                    if (getAlive()) {
                        message += "   ALIVE";
                    } else {
                        message += "DEAD";
                    }
                    break;
                case "COMPASS":
                    reading = getCompass();
                    message += "   " + (int) reading;
                    break;
                case "ANGULAR":
                    reading = getAngular();
                    message += "   " + (int) reading;
                    break;
                case "DISTANCE":
                    reading = getDistance();
                    message += "   " + (int) reading;
                    break;
                case "VISUAL":
                case "VISUALHQ":
                    imatrix = getVisual();
                    for (int r = 0; r < imatrix.length; r++) {
                        for (int c = 0; c < imatrix[0].length; c++) {
                            message += String.format("   %03d", imatrix[r][c]);
                        }
                        message += "\n";
                    }
                    break;
                case "LIDAR":
                case "LIDARHQ":
                    imatrix = getLidar();
                    for (int r = 0; r < imatrix.length; r++) {
                        for (int c = 0; c < imatrix[0].length; c++) {
                            message += String.format("   %03d", imatrix[r][c]);
                        }
                        message += "\n";
                    }
                    break;
                case "THERMAL":
                case "THERMALHQ":
                    imatrix = getThermal();
                    for (int r = 0; r < imatrix.length; r++) {
                        for (int c = 0; c < imatrix[0].length; c++) {
                            message += String.format("   %03d", imatrix[r][c]);
                        }
                        message += "\n";
                    }
                    break;
            }
            message += "\n";
        }
        return message;
    }

}
