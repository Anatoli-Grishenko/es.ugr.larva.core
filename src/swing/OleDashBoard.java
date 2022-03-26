/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import data.Ole;
import data.OleFile;
import geometry.OleBag;
import geometry.OleDiode;
import geometry.OleLinear;
import geometry.OleMap;
import geometry.OleRotatory;
import geometry.OleRoundPB;
import geometry.OleSemiDial;
import geometry.Point3D;
import jade.lang.acl.ACLMessage;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.util.HashMap;
import map2D.Palette;
import world.SensorDecoder;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleDashBoard extends OleDrawPane {

    public final Color cDeck = Color.GRAY, cFrame = Color.DARK_GRAY, cGauge = new Color(0, 15, 0),
            cGoal = Color.YELLOW, cPath = Color.PINK, cCompass = new Color(0, 75, 0), cLabels = SwingTools.doDarker(Color.WHITE);

    protected HashMap<String, OleSensor> mySensorsVisual, myExternalSensor;
    protected Component myParent;
    protected SensorDecoder decoder;
    OleSemiDial osAltitude, osBattery;
    OleRoundPB osGround;
    OleRotatory orCompass1, osHud;
    OleDiode odLed[] = new OleDiode[10];
    OleLinear olTime, olSteps, olGPS;
    OleBag olPayload, olCommand;
    OleMap osMap;
    Palette pal;
    String sperception="", agentName, otherAgents;
    int iTrace, lastx, lasty;
    double arrayReading[], gps[];
    int iVisual[][], iLidar[][], iThermal[][];

    public OleDashBoard(Component parent, String nameagent) {
        myParent = parent;
        mySensorsVisual = new HashMap();
        myExternalSensor = new HashMap();
        decoder = new SensorDecoder();
        this.setLayout((LayoutManager) null);
        agentName = nameagent;
        initLayout();
    }

    @Override
    public void OleDraw(Graphics2D g) {
        myg = g;

//        myg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (String s : mySensorsVisual.keySet()) {
            mySensorsVisual.get(s).viewSensor(g);
        }
//        System.out.println("Repaint");
    }

    public void addSensor(OleSensor oles) {
        mySensorsVisual.put(oles.getName(), oles);
        mySensorsVisual.put(oles.getExternalSensor(), oles);
    }

    public void initLayout() {
        int yy = 0, xx = 0, ww = 150, ww2 = 50, hh = 25;

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

//        osGround = new OleRoundPB(this,  "Ground");
//        osGround.showFrame(true);
//        osGround.setMinValue(0);
//        osGround.setMaxValue(275);
//        osGround.setStartAngle(225);
//        osGround.setEndAngle(-45);
//        osGround.setnDivisions(11);
//        osGround.setBounds(0, 200, 100, 100);
//        osGround.setForeground(Color.WHITE);
//        osGround.setBackground(Color.BLACK);
//        pal = new Palette();
//        pal.addWayPoint(0, Color.GREEN);
//        pal.addWayPoint(70, Color.YELLOW);
//        pal.addWayPoint(100, Color.red);
//        pal.fillWayPoints(275);
//        osGround.setPalette(pal);
//        osGround.showScale(true);
//        osGround.showScaleNumbers(false);
//        osGround.validate();

        osMap = new OleMap(this, "MAP");
        osMap.setBounds(ww, 00, 4 * ww, 4 * ww);
        osMap.setForeground(Color.WHITE);
        osMap.setBackground(Color.BLACK);
        osMap.showFrame(true);
        osMap.validate();

        olGPS = new OleLinear(this, "GPS");
        olGPS.setForeground(Color.WHITE);
        olGPS.setBackground(Color.DARK_GRAY);
        olGPS.setnColumns(2);
        olGPS.getAllReadings()[0][0] = 100;
        olGPS.getAllReadings()[0][1] = 121;
        olGPS.setBounds(5 * ww, 00, 2 * ww2, 4 * hh);
        olGPS.showFrame(true);
        olGPS.validate();

        olTime = new OleLinear(this, "TIME");
        olTime.setForeground(Color.WHITE);
        olTime.setBackground(Color.DARK_GRAY);
        olTime.setnColumns(1);
        olTime.getAllReadings()[0][0] = 123;
        olTime.setBounds(5 * ww, 4 * hh, 2 * ww2, 3 * hh);
        olTime.showFrame(true);
        olTime.validate();

        olSteps = new OleLinear(this, "STEPS");
        olSteps.setForeground(Color.WHITE);
        olSteps.setBackground(Color.DARK_GRAY);
        olSteps.setnColumns(1);
        olSteps.getAllReadings()[0][0] = 123;
        olSteps.setBounds(5 * ww, 7 * hh, 2 * ww2, 3 * hh);
        olSteps.showFrame(true);
        olSteps.validate();

        xx = 5 * ww;
        yy = 10 * hh;
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
        xx+=2*ww2;
        yy=0;
        olCommand = new OleBag(this, "COMMAND");
        olCommand.setForeground(Color.GREEN);
        olCommand.setBackground(Color.DARK_GRAY);
        olCommand.setBounds(xx, yy, 3 * ww2, 24 * hh);
        olCommand.showFrame(true);
        olCommand.validate();

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
        this.addSensor(olGPS);
        this.addSensor(olTime);
        this.addSensor(olSteps);
        this.addSensor(olPayload);
        this.addSensor(olCommand);
        
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
        if (content.contains("filedata")) {
            Ole ocontent = new Ole().set(content);
            OleFile ofile = new OleFile(ocontent.getOle("surface"));
            int maxlevel = ocontent.getInt("maxflight");
            decoder.setWorldMap(ofile.toString(), maxlevel);
            this.mySensorsVisual.get("MAP").setMap(decoder.getWorldMap());
            this.mySensorsVisual.get("MAP").validate();
            this.repaint();            
            res = true;
        }
        if (content.contains("perceptions")) {
            this.feedPerception(content);
            res = false;
        }
        return res;
    }

    public void feedPerception(String perception) {
        if (!perception.equals(sperception)) {
            feedPerceptionLocal(perception);
            sperception = perception;
        }
    }

    protected void feedPerceptionLocal(String perception) {
        String stepbystep = "";
        try {
            decoder.feedPerception(perception);
//            System.out.println("Processed : "+decoder.getNSteps());
//            String[] trace = decoder.getTrace();
//            if (trace != null) { //&& iIter > trace.length && trace.length>0) {
//                for (; iTrace < trace.length; iTrace++) {
//                    this.addAction(trace[iTrace]);
//                }
//            }
//            if (!decoder.getName().equals(this.agentName))
//                    return;
            this.mySensorsVisual.get("GPS").setCurrentValue(decoder.getGPS());
            this.mySensorsVisual.get("Compass").setCurrentValue(decoder.getCompass());
            this.mySensorsVisual.get("Ground").setCurrentValue(decoder.getGround());
            this.mySensorsVisual.get("Altitude").setCurrentValue(decoder.getGPS()[2]);
            this.mySensorsVisual.get("Energy").setCurrentValue(decoder.getEnergy());
            this.mySensorsVisual.get("ALV").setCurrentValue(decoder.getAlive());
            this.mySensorsVisual.get("TAR").setCurrentValue(decoder.getOnTarget());
            this.mySensorsVisual.get("STEPS").setCurrentValue(decoder.getNSteps());
            this.mySensorsVisual.get("MAP").setCurrentValue(decoder.getCompass());
            this.mySensorsVisual.get("MAP").getAllReadings()[0][1]=decoder.getAngular();
            this.mySensorsVisual.get("MAP").getAllReadings()[0][2]=decoder.getDistance();
            this.mySensorsVisual.get("COMMAND").addToBag(String.format("%03d ", this.mySensorsVisual.get("COMMAND").getBagSize())+decoder.getLastTrace());
            
            ((OleMap) this.mySensorsVisual.get("MAP")).addTrail(decoder.getName(), new Point3D(decoder.getGPS()[0], decoder.getGPS()[1]));

            addStatus(decoder.getStatus());
            this.repaint();
        } catch (Exception ex) {
            System.err.println("Error processing perceptions " + ex.toString()+"\ndata: "+perception);
            System.exit(1);
        }
    }
}
