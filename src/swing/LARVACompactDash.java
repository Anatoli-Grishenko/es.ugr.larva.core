///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
//https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
// */
//package swing;
//
//import com.eclipsesource.json.JsonArray;
//import data.Ole;
//import data.OleFile;
//import glossary.sensors;
//import jade.core.Agent;
//import jade.lang.acl.ACLMessage;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.Font;
//import java.awt.Graphics2D;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.concurrent.Semaphore;
//import java.util.function.Consumer;
//import javax.swing.BoxLayout;
//import javax.swing.JCheckBox;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTextPane;
//import javax.swing.ScrollPaneConstants;
//import javax.swing.border.EmptyBorder;
//import javax.swing.text.AttributeSet;
//import javax.swing.text.BadLocationException;
//import javax.swing.text.SimpleAttributeSet;
//import javax.swing.text.StyleConstants;
//import javax.swing.text.StyleContext;
//import javax.swing.text.StyledDocument;
//import map2D.Map2DColor;
//import map2D.Palette;
//import messaging.Sentence;
//import tools.TimeHandler;
//import static world.Perceptor.NULLREAD;
//import world.SensorDecoder;
//import static world.liveBot.MAXFLIGHT;
//
///**
// *
// * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
// */
//public class LARVACompactDash extends LARVADash {
//
//    protected int iRightW, iRightH;
//
//    public LARVACompactDash(Agent a) {
//        super(a);
//        myLayout = Layout.DASHBOARD;
//        lastPerception = new SensorDecoder();
//        Palettes = new HashMap();
//        myAgent = a;
//        smReady = new Semaphore(0);
//        smContinue = new Semaphore(0);
//        smStart = new Semaphore(0);
//        smPlay = new Semaphore(1);
//        smReadyData = new Semaphore(0);
//        smReadyFX = new Semaphore(1);
//        smAllowAgent = new Semaphore(0);
//        smContinue = new Semaphore(0);
//        File f = new File(splashlock);
//        this.showsplash = false;
//
//        this.whenExecute(s -> this.doExecute(s));
//    }
//
//    @Override
//    public boolean preProcessACLM(ACLMessage msg) {
//        boolean res = false;
//        if (msg.getContent().contains("filedata")) {
//            Ole ocontent = new Ole().set(msg.getContent());
//            OleFile ofile;
//            ofile = new OleFile(ocontent.getOle("surface"));
//            int maxlevel = ocontent.getInt("maxflight");
//            if (isActivated()) {
//                bGame.setEnabled(enablesimulation);
//                try {
//                    smStart.acquire();
//                } catch (Exception ex) {
//                }
//
//                SwingTools.doSwingWait(() -> {
//                    fullLayout();
//                });
//            }
//            setWorldMap(ofile.toString(), maxlevel, ocontent.getField("palette"));
//            res = true;
//        }
//        if (msg.getContent().contains("perceptions")) {
//            if (isActivated()) {
//                if (!exitdashboard) {
//                    try {
//                        smStart.acquire();
//                    } catch (Exception ex) {
//                    }
//                }
//            }
//            dashInbox = msg;
//            this.feedPerception(msg.getContent());
//            res = false;
//        }
//        return res;
//    }
//
////    public boolean preProcessACLM(ACLMessage msg) {
////        boolean res = false;
////        if (msg.getContent().contains("filedata")) {
////            Ole ocontent = new Ole().set(msg.getContent());
////            OleFile ofile = new OleFile(ocontent.getOle("surface"));
////            int maxlevel = ocontent.getInt("maxflight");
////            enablesimulation = ocontent.getBoolean("simulator");
////            if (isActivated()) {
////                bGame.setEnabled(enablesimulation);
////                try {
////                    smStart.acquire();
////                } catch (Exception ex) {
////                }
////
////                SwingTools.doSwingWait(() -> {
////                    fullLayout();
////                });
////                setWorldMap(ofile.toString(), maxlevel, ocontent.getField("palette"));
////            }
////            res = true;
////        }
////        if (msg.getContent().contains("perceptions")) {
////            if (isActivated()) {
////                if (!exitdashboard) {
////                    try {
////                        smStart.acquire();
////                    } catch (Exception ex) {
////                    }
////                    dashInbox = msg;
////                }
////            }
////            this.feedPerception(msg.getContent());
////            res = false;
////        }
////        return res;
////    }
////
////    @Override
////    protected boolean setWorldMap(String olefile, int maxlevel, String spalette) {
////        this.lastPerception.setWorldMap(olefile, maxlevel);
////        iMaxLevel = maxlevel;
////        this.palMap = spalette;
////
////        worldw = lastPerception.getWorldMap().getWidth();
////        worldh = lastPerception.getWorldMap().getHeight();
//////        mMap = new Color[worldw][worldh];
//////        for (int i = 0; i < worldw; i++) {
//////            for (int j = 0; j < worldh; j++) {
//////                if (lastPerception.getWorldMap().getStepLevel(i, j) > maxlevel) {
//////                    mMap[i][j] = cBad;
//////                } else {
//////                    mMap[i][j] = p.getColor(lastPerception.getWorldMap().getStepLevel(i, j));
//////                }
//////            }
//////        }
////        OleFile ofile = new OleFile();
////        ofile.set(olefile);
////        tinit = new TimeHandler();
////        iMaxDistance = worldw + worldh;
////        if (isActivated()) {
////            this.fDashboard.setTitle("| MAP: " + ofile.getFileName() + "|");
////            rpbDistance.setMaxValue(iMaxDistance);
////        }
////        iFlightw = iRight - factor;
////        iFlighth = 125;
////        hFlight = new Map2DColor(iFlightw, iFlighth, Color.BLACK);
////        for (int i = 0; i < hFlight.getWidth(); i++) {
////            for (int j = 0; j < hFlight.getHeight(); j++) {
////                if (j % 10 == 0 || i % 10 == 0) {
////                    hFlight.setColor(i, j, Color.DARK_GRAY);
////                } else {
////                    hFlight.setColor(i, j, Color.GRAY);
////                }
////            }
////        }
////
////        refresh();
////        return true;
////    }
////
////    @Override
////    public void feedPerception(String perception) {
////        if (!perception.equals(sperception)) {
////            feedPerceptionLocal(perception);
////            sperception = perception;
////            if (isActivated()) {
////                try {
////                    smAllowAgent.acquire(1);
////                    smContinue.acquire(1);
////                } catch (Exception ex) {
////                }
////            }
////        }
////    }
////
////    @Override
////    protected void feedPerceptionLocal(String perception) {
////        try {
////            lastPerception.feedPerception(perception);
////            if (this.isActivated()) {
////                String[] trace = lastPerception.getTrace();
////                if (trace != null) { //&& iIter > trace.length && trace.length>0) {
////                    for (; iTrace < trace.length; iTrace++) {
////                        this.addAction(trace[iTrace]);
////                    }
////                }
////            }
////            name = lastPerception.getName();
////
////            if (lastPerception.hasSensor("GPS")) {
////                gps = lastPerception.getGPS();
////                lastx = (int) gps[0];
////                lasty = (int) gps[1];
////
////                int shft = 30;
////                if (hFlight.getWidth() - shft - 1 == iIter - baseFlight) {
////                    hFlight.shiftLeft(shft);
////                    baseFlight += shft;
////                }
////                for (int ih = 0; ih < this.hFlight.getHeight(); ih++) {
////                    int y1 = (ih * 256) / hFlight.getHeight(), y2 = (int) (gps[2] - lastPerception.getGround());
////                    if (y1 < y2) {
////                        if (this.myLayout == Layout.DASHBOARD) {
////                            hFlight.setColor(iIter - baseFlight, hFlight.getHeight() - ih, cDodgerB);
////                        } else {
////                            hFlight.setColor(iIter - baseFlight, hFlight.getHeight() - ih, cSoil);
////                        }
////                    } else {
////                        if (this.myLayout == Layout.DASHBOARD) {
////                            hFlight.setColor(iIter - baseFlight, hFlight.getHeight() - ih, Color.BLACK);
////                        } else {
////                            hFlight.setColor(iIter - baseFlight, hFlight.getHeight() - ih, cSky);
////                        }
////                    }
////                    if ((ih * 256) / hFlight.getHeight() > this.iMaxLevel) {
////                        hFlight.setColor(iIter, hFlight.getHeight() - ih, cBad);
////                    }
////                    hFlight.setColor(iIter - baseFlight, hFlight.getHeight() - (int) (hFlight.getHeight() * gps[2] / 256), cTrace);
////
////                }
////            }
////
////            if (getNsteps() == 1 && isActivated()) {
////                this.fDashboard.setTitle("| Session: " + this.lastPerception.getSession()
////                        + " |Agent: " + name + " " + fDashboard.getTitle());
////
////                int[][] sensor;
////                int rangex, rangey;
////                Palette palette;
////                if (lastPerception.hasSensor("VISUAL") || lastPerception.hasSensor("VISUALHQ")) {
////                    iVisual = lastPerception.getVisualData();
////                    sensor = iVisual;
////                    rangex = sensor[0].length;
////                    rangey = sensor.length;
////                    palette = getPalette("Visual");
////                    if (cVisual
////                            == null) {
////                        cVisual = new Color[rangex][rangey];
////                        for (int i = 0; i < rangex; i++) {
////                            for (int j = 0; j < rangey; j++) {
////                                cVisual[i][j] = palette.getColor(0);
////                            }
////                        }
////                        mpVisual.setMap(cVisual, palette);
////                    }
////                    for (int i = 0;
////                            i < rangex;
////                            i++) {
////                        for (int j = 0; j < rangey; j++) {
////                            if (sensor[j][i] > lastPerception.getMaxlevel()) {
////                                cVisual[i][j] = cBad;
////                            } else {
////                                cVisual[i][j] = palette.getColor(sensor[j][i]);
////                            }
////                        }
////                    }
////                }
////                if (lastPerception.hasSensor("LIDAR") || lastPerception.hasSensor("LIDARHQ")) {
////                    sensor = lastPerception.getLidarData();
////                    rangex = sensor[0].length;
////                    rangey = sensor.length;
////                    palette = getPalette("Lidar");
////                    if (cLidar
////                            == null) {
////                        cLidar = new Color[rangex][rangey];
////                        for (int i = 0; i < rangex; i++) {
////                            for (int j = 0; j < rangey; j++) {
////                                cLidar[i][j] = palette.getColor(0);
////                            }
////                        }
////                        mpLidar.setMap(cLidar, palette);
////                    }
////                    for (int i = 0;
////                            i < rangex;
////                            i++) {
////                        for (int j = 0; j < rangey; j++) {
////                            if (sensor[j][i] < 0) {
////                                cLidar[i][j] = cBad;
////                            } else {
////                                cLidar[i][j] = palette.getColor(sensor[j][i]);
////                            }
////                        }
////                    }
////                }
////                if (lastPerception.hasSensor("THERMAL") || lastPerception.hasSensor("THERMALHQ")) {
////                    sensor = lastPerception.getThermalData();
////                    iThermal = lastPerception.getThermalData();
////                    if (sensor.length
////                            > 0) {
////                        rangex = sensor[0].length;
////                        rangey = sensor.length;
////                        palette = getPalette("Thermal");
////                        if (cThermal == null) {
////                            cThermal = new Color[rangex][rangey];
////                            for (int i = 0; i < rangex; i++) {
////                                for (int j = 0; j < rangey; j++) {
////                                    cThermal[i][j] = palette.getColor(0);
////                                }
////                            }
////                            mpThermal.setMap(cThermal, palette);
////                        }
////                        for (int i = 0; i < rangex; i++) {
////                            for (int j = 0; j < rangey; j++) {
////                                cThermal[i][j] = palette.getColor(sensor[j][i]);
////                            }
////                        }
////                    }
////                }
////
////                addStatus(lastPerception.getStatus());
////                refresh();
////            }
////            iIter++;
////        } catch (Exception ex) {
////            System.err.println("Error processing perceptions " + ex.toString());
////
////        }
////    }
//    protected void whenExecute(Consumer<String> executor) {
//        externalExecutor = executor;
//    }
//
//    protected void goSimulator() {
//        simulation = true;
//        pButton.removeAll();
//        pButton.add(new JLabel(SwingTools.toIcon("./images/blue/blue-TieFighter.png", iButton, iButton)));
//        pButton.add(bUp);
//        pButton.add(bDown);
//        pButton.add(bLeft);
//        pButton.add(bRight);
//        pButton.add(bCapture);
//        pButton.add(bForward);
//        pButton.add(bRecharge);
//        pButton.add(bStop);
//        pButton.validate();
//        pButton.repaint();
//        bUp.on();
//        bDown.on();
//        bLeft.on();
//        bRight.on();
//        bForward.on();
//        this.bCapture.on();
//        this.bRecharge.on();
//        smAllowAgent.release(1); //drainPermits();
//        smStart.release(2);
//    }
//
//    protected void goReal() {
//        simulation = false;
//        pButton.removeAll();
//        pButton.add(new JLabel(SwingTools.toIcon("./images/red/red-TieFighter.png", iButton, iButton)));
//        pButton.add(bPlay);
//        pButton.add(bPlay);
//        pButton.add(bStop);
//        pButton.add(bPause);
//        pButton.add(bStep);
//        pButton.validate();
//        pButton.repaint();
//        bPlay.on();
//        bPause.off();
//        bStop.off();
//        bStep.on();
//        smAllowAgent.release(1);
//        smContinue.release(1);
//        smStart.release(2);
//    }
//
//    public void initGUI() {
//        // Define panels
//        fDashboard = new LARVAFrame(e -> this.DashListener(e));
//        pMain = new JPanel();
//        pMain.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//
//        pMiddle = new JPanel();
//        pMiddle.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        pMiddle.setBackground(cBackgr);
//
//        pLeft = new JPanel();
//        pLeft.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        pLeft.setBackground(cBackgr);
//
//        pLeft = new JPanel();
//        pLeft.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        pLeft.setBackground(cBackgr);
//
//        pRight = new JPanel();
//        pRight.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        pRight.setBackground(cBackgr);
//
//        dpMyStatus = new MyDrawPane(g -> showMyStatus(g));
//        dpMyStatus.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        dpMyStatus.setBackground(cBackgr);
//
//        dpMap = new MyDrawPane(null);
//        dpMap.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        dpMap.setBackground(cBackgr);
//        mpMap = new MyMapPalPane(null);
//        mpMap.addRuler();
//        mpMap.addTrail();
//
//        mpVisual = new MyMapPalPane(null);
//        mpVisual.addRuler();
//        mpVisual.addHotSpot();
//        mpLidar = new MyMapPalPane(null);
//        mpLidar.addRuler();
//        mpLidar.addHotSpot();
//        mpThermal = new MyMapPalPane(null);
//        mpThermal.addRuler();
//        mpThermal.addHotSpot();
//
//        bZoomIn = new MyPlainButton("ZoomIn", "zoom_in.png", this.fDashboard);
//        bZoomOut = new MyPlainButton("ZoomOut", "zoom_out.png", fDashboard);
//        bPlay = new MyPlainButton("Play", "play.png", fDashboard);
//        bPause = new MyPlainButton("Pause", "pause.png", fDashboard);
//        bStep = new MyPlainButton("Step", "skip.png", fDashboard);
//        bStop = new MyPlainButton("Stop", "stop.png", fDashboard);
//        bUp = new MyPlainButton(glossary.capability.UP.name(), "moveUp.png", fDashboard);
//        bCapture = new MyPlainButton(glossary.capability.CAPTURE.name(), "capture.png", fDashboard);
//        bDown = new MyPlainButton(glossary.capability.DOWN.name(), "moveDown.png", fDashboard);
//        bForward = new MyPlainButton(glossary.capability.MOVE.name(), "moveForward.png", fDashboard);
//        bRight = new MyPlainButton(glossary.capability.RIGHT.name(), "rotateRight.png", fDashboard);
//        bLeft = new MyPlainButton(glossary.capability.LEFT.name(), "rotateLeft.png", fDashboard);
//        bRecharge = new MyPlainButton(glossary.capability.RECHARGE.name(), "recharge.png", fDashboard);
//        bGame = new MyPlainButton("Game", "game.png", fDashboard);
//        bAgent = new MyPlainButton("Agent", "Agent.png", fDashboard);
//
//        pButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        pButton.setBackground(cBackgr);
//
//        tpLog = new JTextPane();
//        tpLog.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        tpLog.setBackground(this.cStatus);
//        tpLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
//
//        spLog = new JScrollPane(tpLog);
//        spLog.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        spLog.setBackground(cBackgr);
//        spLog.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
//        spLog.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//
//        rpbEnergy = new RoundProgressBar(0, iMaxEnergy);
//        rpbAltimeter = new RoundProgressBar(0, 256);
//        rpbDistance = new RoundProgressBar(0, 256);
//        rpbCompass = new Angular();
//        rpbAngular = new Angular();
//
//        fDashboard.setVisible(true);
//        fDashboard.setResizable(false);
//        fDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        // Palettes
//        Palette pal = new Palette();
//        pal.addWayPoint(0, new Color(0, 0, 0)); //Terrain
//        pal.addWayPoint(10, new Color(0, 160, 0));
//        pal.addWayPoint(40, new Color(51, 60, 0));
//        pal.addWayPoint(80, new Color(153, 79, 0));
//        pal.addWayPoint(100, Color.WHITE);
//        pal.fillWayPoints(256);
//        Palettes.put("Terrain", pal);
//
//        pal = new Palette();
//        pal.addWayPoint(0, new Color(0, 0, 200)); //Terrain
//        pal.addWayPoint(10, new Color(0, 0, 160));
//        pal.addWayPoint(40, new Color(51, 60, 0));
//        pal.addWayPoint(80, new Color(153, 79, 0));
//        pal.addWayPoint(100, Color.WHITE);
//        pal.fillWayPoints(256);
//        Palettes.put("Water", pal);
//
//        pal = new Palette();
//        pal.addWayPoint(0, new Color(255, 0, 0)); //Mostaphar
//        pal.addWayPoint(40, new Color(35, 20, 0));
//        pal.addWayPoint(90, new Color(153, 79, 0));
//        pal.addWayPoint(100, Color.WHITE);
//        pal.fillWayPoints(256);
//        Palettes.put("Mostapahar", pal);
//
//        pal = new Palette();
//        pal.addWayPoint(0, new Color(0, 0, 0));    // Hoth
//        pal.addWayPoint(10, new Color(0, 20, 51));
//        pal.addWayPoint(90, new Color(179, 209, 255));
//        pal.addWayPoint(100, Color.WHITE);
//        pal.fillWayPoints(256);
//        Palettes.put("Hoth", pal);
//
//        pal = new Palette();
//        pal.addWayPoint(0, new Color(0, 0, 0));    // Tatooine
//        pal.addWayPoint(30, new Color(102, 61, 0));
//        pal.addWayPoint(100, new Color(255, 235, 204));
//        pal.fillWayPoints(256);
//        Palettes.put("Tatooine", pal);
//
//        pal = new Palette();
//        pal.addWayPoint(0, new Color(0, 0, 255));    // Dagobah
//        pal.addWayPoint(40, new Color(0, 10, 50));
//        pal.addWayPoint(80, new Color(0, 120, 0));
//        pal.addWayPoint(100, new Color(0, 255, 0));
//        pal.fillWayPoints(256);
//        Palettes.put("Dagobah", pal);
//
//        pal = new Palette();
//        pal.addWayPoint(0, Color.BLACK); // BW
//        pal.addWayPoint(100, Color.WHITE);
//        pal.fillWayPoints(256);
//        Palettes.put("Visual", pal);
//
//        pal = new Palette();
//        pal.addWayPoint(0, Color.WHITE); // WB
//        pal.addWayPoint(100, Color.BLACK);
//        pal.fillWayPoints(256);
//        Palettes.put("WB", pal);
//
////        pal = new Palette().intoThermal(256);
//        pal = new Palette();
//        pal.addWayPoint(0, Color.WHITE); // WB
//        pal.addWayPoint(5, Color.RED);
//        pal.addWayPoint(10, Color.YELLOW);
//        pal.addWayPoint(20, Color.GREEN);
//        pal.addWayPoint(40, Color.CYAN);
//        pal.addWayPoint(60, Color.BLUE);
//        pal.addWayPoint(80, Color.MAGENTA);
//        pal.addWayPoint(100, Color.BLACK);
//        pal.fillWayPoints(256);
//        Palettes.put("Thermal", pal);
//
//        pal = new Palette();
//        pal.addWayPoint(0, new Color(0, 180, 0)); // WB
//        pal.addWayPoint(100, new Color(0, 10, 0));
//        pal.fillWayPoints(256);
//        Palettes.put("Lidar", pal);
//
//        preLayout();
//        this.fDashboard.addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//                disableDashBoard();
//            }
//        });
//
//    }
//
//    protected void preLayout() {
//
//        switch (myLayout) {
//            case DASHBOARD:
//                family = "blue";
//                // Define sizes
//                iLeft = 450;
//                iLog = iLeft / 3;
//                iRightW = 600;
//                this.iRightH = 450;
//                iButton = 64;
//                iStatus = iLeft;
//
//                pMain.setPreferredSize(new Dimension(iRightW + iLog, iRightH + iButton));
//                pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
//
//                pLeft.setPreferredSize(new Dimension(iLeft, iLeft));
//                pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));
//
//                pRight.setPreferredSize(new Dimension(iRightW, iRightH));
//                pRight.setLayout(new BoxLayout(pRight, BoxLayout.Y_AXIS));
//
//                pMiddle.setPreferredSize(pMain.getPreferredSize());//new Dimension(iLeft + iRightW + iLog, iLeft));
//                pMiddle.setLayout(new FlowLayout(FlowLayout.LEFT, iset, iset));
//
//                pButton.setPreferredSize(new Dimension(pMain.getPreferredSize().width, iButton)); //iLeft + iRightW + iLog, iButton));
//                pButton.setLayout(new FlowLayout(FlowLayout.CENTER));
//
//                // Mount panels
//                pButton.add(new JLabel(SwingTools.toIcon("./images/gray/gray-TieFighter.png", iButton, iButton)));
//
//                pButton.add(bAgent);
//                pButton.add(bGame);
//
//                cbShowSplash = new JCheckBox();
//                cbShowSplash.setText("Don't show this again");
//                pButton.add(this.cbShowSplash);
//
////                pMiddle.add(pLeft);
//                pMiddle.add(pRight);
//
//                pMain.add(pMiddle);
//                pMain.add(pButton);
//                fDashboard.add(pMain);
//                fDashboard.pack();
//                fDashboard.show();
//
//                break;
//
//            case COMPACT:
//            default:
//        }
//        if (showsplash) {
////            this.pLeft.getGraphics().drawImage(SwingTools.toIcon("./images/black/" + splash1 + ".png", iStatus, iStatus).getImage(), iRightW / 2 - iStatus / 2, 0, null);
////            this.pLeft.getGraphics().drawImage(SwingTools.toIcon("./images/neg/neg-logougr.png", iLeft, iLeft).getImage(), 0, 0, null);
//            this.fDashboard.setTitle("XUI: eXternal User Interface v 1.0");
//            this.pRight.getGraphics().drawImage(SwingTools.toIcon("./images/neg/neg-logougr.png", iRightH, iRightH).getImage(), 200, 0, null);
//            this.pRight.getGraphics().drawImage(SwingTools.toIcon("./images/black/" + splash2 + ".png", iRightH, iRightH).getImage(), 200 + iRightH, 0, null);
//        } else {
//            goReal();
//            fullLayout();
//        }
//    }
//
//    protected void fullLayout() {
//
//        if (fulllayout) {
//            return;
//        }
//        switch (myLayout) {
//            case DASHBOARD:
//                if (cbShowSplash.isSelected()) {
//                    File f = new File(splashlock);
//                    try {
//                        f.createNewFile();
//                        this.showsplash = false;
//                    } catch (IOException ex) {
//                    }
//                }
//                pLeft.removeAll();
//                pRight.removeAll();
//                dpMyStatus.setPreferredSize(new Dimension(iRight, iStatus));
//                dpMyStatus.setLayout(null);
//                mpVisual.setBounds(10, 150, 185, 150);
//                dpMyStatus.add(mpVisual);
//                mpLidar.setBounds(210, 150, 185, 150);
//                dpMyStatus.add(mpLidar);
//                mpThermal.setBounds(410, 150, 185, 150);
//                dpMyStatus.add(mpThermal);
//                // Mount panels
//                spLog.setPreferredSize(new Dimension(iLog, iRightH));
//
//                pLeft.add(dpMap);
//
//                pRight.add(dpMyStatus);
//
////                pMiddle.add(pLeft);
//                pMiddle.add(pRight);
//                pMiddle.add(spLog);
//
//                pMain.add(pMiddle);
//                pMain.add(pButton);
//                fDashboard.add(pMain);
//                fDashboard.pack();
//                fDashboard.show();
//                break;
//
//            case COMPACT:
//            default:
//                break;
//        }
//        refresh();
//        fulllayout = true;
//    }
//
//    protected void initLayout() {
//
//        switch (myLayout) {
//            case DASHBOARD:
//                family = "blue";
//                // Define sizes
//                iLeft = 450;
//                iLog = iLeft / 3;
//                iRightW = 600;
//                iButton = 64;
//                iStatus = iLeft;
//                iFlightw = iRightW - factor;
//                iFlighth = 100;
//
//                pMain.setPreferredSize(new Dimension(iLeft + iRightW + iLog, iRightH + iButton));
//                pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
//
//                pLeft.setPreferredSize(new Dimension(iLeft, iLeft));
//                pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));
//
//                pRight.setPreferredSize(new Dimension(iRightW, iRightH));
//                pRight.setLayout(new BoxLayout(pRight, BoxLayout.Y_AXIS));
//
//                pMiddle.setPreferredSize(pMain.getPreferredSize());//new Dimension(iLeft + iRightW + iLog, iLeft));
//                pMiddle.setLayout(new FlowLayout(FlowLayout.LEFT, iset, iset));
//
//                pButton.setPreferredSize(new Dimension(pMain.getPreferredSize().width, iButton)); //iLeft + iRightW + iLog, iButton));
//                pButton.setLayout(new FlowLayout(FlowLayout.CENTER));
//
//                dpMyStatus.setPreferredSize(new Dimension(iRightW, iRightH));
//                dpMyStatus.setLayout(null);
//
//                // Mount panels
//                //spLog.setPreferredSize(new Dimension(iLog, iLeft));
//                pButton.add(new JLabel(SwingTools.toIcon("./images/gray/gray-TieFighter.png", iButton, iButton)));
//
//                pButton.add(bAgent);
//                pButton.add(bGame);
//
//                pRight.add(dpMyStatus);
//
//                pMiddle.add(pLeft);
//                pMiddle.add(pRight);
//                pMiddle.add(spLog);
//
//                pMain.add(pMiddle);
//                pMain.add(pButton);
//                fDashboard.add(pMain);
//                fDashboard.pack();
//                fDashboard.show();
//                break;
//
//            case COMPACT:
//            default:
//                break;
//        }
//
//    }
//
//    protected void DashListener(ActionEvent e) {
//        switch (e.getActionCommand()) {
//            case "Agent":
//                goReal();
//                break;
//            case "Play":
//                smContinue.release(1000);
//                smPlay.release(1000);
//                smAllowAgent.release(1000);
//                smStart.release(1000);
//                bPlay.off();
//                bPause.on();
//                bStop.on();
////                bStep.on();
//                bStep.off();
//
//                break;
//            case "Game":
//                goSimulator();
//                break;
//            case "Step":
//                smContinue.release(1);
//                bStop.on();
//                break;
//            case "Stop":
//                smContinue.drainPermits();
//                bPlay.off();
//                bStop.off();
//                bStep.off();
//                bPause.off();
//                this.disableDashBoard();
//                break;
//            case "Pause":
//                smContinue.drainPermits();
//                bPlay.on();
//                bStop.on();
//                bStep.on();
//                bPause.off();
//                break;
//            default:
//                externalExecutor.accept(e.getActionCommand());
//                break;
//        }
//    }
//
//    protected void showTerrain(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//
//        g.drawImage(hFlight.getMap(), null, x, y);
//    }
//
//    protected void showMyStatus(Graphics2D g) {
//        if (!lastPerception.isReady()) {
//            return;
//        }
//
//        g.setBackground(Color.BLACK);
//        g.setColor(Color.BLACK);
//        switch (myLayout) {
//            case DASHBOARD:
//                DashBoardLayout(g);
//                break;
//            case COMPACT:
//            default:
//                CompactLayout(g);
//        }
//    }
//
//    protected void CompactLayout(Graphics2D g) {
//        if (lastPerception.isReady()) {
//            showName(g, 0, 0);
//
//            showAlive(g, 0, 1);
//            showEnergy(g, 1, 1);
//            showCompass(g, 2, 1);
//            showAltimeter(g, 3, 1);
//            showGPS(g, 4, 1);
//
//            showOnTarget(g, 0, 2);
//            showPayLoad(g, 1, 2);
//            showAngular(g, 2, 2);
//            showDistance(g, 3, 2);
//
//            showNSteps(g, 4, 2);
//            showTimer(g, 5, 2);
//
//            showTerrain(g, 0, 3);
//        }
//    }
//
//    protected void DashBoardLayout(Graphics2D g) {
//        if (lastPerception.isReady()) {
//            try {
//                g.setColor(cDodgerB);
//                showName(g, 0, 0);
//
//                showAlive(g, 0, 1);
//                showOnTarget(g, 0, 2);
//                showPayLoad(g, 0, 3);
//                showAltimeter(g, 0, 4);
//                showGPS(g, 0, 5);
//                showNSteps(g, 2, 5);
//                showTimer(g, 4, 5);
//
//                this.showEnergyPB(g, 1, 1, 4);
//                this.showCompassPB(g, 2, 1, 4);
//                this.showAltimeterPB(g, 3, 1, 4);
//                this.showAngularPB(g, 4, 1, 4);
//                this.showDistancePB(g, 5, 1, 4);
//
//                showMiniVisual(g, 1, 6);
//                showMiniLidar(g, 2, 6);
//                showMiniThermal(g, 3, 6);
//                this.showTerrain(g, 0, 13);
//            } catch (Exception ex) {
//
//            }
//        }
//    }
//
//}
