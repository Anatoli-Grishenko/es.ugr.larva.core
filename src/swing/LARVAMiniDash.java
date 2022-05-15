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
//import glossary.Sensors;
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
//public class LARVAMiniDash extends LARVADash {
//
//    protected int iRightW, iRightH;
//
//    public LARVAMiniDash(Agent a) {
//        super(a);
//        
//        myLayout = Layout.DASHBOARD;
//        lastPerception = new SensorDecoder();
//        Palettes = new HashMap();
//        myAgent = a;
//        File f = new File(splashlock);
//        this.showsplash = !f.exists();
//        factor = 13;
//        space = 3;
//        skip = 3;
//        stringskip = 12;
//        zoomSensors = 25;
//        this.initGUI();
//    }
//
//    public boolean preProcessACLM(ACLMessage msg) {
//        boolean res = false;
//        if (msg.getContent().contains("filedata")) {
//            Ole ocontent = new Ole().set(msg.getContent());
//            OleFile ofile = new OleFile(ocontent.getOle("surface"));
//            int maxlevel = ocontent.getInt("maxflight");
//            setWorldMap(ofile.toString(), maxlevel, ocontent.getField("palette"));
//            res = true;
//        }
//        if (msg.getContent().contains("perceptions")) {
//            dashInbox = msg;
//            this.feedPerception(msg.getContent());
//            res = false;
//        }
//        return res;
//    }
//
//    protected boolean setWorldMap(String olefile, int maxlevel, String spalette) {
//        this.lastPerception.setWorldMap(olefile, maxlevel);
//        iMaxLevel = maxlevel;
//
//        tinit = new TimeHandler();
//        worldw = lastPerception.getWorldMap().getWidth();
//        worldh = lastPerception.getWorldMap().getHeight();
//        iMaxDistance = worldw + worldh;
//        rpbDistance.setMaxValue(iMaxDistance);
//        refresh();
//        return true;
//    }
//
//    public void feedPerception(String perception) {
//        if (!perception.equals(sperception)) {
//            feedPerceptionLocal(perception);
//            sperception = perception;
//        }
//    }
//
//    protected void feedPerceptionLocal(String perception) {
//        try {
//            lastPerception.feedPerception(perception);
//            name = lastPerception.getName();
//
//            if (lastPerception.hasSensor("GPS")) {
//                gps = lastPerception.getGPSMemory();
//                lastx = (int) gps[0];
//                lasty = (int) gps[1];
//
//                lastx = (int) gps[0];
//                lasty = (int) gps[1];
//
//            }
//
//            if (getNsteps() == 1) {
//                this.fDashboard.setTitle("| Session: " + this.lastPerception.getSession()
//                        + " |Agent: " + name + " " + fDashboard.getTitle());
//            }
//
//            refresh();
//            iIter++;
//        } catch (Exception ex) {
//            System.err.println("Error processing perceptions");
//
//        }
//    }
//
//    public void initGUI() {
//        if (isActivated())
//            return;
//        // Define panels
//        fDashboard = new LARVAFrame(e -> this.DashListener(e));
//        fDashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
////        fDashboard.setUndecorated(true);
//        pMain = new JPanel();
//        pMain.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//
//        dpMyStatus = new MyDrawPane(g -> showMyStatus(g));
//        dpMyStatus.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        dpMyStatus.setBackground(cBackgr);
//
//        rpbEnergy = new RoundProgressBar(0, iMaxEnergy);
//        rpbAltimeter = new RoundProgressBar(0, 256);
//        rpbDistance = new RoundProgressBar(0, 256);
//        rpbCompass = new Angular();
//        rpbAngular = new Angular();
//
////        fDashboard.setVisible(true);
//        fDashboard.setResizable(false);
//        fDashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
//        family = "blue";
//        // Define sizes
//        iRightW = 450;
//        this.iRightH = 100  ;
//        pMain.setPreferredSize(new Dimension(iRightW, iRightH));
//        pMain.setLayout(new BoxLayout(pMain, BoxLayout.Y_AXIS));
//
//        // Mount panels
//        pMain.add(dpMyStatus);
//        fDashboard.add(pMain);
//        fDashboard.pack();
//        fDashboard.show();
//        dpMyStatus.setPreferredSize(new Dimension(iRightW, iRightH));
//        dpMyStatus.setLayout(null);
//
//        refresh();
//        fulllayout = true;
//    }
//
//    protected void initLayout() {
//
//    }
//
//    protected void DashListener(ActionEvent e) {
//    }
//
//    protected void showMyStatus(Graphics2D g) {
//        if (!lastPerception.isReady()) {
//            return;
//        }
//
//        g.setBackground(Color.BLACK);
//        g.setColor(Color.BLACK);
//        DashBoardLayout(g);
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
//                showNSteps(g, 3, 5);
//                showTimer(g, 5, 5);
//
//                this.showEnergyPB(g, 2, 1, 4);
//                this.showCompassPB(g, 4, 1, 4);
//                this.showAltimeterPB(g, 6, 1, 4);
//                this.showAngularPB(g, 8, 1, 4);
//                this.showDistancePB(g, 10, 1, 4);
//
//                this.showTerrain(g, 0, 13);
//            } catch (Exception ex) {
//
//            }
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
//    protected void showName(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        g.drawString(name, x, y + stringskip);
//    }
//
//    protected void showAlive(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.ALIVE.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        if (lastPerception.getAlive()) {
//            g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-alive.png", factor, factor).getImage(), x, y, null);
//            g.setColor(Color.WHITE);
//            g.drawString("ALIVE", x + factor, y + stringskip);
//        } else {
//            g.drawImage(SwingTools.toIcon("./images/red/red-dead.png", factor, factor).getImage(), x, y, null);
//            g.setColor(Color.WHITE);
//            g.drawString("DEAD", x + factor, y + stringskip);
//        }
//
//    }
//
//    protected void showOnTarget(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.ONTARGET.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        if (lastPerception.getOnTarget()) {
//            g.drawImage(SwingTools.toIcon("./images/phos/phos-target.png", factor, factor).getImage(), x, y, null);
//            g.setColor(Color.WHITE);
//            g.drawString("TARGET", x + factor, y + stringskip);
//        } else {
//            g.drawImage(SwingTools.toIcon("./images/gray/gray-target.png", factor, factor).getImage(), x, y, null);
//            g.setColor(Color.GRAY);
//            g.drawString("UNLCK", x + factor, y + stringskip);
//        }
//
//    }
//
//    protected void showEnergy(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.ENERGY.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        double energy = lastPerception.getEnergy();
//        if (energy > 750) {
//            g.drawImage(SwingTools.toIcon("./images/green/green-energy.png", factor, factor).getImage(), x, y, null);
//        } else if (energy > 500) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-energy.png", factor, factor).getImage(), x, y, null);
//        } else if (energy > 250) {
//            g.drawImage(SwingTools.toIcon("./images/orange/orange-energy.png", factor, factor).getImage(), x, y, null);
//        } else {
//            g.drawImage(SwingTools.toIcon("./images/red/red-energy.png", factor, factor).getImage(), x, y, null);
//        }
//        g.setColor(Color.WHITE);
//
//        g.drawString(String.format(" %04d W", (int) lastPerception.getEnergy()), x + factor, y + stringskip);
//
//    }
//
//    protected void showEnergyPB(Graphics2D g, int px, int py, int w) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.ENERGY.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        double energy = lastPerception.getEnergy();
//
//        rpbEnergy.setPosition(x, y);
//        rpbEnergy.setPreferredSize(w * factor);
//        this.rpbEnergy.setBackground(Color.DARK_GRAY);
//        this.rpbEnergy.setColor(cDodgerB);
//        this.rpbEnergy.setThick(20);
//        rpbAltimeter.setUnits("W");
//        rpbEnergy.setValue((int) energy);
//        rpbEnergy.showProgressBar(g);
//        g.setColor(Color.WHITE);
//
//        if (energy > 750) {
//            g.drawImage(SwingTools.toIcon("./images/green/green-energy.png", factor, factor).getImage(),
//                    x + w * factor / 2 - factor / 2, y + w * factor - factor, null);
//        } else if (energy > 500) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-energy.png", factor, factor).getImage(),
//                    x + w * factor / 2 - factor / 2, y + w * factor - factor, null);
//        } else if (energy > 250) {
//            g.drawImage(SwingTools.toIcon("./images/orange/orange-energy.png", factor, factor).getImage(),
//                    x + w * factor / 2 - factor / 2, y + w * factor - factor, null);
//        } else {
//            g.drawImage(SwingTools.toIcon("./images/red/red-energy.png", factor, factor).getImage(),
//                    x + w * factor / 2 - factor / 2, y + w * factor - factor, null);
//        }
//
//    }
//
//    protected void showAngularPB(Graphics2D g, int px, int py, int w) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.ANGULAR.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        rpbAngular.setPosition(x, y);
//        rpbAngular.setPreferredSize(w * factor);
//        rpbAngular.setThick(15);
//        rpbAngular.setValue((int) lastPerception.getAbsoluteAngular());
//        this.rpbAngular.setColor(cDodgerB);
//        this.rpbAngular.setBackground(Color.DARK_GRAY);
//        rpbAngular.showAngle(g);
//        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-Angular_" + (int) (Math.round(lastPerception.getAbsoluteAngular() / 45) * 45) + ".png",
//                2 * factor, 2 * factor).getImage(),
//                x + w * factor / 2 - factor, y + w * factor / 2 - factor, null);
//        g.setColor(Color.WHITE);
//
//    }
//
//    protected void showCompassPB(Graphics2D g, int px, int py, int w) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.COMPASS.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        rpbCompass.setPosition(x, y);
//        rpbCompass.setPreferredSize(w * factor);
//        rpbCompass.setThick(15);
//        rpbCompass.setValue((int) lastPerception.getCompass());
//        this.rpbCompass.setColor(cDodgerB);
//        this.rpbCompass.setBackground(Color.DARK_GRAY);
//        rpbCompass.showAngle(g);
//        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-Tie_" + lastPerception.getCompass() + ".png",
//                2 * factor, 2 * factor).getImage(),
//                x + w * factor / 2 - factor, y + w * factor / 2 - factor, null);
//        g.setColor(Color.WHITE);
//
//    }
//
//    protected void showCompass(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.COMPASS.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        int compass = lastPerception.getCompass();
//        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-Tie_" + lastPerception.getCompass() + ".png", factor, factor).getImage(), x, y, null);
//        g.setColor(Color.WHITE);
//        g.drawString(String.format(" %03d º", lastPerception.getCompass()), x + factor, y + stringskip);
//    }
//
//    protected void showAltimeter(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.ALTITUDE.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-altitude.png", factor, factor).getImage(), x, y, null);
//        g.setColor(Color.WHITE);
//        g.drawString(String.format(" %03d m", lastPerception.getGround()), x + factor, y + stringskip);
//    }
//
//    protected void showAltimeterPB(Graphics2D g, int px, int py, int w) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.ALTITUDE.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        int realv = lastPerception.getGround(),
//                maxv = MAXFLIGHT;
//        double ratio = 1.0 * realv / maxv;
//        rpbAltimeter.setPosition(x, y);
//        rpbAltimeter.setPreferredSize(w * factor);
//        this.rpbAltimeter.setBackground(Color.DARK_GRAY);
//        this.rpbAltimeter.setThick(20);
//        rpbAltimeter.setUnits("m");
//        rpbAltimeter.setMaxValue(maxv);
//        rpbAltimeter.setValue(realv);
//        if (ratio <= 0.5) {
//            this.rpbAltimeter.setColor(Color.GREEN);
//        } else if (ratio <= 0.75) {
//            this.rpbAltimeter.setColor(Color.YELLOW);
//        } else if (ratio <= 0.90) {
//            this.rpbAltimeter.setColor(Color.ORANGE);
//        } else {
//            this.rpbAltimeter.setColor(Color.RED);
//        }
//        rpbAltimeter.showProgressBar(g);
//        g.setColor(Color.WHITE);
//
//        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-altitude.png", factor, factor).getImage(),
//                x + w * factor / 2 - factor / 2, y + w * factor - factor, null);
//    }
//
//    protected void showNSteps(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-nsteps.png", factor, factor).getImage(), x, y, null);
//        g.setColor(Color.WHITE);
//        g.drawString(String.format(" %03d ", getNsteps()), x + factor, y + stringskip);
//    }
//
//    protected void showAngular(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.ANGULAR.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-Angular_" + (int) (Math.round(lastPerception.getAbsoluteAngular() / 45) * 45) + ".png", factor, factor).getImage(), x, y, null);
//        g.setColor(Color.WHITE);
//        g.drawString(String.format(" %4.0f º", lastPerception.getAbsoluteAngular()), x + factor, y + stringskip);
//    }
//
//    protected void showDistance(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.DISTANCE.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-distance.png", factor, factor).getImage(), x, y, null);
//        g.setColor(Color.WHITE);
//        g.drawString(String.format(" %5.1f m", lastPerception.getDistance()), x + factor, y + stringskip);
//    }
//
//    protected void showDistancePB(Graphics2D g, int px, int py, int w) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.DISTANCE.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        int realv = (int) lastPerception.getDistance();
//        rpbDistance.setPosition(x, y);
//        rpbDistance.setPreferredSize(w * factor);
//        this.rpbDistance.setBackground(Color.DARK_GRAY);
//        this.rpbDistance.setThick(20);
//        rpbDistance.setUnits("m");
//        rpbDistance.setValue(realv);
//        this.rpbDistance.setColor(this.cDodgerB);
//        rpbDistance.showProgressBar(g);
//        g.setColor(Color.WHITE);
//
//        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-distance.png", factor, factor).getImage(),
//                x + w * factor / 2 - factor / 2, y + w * factor - factor, null);
//    }
//
//    protected void showPayLoad(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.PAYLOAD.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-payload.png", factor, factor).getImage(), x, y, null);
//        g.setColor(Color.WHITE);
//        g.drawString(String.format(" %02d", lastPerception.getPayload()), x + factor, y + stringskip);
//    }
//
//    protected void showGPS(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        if (!lastPerception.hasSensor(Sensors.GPS.name())) {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//            return;
//        }
//        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-gps.png", factor, factor).getImage(), x, y, null);
//        double gps[] = lastPerception.getGPSMemory();
//        g.drawString(String.format(" %03d-%03d-%03d", (int) gps[0], (int) gps[1], (int) gps[2]), x + factor, y + stringskip);
//
//    }
//
//    protected void showTimer(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        if (!lastPerception.isReady()) {
//            return;
//        }
//        g.drawImage(SwingTools.toIcon("./images/" + family + "/" + family + "-timer.png", factor, factor).getImage(), x, y, null);
//        g.setColor(Color.WHITE);
//        g.drawString(String.format(" %03d s", getTimerSecs()), x + factor, y + stringskip);
//    }
//
//    protected double[] fromJsonArray(JsonArray jsa) {
//        double res[] = new double[jsa.size()];
//        for (int i = 0; i < jsa.size(); i++) {
//            res[i] = jsa.get(i).asDouble();
//        }
//        return res;
//    }
//
//    public boolean isTooHigh(int level) {
//        return level >= getMaxlevel();
//    }
//
//    public boolean isNullread(int level) {
//        return level == NULLREAD;
//    }
//
//    public int getNsteps() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getNSteps();
//        }
//        return 0;
//
//    }
//
//    public int getTimerSecs() {
//        if (lastPerception.isReady()) {
//            return (int) tinit.elapsedTimeSecs(new TimeHandler());
//        }
//        return 0;
//    }
//
//    public int getMaxlevel() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getMaxlevel();
//        }
//        return -1;
//    }
//
//    public String getStatus() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getStatus();
//        }
//        return "";
//    }
//
//    public boolean getAlive() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getAlive();
//        }
//        return false;
//    }
//
//    public boolean getOnTarget() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getOnTarget();
//        }
//        return false;
//    }
//
//    public double[] getGPSMemory() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getGPSMemory();
//        }
//        return new double[0];
//    }
//
//    public int getPayload() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getPayload();
//        }
//        return -1;
//    }
//
//    public int getEnergyBurnt() {
//        if (lastPerception.isReady()) {
//            return (int) lastPerception.getEnergyBurnt();
//        }
//        return -1;
//    }
//
//    public int getCompass() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getCompass();
//        }
//        return -1;
//    }
//
//    public int getAltitude() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getGround();
//        }
//        return -1;
//    }
//
//    public double getDistance() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getDistance();
//        }
//        return -1;
//
//    }
//
//    public double getAngular() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getAbsoluteAngular();
//        }
//        return -1;
//    }
//
//    public double getEnergy() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getEnergy();
//        }
//        return -1;
//    }
//
//    public int[][] getVisual() {
//        return lastPerception.getVisualData();
//    }
//
//    public int[][] getLidar() {
//        return lastPerception.getLidarData();
//    }
//
//    public int[][] getThermal() {
//        return lastPerception.getThermalData();
//    }
//
//    public String getName() {
//        if (lastPerception.isReady()) {
//            return lastPerception.getStatus();
//        }
//        return "";
//    }
//
//    protected String doReadPerceptions() {
//        ACLMessage outbox = dashInbox.createReply();
//        outbox.setContent("Query Sensors session " + dashInbox.getConversationId());
//        myAgent.send(outbox);
//        dashInbox = myAgent.blockingReceive();
//        this.feedPerceptionLocal(dashInbox.getContent());
//        return dashInbox.getContent();
//    }
//
//    public boolean isOpen() {
//        return !exitdashboard;
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
//    public String printSensors() {
//        String message = "";
//        double reading;
//        double row[];
//        int imatrix[][];
//        double dmatrix[][];
//        for (String s : this.lastPerception.getSensorList()) {
//            message += "\n" + s + ":\n";
//            switch (s.toUpperCase()) {
//                case "ONTARGET":
//                    if (getOnTarget()) {
//                        message += "   TARGET";
//                    } else {
//                        message += "EMPTY";
//                    }
//                    break;
//                case "GPS":
//                    row = getGPSMemory();
//                    message += "   X=" + (int) row[0] + " Y=" + (int) row[1] + " Z=" + (int) row[2];
//                    break;
//                case "ALTITUDE":
//                    reading = getAltitude();
//                    message += "   " + (int) reading;
//                    break;
//                case "ENERGY":
//                    reading = getEnergy();
//                    message += "   " + (int) reading;
//                    break;
//                case "ALIVE":
//                    if (getAlive()) {
//                        message += "   ALIVE";
//                    } else {
//                        message += "DEAD";
//                    }
//                    break;
//                case "COMPASS":
//                    reading = getCompass();
//                    message += "   " + (int) reading;
//                    break;
//                case "ANGULAR":
//                    reading = getAngular();
//                    message += "   " + (int) reading;
//                    break;
//                case "DISTANCE":
//                    reading = getDistance();
//                    message += "   " + (int) reading;
//                    break;
//                case "VISUAL":
//                case "VISUALHQ":
//                    imatrix = getVisual();
//                    for (int r = 0; r < imatrix.length; r++) {
//                        for (int c = 0; c < imatrix[0].length; c++) {
//                            message += String.format("   %03d", imatrix[r][c]);
//                        }
//                        message += "\n";
//                    }
//                    break;
//                case "LIDAR":
//                case "LIDARHQ":
//                    imatrix = getLidar();
//                    for (int r = 0; r < imatrix.length; r++) {
//                        for (int c = 0; c < imatrix[0].length; c++) {
//                            message += String.format("   %03d", imatrix[r][c]);
//                        }
//                        message += "\n";
//                    }
//                    break;
//                case "THERMAL":
//                case "THERMALHQ":
//                    imatrix = getThermal();
//                    for (int r = 0; r < imatrix.length; r++) {
//                        for (int c = 0; c < imatrix[0].length; c++) {
//                            message += String.format("   %03d", imatrix[r][c]);
//                        }
//                        message += "\n";
//                    }
//                    break;
//            }
//            message += "\n";
//        }
//        return message;
//    }
//
//}
