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
    protected ArrayList<Point3D>trail;
    protected String _type;
    protected ArrayList<Perceptor> _sensors;
    protected boolean hasHeliport, hasPort, hasAirport, isCity, isMountain, isArea;
    protected int nLightH, nHeavyH, nLightT, nHeavyG, nFB, nLightS;
    

    public Thing(String name) { 
        super(name);
        _sensors = new ArrayList<>();
    }

    public Thing(String name, World w) {
        super(name);
        _refWorld = w;
        _sensors = new ArrayList<>();
    }

    public Thing setType(String c) {
        _type = c;
        return this;
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

    public int getEnergy() {
        return Perceptor.NULLREAD;
    }

    public  int getOnTarget(){
        return Perceptor.NULLREAD;
    }
                     

    public int getAlive(){
        return Perceptor.NULLREAD;
    }
              
    public int getPayload(){
        return Perceptor.NULLREAD;
    }
              
    
    public Thing placeAtSurface(Point3D p) {
        this.setPosition(new Point3D(p.getX(), p.getY(), this.getWorld().getEnvironment().getSurface().getStepLevel(p.getX(), p.getY())));
        return this;
    }

    public Thing addSensor(Perceptor p) {
        _sensors.add(p);
        return this;
    }

    public JsonObject readPerceptions() {
        JsonArray res = new JsonArray();
        for (Perceptor s : _sensors) {
            res.add(s.getReading());
        }
        return new JsonObject().add("name", getName()).add("perceptions", res);
    }
    
    public int sizePerceptions() {
        return _sensors.size();
    }

    public JsonObject toJson() {
        JsonObject res = new JsonObject();
        res.add("objectid", this.getId());
        res.add("name", this.getName());
        res.add("type", this.getType());
        res.add("position", this.getPosition().toString());
        if (this.getType().toUpperCase().equals("AGENT")) {
            res.add("orientation", this.getOrientation());
            res.merge(this.readPerceptions());
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

    public ArrayList<Point3D> getTrail() {
        return trail;
    }

    public void setTrail(ArrayList<Point3D> trail) {
        this.trail = trail;
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

}
