/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import Environment.Environment;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import data.Ole;
import data.OleFile;
import geometry.OleBag;
import geometry.OleDiode;
import geometry.OleHud;
import geometry.OleLabels;
import geometry.OleLinear;
import geometry.OleMap;
import geometry.OleRotatory;
import geometry.OleRoundPB;
import geometry.OleSemiDial;
import geometry.Point3D;
import geometry.PolarSurface;
import geometry.SimpleVector3D;
import jade.lang.acl.ACLMessage;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;
import map2D.Map2DColor;
import map2D.Palette;
import tools.TimeHandler;
import tools.emojis;
import world.SensorDecoder;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleDashBoard extends OleDrawPane {

    public static Color cDeck = Color.GRAY, cFrame = Color.DARK_GRAY, cGauge = new Color(0, 0, 0), cDial = SwingTools.doDarker(Color.WHITE), cBad = SwingTools.doDarker(SwingTools.doDarker(Color.RED)),
            cDistance = SwingTools.doDarker(Color.MAGENTA), cAngle = SwingTools.doDarker(Color.CYAN), cGround = SwingTools.doDarker(SwingTools.doDarker(Color.ORANGE)),
            cGoal = Color.YELLOW, cTrack = Color.GREEN, cCompass = Color.WHITE, cLabels = SwingTools.doDarker(Color.WHITE);

    public HashMap<String, OleSensor> mySensorsVisual, myExternalSensor;
    public ArrayList<String> layoutSensors;

    protected Component myParent;
    public Environment decoder;
    OleSemiDial osAltitude, osBattery;
    OleSensor osGround;
    OleRotatory orCompass1;
    OleDiode odLed[] = new OleDiode[10];
    OleLinear olTime, olSteps, olGPS, olBurnt;
    OleBag olPayload, olCommand;
    OleMap osMap;
    OleHud osHud;
    OleLabels topLabels;
    Palette pal;
    String sperception = "", agentName, otherAgents;
    int iTrace, lastx, lasty;
    double arrayReading[], gps[];
    int iVisual[][], iLidar[][], iThermal[][];
    String lastPerception = "";
    TimeHandler tstart;
    boolean showTrail, availableDashBoard;
    int trailSize;

    public OleDashBoard(Component parent, String nameagent) {
        myParent = parent;
        mySensorsVisual = new HashMap();
        myExternalSensor = new HashMap();
        layoutSensors = new ArrayList();
        decoder = new Environment();
        this.setLayout((LayoutManager) null);
        agentName = nameagent;
        availableDashBoard = false;
        initLayout();
    }

    @Override
    public void OleDraw(Graphics2D g) {
        myg = g;

//        myg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (String s : layoutSensors) {
            if (availableDashBoard) {
                mySensorsVisual.get(s).viewSensor(g);
            }
        }
//        if (decoder.getVisualData().length > 0) {
//            Map2DColor m;
//            int zoom = 4;
//            g.setColor(Color.WHITE);
//            m = decoder.getFullZenitalVisual();
//            g.drawImage(m.getMap(), 0, 700, m.getWidth() * 3, m.getHeight() * 3, null);
//            g.drawRect(0, 700, m.getWidth() * 3, m.getHeight() * 3);
//            m = decoder.getRelativeVisual();
//            g.drawImage(m.getMap(), 200, 700, m.getWidth() * 4, m.getHeight() * 4, null);
//            g.drawRect(200, 700, m.getWidth() * 4, m.getHeight() * 4);
//            m = decoder.getRelativeVisual();
//            g.drawImage(m.getMap(), 400, 700, m.getWidth() * 4, m.getHeight() * 4, null);
//            g.drawRect(400, 700, m.getWidth() * 4, m.getHeight() * 4);
//            m = decoder.getPolarVisual();
//            g.drawImage(m.getMap(), 600, 700, m.getWidth() * 4, m.getHeight() * 4, null);
//            g.drawRect(600, 700, m.getWidth() * 4, m.getHeight() * 4);
//        }
//        System.out.println("Repaint");
    }

    public void addSensor(OleSensor oles) {
        mySensorsVisual.put(oles.getName(), oles);
        mySensorsVisual.put(oles.getExternalSensor(), oles);
        layoutSensors.add(oles.getName());
    }

    public void initLayout() {
        Layout2();
        this.validate();
        this.repaint();
    }

    protected void Layout1() {
        int hLabels = 50, yy = hLabels, xx = 0, ww = 150, ww2 = 50, hh = 25;

        osMap = new OleMap(this, "MAP");
        osMap.setBounds(0 * ww, hLabels, 4 * ww, 4 * ww);
        osMap.setForeground(Color.WHITE);
        osMap.setBackground(Color.BLACK);
        osMap.showFrame(true);
        osMap.validate();

        osHud = new OleHud(this, "AUX");
        osHud.setBounds(4 * ww, hLabels, 4 * ww, 4 * ww);
        osHud.setForeground(Color.WHITE);
        osHud.setBackground(Color.BLACK);
        osHud.showFrame(true);
        osHud.setVPStretch(37);
        osHud.setSPStretch(22);
        osHud.validate();

        orCompass1 = new OleRotatory(this, "Compass");
        orCompass1.setMinValue(0);
        orCompass1.setMaxValue(360);
        orCompass1.setMinVisual(0);
        orCompass1.setMaxVisual(360);
        orCompass1.setnDivisions(8);
        orCompass1.setLabels(new String[]{"N", "NW", "W", "SW", "S", "SE", "E", "NE"});
        orCompass1.showScaleNumbers(true);
        orCompass1.setBounds(20 + 4 * ww, hLabels + 4 * ww - 141, 125, 125);
        orCompass1.setForeground(this.cLabels);
        orCompass1.setBackground(this.cGauge);
//        orCompass1.showFrame(true);
        orCompass1.setAutoRotate(false);
        orCompass1.validate();

        osAltitude = new OleSemiDial(this, "Altitude");
        osAltitude.setMinValue(0);
        osAltitude.setMaxValue(255);
        osAltitude.setStartAngle(225);
        osAltitude.setEndAngle(-45);
        osAltitude.setnDivisions(10);
        osAltitude.setBounds(145 + 4 * ww, hLabels + 4 * ww - 141, 125, 125);
        osAltitude.setForeground(this.cLabels);
        osAltitude.setBackground(this.cGauge); //SwingTools.doDarker(Color.DARK_GRAY));
        pal = new Palette();
        pal.addWayPoint(0, Color.BLACK);
        pal.addWayPoint(100, this.cGauge);
        pal.fillWayPoints(255);
        osAltitude.setPalette(pal);
        osAltitude.showScale(true);
        osAltitude.showScaleNumbers(true);
//        osAltitude.showFrame(true);
        osAltitude.validate();

        osGround = new OleRoundPB(this, "Ground");
        osGround.setMinValue(0);
        osGround.setMaxValue(275);
        osGround.setStartAngle(225);
        osGround.setEndAngle(-45);
        osGround.setnDivisions(11);
        osGround.setBounds(265 + 4 * ww, hLabels + 4 * ww - 141, 125, 125);
        osGround.setForeground(this.cLabels);
        osGround.setBackground(this.cGauge); //SwingTools.doDarker(Color.DARK_GRAY));
//        pal = new Palette();
//        pal.addWayPoint(0, Color.RED);
//        pal.addWayPoint(20, Color.WHITE);
//        pal.addWayPoint(100, Color.WHITE);
//        pal.fillWayPoints(275);
//        osGround.setPalette(pal);
        osGround.showScale(true);
        osGround.showScaleNumbers(false);
        osGround.setAlertLimitBelow(10);
        osGround.validate();

        osBattery = new OleSemiDial(this, "Energy");
        osBattery.setMinValue(0);
        osBattery.setMaxValue(3500);
        osBattery.setStartAngle(180);
        osBattery.setEndAngle(0);
        osBattery.setnDivisions(4);
        osBattery.setBounds(385 + 4 * ww, hLabels + 4 * ww - 141, 125, 125);
        osBattery.setForeground(this.cLabels);
        osBattery.setBackground(this.cGauge); //SwingTools.doDarker(Color.DARK_GRAY));
//        pal = new Palette();
//        pal.addWayPoint(0, Color.RED);
//        pal.addWayPoint(50, Color.YELLOW);
//        pal.addWayPoint(100, Color.GREEN);
//        pal.fillWayPoints(3500);
//        osBattery.setPalette(pal);
        osBattery.showScale(true);
        osBattery.showScaleNumbers(true);
        osBattery.setAlertLimitBelow(300);
        osBattery.validate();

        olGPS = new OleLinear(this, "GPS");
        olGPS.setBackground(Color.DARK_GRAY);
        olGPS.setnColumns(2);
        olGPS.getAllReadings()[0][0] = 100;
        olGPS.getAllReadings()[0][1] = 121;
        olGPS.setBounds(8 * ww, hLabels, 2 * ww2, 4 * hh);
        olGPS.showFrame(true);
        olGPS.setForeground(this.cLabels);
        olGPS.validate();

        olTime = new OleLinear(this, "TIME");
        olTime.setForeground(this.cLabels);
        olTime.setBackground(Color.DARK_GRAY);
        olTime.setnColumns(1);
        olTime.getAllReadings()[0][0] = 123;
        olTime.setBounds(8 * ww, 4 * hh + hLabels, 2 * ww2, 2 * hh);
        olTime.showFrame(true);
        olTime.validate();

        olSteps = new OleLinear(this, "STEPS");
        olSteps.setForeground(this.cLabels);
        olSteps.setBackground(Color.DARK_GRAY);
        olSteps.setnColumns(1);
        olSteps.getAllReadings()[0][0] = 123;
        olSteps.setBounds(8 * ww, 6 * hh + hLabels, 2 * ww2, 2 * hh);
        olSteps.showFrame(true);
        olSteps.validate();

        olBurnt = new OleLinear(this, "BURNT");
        olBurnt.setForeground(this.cLabels);
        olBurnt.setBackground(Color.DARK_GRAY);
        olBurnt.setnColumns(1);
        olBurnt.getAllReadings()[0][0] = 123;
        olBurnt.setBounds(8 * ww, 8 * hh + hLabels, 2 * ww2, 2 * hh);
        olBurnt.showFrame(true);
        olBurnt.validate();

        xx = 8 * ww;
        yy = 10 * hh + hLabels;
        odLed[0] = new OleDiode(this, "ALV");
        odLed[0].attachToExternalSensor("alive");
        odLed[0].setBounds(xx, yy, 2 * ww2, hh);
        odLed[0].setForeground(Color.GREEN);
        odLed[0].setBackground(Color.BLACK);
        odLed[0].showFrame(true);
        odLed[0].validate();
        yy += hh;
        odLed[1] = new OleDiode(this, "TAR");
        odLed[1].attachToExternalSensor("ontarget");
        odLed[1].setBounds(xx, yy, 2 * ww2, hh);
        odLed[1].setForeground(Color.GREEN);
        odLed[1].setBackground(Color.BLACK);
        odLed[1].showFrame(true);
        odLed[1].validate();
        yy += hh;

        odLed[2] = new OleDiode(this, "PAY");
        odLed[2].setBounds(xx, yy, 2 * ww2, hh);
        odLed[2].setForeground(Color.GREEN);
        odLed[2].setBackground(Color.BLACK);
        odLed[2].showFrame(true);
        odLed[2].validate();
        yy += hh;

//        odLed[3] = new OleDiode(this, "SND");
//        odLed[3].setBounds(xx, yy, 2 * ww2, hh);
//        odLed[3].setForeground(OleApplication.DodgerBlue);
//        odLed[3].setBackground(Color.DARK_GRAY);
//        odLed[3].showFrame(true);
//        odLed[3].validate();
//        yy += hh;
//
//        odLed[4] = new OleDiode(this, "RCV");
//        odLed[4].setBounds(xx, yy, 2 * ww2, hh);
//        odLed[4].setForeground(OleApplication.Maroon);
//        odLed[4].setBackground(Color.BLACK);
//        odLed[4].showFrame(true);
//        odLed[4].validate();
//        yy += hh;
        olPayload = new OleBag(this, "PAYLOAD");
        olPayload.setForeground(Color.YELLOW);
        olPayload.setBackground(Color.DARK_GRAY);
        olPayload.setBounds(xx, yy, 2 * ww2, 11 * hh);
        olPayload.showFrame(true);
        olPayload.validate();
        xx += 2 * ww2;
        yy = hLabels;
        olCommand = new OleBag(this, "COMMAND");
        olCommand.setForeground(Color.GREEN);
        olCommand.setBackground(Color.DARK_GRAY);
        olCommand.setBounds(xx, yy, 3 * ww2, 24 * hh);
        olCommand.showFrame(true);
        olCommand.validate();

        topLabels = new OleLabels(this, "LABELS");
        topLabels.setForeground(Color.WHITE);
        topLabels.setBackground(Color.DARK_GRAY);
        topLabels.setBounds(0, 0, 1450, hLabels);
        topLabels.showFrame(true);
        topLabels.validate();

        this.addSensor(osHud);
        this.addSensor(odLed[0]);
        this.addSensor(odLed[1]);
        this.addSensor(odLed[2]);
//        this.addSensor(odLed[3]);
//        this.addSensor(odLed[4]);
        this.addSensor(osMap);
        this.addSensor(olGPS);
        this.addSensor(olTime);
        this.addSensor(olSteps);
        this.addSensor(olBurnt);
        this.addSensor(olPayload);
        this.addSensor(olCommand);
        this.addSensor(topLabels);
        this.addSensor(orCompass1);
        this.addSensor(osAltitude);
        this.addSensor(osGround);
        this.addSensor(osBattery);
        myParent.validate();

    }

    protected void Layout2() {
        int hLabels = 50, yy = hLabels, xx = 0, ww = 150, ww2 = 50, hh = 25, ldials = 110;

        osMap = new OleMap(this, "MAP");
        osMap.setBounds(0 * ww, hLabels, 4 * ww, 4 * ww);
        osMap.setForeground(Color.WHITE);
        osMap.setBackground(Color.BLACK);
        osMap.showFrame(true);
        osMap.validate();

        osHud = new OleHud(this, "AUX");
        osHud.setBounds(4 * ww, hLabels, 4 * ww, 4 * ww);
        osHud.setForeground(Color.WHITE);
        osHud.setBackground(Color.BLACK);
        osHud.setSPStretch(0);
        osHud.setVPStretch(ldials / 2);
        osHud.showFrame(true);
        osHud.validate();

        orCompass1 = new OleRotatory(this, "Compass");
        orCompass1.setMinValue(0);
        orCompass1.setMaxValue(360);
        orCompass1.setMinVisual(0);
        orCompass1.setMaxVisual(360);
        orCompass1.setnDivisions(8);
        orCompass1.setLabels(new String[]{"N", "NW", "W", "SW", "S", "SE", "E", "NE"});
        orCompass1.showScaleNumbers(true);
        orCompass1.setBounds(4 * ww, 20 + hLabels, ldials, ldials);
        orCompass1.setForeground(this.cLabels);
        orCompass1.setBackground(this.cGauge);
//        orCompass1.showFrame(true);
        orCompass1.setAutoRotate(true);
        orCompass1.validate();

        osAltitude = new OleSemiDial(this, "Altitude");
        osAltitude.setMinValue(0);
        osAltitude.setMaxValue(255);
        osAltitude.setStartAngle(225);
        osAltitude.setEndAngle(0);
        osAltitude.setnDivisions(10);
        osAltitude.setBounds(4 * ww, 20 + hLabels + ldials, ldials, ldials);
        osAltitude.setForeground(this.cLabels);
        osAltitude.setBackground(Color.BLACK); //SwingTools.doDarker(Color.DARK_GRAY));
        osAltitude.setSimplifiedDial(true);
//        pal = new Palette();
//        pal.addWayPoint(0, Color.WHITE);
//        pal.addWayPoint(100, Color.RED);
//        pal.fillWayPoints(255);
//        osAltitude.setPalette(pal);
//        osAltitude.showScale(true);
//        osAltitude.showScaleNumbers(true);
//        osAltitude.showFrame(true);
        osAltitude.validate();

        osGround = new OleSemiDial(this, "Ground");
        osGround.setMinValue(0);
        osGround.setMaxValue(255);
        osGround.setStartAngle(225);
        osGround.setEndAngle(0);
        osGround.setnDivisions(10);
        osGround.setBounds(4 * ww, 20 + hLabels + ldials * 2, ldials, ldials);
        osGround.setForeground(this.cLabels);
        osGround.setBackground(this.cGauge); //SwingTools.doDarker(Color.DARK_GRAY));
        osGround.setSimplifiedDial(true);
//        pal = new Palette();
//        pal.addWayPoint(0, Color.RED);
//        pal.addWayPoint(20, Color.WHITE);
//        pal.addWayPoint(100, Color.WHITE);
//        pal.fillWayPoints(275);
//        osGround.setPalette(pal);
//        osGround.showScale(true);
//        osGround.showScaleNumbers(false);
        osGround.setAlertLimitBelow(10);
        osGround.validate();

        osBattery = new OleSemiDial(this, "Energy");
        osBattery.setMinValue(0);
        osBattery.setMaxValue(3500);
        osBattery.setStartAngle(180);
        osBattery.setEndAngle(0);
        osBattery.setnDivisions(4);
        osBattery.setBounds(4 * ww, 20 + hLabels + ldials * 3, ldials, ldials);
        osBattery.setForeground(this.cLabels);
        osBattery.setBackground(Color.BLACK); //SwingTools.doDarker(Color.DARK_GRAY));
        osBattery.setSimplifiedDial(true);
        osBattery.setAlertLimitBelow((int) (osBattery.getMaxValue() / 5));
        pal = new Palette();
        pal.addWayPoint(0, Color.RED);
        pal.addWayPoint(20, Color.RED);
        pal.addWayPoint(21, Color.YELLOW);
        pal.addWayPoint(75, Color.YELLOW);
        pal.addWayPoint(76, Color.YELLOW);
        pal.addWayPoint(100, Color.GREEN);
        pal.fillWayPoints(3500);
        osBattery.setPalette(pal);
//        osBattery.showScale(true);
//        osBattery.showScaleNumbers(true);
        osBattery.validate();

        olGPS = new OleLinear(this, "GPS");
        olGPS.setBackground(Color.DARK_GRAY);
        olGPS.setnColumns(2);
        olGPS.getAllReadings()[0][0] = 100;
        olGPS.getAllReadings()[0][1] = 121;
        olGPS.setBounds(8 * ww, hLabels, 2 * ww2, 4 * hh);
        olGPS.showFrame(true);
        olGPS.setForeground(this.cLabels);
        olGPS.validate();

        olTime = new OleLinear(this, "TIME");
        olTime.setForeground(this.cLabels);
        olTime.setBackground(Color.DARK_GRAY);
        olTime.setnColumns(1);
        olTime.getAllReadings()[0][0] = 123;
        olTime.setBounds(8 * ww, 4 * hh + hLabels, 2 * ww2, 2 * hh);
        olTime.showFrame(true);
        olTime.validate();

        olSteps = new OleLinear(this, "STEPS");
        olSteps.setForeground(this.cLabels);
        olSteps.setBackground(Color.DARK_GRAY);
        olSteps.setnColumns(1);
        olSteps.getAllReadings()[0][0] = 123;
        olSteps.setBounds(8 * ww, 6 * hh + hLabels, 2 * ww2, 2 * hh);
        olSteps.showFrame(true);
        olSteps.validate();

        olBurnt = new OleLinear(this, "BURNT");
        olBurnt.setForeground(this.cLabels);
        olBurnt.setBackground(Color.DARK_GRAY);
        olBurnt.setnColumns(1);
        olBurnt.getAllReadings()[0][0] = 123;
        olBurnt.setBounds(8 * ww, 8 * hh + hLabels, 2 * ww2, 2 * hh);
        olBurnt.showFrame(true);
        olBurnt.validate();

        xx = 8 * ww;
        yy = 10 * hh + hLabels;
        odLed[0] = new OleDiode(this, "ALV");
        odLed[0].attachToExternalSensor("alive");
        odLed[0].setBounds(xx, yy, 2 * ww2, hh);
        odLed[0].setForeground(Color.GREEN);
        odLed[0].setBackground(Color.BLACK);
        odLed[0].showFrame(true);
        odLed[0].validate();
        yy += hh;
        odLed[1] = new OleDiode(this, "TAR");
        odLed[1].attachToExternalSensor("ontarget");
        odLed[1].setBounds(xx, yy, 2 * ww2, hh);
        odLed[1].setForeground(Color.GREEN);
        odLed[1].setBackground(Color.BLACK);
        odLed[1].showFrame(true);
        odLed[1].validate();
        yy += hh;

        odLed[2] = new OleDiode(this, "PAY");
        odLed[2].setBounds(xx, yy, 2 * ww2, hh);
        odLed[2].setForeground(Color.GREEN);
        odLed[2].setBackground(Color.BLACK);
        odLed[2].showFrame(true);
        odLed[2].validate();
        yy += hh;

//        odLed[3] = new OleDiode(this, "SND");
//        odLed[3].setBounds(xx, yy, 2 * ww2, hh);
//        odLed[3].setForeground(OleApplication.DodgerBlue);
//        odLed[3].setBackground(Color.DARK_GRAY);
//        odLed[3].showFrame(true);
//        odLed[3].validate();
//        yy += hh;
//
//        odLed[4] = new OleDiode(this, "RCV");
//        odLed[4].setBounds(xx, yy, 2 * ww2, hh);
//        odLed[4].setForeground(OleApplication.Maroon);
//        odLed[4].setBackground(Color.BLACK);
//        odLed[4].showFrame(true);
//        odLed[4].validate();
//        yy += hh;
        olPayload = new OleBag(this, "PAYLOAD");
        olPayload.setForeground(Color.YELLOW);
        olPayload.setBackground(Color.DARK_GRAY);
        olPayload.setBounds(xx, yy, 2 * ww2, 11 * hh);
        olPayload.showFrame(true);
        olPayload.validate();
        xx += 2 * ww2;
        yy = hLabels;
        olCommand = new OleBag(this, "COMMAND");
        olCommand.setForeground(Color.GREEN);
        olCommand.setBackground(Color.DARK_GRAY);
        olCommand.setBounds(xx, yy, 3 * ww2, 24 * hh);
        olCommand.showFrame(true);
        olCommand.validate();

        topLabels = new OleLabels(this, "LABELS");
        topLabels.setForeground(Color.WHITE);
        topLabels.setBackground(Color.DARK_GRAY);
        topLabels.setBounds(0, 0, 1450, hLabels);
        topLabels.showFrame(true);
        topLabels.validate();

        this.addSensor(osHud);
        this.addSensor(odLed[0]);
        this.addSensor(odLed[1]);
        this.addSensor(odLed[2]);
//        this.addSensor(odLed[3]);
//        this.addSensor(odLed[4]);
        this.addSensor(osMap);
        this.addSensor(olGPS);
        this.addSensor(olTime);
        this.addSensor(olSteps);
        this.addSensor(olBurnt);
        this.addSensor(olPayload);
        this.addSensor(olCommand);
        this.addSensor(topLabels);
        this.addSensor(orCompass1);
        this.addSensor(osAltitude);
        this.addSensor(osGround);
        this.addSensor(osBattery);
        myParent.validate();

    }

    public void clear() {
        osMap.clearTrail();
        osMap.clearTrail();
        tstart = new TimeHandler();
        for (String s : this.mySensorsVisual.keySet()) {
            this.mySensorsVisual.get(s).clear();
        }
        this.olCommand.clear();
        this.olPayload.clear();
    }

    public boolean preProcessACLM(String content) {
        boolean res = false;
//        System.out.println("DashBoard Preprocess");

        if (content.contains("filedata")) {
//            System.out.println("filedata");
            Ole ocontent = new Ole().set(content);
            OleFile ofile = new OleFile(ocontent.getOle("surface"));
            int maxlevel = ocontent.getInt("maxflight");
            this.clear();
            decoder.setWorldMap(ofile.toString(), maxlevel);
            osMap.setMap(decoder.getWorldMap());
            osMap.validate();
            this.osAltitude.setAlertLimitAbove(maxlevel);
            osHud.resetTerrain();
            availableDashBoard = false;
            res = true;
        } else if (content.contains("perceptions")) {
//            System.out.println("DashBoard perceptions");
            availableDashBoard = true;
            this.feedPerception(content);
            res = false;
        } else if (content.contains("cities")) {
           decoder.setExternalThings(content);
        }else if (content.contains("people")) {
           decoder.setExternalThings(content);
        }
        this.repaint();

        return res;
    }

    public void feedPerception(String perception) {
        String stepbystep = "";

        try {
            decoder.feedPerception(perception);
//            System.out.println(decoder.printStatus(" Dashboard of " + decoder.getName()));
            if (decoder.getAlive()) {
                cLabels = SwingTools.doDarker(Color.WHITE);
            } else {
                cLabels = SwingTools.doDarker(Color.RED);
            }
//            System.out.println("Processed : "+decoder.getNSteps());
//            String[] trace = decoder.getTrace();
//            if (trace != null) { //&& iIter > trace.length && trace.length>0) {
//                for (; iTrace < trace.length; iTrace++) {
//                    this.addAction(trace[iTrace]);
//                }
//            }
//            if (!decoder.getName().equals(this.agentName))
//                    return;
            if (!decoder.getName().equals("")) {
                if (!topLabels.containsLabel("Name")) {
                    topLabels.addLabel("Name", decoder.getName());
                }
            }
            if (!decoder.getSessionid().equals("")) {
                if (!topLabels.containsLabel("SSID")) {
                    topLabels.addLabel("SSID", decoder.getSessionid());
                }
            }
//            osAltitude.setAlertLimitAbove(decoder.getMaxlevel());
            olGPS.setCurrentValue(decoder.getGPS().toArray());
            orCompass1.setCurrentValue(decoder.getCompass());
//            orCompass1.setHidden(this.mySensorsVisual.get("AUX").isMap);
            osGround.setCurrentValue(decoder.getGround());
//            osGround.setHidden(this.mySensorsVisual.get("AUX").isMap);
            osAltitude.setCurrentValue(decoder.getGPS().getZInt());
            if (osAltitude.getMaxValue()!= (decoder.getMaxlevel())){
                osAltitude.setMinValue(decoder.getMinlevel());
                osAltitude.setMaxValue(decoder.getMaxlevel());
                osAltitude.setAlertLimitAbove(decoder.getMaxlevel()-10);             
            }
//            osAltitude.setHidden(this.mySensorsVisual.get("AUX").isMap);
            osBattery.setCurrentValue(decoder.getEnergy());
            if (osBattery.getMaxValue() != decoder.getAutonomy()) {
                osBattery.setMaxValue(decoder.getAutonomy());
                osBattery.setAlertLimitBelow((int) (osBattery.getMaxValue() / 15));
                pal = new Palette();
                pal.addWayPoint(0, Color.RED);
                pal.addWayPoint(20, Color.RED);
                pal.addWayPoint(21, Color.YELLOW);
                pal.addWayPoint(75, Color.YELLOW);
                pal.addWayPoint(76, Color.YELLOW);
                pal.addWayPoint(100, Color.GREEN);
                pal.fillWayPoints(decoder.getAutonomy());
                osBattery.setPalette(pal);
            }
//            osBattery.setHidden(this.mySensorsVisual.get("AUX").isMap);

            odLed[0].setCurrentValue(decoder.getAlive());
            odLed[1].setCurrentValue(decoder.getOntarget());
            odLed[2].setCurrentValue(decoder.getCargo().length > 0);
            olBurnt.getAllReadings()[0][0] = decoder.getEnergyburnt();
            olSteps.setCurrentValue(decoder.getNSteps());
            olTime.setCurrentValue(tstart.elapsedTimeSecs(new TimeHandler()));

            String newBag[] = decoder.getTrace();
            if (newBag.length > olCommand.getBagSize()) {
                for (int i = olCommand.getBagSize(); i < newBag.length; i++) {
                    olCommand.addToBag(String.format("%03d ", this.mySensorsVisual.get("COMMAND").getBagSize()) + newBag[i]);
                }
            }
//            if (decoder.getStatus().length() > 0) {
//                olCommand.addToBag(emojis.INFO + " " + decoder.getStatus());
//            }
//            osAltitude.setAlertLimitAbove(decoder.getMaxlevel() - 10);

            String newCargo[] = decoder.getCargo();
            if (newCargo.length > olPayload.getBag().size()) {
                for (int i = olPayload.getBagSize(); i < newCargo.length; i++) {
                    olPayload.addToBag(String.format("%03d ", this.mySensorsVisual.get("PAYLOAD").getBagSize()) + newCargo[i]);
                }
            }
            osMap.addTrail(decoder.getName(), decoder.getGPSVector());

            SimpleVector3D me = decoder.getGPSVector();
            PolarSurface ps = new PolarSurface(me);
            ps.setRadius(15);
            Map2DColor sensor = ps.applyPolarTo(decoder.getWorldMap());
            lastPerception = perception;
        } catch (Exception ex) {
            System.err.println("Error processing perceptions " + ex.toString() + "\ndata: " + perception);
            ex.printStackTrace(System.out);
            System.exit(1);
        }
    }

    public void feedGoals(String goals) {
//        try {
//            JsonObject jso = Json.parse(goals).asObject();
//            mySensorsVisual.get("MAP").setJsaGoals(jso.get("goals").asArray());
//            mySensorsVisual.get("AUX").setJsaGoals(jso.get("goals").asArray());
//            decoder.findCourseTo(new Point3D(10,0,decoder.getAltitude()));
//        } catch (Exception ex) {
//
//        }
    }

    public boolean isShowTrail() {
        return showTrail;
    }

    public void setShowTrail(boolean showTrail) {
        this.showTrail = showTrail;
        this.osMap.setShowTrail(showTrail);
    }

    public int getTrailSize() {
        return trailSize;
    }

    public void setTrailSize(int trailSize) {
        this.trailSize = trailSize;
        this.osMap.setTrailSize(trailSize);
    }

}
