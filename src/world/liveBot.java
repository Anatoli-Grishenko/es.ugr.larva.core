/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import geometry.Point;
import world.Thing;
import world.World;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import glossary.Roles;
import java.awt.Color;
import java.util.ArrayList;

/**
 *
 * @author lcv
 */
public class liveBot extends Thing {

    public static final int MAXFLIGHT = 256, MAXENERGY = 3500;
    
     String groupname;
     glossary.Roles role;
     int energylevel, burnmovement, burnsensor, compass, altitude, order;
     double distance, angle;
     Point origin;
     public int minAllowedLevel, maxAllowedLevel, range, alive, ontarget;
     ArrayList<String> capabilities, attachments;
     JsonObject lastPerceptions;
     public String statusinfo;
     ArrayList<Thing> thePayload;
     Color colorcode;
     public String lastEvent, relpywith;
     int initialDistance = -1, currentDistance;
    int energyBurnt = -1, numSteps = -1, timeSecs = 0;
    String myCommitment="";

    public liveBot(String name) {
        super(name);
        capabilities = new ArrayList<>();
        attachments = new ArrayList<>();
        thePayload = new ArrayList<>();
        statusinfo = "Fresh new";
        lastEvent = statusinfo;
    }

    public liveBot(String name, World w) {
        super(name, w);
        capabilities = new ArrayList<>();
        thePayload = new ArrayList<>();
        attachments = new ArrayList<>();
        statusinfo = "Fresh new";
        lastEvent = statusinfo;
    }

    public boolean isAtBase() {
        return getPosition().isEqualTo(origin);
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsdrone = new JsonObject();
        jsdrone.set("name", getName());
        jsdrone.set("team", groupname);
        jsdrone.set("last", lastEvent);
        jsdrone.set("ontarget", ontarget);
        jsdrone.set("alive", alive);
        jsdrone.set("x", getPosition().getX());
        jsdrone.set("y", getPosition().getY());
        jsdrone.set("z", getPosition().getZ());
        jsdrone.set("energy", energylevel);
        jsdrone.set("altitude", altitude);
        jsdrone.set("distance", distance);
        jsdrone.set("angle", angle);
        jsdrone.set("compass", this.compass);
        jsdrone.set("payload", getFullPayload());
        return jsdrone;
    }

    public void fromJson(JsonObject update) {
        _position = new Point(update.getInt("x", -1),
                update.getInt("y", -1), update.getInt("z", -1));
        energylevel = update.getInt("energy", -1);
        altitude = update.getInt("altitude", -1);
        angle = update.getDouble("angle", 0);
        distance = update.getDouble("distance", 0);
        _name = update.getString("name", "unknown");
        groupname = update.getString("team", "unknown");
        this.lastEvent = update.getString("last", "---");
        ontarget = update.getInt("ontarget", -1);
        alive = update.getInt("alive", -1);
        thePayload = new ArrayList();
        for (JsonValue jsv : update.get("payload").asArray()) {
            thePayload.add(new Thing(jsv.asString()));
        }
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    @Override

    public int getEnergy() {
        return energylevel;
    }

    @Override
    public int getOnTarget() {
        return ontarget;
    }

    @Override
    public int getAlive() {
        return alive;
    }

    @Override
    public int getPayload() {
        return thePayload.size();
    }
    
    public ArrayList<Thing> getAllPayload() {
        return thePayload;
    }
    
    public void addPayload(Thing what) {
        thePayload.add(what);
    }

    public String getStatus() {
        return this.statusinfo;
    }

    public void setStatus(String s) {
        statusinfo = s;
    }

    public JsonArray getFullPayload() {
        JsonArray jspl = new JsonArray();
        for (Thing t : thePayload) {
            jspl.add(t.getName());
        }
        return jspl;

    }

    @Override
    public Point getPosition() {
        return this._position;
    }

    public int getEnergyBurnt() {
        return energyBurnt;
    }

    public void addEnergyBurnt(int increment) {
        if (energyBurnt < 0) {
            energyBurnt = increment;
        } else {
            energyBurnt += increment;
        }
    }

    public int getNumSteps() {
        return numSteps;
    }

    public void addNumSteps(int increment) {
        if (numSteps < 0) {
            numSteps = increment;
        } else {
            numSteps += increment;
        }
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public ArrayList<String> getCapabilities() {
        return capabilities;
    }

    public void addCapabilities(String capability) {
        this.capabilities.add(capability);
    }

    public ArrayList<String> getAttachments() {
        return attachments;
    }

    public void addAttachments(String attachment) {
        this.attachments.add(attachment);
    }

    public ArrayList<Thing> getThePayload() {
        return thePayload;
    }

    public void addThePayload(Thing capture) {
        this.thePayload.add(capture);
    }

    public int getEnergylevel() {
        return energylevel;
    }

    public void setEnergylevel(int energylevel) {
        this.energylevel = energylevel;
    }

    public void burnEnergylevel(int increment) {
        this.energylevel -= increment;
    }

    public int getBurnmovement() {
        return burnmovement;
    }

    public void setBurnmovement(int burnmovement) {
        this.burnmovement = burnmovement;
    }

    public int getBurnsensor() {
        return burnsensor;
    }

    public void setBurnsensor(int burnsensor) {
        this.burnsensor = burnsensor;
    }

    public int getMinAllowedLevel() {
        return minAllowedLevel;
    }

    public void setMinAllowedLevel(int minAllowedLevel) {
        this.minAllowedLevel = minAllowedLevel;
    }

    public int getMaxAllowedLevel() {
        return maxAllowedLevel;
    }

    public void setMaxAllowedLevel(int maxAllowedLevel) {
        this.maxAllowedLevel = maxAllowedLevel;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
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

    public String getMyCommitment() {
        return myCommitment;
    }

    public void setMyCommitment(String myCommitment) {
        this.myCommitment = myCommitment;
    }

    

}
