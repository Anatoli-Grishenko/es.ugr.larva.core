///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
//https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
// */
//package swing;
//
//import agswing.AG3DDrawPane;
//import agswing.Circle3D;
//import agswing.Line3D;
//import agswing.Polygon3D;
//import agswing.String3D;
//import com.eclipsesource.json.Json;
//import com.eclipsesource.json.JsonArray;
//import com.eclipsesource.json.JsonObject;
//import data.OleFile;
//import geometry.Point3D;
//import jade.core.Agent;
//import jade.lang.acl.ACLMessage;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.io.File;
//import java.util.HashMap;
//import javax.swing.BoxLayout;
//import javax.swing.ImageIcon;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JScrollBar;
//import javax.swing.JScrollPane;
//import javax.swing.ScrollPaneConstants;
//import javax.swing.border.EmptyBorder;
//import map2D.Palette;
//import tools.TimeHandler;
//import world.SensorDecoder;
//import static world.liveBot.MAXENERGY;
//
///**
// *
// * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
// */
//public class LARVAAirTrafficControl3D {
//
//    public static enum Layout {
//        COMPACT, EXTENDED, DASHBOARD
//    }
//    public static final String MARK = "@DASH@";
//    public static final Color cBad = new Color(100, 0, 0), cThing = Color.YELLOW;
//
//    // Layout GUI
//    protected LARVAFrame fDashboard;
//    protected JPanel pMain;
//    protected AG3DDrawPane dpMap3D;
//    protected JScrollPane sp3D;
//    protected int iRight, iButton, iTiles,
//            iset = 0, iIter = 0, iMaxLevel, iMaxDistance, iMaxEnergy = MAXENERGY,
//            worldw, worldh, iPalette, lastx = -1, lasty = -1;
//    protected double dZoom = 3.0;
//    protected ImageIcon map;
//    protected JLabel lSample;
//    protected SensorDecoder lastPerception;
//    protected TimeHandler tinit, tnow;
//    Color[][] mMap;
//    protected double[] gps;
//
//    protected Color cBackgr = new Color(25, 25, 25), cGreen = new Color(32, 178, 170),
//            cTrace = new Color(0, 255, 0), cSky = new Color(100, 100, 100),
//            cSoil = new Color(204, 102, 0), cDodgerB = new Color(0, 102, 204),
//            cStatus = new Color(0, 50, 0), cTextStatus = new Color(0, 200, 0);
//    HashMap<String, Palette> Palettes;
//
//    int factor = 24, space = 10, skip = 4, stringskip = 18, zoomSensors = 25;
//
//    String family = "phos", splash1 = "TieFighterHelmet", splash2 = "RealTieFighter", name = "unknown",
//            palMap, sperception = "";
//
//    // Remote operation
//    Agent myAgent;
//    ACLMessage dashInbox;
//    boolean simulation = false, showsplash, enablesimulation, fulllayout = false, exitdashboard = false, activated = false;
//    String splashlock = "./dont.show";
//
//    HashMap<String, LARVAEmbeddedDash> Dashboards;
//    OleFile ofile;
//    protected int nFrames, maxFrames = 5;
//    protected HashMap<String, ATC_Trail> trails;
//    protected JsonArray jsaGoals=new JsonArray();
//    int step = 5;
//    int dimx, dimy;
//    public LARVAAirTrafficControl3D() {
//        lastPerception = new SensorDecoder();
//        Palettes = new HashMap();
//        File f = new File(splashlock);
//        this.showsplash = !f.exists();
//        Dashboards = new HashMap();
//        initGUI();
//    }
//
//    public void setTitle(String title) {
//        this.fDashboard.setTitle(title);
//    }
//
//    public String getTitle() {
//        return this.fDashboard.getTitle();
//    }
//
//    public void clear() {
//        OleFile ofile = new OleFile();
//        ofile.loadFile("./images/neg/defaultMap3.png");
//        this.setWorldMap(ofile.toString(), 256, "Visual");
//        nFrames = 0;
//    }
//
//    public boolean setWorldMap(String olefile, int maxlevel, String spalette) {
//        this.lastPerception.setWorldMap(olefile, maxlevel);
//        iMaxLevel = maxlevel;
//        this.palMap = spalette;
//        Palette p = this.getPalette(this.palMap);
//
//        worldw = lastPerception.getWorldMap().getWidth();
//        worldh = lastPerception.getWorldMap().getHeight();
//        mMap = new Color[worldw][worldh];
//        for (int i = 0; i < worldw; i++) {
//            for (int j = 0; j < worldh; j++) {
//                if (lastPerception.getWorldMap().getStepLevel(i, j) > maxlevel) {
//                    mMap[i][j] = cBad;
//                } else {
//                    mMap[i][j] = p.getColor(lastPerception.getWorldMap().getStepLevel(i, j));
//                }
//            }
//        }
//        this.setMap(mMap, p);
//        ofile = new OleFile();
//        ofile.set(olefile);
//        tinit = new TimeHandler();
//        refresh();
//        return true;
//    }
//
//    public void setMap(Color cmap[][], Palette p) {
//        dimx = cmap[0].length;
//        dimy = cmap.length;
//        int wi = dimx * step, he = dimy * step;
//        Palette pal = p;
//
//
//        dpMap3D.clearScene3D();
//        this.trails = new HashMap();
//        this.jsaGoals = new JsonArray();
//        
//        dpMap3D.setCamdistance(5000);
//        String3D s3d;
//        dpMap3D.addObject3D(new Line3D(new Point3D(0, 0, 0), new Point3D(wi, 0, 0)).setColor(Color.GREEN).setName("EjeX"));
//        dpMap3D.addObject3D(new Line3D(new Point3D(0, 0, 0), new Point3D(0, he, 0)).setColor(Color.BLUE).setName("EjeY"));
//        dpMap3D.addObject3D(new Line3D(new Point3D(0, 0, 0), new Point3D(0, 0, wi)).setColor(Color.RED).setName("EjeZ"));
//        
//        s3d = new String3D("X");
//        s3d.setPosition(new Point3D(wi, 0, 0));
//        s3d.setColor(Color.GREEN);
//        dpMap3D.addObject3D(s3d);
//        s3d = new String3D("Y");
//        s3d.setPosition(new Point3D(0, he, 0));
//        s3d.setColor(Color.BLUE);
//        dpMap3D.addObject3D(s3d);
//        s3d = new String3D("Z");
//        s3d.setPosition(new Point3D(0, 0,wi));
//        s3d.setColor(Color.RED);
//        dpMap3D.addObject3D(s3d);
//
//        double f = 0.5; // factor de escala de la altura
//        for (int i = 0; i < dimx - 1; i++) {
//            for (int j = 0; j < dimy - 1; j++) {
//                Color c = pal.getColor(cmap[i][j].getBlue());
//                int alt = (int) (cmap[i][j].getBlue() / f),
//                        alt2 = (int) (cmap[i+1][j].getBlue()/ f),
//                        alt3 = (int) (cmap[i+1][j+1].getBlue() / f),
//                        alt4 = (int) (cmap[i][j+1].getBlue() / f),
//                        alt5 = Math.max(Math.max(alt, alt3), Math.max(alt2, alt4)) + 50;
//                int x = i - dimx / 2, y = j - dimy / 2;
//                Polygon3D poly = new Polygon3D(new Point3D(x * step, y * step, alt)).
//                        setFilled(true).
//                        addVertex(new Point3D(x * step + step, y * step, alt2)).
//                        addVertex(new Point3D(x * step + step, y * step + step, alt3)).
//                        addVertex(new Point3D(x * step, y * step + step, alt4)).
//                        addVertex(new Point3D(x * step, y * step, alt));
//                poly.setColor(c).setName("OBJ-" + i + "-" + j);
//                dpMap3D.addObject3D(poly);
//                if (i == 0) {
//                    dpMap3D.addObject3D(new Polygon3D(new Point3D(x * step, y * step, alt)).
//                            setFilled(true).
//                            addVertex(new Point3D(x * step, y * step + step, alt4)).
//                            addVertex(new Point3D(x * step, y * step + step, 0)).
//                            addVertex(new Point3D(x * step, y * step, 0)).
//                            addVertex(new Point3D(x * step, y * step, alt)).setColor(Color.ORANGE)
//                    );
//                }
//                if (i == dimx - 2) {
//                    dpMap3D.addObject3D(new Polygon3D(new Point3D(x * step + step, y * step, alt2)).
//                            setFilled(true).
//                            addVertex(new Point3D(x * step + step, y * step + step, alt3)).
//                            addVertex(new Point3D(x * step + step, y * step + step, 0)).
//                            addVertex(new Point3D(x * step + step, y * step, 0)).
//                            addVertex(new Point3D(x * step + step, y * step, alt2)).setColor(Color.ORANGE)
//                    );
//                }
//                if (j == 0) {
//                    dpMap3D.addObject3D(new Polygon3D(new Point3D(x * step, y * step, alt)).
//                            setFilled(true).
//                            addVertex(new Point3D(x * step + step, y * step, alt2)).
//                            addVertex(new Point3D(x * step + step, y * step, 0)).
//                            addVertex(new Point3D(x * step, y * step, 0)).
//                            addVertex(new Point3D(x * step, y * step, alt)).setColor(Color.ORANGE)
//                    );
//                }
//                if (j == dimy - 2) {
//                    dpMap3D.addObject3D(new Polygon3D(new Point3D(x * step, y * step + step, alt4)).
//                            setFilled(true).
//                            addVertex(new Point3D(x * step + step, y * step + step, alt3)).
//                            addVertex(new Point3D(x * step + step, y * step + step, 0)).
//                            addVertex(new Point3D(x * step, y * step + step, 0)).
//                            addVertex(new Point3D(x * step, y * step + step, alt4)).setColor(Color.ORANGE)
//                    );
//                }
//                if (i == dimy - j) {
//                    Polygon3D poly2 = new Polygon3D(new Point3D(x * step, y * step, alt5)).
//                            setFilled(true).
//                            addVertex(new Point3D(x * step + step, y * step, alt5)).
//                            addVertex(new Point3D(x * step + step, y * step + step, alt5)).
//                            addVertex(new Point3D(x * step, y * step + step, alt5)).
//                            addVertex(new Point3D(x * step, y * step, alt5));
//                }
//            }
//        }
//        JScrollBar sbaux;
//        sbaux = this.sp3D.getVerticalScrollBar();
//        sbaux.setValue((sbaux.getMaximum()-sbaux.getMinimum())/2);
//        sbaux = this.sp3D.getHorizontalScrollBar();
//        sbaux.setValue((sbaux.getMaximum()-sbaux.getMinimum())/2);
//    }
//    
//    public void setGoals(JsonObject jsgoals) {
//        try {
//            jsaGoals = jsgoals.get("goals").asArray();
//        } catch (Exception ex) {
//
//        }
//    }
//    
//    public void addTrail(String ID, int x, int y, int z) {
//        if (!trails.keySet().contains(ID)) {
//            if (ID.startsWith("F")) {
//                trails.put(ID, new ATC_Trail(ID, Color.GREEN)); //colors[trails.keySet().size()]));
//            } else if (ID.startsWith("C")) {
//                trails.put(ID, new ATC_Trail(ID, Color.RED)); //colors[trails.keySet().size()]));
//            } else if (ID.startsWith("D")) {
//                trails.put(ID, new ATC_Trail(ID, Color.WHITE)); //colors[trails.keySet().size()]));
//            } else if (ID.startsWith("R")) {
//                trails.put(ID, new ATC_Trail(ID, Color.CYAN)); //colors[trails.keySet().size()]));
//            } else {
//                trails.put(ID, new ATC_Trail(ID, Color.YELLOW)); //colors[trails.keySet().size()]));
//            }
//        }
//        Point3D p = new Point3D((x-dimx/2)*step, (y-dimy/2)*step, z+10);
//        trails.get(ID).pushTrail(p);       
//        Circle3D o3d = new Circle3D(p, 10);
//        o3d.setColor(trails.get(ID).c);
////        String3D o3d = new String3D("X");
////        o3d.setColor(trails.get(ID).c).setPosition(p);
//        dpMap3D.addObject3D(o3d);
//        dpMap3D.repaint();
//    }
//    
//    public void feedGoals(String goals) {
//        try {
//            JsonObject jso = Json.parse(goals).asObject();
//            this.setGoals(jso);
//        } catch (Exception ex) {
//
//        }
//    }
//
//    public void feedPerception(String perception) {
//        try {
//            nFrames++;
//            lastPerception.feedPerception(perception);
//            name = lastPerception.getName();
//
//            if (lastPerception.hasSensor("GPS")) {
//                gps = lastPerception.getGPS();
//                lastx = (int) gps[0];
//                lasty = (int) gps[1];
//                int z =(int) gps[2];
//                if (lastx >= 0) {
//                    addTrail(name, lastx, lasty, z);
//                }
//            }
//            // new panel
//        } catch (Exception ex) {
//            System.err.println(ex.toString());
//        }
//    }
//
//    public void initGUI() {
//        // Define sizes
//        iRight = 710;
//
//        // Define panels
//        fDashboard = new LARVAFrame(e -> this.DashListener(e));
//        pMain = new JPanel();
//        pMain.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        pMain.setLayout(new BoxLayout(pMain, BoxLayout.X_AXIS));
//
//        dpMap3D = new AG3DDrawPane(iRight,iRight);
//
//        sp3D = new JScrollPane(dpMap3D);
//        sp3D.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        sp3D.setBackground(Color.BLACK);
//        sp3D.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
//        sp3D.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
//        sp3D.setSize(new Dimension(iRight-10, iRight-10));
//        dpMap3D.setPreferredSize(new Dimension(iRight*5,iRight*5));
//        sp3D.setViewportView(dpMap3D);
//
//
//
//        pMain.setPreferredSize(new Dimension(iRight + iTiles, iRight));
//        pMain.add(sp3D);
//
//        this.fDashboard.addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//                disableDashBoard();
//            }
//        });
//
//        fDashboard.setVisible(true);
//        fDashboard.setResizable(false);
//        fDashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        fDashboard.add(pMain);
//        fDashboard.pack();
//        fDashboard.show();
//        refresh();
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
//        pal.addWayPoint(0, Color.WHITE); // THERMAL
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
//        pal.addWayPoint(0, new Color(0, 180, 0)); // LIDAR
//        pal.addWayPoint(100, new Color(0, 10, 0));
//        pal.fillWayPoints(256);
//        Palettes.put("Lidar", pal);
//
//    }
//
//    protected void DashListener(ActionEvent e) {
//        switch (e.getActionCommand()) {
//        }
//    }
//
//    protected void refresh() {
//        String trace = "";
//        int n = 0, k = 2;
//
//        SwingTools.doSwingWait(() -> {
//            fDashboard.validate();
//            this.fDashboard.repaint();
//        });
//    }
//
//    protected void disableDashBoard() {
//        exitdashboard = true;
//        refresh();
//    }
//
//    public boolean isActivated() {
//        return activated;
//    }
//
//    public void setActivated(boolean activated) {
//        this.activated = activated;
//    }
//
//    protected Palette getPalette(String name) {
//        Palette res = Palettes.get(name);
//        if (res == null) {
//            res = Palettes.get(Palettes.keySet().toArray()[0]);
//        }
//        return res;
//    }
//
//    public HashMap<String, LARVAEmbeddedDash> getDashboards() {
//        return Dashboards;
//    }
//
//    public int getWidth() {
//        int res = this.fDashboard.getWidth();
//        return res;
//    }
//
//    public int getHeight() {
//        int res = this.fDashboard.getHeight();
//        return res;
//    }
//}
