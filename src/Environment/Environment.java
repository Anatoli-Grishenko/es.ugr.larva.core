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
    protected int[][] shortRadar, shortDistances, shortLidar, shortVisual;
    protected int[][] visualData, lidarData, thermalData;
    protected int[] shortPolar;
    protected String cargo[];

    public Environment() {
        Perceptions = new SensorDecoder();
        Memory = new ArrayList();
        shortRadar = new int[3][3];
        shortDistances = new int[3][3];
        shortLidar = new int[3][3];
        shortVisual = new int[3][3];
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
        String action = a.getName();
        Environment result = new Environment();
        result.setX(this.getX());
        result.setY(this.getY());
        result.setZ(this.getZ());
        result.setPosition(this.getPosition());
        result.setCompass(this.getCompass());
        result.setAltitude(this.getAltitude());
        result.setGround(this.getGround());
        result.setEnergy(this.getEnergy());
        result.setMaxlevel(this.getMaxlevel());
        result.setCompass(this.getCompass());
        result.setNsteps(this.getNsteps() + 1);
        result.setDistance(this.getDistance());
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
                result.setGround(this.getAltitude() - this.getVisualFront());
                result.setDistance(this.getThermalFront());
                result.setEnergy(result.getEnergy() - 1);
                result.setPosition(new Point3D(result.getX(), result.getY(), result.getZ()));
                break;
            case "LEFT":
                result.setCompass((45 + getCompass()) % 360);
                result.setEnergy(result.getEnergy() - 1);
                break;
            case "RIGHT":
                result.setCompass((315 + getCompass()) % 360);
                result.setEnergy(result.getEnergy() - 1);
                break;
            case "UP":
                result.setZ(result.getZ() + 5);
                result.setAltitude(result.getAltitude() + 5);
                result.setGround(result.getAltitude() + 5);
                result.setEnergy(result.getEnergy() - 5);
                result.setPosition(new Point3D(result.getX(), result.getY(), result.getZ()));
                break;
            case "DOWN":
                result.setZ(result.getZ() - 5);
                result.setAltitude(result.getAltitude() - 5);
                result.setGround(result.getAltitude() - 5);
                result.setEnergy(result.getEnergy() - 5);
                result.setPosition(new Point3D(result.getX(), result.getY(), result.getZ()));
                break;
            case "IDLE":
                break;
            case "RECHARGE":
                if (result.getGround() == 0) {
                    result.setEnergy(liveBot.MAXENERGY);
                }
                break;
            case "CAPTURE":
                if (result.getGround() == 0 && Transform.centroid(thermalData, 0, 0, -1) == 0) {
                    result.setCargo(new String[]{"SOMETHING"});
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
        setPosition(Perceptions.getGPSPosition());
        setAltitude(getZ());
        setGround(Perceptions.getGround());
        setCompass(Perceptions.getCompass());
        setEnergy(Perceptions.getEnergy());
        setDistance(Perceptions.getDistance());
        setAngular(Perceptions.getAbsoluteAngular());
        setRelativeangular(Perceptions.getRelativeAngular());
        setMaxlevel(Perceptions.getMaxlevel());
        setNsteps(Perceptions.getNSteps());
        setCargo(Perceptions.getCargo());
        setOntarget(Perceptions.getOnTarget());
        setAlive(Perceptions.getAlive());
        this.worldWidth = Perceptions.getWorldMap().getWidth();
        this.worldHeight = Perceptions.getWorldMap().getHeight();
        this.setShortDistances(getShort(thermalData));
        this.setShortLidar(getShort(lidarData));
        this.setShortVisual(getShort(visualData));
        this.setShortPolar(getPolar(lidarData));

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

    public void setGround(int ground) {
        this.ground = ground;
    }

    public int getCompass() {
        return compass;
    }

    public void setCompass(int compass) {
        this.compass = compass;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getAngular() {
        return angular;
    }

    public void setAngular(double angular) {
        this.angular = angular;
    }

    public double getRelativeangular() {
        return relativeangular;
    }

    public void setRelativeangular(double relativeangular) {
        this.relativeangular = relativeangular;
    }

    public int[][] getShortRadar() {
        int r[][] = getShortLidar();
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (r[x][y] < 0) {
                    r[x][y] = 1;
                } else {
                    r[x][y] = 0;
                }
            }
        }
        return r;
    }

    public void setShortRadar(int[][] shortRadar) {
        this.shortRadar = shortRadar;
    }

    public int[][] getShortDistances() {
        return shortDistances;
    }

    public void setShortDistances(int[][] shortDistances) {
        this.shortDistances = shortDistances;
    }

    public int[] getShortPolar() {
        return shortPolar;
    }

    public void setShortPolar(int[] shortPolar) {
        this.shortPolar = shortPolar;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public int[][] getShortLidar() {
        return shortLidar;
    }

    public void setShortLidar(int[][] shortLidar) {
        this.shortLidar = shortLidar;
    }

    public int[][] getShortVisual() {
        return shortVisual;
    }

    public void setShortVisual(int[][] shortVisual) {
        this.shortVisual = shortVisual;
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

    public void setCargo(String[] cargo) {
        this.cargo = cargo;
    }

    public int getNSensors() {
        return Perceptions.getSensorList().length;
    }

    public Point3D getPosition() {
        return position;
    }

    public void setPosition(Point3D position) {
        this.position = position;
    }

    protected int[][] getShort(int[][] data) {
        int result[][] = new int[3][3];
        for (int x = 0; x < result[0].length; x++) {
            for (int y = 0; y < result.length; y++) {
                result[x][y] = Transform.centroid(data, x - 1, y - 1, Perceptor.NULLREAD);
            }
        }
        return result;
    }

    protected int[] getPolar(int[][] data) {
        int result[] = new int[5];
        result[0] = getLeftmost(data);
        result[1] = getLeft(data);
        result[2] = getFront(data);
        result[3] = getRight(data);
        result[4] = getRightmost(data);
        return result;
    }

    protected int getHere(int[][] data) {
        return Transform.centroid(data, 0,0, Perceptor.NULLREAD);
    }

    protected int getFront(int[][] data) {
        return Transform.centroid(data, (int) Compass.SHIFT[this.getCompass() / 45].moduloX(),
                (int) Compass.SHIFT[this.getCompass() / 45].moduloY(), Perceptor.NULLREAD);
    }

    protected int getLeft(int[][] data) {
        return Transform.centroid(data, (int) Compass.SHIFT[Compass.Left(this.getCompass()) / 45].moduloX(),
                (int) Compass.SHIFT[Compass.Left(this.getCompass()) / 45].moduloY(), Perceptor.NULLREAD);
    }

    public int getLeftmost(int[][] data) {
        return Transform.centroid(data, (int) Compass.SHIFT[Compass.Left(Compass.Left(this.getCompass())) / 45].moduloX(),
                (int) Compass.SHIFT[Compass.Left(Compass.Left(this.getCompass())) / 45].moduloY(), Perceptor.NULLREAD);
    }

    protected int getRight(int[][] data) {
        return Transform.centroid(data, (int) Compass.SHIFT[Compass.Right(this.getCompass()) / 45].moduloX(),
                (int) Compass.SHIFT[Compass.Right(this.getCompass()) / 45].moduloY(), Perceptor.NULLREAD);
    }

    public int getRightmost(int[][] data) {
        return Transform.centroid(data, (int) Compass.SHIFT[Compass.Right(Compass.Right(this.getCompass())) / 45].moduloX(),
                (int) Compass.SHIFT[Compass.Right(Compass.Right(this.getCompass())) / 45].moduloY(), Perceptor.NULLREAD);
    }

    public int getLidarFront() {
        return getFront(lidarData);
    }

    public int getLidarLeft() {
        return getLeft(lidarData);
    }

    public int getLidarLeftmost() {
        return getLeftmost(lidarData);
    }

    public int getLidarRight() {
        return getRight(lidarData);
    }

    public int getLidarRightmost() {
        return getRightmost(lidarData);
    }

    public int getVisualFront() {
        return getFront(visualData);
    }

    public int getVisualLeft() {
        return getLeft(visualData);
    }

    public int getVisualLeftmost() {
        return getLeftmost(visualData);
    }

    public int getVisualRight() {
        return getRight(visualData);
    }

    public int getVisualRightmost() {
        return getRightmost(visualData);
    }

    public int getThermalFront() {
        return getFront(thermalData);
    }

    public int getThermalLeft() {
        return getLeft(thermalData);
    }

    public int getThermalLeftmost() {
        return getLeftmost(thermalData);
    }

    public int getThermalRight() {
        return getRight(thermalData);
    }

    public int getThermalRightmost() {
        return getRightmost(thermalData);
    }

    public int getThermalHere() {
        return getHere(thermalData);
    }

}
