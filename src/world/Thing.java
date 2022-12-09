/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import geometry.Entity3D;
import geometry.Point3D;
import map2D.Map2DColor;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import data.Transform;
import geometry.Compass;
import glossary.Sensors;
import java.util.ArrayList;

/**
 *
 * @author lcv
 */
public class Thing extends Entity3D {

    public static enum PROPERTY {
        POSITION, PRESENCE, ORIENTATION, SURFACE, TEMPERATURE, REPORT, ENERGY, STATUS, PAYLOAD, CARGO, ONTARGET,
        CHANNEL1, CHANNEL2, CHANNEL3, CHANNEL4, CHANNEL5, CHANNEL6, CHANNEL7, CHANNEL8, CHANNEL9, CHANNEL10
    }

    protected World _refWorld;
    protected Map2DColor _surface;
    protected ArrayList<Point3D> trail;
    protected String _type = "", _belongsTo = "";
    protected ArrayList<Perceptor> _rawSensors;
    protected boolean hasHeliport = false, hasPort = false, hasAirport = false, isCity, isMountain, isArea;
    protected int nLightH, nHeavyH, nLightT, nHeavyG, nFB, nLightS;
    protected SensorDecoder myPerceptions;
    protected Sensors[] minimal = new Sensors[] {
        Sensors.GPS,
        Sensors.CURRENTGOAL,
        Sensors.CURRENTMISSION,
        Sensors.ENERGY,
        Sensors.COMPASS,
        Sensors.GROUND,
        Sensors.AUTONOMY,
        Sensors.PAYLOAD,
        Sensors.ALIVE,
        Sensors.COMPASS,
        Sensors.DESTINATION,        
        Sensors.COURSE, 
        Sensors.TARGET,
        Sensors.TRACE,
        Sensors.TYPE,
        Sensors.DISTANCE,
        Sensors.DISTANCE,
        Sensors.ANGULAR
    };


    public Thing(String name) {
        super(name);
        _rawSensors = new ArrayList<>();
        myPerceptions = new SensorDecoder();
    }

    public Thing(String name, World w) {
        super(name);
        _refWorld = w;
        _rawSensors = new ArrayList<>();
        myPerceptions = new SensorDecoder();
        minimal = new Sensors[] {Sensors.GPS,Sensors.CURRENTGOAL,Sensors.CURRENTMISSION,Sensors.ENERGY,Sensors.AUTONOMY,
            Sensors.PAYLOAD,Sensors.ALIVE, Sensors.COMPASS,Sensors.DESTINATION,
        Sensors.COURSE, Sensors.TARGET,Sensors.TYPE};
    }

    public World getWorld() {
        return _refWorld;
    }
    
    public Thing setSurface(Map2DColor cartography) {
        _surface = cartography;
        return this;
    }

    public Map2DColor getSurface() {
        return _surface;
    }

    public Thing placeAtSurface(Point3D p) {
        this.setPosition(new Point3D(p.getX(), p.getY(), this.getWorld().getEnvironment().getSurface().getStepLevel(p.getX(), p.getY())));
        return this;
    }

    public Thing addSensor(Perceptor p) {
        _rawSensors.add(p);
        return this;
    }

    public JsonObject getPerceptions() {
//        System.out.println("Agent "+this.getName()+" querying "+Raw().indexperception.size()+" perceptions");
        return this.Raw().toJson();
    }

    public JsonObject getMinimalPerceptions() {
        return this.Raw().toJson(minimal);
    }

    public void readPerceptions() {
        JsonArray res = new JsonArray();
        JsonObject reading;
//        System.out.println("Agent "+this.getName()+" reading "+_rawSensors.size()+" perceptions ");
        for (Perceptor s : _rawSensors) {
            if (s.getType() != null) {
                reading = s.getReading();
                res.add(reading);
//                System.out.println("     Agent "+this.getName()+" reading sensor " + s.getName()+" = "+reading.toString());
            }
//            else
//                System.out.println("*****Agent "+this.getName()+" reading sensor " + s.getName()+" error");
        }
//        System.out.println("Agent "+this.getName()+" found "+res.size()+" perceptions ");
        JsonObject jsoreading = new JsonObject().add("name", getName()).add("perceptions", res);
        myPerceptions.feedPerception(jsoreading);
        myPerceptions.encodeSensor(Sensors.NAME, SensorDecoder.encodeValues(getName()));
        myPerceptions.encodeSensor(Sensors.WORLDHEIGHT,
                SensorDecoder.encodeValues(this.getWorld().getEnvironment().getSurface().getHeight()));
        myPerceptions.encodeSensor(Sensors.WORLDWIDTH,
                SensorDecoder.encodeValues(this.getWorld().getEnvironment().getSurface().getWidth()));
    }

    public int sizePerceptions() {
        return _rawSensors.size();
    }

    public JsonObject toJson() {
        JsonObject res = new JsonObject();

        res.add("name", getName());
        res.add("type", getType());
        res.add("orientation", this.getOrientation());
        res.add("position", new JsonArray().
                add(getPosition().getXInt()).add(getPosition().getYInt()).add(getPosition().getZInt()));
        res.add("belongs", this.getBelongsTo());
        res.add("capacity", this.getCapacity());
        res.add("isavailable", this.isAvailable()
        );

//        res.add("objectid", this.getId());
//        res.add("name", this.getName());
//        res.add("type", this.getType());
//        res.add("position", this.getPosition().toString());
//        res.add("surface-location", Transform.Matrix2JsonArray(this.getPosition().to2D().toArray()));
//        if (this.getType().toUpperCase().equals("AGENT")) {
//            res.add("orientation", this.getOrientation());
//            res.merge(this.getPerceptions());
//        }
//        res.add("properties", new JsonArray());
//        res.add("hasport", this.isHasPort());
//        res.add("hasairport", this.isHasAirport());
//        res.add("belongs", this.getBelongsTo());
        return res;
    }

    public void fromJson(JsonObject o) {
        this.setName(o.getString("name", ""));
        this.setType(o.getString("type", ""));
        this.setBelongsTo(o.getString("belongs", ""));
        this.setPosition(new Point3D(o.get("position").asArray()));
        this.setOrientation(o.getInt("orientation", Compass.NORTH));
        this.setHasPort(o.getBoolean("hasport", false));
        this.setHasAirport(o.getBoolean("hasairport", false));
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public boolean isHasHeliport() {
        return hasHeliport;
    }

    public void setHasHeliport(boolean hasHeliport) {
        this.hasHeliport = hasHeliport;
    }

    public boolean isHasPort() {
        return hasPort;
    }

    public void setHasPort(boolean hasPort) {
        this.hasPort = hasPort;
    }

    public boolean isHasAirport() {
        return hasAirport;
    }

    public void setHasAirport(boolean hastAirport) {
        this.hasAirport = hastAirport;
    }

    public SensorDecoder Raw() {
        return this.myPerceptions;
    }

    public String getBelongsTo() {
        return _belongsTo;
    }

    public void setBelongsTo(String _belongsTo) {
        this._belongsTo = _belongsTo;
    }

}
