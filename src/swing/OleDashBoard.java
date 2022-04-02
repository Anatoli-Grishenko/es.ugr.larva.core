/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import data.Ole;
import data.OleFile;
import geometry.OleBag;
import geometry.OleDiode;
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
import world.SensorDecoder;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleDashBoard extends OleDrawPane {

    public Color cDeck = Color.GRAY, cFrame = Color.DARK_GRAY, cGauge = new Color(0, 0, 0),
            cGoal = Color.YELLOW, cPath = Color.PINK, cCompass = new Color(0, 75, 0), cLabels = SwingTools.doDarker(Color.WHITE);

    public HashMap<String, OleSensor> mySensorsVisual, myExternalSensor;
    public ArrayList<String> layoutSensors;

    protected Component myParent;
    public SensorDecoder decoder;
    OleSemiDial osAltitude, osBattery;
    OleRoundPB osGround;
    OleRotatory orCompass1, osHud;
    OleDiode odLed[] = new OleDiode[10];
    OleLinear olTime, olSteps, olGPS,olBurnt;
    OleBag olPayload, olCommand;
    OleMap osMap, osMap2;
    OleLabels topLabels;
    Palette pal;
    String sperception = "", agentName, otherAgents;
    int iTrace, lastx, lasty;
    double arrayReading[], gps[];
    int iVisual[][], iLidar[][], iThermal[][];
    TimeHandler tstart;

    public OleDashBoard(Component parent, String nameagent) {
        myParent = parent;
        mySensorsVisual = new HashMap();
        myExternalSensor = new HashMap();
        layoutSensors = new ArrayList();
        decoder = new SensorDecoder();
        this.setLayout((LayoutManager) null);
        agentName = nameagent;
        initLayout();
    }

    @Override
    public void OleDraw(Graphics2D g) {
        myg = g;

//        myg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (String s : layoutSensors) {
            mySensorsVisual.get(s).viewSensor(g);
        }
//        System.out.println("Repaint");
    }

    public void addSensor(OleSensor oles) {
        mySensorsVisual.put(oles.getName(), oles);
        mySensorsVisual.put(oles.getExternalSensor(), oles);
        layoutSensors.add(oles.getName());
    }

    public void initLayout() {
        Layout2();
    }

    protected void Layout2() {
        int hLabels = 50, yy = hLabels, xx = 0, ww = 150, ww2 = 50, hh = 25;

        osMap = new OleMap(this, "MAP");
        osMap.setBounds(0 * ww, hLabels, 4 * ww, 4 * ww);
        osMap.setForeground(Color.WHITE);
        osMap.setBackground(Color.BLACK);
        osMap.showFrame(true);
        osMap.setIsMap(true);
        osMap.validate();

        osMap2 = new OleMap(this, "AUX");
        osMap2.setBounds(4 * ww, hLabels, 4 * ww, 4 * ww);
        osMap2.setForeground(Color.WHITE);
        osMap2.setBackground(Color.BLACK);
        osMap2.showFrame(true);
        osMap2.setIsMap(false);
        osMap2.validate();

        orCompass1 = new OleRotatory(this, "Compass");
        orCompass1.setMinValue(0);
        orCompass1.setMaxValue(360);
        orCompass1.setMinVisual(0);
        orCompass1.setMaxVisual(360);
        orCompass1.setnDivisions(8);
        orCompass1.setLabels(new String[]{"N", "NW", "W", "SW", "S", "SE", "E", "NE"});
        orCompass1.showScaleNumbers(true);
        orCompass1.setBounds(20 + 4 * ww, hLabels + 4 * ww - 125, 125, 125);
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
        osAltitude.setBounds(145 + 4 * ww, hLabels + 4 * ww - 125, 125, 125);
        osAltitude.setForeground(this.cLabels);
        osAltitude.setBackground(this.cGauge); //SwingTools.doDarker(Color.DARK_GRAY));
        pal = new Palette();
        pal.addWayPoint(0, Color.BLACK);
        pal.addWayPoint(80, this.cGauge);
        pal.addWayPoint(100, Color.RED);
        pal.fillWayPoints(255);
        osAltitude.setPalette(pal);
        osAltitude.showScale(true);
        osAltitude.showScaleNumbers(true);
        osAltitude.setAlertLimitAbove(240);
//        osAltitude.showFrame(true);
        osAltitude.validate();

        osGround = new OleRoundPB(this, "Ground");
        osGround.setMinValue(0);
        osGround.setMaxValue(275);
        osGround.setStartAngle(225);
        osGround.setEndAngle(-45);
        osGround.setnDivisions(11);
        osGround.setBounds(265 + 4 * ww, hLabels + 4 * ww - 125, 125, 125);
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
        osBattery.setBounds(385 + 4 * ww, hLabels + 4 * ww - 125, 125, 125);
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

        this.addSensor(osMap2);
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

    protected void Layout1() {
        int hLabels = 50, yy = hLabels, xx = 0, ww = 150, ww2 = 50, hh = 25;

        orCompass1 = new OleRotatory(this, "Compass");
        orCompass1.setMinValue(0);
        orCompass1.setMaxValue(360);
        orCompass1.setMinVisual(0);
        orCompass1.setMaxVisual(360);
        orCompass1.setnDivisions(8);
        orCompass1.setLabels(new String[]{"N", "NW", "W", "SW", "S", "SE", "E", "NE"});
        orCompass1.showScaleNumbers(true);
        orCompass1.setBounds(0, yy, ww, ww);
        orCompass1.setForeground(this.cLabels);
        orCompass1.setBackground(this.cGauge);
        orCompass1.showFrame(true);
        orCompass1.setAutoRotate(false);
        orCompass1.validate();
        yy += ww;

        osAltitude = new OleSemiDial(this, "Altitude");
        osAltitude.setMinValue(0);
        osAltitude.setMaxValue(255);
        osAltitude.setStartAngle(225);
        osAltitude.setEndAngle(-45);
        osAltitude.setnDivisions(13);
        osAltitude.setBounds(0, yy, ww, ww);
        osAltitude.setForeground(this.cLabels);
        osAltitude.setBackground(this.cGauge); //SwingTools.doDarker(Color.DARK_GRAY));
        pal = new Palette();
        pal.addWayPoint(0, Color.BLACK);
        pal.addWayPoint(80, this.cGauge);
        pal.addWayPoint(100, Color.RED);
        pal.fillWayPoints(275);
        osAltitude.setPalette(pal);
        osAltitude.showScale(true);
        osAltitude.showScaleNumbers(true);
        osAltitude.showFrame(true);
        osAltitude.validate();
        yy += ww;

        osGround = new OleRoundPB(this, "Ground");
        osGround.setMinValue(0);
        osGround.setMaxValue(275);
        osGround.setStartAngle(225);
        osGround.setEndAngle(-45);
        osGround.setnDivisions(11);
        osGround.setBounds(0, yy, ww, ww);
        osGround.setForeground(Color.WHITE);
        osGround.setBackground(Color.BLACK);
        pal = new Palette();
        pal.addWayPoint(0, Color.RED);
        pal.addWayPoint(20, Color.WHITE);
        pal.addWayPoint(100, Color.WHITE);
        pal.fillWayPoints(275);
        osGround.setPalette(pal);
        osGround.showScale(true);
        osGround.showScaleNumbers(false);
        osGround.showFrame(true);
        osGround.validate();
        yy += ww;

        osBattery = new OleSemiDial(this, "Energy");
        osBattery.setMinValue(0);
        osBattery.setMaxValue(3500);
        osBattery.setStartAngle(180);
        osBattery.setEndAngle(0);
        osBattery.setnDivisions(5);
        osBattery.setBounds(0, yy, ww, ww);
        osBattery.setForeground(Color.BLACK);
        osBattery.setBackground(Color.WHITE);
        pal = new Palette();
        pal.addWayPoint(0, Color.RED);
        pal.addWayPoint(50, Color.YELLOW);
        pal.addWayPoint(100, Color.GREEN);
        pal.fillWayPoints(3500);
        osBattery.setPalette(pal);
        osBattery.showScale(true);
        osBattery.showScaleNumbers(true);
        osBattery.showFrame(true);
        osBattery.validate();

        osMap = new OleMap(this, "MAP");
        osMap.setBounds(ww, hLabels, 4 * ww, 4 * ww);
        osMap.setForeground(Color.WHITE);
        osMap.setBackground(Color.BLACK);
        osMap.showFrame(true);
        osMap.setIsMap(true);
        osMap.validate();

        osMap2 = new OleMap(this, "AUX");
        osMap2.setBounds(5 * ww, hLabels, 4 * ww, 4 * ww);
        osMap2.setForeground(Color.WHITE);
        osMap2.setBackground(Color.BLACK);
        osMap2.showFrame(true);
        osMap2.setIsMap(false);
        osMap2.validate();

        olGPS = new OleLinear(this, "GPS");
        olGPS.setForeground(Color.WHITE);
        olGPS.setBackground(Color.DARK_GRAY);
        olGPS.setnColumns(2);
        olGPS.getAllReadings()[0][0] = 100;
        olGPS.getAllReadings()[0][1] = 121;
        olGPS.setBounds(9 * ww, hLabels, 2 * ww2, 4 * hh);
        olGPS.showFrame(true);
        olGPS.validate();

        olTime = new OleLinear(this, "TIME");
        olTime.setForeground(Color.WHITE);
        olTime.setBackground(Color.DARK_GRAY);
        olTime.setnColumns(1);
        olTime.getAllReadings()[0][0] = 123;
        olTime.setBounds(9 * ww, 4 * hh + hLabels, 2 * ww2, 3 * hh);
        olTime.showFrame(true);
        olTime.validate();

        olSteps = new OleLinear(this, "STEPS");
        olSteps.setForeground(Color.WHITE);
        olSteps.setBackground(Color.DARK_GRAY);
        olSteps.setnColumns(1);
        olSteps.getAllReadings()[0][0] = 0;
        olSteps.setBounds(9 * ww, 7 * hh + hLabels, 2 * ww2, 3 * hh);
        olSteps.showFrame(true);
        olSteps.validate();

        xx = 9 * ww;
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

        odLed[3] = new OleDiode(this, "SND");
        odLed[3].setBounds(xx, yy, 2 * ww2, hh);
        odLed[3].setForeground(OleApplication.DodgerBlue);
        odLed[3].setBackground(Color.DARK_GRAY);
        odLed[3].showFrame(true);
        odLed[3].validate();
        yy += hh;

        odLed[4] = new OleDiode(this, "RCV");
        odLed[4].setBounds(xx, yy, 2 * ww2, hh);
        odLed[4].setForeground(OleApplication.Maroon);
        odLed[4].setBackground(Color.BLACK);
        odLed[4].showFrame(true);
        odLed[4].validate();
        yy += hh;

        olPayload = new OleBag(this, "PAYLOAD");
        olPayload.setForeground(Color.YELLOW);
        olPayload.setBackground(Color.DARK_GRAY);
        olPayload.setBounds(xx, yy, 2 * ww2, 9 * hh);
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
        topLabels.setBounds(0, 0, 1600, hLabels);
        topLabels.showFrame(true);
        topLabels.validate();

        this.addSensor(osAltitude);
        this.addSensor(osBattery);
        this.addSensor(osGround);
        this.addSensor(orCompass1);
        this.addSensor(odLed[0]);
        this.addSensor(odLed[1]);
        this.addSensor(odLed[2]);
        this.addSensor(odLed[3]);
        this.addSensor(odLed[4]);
        this.addSensor(osMap);
        this.addSensor(osMap2);
        this.addSensor(olGPS);
        this.addSensor(olTime);
        this.addSensor(olSteps);
        this.addSensor(olPayload);
        this.addSensor(olCommand);
        this.addSensor(topLabels);

    }

    protected void addStatus(String status) {
//        if (status.length() < 1) {
//            return;
//        }
//        tpLog.setBackground(cStatus);
//        StyleContext sc = StyleContext.getDefaultStyleContext();
//        AttributeSet aset1 = sc.addAttribute(SimpleAttributeSet.EMPTY,
//                StyleConstants.Foreground, cTextStatus), aset2 = sc.addAttribute(SimpleAttributeSet.EMPTY,
//                        StyleConstants.FontFamily, Font.SERIF);
//        AttributeSet aset = sc.addAttributes(aset1, aset2);
//        StyledDocument doc = tpLog.getStyledDocument();
//        try {
//            doc.insertString(doc.getLength(), "\n" + status + "\n", aset);
//        } catch (BadLocationException ex) {
//        }
//        tpLog.setCaretPosition(doc.getLength());

    }

    protected void addAction(String action) {
//        tpLog.setBackground(cStatus);
//        StyleContext sc = StyleContext.getDefaultStyleContext();
//        AttributeSet aset1 = sc.addAttribute(SimpleAttributeSet.EMPTY,
//                StyleConstants.Foreground, Color.WHITE), aset2 = sc.addAttribute(SimpleAttributeSet.EMPTY,
//                        StyleConstants.FontFamily, Font.SERIF);
//        AttributeSet aset = sc.addAttributes(aset1, aset2);
//        StyledDocument doc = tpLog.getStyledDocument();
//        try {
//            doc.insertString(doc.getLength(), (this.getNsteps()) + ". " + action + "\n", aset);
//        } catch (BadLocationException ex) {
//        }
//        tpLog.setCaretPosition(doc.getLength());
//        refresh();
    }

    public boolean preProcessACLM(String content) {
        boolean res = false;
        System.out.println("Preprocess");

        if (content.contains("filedata")) {
            System.out.println("filedata");
            Ole ocontent = new Ole().set(content);
            OleFile ofile = new OleFile(ocontent.getOle("surface"));
            int maxlevel = ocontent.getInt("maxflight");
            decoder.setWorldMap(ofile.toString(), maxlevel);
            this.mySensorsVisual.get("MAP").setMap(decoder.getWorldMap());
            this.mySensorsVisual.get("MAP").validate();
            this.mySensorsVisual.get("AUX").setMap(decoder.getWorldMap());
            this.mySensorsVisual.get("AUX").validate();
            ((OleMap) this.mySensorsVisual.get("MAP")).clearTrail();            
            this.repaint();
            tstart = new TimeHandler();
            res = true;
        }
        if (content.contains("perceptions")) {
            System.out.println("perceptions");
            this.feedPerception(content);
            res = false;
        }
        if (content.contains("goals")) {
            System.out.println("goals");
            this.feedGoals(content);
            res = false;
        }
        return res;
    }

    public void feedPerception(String perception) {
        String stepbystep = "";
        try {
            decoder.feedPerception(perception);
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
            if (!decoder.getSessionID().equals("")) {
                if (!topLabels.containsLabel("SSID")) {
                    topLabels.addLabel("SSID", decoder.getSessionID());
                }
            }
            this.mySensorsVisual.get("GPS").setCurrentValue(decoder.getGPS());
            this.mySensorsVisual.get("Compass").setCurrentValue(decoder.getCompass());
            this.mySensorsVisual.get("Compass").setHidden(this.mySensorsVisual.get("AUX").isMap);
            this.mySensorsVisual.get("Ground").setCurrentValue(decoder.getGround());
            this.mySensorsVisual.get("Ground").setHidden(this.mySensorsVisual.get("AUX").isMap);
            this.mySensorsVisual.get("Altitude").setCurrentValue(decoder.getGPS()[2]);
            this.mySensorsVisual.get("Altitude").setHidden(this.mySensorsVisual.get("AUX").isMap);
            this.mySensorsVisual.get("Energy").setCurrentValue(decoder.getEnergy());
            this.mySensorsVisual.get("Energy").setHidden(this.mySensorsVisual.get("AUX").isMap);
            this.mySensorsVisual.get("ALV").setCurrentValue(decoder.getAlive());
            this.mySensorsVisual.get("TAR").setCurrentValue(decoder.getOnTarget());
            this.mySensorsVisual.get("PAY").setCurrentValue(decoder.getCargo().length > 0);
            this.mySensorsVisual.get("BURNT").getAllReadings()[0][0] = decoder.getEnergyBurnt();
            this.mySensorsVisual.get("STEPS").getAllReadings()[0][0] = decoder.getNSteps();
            this.mySensorsVisual.get("TIME").getAllReadings()[0][0] = tstart.elapsedTimeSecs(new TimeHandler());
            this.mySensorsVisual.get("COMMAND").addToBag(String.format("%03d ", this.mySensorsVisual.get("COMMAND").getBagSize()) + decoder.getLastTrace());
            if (mySensorsVisual.get("PAYLOAD").getBag().size() < decoder.getCargo().length) {
                this.mySensorsVisual.get("PAYLOAD").addToBag(String.format("%03d  %s", decoder.getCargo().length, decoder.getCargo()[decoder.getCargo().length-1]));
            }
            this.mySensorsVisual.get("MAP").setCurrentValue(decoder.getCompass());
            this.mySensorsVisual.get("MAP").getAllReadings()[0][1] = decoder.getAngular();
            this.mySensorsVisual.get("MAP").getAllReadings()[0][2] = decoder.getDistance();
            ((OleMap) this.mySensorsVisual.get("MAP")).addTrail(decoder.getName(), decoder.getGPSVector());

            this.mySensorsVisual.get("AUX").setCurrentValue(decoder.getCompass());
            this.mySensorsVisual.get("AUX").getAllReadings()[0][1] = decoder.getAngular();
            this.mySensorsVisual.get("AUX").getAllReadings()[0][2] = decoder.getDistance();
//            ((OleMap) this.mySensorsVisual.get("AUX")).addTrail(decoder.getName(), new Point3D(decoder.getGPS()[0], decoder.getGPS()[1]));

            SimpleVector3D me = new SimpleVector3D((int) decoder.getGPS()[0],
                    (int) decoder.getGPS()[1], (int) (decoder.getCompass()) / 45);

//            System.out.println("Compass: " + decoder.getCompass());
//            System.out.println("Orientation: " + decoder.getCompass() / 45);
//            System.out.println("Angular: " + decoder.getAngular());
//            System.out.println("XY: " + decoder.getGPS()[0]+", "+decoder.getGPS()[1]);
            PolarSurface ps = new PolarSurface(me);
            ps.setRadius(15);
            Map2DColor sensor = ps.applyPolarTo(decoder.getWorldMap());
            mySensorsVisual.get("MAP").setImage1(sensor);
            mySensorsVisual.get("AUX").setImage1(sensor);
            addStatus(decoder.getStatus());
            this.repaint();
        } catch (Exception ex) {
            System.err.println("Error processing perceptions " + ex.toString() + "\ndata: " + perception);
            ex.printStackTrace(System.out);
            System.exit(1);
        }
    }

    public void feedGoals(String goals) {
        try {
            JsonObject jso = Json.parse(goals).asObject();
            mySensorsVisual.get("MAP").setJsaGoals(jso.get("goals").asArray());
            mySensorsVisual.get("AUX").setJsaGoals(jso.get("goals").asArray());
        } catch (Exception ex) {

        }
    }
}
