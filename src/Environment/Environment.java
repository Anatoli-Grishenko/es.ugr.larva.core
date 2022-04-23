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
import java.util.ArrayList;
import world.Perceptor;
import world.SensorDecoder;
import world.World;
import world.liveBot;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Environment {

    protected SensorDecoder Perceptions;
    protected World World;
    protected ArrayList<Environment> Memory;
    protected int x = Perceptor.NULLREAD, y = Perceptor.NULLREAD, z = Perceptor.NULLREAD,
            ground = Perceptor.NULLREAD, compass = Perceptor.NULLREAD, altitude = Perceptor.NULLREAD,
            nsteps = Perceptor.NULLREAD, maxlevel = Perceptor.NULLREAD, incrx, incry, incrz,
            worldWidth = Perceptor.NULLREAD, worldHeight = Perceptor.NULLREAD;
    protected double energy = Perceptor.NULLREAD, distance = Perceptor.NULLREAD, angular = Perceptor.NULLREAD,
            relativeangular = Perceptor.NULLREAD;
    protected boolean alive, ontarget;
    protected Point3D position;
    protected SimpleVector3D gpsVector;
    protected int[][] visualData, lidarData, thermalData;
    protected int[] shortPolar;
    protected String cargo[];

    public Environment() {
        Perceptions = new SensorDecoder();
        Memory = new ArrayList();
        shortPolar = new int[5];
    }

    public Environment(Environment other) {
        Perceptions = new SensorDecoder();
        String saux = other.getPerceptions().toJson().toString();
        JsonObject jsaux = Json.parse(saux).asObject();
        this.Perceptions.fromJson(jsaux.get("perceptions").asArray());
        cache();
        Memory = new ArrayList();

    }

    public SensorDecoder getPerceptions() {
        return Perceptions;
    }

    public Environment setExternalPerceptions(String perceptions) {
        Perceptions.feedPerception(perceptions);
        cache();
        return this;
    }

    public Environment simmulate(Choice a) {
        double y, x;
        String action = a.getName();
        Environment result = new Environment();
        result.setX(this.getX());
        result.setY(this.getY());
        result.setZ(this.getZ());
        result.gpsVector=this.getGPSVector();
        result.position= this.getPosition();
        result.gpsVector = this.getGPSVector();
        result.compass = this.getCompass();
        result.altitude = this.getAltitude();
        result.ground = this.getGround();
        result.energy = this.getEnergy();
        result.maxlevel = this.getMaxlevel();
        result.nsteps = this.getNsteps() + 1;
        result.distance= this.getDistance();
        result.incrx = 0;
        result.incry = 0;
        result.worldWidth = this.getWorldWidth();
        result.worldHeight = this.getWorldHeight();
        switch (action.toUpperCase()) {
            case "MOVE":
                result.incrx = (int) Compass.SHIFT[this.getCompass() / 45].moduloX();
                result.incry = (int) Compass.SHIFT[this.getCompass() / 45].moduloY();
                result.setX(this.getX() + result.incrx);
                result.setY(this.getY() + result.incry);
                result.ground = this.getAltitude() - this.getVisualFront();
                result.distance = this.getThermalFront() / 100.0;
                result.energy = result.getEnergy() - 1;
                result.position = new Point3D(result.getX(), result.getY(), result.getZ());
                result.gpsVector = new SimpleVector3D(result.position, this.getGPSVector().getsOrient());
                y = this.getDistance() * Math.sin(Math.toRadians(this.getRelativeangular() + 90));
                x = this.getDistance() * Math.cos(Math.toRadians(this.getRelativeangular() + 90));
                result.relativeangular = Math.toDegrees(Math.acos((y + incry) / result.getDistance()));
                break;
            case "LEFT":
                result.compass = (45 + getCompass()) % 360;
                result.energy = result.getEnergy() - 1;
                result.gpsVector = new SimpleVector3D(result.position, result.compass/45);
                result.relativeangular = (this.relativeangular+45)%360;
                break;
            case "RIGHT":
                result.compass = (315 + getCompass()) % 360;
                result.energy = result.getEnergy() - 1;
                result.gpsVector = new SimpleVector3D(result.position, result.compass/45);
                result.relativeangular = (this.relativeangular+270)%360;
                break;
            case "UP":
                result.setZ(result.getZ() + 5);
                result.altitude = result.getAltitude() + 5;
                result.ground = result.getAltitude() + 5;
                result.energy = result.getEnergy() - 5;
                result.position = new Point3D(result.getX(), result.getY(), result.getZ());
                result.gpsVector = new SimpleVector3D(result.position, this.getGPSVector().getsOrient());
                break;
            case "DOWN":
                result.setZ(result.getZ() - 5);
                result.altitude = result.getAltitude() - 5;
                result.ground = result.getAltitude() - 5;
                result.energy = result.getEnergy() - 5;
                result.position = new Point3D(result.getX(), result.getY(), result.getZ());
                result.gpsVector = new SimpleVector3D(result.position, this.getGPSVector().getsOrient());
                break;
            case "IDLE":
                break;
            case "RECHARGE":
                if (result.getGround() == 0) {
                    result.energy = liveBot.MAXENERGY;
                }
                break;
            case "CAPTURE":
                if (result.getGround() == 0 && Transform.centroid(thermalData, 0, 0, -1) == 0) {
                    result.cargo = new String[]{"SOMETHING"};
                }
                break;
            default:
        }
        result.thermalData = Transform.shift(this.thermalData, result.incrx, result.incry, Perceptor.NULLREAD);
        result.visualData = Transform.shift(this.visualData, result.incrx, result.incry, Perceptor.NULLREAD);
        result.lidarData = Transform.shift(this.lidarData, result.incrx, result.incry, Perceptor.NULLREAD);
        result.alive = (result.getEnergy() > 0 && result.getGround() >= 0);
        result.ontarget = Transform.centroid(result.thermalData, 0, 0, -1) == 0;
        return result;
    }

    public void cache() {
        lidarData = Perceptions.getLidarData();
        visualData = Perceptions.getVisualData();
        thermalData = Perceptions.getThermalData();
        setX(Perceptions.getGPSPosition().getXInt());
        setY(Perceptions.getGPSPosition().getYInt());
        setZ(Perceptions.getGPSPosition().getZInt());
        this.position = Perceptions.getGPSPosition();
        this.gpsVector = Perceptions.getGPSVector();
        this.altitude=getZ();
        this.ground = Perceptions.getGround();
        this.compass=Perceptions.getCompass();
        this.energy = Perceptions.getEnergy();
        this.distance = Perceptions.getDistance();
        this.angular  = Perceptions.getAbsoluteAngular();
        this.relativeangular = Perceptions.getRelativeAngular();
        this.maxlevel= Perceptions.getMaxlevel();
        setNsteps(Perceptions.getNSteps());
        this.cargo = Perceptions.getCargo();
        setOntarget(Perceptions.getOnTarget());
        setAlive(Perceptions.getAlive());
        this.worldWidth = Perceptions.getWorldMap().getWidth();
        this.worldHeight = Perceptions.getWorldMap().getHeight();
        incrx = (int) Compass.SHIFT[this.getCompass() / 45].moduloX();
        incry = (int) Compass.SHIFT[this.getCompass() / 45].moduloY();
        incrz = 5;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getGround() {
        return ground;
    }

    public int getCompass() {
        return compass;
    }

    public double getEnergy() {
        return energy;
    }

    public double getDistance() {
        return distance;
    }

    public double getAngular() {
        return angular;
    }

    public double getRelativeangular() {
        return relativeangular;
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

    public int getAltitude() {
        return altitude;
    }

    public int[][] getShortLidar() {
        return this.getShortGeneral(lidarData);
    }

    public int[][] getShortVisual() {
        return this.getShortGeneral(visualData);
    }

    public int getNsteps() {
        return nsteps;
    }

    public void setNsteps(int nsteps) {
        this.nsteps = nsteps;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isOntarget() {
        return ontarget;
    }

    public void setOntarget(boolean ontarget) {
        this.ontarget = ontarget;
    }

    public int getMaxlevel() {
        return maxlevel;
    }

    public void setMaxlevel(int maxlevel) {
        this.maxlevel = maxlevel;
    }

    public int getWorldWidth() {
        return this.worldWidth;
    }

    public int getWorldHeight() {
        return this.worldHeight;
    }

    public String[] getCargo() {
        return cargo;
    }

    public int getNSensors() {
        return Perceptions.getSensorList().length;
    }

    public Point3D getPosition() {
        return position;
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

    public int[][] getPolarGeneral(int[][] data) {
        int initial[][] = data, res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()));
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyPolarTo(initial);
        return res;
    }

    public int[][] getAbsoluteGeneral(int[][] data) {
        int initial[][] = data, res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()));
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyAbsoluteTo(initial);
        return res;
    }

    public int[][] getRelativeGeneral(int[][] data) {
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

    public int getLeftmostGeneral(int[][] data) {
        return Transform.centroid(data, (int) Compass.SHIFT[Compass.Left(Compass.Left(this.getCompass())) / 45].moduloX(),
                (int) Compass.SHIFT[Compass.Left(Compass.Left(this.getCompass())) / 45].moduloY(), Perceptor.NULLREAD);
    }

    protected int getRightGeneral(int[][] data) {
        return Transform.centroid(data, (int) Compass.SHIFT[Compass.Right(this.getCompass()) / 45].moduloX(),
                (int) Compass.SHIFT[Compass.Right(this.getCompass()) / 45].moduloY(), Perceptor.NULLREAD);
    }

    public int getRightmostGeneral(int[][] data) {
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

    private SimpleVector3D getGPSVector() {
        return this.gpsVector;
    }

    public int[][] getAbsoluteVisual(){
        return this.getAbsoluteGeneral(visualData);
    }
    
    public int[][] getAbsoluteLidar(){
        return this.getAbsoluteGeneral(lidarData);
    }
    
    public int[][] getAbsoluteThermal(){
        return this.getAbsoluteGeneral(thermalData);
    }
    
    public int[][] getPolarVisual(){
        return this.getPolarGeneral(visualData);
    }
    
    public int[][] getPolarLidar(){
        return this.getPolarGeneral(lidarData);
    }
    
    public int[][] getPolarThermal(){
        return this.getPolarGeneral(thermalData);
    }
    public int[][] getRelativeVisual(){
        return this.getRelativeGeneral(visualData);
    }
    
    public int[][] getRelativeLidar(){
        return this.getRelativeGeneral(lidarData);
    }
    
    public int[][] getRelativeThermal(){
        return this.getRelativeGeneral(thermalData);
    }
    
}
