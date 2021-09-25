/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
 */
package swing;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import data.Ole;
import data.OleFile;
import data.Transform;
import jade.lang.acl.ACLMessage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import static javax.swing.SwingConstants.VERTICAL;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import map2D.Map2DColor;
import map2D.Palette;
import tools.TimeHandler;
import static world.Perceptor.NULLREAD;
import world.SensorDecoder;
import static world.SensorDecoder.cBad;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class LARVADash_old {

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
//    protected JButton bZoomIn, bZoomOut, bPlay, bPause, bMe, bGroup, bClass, bDirections, bStatus;
    protected MyPlainButton bZoomIn, bZoomOut, bPlay, bPause, bStop, bStep;
    protected MyPlainButton bRecharge, bRescue, bUp, bDown, bLeft, bRight, bForward, bTouchD;
    protected JTextArea taLog, taStatuses;
    protected JTextPane tpLog;
    protected int iLeft = 400, iRight = 650, iX, iY, iW, iH, iButton = 48,
            iLog = 350, iStatus = 250, iset = 0, iIter = 0, iMaxLevel, iMaxDistance, iMaxEnergy = 1000,
            iFlightw, iFlighth, worldw, worldh, iPalette, lastx = -1, lasty = -1;
    protected MyMapPalPane mpMap, mpVisual, mpLidar, mpThermal;
    protected Layout myLayout;
    protected double dZoom = 3.0;
    protected ImageIcon map;
    protected JLabel lSample;
    protected Map2DColor hFlight; // hVisual
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
    protected Semaphore smPlay;

    int factor = 24, space = 10, skip = 4, stringskip = 18, zoomSensors = 25;

    String family = "phos", splash1 = "TieFighterHelmet", splash2 = "RealTieFighter", name = "unknown",
            palMap;

    protected Consumer<String> externalExecutor;

    public LARVADash_old(Layout layout) {
        myLayout = layout;
        lastPerception = new SensorDecoder();
        Palettes = new HashMap();
        initGUI();
        initLayout();
    }

    public void whenExecute(Consumer<String> executor) {
        externalExecutor = executor;
    }

    protected void initGUI() {
        // Define panels
        fDashboard = new LARVAFrame(e -> this.DashListener(e));
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
        mpVisual = new MyMapPalPane(null);
        mpLidar = new MyMapPalPane(null);
        mpThermal = new MyMapPalPane(null);

        bZoomIn = new MyPlainButton("ZoomIn", "zoom_in.png", this.fDashboard);
        bZoomOut = new MyPlainButton("ZoomOut", "zoom_out.png", fDashboard);
        bPlay = new MyPlainButton("Play", "play.png", fDashboard);
        bPause = new MyPlainButton("Pause", "pause.png", fDashboard);
        bStep = new MyPlainButton("Step", "skip.png", fDashboard);
        bStop = new MyPlainButton("Stop", "stop.png", fDashboard);
        bUp = new MyPlainButton(glossary.capability.UP.name(), "moveUp.png", fDashboard);
        bTouchD = new MyPlainButton(glossary.capability.CAPTURE.name(), "touchDown.png", fDashboard);
        bDown = new MyPlainButton(glossary.capability.DOWN.name(), "moveDown.png", fDashboard);
        bForward = new MyPlainButton(glossary.capability.MOVE.name(), "moveForward.png", fDashboard);
        bRight = new MyPlainButton(glossary.capability.RIGHT.name(), "rotateRight.png", fDashboard);
        bLeft = new MyPlainButton(glossary.capability.LEFT.name(), "rotateLeft.png", fDashboard);
        bRecharge = new MyPlainButton(glossary.capability.RECHARGE.name(), "recharge.png", fDashboard);
        bUp.off();
        bDown.off();
        bLeft.off();
        bRight.off();
        bForward.off();
        this.bTouchD.off();
        this.bRecharge.off();
        bPause.off();
        bStop.off();

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

        smPlay = new Semaphore(0);

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
        pal.addWayPoint(0, new Color(255, 0, 0)); //Mostaphar
//        pal.addWayPoint(10, new Color(25, 0, 0));
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

        pal = new Palette().intoThermal(256);
        Palettes.put("Thermal", pal);

        pal = new Palette();
        pal.addWayPoint(0, new Color(0, 180, 0)); // WB
        pal.addWayPoint(100, new Color(0, 10, 0));
        pal.fillWayPoints(256);
        Palettes.put("Lidar", pal);

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

//                spMap.setPreferredSize(new Dimension(iLeft, iLeft));
//                dpPalette.setPreferredSize(new Dimension(iPalette, iLeft));
//                dpPalette.setLayout(new FlowLayout(FlowLayout.LEFT, iset, iset));
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
//                dpMap = new MyDrawPane(null); //g -> showMpMap(g));
//                dpMap.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//                dpMap.setBackground(cBackgr);
//                pbDistance.setBounds(iRight - factor * 7 + factor / 2, space, 20, factor * 4);
//                pbDistance.setOrientation(VERTICAL);
//                pbDistance.setBackground(Color.DARK_GRAY);
//                pbDistance.setForeground(cDodgerB);
//                pbDistance.setVisible(false);
//                dpMyStatus.add(pbDistance);
//
//                pbAltitude.setBounds(iRight - factor * 6 + factor / 2, space + factor * 5, 20, factor * 4);
//                pbAltitude.setOrientation(VERTICAL);
//                pbAltitude.setBackground(Color.DARK_GRAY);
//                pbAltitude.setForeground(cDodgerB);
//                pbAltitude.setVisible(false);
//                dpMyStatus.add(pbAltitude);
//
//                pbMaxlevel.setBounds(iRight - factor * 7 + factor / 2, space + factor * 5, 20, factor * 4);
//                pbMaxlevel.setOrientation(VERTICAL);
//                pbMaxlevel.setBackground(Color.DARK_GRAY);
//                pbMaxlevel.setForeground(Color.RED);
//                pbMaxlevel.setVisible(false);
//                dpMyStatus.add(pbMaxlevel);
                // Mount panels
                spLog.setPreferredSize(new Dimension(iLog, iLeft));

                pButton.add(new JLabel(SwingTools.toIcon("./images/" + family + "/" + family + "-TieFighter.png", iButton, iButton)));

//                pButton.add(bZoomIn);
//                pButton.add(bZoomOut);
                pButton.add(bPlay);
                pButton.add(bStop);
                pButton.add(bPause);
                pButton.add(bStep);

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
            case EXTENDED:
                family = "phos";
                iLeft = 400;
                iRight = 800;
                iButton = 64;
                iLog = 200;
                iStatus = iLeft + iLog;
                iFlightw = iRight - factor;
                iFlighth = 128;

//                pMain.setPreferredSize(new Dimension(iLeft + iRight, iLeft + iButton+iStatus));
                pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));

                pLeft.setPreferredSize(new Dimension(iLeft, iLeft + iLog));
                pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));

                pMiddle.setPreferredSize(new Dimension(iLeft + iRight, iLeft + iLog));
                pMiddle.setLayout(new FlowLayout(FlowLayout.LEFT, iset, iset));

                spMap.setPreferredSize(new Dimension(iLeft, iLeft));

                pButton.setPreferredSize(new Dimension(iLeft + iRight, iButton));
                pButton.setLayout(new FlowLayout(FlowLayout.LEFT));

                dpMyStatus.setPreferredSize(new Dimension(iRight, iStatus));
                dpMyStatus.setLayout(null);

                tpLog.setPreferredSize(new Dimension(iLog - 32, iLeft));
                spLog.setPreferredSize(new Dimension(iLog, iLeft));

                spLog = new JScrollPane(tpLog);

                // Mount panels
                pButton.add(bZoomIn);
                pButton.add(bZoomOut);
                pButton.add(bPlay);
                pButton.add(bStop);
                pButton.add(bPause);
                pButton.add(bStep);

                pLeft.add(spMap);
                pLeft.add(spLog);

                pMiddle.add(pLeft);
                pMiddle.add(dpMyStatus);

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

//                pMain.setPreferredSize(new Dimension(iLeft + iRight, iLeft + iButton+iStatus));
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
                pButton.add(bZoomIn);
                pButton.add(bZoomOut);
                pButton.add(bPlay);
                pButton.add(bStop);
                pButton.add(bPause);
                pButton.add(bStep);

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
        if (getNsteps() == 0) {
            this.dpMyStatus.getGraphics().drawImage(SwingTools.toIcon("./images/black/" + splash2 + ".png", iStatus, iStatus).getImage(), iRight / 2 - iStatus / 2, 0, null);
            this.mpMap.getGraphics().drawImage(SwingTools.toIcon("./images/black/" + splash1 + ".png", iLeft, iLeft).getImage(), 0, 0, null);
        }

    }

    protected void setZoom(int zoom) {
        dZoom = zoom;
        if (mMap != null) {
            dpMap.setPreferredSize(new Dimension(worldw * (int) dZoom + 2, worldh * (int) dZoom + 2));
            spMap.setViewportView(dpMap);
            spMap.repaint();
        }
    }

    protected void ZoomIn() {

        setZoom((int) dZoom + 1);

    }

    protected void ZoomOut() {
        if (dZoom > 1) {
            setZoom((int) dZoom - 1);
        }

    }

    protected void DashListener(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "ZoomIn":
                ZoomIn();
                break;
            case "ZoomOut":
                ZoomOut();
                break;
            case "Play":
                smPlay.release(10000);
                bPlay.off();
                bPause.on();
                bStop.on();
                bStep.off();
                if (this.externalExecutor != null) {
                    bUp.on();
                    bDown.on();
                    bLeft.on();
                    bRight.on();
                    bForward.on();
                    this.bTouchD.on();
                    this.bRecharge.on();
                    bStep.off();
                    bPlay.off();
                }
                break;
            case "Step":
                smPlay.release(1);
                if (this.externalExecutor != null) {
                    bUp.on();
                    bDown.on();
                    bLeft.on();
                    bRight.on();
                    bForward.on();
                    this.bTouchD.on();
                    this.bRecharge.on();
                    bStep.off();
                    bPlay.off();
                }
                break;
            case "Stop":
                smPlay.drainPermits();
                bZoomIn.off();
                bZoomOut.off();
                bPlay.off();
                bStop.off();
                bStep.off();
                bPause.off();
                break;
            case "Pause":
                smPlay.drainPermits();
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

    public boolean preProcessACLM(ACLMessage msg) {
        if (msg.getContent().contains("filedata")) {
            Ole ocontent = new Ole().set(msg.getContent());
            OleFile ofile = new OleFile(ocontent.getOle("surface"));
            int maxlevel = ocontent.getInt("maxflight");
            setWorldMap(ofile.toString(), maxlevel, ocontent.getField("palette"));
            return true;
        }
        return false;
    }

    public boolean setWorldMap(String olefile, int maxlevel, String spalette) {
        this.lastPerception.setWorldMap(olefile, maxlevel);
        iMaxLevel = maxlevel;
        this.palMap = spalette;
        Palette p = this.getPalette(this.palMap);
//        System.out.println(p.toString());
//        hMap = lastPerception.getWorldMap();
        worldw = lastPerception.getWorldMap().getWidth();
        worldh = lastPerception.getWorldMap().getHeight();
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
        OleFile ofile = new OleFile();
        ofile.set(olefile);
        this.fDashboard.setTitle("| MAP: " + ofile.getFileName() + "|");
        hFlight = new Map2DColor(iFlightw, iFlighth, Color.BLACK);
        for (int i = 0; i < hFlight.getWidth(); i++) {
            for (int j = 0; j < hFlight.getHeight(); j++) {
                if (j % 10 == 0 || i % 10 == 0) {
                    hFlight.setColor(i, j, Color.DARK_GRAY);
                } else {
                    hFlight.setColor(i, j, Color.BLACK);
                }
            }
        }
        tinit = new TimeHandler();
        iMaxDistance = worldw + worldh;
        rpbDistance.setMaxValue(iMaxDistance);
        refresh();
        return true;
    }

    public void feedPerception(String perception) {

        lastPerception.feedPerception(perception);
        name = lastPerception.getName();
        gps = lastPerception.getGPS();
        iVisual = lastPerception.getVisualData();
        iLidar = lastPerception.getLidarData();
        iThermal = lastPerception.getThermalData();
        if (lastx >= 0) {
            mpMap.setImage(null, lastx, lasty);
        }
        mpMap.setImage(SwingTools.toIcon("./images/" + "phos" + "/" + "phos" + "-Tie_" + lastPerception.getCompass() + ".png",
                20, 20),
                (int) gps[0], (int) gps[1]);
        lastx = (int) gps[0];
        lasty = (int) gps[1];
        if (lastx >= 0) {
            mpMap.setImage(null, lastx, lasty);
        }
        mpMap.setImage(SwingTools.toIcon("./images/" + "phos" + "/" + "phos" + "-Tie_" + lastPerception.getCompass() + ".png",
                20, 20),
                (int) gps[0], (int) gps[1]);
        lastx = (int) gps[0];
        lasty = (int) gps[1];
        for (int ih = 0; ih < this.hFlight.getHeight(); ih++) {
            if ((ih * 256) / hFlight.getHeight() < gps[2] - lastPerception.getAltitude()) {
                if (this.myLayout == Layout.DASHBOARD) {
                    hFlight.setColor(iIter, hFlight.getHeight() - ih, cDodgerB);
                } else {
                    hFlight.setColor(iIter, hFlight.getHeight() - ih, cSoil);
                }
            } else {
                if (this.myLayout == Layout.DASHBOARD) {
                    //  hFlight.setColor(iIter, hFlight.getHeight() - ih, cSky);
                } else {
                    hFlight.setColor(iIter, hFlight.getHeight() - ih, cSky);
                }
            }
            if ((ih * 256) / hFlight.getHeight() >= this.iMaxLevel) {
                hFlight.setColor(iIter, hFlight.getHeight() - ih, cBad);
            }
            hFlight.setColor(iIter, hFlight.getHeight() - (int) (hFlight.getHeight() * gps[2] / 256), cTrace);

        }
        if (getNsteps() == 1) {
            this.fDashboard.setTitle("| Session: " + this.lastPerception.getSession()
                    + " |Agent: " + name + " " + fDashboard.getTitle());
        }
        int[][] sensor = iVisual;
        int rangex = sensor[0].length, rangey = sensor.length;
        Palette palette = getPalette("Visual");
        if (cVisual == null) {
            cVisual = new Color[rangex][rangey];
            for (int i = 0; i < rangex; i++) {
                for (int j = 0; j < rangey; j++) {
                    cVisual[i][j] = palette.getColor(0);
                }
            }
            mpVisual.setMap(cVisual, palette);
        }
        for (int i = 0; i < rangex; i++) {
            for (int j = 0; j < rangey; j++) {
                if (sensor[j][i] > lastPerception.getMaxlevel()) {
                    cVisual[i][j] = cBad;
                } else {
                    cVisual[i][j] = palette.getColor(sensor[j][i]);
                }
            }
        }

        sensor = lastPerception.getLidarData();
        rangex = sensor[0].length;
        rangey = sensor.length;
        palette = getPalette("Lidar");
        if (cLidar == null) {
            cLidar = new Color[rangex][rangey];
            for (int i = 0; i < rangex; i++) {
                for (int j = 0; j < rangey; j++) {
                    cLidar[i][j] = palette.getColor(0);
                }
            }
            mpLidar.setMap(cLidar, palette);
        }
        for (int i = 0; i < rangex; i++) {
            for (int j = 0; j < rangey; j++) {
                if (sensor[j][i] < 0) {
                    cLidar[i][j] = cBad;
                } else {
                    cLidar[i][j] = palette.getColor(sensor[j][i]);
                }
            }
        }

        sensor = lastPerception.getThermalData();
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

        addStatus(lastPerception.getStatus());
        refresh();
        iIter++;
        try {
            smPlay.acquire();
        } catch (Exception ex) {
        }
    }

    protected void showMpMap(Graphics2D g) {

    }

    protected void showMap(Graphics2D g) {
        if (mMap == null) {
            return;
        }
        g.setBackground(Color.BLACK);
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, dpMap.getPreferredSize().width, dpMap.getPreferredSize().height);
        Palette p = getPalette(palMap);
        for (int y = 0; y < worldh; y++) {
            for (int x = 0; x < worldw; x++) {
                int px = (int) (1 + x * dZoom), py = (int) (1 + y * dZoom);
//                if (px <= dpMyStatus.getPreferredSize().getWidth()
//                        && py < dpMyStatus.getPreferredSize().getHeight()) {
                if (mMap[x][y] != cTrace) {
                    g.setColor(mMap[x][y]);
                } else {
                    g.setColor(cTrace);
                }
                g.fillRect(px, py, (int) (dZoom), (int) (dZoom));
//                }
            }
        }
        g.setColor(Color.RED);
        g.drawRect(0, 0, (int) (worldw * dZoom + 2), (int) (worldh * dZoom + 2));

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
//        g.fillRect(0, 0, (int) dpMyStatus.getPreferredSize().getWidth(), (int) (dpMyStatus.getPreferredSize().getHeight()));
        switch (myLayout) {
            case DASHBOARD:
                DashBoardLayout(g);
                break;
            case EXTENDED:
                ExtendedLayout(g);
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
//            this.showGroup(g, 1, 6, 1, 2, "VISUAL");
            showMiniLidar(g, 2, 6);
//            this.showGroup(g, 2, 6, 1, 2, "LIDAR");
            showMiniThermal(g, 3, 6);
//            this.showGroup(g, 3, 6, 1, 2, "THERMAL");
            this.showTerrain(g, 0, 13);
        }
    }

    protected void ExtendedLayout(Graphics2D g) {
        if (lastPerception.isReady()) {
            //showName(g, 0, 0);

            showAlive(g, 0, 1);
            showEnergy(g, 1, 1);
            showGroup(g, 0, 1, 2, 1, "CONDITION");

            showCompass(g, 3, 1);
            showAltimeter(g, 4, 1);
            showGPS(g, 5, 1);
            showGroup(g, 3, 1, 4, 1, "ORIENTATION");

            showNSteps(g, 0, 3);
            showTimer(g, 1, 3);
            showGroup(g, 0, 3, 2, 1, "STATS");

            showOnTarget(g, 3, 3);
            showPayLoad(g, 4, 3);
            showAngular(g, 5, 3);
            showDistance(g, 6, 3);
            showGroup(g, 3, 3, 4, 1, "GOAL");

            showVisual(g, 0, 5);
            showGroup(g, 0, 5, 2, 8, "VISUAL");

            showLidar(g, 3, 5);
            showGroup(g, 3, 5, 2, 8, "LIDAR");

            showThermal(g, 6, 5);
            showGroup(g, 6, 5, 2, 8, "THERMAL");

            showTerrain(g, 0, 14);
        }
    }

    protected void Lyout2(Graphics2D g) {
        if (lastPerception.isReady()) {
            //showName(g, 0, 0);

            showAlive(g, 0, 1);
            showEnergy(g, 1, 1);
            showGroup(g, 0, 1, 2, 1, "CONDITION");

            showCompass(g, 3, 1);
            showAltimeter(g, 4, 1);
            showGPS(g, 5, 1);
            showGroup(g, 3, 1, 4, 1, "ORIENTATION");

            showNSteps(g, 0, 3);
            showTimer(g, 1, 3);
            showGroup(g, 0, 3, 2, 1, "STATS");

            showOnTarget(g, 3, 3);
            showPayLoad(g, 4, 3);
            showAngular(g, 5, 3);
            showDistance(g, 6, 3);
            showGroup(g, 3, 3, 4, 1, "GOAL");

            showVisual(g, 0, 5);
            showGroup(g, 0, 5, 2, 8, "VISUAL");

            showTerrain(g, 3, 5);
            showGroup(g, 3, 5, 4, 6, "FLIGHT");

            showLidar(g, 0, 14);
            showGroup(g, 0, 14, 2, 8, "LIDAR");

            showEnergyPB(g, 3, 14, 4);

            showThermal(g, 0, 23);
            showGroup(g, 0, 23, 2, 8, "THERMAL");

        }
    }

    public void addAction(String action) {
//        SwingTools.doSwingWait(() -> {
        tpLog.setBackground(cStatus);
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset1 = sc.addAttribute(SimpleAttributeSet.EMPTY,
                StyleConstants.Foreground, Color.WHITE), aset2 = sc.addAttribute(SimpleAttributeSet.EMPTY,
                        StyleConstants.FontFamily, Font.SERIF);
        AttributeSet aset = sc.addAttributes(aset1, aset2);
        StyledDocument doc = tpLog.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), " " + action + " ", aset);
        } catch (BadLocationException ex) {
        }
        tpLog.setCaretPosition(doc.getLength());
//        });
        refresh();
    }

    public void addStatus(String status) {
        if (status.length() < 1) {
            return;
        }
//        SwingTools.doSwingWait(() -> {
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
//            tpLog.repaint();
//        });
    }

    protected void refresh() {
        String trace = "";
        int n = 0, k = 2;
        SwingTools.doSwingWait(() -> {
            if (externalExecutor != null) {
                pButton.add(bUp);
                pButton.add(bDown);
                pButton.add(bLeft);
                pButton.add(bRight);
                pButton.add(bForward);
                pButton.add(bTouchD);
                pButton.add(bRecharge);
            }
//            dpMyStatus.removeAll();
            fDashboard.validate();
            this.fDashboard.repaint();
        });
    }

//    protected JsonArray getSensor(String sensorname) {
//        if (lastPerception != null) {
//            for (JsonValue jsvsensor : lastPerception.get("perceptions").asArray()) {
//                if (jsvsensor.asObject().getString("sensor", "").equals(sensorname)) {
//                    return jsvsensor.asObject().get("data").asArray();
//                }
//            }
//        }
//        return new JsonArray();
//    }
//
    protected void showName(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        g.drawString(name, x, y + stringskip);
    }

    protected void showAlive(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
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

    protected void showGroup(Graphics2D g, int px, int py, int pw, int ph, String name) {
        int ratio = 4;
        int x = space + px * skip * factor, y = py * factor, w = pw * skip * factor, h = ph * factor;
        Font f = g.getFont();
        g.setFont(new Font(f.getFamily(), f.getStyle(), f.getSize() - ratio));
        if (this.myLayout == Layout.DASHBOARD) {
            g.setColor(cDodgerB);
            g.drawString(name, x + factor, y - ratio);
        } else {
            g.setColor(cTrace);
//            g.drawRect(x - ratio, y - factor / 2, w, h + factor / 2);
            g.drawString(name, x, y - ratio);
        }
        g.setFont(f);
    }

    protected void showEnergy(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
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
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-altitude.png", factor, factor).getImage(), x, y, null);
        g.setColor(Color.WHITE);
        g.drawString(String.format(" %03d m", lastPerception.getAltitude()), x + factor, y + stringskip);
    }

    protected void showAltimeterPB(Graphics2D g, int px, int py, int w) {
        int x = space + px * skip * factor, y = py * factor;
        int realv = lastPerception.getAltitude(),
                maxv = lastPerception.getMaxlevel() - iVisual[iVisual.length / 2][iVisual.length / 2];
        double ratio = 1.0 * realv / maxv;
        if (!lastPerception.isReady()) {
            return;
        }

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
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-Angular_" + (int) (Math.round(lastPerception.getAngular() / 45) * 45) + ".png", factor, factor).getImage(), x, y, null);
        g.setColor(Color.WHITE);
        g.drawString(String.format(" %4.0f º", lastPerception.getAngular()), x + factor, y + stringskip);
    }

    protected void showDistance(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-distance.png", factor, factor).getImage(), x, y, null);
        g.setColor(Color.WHITE);
        g.drawString(String.format(" %5.1f m", lastPerception.getDistance()), x + factor, y + stringskip);
    }

    protected void showDistancePB(Graphics2D g, int px, int py, int w) {
        int x = space + px * skip * factor, y = py * factor;
        int realv = (int) lastPerception.getDistance();
        if (!lastPerception.isReady()) {
            return;
        }

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
        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-payload.png", factor, factor).getImage(), x, y, null);
        g.setColor(Color.WHITE);
        g.drawString(String.format(" %02d", lastPerception.getPayload()), x + factor, y + stringskip);
    }

    protected void showGPS(Graphics2D g, int px, int py) {
        int x = space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
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
        g.drawString(String.format(" %03d s", getTimer()), x + factor, y + stringskip);
    }

    public boolean isTooHigh(int level) {
        return level >= getMaxlevel();
    }

    public boolean isNullread(int level) {
        return level == NULLREAD;
    }

    protected void showVisual(Graphics2D g, int px, int py) {
        int x = space / 2 + space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        int clevel;
        String label;
        Palette palVisual = getPalette("Visual");
        zoomSensors = (25 * 7) / iVisual.length;
        JTextArea taaux;
        for (int row = 0; row < iVisual.length; row++) {
            for (int col = 0; col < iVisual[0].length; col++) {
                taaux = new JTextArea();
                taaux.setBounds(x + col * zoomSensors, y + row * zoomSensors, zoomSensors, zoomSensors);
                clevel = iVisual[row][col];
                Color color = palVisual.getColor(clevel);
                if (isNullread(clevel)) {
                    taaux.setBackground(cBackgr);
                    label = "";
                } else if (isTooHigh(clevel)) {
                    taaux.setBackground(cBad);
                    label = String.format("%03d", clevel);
                } else {
                    taaux.setBackground(color);
                    label = String.format("%03d", clevel);
                }
                taaux.setToolTipText(label);
                if (row == iVisual.length / 2 && col == iVisual.length / 2) {
                    taaux.setText("X");
                    taaux.setBorder(BorderFactory.createLineBorder(cTrace));
                }
                this.dpMyStatus.add(taaux);

            }
        }
        drawPalette(palVisual, g, x + iVisual.length * zoomSensors, y, 35, iVisual.length * zoomSensors);

//        g.setColor(cTrace);
//        g.drawRect(x + iVisual.length / 2 * zoomSensors, y + iVisual[0].length / 2 * zoomSensors, zoomSensors, zoomSensors);
    }

    protected void showMiniVisual(Graphics2D g, int px, int py) {
        if (!lastPerception.isReady() && cVisual != null) {
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
//    protected void showMiniVisual(Graphics2D g, int px, int py) {
//        int x = space / 2 + space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        int clevel;
//        String label;
//        Palette palVisual = getPalette("Visual");
//        zoomSensors = (12 * 7) / iVisual.length;
//        JTextArea taaux;
//        for (int row = 0; row < iVisual.length; row++) {
//            for (int col = 0; col < iVisual[0].length; col++) {
//                taaux = new JTextArea();
//                taaux.setBounds(x + col * zoomSensors, y + row * zoomSensors, zoomSensors, zoomSensors);
//                clevel = iVisual[row][col];
//                Color color = palVisual.getColor(clevel);
//                if (isNullread(clevel)) {
//                    taaux.setBackground(cBackgr);
//                    label = "";
//                } else if (isTooHigh(clevel)) {
//                    taaux.setBackground(cBad);
//                    label = String.format("%03d", clevel);
//                } else {
//                    taaux.setBackground(color);
//                    label = String.format("%03d", clevel);
//                }
//                taaux.setToolTipText(label);
//                if (row == iVisual.length / 2 && col == iVisual.length / 2) {
//                    taaux.setText("^");
//                    taaux.setBorder(BorderFactory.createLineBorder(cTrace));
//                }
//                this.dpMyStatus.add(taaux);
//
//            }
//        }
//        g.drawImage(SwingTools.toIcon("./images/black/Tie_" + lastPerception.getCompass() + ".png",
//                zoomSensors, zoomSensors).getImage(),
//                x + iVisual.length / 2 * zoomSensors, y + iVisual.length / 2 * zoomSensors, null);
//        drawPalette(palVisual, g, x + iVisual.length * zoomSensors, y, iVisual.length * zoomSensors);

//        g.setColor(cTrace);
//        g.drawRect(x + iVisual.length / 2 * zoomSensors, y + iVisual[0].length / 2 * zoomSensors, zoomSensors, zoomSensors);
//        int x = space / 2 + space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        Map2DColor mVisual = lastPerception.getVisual();
//        int level, clevel;
//        int oldzoom = zoomSensors;
//        zoomSensors = 12;
//        for (int row = 0; row < mVisual.getHeight(); row++) {
//            for (int col = 0; col < mVisual.getWidth(); col++) {
//                clevel = mVisual.getStepLevel(col, row);
//                Color color = mVisual.getColor(col, row);
//                if (color.equals(cBad)) {
//                    g.setColor(cBad);
//                    g.fillRect(x + col * zoomSensors, y + row * zoomSensors, zoomSensors, zoomSensors);
//                } else {
//                    g.setColor(new Color(clevel, clevel, clevel));
//                    g.fillRect(x + col * zoomSensors, y + row * zoomSensors, zoomSensors, zoomSensors);
//                }
//            }
//        }
//        g.drawImage(SwingTools.toIcon("./images/black/Tie_" + lastPerception.getCompass() + ".png",
//                zoomSensors, zoomSensors).getImage(),
//                x + mVisual.getWidth() / 2 * zoomSensors, y + mVisual.getHeight() / 2 * zoomSensors, null);
//        zoomSensors = oldzoom;
//    }
    protected void showLidar(Graphics2D g, int px, int py) {
        int x = space / 2 + space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        int clevel;
        String label;
        Palette pal = getPalette("Lidar");
        int[][] isensor = this.getLidar();
        zoomSensors = (25 * 7) / isensor.length;
        JTextArea taaux;
        for (int row = 0; row < isensor.length; row++) {
            for (int col = 0; col < isensor.length; col++) {
                taaux = new JTextArea();
                taaux.setBounds(x + col * zoomSensors, y + row * zoomSensors, zoomSensors, zoomSensors);
                clevel = isensor[row][col];
                Color color = pal.getColor(clevel);
                if (isNullread(clevel)) {
                    taaux.setBackground(cBackgr);
                    label = "";
                } else if (isTooHigh(clevel)) {
                    taaux.setBackground(cBad);
                    label = String.format("%03d", clevel);
                } else {
                    taaux.setBackground(color);
                    label = String.format("%03d", clevel);
                }
                taaux.setToolTipText(label);
                if (row == isensor.length / 2 && col == isensor.length / 2) {
                    taaux.setText("X");
                    taaux.setBorder(BorderFactory.createLineBorder(cTrace));
                }
                this.dpMyStatus.add(taaux);

            }
        }
        drawPalette(pal, g, x + isensor.length * zoomSensors, y, 35, isensor.length * zoomSensors);

    }

    protected void showThermal(Graphics2D g, int px, int py) {
        int x = space / 2 + space + px * skip * factor, y = py * factor;
        if (!lastPerception.isReady()) {
            return;
        }
        int clevel;
        String label;
        Palette pal = getPalette("Thermal");
        int[][] isensor = this.getThermal();
        zoomSensors = (25 * 7) / isensor.length;
        JTextArea taaux;
        for (int row = 0; row < isensor.length; row++) {
            for (int col = 0; col < isensor.length; col++) {
                taaux = new JTextArea();
                taaux.setBounds(x + col * zoomSensors, y + row * zoomSensors, zoomSensors, zoomSensors);
                clevel = isensor[row][col];
                Color color = pal.getColor(clevel);
                if (isNullread(clevel)) {
                    taaux.setBackground(cBackgr);
                    label = "";
                } else if (isTooHigh(clevel)) {
                    taaux.setBackground(cBad);
                    label = String.format("%03d", clevel);
                } else {
                    taaux.setBackground(color);
                    label = String.format("%03d", clevel);
                }
                taaux.setToolTipText(label);
                if (row == isensor.length / 2 && col == isensor.length / 2) {
                    taaux.setText("X");
                    taaux.setBorder(BorderFactory.createLineBorder(cTrace));
                }
                this.dpMyStatus.add(taaux);

            }
        }
        drawPalette(pal, g, x + isensor.length * zoomSensors, y, 35, isensor.length * zoomSensors);

    }

    protected void showMiniLidar(Graphics2D g, int px, int py) {
        if (!lastPerception.isReady() && cLidar != null) {
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
        if (!lastPerception.isReady() && cThermal != null) {
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
//        int x = space / 2 + space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        int oldzoom = zoomSensors;
//        zoomSensors = 12;
//        Map2DColor mThermal = lastPerception.getThermal();
//        int level, clevel;
//        for (int row = 0; row < mThermal.getHeight(); row++) {
//            for (int col = 0; col < mThermal.getWidth(); col++) {
//                level = mThermal.getStepLevel(col, row);
//                Color color = mThermal.getColor(col, row);
//                g.setColor(color);
//                g.fillRect(x + col * zoomSensors, y + row * zoomSensors, zoomSensors, zoomSensors);
////                g.setColor(Color.BLACK);
////                g.drawRect(x + col * zoomSensors, y + row * zoomSensors, zoomSensors, zoomSensors);
//            }
//        }
////        g.setColor(cTrace);
////        g.drawRect(x + mThermal.getWidth() / 2 * zoomSensors, y + mThermal.getHeight() / 2 * zoomSensors, zoomSensors, zoomSensors);
////        g.setColor(cTrace);
////        g.drawLine(x + mThermal.getWidth() / 2 * zoomSensors, y + mThermal.getHeight() / 2 * zoomSensors,
////                x + mThermal.getWidth() / 2 * zoomSensors, y + mThermal.getHeight() / 2 * zoomSensors + zoomSensors);
////        g.drawLine(x + mThermal.getWidth() / 2 * zoomSensors, y + mThermal.getHeight() / 2 * zoomSensors + zoomSensors / 2,
////                x + mThermal.getWidth() / 2 * zoomSensors + zoomSensors, y + mThermal.getHeight() / 2 * zoomSensors + zoomSensors / 2);
////        g.drawLine(x + mThermal.getWidth() / 2 * zoomSensors + zoomSensors, y + mThermal.getHeight() / 2 * zoomSensors,
////                x + mThermal.getWidth() / 2 * zoomSensors + zoomSensors, y + mThermal.getHeight() / 2 * zoomSensors + zoomSensors);
//
//        g.drawImage(SwingTools.toIcon("./images/black/Tie_" + lastPerception.getCompass() + ".png",
//                zoomSensors, zoomSensors).getImage(),
//                x + mThermal.getWidth() / 2 * zoomSensors, y + mThermal.getHeight() / 2 * zoomSensors, null);
//        zoomSensors = oldzoom;

    protected double[] fromJsonArray(JsonArray jsa) {
        double res[] = new double[jsa.size()];
        for (int i = 0; i < jsa.size(); i++) {
            res[i] = jsa.get(i).asDouble();
        }
        return res;
    }

    public int getNsteps() {
        if (lastPerception.isReady()) {
            return lastPerception.getTrace().length;
        }
        return 0;

    }

    public int getTimer() {
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

    public String getStaus() {
        if (lastPerception.isReady()) {
            return lastPerception.getStatus();
        }
        return "";
    }

    public String getSessionID() {
        if (lastPerception.isReady()) {
            return lastPerception.getStatus();
        }
        return "";
    }

    public Map2DColor getWorldMap() {
        return lastPerception.getWorldMap();
//        Map2DColor res = new Map2DColor(worldw, worldh);
//        for (int f = 0; f < res.getHeight(); f++) {
//            for (int c = 0; c < res.getWidth(); c++) {
//                int level = hMap.getStepLevel(c, f);
//                if (level > maxlevel) {
//                    res.setColor(c, f, cBad);
//                } else {
//                    res.setColor(c, f, hMap.getColor(c, f));
//                }
//            }
//        }
//        return res;
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

    public String[] getTrace() {
        if (lastPerception.isReady()) {
            return lastPerception.getTrace();
        }
        return new String[0];
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
            return lastPerception.getStatus();
        }
        return "";
    }

    public Palette getPalette(String name) {
        Palette res = Palettes.get(name);
        if (res == null) {
            res = Palettes.get(Palettes.keySet().toArray()[0]);
        }
        return res;
    }

    public void drawPalette(Palette p, Graphics2D g, int x, int y, int w, int h) {
        int ph = 10, pt = (2 * w) / 3 - 1, n = (h - ph) / ph;
        Font f = g.getFont();
        g.setFont(new Font(f.getFamily(), f.getStyle(), f.getSize() - 2));
        for (int i = 0, k = 0; i <= n; k++, i++) {
            int c = (i * p.size()) / n;
            if (c >= p.size()) {
                c = p.size() - 1;
            }
            g.setColor(p.getColor(c));
            g.fillRect(x + pt, y + k * ph, w - pt, ph);
            g.setColor(Color.WHITE);
            g.drawRect(x + pt, y + k * ph, w - pt, ph);
            g.drawString(String.format("%03d", c), x, y + k * ph + ph);
        }
        g.setFont(f);

    }

}
