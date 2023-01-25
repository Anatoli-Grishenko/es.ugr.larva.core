/**
 * @file Environment.java
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
import java.util.Collections;
import profiling.Profiler;
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
    protected liveBot live;
    // Remote execution
    protected int[][] visualData, lidarData, thermalData;
    protected int[] shortPolar;
    protected ThingSet cadastre;
    protected ThingSet census;
    protected int slope;
    protected LARVAFirstAgent ownerHook;
    protected Profiler refProfiler;

//    public Environment() {
//        super();
////        shortPolar = new int[5];
////        World = new World("innerworld");
//        cadastre = new ThingSet();
//        census = new ThingSet();
//    }
    public Environment(LARVAFirstAgent owner) {
        super();
        ownerHook = owner;
//        shortPolar = new int[5];
//        World = new World("innerworld");
        cadastre = new ThingSet();
        census = new ThingSet();
        refProfiler = owner.getMyCPUProfiler();
    }

    protected Environment(Environment other) {
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

    /**
     * This method allows the integration of the perceptions coming from the
     * server into the Environment and its further reading through specific
     * methods
     *
     * @param perceptions A binary (zip'ed) String which is the content of the
     * message received anfter reading the perceptoins
     * @return A copy of the same instance (to chain methods calls)
     */
    public Environment setExternalPerceptions(String perceptions) {

        try {
// Profiling: this is irrelevant: +- 1 ms always
            refProfiler.profileThis("READPERCEPTIONS", "" + perceptions.length(),
                    () -> {
                        feedPerception(perceptions);
                        if (this.getGPSMemorySize() > 1) {
                            slope = this.getGPS().getZInt() - this.getGPSMemory(1).getZInt();
                        } else {
                            slope = 0;
                        }
                        cache();
                    });
            return this;
        } catch (Exception ex) {
            return null;
        }
    }

    public Environment setExternalObjects(String things) {
        try {
            refProfiler.profileThis("READEXTERNALOBJECTS", "" + things.length(),
                    () -> {
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
                    });
        } catch (Exception ex) {

        }
        return this;
    }

    protected Environment addThings(ArrayList<Thing> listT) {
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

    protected Environment readInternalPerceptions() {
//        System.out.println(live.getPerceptions().toString());
        feedPerception(live.getPerceptions());
//        feedPerception(live.Raw().toJson());
        cache();
        return this;
    }

    protected boolean executeInternalAction(String action) {
        return World.execAgent(live, action);
    }

    protected boolean isEquivalentTo(Environment other) {
        return (other.getGPS().getX() == this.getGPS().getX() && other.getGPS().getY() == this.getGPS().getY() && other.getGPS().getZ() == this.getGPS().getZ());
    }

    @Override
    public Environment clone() {
        Environment res = new Environment(ownerHook);
        for (Sensors sname : this.indexperception.keySet()) {
            if (getSensor(sname) != null) {
                res.encodeSensor(sname, Json.parse(getSensor(sname).toString()).asArray());
            }
        }
        res.hMap = this.hMap;
        return res;
    }

    /**
     * This is a powerful method and it is able to simmulate the execution of an
     * action, given as a CHoice, into tht current instance of Environment
     *
     * @param a The action to execute
     * @return A copy of the instance
     */
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
//            result.thermalData = Transform.shift(this.thermalData, incrx, incry, Perceptor.NULLREAD);
//            result.encodeSensor(Sensors.THERMAL, Transform.Matrix2JsonArray(result.thermalData));
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

    protected boolean willBeAlive() {
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

    /**
     * It returns the value of the lidar just in front of the agent. Coordenadas
     * polares x=2, y=1;
     *
     * @return
     */
    public int getLidarFront() {
        return getFrontGeneral(lidarData);
    }

    /**
     * It returns the value of lidar in just at the left. Coordenadas polares
     * x=1, y=1;
     *
     * @return
     */
    public int getLidarLeft() {
        return getLeftGeneral(lidarData);
    }

    /**
     * It returns the value of lidar at thev very left. Coordenadas polares x=0,
     * y=1;
     *
     * @return
     */
    public int getLidarLeftmost() {
        return getLeftmostGeneral(lidarData);
    }

    /**
     * It returns the value of lidar at the right. Coordenadas polares x=3, y=1;
     *
     * @return
     */
    public int getLidarRight() {
        return getRightGeneral(lidarData);
    }

    /**
     * It returns the value of lidar at the very right. Coordenadas polares x=4,
     * y=1;
     *
     * @return
     */
    public int getLidarRightmost() {
        return getRightmostGeneral(lidarData);
    }

    /**
     * It returns the value of the visual just in front of the agent.
     * Coordenadas polares x=2, y=1;
     *
     * @return
     */
    public int getVisualFront() {
        return getFrontGeneral(visualData);
    }

    /**
     * It returns the value of visual in just at the left. Coordenadas polares
     * x=1, y=1;
     *
     * @return
     */
    public int getVisualLeft() {
        return getLeftGeneral(visualData);
    }

    /**
     * It returns the value of visual at thev very left. Coordenadas polares
     * x=0, y=1;
     *
     * @return
     */
    public int getVisualLeftmost() {
        return getLeftmostGeneral(visualData);
    }

    /**
     * It returns the value of visual at the very right. Coordenadas polares
     * x=4, y=1;
     *
     * @return
     */
    public int getVisualRight() {
        return getRightGeneral(visualData);
    }

    /**
     * It returns the value of visual at the very right. Coordenadas polares
     * x=4, y=1;
     *
     * @return
     */
    public int getVisualRightmost() {
        return getRightmostGeneral(visualData);
    }

//    public int getThermalFront() {
//        return getFrontGeneral(thermalData);
//    }
//
//    public int getThermalLeft() {
//        return getLeftGeneral(thermalData);
//    }
//
//    public int getThermalLeftmost() {
//        return getLeftmostGeneral(thermalData);
//    }
//
//    public int getThermalRight() {
//        return getRightGeneral(thermalData);
//    }
//
//    public int getThermalRightmost() {
//        return getRightmostGeneral(thermalData);
//    }
//
//    public int getThermalHere() {
//        return getHereGeneral(thermalData);
//    }
    /**
     * It returns the value of visual just in the position of the agent.
     * Coordenadas polares x=0, y=0;
     *
     * @return
     */
    public int getVisualHere() {
        return getHereGeneral(visualData);
    }

    /**
     * It returns the value of lidar just in the position of the agent.
     * Coordenadas polares x=0, y=0;
     *
     * @return
     */
    public int getLidarHere() {
        return getHereGeneral(lidarData);
    }

    /**
     * It returns true when the agent is not alive
     *
     * @return
     */
    public boolean isCrahsed() {
        return !this.getAlive();
    }

    protected int getSlope() {
        return slope;

    }

    /**
     * It returns true when energy == 0
     *
     * @return
     */
    public boolean isEnergyExhausted() {
        return this.getEnergy() < 0;
    }

    /**
     * It checks that the cell just in front of the agent is available to move,
     * that is the slope is valid (taking into account maxSlope()) and the
     * height is valid too (taking into account minLevel() and maxLevel()
     *
     * @return true if the agent can move forwards, false otherwise
     */
    public boolean isFreeFront() {
        return this.getLidarFront() >= -getMaxslope() && getLidarFront() <= getMaxslope()
                && getVisualFront() >= this.getMinlevel()
                && getVisualFront() <= this.getMaxlevel();
    }

//    public boolean isFreeFront() {
//        int slopeFront, visualFront, visualHere;
//
//        // Polar sensors
//        //visualHere= this.getPolarVisual()[0][0];   
//        
//        // Check the height in front of me
//        visualFront = this.getPolarVisual()[2][1];
//        
//        // Check the slope in front of me
//        slopeFront = this.getPolarLidar()[2][1];
//        
//        return slopeFront >= -getMaxslope() && slopeFront <= getMaxslope()
//                && visualFront >= this.getMinlevel()
//                && visualFront <= this.getMaxlevel();
//    }
//
//    public boolean isFreeFront() {
//        int slopeFront, visualFront, visualHere;
//
//        // Polar sensors
//        visualHere= this.getPolarVisual()[0][0];   
//        
//        // Check the height in front of me
//        visualFront = this.getPolarVisual()[2][1];
//        
//        // Check the slope in front of me
//        slopeFront = visualHere-visualFront;
//        
//        return slopeFront >= -getMaxslope() && slopeFront <= getMaxslope()
//                && visualFront >= this.getMinlevel()
//                && visualFront <= this.getMaxlevel();
//    }
//
//    public boolean isFreeFront() {
//        int slopeFront, visualFront, visualHere;
//
//        // Relative sensors
//        visualHere= this.getRelativeVisual()[getRange() / 2][getRange() / 2];   
//        
//        // Check the height in front of me
//        visualFront = this.getRelativeVisual()[getRange() / 2][getRange() / 2 - 1];
//        
//        // Check the slope in front of me
//        slopeFront = visualHere-visualFront;
//        
//        return slopeFront >= -getMaxslope() && slopeFront <= getMaxslope()
//                && visualFront >= this.getMinlevel()
//                && visualFront <= this.getMaxlevel();
//    }
//
//    public boolean isFreeFront() {
//        int lidarFront, visualFront, visualHere;
//
//        // Simplest
//        visualHere= this.getAbsoluteVisual()[getRange() / 2][getRange() / 2];   // Percepción absoluta
////        visualHere= this.getRelativeVisual()[getRange() / 2][getRange() / 2];   // Percepción relativa
////        visualHere= this.getPolarVisual()[0][0];                                // Percepción polar
//        
//        // Check the height in front of me
//        visualFront = this.getRelativeVisual()[getRange() / 2][getRange() / 2 - 1];
////        visualFront = this.getPolarVisual()[1][2];
//        
//        // Check the slope in front of me
//        lidarFront = visualFront-visualHere;
////        lidarFront = this.getRelativeLidar()[getRange() / 2][getRange() / 2 - 1];
////        lidarFront = this.getPolarLidar()[1][2];
//
//        // Use macros
////        lidarFront = this.getLidarFront();
////        visualFront = this.getVisualFront();
//
//        return lidarFront >= -getMaxslope() && lidarFront <= getMaxslope()
//                && visualFront >= this.getMinlevel()
//                && visualFront <= this.getMaxlevel();
//    }
//
    //        return this.getPolarLidar()[1][2] >= -getMaxslope() && this.getPolarLidar()[1][2] <= getMaxslope()
//                && this.getPolarVisual()[1][2] >= this.getMinlevel()
//                && this.getPolarVisual()[1][2] <= this.getMaxlevel();
//        return this.getLidarFront() >= -getMaxslope() && getLidarFront() <= getMaxslope()
//                && getVisualFront() >= this.getMinlevel()
//                && getVisualFront() <= this.getMaxlevel();
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

    protected int isMemoryVector() {
        return this.getGPSVectorMemory(this.getGPSVector());
    }

    protected int isMemoryGPS(Point3D current) {
        return this.getGPSMemory(current);
    }

    protected int isMemoryGPSVector(SimpleVector3D current) {
        return this.getGPSVectorMemory(current);
    }

    public ThingSet getFullCadastre() {
        return cadastre;
    }

    protected ThingSet getCensus() {
        return census;
    }

    public String[] getCitiesAround(int radius) {
        ArrayList<String> citiesaround = new ArrayList();
        Point3D mypos = this.getGPS();
        return getCitiesAround(mypos, radius);
//        if (mypos != null) {
//            for (String s : getCityList()) {
//                try {
//                    if (getCityPosition(s).planeDistanceTo(mypos) <= radius) {
//                        citiesaround.add(s);
//                    }
//                } catch (Exception ex) {
//                    System.out.println("EX");
//                }
//            }
//        } else {
//            return getCityList();
//        }
//        return Transform.toArrayString(citiesaround);
    }

    public String[] getCitiesAround(Point3D p, int radius) {
        ArrayList<String> citiesaround = new ArrayList();
        Point3D mypos = p;
        if (mypos != null) {
            for (String s : getCityList()) {
                try {
                    Point3D pcity = getCityPosition(s);
                    if (getGPS() != null) {
                        if (pcity.planeDistanceTo(mypos) <= radius
                                && pcity.getZ() <= getMaxlevel()
                                && pcity.getZ() >= getMinlevel()) {
                            citiesaround.add(s);
                        }
                    } else {
                        if (pcity.planeDistanceTo(mypos) <= radius) {
                            citiesaround.add(s);
                        }

                    }
                } catch (Exception ex) {
                    System.out.println("EX");
                }
            }
        } else {
            return getCityList();
        }
        Collections.shuffle(citiesaround);
        return Transform.toArrayString(citiesaround);
    }

    /**
     * It returns an array of String which contains the full list of cities in
     * the world
     *
     * @return
     */
    public String[] getCityList() {
        return Transform.toArrayString(new ArrayList(Transform.toArrayList(this.getSensor(Sensors.CITIES))));
    }

    /**
     * It returns the position of the city
     *
     * @param city The name of the city
     * @return The positin of the city, null when the city does not exist
     */
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

    /**
     * It returns the name of the current mission
     *
     * @return
     */
    @Override
    public Mission getCurrentMission() {
        return super.getCurrentMission();
    }

    /**
     * It activates the mission whose name is given. It also activates the first
     * goal in that mission
     *
     * @param missionName The name of the mission, amongst all possible names
     */
    @Override
    public void setCurrentMission(String missionName) {
        super.setCurrentMission(missionName);
        this.ownerHook.sendStealthTransponder();
    }

    @Override
    public void makeCurrentMission(String missionString) {
        super.makeCurrentMission(missionString);
        this.ownerHook.sendStealthTransponder();
    }

    /**
     * Defines a customized mission
     *
     * @param missionName The name of the mission
     * @param goals Each consecutive goal of the mission
     */
    @Override
    public void setCurrentMission(String missionName, String goals[]) {
        super.setCurrentMission(missionName, goals);
        this.ownerHook.sendStealthTransponder();
    }

    public void setCurrentMission(Mission m) {
        super.setCurrentMission(m.getName(), m.getAllGoals());
        this.ownerHook.sendStealthTransponder();
    }

    ///////////////////// GOALS
    /**
     * It gives the current goal being solved
     *
     * @return
     */
    @Override
    public String getCurrentGoal() {
        return super.getCurrentGoal();
    }

    @Override
    public void setCurrentGoal(String goal) {
        super.setCurrentGoal(goal);
        this.ownerHook.sendStealthTransponder();
    }

    /**
     * It closes the current goal, and moves to the next goal in the missoin
     *
     * @return
     */
    @Override
    public String setNextGoal() {
        String res = super.setNextGoal();
        this.ownerHook.sendStealthTransponder();
        return res;
    }

    /**
     * Whe the agent is on the ground and exactly in the position of a city, it
     * gives the name of that city:
     *
     * @return The name of the city in which the agent is on the ground. Should
     * the agent be flying or outside of a city, it returns "MOVING"
     */
    @Override
    public String getCurrentCity() {
        String res = super.getCurrentCity();
        return res;
    }

    /**
     * When a course is fixed towards a given city, it returns the name of that
     * city. Otherwise it returns "NONE"
     *
     * @return
     */
    @Override
    public String getDestinationCity() {
        return super.getDestinationCity();
    }

}
