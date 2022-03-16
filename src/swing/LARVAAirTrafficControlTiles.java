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
import data.OleFile;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import map2D.Palette;
import tools.TimeHandler;
import world.SensorDecoder;
import static world.liveBot.MAXENERGY;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class LARVAAirTrafficControlTiles {

    public static enum Layout {
        COMPACT, EXTENDED, DASHBOARD
    }
    public static final String MARK = "@DASH@";
    public static final Color cBad = new Color(100, 0, 0), cThing = Color.YELLOW;

    // Layout GUI
    protected LARVAFrame fDashboard;
    protected JPanel pMain, jpXUI;
    protected MyDrawPane dpMap, dpTiles;
    protected JScrollPane spTiles;
    protected int iRight, iButton, iTiles,
            iset = 0, iIter = 0, iMaxLevel, iMaxDistance, iMaxEnergy = MAXENERGY,
            worldw, worldh, iPalette, lastx = -1, lasty = -1;
    protected AirTrafficControl mpMap;
    protected double dZoom = 3.0;
    protected ImageIcon map;
    protected JLabel lSample;
    protected SensorDecoder lastPerception;
    protected TimeHandler tinit, tnow;
    Color[][] mMap;
    protected double[] gps;

    protected Color cBackgr = new Color(25, 25, 25), cGreen = new Color(32, 178, 170),
            cTrace = new Color(0, 255, 0), cSky = new Color(100, 100, 100),
            cSoil = new Color(204, 102, 0), cDodgerB = new Color(0, 102, 204),
            cStatus = new Color(0, 50, 0), cTextStatus = new Color(0, 200, 0);
    HashMap<String, Palette> Palettes;

    int factor = 24, space = 10, skip = 4, stringskip = 18, zoomSensors = 25;

    String family = "phos", splash1 = "TieFighterHelmet", splash2 = "RealTieFighter", name = "unknown",
            palMap, sperception = "";

    // Remote operation
    Agent myAgent;
    ACLMessage dashInbox;
    boolean simulation = false, showsplash, enablesimulation, fulllayout = false, exitdashboard = false, activated = false;
    String splashlock = "./dont.show";

    HashMap<String, LARVAEmbeddedDash> Dashboards;
    OleFile ofile;
    protected int nFrames, maxFrames = 5;

    public LARVAAirTrafficControlTiles(JPanel XUI) {
        jpXUI = XUI;
        lastPerception = new SensorDecoder();
        Palettes = new HashMap();
        File f = new File(splashlock);
        this.showsplash = !f.exists();
        Dashboards = new HashMap();
        initGUI();
    }

    public LARVAAirTrafficControlTiles() {
        jpXUI = null;
        lastPerception = new SensorDecoder();
        Palettes = new HashMap();
        File f = new File(splashlock);
        this.showsplash = !f.exists();
        Dashboards = new HashMap();
        initGUI();
    }


    public void clear() {
        OleFile ofile = new OleFile();
        ofile.loadFile("./images/neg/defaultMap2.png");
        this.setWorldMap(ofile.toString(), 256, "WB");
//        this.mpMap.setZoom(this.mpMap.zoom + 1);
        for (String s : Dashboards.keySet()) {
            this.dpTiles.remove(Dashboards.get(s));
        }
        this.mpMap.trails = new HashMap();
        Dashboards = new HashMap();
        nFrames = 0;
    }

    public boolean setWorldMap(String olefile, int maxlevel, String spalette) {
        this.lastPerception.setWorldMap(olefile, maxlevel);
        iMaxLevel = maxlevel;
        this.palMap = spalette;
        Palette p = this.getPalette(this.palMap);

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
        ofile = new OleFile();
        ofile.set(olefile);
        tinit = new TimeHandler();
        refresh();
        return true;
    }

    public void feedGoals(String goals) {
        try {
            JsonObject jso = Json.parse(goals).asObject();
            this.mpMap.setGoals(jso);
        } catch (Exception ex) {

        }
    }

    public void feedPerception(String perception) {
        try {
            nFrames++;
            lastPerception.feedPerception(perception);
            name = lastPerception.getName();
            if (!Dashboards.keySet().contains(name)) {
                LARVAEmbeddedDash aux = new LARVAEmbeddedDash(null);
                aux.setWorldMap(this.ofile.toString(), iMaxLevel, null);
                this.dpTiles.add(aux);
//                aux.refresh();
                Dashboards.put(name, aux);
//                this.refresh();
            }

            if (lastPerception.hasSensor("GPS")) {
                gps = lastPerception.getGPS();
                lastx = (int) gps[0];
                lasty = (int) gps[1];
                if (lastx >= 0) {
                    mpMap.addTrail(name, lastx, lasty, 0);
                }
            }
//            Dashboards.get(name).feedPerception(perception);
            // new panel
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
    }

    public void initGUI() {
        // Define panels
        if (jpXUI == null) {
            fDashboard = new LARVAFrame(e -> this.DashListener(e));
            pMain = new JPanel();
            pMain.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            pMain.setLayout(new BoxLayout(pMain, BoxLayout.X_AXIS));

            dpMap = new MyDrawPane(null);
            dpMap.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            dpMap.setBackground(cBackgr);

            dpTiles = new MyDrawPane(null);
            dpTiles.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            dpTiles.setBackground(cBackgr);
            dpTiles.setLayout(new FlowLayout(FlowLayout.CENTER));

            mpMap = new AirTrafficControl(null);
//        mpMap.addRuler();
//        mpMap.addTrail();

            spTiles = new JScrollPane(dpTiles);
            spTiles.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            spTiles.setBackground(cBackgr);
            spTiles.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            spTiles.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            // Define sizes
            iRight = 710;
            iButton = 48;
            iTiles = 0;//455;
            pMain.setPreferredSize(new Dimension(iRight + iTiles, iRight));
            mpMap.setBounds(0, 0, iRight - 2, iRight - 2);
//        dpTiles.setPreferredSize(new Dimension(iTiles, iRight - 2));

            // Mount panes
            dpMap.add(mpMap);
            pMain.add(dpMap);
//        pMain.add(spTiles);

            this.fDashboard.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    disableDashBoard();
                }
            });

            fDashboard.setVisible(true);
            fDashboard.setResizable(false);
            fDashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            fDashboard.add(pMain);
            fDashboard.pack();
            fDashboard.show();
        } else {
            jpXUI.setLayout(new BorderLayout());
            pMain = new JPanel();
            pMain.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            pMain.setLayout(new BoxLayout(pMain, BoxLayout.X_AXIS));

            dpMap = new MyDrawPane(null);
            dpMap.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            dpMap.setBackground(cBackgr);

            dpTiles = new MyDrawPane(null);
            dpTiles.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            dpTiles.setBackground(cBackgr);
            dpTiles.setLayout(new FlowLayout(FlowLayout.CENTER));

            mpMap = new AirTrafficControl(null);


            // Define sizes
            iRight = 710;
            iButton = 48;
            iTiles = 0;//455;
            pMain.setPreferredSize(new Dimension(iRight + iTiles, iRight));
            mpMap.setBounds(0, 0, iRight - 2, iRight - 2);

            // Mount panes
            dpMap.add(mpMap);
            pMain.add(dpMap);            
            jpXUI.add(pMain, BorderLayout.NORTH);
        }
        refresh();

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
        pal.addWayPoint(0, Color.WHITE); // THERMAL
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
        pal.addWayPoint(0, new Color(0, 180, 0)); // LIDAR
        pal.addWayPoint(100, new Color(0, 10, 0));
        pal.fillWayPoints(256);
        Palettes.put("Lidar", pal);

    }

    protected void DashListener(ActionEvent e) {
        switch (e.getActionCommand()) {
        }
    }

    protected void refresh() {
        String trace = "";
        int n = 0, k = 2;

        SwingTools.doSwingWait(() -> {
            pMain.validate();
            pMain.repaint();
        });
    }

    protected void disableDashBoard() {
        exitdashboard = true;
        refresh();
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    protected Palette getPalette(String name) {
        Palette res = Palettes.get(name);
        if (res == null) {
            res = Palettes.get(Palettes.keySet().toArray()[0]);
        }
        return res;
    }

    public HashMap<String, LARVAEmbeddedDash> getDashboards() {
        return Dashboards;
    }

    public int getWidth() {
        int res = this.fDashboard.getWidth();
        return res;
    }

    public int getHeight() {
        int res = this.fDashboard.getHeight();
        return res;
    }
}
