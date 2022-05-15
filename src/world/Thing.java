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
    protected String _type;
    protected ArrayList<Perceptor> _rawSensors;
    protected boolean hasHeliport, hasPort, hasAirport, isCity, isMountain, isArea;
    protected int nLightH, nHeavyH, nLightT, nHeavyG, nFB, nLightS;
    protected SensorDecoder myPerceptions;

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
    }

    public void setType(String c) {
        _type = c;
    }

    public String getType() {
        return _type;
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
    }

    public int sizePerceptions() {
        return _rawSensors.size();
    }

    public JsonObject toJson() {
        JsonObject res = new JsonObject();
        res.add("objectid", this.getId());
        res.add("name", this.getName());
        res.add("type", this.getType());
        res.add("position", this.getPosition().toString());
        if (this.getType().toUpperCase().equals("AGENT")) {
            res.add("orientation", this.getOrientation());
            res.merge(this.getPerceptions());
        }
        return res;
    }

    public void fromJson(JsonObject o) {
        this.setName(o.getString("name", ""));
        this.setType(o.getString("type", ""));
        this.setPosition(new Point3D(o.getString("position", "")));
        this.setOrientation(o.getInt("orientation", Compass.NORTH));
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
}
