/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import ai.Mission;
import ai.MissionSet;
import ai.TracePositions;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.WriterConfig;
import data.Ole;
import data.OleFile;
import data.Transform;
import geometry.Compass;
import geometry.Point3D;
import geometry.PolarSurface;
import geometry.SimpleVector3D;
import geometry.Vector3D;
import glossary.Sensors;
import glossary.capability;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import map2D.Map2DColor;
import tools.TimeHandler;
import static zip.ZipTools.unzipString;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class SensorDecoder {

    protected HashMap<Sensors, JsonArray> indexperception;
    protected Map2DColor hMap;
    protected ArrayList<SimpleVector3D> TraceGPS;
    protected int stuck, energyBase = 50;
    protected Mission currentMission;
    protected String cachedCurrentCity = "", cachedDestinationCity = "";
    protected TimeHandler lastRead;
    public boolean verbose = true;

    // SensorsDISTANCE,
    // Memory
    // ParametersBURNRATEMOVE, BURNRATEREAD,
    // Behaviour
    public SensorDecoder() {
        indexperception = new HashMap();
        this.TraceGPS = new ArrayList();
        encodeSensor(Sensors.TRACE, new JsonArray());
        encodeSensor(Sensors.CARGO, new JsonArray());
        encodeSensor(Sensors.PEOPLE, new JsonArray());
        encodeSensor(Sensors.CAPABILITIES, new JsonArray());
        encodeSensor(Sensors.STOP, false);
        lastRead = new TimeHandler();
    }

    public boolean setWorldMap(String content, int maxlevel) {
        return setWorldMap(content);
    }

    public boolean setWorldMap(String content) {
        OleFile mapa = new OleFile();
        mapa.set(content);
        mapa.saveFile("./maps/");
        this.TraceGPS.clear();
        stuck = 0;
        String name = mapa.getFileName();
        return loadWorldMap("./maps/" + name);

    }

    public boolean loadWorldMap(String name) {
        try {
            hMap = new Map2DColor();
            hMap.loadMapRaw(name);
            return true;
        } catch (IOException ex) {
        }
        return false;
    }

    public boolean loadWorldMap(Map2DColor map) {
        hMap = map;
        return true;
    }

    public JsonArray getSensor(Sensors s) {
        if (this.indexperception.keySet().contains(s)) {
            return indexperception.get(s);
        }
        return null;
    }

    public JsonArray getSensor(String sensorname) {
        try {
            Sensors s = Sensors.valueOf(sensorname);
            return getSensor(s);
        } catch (Exception ex) {
        }
        return null;
    }

    public void encodeSensor(String sensorname, JsonArray reading) {
        try {
            Sensors s = Sensors.valueOf(sensorname.toUpperCase());
            encodeSensor(s, reading);
        } catch (Exception ex) {
        }
    }

    public void removeSensor(Sensors s) {
        indexperception.put(s, null);
    }

    public void encodeSensor(Sensors s, JsonArray reading) {
        indexperception.put(s, reading);
    }

    public void encodeSensor(Sensors s, double value) {
        encodeSensor(s, encodeValues(value));
    }

    public void encodeSensor(Sensors s, boolean value) {
        encodeSensor(s, encodeValues(value));
    }

    public void encodeSensor(Sensors s, double[] value) {
        encodeSensor(s, encodeValues(value));
    }

    public void encodeSensor(Sensors s, String[] value) {
        encodeSensor(s, encodeValues(value));
    }

    public void encodeSensor(Sensors s, String value) {
        encodeSensor(s, encodeValues(value));
    }

    public static JsonArray encodeValues(double d) {
        return new JsonArray().add(d);
    }

    public static JsonArray encodeValues(boolean b) {
        return new JsonArray().add(b);
    }

    public static JsonArray encodeValues(String s) {
        return new JsonArray().add(s);
    }

    public static JsonArray encodeValues(double[] d) {
        return Transform.toJsonArray(new ArrayList(Arrays.asList(d)));
    }

    public static JsonArray encodeValues(String[] d) {
        return Transform.toJsonArray(new ArrayList(Arrays.asList(d)));
    }

    public Map2DColor getWorldMap() {
        return hMap;
    }

    public int getWorldWidth() {
//        return getWorldMap().getWidth();
        return this.getSensor(Sensors.WORLDWIDTH).get(0).asInt();
    }

    public int getWorldHeight() {
//        return getWorldMap().getHeight();
        return this.getSensor(Sensors.WORLDHEIGHT).get(0).asInt();
    }

    protected double[] fromJsonArray(JsonArray jsa) {
        double res[] = new double[jsa.size()];
        for (int i = 0; i < jsa.size(); i++) {
            res[i] = jsa.get(i).asDouble();
        }
        return res;
    }

//    protected String getMission() {
//        if (this.getSensor(Sensors.MISSIONSET) != null) {
//            return this.getSensor(Sensors.MISSIONSET).get(0).asString();
//        } else {
//            return "";
//        }
//    }
//
//    public void setMission(String Name) {
//        encodeSensor(Sensors.MISSIONSET, encodeValues(Name));
//    }
//    public String getGoal() {
//        if (this.getSensor(Sensors.CURRENTMISSION) != null
//                && this.getSensor(Sensors.CURRENTMISSION).get(0) != null) {
//            if (this.getSensor(Sensors.CURRENTMISSION).get(0).isString()) {
//                return this.getSensor(Sensors.CURRENTMISSION).get(0).asString();
//            } else {
//                return "";
//            }
//        } else {
//            return "";
//        }
//    }
//
//    public void setTask(String Name) {
//        encodeSensor(Sensors.CURRENTMISSION, encodeValues(Name));
//    }
    public String getCityBase() {
        return this.getSensor(Sensors.CITYBASE).get(0).asString();
    }

    public void setCityBase(String Name) {
        encodeSensor(Sensors.CITYBASE, encodeValues(Name));
    }

    /**
     * Internal use only
     *
     * @return
     */
    public String getCityDestination() {
        return this.getSensor(Sensors.CITYDESTINATION).get(0).asString();
    }

    /**
     * Internal use only
     *
     * @param Name
     */
    public void setCityDestination(String Name) {
        encodeSensor(Sensors.CITYDESTINATION, encodeValues(Name));
    }

    /**
     * It returns the name of the agent who owns this environment
     *
     * @return An agent's name
     */
    public String getName() {
        if (this.getSensor(Sensors.NAME) != null) {
            return this.getSensor(Sensors.NAME).get(0).asString();
        } else {
            return null;
        }
    }

    /**
     * Internal use only
     *
     * @param Name
     */
    public void setName(String Name) {
        encodeSensor(Sensors.NAME, encodeValues(Name));
    }

    /**
     * It returns the type used by the agent to register in the DF
     *
     * @param type The type
     */
    public void setType(String type) {
        encodeSensor(Sensors.NAME, encodeValues(type));
    }
//
//    public String getTeam() {
//        return this.getSensor(Sensors.TEAM).get(0).asString();
//    }
//
//    public void setTeam(String Team) {
//        encodeSensor(Sensors.TEAM, encodeValues(Team));
//    }
//

    /**
     * It returns a description of the general Status of the agent.
     *
     * @return A string that describes the status. If empty, then everything is
     * ok. Otherwise, it shows the error or erros commited
     */
    public String getStatus() {
        return this.getSensor(Sensors.STATUS).get(0).asString();
    }

    /**
     * Internal use only
     *
     * @param Status
     */
    public void setStatus(String Status) {
        encodeSensor(Sensors.STATUS, encodeValues(Status));
    }

    /**
     * Internal use only
     *
     * @param Status
     */
    public void addStatus(String Status) {
        encodeSensor(Sensors.STATUS, encodeValues(getStatus() + "\n" + Status));
    }

    /**
     * It returns the sessionID within which the last perceptions were receibed
     *
     * @return
     */
    public String getSessionid() {
        if (this.getSensor(Sensors.SESSIONID) != null) {
            return this.getSensor(Sensors.SESSIONID).get(0).asString();
        } else {
            return null;
        }
    }

    /**
     * Internal use only
     *
     * @param Sessionid
     */
    public void setSessionid(String Sessionid) {
        encodeSensor(Sensors.SESSIONID, encodeValues(Sessionid));
    }

    @Deprecated
    public String getCommitment() {
        return this.getSensor(Sensors.COMMITMENT).get(0).asString();
    }

    @Deprecated
    public void setCommitment(String Commitment) {
        encodeSensor(Sensors.COMMITMENT, encodeValues(Commitment));
    }

    /**
     * It returns the reading of the compass as an angle staring in North=0 and
     * continuing couterclockwise every 45 degrees
     *
     * @return The angle of orientation of the agent
     */
    public int getCompass() {
//        return (int) this.getSensor(Sensors.COMPASS).get(0).asDouble();
        int v = (int) getSensor(Sensors.COMPASS).get(0).asDouble();
//        v = 360 - v;
//        return v % 360;
        return v;
    }

    /**
     * Internal use only
     *
     * @param Compass
     */
    public void setCompass(int Compass) {
        encodeSensor(Sensors.COMPASS, encodeValues(Compass));
    }

    /**
     * It gives the reading of the ground sensor. That is the height over the
     * ground. When ground == 0 the agent is exactly on the surface.
     *
     * @return Thje height over the ground level
     */
    public int getGround() {
        return this.getSensor(Sensors.GROUND).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Ground
     */
    public void setGround(int Ground) {
        encodeSensor(Sensors.GROUND, encodeValues(Ground));
    }

    /**
     * It gives the current level of energy
     *
     * @return The level of energy right now
     */
    public int getEnergy() {
        return this.getSensor(Sensors.ENERGY).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Energy
     */
    public void setEnergy(int Energy) {
        encodeSensor(Sensors.ENERGY, encodeValues(Energy));
    }

    /**
     * It gives the number of objects in the cargo compartment of the agent
     *
     * @return The number of object in the cargo
     */
    public int getPayload() {
        //return this.getSensor(Sensors.PAYLOAD).get(0).asInt();
        return this.getCargo().length;
    }

    /**
     * Internal use only
     *
     * @param Payload
     */
    public void setPayload(int Payload) {
        encodeSensor(Sensors.PAYLOAD, encodeValues(Payload));
    }

    /**
     * It returns the number of actions execcuted so far
     *
     * @return
     */
    public int getNumsteps() {
        return this.getSensor(Sensors.NUMSTEPS).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Numsteps
     */
    public void setNumsteps(int Numsteps) {
        encodeSensor(Sensors.NUMSTEPS, encodeValues(Numsteps));
    }

    /**
     * It returns the width of the matrix sensors, that is, if the matrix sensor
     * is NxN it returns N
     *
     * @return
     */
    public int getRange() {
        return this.getSensor(Sensors.RANGE).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Range
     */
    public void setRange(int Range) {
        encodeSensor(Sensors.RANGE, encodeValues(Range));
    }

    /**
     * The amount of energy burnt so far
     *
     * @return
     */
    public int getEnergyburnt() {
        return this.getSensor(Sensors.ENERGYBURNT).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Energyburnt
     */
    public void setEnergyburnt(int Energyburnt) {
        encodeSensor(Sensors.ENERGYBURNT, encodeValues(Energyburnt));
    }

    /**
     * It returns the running time in seconds since the begining of the problem
     *
     * @return
     */
    public int getTime() {
        return this.getSensor(Sensors.TIME).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Time
     */
    public void setTime(int Time) {
        encodeSensor(Sensors.TIME, encodeValues(Time));
    }

    /**
     * It returns the maximum level (height or Z-coordinate) supported by the
     * agent
     *
     * @return
     */
    public int getMaxlevel() {
        return this.getSensor(Sensors.MAXLEVEL).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Maxlevel
     */
    public void setMaxlevel(int Maxlevel) {
        encodeSensor(Sensors.MAXLEVEL, encodeValues(Maxlevel));
    }

    /**
     * It returns the minimul leve (height or Z-coordinate) supported by the
     * agent
     *
     * @return
     */
    public int getMinlevel() {
        return this.getSensor(Sensors.MINLEVEL).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Minlevel
     */
    public void setMinlevel(int Minlevel) {
        encodeSensor(Sensors.MINLEVEL, encodeValues(Minlevel));
    }

    /**
     * It returns the maximum different
     *
     * @return
     */
    public int getMaxslope() {
        return this.getSensor(Sensors.MAXSLOPE).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Maxslope
     */
    public void setMaxslope(int Maxslope) {
        encodeSensor(Sensors.MAXSLOPE, encodeValues(Maxslope));
    }

    /**
     * IT returns the maximum number of objects that the agent can carry out
     *
     * @return
     */
    public int getMaxcargo() {
        return this.getSensor(Sensors.MAXCARGO).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Maxcargo
     */
    public void setMaxcargo(int Maxcargo) {
        encodeSensor(Sensors.MAXCARGO, encodeValues(Maxcargo));
    }

    /**
     * It returns the maximum amount of energy that the agent can have
     *
     * @return
     */
    public int getAutonomy() {
        return this.getSensor(Sensors.AUTONOMY).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Autonomy
     */
    public void setAutonomy(int Autonomy) {
        encodeSensor(Sensors.AUTONOMY, encodeValues(Autonomy));
    }

    /**
     * It returns how many energy units are burnt in every movement
     *
     * @return
     */
    public int getBurnratemove() {
        return this.getSensor(Sensors.BURNRATEMOVE).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Burnratemove
     */
    public void setBurnratemove(int Burnratemove) {
        encodeSensor(Sensors.BURNRATEMOVE, encodeValues(Burnratemove));
    }

    /**
     * It returns how many energy units are spent in every reading of sensors
     *
     * @return
     */
    public int getBurnrateread() {
        return this.getSensor(Sensors.BURNRATEREAD).get(0).asInt();
    }

    /**
     * Internal use only
     *
     * @param Burnrateread
     */
    public void setBurnrateread(int Burnrateread) {
        encodeSensor(Sensors.BURNRATEREAD, encodeValues(Burnrateread));
    }

    /**
     * Returns true if the agent is exactly in the next waypoint of a course
     *
     * @return
     */
    public boolean getOntarget() {
//        return this.getSensor(Sensors.ONTARGET).get(0).asBoolean();
        return (this.getTarget() != null && getTarget().isEqualTo(getGPS()));
    }

    /**
     * It returns true when the agent is exactly at the end of a course
     *
     * @return
     */
    public boolean getOnDestination() {
        if (getDestination() == null) {
            return false;
        }
        return getDestination().isEqualTo(getGPS());
    }

    /**
     * Internal use only
     *
     * @param Ontarget
     */
    public void setOntarget(boolean Ontarget) {
        encodeSensor(Sensors.ONTARGET, encodeValues(Ontarget));
    }

    /**
     * It returns true if the agent is atill alive
     *
     * @return
     */
    public boolean getAlive() {
        return this.getSensor(Sensors.ALIVE).get(0).asBoolean();
    }

//    public boolean getStop() {
//        return this.getSensor(Sensors.STOP).get(0).asBoolean();
//    }
//
//    public void setStop(boolean stop) {
//        encodeSensor(Sensors.STOP, encodeValues(stop));
//    }
//
    /**
     * Internal use only
     *
     * @param Alive
     */
    public void setAlive(boolean Alive) {
        encodeSensor(Sensors.ALIVE, encodeValues(Alive));
    }

    /**
     * It returns the names of the objects carried out by the agent
     *
     * @return
     */
    public String[] getCargo() {
        return Transform.toArrayString(new ArrayList(Transform.toArrayList(this.getSensor(Sensors.CARGO))));
    }

    /**
     * It returns true if the parameter is a name of an object carried out by
     * the agent
     *
     * @param what
     * @return
     */
    public int containsCargo(String what) {
        JsonArray res = getSensor(Sensors.CARGO);
        for (int i = 0; i < res.size(); i++) {
            if (res.get(i).asString().equals(what)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Internal use only
     *
     * @param Cargo
     */
    public void setCargo(String[] Cargo) {
        encodeSensor(Sensors.CARGO, encodeValues(Cargo));
    }

    /**
     * Internal use only
     *
     * @param Value
     */
    public void addCargo(String Value) {
        getSensor(Sensors.CARGO).add(Value);
    }

    /**
     * Internal use only
     *
     * @param Value
     */
    public void removeCargo(String Value) {
        int i = this.containsCargo(Value);
        if (i >= 0) {
            JsonArray res = getSensor(Sensors.CARGO);
            res.remove(i);
        }

    }

    /**
     * It returns the whole sequence of actions succesfully executed by the
     * agent
     *
     * @return
     */
    public String[] getTrace() {
        return Transform.toArrayString(new ArrayList(Transform.toArrayList(this.getSensor(Sensors.TRACE))));
    }

    /**
     * Internal use only
     *
     * @param Trace
     */
    public void setTrace(String[] Trace) {
        encodeSensor(Sensors.TRACE, encodeValues(Trace));
    }

    /**
     * Internal use only
     *
     * @param Value
     */
    public void addTrace(String Value) {
        getSensor(Sensors.TRACE).add(Value);
    }

    /**
     * Internal use only
     *
     * @param Value
     */
    public void removeTrace(String Value) {
        JsonArray res = getSensor(Sensors.TRACE);
        for (int i = 0; i < res.size(); i++) {
            if (res.get(i).asString().equals(Value)) {
                res.remove(i);
                break;
            }
        }
    }

    /**
     * Get a list of all mission names in a certain world
     *
     * @return
     */
    public String[] getAllMissions() {
        String res[] = new String[this.getSensor(Sensors.MISSIONSET).size()];
        int i = 0;
        for (JsonValue jsv : this.getSensor(Sensors.MISSIONSET)) {
            String mission[] = jsv.asString().split(Mission.sepMissions);
            res[i++] = mission[0];
        }
        return res;
    }

    /**
     * Internal use only
     *
     * @param missionset
     */
    public void setMissions(String[] missionset) {
        encodeSensor(Sensors.MISSIONSET, encodeValues(missionset));
    }

    /**
     * It returns the content of a certain mission, which is a sequence of goals
     * separated with semicolons ;
     *
     * @param missionName The mission to query
     * @return
     */
    public String getMission(String missionName) {
        ArrayList<String> mission = new ArrayList(Transform.toArrayListString(this.getSensor(Sensors.MISSIONSET)));
        for (String sm : mission) {
            if (sm.split(Mission.sepMissions)[0].equals(missionName)) {
                return sm;
            }
        }
        return "";
    }

    /**
     * It returns an array of all the goals of a mission
     *
     * @param missionName
     * @return
     */
    public String[] getMissionGoals(String missionName) {
        for (String smission : getAllMissions()) {
            if (smission.startsWith(missionName)) {
                String fullmission = this.getMission(missionName);
                return fullmission.replace(missionName + Mission.sepMissions, "").split(Mission.sepMissions);
            }
        }
        return new String[]{};
    }

    public String[] getPeople() {
        return Transform.toArrayString(new ArrayList(Transform.toArrayList(this.getSensor(Sensors.PEOPLE))));
    }

    public void setPeople(String[] People) {
        encodeSensor(Sensors.PEOPLE, encodeValues(People));
    }

    public String[] getCapabilities() {
        return Transform.toArrayString(new ArrayList(Transform.toArrayList(this.getSensor(Sensors.CAPABILITIES))));
    }

    public void setCapabilities(String[] Capabilities) {
        encodeSensor(Sensors.CAPABILITIES, encodeValues(Capabilities));
    }

    public void addCapabilities(String Value) {
        getSensor(Sensors.CAPABILITIES).add(Value.toUpperCase());
    }

    public void removeCapabilities(String Value) {
        JsonArray res = getSensor(Sensors.CAPABILITIES);
        for (int i = 0; i < res.size(); i++) {
            if (res.get(i).asString().equals(Value)) {
                res.remove(i);
                break;
            }
        }
    }

    public Point3D getTarget() {
        if (getSensor(Sensors.TARGET) != null) {
            return new Point3D(getSensor(Sensors.TARGET));
        } else {
            return null;
        }
    }

    public void setTarget(Point3D Target) {
        if (Target == null) {
            encodeSensor(Sensors.TARGET, (JsonArray) null);
        } else {
            encodeSensor(Sensors.TARGET, Target.toJson());
        }
    }

    public Point3D getGPS() {
        if (this.getSensor(Sensors.GPS) != null) {
            return new Point3D(this.getSensor(Sensors.GPS).get(0).asArray());
        } else {
            return null;
        }
    }

    public Point3D getGPSMemory(int i) {
        return getGPSVectorMemory(i).getSource();
    }

    public int getGPSMemory(Point3D s) {
        for (int i = 1; i < TraceGPS.size(); i++) {
            if (this.TraceGPS.get(i).getSource().isEqualTo(s)) {
                return i;
            }
        }
        return -1;
    }

    public int getGPSMemorySize() {
        return this.TraceGPS.size();
    }

    public boolean containsGPSMemory(Point3D gps, int max) {
        for (int i = 1; i < max && i < this.getGPSMemorySize(); i++) {
            if (this.getGPSMemory(i).isEqualTo(gps)) {
                return true;
            }
        }
        return false;
    }

    public void setGPS(Point3D GPS) {
        encodeSensor(Sensors.GPS, new JsonArray().add(GPS.toJson()));
    }

    public Point3D getDestination() {
        if (getSensor(Sensors.DESTINATION) != null) {
            return new Point3D(getSensor(Sensors.DESTINATION));
        } else {
            return null;
        }
    }

    public void setDestination(Point3D destination) {
        if (destination == null) {
            encodeSensor(Sensors.TARGET, (JsonArray) null);
        } else {
            encodeSensor(Sensors.DESTINATION, destination.toJson());
        }
    }

    // Derived sensors
    public double getAltitude() {
        return getGPS().getZInt();
    }

    public SimpleVector3D getGPSVector() {
        return new SimpleVector3D(getGPS(), getCompass() / 45);
    }

    public SimpleVector3D getGPSVectorMemory(int old) {
        if (0 <= old && old < this.TraceGPS.size()) {
            return this.TraceGPS.get(old);
        } else {
            return new SimpleVector3D();
        }
    }

    public int getGPSVectorMemory(SimpleVector3D s) {
        for (int i = 2; i < TraceGPS.size(); i++) {
            if (this.TraceGPS.get(i).isEqualTo(s)) {
                return i;
            }
        }
        return -1;
    }

    public int getOrientation() {
        return getCompass() / 45;
    }

    public Point3D getGPSComingPosition() {
        return getGPSVector().getTarget();
    }

    public double getDistance() {
        return this.getTargetDistance();
    }

    public double getAngular() {
        return this.getTargetAbsoluteAngular();
    }

    public int getCompassLeft() {
        return (getCompass() + 45 + 360) % 360;
    }

    public int getCompassRight() {
        return (getCompass() - 45 + 360) % 360;
    }

    public double getRelativeAngular() {
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
    }

    public double getRelativeAngularto(Point3D p) {
        double a = getAbsoluteAngularTo(p), c = getCompass();
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
    }

    public double getAbsoluteAngular() {
        if (getTarget() == null) {
            return Perceptor.NULLREAD;
        }
        return this.getAbsoluteAngularTo(getTarget());
    }

    public double getAbsoluteAngularTo(Point3D p) {
        Vector3D Norte = new Vector3D(new Point3D(0, 0), new Point3D(0, -10));
        Point3D me = getGPS();
        Vector3D Busca = new Vector3D(me, p);

//        int v = (int) Norte.angleXYTo(Busca);
//        v = 360 - v + 360;
//        return (int) v % 360;
        return Norte.angleXYTo(Busca);
    }

    public double getAbsoluteAngularTo(Point3D orig, Point3D dest) {
        Vector3D Norte = new Vector3D(new Point3D(0, 0), new Point3D(0, -10));
        Point3D me = orig;
        Vector3D Busca = new Vector3D(me, dest);

        int v = (int) Norte.angleXYTo(Busca);
//        v = 270 - v;
//        return (int) v % 360;
        return Norte.angleXYTo(Busca);
    }

    public double getRelativeAngularto(Point3D orig, int compass, Point3D dest) {
        double a = getAbsoluteAngularTo(orig, dest), c = compass;
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
    }

    public int getNSteps() {
        if (this.getTrace() != null) {
            return this.getTrace().length;
        }
        return 0;
    }

    public Map2DColor getFullZenitalVisual() {
        if (getVisualData() == null) {
            return null;
        }
        Map2DColor initial, res;
        int[][] levels = getVisualData();
//        SimpleVector3D myv = this.getGPSVectorMemory();
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

    public int[][] getPolarCourse() {
        int initial[][] = this.getCourseData(), res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()), myv);
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyPolarTo(initial);
        return res;
    }

    public int[][] getPolarVisual() {
        if (getVisualData() == null) {
            return null;
        }
        int initial[][] = this.getVisualData(), res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()), myv);
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyPolarTo(initial);
        return res;
    }

    public Point3D[][] getPolarPoints() {
        if (getVisualData() == null) {
            return null;
        }
        int initial[][] = this.getVisualData();
        Point3D res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()), myv);
        ps.setRadius(mhh / 2 + 1);
        res = ps.applyPolarToPoints(initial);
        return res;
    }

    public int[][] getPolarLidar() {
        if (getLidarData() == null) {
            return null;
        }
        int initial[][] = this.getLidarData(), res[][];
        SimpleVector3D myv = this.getGPSVector();
        int mww = initial[0].length, mhh = initial.length;
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()), myv);
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
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()), myv);
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
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()), myv);
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
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()), myv);
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
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh / 2, myv.getsOrient()), myv);
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
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh - 1, myv.getsOrient()), myv);
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
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh - 1, myv.getsOrient()), myv);
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
        PolarSurface ps = new PolarSurface(new SimpleVector3D(mww / 2, mhh - 1, myv.getsOrient()), myv);
        ps.setRadius(mww / 2 + 1);
        res = ps.applyRelativeTo(initial);
        return res;
    }

    public int[][] getCourseData() {
        JsonArray jsaReading = getSensor(Sensors.COURSE);
        Point3D mypos = this.getGPS();
        int range = getSensor(Sensors.VISUAL).size(), x, y;

        int[][] res = new int[range][range];

        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[0].length; j++) {
                res[j][i] = Perceptor.NULLREAD;
            }
        }
        if (jsaReading == null) {
            return res;
        }
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[0].length; j++) {
                res[j][i] = 0;
                x = mypos.getXInt() + j - range / 2;
                y = mypos.getYInt() + i - range / 2;
                for (int wp = 0; wp < jsaReading.size(); wp++) {
                    if (x == jsaReading.get(wp).asArray().get(0).asInt()
                            && y == jsaReading.get(wp).asArray().get(1).asInt()) {
                        res[j][i] = 1;
                    }
                }
            }
        }
        return res;
    }

    public int[][] getVisualData() {
        JsonArray jsaReading = null;
        jsaReading = getSensor(Sensors.VISUAL);
        if (jsaReading == null) {
            return null;
        }
        int range = jsaReading.size();

        int[][] res = new int[range][range]; //jsaVisual.size(), jsaVisual.size());
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[0].length; j++) {
                res[j][i] = jsaReading.get(i).asArray().get(j).asInt();
            }
        }
        return res;
    }

    public int[][] getLidarData() {
        JsonArray jsaReading = null;
        jsaReading = getSensor(Sensors.LIDAR);
        if (jsaReading == null) {
            return null;
        }

        int range = jsaReading.size();
        int[][] res = new int[range][range]; //jsaVisual.size(), jsaVisual.size());
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[0].length; j++) {
                res[j][i] = jsaReading.get(i).asArray().get(j).asInt();
            }
        }
        return res;
    }

    public int[][] getThermalData() {
        JsonArray jsaReading = null;
        jsaReading = getSensor(Sensors.THERMAL);
        if (jsaReading == null) {
            return null;
        }

        int range = jsaReading.size();
        int[][] res = new int[range][range]; //jsaVisual.size(), jsaVisual.size());
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[0].length; j++) {
                res[j][i] = (int) (jsaReading.get(i).asArray().get(j).asDouble());
            }
        }
        return res;
    }

    public void resetCourse() {
        this.encodeSensor(Sensors.COURSE, new JsonArray());
    }

    public Point3D[] getCourse() {
        JsonArray jsaReading = null, jsarow;
        jsaReading = getSensor(Sensors.COURSE);
        if (jsaReading == null) {
            return null;
        }
        Point3D res[] = new Point3D[jsaReading.size()];
        for (int i = 0; i < jsaReading.size(); i++) {
            res[i] = new Point3D(jsaReading.get(i).asArray());
        }
        return res;
    }

    public int sizeCourse() {
        return getCourse().length;
    }

    public void cleanCourse() {
        this.encodeSensor(Sensors.COURSE, new JsonArray());
    }

    public Point3D getCourse(int i) {
        if (0 <= i && i < sizeCourse()) {
            return getCourse()[i];
        } else {
            return null;
        }
    }

    public void addCourse(Point3D p) {
        if (this.getSensor(Sensors.COURSE) == null) {
            this.cleanCourse();
        }
        this.encodeSensor(Sensors.COURSE, this.getSensor(Sensors.COURSE).add(p.toJson()));
    }

    public void activateCourse() {
        if (sizeCourse() > 0) {
            setDestination(getCourse(sizeCourse() - 1));
            setTarget(getCourse(0));
        }
    }

    public void nextCourse() {
        if (getTarget() == null) {
            activateCourse();
        }
        int inext = this.findNextCourseIndex();
        if (inext > 0) {
            setTarget(getCourse(inext));
        }
    }

    public int findNextCourseIndex() {
        if (getTarget() == null && getCourse() != null) {
            activateCourse();
        }
        for (int i = 0; i < sizeCourse(); i++) {
            if (getTarget().isEqualTo(getCourse(i)) && i < sizeCourse() - 1) {
//                setTarget(getCourse(i + 1));
                return i + 1;
            }
        }
        return -1;
    }

    public JsonObject toJson() {
        return toJson(Sensors.values());
    }

    public JsonObject toJson(Sensors[] whichones) {
        JsonObject jsores = new JsonObject();
        JsonArray jsareadings = new JsonArray();
        for (Sensors sSensor : whichones) {
            if (indexperception.get(sSensor) != null) {
                jsareadings.add(new JsonObject().add("sensor", sSensor.name().toUpperCase()).add("data", indexperception.get(sSensor)));
            }
        }
        return new JsonObject().add("perceptions", jsareadings);
    }

    public Ole toOle() {
        return new Ole(toJson());
    }

    public void fromJson(JsonObject jsoreading) {
        fromJson(jsoreading.get("perceptions").asArray());
    }

    public void fromJson(JsonArray jsareading) {
        if (verbose) {
            System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>\n" + "SensorDecoder::Procesing readings");
        }
        if (jsareading == null) {
            if (verbose) {
                System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>\n" + "SensorDecoder::Empty readings");
            }
            return;
        }
        for (int i = 0; i < jsareading.size(); i++) {
            JsonObject jsosensor = jsareading.get(i).asObject();
            String name = jsosensor.getString("sensor", "");
            if (verbose) {
                System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>\n" + "SensorDecoder::Found sensor " + name);
            }
            encodeSensor(name, jsosensor.get("data").asArray());
            if (name.toUpperCase().equals(Sensors.COURSE.name()) && getSensor(Sensors.CITIESPOSITIONS) != null) { // XUI
                ArrayList<String> positions = new ArrayList(Transform.toArrayList(this.getSensor(Sensors.CITIESPOSITIONS)));
                Point3D destPoint = this.getCourse(this.getCourse().length - 1);
                for (String scity : positions) {
                    Point3D citypos = new Point3D(scity.split(Mission.sepMissions)[1]);
                    if (destPoint.isEqualTo(citypos)) {
                        this.cachedDestinationCity = scity.split(Mission.sepMissions)[0];
                    }
                }

            }
        }
    }

    public void fromOle(ArrayList<Ole> oreading) {
//        clear();
        for (Ole osensor : oreading) {
            String sensorname = osensor.getField("sensor");
            encodeSensor(sensorname, Transform.toJsonArray(new ArrayList(osensor.getArray("data"))));
        }
    }

    public void feedPerception(JsonObject jsoperception) {
        fromJson(jsoperception.get("perceptions").asArray());
//        this.TraceGPS.add(0, this.getGPSVector());
        if (this.getGPS() != null) {
            if (this.TraceGPS.size() == 0 || !this.getGPS().isEqualTo(TraceGPS.get(0).getSource())) {
                this.TraceGPS.add(0, this.getGPSVector());
            } else {
                if (TraceGPS.size() > 2 && this.getGPS().isEqualTo(TraceGPS.get(0).getSource())) {
                    stuck++;
                } else {
                    stuck = 0;
                }
            }

        }
    }

    public int getStuck() {
        return stuck;
    }

    public void feedPerception(String content) {
        JsonObject jsoperception;
//        if (content.contains("zipdata")) {
//            jsoperception = Json.parse(new Ole().UnzipThis(new Ole(content))).asObject();
//        } else {
//            jsoperception = Json.parse(content).asObject();
//        }
//        System.out.println(jsoperception.toString(WriterConfig.PRETTY_PRINT));
        String unzipedcontent = unzipString(content);
        jsoperception = Json.parse(unzipedcontent).asObject();
        feedPerception(jsoperception);
    }

    public String[] getSensorList() {
        return this.indexperception.keySet().toArray(new String[this.indexperception.keySet().size()]);
    }

    public String printStatus(String requester) {
        String res = "", line;
        res = "Under request from " + requester + "\n";
        if (getSensor(Sensors.NAME) == null) {
            return "N/A";
        }
        res += "|  " + getNSteps() + "\n";
        res += "|  Status of: " + getName() + "\n";
        res += "| |Capablities:\n";
        res += "| |(" + this.getCapabilities().length + ") " + new ArrayList(Transform.toArrayList(this.getCapabilities())) + "\n";
        res += "| |\n";
        res += "| |Memory:\n";
        res += "| |(" + this.TraceGPS.size() + ") " + this.getGPSMemory(1) + "\n";
        res += "| |\n";
        res += "|  EN:" + getEnergy() + "W \n";
        res += "|  X:" + getGPS().getXInt() + " Y:" + getGPS().getYInt() + " Z:" + (int) getAltitude() + "\n";
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
            res += line + "\n|\n";
        }
        res += printStatusExtended();
        return res;
    }

    public String printStatusExtended() {
        String line, res = "";
        int visual[][] = getVisualData(), lidar[][] = getLidarData(), thermal[][] = getThermalData();
        line = "";
        for (int y = 0; y < visual.length; y++) {
            if (y == 0) {
                line = "| V|";
            } else {
                line = "|  |";
            }
            for (int x = 0; x < visual[0].length; x++) {
                if (visual[x][y] == Perceptor.NULLREAD) {
                    line += "XXX|";
                } else {
                    line += String.format("%03d|", visual[x][y]);
                }
            }
            if (y == 0) {
                line += "  L|";
            } else {
                line += "   |";
            }
            for (int x = 0; x < lidar[0].length; x++) {
                if (lidar[x][y] == Perceptor.NULLREAD) {
                    line += "XXX|";
                } else {
                    line += String.format("%03d|", lidar[x][y]);
                }
            }
            if (y == 0) {
                line += "  T|";
            } else {
                line += "   |";
            }
            for (int x = 0; x < thermal[0].length; x++) {
                if (thermal[x][y] == Perceptor.NULLREAD) {
                    line += "XXX|";
                } else {
                    line += String.format("%03d|", thermal[x][y]);
                }
            }
            res += line + "\n";
        }
        res += "\n";
        visual = this.getAbsoluteVisual();
        lidar = getAbsoluteLidar();
        thermal = getAbsoluteThermal();
        line = "";
        for (int y = 0; y < visual.length; y++) {
            if (y == 0) {
                line = "|AV|";
            } else {
                line = "|  |";
            }
            for (int x = 0; x < visual.length; x++) {
                if (y < visual[0].length) {
                    if (visual[x][y] == Perceptor.NULLREAD) {
                        line += "XXX|";
                    } else {
                        line += String.format("%03d|", visual[x][y]);
                    }
                } else {
                    line += "   |";
                }
            }
            if (y == 0) {
                line += " AL|";
            } else {
                line += "   |";
            }
            for (int x = 0; x < lidar.length; x++) {
                if (y < lidar[0].length) {
                    if (lidar[x][y] == Perceptor.NULLREAD) {
                        line += "XXX|";
                    } else {
                        line += String.format("%03d|", lidar[x][y]);
                    }
                } else {
                    line += "   |";
                }
            }
            if (y == 0) {
                line += " AT|";
            } else {
                line += "   |";
            }
            for (int x = 0; x < thermal.length; x++) {
                if (y < thermal[0].length) {
                    if (thermal[x][y] == Perceptor.NULLREAD) {
                        line += "XXX|";
                    } else {
                        line += String.format("%03d|", thermal[x][y]);
                    }
                } else {
                    line += "   |";
                }
            }
            res += line + "\n";
        }
        res += "\n";
        visual = this.getRelativeVisual();
        lidar = getRelativeLidar();
        thermal = getRelativeThermal();
        line = "";
        for (int y = 0; y < visual.length; y++) {
            if (y == 0) {
                line = "|RV|";
            } else {
                line = "|  |";
            }
            for (int x = 0; x < visual.length; x++) {
                if (y < visual[0].length) {
                    if (visual[x][y] == Perceptor.NULLREAD) {
                        line += "XXX|";
                    } else {
                        line += String.format("%03d|", visual[x][y]);
                    }
                } else {
                    line += "   |";
                }
            }
            if (y == 0) {
                line += " RL|";
            } else {
                line += "   |";
            }
            for (int x = 0; x < lidar.length; x++) {
                if (y < lidar[0].length) {
                    if (lidar[x][y] == Perceptor.NULLREAD) {
                        line += "XXX|";
                    } else {
                        line += String.format("%03d|", lidar[x][y]);
                    }
                } else {
                    line += "   |";
                }
            }
            if (y == 0) {
                line += " RT|";
            } else {
                line += "   |";
            }
            for (int x = 0; x < thermal.length; x++) {
                if (y < thermal[0].length) {
                    if (thermal[x][y] == Perceptor.NULLREAD) {
                        line += "XXX|";
                    } else {
                        line += String.format("%03d|", thermal[x][y]);
                    }
                } else {
                    line += "   |";
                }
            }
            res += line + "\n";
        }
        res += "|\n\n";
        return res;
    }

    public double getTargetDistance() {
        if (getTarget() == null) {
            return Perceptor.NULLREAD;
        }
        Point3D target = getTarget();
        if (target != null) {
            return getGPS().planeDistanceTo(target);
        } else {
            return Perceptor.NULLREAD;
        }
    }

    public double getTargetAbsoluteAngular() {
        if (getTarget() == null) {
            return Perceptor.NULLREAD;
        }
        double ang = Math.toDegrees(Math.atan2((this.getGPS().getY() - getTarget().getY()), (getTarget().getX() - this.getGPS().getX()))) - 90;
        return (ang + 360) % 360;
    }

    public double getTargetRelativeAngular() {
        double ang = getTargetAbsoluteAngular();
        double c = getCompass(), ar;
        if (ang > c) {
            if (ang - c <= 180) {
                ar = ang - c;
            } else {
                ar = -(c + 360 - ang);
            }
        } else {
            if (c - ang < 180) {
                ar = ang - c;
            } else {
                ar = (ang + 360 - c);
            }

        }
        return ar;
    }

    public double get3DDistance() {
        if (getTarget() == null) {
            return Perceptor.NULLREAD;
        }
        Point3D target = getTarget();
        if (target != null) {
            return getGPS().realDistanceTo(target);
        } else {
            return Perceptor.NULLREAD;
        }
    }

    public int getGridDistance() {
        if (getTarget() == null) {
            return Perceptor.NULLREAD;
        }
        Point3D target = getTarget();
        if (target != null) {
            return getGPS().gridDistanceTo(getTarget());
        } else {
            return Perceptor.NULLREAD;
        }
    }

    public double getPlaneDistance() {
        if (getTarget() == null) {
            return Perceptor.NULLREAD;
        }
        Point3D target = getTarget();
        if (target != null) {
            return getGPS().planeDistanceTo(getTarget());
        } else {
            return Perceptor.NULLREAD;
        }
    }

    public String getType() {
        return this.getSensor(Sensors.TYPE).get(0).asString();
    }

    void configureType(String type) {
        encodeSensor(Sensors.TYPE, type);
        switch (type.toUpperCase()) {
            default:
            case "HUMMER":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL + 5);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXSLOPE, 30);
                encodeSensor(Sensors.MAXCARGO, 6);
                encodeSensor(Sensors.RANGE, 11);
                encodeSensor(Sensors.AUTONOMY, 60 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.BURNRATEMOVE, 1);
                encodeSensor(Sensors.BURNRATEREAD, 1);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "VAAT":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXSLOPE, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXCARGO, 25);
                encodeSensor(Sensors.AUTONOMY, 160 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 11);
                encodeSensor(Sensors.BURNRATEMOVE, 0);
                encodeSensor(Sensors.BURNRATEREAD, 0);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.UP.name().toUpperCase(),
                    capability.DOWN.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "BB1F":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXSLOPE, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXCARGO, 0);
                encodeSensor(Sensors.AUTONOMY, 360 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 11);
                encodeSensor(Sensors.BURNRATEMOVE, 0);
                encodeSensor(Sensors.BURNRATEREAD, 0);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.UP.name().toUpperCase(),
                    capability.DOWN.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "YV":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXSLOPE, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXCARGO, 250);
                encodeSensor(Sensors.AUTONOMY, 360 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 11);
                encodeSensor(Sensors.BURNRATEMOVE, 0);
                encodeSensor(Sensors.BURNRATEREAD, 0);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.UP.name().toUpperCase(),
                    capability.DOWN.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "DEST":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXSLOPE, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXCARGO, 250);
                encodeSensor(Sensors.AUTONOMY, 360 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 11);
                encodeSensor(Sensors.BURNRATEMOVE, 0);
                encodeSensor(Sensors.BURNRATEREAD, 0);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.UP.name().toUpperCase(),
                    capability.DOWN.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "AT_ST":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL + 5);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXSLOPE, 20);
                encodeSensor(Sensors.MAXCARGO, 1);
                encodeSensor(Sensors.AUTONOMY, 600);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 21);
                encodeSensor(Sensors.BURNRATEMOVE, 1);
                encodeSensor(Sensors.BURNRATEREAD, 1);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "ITT":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL + 5);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXSLOPE, 30);
                encodeSensor(Sensors.MAXCARGO, 5);
                encodeSensor(Sensors.AUTONOMY, 60 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 21);
                encodeSensor(Sensors.BURNRATEMOVE, 1);
                encodeSensor(Sensors.BURNRATEREAD, 1);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "SC":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL + 5);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXSLOPE, 15);
                encodeSensor(Sensors.MAXCARGO, 20);
                encodeSensor(Sensors.AUTONOMY, 60 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 31);
                encodeSensor(Sensors.BURNRATEMOVE, 2);
                encodeSensor(Sensors.BURNRATEREAD, 2);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "STF":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL - 35);
                encodeSensor(Sensors.MAXSLOPE, 255);
                encodeSensor(Sensors.MAXCARGO, 1);
                encodeSensor(Sensors.AUTONOMY, 60 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 21);
                encodeSensor(Sensors.BURNRATEMOVE, 1);
                encodeSensor(Sensors.BURNRATEREAD, 1);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.UP.name().toUpperCase(),
                    capability.DOWN.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "TS":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL - 35);
                encodeSensor(Sensors.MAXSLOPE, 255);
                encodeSensor(Sensors.MAXCARGO, 5);
                encodeSensor(Sensors.AUTONOMY, 50 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 21);
                encodeSensor(Sensors.BURNRATEMOVE, 1);
                encodeSensor(Sensors.BURNRATEREAD, 1);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.UP.name().toUpperCase(),
                    capability.DOWN.name().toUpperCase(),
                    capability.CAPTURE.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "YT":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL - 35);
                encodeSensor(Sensors.MAXSLOPE, 255);
                encodeSensor(Sensors.MAXCARGO, 30);
                encodeSensor(Sensors.AUTONOMY, 50 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 21);
                encodeSensor(Sensors.BURNRATEMOVE, 2);
                encodeSensor(Sensors.BURNRATEREAD, 1);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.UP.name().toUpperCase(),
                    capability.DOWN.name().toUpperCase(),
                    capability.CAPTURE.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "HEMTT":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL + 5);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXSLOPE, 20);
                encodeSensor(Sensors.MAXCARGO, 40);
                encodeSensor(Sensors.AUTONOMY, 60 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 31);
                encodeSensor(Sensors.BURNRATEMOVE, 2);
                encodeSensor(Sensors.BURNRATEREAD, 0);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "COLIBRI":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL - 35);
                encodeSensor(Sensors.MAXSLOPE, getSensor(Sensors.MAXLEVEL).get(0).asInt());
                encodeSensor(Sensors.MAXCARGO, 6);
                encodeSensor(Sensors.AUTONOMY, 80 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 21);
                encodeSensor(Sensors.BURNRATEMOVE, 1);
                encodeSensor(Sensors.BURNRATEREAD, 0);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.UP.name().toUpperCase(),
                    capability.DOWN.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "BLACKHAWK":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL - 35);
                encodeSensor(Sensors.MAXSLOPE, getSensor(Sensors.MAXLEVEL).get(0).asInt());
                encodeSensor(Sensors.MAXCARGO, 20);
                encodeSensor(Sensors.AUTONOMY, 100 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 31);
                encodeSensor(Sensors.BURNRATEMOVE, 2);
                encodeSensor(Sensors.BURNRATEREAD, 0);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.UP.name().toUpperCase(),
                    capability.DOWN.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "AOSHIMA":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL - 35);
                encodeSensor(Sensors.MAXSLOPE, getSensor(Sensors.MAXLEVEL).get(0).asInt());
                encodeSensor(Sensors.MAXCARGO, 20);
                encodeSensor(Sensors.AUTONOMY, 70 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 31);
                encodeSensor(Sensors.BURNRATEMOVE, 1);
                encodeSensor(Sensors.BURNRATEREAD, 0);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.UP.name().toUpperCase(),
                    capability.DOWN.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "ATLASA400":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MAXLEVEL);
                encodeSensor(Sensors.MAXSLOPE, getSensor(Sensors.MAXLEVEL).get(0).asInt());
                encodeSensor(Sensors.MAXCARGO, 140);
                encodeSensor(Sensors.AUTONOMY, 200 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 51);
                encodeSensor(Sensors.BURNRATEMOVE, 5);
                encodeSensor(Sensors.BURNRATEREAD, 0);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.UP.name().toUpperCase(),
                    capability.DOWN.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "CORSAIR":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MINLEVEL + 10);
                encodeSensor(Sensors.MAXSLOPE, 0);
                encodeSensor(Sensors.MAXCARGO, 15);
                encodeSensor(Sensors.AUTONOMY, 100 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 31);
                encodeSensor(Sensors.BURNRATEMOVE, 1);
                encodeSensor(Sensors.BURNRATEREAD, 0);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
            case "MARWAND":
                encodeSensor(Sensors.MINLEVEL, Map2DColor.MINLEVEL);
                encodeSensor(Sensors.MAXLEVEL, Map2DColor.MINLEVEL + 10);
                encodeSensor(Sensors.MAXSLOPE, 0);
                encodeSensor(Sensors.MAXCARGO, 250);
                encodeSensor(Sensors.AUTONOMY, 300 * energyBase);
                encodeSensor(Sensors.ENERGYBURNT, 0);
                encodeSensor(Sensors.RANGE, 31);
                encodeSensor(Sensors.BURNRATEMOVE, 1);
                encodeSensor(Sensors.BURNRATEREAD, 0);
                encodeSensor(Sensors.CAPABILITIES, new String[]{
                    capability.MOVE.name().toUpperCase(),
                    capability.LEFT.name().toUpperCase(),
                    capability.RIGHT.name().toUpperCase(),
                    capability.BOARD.name().toUpperCase(),
                    capability.DEBARK.name().toUpperCase(),
                    capability.RECHARGE.name().toUpperCase(),
                    capability.QUERY.name().toUpperCase()
                });
                break;
        }
    }

    public Mission getCurrentMission() {
        return currentMission;
    }

    public void setCurrentMission(String mission) {
        String goals[] = getMissionGoals(mission);
        this.setCurrentMission(mission, goals);        
    }

    public void setCurrentMission(String missionName, String goals[]) {
        currentMission = new Mission(missionName, goals);

    }

    ///////////////////// GOALS
    public String getCurrentGoal() {
        if (getCurrentMission() != null) {
            return getCurrentMission().getCurrentGoal();
        }
        return null;
    }

    public String setNextGoal() {
        if (getCurrentMission().isOver()) {
            return "";
        } else {
            getCurrentMission().nextGoal();
            return "";
        }
    }

    public String getCurrentCity() {
        if (getGround() == 0) {
            ArrayList<String> positions = new ArrayList(Transform.toArrayList(this.getSensor(Sensors.CITIESPOSITIONS)));
            for (String scity : positions) {
                if (new Point3D(scity.split(Mission.sepMissions)[1]).isEqualTo(getGPS())) {
                    cachedCurrentCity = scity.split(Mission.sepMissions)[0];
                    return cachedCurrentCity;
                }
            }
        }
        return "MOVING";
    }

    public String getDestinationCity() {
        if (getDestination() != null) {
            return this.cachedDestinationCity;
        } else {
            return "NONE";
        }
    }

    public TimeHandler getLastRead() {
        return lastRead;
    }

}
