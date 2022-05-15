/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Environment;

import ai.Choice;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import data.Transform;
import geometry.Compass;
import geometry.Point3D;
import geometry.PolarSurface;
import geometry.SimpleVector3D;
import glossary.Roles;
import glossary.Sensors;
import java.util.ArrayList;
import world.Perceptor;
import world.SensorDecoder;
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

    public Environment() {
        super();
        shortPolar = new int[5];
        World = new World("innerworld");
    }

    public Environment(Environment other) {
        super();
        String saux = other.toJson().toString();
        JsonObject jsaux = Json.parse(saux).asObject();
        this.fromJson(jsaux.get("perceptions").asArray());
        cache();

    }

    public boolean loadWorld(String worldname, String name, Roles r, Sensors attach[]) {
        if (!World.loadConfig("./worlds/" + worldname + ".worldconf.json").equals("ok")) {
            return false;
        }
        if (!loadWorldMap(World.getEnvironment().getSurface())) {
            return false;
        }
        this.setTarget(World.getThingByName("Guybrush Threepwood").getPosition());
        this.live = World.registerAgent(name, r.name(), attach);
//        System.out.println(live.getPerceptions().toString());
//        feedPerception(this.live.getPerceptions());
        return true;
    }

    public Environment setExternalPerceptions(String perceptions) {
        feedPerception(perceptions);
        cache();
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
            res.encodeSensor(sname, Json.parse(getSensor(sname).toString()).asArray());
        }
        res.hMap = this.hMap;
        return res;
    }

    public Environment simmulate(Choice a) {
        double y, x, dincrx, dincry;
        String action = a.getName();
        Environment result = this.clone();

        switch (action.toUpperCase()) {
            case "MOVE":
//                dincrx = (int) Compass.SHIFT[this.getCompass() / 45].moduloX();
//                dincry = (int) Compass.SHIFT[this.getCompass() / 45].moduloY();
//                result.setGPS(new Point3D(this.getGPSMemory().getX()+dincrx,getGPSMemory().getY()+dincry,getGPSMemory().getZ()));
                result.setGPS(this.getGPSComingPosition());
                result.setGround((int) (this.getAltitude() - this.getVisualFront()));
                result.setEnergy(this.getEnergy() - 1);
                break;
            case "LEFT":
//                result.setCompass((45 + getCompass()) % 360);
                result.setCompass(this.getCompassLeft());
                result.setEnergy(this.getEnergy() - 1);
                break;
            case "RIGHT":
//                result.setCompass((270 + getCompass()) % 360);
                result.setCompass(this.getCompassRight());
                result.setEnergy(this.getEnergy() - 1);
                break;
            case "UP":
                result.setGPS(this.getGPSComingPosition());
                result.setEnergy(this.getEnergy() - 1);
                break;
            case "DOWN":
                result.setGPS(this.getGPSComingPosition());
                result.setEnergy(this.getEnergy() - 1);
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
        int incrx = (int) Compass.SHIFT[this.getCompass() / 45].moduloX();
        int incry = (int) Compass.SHIFT[this.getCompass() / 45].moduloY();
        result.thermalData = Transform.shift(this.thermalData, incrx, incry, Perceptor.NULLREAD);
        result.visualData = Transform.shift(this.visualData, incrx, incry, Perceptor.NULLREAD);
        result.lidarData = Transform.shift(this.lidarData, incrx, incry, Perceptor.NULLREAD);
        return result;
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
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()));
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyPolarTo(initial);
        return res;
    }

    protected int[][] getAbsoluteGeneral(int[][] data) {
        int initial[][] = data, res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()));
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyAbsoluteTo(initial);
        return res;
    }

    protected int[][] getRelativeGeneral(int[][] data) {
        int initial[][] = data, res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length / 2 + 1;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh - 1, myv.getsOrient()));
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

    public boolean isEnergyExhausted() {
        return this.getEnergy() < 0;
    }

    public boolean isFreeFront() {
        return this.getPolarLidar()[2][1] >= 0;
//        return this.getLidarFront() >= 0;
    }

    public boolean isFreeFrontLeft() {
        return this.getPolarLidar()[1][1] >= 0;
//        return this.getLidarLeft() >= 0;
    }

    public boolean isFreeFrontRight() {
        return this.getPolarLidar()[3][1] >= 0;
//        return this.getLidarRight() >= 0;
    }

    public boolean isFreeLeft() {
        return this.getLidarLeftmost() >= 0;
    }

    public boolean isFreeRight() {
        return this.getLidarRightmost() >= 0;
    }

    public boolean isTargetFront() {
        return getRelativeAngular() > -45 && getRelativeAngular() < 45;
    }

    public boolean isTargetLeft() {
        return getRelativeAngular() >= 45;
    }

    public boolean isTargetRight() {
        return getRelativeAngular() <= -45;
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
}
