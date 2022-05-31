/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import geometry.Point3D;
import world.Thing;
import world.World;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import data.Transform;
import geometry.Compass;
import geometry.Entity3D;
import geometry.SimpleVector3D;
import geometry.Vector3D;
import glossary.Roles;
import glossary.Sensors;
import glossary.capability;
import glossary.direction;
import java.awt.Color;
import java.util.ArrayList;
import map2D.Map2DColor;
import static glossary.Roles.HUMMER;

/**
 *
 * @author lcv. TO remove
 */
public class liveBot extends Thing {

    String groupname;
    Point3D origin, destination;
    ArrayList<String> capabilities, attachments;
//    JsonObject lastPerceptions;
    Color colorcode;
    public String relpywith;
    int initialDistance = -1, currentDistance, order, slope;
    int energyBurnt = -1, timeSecs = 0;
    String myCommitment = "";

    public liveBot(String name) {
        super(name);
        capabilities = new ArrayList<>();
        attachments = new ArrayList<>();
        myPerceptions = new SensorDecoder();
        myPerceptions.setName(name);
        setSlope(0);
    }

    public liveBot(String name, World w) {
        super(name, w);
        capabilities = new ArrayList<>();
        attachments = new ArrayList<>();
        myPerceptions = new SensorDecoder();
        myPerceptions.setName(name);
        setSlope(0);
    }

    @Override
    public void setType(String type) {
        super.setType(type);
        this.Raw().configureType(type);
    }

    public JsonObject toXUIJson() {
        JsonObject jsdrone = this.myPerceptions.toJson(new Sensors[]{Sensors.NAME, Sensors.TEAM, Sensors.ONTARGET,
            Sensors.ALIVE, Sensors.GPS, Sensors.ENERGY, Sensors.GROUND, Sensors.DISTANCE,
            Sensors.ANGULAR, Sensors.TARGET, Sensors.COURSE, Sensors.CARGO, Sensors.TRACE});

        return jsdrone;
    }

    @Override
    public String toString() {
        return toXUIJson().toString();
    }

    @Override
    public void readPerceptions() {
        super.readPerceptions();
        checkStatus();
    }

    public liveBot addToSensor(Sensors s, String what) {
        ArrayList<String> sensorreading = new ArrayList(Transform.toArrayList(this.Raw().getSensor(s)));
        sensorreading.add(what);
        this.Raw().encodeSensor(s, Transform.toJsonArray(new ArrayList(sensorreading)));
        return this;
    }

    public ArrayList<String> getCapabilities() {
        return new ArrayList(Transform.toArrayListString(this.Raw().getSensor(Sensors.CAPABILITIES)));
    }

    public int getInitialDistance() {
        return initialDistance;
    }

    public void setInitialDistance(int initialDistance) {
        this.initialDistance = initialDistance;
    }

    public int getCurrentDistance() {
        return currentDistance;
    }

    public void setCurrentDistance(int currentDistance) {
        this.currentDistance = currentDistance;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Point3D getOrigin() {
        return origin;
    }

    public void setOrigin(Point3D origin) {
        this.origin = origin;
    }

    public JsonObject getLastPerceptions() {
        return Raw().toJson();
    }

    public String getRelpywith() {
        return relpywith;
    }

    public void setRelpywith(String relpywith) {
        this.relpywith = relpywith;
    }

    public Point3D getDestination() {
        return destination;
    }

    public void setDestination(Point3D destination) {
        this.destination = destination;
        this.myPerceptions.setDestination(destination);
    }

    public ArrayList<String> getAttachments() {
        return attachments;
    }

    public void addAttachments(String attachment) {
        this.attachments.add(attachment);
    }

    public liveBot move(Vector3D shift) {
        getVector().plus(shift.canonical().getTarget());
        Raw().setEnergy(Raw().getEnergy() - Raw().getBurnratemove());
        Raw().setEnergyburnt(Raw().getEnergyburnt() + Raw().getBurnratemove());
        return this;
    }

    public liveBot moveForward(int units) {
        setSlope(Raw().getPolarLidar()[2][1]);
        Raw().setEnergy(Raw().getEnergy() - Raw().getBurnratemove());
        Raw().setEnergyburnt(Raw().getEnergyburnt() + Raw().getBurnratemove());
        return move(this.getVector().canonical().clone().scalar(units));
    }

    public liveBot moveUp(int units) {
        setSlope(5);

        Raw().setEnergy(Raw().getEnergy() - Raw().getBurnratemove());
        Raw().setEnergyburnt(Raw().getEnergyburnt() + Raw().getBurnratemove());
        return move(Compass.SHIFT[direction.UP.ordinal()].clone().scalar(units));
    }

    public liveBot moveDown(int units) {
        setSlope(-5);
        Raw().setEnergy(Raw().getEnergy() - Raw().getBurnratemove());
        Raw().setEnergyburnt(Raw().getEnergyburnt() + Raw().getBurnratemove());
        return move(Compass.SHIFT[direction.DOWN.ordinal()].clone().scalar(units));
    }

    private liveBot RotateXY(double degrees) {
//        Vector3D orientation=getOrientation().canonical();
//        double radio=orientation.modulo();
//        orientation=new Vector3D()
//        setOrientation(getOrienta)
//        base.define(L.getPosition().getX()+radio*Math.cos(i*Math.PI/180),L.getPosition().getY()-radio*Math.sin(i*Math.PI/180));
        return this;
    }

    public static int rotateLeft(int sdirection) {
        return (sdirection + 1) % 8;
    }

    public static int rotateRight(int sdirection) {
        return (sdirection + 7) % 8;
    }

    public static int Opposite(int sdirection) {
        return (sdirection + 4) % 8;
    }

    public liveBot rotateLeft() {
        setSlope(0);
        setOrientation(liveBot.this.rotateLeft(getOrientation()));
        return this;
    }

    public liveBot rotateRight() {
        setSlope(0);
        setOrientation(liveBot.this.rotateRight(getOrientation()));
        return this;
    }

    public liveBot recharge() {
        Raw().setEnergy(Raw().getAutonomy());
        return this;
    }

    public boolean isGoal() {
        if (Raw().getTarget() == null) {
            return false;
        }
        return Raw().getGPS().isEqualTo(Raw().getTarget());
    }

    protected void checkStatus() {
        boolean single = false, multiple = true, crashtotoher,
                crashtoground, crashtolevel, crashtoenergy, crashtoborder, crashtoslope;
        
        Raw().setStatus("");
        Raw().setEnergy((int) Math.max(Raw().getEnergy(), 0));
        crashtoenergy = Raw().getEnergy() < 1;
        if (crashtoenergy) {
            Raw().addStatus("Energy exhausted. ");
        }

        crashtoground = getPosition().getZ() < this.getWorld().getEnvironment().getSurface().getStepLevel(getPosition().getX(), getPosition().getY());
        if (crashtoground) {
            Raw().addStatus("Crash onto the ground. ");
        }
        crashtoborder = getPosition().getX() < 0 || getPosition().getX() >= this.getWorld().getEnvironment().getSurface().getWidth()
                || getPosition().getY() < 0 || getPosition().getY() >= this.getWorld().getEnvironment().getSurface().getHeight();
        if (crashtoborder) {
            Raw().addStatus("Crash onto world's boundaries. ");
        }
        crashtolevel = getPosition().getZ() > Raw().getMaxlevel()
                || getPosition().getZ() < Raw().getMinlevel();
        if (crashtolevel) {
            Raw().addStatus("Out ot operational altitude limits. ");
        }
        crashtoslope = getSlope() < -Raw().getMaxslope() || getSlope() > Raw().getMaxslope();
        if (crashtoslope) {
            Raw().addStatus("Max allowed slope exceeded!");
        }
        single = (!(crashtoenergy
                || crashtolevel
                || crashtoborder
                || crashtoslope
                || crashtoground));
        Raw().setAlive(single);
        Raw().setOntarget(isGoal());
        setCurrentDistance((int) Raw().getDistance());
        if (getInitialDistance() < 0) {
            setInitialDistance(getCurrentDistance());
        }

    }

    public void printSensorIndex() {
        System.out.println("[" + this.getName() + "] -->" + this.Raw().indexperception.keySet().toString());
    }

    public int getSlope() {
        return slope;
    }

    public void setSlope(int slope) {
        this.slope = slope;
    }

}
