///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
//https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
// */
//package swing;
//
//import JsonObject.JsonArray;
//import glossary.Sensors;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.Graphics2D;
//import java.awt.Insets;
//import java.util.function.Consumer;
//import javax.swing.BorderFactory;
//import javax.swing.JProgressBar;
//import javax.swing.border.EmptyBorder;
//import tools.TimeHandler;
//import static world.Perceptor.NULLREAD;
//import world.SensorDecoder;
//import static world.liveBot.MAXENERGY;
//import static world.liveBot.MAXFLIGHT;
//
///**
// *
// * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
// */
//public class LARVAEmbeddedDash extends MyDrawPane {
//
//    protected int iRightW, iRightH, factor, space, skip, stringskip, zoomSensors,
//            iMaxLevel, worldw, worldh, iMaxDistance, iMaxEnergy = MAXENERGY, lastx, lasty, iIter = 0;
//    protected SensorDecoder lastPerception;
//    protected double[] gps;
//    protected RoundProgressBar rpbEnergy, rpbAltimeter, rpbDistance;
//    protected JProgressBar pbDistance, pbAltitude, pbMaxlevel;
//    protected Angular rpbCompass, rpbAngular;
//    protected TimeHandler tinit, tnow;
//    protected Color cBackgr = new Color(25, 25, 25), cGreen = new Color(32, 178, 170),
//            cTrace = new Color(0, 255, 0), cSky = new Color(100, 100, 100),
//            cSoil = new Color(204, 102, 0), cDodgerB = new Color(0, 102, 204),
//            cStatus = new Color(0, 50, 0), cTextStatus = new Color(0, 200, 0);
//    Color[][] mMap;
//    String sperception = "", name = "", family, myCommitment = "";
//    protected MyDrawPane dpPane;
//
//    public LARVAEmbeddedDash(Consumer<Graphics2D> function) {
//        super(function);
//        this.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//        this.setVisible(true);
//        this.setPreferredSize(new Dimension(450, 100));
//        this.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        setBackground(this.cBackgr);
//
//        lastPerception = new SensorDecoder();
//        factor = 18;
//        space = 10;
//        skip = 4;
//        stringskip = 18;
//        zoomSensors = 25;
//        this.initGUI();
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
////        this.repaint();
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
//            iIter++;
//        } catch (Exception ex) {
//            System.err.println("Error processing perceptions");
//
//        }
//    }
//
//    public void initGUI() {
//        dpPane = new MyDrawPane(g -> this.showMyStatus(g));
//        dpPane.setPreferredSize(new Dimension(450, 100));
//        dpPane.setBackground(this.cBackgr);
//        dpPane.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        this.add(dpPane);
//        this.setVisible(true);
//        rpbEnergy = new RoundProgressBar(0, iMaxEnergy);
//        rpbAltimeter = new RoundProgressBar(0, 256);
//        rpbDistance = new RoundProgressBar(0, 256);
//        rpbCompass = new Angular();
//        rpbAngular = new Angular();
//        family = "blue";
//        // Define sizes
////        this.validate();
////        this.repaint();
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
//                g.setColor(Color.GREEN);
//                showName(g, 0, 0);
//
//                showAlive(g, 0, 1);
//                showEnergy(g, 1, 1);
//                showCompass(g, 2, 1);
//                showAltimeter(g, 3, 1);
//                showGPS(g, 4, 1);
//
//                showOnTarget(g, 0, 2);
//                showPayLoad(g, 1, 2);
//                showAngular(g, 2, 2);
//                showDistance(g, 3, 2);
//
//                showNSteps(g, 4, 2);
//                showTimer(g, 5, 2);
//            } catch (Exception ex) {
//
//            }
//        }
////        this.repaint();
//        System.out.println("   Dashboard repaint");
//
//    }
////    protected void DashBoardLayout(Graphics2D g) {
////        if (lastPerception.isReady()) {
////            try {
////                g.setColor(cTrace);
////                showName(g, 0, 0);
////                g.setColor(cDodgerB);
////                showCargo(g, 0, 6);
//////
////                showAlive(g, 0, 1);
//////                showOnTarget(g, 0, 2);
//////                showPayLoad(g, 0, 3);
//////                showAltimeter(g, 0, 4);
////                showGPS(g, 0, 5);
////                showNSteps(g, 3, 5);
//////                showTimer(g, 5, 5);
//////
////                this.showEnergyPB(g, 2, 1, 4);
//////                this.showCompassPB(g, 4, 1, 4);
//////                this.showAltimeterPB(g, 6, 1, 4);
//////                this.showAngularPB(g, 8, 1, 4);
//////                this.showDistancePB(g, 10, 1, 4);
//////
////            } catch (Exception ex) {
////
////            }
////        }
////    }
//
//    protected void refresh() {
////        SwingTools.doSwingWait(() -> {
//            System.out.println("   Repaint dashboard");
////            this.validate();
////            this.repaint();
////        });
//    }
////    protected void showName(Graphics2D g, int px, int py) {
////        int x = space + px * skip * factor, y = py * factor;
////        g.drawString(name, x, y + stringskip);
////    }
//    protected void showName(Graphics2D g, int px, int py) {
//        String msg = getName();
//        if (getMyCommitment() != null && getMyCommitment().length() > 0) {
//            msg = "▶️️ " + msg; //+" { "+getMyCommitment()+" }";
//        }
//        if (getMyCommitment() != null && getMyCommitment().length() < 1) {
//            msg = "⏸ " + msg;
//        }
////        msg +=" " +getStatus();
//        int x = space + px * skip * factor, y = py * factor;
//        g.drawString(msg, x, y + stringskip);
//    }
//
//    protected void showCargo(Graphics2D g, int px, int py) {
//        int x = space + px * skip * factor, y = py * factor;
//        String cargo = "";
//        for (String s : lastPerception.getCargo()) {
//            cargo += s + ", ";
//        }
//        cargo = "" + lastPerception.getCargo().length + "x " + cargo;
//        g.drawString(cargo, x, y + stringskip);
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
//        rpbEnergy.setUnits("W");
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
//        if (-360 <= lastPerception.getAbsoluteAngular() && lastPerception.getAbsoluteAngular() <= 360) {
//            g.drawString(String.format(" %4.0f º", lastPerception.getAbsoluteAngular()), x + factor, y + stringskip);
//        } else {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//        }
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
//        if (lastPerception.getDistance() >= 0) {
//            g.drawString(String.format(" %5.1f m", lastPerception.getDistance()), x + factor, y + stringskip);
//        } else {
//            g.drawImage(SwingTools.toIcon("./images/gold/gold-warning.png", factor, factor).getImage(), x, y, null);
//        }
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
//            return lastPerception.getName();
//        }
//        return "";
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
//    public String getMyCommitment() {
//        return lastPerception.getCommitment();
//    }
//
//}
