/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Environment;

import agents.LARVAFirstAgent;
import ai.AStar;
import ai.Choice;
import ai.Mission;
import ai.Plan;
import ai.Search;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import data.Transform;
import geometry.Compass;
import geometry.Point3D;
import geometry.PolarSurface;
import geometry.SimpleVector3D;
import glossary.Roles;
import glossary.Sensors;
import java.util.ArrayList;
import world.ThingSet;
import world.Perceptor;
import world.SensorDecoder;
import world.Thing;
import world.World;
import world.liveBot;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Environment extends SensorDecoder {

    // Inner execution
    protected World World;
    liveBot live;
    // Remote execution
    protected int[][] visualData, lidarData, thermalData;
    protected int[] shortPolar;
    protected ThingSet cadastre;
    protected ThingSet census;
    protected int slope;

    public Environment() {
        super();
//        shortPolar = new int[5];
//        World = new World("innerworld");
        cadastre = new ThingSet();
        census = new ThingSet();
    }

    public Environment(Environment other) {
        super();
        String saux = other.toJson().toString();
        JsonObject jsaux = Json.parse(saux).asObject();
        this.fromJson(jsaux.get("perceptions").asArray());
        cadastre = other.getFullCadastre();
        census = other.getCensus();
        cache();

    }

    public boolean loadWorld(String worldname, String name, Roles r, Sensors attach[]) {
        if (!World.loadConfig("./worlds/" + worldname + ".worldconf.json").equals("ok")) {
            return false;
        }
        if (!loadWorldMap(World.getEnvironment().getSurface())) {
            return false;
        }
        this.live = World.registerAgent(name, r.name(), attach);
        World.locateAgent(live, new Point3D(-1, -1, 0));
//        System.out.println(live.getPerceptions().toString());
//        feedPerception(this.live.getPerceptions());
        return true;
    }

    public Environment setExternalPerceptions(String perceptions) {
        try {
            feedPerception(perceptions);
            if (this.getGPSMemorySize() > 1) {
                slope = this.getGPS().getZInt() - this.getGPSMemory(1).getZInt();
            } else {
                slope = 0;
            }
            cache();
            return this;
        } catch (Exception ex) {
            return null;
        }
    }

    public Environment setExternalObjects(String things) {
        try {
            JsonObject jsoThings = Json.parse(things).asObject();
            JsonArray jsathings;
            if (jsoThings.get("city") != null) {
                if (cadastre == null) {
                    cadastre = new ThingSet();
                }
                cadastre.fromJson(jsoThings.get("city").asArray());
            }
            if (jsoThings.get("people") != null) {
                if (census == null) {
                    census = new ThingSet();
                }
                census.fromJson(jsoThings.get("cities").asArray());
            }
            cache();
        } catch (Exception ex) {

        }
        return this;
    }

    public Environment addThings(ArrayList<Thing> listT) {
        for (Thing t : listT) {
            if (t.getType().equals("city")) {
                this.cadastre.addThing(t);
            }
            if (t.getType().equals("people")) {
                this.census.addThing(t);
            }
        }
        return this;
    }

    public Environment readInternalPerceptions() {
//        System.out.println(live.getPerceptions().toString());
        feedPerception(live.getPerceptions());
//        feedPerception(live.Raw().toJson());
        cache();
        return this;
    }

    public boolean executeInternalAction(String action) {
        return World.execAgent(live, action);
    }

    public boolean isEquivalentTo(Environment other) {
        return (other.getGPS().getX() == this.getGPS().getX() && other.getGPS().getY() == this.getGPS().getY() && other.getGPS().getZ() == this.getGPS().getZ());
    }

    public Environment clone() {
        Environment res = new Environment();
        for (Sensors sname : this.indexperception.keySet()) {
            if (getSensor(sname) != null) {
                res.encodeSensor(sname, Json.parse(getSensor(sname).toString()).asArray());
            }
        }
        res.hMap = this.hMap;
        return res;
    }

    public Environment simmulate(Choice a) {
        double y, x, dincrx, dincry;
        String action = a.getName();
        Environment result = this.clone();
        boolean movement = false;

        int incrx = 0, incry = 0, incrz = 0;
        result.cache();
        switch (action.toUpperCase()) {
            case "MOVE":
                incrx = (int) Compass.SHIFT[this.getCompass() / 45].moduloX();
                incry = (int) Compass.SHIFT[this.getCompass() / 45].moduloY();
                if (this.getMaxslope() >= this.getMaxlevel() / 2) {  // AIRBORNE
                    incrz = 0;
                } else {
//                    incrz = this.getVisualFront() - this.getVisualHere() ;
                    incrz = -this.getLidarFront(); //this.getVisualFront() - this.getVisualHere() ;
                }
                movement = true;
                break;
            case "LEFT":
                incrx = 0;
                incry = 0;
                incrz = 0;
                result.setCompass(this.getCompassLeft());
                movement = true;
                break;
            case "RIGHT":
//                result.setCompass((270 + getCompass()) % 360);
                incrx = 0;
                incry = 0;
                incrz = 0;
                result.setCompass(this.getCompassRight());
                movement = true;
                break;
            case "UP":
                incrx = 0;
                incry = 0;
                incrz = 5;
                movement = true;
                break;
            case "DOWN":
                incrx = 0;
                incry = 0;
                incrz = -5;
                movement = true;
                break;
            case "IDLE":
                break;
            case "RECHARGE":
                if (result.getGround() == 0) {
                    result.setEnergy(this.getAutonomy());
                }
                break;
            case "RESCUE":
            case "CAPTURE":
                break;
            default:
        }
        if (movement) {
            result.currentMission = this.currentMission;
            result.slope = incrz;
            result.setGPS(new Point3D(this.getGPS().getX() + incrx, this.getGPS().getY() + incry, this.getGPS().getZ() + incrz));
            result.setEnergy(this.getEnergy() - this.getBurnratemove());
            result.thermalData = Transform.shift(this.thermalData, incrx, incry, Perceptor.NULLREAD);
            result.encodeSensor(Sensors.THERMAL, Transform.Matrix2JsonArray(result.thermalData));
            result.visualData = Transform.shift(this.visualData, incrx, incry, Perceptor.NULLREAD);
            result.encodeSensor(Sensors.VISUAL, Transform.Matrix2JsonArray(result.visualData));
            result.lidarData = Transform.shift(this.lidarData, incrx, incry, Perceptor.NULLREAD);
            for (int lx = 0; lx < result.lidarData.length; lx++) {
                for (int ly = 0; ly < result.lidarData[0].length; ly++) {
                    result.lidarData[lx][ly] = (int) (result.lidarData[lx][ly] == Choice.MAX_UTILITY ? Choice.MAX_UTILITY : result.lidarData[lx][ly] + incrz);
                }
            }
            result.encodeSensor(Sensors.LIDAR, Transform.Matrix2JsonArray(result.lidarData));
            if (result.getTarget() != null) {
                result.setOntarget(result.getGPS().isEqualTo(result.getTarget()));
            } else {
                result.setOntarget(false);
            }
            result.setAlive(result.willBeAlive());
            result.setGround(result.getLidarHere());
        }
        return result;
    }

    public boolean willBeAlive() {
        if (getGPS().getZ() < getVisualHere()) {
            return false;
        }
        if (getGPS().getX() < 0 || getGPS().getX() >= getWorldWidth()) {
            return false;
        }
        if (getGPS().getY() < 0 || getGPS().getY() >= getWorldHeight()) {
            return false;
        }
        if (getEnergy() < 1) {
            return false;
        }
        if (getSlope() > getMaxslope() || getSlope() < -getMaxslope()) {
            return false;
        }
        if (getGPS().getZ() > getMaxlevel() || getGPS().getZ() < getMinlevel()) {
            return false;
        }
        return true;
    }

    public void cache() {
        lidarData = getLidarData();
        visualData = getVisualData();
        thermalData = getThermalData();
    }

    public int[][] getShortRadar() {
        int res[][] = this.getShortGeneral(lidarData);
        for (int x = 0; x < res.length; x++) {
            for (int y = 0; y < res[0].length; y++) {
                if (res[x][y] < 0) {
                    res[x][y] = 1;
                } else {
                    res[x][y] = 0;
                }
            }
        }
        return res;
    }

    public int[][] getShortDistances() {
        return this.getShortGeneral(thermalData);
    }

    public int[] getShortPolar() {
        int result[] = new int[5];
        result[0] = getLeftmostGeneral(lidarData);
        result[1] = getLeftGeneral(lidarData);
        result[2] = getFrontGeneral(lidarData);
        result[3] = getRightGeneral(lidarData);
        result[4] = getRightmostGeneral(lidarData);
        return result;
    }

    public int[][] getShortLidar() {
        return this.getShortGeneral(lidarData);
    }

    public int[][] getShortVisual() {
        return this.getShortGeneral(visualData);
    }

    protected int[][] getShortGeneral(int[][] data) {
        int result[][] = new int[3][3];
        for (int x = 0; x < result[0].length; x++) {
            for (int y = 0; y < result.length; y++) {
                result[x][y] = Transform.centroid(data, x - 1, y - 1, Perceptor.NULLREAD);
            }
        }
        return result;
    }

    protected int[][] getPolarGeneral(int[][] data) {
        int initial[][] = data, res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()), myv);
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyPolarTo(initial);
        return res;
    }

    protected int[][] getAbsoluteGeneral(int[][] data) {
        int initial[][] = data, res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()), myv);
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyAbsoluteTo(initial);
        return res;
    }

    protected int[][] getRelativeGeneral(int[][] data) {
        int initial[][] = data, res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length / 2 + 1;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh - 1, myv.getsOrient()), myv);
        ps.setRadius(mww / 2 + 1);
        res = ps.applyRelativeTo(initial);
        return res;
    }

    protected int getHereGeneral(int[][] data) {
        return Transform.centroid(data, 0, 0, Perceptor.NULLREAD);
    }

    protected int getFrontGeneral(int[][] data) {
        return Transform.centroid(data, (int) Compass.SHIFT[this.getCompass() / 45].moduloX(),
                (int) Compass.SHIFT[this.getCompass() / 45].moduloY(), Perceptor.NULLREAD);
    }

    protected int getLeftGeneral(int[][] data) {
        return Transform.centroid(data, (int) Compass.SHIFT[Compass.Left(this.getCompass()) / 45].moduloX(),
                (int) Compass.SHIFT[Compass.Left(this.getCompass()) / 45].moduloY(), Perceptor.NULLREAD);
    }

    protected int getLeftmostGeneral(int[][] data) {
        return Transform.centroid(data, (int) Compass.SHIFT[Compass.Left(Compass.Left(this.getCompass())) / 45].moduloX(),
                (int) Compass.SHIFT[Compass.Left(Compass.Left(this.getCompass())) / 45].moduloY(), Perceptor.NULLREAD);
    }

    protected int getRightGeneral(int[][] data) {
        return Transform.centroid(data, (int) Compass.SHIFT[Compass.Right(this.getCompass()) / 45].moduloX(),
                (int) Compass.SHIFT[Compass.Right(this.getCompass()) / 45].moduloY(), Perceptor.NULLREAD);
    }

    protected int getRightmostGeneral(int[][] data) {
        return Transform.centroid(data, (int) Compass.SHIFT[Compass.Right(Compass.Right(this.getCompass())) / 45].moduloX(),
                (int) Compass.SHIFT[Compass.Right(Compass.Right(this.getCompass())) / 45].moduloY(), Perceptor.NULLREAD);
    }

    public int getLidarFront() {
        return getFrontGeneral(lidarData);
    }

    public int getLidarLeft() {
        return getLeftGeneral(lidarData);
    }

    public int getLidarLeftmost() {
        return getLeftmostGeneral(lidarData);
    }

    public int getLidarRight() {
        return getRightGeneral(lidarData);
    }

    public int getLidarRightmost() {
        return getRightmostGeneral(lidarData);
    }

    public int getVisualFront() {
        return getFrontGeneral(visualData);
    }

    public int getVisualLeft() {
        return getLeftGeneral(visualData);
    }

    public int getVisualLeftmost() {
        return getLeftmostGeneral(visualData);
    }

    public int getVisualRight() {
        return getRightGeneral(visualData);
    }

    public int getVisualRightmost() {
        return getRightmostGeneral(visualData);
    }

    public int getThermalFront() {
        return getFrontGeneral(thermalData);
    }

    public int getThermalLeft() {
        return getLeftGeneral(thermalData);
    }

    public int getThermalLeftmost() {
        return getLeftmostGeneral(thermalData);
    }

    public int getThermalRight() {
        return getRightGeneral(thermalData);
    }

    public int getThermalRightmost() {
        return getRightmostGeneral(thermalData);
    }

    public int getThermalHere() {
        return getHereGeneral(thermalData);
    }

    public int getVisualHere() {
        return getHereGeneral(visualData);
    }

    public int getLidarHere() {
        return getHereGeneral(lidarData);
    }

    public boolean isCrahsed() {
        return !this.getAlive();
    }

    public int getSlope() {
        return slope;

    }

    public boolean isEnergyExhausted() {
        return this.getEnergy() < 0;
    }

    public boolean isFreeFront() {
        return this.getLidarFront() >= -getMaxslope() && getLidarFront() <= getMaxslope()
                && getVisualFront() >= this.getMinlevel()
                && getVisualFront() <= this.getMaxlevel();
//        return this.getLidarFront() >= 0;
    }

    public boolean isFreeFrontLeft() {
        return this.getLidarLeft() >= -getMaxslope() && getLidarLeft() <= getMaxslope()
                && getVisualLeft() >= this.getMinlevel()
                && getVisualLeft() <= this.getMaxlevel();
    }

    public boolean isFreeFrontRight() {
        return this.getLidarRight() >= -getMaxslope() && getLidarRight() <= getMaxslope()
                && getVisualRight() >= this.getMinlevel()
                && getVisualRight() <= this.getMaxlevel();
    }

    public boolean isFreeLeft() {
        return this.getLidarLeftmost() >= -getMaxslope() && getLidarLeftmost() <= getMaxslope()
                && getVisualLeftmost() >= this.getMinlevel()
                && getVisualLeftmost() <= this.getMaxlevel();
    }

    public boolean isFreeRight() {
        return this.getLidarRightmost() >= -getMaxslope() && getLidarRightmost() <= getMaxslope()
                && getVisualRightmost() >= this.getMinlevel()
                && getVisualRightmost() <= this.getMaxlevel();
    }

    public boolean isTargetAhead() {
        return getRelativeAngular() >= -90 && getRelativeAngular() <= 90;
//        return getRelativeAngular() > -45 && getRelativeAngular() < 45;
    }

    public boolean isTargetFront() {
        return getRelativeAngular() > -22 && getRelativeAngular() < 22;
//        return getRelativeAngular() > -45 && getRelativeAngular() < 45;
    }

    public boolean isTargetBack() {
        return getRelativeAngular() > 90 || getRelativeAngular() < -90;
//        return getRelativeAngular() > -45 && getRelativeAngular() < 45;
    }

    public boolean isTargetLeft() {
        return getRelativeAngular() > 0;
//        return getRelativeAngular() >= 45;
    }

    public boolean isTargetRight() {
        return getRelativeAngular() <= 0;
//        return getRelativeAngular() <= -45;
    }

    public boolean isTargetFrontLeft() {
        return getRelativeAngular() >= 22;
//        return getRelativeAngular() >= 45;
    }

    public boolean isTargetFrontRight() {
        return getRelativeAngular() <= -22;
//        return getRelativeAngular() <= -45;
    }

    public boolean isTargetLeftmost() {
        return getRelativeAngular() >= 45;
//        return getRelativeAngular() >= 45;
    }

    public boolean isTargetRightmost() {
        return getRelativeAngular() <= -45;
//        return getRelativeAngular() <= -45;
    }

    public int isMemoryVector() {
        return this.getGPSVectorMemory(this.getGPSVector());
    }

    public int isMemoryGPS(Point3D current) {
        return this.getGPSMemory(current);
    }

    public int isMemoryGPSVector(SimpleVector3D current) {
        return this.getGPSVectorMemory(current);
    }

    public void findCourseTo(Point3D dest) {
        Choice root, destination;
        root = new Choice("");
        root.setPosition(this.getGPS());
        destination = new Choice("");
        destination.setPosition(dest);
        AStar pathfinder = new AStar(this.getWorldMap());
        pathfinder.setMaxSeconds(30);
        pathfinder.setMaxDepth(300);
        pathfinder.setMinlevel(this.getMinlevel());
        pathfinder.setMaxlevel(this.getMaxlevel());
        pathfinder.setType(Search.PathType.ROAD);
        Plan p = pathfinder.SearchLowest(root, destination);
        this.cleanCourse();
        for (Choice c : p) {
            this.addCourse(c.getPosition());
        }
        this.activateCourse();
    }

    public ThingSet getFullCadastre() {
        return cadastre;
    }

    public ThingSet getCensus() {
        return census;
    }

    public String[] getCitiesAround(int radius) {
        ArrayList<String> citiesaround = new ArrayList();
        Point3D mypos = this.getGPS();
        for (String s : getCityList()) {
            try {
                if (getCityPosition(s).planeDistanceTo(mypos) <= radius) {
                    citiesaround.add(s);
                }
            } catch (Exception ex) {
                System.out.println("EX");
            }
        }
        return Transform.toArrayString(citiesaround);
    }

    public String[] getCityList() {
        return Transform.toArrayString(new ArrayList(Transform.toArrayList(this.getSensor(Sensors.CITIES))));
    }

    public Point3D getCityPosition(String city) {
        if (new ArrayList(Transform.toArrayList(this.getSensor(Sensors.CITIES))).contains(city)) {
            ArrayList<String> positions = new ArrayList(Transform.toArrayList(this.getSensor(Sensors.CITIESPOSITIONS)));
            for (String scity : positions) {
                if (scity.split(Mission.sepMissions)[0].equals(city)) {
                    return new Point3D(scity.split(Mission.sepMissions)[1]);
                }
            }
            return null;
        } else {
            return null;
        }
    }

}
