/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import Environment.Environment;
import agents.LARVAFirstAgent;
import JsonObject.Json;
import JsonObject.JsonObject;
import data.Ole;
import data.OleFile;
import geometry.OleBag;
import geometry.OleDiode;
import geometry.OleHud;
import geometry.OleLabels;
import geometry.OleLinear;
import geometry.OleSuperMap;
import geometry.OleRotatory;
import geometry.OleRoundPB;
import geometry.OleSemiDial;
import glossary.Sensors;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import map2D.Palette;
import profiling.Profiler;
import static swing.SwingTools.doSwingLater;
import static swing.SwingTools.doSwingWait;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleDashBoard extends OleDrawPane implements MouseListener {

    public static Color cDeck = Color.GRAY, cFrame = Color.DARK_GRAY, cGauge = new Color(0, 0, 0),
            cDial = SwingTools.doDarker(Color.WHITE), cBad = new Color(50, 0, 0),
            cDistance = SwingTools.doDarker(Color.MAGENTA), cAngle = SwingTools.doDarker(Color.CYAN),
            cGround = new Color(51, 25, 0),
            cGoal = Color.YELLOW, cTrack = new Color(0, 153, 0), cCompass = Color.WHITE, cLabels = SwingTools.doDarker(Color.WHITE),
            cSea = new Color(0, 0, 41);

    public HashMap<String, OleSensor> mySensorsVisual, myExternalSensor;
    public ArrayList<String> layoutSensors;
    public String agentOwner;

    protected Component myParent;
    public HashMap<String, Environment> decoderSet;
    OleSemiDial osAltitude, osBattery;
    OleSensor osGround;
    OleRotatory orCompass1;
    OleDiode odLed[] = new OleDiode[10];
    OleLinear olTime, olSteps, olGPS, olBurnt;
    OleBag olPayload, olCommand;
    OleSuperMap osMap;
    OleHud osHud;
    OleLabels topLabels;
    TelegramBackdoor olTele;
    Palette pal;
    String sperception = "", agentName, otherAgents;
    int iTrace, lastx, lasty;
    double arrayReading[], gps[];
    int iVisual[][], iLidar[][], iThermal[][];
    String lastPerception = "";
    TimeHandler tstart;
    public boolean showTrail, availableDashBoard, verbose = false;
    int trailSize;
    OleFrame of;
    int ndash, nprepr;
    LARVAFirstAgent myXUIAgent;
    public Profiler refProfiler;
    String newBag[];
    int max = 30;
    ArrayList<String> monitorSensors = new ArrayList();

    public OleDashBoard(Component parent, String nameagent) {
        myParent = parent;
        mySensorsVisual = new HashMap();
        myExternalSensor = new HashMap();
        layoutSensors = new ArrayList();
        decoderSet = new HashMap();
        this.setLayout((LayoutManager) null);
        agentName = nameagent;
        agentOwner = "";
        availableDashBoard = false;
        initLayout();
        this.addMouseListener(this);
        refProfiler = new Profiler();
    }

    public void closeProfiler() {
        if (refProfiler.isActive()) {
            refProfiler.close();
        }
    }
//    public void purge() {
//        TimeHandler thNow = new TimeHandler();
//        ArrayList<String> purge = new ArrayList();
//        for (String sname : this.decoderSet.keySet()) {
//            if (decoderSet.get(sname).getLastRead().elapsedTimeSecsUntil(thNow) > 10) {
//                purge.add(sname);
//            }
//        }
//        for (String s : purge) {
//            decoderSet.remove(s);
//        }
//    }
    public Environment getMyDecoder() {
        return decoderSet.get(agentOwner);
    }

    public Environment getDecoderOf(String name) {
        return decoderSet.get(name);
    }

    @Override
    public void OleDraw(Graphics2D g) {
        myg = g;

//        monitorSensors.clear();
//        myg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        refProfiler.profileThis(""+this.myXUIAgent.getNCycles(), "DRAWING", () -> {
            doSwingLater(() -> {
                for (String s : layoutSensors) {
                    if (availableDashBoard) {
                        try {
                            if (mySensorsVisual.get(s) != null) {
//                                if (monitorSensors.contains(s)) {
                                    refProfiler.profileThis(s, () -> {
                                        mySensorsVisual.get(s).viewSensor(g);
                                    });
//                                } else {
//                                    mySensorsVisual.get(s).viewSensor(g);
//                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("Exception reading sensor " + s + " " + ex.toString());
                        }
                    }
                }
            });
        });
//        if (getMyDecoder().getVisualData().length > 0) {
//            Map2DColor m;
//            int zoom = 4;
//            g.setColor(Color.WHITE);
//            m = getMyDecoder().getFullZenitalVisual();
//            g.drawImage(m.getMap(), 0, 700, m.getWidth() * 3, m.getHeight() * 3, null);
//            g.drawRect(0, 700, m.getWidth() * 3, m.getHeight() * 3);
//            m = getMyDecoder().getRelativeVisual();
//            g.drawImage(m.getMap(), 200, 700, m.getWidth() * 4, m.getHeight() * 4, null);
//            g.drawRect(200, 700, m.getWidth() * 4, m.getHeight() * 4);
//            m = getMyDecoder().getRelativeVisual();
//            g.drawImage(m.getMap(), 400, 700, m.getWidth() * 4, m.getHeight() * 4, null);
//            g.drawRect(400, 700, m.getWidth() * 4, m.getHeight() * 4);
//            m = getMyDecoder().getPolarVisual();
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
        Layout4();
        this.validate();
        this.repaint();
    }

    protected void Layout4() {
        int hLabels = 0, yy = hLabels, xx = 0, ww = 150, ww2 = 50, hh = 25, ldials = 110, skipdials = ldials + 25; //(int) (5*ww/4);

        osMap = new OleSuperMap(this, "MAP");
        osMap.setBounds(0 * ww, hLabels, 5 * ww, 5 * ww);
        osMap.setForeground(Color.WHITE);
        osMap.setBackground(Color.BLACK);
        osMap.showFrame(true);
        osMap.validate();
        monitorSensors.add(osMap.getName());

        osHud = new OleHud(this, "HUD");
        osHud.setBounds(5 * ww, hLabels, 5 * ww, 5 * ww);
        osHud.setForeground(Color.WHITE);
        osHud.setBackground(Color.BLACK);
        osHud.setSPStretch(0);
        osHud.setVPStretch(ldials / 2);
        osHud.showFrame(true);
        osHud.validate();
        monitorSensors.add(osHud.getName());

        orCompass1 = new OleRotatory(this, "Compass");
        orCompass1.setMinValue(0);
        orCompass1.setCurrentValue(0.0);
        orCompass1.setMaxValue(360);
        orCompass1.setMinVisual(0);
        orCompass1.setMaxVisual(360);
        orCompass1.setnDivisions(8);
        orCompass1.setLabels(new String[]{"N", "NW", "W", "SW", "S", "SE", "E", "NE"});
        orCompass1.showScaleNumbers(true);
        orCompass1.setBounds(5 * ww, 20 + hLabels + 0 * skipdials, ldials, ldials);
        orCompass1.setForeground(this.cLabels);
        orCompass1.setBackground(this.cGauge);
//        orCompass1.showFrame(true);
        orCompass1.setAutoRotate(false);
        orCompass1.validate();
        monitorSensors.add(orCompass1.getName());

        osAltitude = new OleSemiDial(this, "Altitude");
        osAltitude.setMinValue(0);
        osAltitude.setCurrentValue(0.0);
        osAltitude.setMaxValue(255);
        osAltitude.setStartAngle(225);
        osAltitude.setEndAngle(0);
        osAltitude.setnDivisions(10);
        osAltitude.setBounds(5 * ww, 20 + hLabels + 1 * skipdials, ldials, ldials);
        osAltitude.setForeground(this.cLabels);
        osAltitude.setBackground(Color.BLACK); //SwingTools.doDarker(Color.DARK_GRAY));
        osAltitude.setSimplifiedDial(true);
//        pal = new Palette();
//        pal.addWayPoint(0, Color.WHITE);
//        pal.addWayPoint(100, Color.RED);
//        pal.fillWayPointsPerc(255);
//        osAltitude.setPalette(pal);
//        osAltitude.showScale(true);
//        osAltitude.showScaleNumbers(true);
//        osAltitude.showFrame(true);
        osAltitude.validate();

        osGround = new OleSemiDial(this, "Ground");
        osGround.setMinValue(0);
        osGround.setCurrentValue(0.0);
        osGround.setMaxValue(255);
        osGround.setStartAngle(225);
        osGround.setEndAngle(0);
        osGround.setnDivisions(10);
        osGround.setBounds(5 * ww, 20 + hLabels + 2 * skipdials, ldials, ldials);
        osGround.setForeground(this.cLabels);
        osGround.setBackground(this.cGauge); //SwingTools.doDarker(Color.DARK_GRAY));
        osGround.setSimplifiedDial(true);
//        pal = new Palette();
//        pal.addWayPoint(0, Color.RED);
//        pal.addWayPoint(20, Color.WHITE);
//        pal.addWayPoint(100, Color.WHITE);
//        pal.fillWayPointsPerc(275);
//        osGround.setPalette(pal);
//        osGround.showScale(true);
//        osGround.showScaleNumbers(false);
        osGround.setAlertLimitBelow(10);
        osGround.validate();

        osBattery = new OleSemiDial(this, "Energy");
        osBattery.setMinValue(0);
        osBattery.setCurrentValue(0.0);
        osBattery.setMaxValue(3500);
        osBattery.setStartAngle(180);
        osBattery.setEndAngle(0);
        osBattery.setnDivisions(4);
        osBattery.setBounds(5 * ww, 20 + hLabels + 3 * skipdials, ldials, ldials);
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
        pal.fillWayPointsPerc(3500);
        osBattery.setPalette(pal);
//        osBattery.showScale(true);
//        osBattery.showScaleNumbers(true);
        osBattery.validate();

        olGPS = new OleLinear(this, "GPS");
        olGPS.setBackground(Color.DARK_GRAY);
        olGPS.setnColumns(2);
        olGPS.getAllReadings()[0][0] = 100;
        olGPS.getAllReadings()[0][1] = 121;
        olGPS.setBounds(10 * ww, hLabels, 2 * ww2, 4 * hh);
        olGPS.showFrame(true);
        olGPS.setForeground(this.cLabels);
        olGPS.validate();

        olTime = new OleLinear(this, "TIME");
        olTime.setForeground(this.cLabels);
        olTime.setBackground(Color.DARK_GRAY);
        olTime.setnColumns(1);
        olTime.getAllReadings()[0][0] = 123;
        olTime.setBounds(10 * ww, 4 * hh + hLabels, 2 * ww2, 2 * hh);
        olTime.showFrame(true);
        olTime.validate();

        olSteps = new OleLinear(this, "STEPS");
        olSteps.setForeground(this.cLabels);
        olSteps.setBackground(Color.DARK_GRAY);
        olSteps.setnColumns(1);
        olSteps.setCurrentValue(0);
        olSteps.getAllReadings()[0][0] = 123;
        olSteps.setBounds(10 * ww, 6 * hh + hLabels, 2 * ww2, 2 * hh);
        olSteps.showFrame(true);
        olSteps.validate();

        olBurnt = new OleLinear(this, "BURNT");
        olBurnt.setForeground(this.cLabels);
        olBurnt.setBackground(Color.DARK_GRAY);
        olBurnt.setnColumns(1);
        olBurnt.setCurrentValue(0);
        olBurnt.getAllReadings()[0][0] = 123;
        olBurnt.setBounds(10 * ww, 8 * hh + hLabels, 2 * ww2, 2 * hh);
        olBurnt.showFrame(true);
        olBurnt.validate();

        xx = 10 * ww;
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
        olPayload.clear();
        olPayload.setBounds(xx, yy, 2 * ww2, 11 * hh);
        olPayload.showFrame(true);
        olPayload.validate();
        monitorSensors.add(olPayload.getName());

        xx += 2 * ww2;
        yy = hLabels;

        olCommand = new OleBag(this, "COMMAND");
        olCommand.setForeground(Color.GREEN);
        olCommand.setBackground(Color.DARK_GRAY);
        olCommand.setBounds(xx, yy, 3 * ww2, 24 * hh);
        olCommand.showFrame(true);
        olCommand.clear();
        olCommand.validate();
        monitorSensors.add(olCommand.getName());

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
//        this.olCommand.clear();
        this.olPayload.clear();
    }

    public void setMyXUIAgent(LARVAFirstAgent myXUIAgent) {
        this.myXUIAgent = myXUIAgent;
    }

    public boolean preProcessACLM(String content) {
        boolean res = false;
//        System.out.println("DashBoard Preprocess");

        if (content.contains("filedata")) {
            res = true;
            Ole ocontent = new Ole().set(content);
            agentName = ocontent.getString("owner", "");
            agentOwner = agentName;
            OleFile ofile = new OleFile(ocontent.getOle("surface"));
            int maxlevel = ocontent.getInt("maxflight");
            this.clear();
            decoderSet.clear();
            decoderSet.put(agentName, new Environment(this.myXUIAgent));
            decoderSet.get(agentName).setWorldMap(ofile.toString(), maxlevel);
            osMap.setMap(decoderSet.get(agentName).getWorldMap());
            this.osAltitude.setAlertLimitAbove(maxlevel);
            osHud.resetTerrain();
            availableDashBoard = false;
            ndash = 0;
            this.nprepr = 0;
            if (verbose) {
                System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>\n" + "Dashboard::" + agentName + " has received the map owned by " + ocontent.getString("owner", "unkwnown") + ". Fields " + ocontent.getFieldList());
            }
        } else if (content.contains("perceptions")) {
            if (verbose) {
                System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>\n" + "Dashboard::" + "DashBoard perceptions");
            }
            availableDashBoard = true;
//            System.out.println("ndash "+ndash++);
            this.feedPerception(content);
            res = false;
        } else if (content.contains("city")) {
            try {
                getMyDecoder().setExternalObjects(content);
            } catch (Exception ex) {
                System.out.println("");
            }
        } else if (content.contains("people")) {
            getMyDecoder().setExternalObjects(content);
        }
        this.repaint();

        return res;
    }

    public void feedPerception(String perception) {
        String stepbystep = "";
//        this.purge();
        try {
            JsonObject jsoperception = Json.parse(perception).asObject();
//            System.out.println(jsoperception.toString(WriterConfig.PRETTY_PRINT));
            agentName = jsoperception.getString("name", "");
//            System.out.println("Received perceptions of agent " + agentName);
            if (agentName.length() == 0) {
                return;
            }
//            refProfiler.profileThis("perceptions, " + agentName, "" + perception.length(),
//                    () -> {

            if (decoderSet.get(agentName) == null) {
                decoderSet.put(agentName, new Environment(this.myXUIAgent));
                decoderSet.get(agentName).verbose = this.verbose;
            }

            decoderSet.get(agentName).feedPerception(jsoperception);
            osMap.addTrail(agentName, decoderSet.get(agentName).getGPSVector());
            if (!agentName.equals(agentOwner)) {
                return;
            }
            if (getMyDecoder().getAlive()) {
                cLabels = SwingTools.doDarker(Color.WHITE);
            } else {
                cLabels = SwingTools.doDarker(Color.RED);
            }
//            if (getMyDecoder().getSessionid() != null) {
//                this.olCommand.setName(getMyDecoder().getSessionid());
//            } else {
//                this.olCommand.setName("COMMAND");
//            }
            olGPS.setCurrentValue(getMyDecoder().getGPS().toArray());
            orCompass1.setCurrentValue(getMyDecoder().getCompass());
            osGround.setCurrentValue(getMyDecoder().getGround());
            osAltitude.setCurrentValue(getMyDecoder().getGPS().getZInt());
            if (osAltitude.getMaxValue() != (getMyDecoder().getMaxlevel())) {
                osAltitude.setMinValue(getMyDecoder().getMinlevel());
                osAltitude.setMaxValue(getMyDecoder().getMaxlevel());
                osAltitude.setAlertLimitAbove(getMyDecoder().getMaxlevel() - 10);
            }
//            osAltitude.setHidden(this.mySensorsVisual.get("AUX").isMap);
            osBattery.setCurrentValue(getMyDecoder().getEnergy());
            if (osBattery.getMaxValue() != getMyDecoder().getAutonomy()) {
                osBattery.setMaxValue(getMyDecoder().getAutonomy());
                osBattery.setAlertLimitBelow((int) (osBattery.getMaxValue() / 15));
                pal = new Palette();
                pal.addWayPoint(0, Color.RED);
                pal.addWayPoint(20, Color.RED);
                pal.addWayPoint(21, Color.YELLOW);
                pal.addWayPoint(75, Color.YELLOW);
                pal.addWayPoint(76, Color.YELLOW);
                pal.addWayPoint(100, Color.GREEN);
                pal.fillWayPointsPerc(getMyDecoder().getAutonomy());
                osBattery.setPalette(pal);
            }
            odLed[0].setCurrentValue(getMyDecoder().getAlive());
            odLed[1].setCurrentValue(getMyDecoder().getOntarget());
            odLed[2].setCurrentValue(getMyDecoder().getCargo().length > 0);
            olBurnt.getAllReadings()[0][0] = getMyDecoder().getEnergyburnt();
            olSteps.setCurrentValue(getMyDecoder().getNSteps());
            olTime.setCurrentValue(tstart.elapsedTimeSecsUntil(new TimeHandler()));
            osHud.setCurrentValue(-1); // Ficticious, just to update terrain
            newBag = getMyDecoder().getTrace();
            max = 30;
            olCommand.clear();
            try {
                olCommand.setDescription(getMyDecoder().getSensor(Sensors.CURRENTGOAL).get(0).asString());
            } catch (Exception ex) {
                olCommand.setDescription(getMyDecoder().getSessionid());
            }

            for (int i = (int) (Math.max(0, newBag.length - max)); i < newBag.length; i++) {
                olCommand.addToBag(String.format("%03d %s", i, newBag[i]));
            }
//            if (newBag.length > olCommand.getBagSize()) {
//                for (int i = olCommand.getBagSize(); i < newBag.length; i++) {
//                    olCommand.addToBag(String.format("%03d ", 
//                            this.mySensorsVisual.get("COMMAND").getBagSize()) + newBag[i]);
//                }
//            }

            String newCargo[] = getMyDecoder().getCargo();
            olPayload.setDescription(agentName);
            if (newCargo.length != olPayload.getBag().size()) {
                olPayload.clear();
                for (int i = 0; i < newCargo.length; i++) {
                    olPayload.addToBag(String.format("%03d %s", i, newCargo[i]));
//                    olPayload.addToBag(String.format("%03d ", this.mySensorsVisual.get("PAYLOAD").getBagSize()) + newCargo[i]);
                }
            }
            lastPerception = perception;
//                    });
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
//            getMyDecoder().findCourseTo(new Point3D(10,0,getMyDecoder().getAltitude()));
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

    protected void Layout1() {
        int hLabels = 50, yy = hLabels, xx = 0, ww = 150, ww2 = 50, hh = 25;

        osMap = new OleSuperMap(this, "MAP");
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
        pal.fillWayPointsPerc(255);
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
//        pal.fillWayPointsPerc(275);
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
//        pal.fillWayPointsPerc(3500);
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

        osMap = new OleSuperMap(this, "MAP");
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
//        pal.fillWayPointsPerc(255);
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
//        pal.fillWayPointsPerc(275);
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
        pal.fillWayPointsPerc(3500);
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

    protected void Layout3() {
        int hLabels = 50, yy = hLabels, xx = 0, ww = 150, ww2 = 50, hh = 25, ldials = 110;

        osMap = new OleSuperMap(this, "MAP");
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
        orCompass1.setAutoRotate(false);
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
//        pal.fillWayPointsPerc(255);
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
//        pal.fillWayPointsPerc(275);
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
        pal.fillWayPointsPerc(3500);
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

    @Override
    public void mouseClicked(MouseEvent e) {
        osHud.mouseClicked(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

}
