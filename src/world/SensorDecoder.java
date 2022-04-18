/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import ai.TracePositions;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import data.Ole;
import data.OleFile;
import data.Transform;
import geometry.Compass;
import geometry.Point3D;
import geometry.PolarSurface;
import geometry.SimpleVector3D;
import geometry.Vector3D;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import map2D.Map2DColor;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class SensorDecoder {

    protected HashMap<String, JsonArray> indexperception;
    protected JsonArray lastPerception;
    protected Map2DColor hMap, hMapMargin;
    protected int maxlevel, mapMargin = 20;
    protected boolean ready, filterreading;
    protected String name, sessionID, commitment, sLastPerception = "";
    protected SimpleVector3D lastPosition;
    protected TracePositions myTrace;
    protected double lastDistance;

    public SensorDecoder() {
        clear();
    }

    public boolean setWorldMap(String content, int maxlevel) {
        setMaxlevel(maxlevel);
        OleFile mapa = new OleFile();
        try {
            mapa.set(content);
            mapa.saveFile("./maps/");
            String name = mapa.getFileName();
            hMap = new Map2DColor();
            hMap.loadMapRaw("./maps/" + name);
            hMapMargin = new Map2DColor(hMap.getWidth() + 2 * mapMargin, hMap.getHeight() + 2 * mapMargin);
            for (int x = 0; x < hMapMargin.getWidth(); x++) {
                for (int y = 0; y < hMapMargin.getHeight(); y++) {
                    if (hMap.getStepLevel(x - mapMargin, y - mapMargin) > getMaxlevel()) {
                        hMapMargin.setColor(x, y, Map2DColor.BADVALUE);
                        hMap.setColor(x - mapMargin, y - mapMargin, Map2DColor.BADVALUE);
                    } else {
                        hMapMargin.setColor(x, y, hMap.getColor(x - mapMargin, y - mapMargin));

                    }
                }
            }

//            File toremove= new File("./maps/" + name);
//            if (toremove.exists())
//                toremove.delete();
            lastPosition = null;

            return true;
        } catch (IOException ex) {
        }
        return false;
    }

    public int getMaxlevel() {
        return maxlevel;
    }

    public void setMaxlevel(int maxlevel) {
        this.maxlevel = maxlevel;
    }

    public String getStatus() {
        return this.getSensor(glossary.sensors.STATUS.name().toLowerCase()).get(0).asString();
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public Map2DColor getWorldMap() {
        return hMap;
    }

    public boolean hasSensor(String sensorname) {
        return isReady() && this.indexperception.keySet().contains(sensorname.toLowerCase());
    }

    protected JsonArray getSensor(String sensorname) {
        if (isReady()) {
            if (this.indexperception.keySet().contains(sensorname)) {
                return indexperception.get(sensorname);
            }
        }
        return null;
    }

    public void setSensor(String sensorname, JsonArray reading) {
        indexperception.put(sensorname, reading);
        lastPerception.add(new JsonObject().add("sensor", sensorname).add("data", reading));
    }

    public void clear() {
        indexperception = new HashMap();
        lastPerception = new JsonArray();
        myTrace = new TracePositions();
        lastDistance = Integer.MAX_VALUE;
        ready = false;
    }

    public boolean getAlive() {
        if (isReady() && hasSensor("ALIVE")) {
            return getSensor("alive").get(0).asInt() > 0;
        }
        return false;
    }

    public boolean getOnTarget() {
        if (isReady() && hasSensor("ONTARGET")) {
            return getSensor("ontarget").get(0).asInt() > 0;
        }
        return false;
    }

    protected double[] fromJsonArray(JsonArray jsa) {
        double res[] = new double[jsa.size()];
        for (int i = 0; i < jsa.size(); i++) {
            res[i] = jsa.get(i).asDouble();
        }
        ready = true;
        return res;
    }

    public boolean isReady() {
        return ready;
    }

    public double[] getGPS() {
        double[] res = new double[3];
        if (isReady() && hasSensor("GPS")) {
            res = fromJsonArray(getSensor("gps").get(0).asArray());
        }
        return res;
    }

    public double getAltitude() {
        double[] res = new double[3];
        if (isReady() && hasSensor("GPS")) {
            return getGPS()[2];
        }
        return -1;
    }

    public int getPayload() {
        if (isReady() && hasSensor("PAYLOAD")) {
            return getSensor("payload").get(0).asInt();
        }
        return -1;
    }

    public SimpleVector3D getGPSVector() {
//        return new SimpleVector3D((int)getGPS()[0], (int)getGPS()[1],getOrientation());
        return new SimpleVector3D(getGPSPosition(), getCompass() / 45);
    }

    public int getOrientation() {
        return getCompass() / 45;
    }

    public Point3D getGPSPosition() {
        return new Point3D(getGPS()[0], getGPS()[1], getGPS()[2]);
    }

    public Point3D getGPSPosition(int n) {
        return myTrace.getLastPosition(n);
    }

    public Point3D getGPSComingPosition() {
        return getGPSVector().getTarget();
    }

    public TracePositions getAllGPSPositions() {
        return myTrace;
    }

    public int getCompass() {
        if (isReady() && hasSensor("COMPASS")) {
            int v = (int) getSensor("compass").get(0).asDouble();
            v = 360 - v;
            return v % 360;
//            v = 360 + 90 - v;
//            return v % 360;
        }
        return -1;
    }

    public int getGround() {
        if (isReady() && hasSensor("ALTITUDE")) {
            return (int) getSensor("altitude").get(0).asDouble();
        }
        return -1;
    }

    public double getDistance() {
        if (isReady() && hasSensor("DISTANCE")) {
            return (int) getSensor("distance").get(0).asDouble();
        }
        return -1;
    }

    public double getDifferentialDistance() {
        return getDistance() - lastDistance;
    }

    public boolean isCloser() {
        return getDifferentialDistance() < 0;
    }

    public boolean isFarther() {
        return getDifferentialDistance() > 0;
    }

    public int getCompassLeft() {
        return (getCompass() + 45 + 360) % 360;
    }

    public int getCompassRight() {
        return (getCompass() - 45 + 360) % 360;
    }

    public double getRelativeAngular() {
        if (isReady() && hasSensor("ANGULAR")) {
            double a = getAbsoluteAngular(), c = getCompass();
            if (a > c) {
                if (a - c <= 180) {
                    return a - c;
                } else {
                    return -(c + 360 - a);
                }
            } else {
                if (c - a < 180) {
                    return a - c;
                } else {
                    return (a + 360 - c);
                }

            }
//            if (getCompass()+360 <= getAbsoluteAngular()+360 && getAbsoluteAngular()+360 <= getCompass()+360 + 180) {
//                return getAbsoluteAngular() - getCompass();
//            } else {
//                return -(getCompass() - (getAbsoluteAngular()));
//            }
        }
        return -1;
    }

    public double getAbsoluteAngular() {
        if (isReady() && hasSensor("ANGULAR")) {
            double v = getSensor("angular").get(0).asDouble();
            v = 360 - v + 360;
//            v = v+getCompass();   
            return (int) v % 360;
        }
        return -1;
    }

    public double getAbsoluteAngularTo(Point3D p) {
        if (isReady() && hasSensor("ANGULAR")) {
            Vector3D Norte = new Vector3D(new Point3D(0, 0), new Point3D(0, -10));
            Point3D me = new Point3D(getGPS()[0], getGPS()[1], getGPS()[2]);
            Vector3D Busca = new Vector3D(me, p);

            int v = (int) Norte.angleXYTo(Busca);;
            v = 360 + 90 - v;
            return v % 360;
        }
        return -1;
    }

    public double getEnergy() {
        if (isReady() && hasSensor("ENERGY")) {
            return (int) getSensor("energy").get(0).asDouble();
        }
        return -1;
    }

    public double getEnergyBurnt() {
        if (isReady() && hasSensor("ENERGYBURNT")) {
            return (int) getSensor("energyburnt").get(0).asDouble();
        }
        return -1;
    }

    public String[] getTrace() {
        if (isReady() && hasSensor("TRACE")) {
            return Transform.toArray(new ArrayList(Transform.toArrayList(getSensor("trace"))));
        }
        return new String[0];
    }

    public String getLastTrace() {
        if (isReady() && hasSensor("TRACE") && getSensor("trace").size() > 0) {
            return getSensor("trace").get(getSensor("trace").size() - 1).asString();
        }
        return "";
    }

    public String[] getCargo() {
        if (isReady() && hasSensor("CARGO")) {
            return Transform.toArray(new ArrayList(Transform.toArrayList(getSensor("cargo"))));
        }
        return new String[0];
    }

    public int getNSteps() {
        if (isReady() && hasSensor("NUMSTEPS")) {
            return this.getTrace().length;
//            return (int) getSensor("numsteps").get(0).asDouble();
        }
        return -1;

    }

    public Map2DColor getFullZenitalVisual() {
        if (getVisualData() == null) {
            return null;
        }
        Map2DColor initial, res;
        int[][] levels = getVisualData();
//        SimpleVector3D myv = this.getGPSVector();
//        PolarSurface ps = new PolarSurface(myv.clone().minus(myv.getSource()));
//        ps.setRadius(levels[0].length);
        initial = new Map2DColor(levels[0].length, levels.length);
        for (int y = 0; y < levels.length; y++) {
            for (int x = 0; x < levels[0].length; x++) {
                initial.setLevel(x, y, levels[y][x]);
            }
        }
        res = initial;
        res.setColor(res.getWidth() / 2, res.getHeight() / 2, Color.GREEN);
//        return ps.applyAbsoluteTo(initial);
        return res;
    }

    public int[][] getPolarVisual() {
        if (getVisualData() == null) {
            return null;
        }
        int initial[][] = this.getVisualData(), res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()));
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyPolarTo(initial);
        return res;
    }

    public int[][] getPolarLidar() {
        if (getLidarData() == null) {
            return null;
        }
        int initial[][] = this.getLidarData(), res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()));
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyPolarTo(initial);
        return res;
    }

    public int[][] getPolarThermal() {
        if (getThermalData() == null) {
            return null;
        }
        int initial[][] = this.getThermalData(), res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()));
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyPolarTo(initial);
        return res;
    }

    public int[][] getAbsoluteLidar() {
        if (getLidarData() == null) {
            return null;
        }
        int initial[][] = this.getLidarData(), res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()));
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyAbsoluteTo(initial);
        return res;
    }

    public int[][] getAbsoluteThermal() {
        if (getThermalData() == null) {
            return null;
        }
        int initial[][] = this.getThermalData(), res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()));
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyAbsoluteTo(initial);
        return res;
    }

    public int[][] getAbsoluteVisual() {
        if (getVisualData() == null) {
            return null;
        }
        int initial[][] = this.getVisualData(), res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()));
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyAbsoluteTo(initial);
        return res;
    }

    public int[][] getRelativeVisual() {
        if (getVisualData() == null) {
            return null;
        }
        int initial[][] = this.getVisualData(), res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length / 2 + 1;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh - 1, myv.getsOrient()));
        ps.setRadius(mww / 2 + 1);
        res = ps.applyRelativeTo(initial);
        return res;
    }

    public int[][] getRelativeLidar() {
        if (getLidarData() == null) {
            return null;
        }
        int initial[][] = this.getLidarData(), res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length / 2 + 1;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh - 1, myv.getsOrient()));
        ps.setRadius(mww / 2 + 1);
        res = ps.applyRelativeTo(initial);
        return res;
    }

    public int[][] getRelativeThermal() {
        if (getThermalData() == null) {
            return null;
        }
        int initial[][] = this.getThermalData(), res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length / 2 + 1;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh - 1, myv.getsOrient()));
        ps.setRadius(mww / 2 + 1);
        res = ps.applyRelativeTo(initial);
        return res;
    }

    protected int[][] getVisualData() {
        JsonArray jsaReading = null;
        jsaReading = getSensor("visual");
        if (jsaReading == null) {
            jsaReading = getSensor("visualhq");
        }
        if (isReady() && jsaReading != null) {
            int range = jsaReading.size();

            int[][] res = new int[range][range]; //jsaVisual.size(), jsaVisual.size());
            for (int i = 0; i < res.length; i++) {
                for (int j = 0; j < res[0].length; j++) {
                    res[j][i] = jsaReading.get(i).asArray().get(j).asInt();
                }
            }

            return res;
        }
        return null;
    }

    protected int[][] getLidarData() {
        JsonArray jsaReading = null;
        jsaReading = getSensor("lidar");
        if (jsaReading == null) {
            jsaReading = getSensor("lidarhq");
        }
        if (isReady() && jsaReading != null) {
            int range = jsaReading.size();
            int[][] res = new int[range][range]; //jsaVisual.size(), jsaVisual.size());
            for (int i = 0; i < res.length; i++) {
                for (int j = 0; j < res[0].length; j++) {
                    res[j][i] = jsaReading.get(i).asArray().get(j).asInt();
                }
            }

            return res;
        }
        return null;
    }

    protected int[][] getThermalData() {
        JsonArray jsaReading = null;
        jsaReading = getSensor("thermal");
        if (jsaReading == null) {
            jsaReading = getSensor("thermalhq");
        }
        if (isReady() && jsaReading != null) {
            int range = jsaReading.size();
            int[][] res = new int[range][range]; //jsaVisual.size(), jsaVisual.size());
            for (int i = 0; i < res.length; i++) {
                for (int j = 0; j < res[0].length; j++) {
                    res[j][i] = (int) jsaReading.get(i).asArray().get(j).asDouble();
                }
            }

            return res;
        }
        return null;
    }

    public JsonObject toJson() {
        return new JsonObject().add("perceptions", lastPerception);
    }

    public Ole toOle() {
        return new Ole(toJson());
    }

    public void fromJson(JsonArray jsareading) {
//        clear();
        for (int i = 0; i < jsareading.size(); i++) {
            JsonObject jsosensor = jsareading.get(i).asObject();
            String name = jsosensor.getString("sensor", "");
            setSensor(name, jsosensor.get("data").asArray());
//            System.out.println("Sensor: " + name);
        }
        ready = true;
    }

    public void fromOle(ArrayList<Ole> oreading) {
//        clear();
        for (Ole osensor : oreading) {
            String sensorname = osensor.getField("sensor");
            setSensor(sensorname, Transform.toJsonArray(new ArrayList(osensor.getArray("data"))));
        }
        ready = true;
    }

    public String getName() {
        if (this.isReady()) {
            return name;
        } else {
            return "";
        }
    }

    public String getSession() {
        if (this.isReady()) {
            return sessionID;
        } else {
            return "";
        }
    }

    public void feedPerception(String content) {
        if (sLastPerception.equals(content)) {
            return;
        }
        try {
            lastPosition = this.getGPSVector();
        } catch (Exception ex) {
            lastPosition = null;
        }

        JsonObject jsoperception = Json.parse(content).asObject();
        name = jsoperception.getString("name", "unknown");
        sessionID = jsoperception.getString("sessionID", "unknown");
        commitment = jsoperception.getString("commitment", "");
        fromJson(jsoperception.get("perceptions").asArray());
        sLastPerception = content;
        ready = true;
        myTrace.addUniquePosition(this.getGPSPosition());
        lastDistance = getDistance();
    }

    public String getCommitment() {
        return commitment;
    }

    public void setCommitment(String commitment) {
        this.commitment = commitment;
    }

    public String[] getSensorList() {
        return this.indexperception.keySet().toArray(new String[this.indexperception.keySet().size()]);
    }

    public int getMapMargin() {
        return mapMargin;
    }

    public void setMapMargin(int mapMargin) {
        this.mapMargin = mapMargin;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public SimpleVector3D getPreviousGPSVector() {
        if (lastPosition == null) {
            return this.getGPSVector();
        } else {
            return lastPosition;
        }
    }

    public int getLidarFront() {
        return getPolarLidar()[2][0];
    }

    public int getLidarLeft() {
        return getPolarLidar()[1][0];
    }

    public int getLidarLeftmost() {
        return getPolarLidar()[0][0];
    }

    public int getLidarRight() {
        return getPolarLidar()[3][0];
    }

    public int getLidarRightmost() {
        return getPolarLidar()[4][0];
    }

    public int[][] getRadarData() {
        int data[][] = this.getLidarData();
        int w = data[0].length, h = data.length;
        int res[][] = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (data[x][y] == Perceptor.NULLREAD) {
                    res[x][y] = data[x][y];
                } else if (data[x][y] < 0) {
                    res[x][y] = 1;
                } else {
                    res[x][y] = 0;
                }
            }
        }
        return res;
    }

    public int[][] getDistancesData() {
        int w = this.getThermalData()[0].length, h = this.getThermalData().length;
        int res[][] = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                res[x][y] = getThermalData()[x][y];
            }
        }
        return res;
    }

    public String printStatus(String requester) {
        String res = "", line;

        res = "Under request from " + requester + "\n";
        res += "|  " + getNSteps() + "\n";
        res += "|  Status of: " + getName() + "\n";
        res += "| |Memory:\n";
        res += "| |(" + myTrace.size() + ") " + this.getGPSPosition(1) + "\n";
        res += "| |\n";
        res += "|  EN:" + getEnergy() + "W \n";
        res += "|  X:" + getGPSPosition().getXInt() + " Y:" + getGPSPosition().getYInt() + " Z:" + (int) getAltitude() + "\n";
        res += "|  GR:" + (int) getGround() + "m" + "\n";
        res += "|  CO:" + Compass.NAME[getCompass() / 45] + " " + getCompass() + "º\n";
        res += "|  --> :" + getGPSVector().toString() + "\n";
        res += "| DI: " + (int) getDistance() + "m  AN:" + getAbsoluteAngular() + "º/" + getRelativeAngular() + "º\n";
        res += "| CA: (" + getCargo().length + ")   " + Transform.toArrayList(this.getCargo()) + "\n";
        res += "| |\n";
        res += "| |Coming move " + this.getGPSComingPosition() + "\n";
        int polar[][] = this.getPolarLidar();
        for (int y = 0; y < 3; y++) {
            if (y == 0) {
                line = "|PL|";
            } else {
                line = "|  |";
            }
            for (int x = 0; x < polar[0].length; x++) {
                if (polar[x][y] == Perceptor.NULLREAD) {
                    line += "XXX|";
                } else {
                    line += String.format("%03d|", polar[x][y]);
                }
            }
            res += line + "\n";
        }

//        int visual[][] = getVisualData(), lidar[][] = getLidarData(), thermal[][] = getThermalData();
//        line = "";
//        for (int y = 0; y < visual.length; y++) {
//            if (y == 0) {
//                line = "| V|";
//            } else {
//                line = "|  |";
//            }
//            for (int x = 0; x < visual[0].length; x++) {
//                if (visual[x][y] == Perceptor.NULLREAD) {
//                    line += "XXX|";
//                } else {
//                    line += String.format("%03d|", visual[x][y]);
//                }
//            }
//            if (y == 0) {
//                line += "  L|";
//            } else {
//                line += "   |";
//            }
//            for (int x = 0; x < lidar[0].length; x++) {
//                if (lidar[x][y] == Perceptor.NULLREAD) {
//                    line += "XXX|";
//                } else {
//                    line += String.format("%03d|", lidar[x][y]);
//                }
//            }
//            if (y == 0) {
//                line += "  T|";
//            } else {
//                line += "   |";
//            }
//            for (int x = 0; x < thermal[0].length; x++) {
//                if (thermal[x][y] == Perceptor.NULLREAD) {
//                    line += "XXX|";
//                } else {
//                    line += String.format("%03d|", thermal[x][y]);
//                }
//            }
//            res += line + "\n";
//        }
//        res += "\n";
//        visual = this.getAbsoluteVisual();
//        lidar = getAbsoluteLidar();
//        thermal = getAbsoluteThermal();
//        line = "";
//        for (int y = 0; y < visual.length; y++) {
//            if (y == 0) {
//                line = "|AV|";
//            } else {
//                line = "|  |";
//            }
//            for (int x = 0; x < visual.length; x++) {
//                if (y < visual[0].length) {
//                    if (visual[x][y] == Perceptor.NULLREAD) {
//                        line += "XXX|";
//                    } else {
//                        line += String.format("%03d|", visual[x][y]);
//                    }
//                } else {
//                    line += "   |";
//                }
//            }
//            if (y == 0) {
//                line += " AL|";
//            } else {
//                line += "   |";
//            }
//            for (int x = 0; x < lidar.length; x++) {
//                if (y < lidar[0].length) {
//                    if (lidar[x][y] == Perceptor.NULLREAD) {
//                        line += "XXX|";
//                    } else {
//                        line += String.format("%03d|", lidar[x][y]);
//                    }
//                } else {
//                    line += "   |";
//                }
//            }
//            if (y == 0) {
//                line += " AT|";
//            } else {
//                line += "   |";
//            }
//            for (int x = 0; x < thermal.length; x++) {
//                if (y < thermal[0].length) {
//                    if (thermal[x][y] == Perceptor.NULLREAD) {
//                        line += "XXX|";
//                    } else {
//                        line += String.format("%03d|", thermal[x][y]);
//                    }
//                } else {
//                    line += "   |";
//                }
//            }
//            res += line + "\n";
//        }
//        res += "\n";
//        visual = this.getRelativeVisual();
//        lidar = getRelativeLidar();
//        thermal = getRelativeThermal();
//        line = "";
//        for (int y = 0; y < visual.length; y++) {
//            if (y == 0) {
//                line = "|RV|";
//            } else {
//                line = "|  |";
//            }
//            for (int x = 0; x < visual.length; x++) {
//                if (y < visual[0].length) {
//                    if (visual[x][y] == Perceptor.NULLREAD) {
//                        line += "XXX|";
//                    } else {
//                        line += String.format("%03d|", visual[x][y]);
//                    }
//                } else {
//                    line += "   |";
//                }
//            }
//            if (y == 0) {
//                line += " RL|";
//            } else {
//                line += "   |";
//            }
//            for (int x = 0; x < lidar.length; x++) {
//                if (y < lidar[0].length) {
//                    if (lidar[x][y] == Perceptor.NULLREAD) {
//                        line += "XXX|";
//                    } else {
//                        line += String.format("%03d|", lidar[x][y]);
//                    }
//                } else {
//                    line += "   |";
//                }
//            }
//            if (y == 0) {
//                line += " RT|";
//            } else {
//                line += "   |";
//            }
//            for (int x = 0; x < thermal.length; x++) {
//                if (y < thermal[0].length) {
//                    if (thermal[x][y] == Perceptor.NULLREAD) {
//                        line += "XXX|";
//                    } else {
//                        line += String.format("%03d|", thermal[x][y]);
//                    }
//                } else {
//                    line += "   |";
//                }
//            }
//            res += line + "\n";
//        }
//        res += "|\n\n";
        return res;
    }
}
