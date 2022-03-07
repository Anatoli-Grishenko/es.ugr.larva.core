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
    protected String _type;
    protected ArrayList<Perceptor> _sensors;

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

//    public JsonObject oldgiveVisible(PROPERTY property, Point3D target) {
//        JsonObject res = new JsonObject();
//        JsonArray valuelist = new JsonArray();
//        if (property == PROPERTY.POSITION) {
//            res.add("position", this._position.toJson());
//        }
//        if (property == PROPERTY.ORIENTATION) {
//            res.add("orientation", this.getVector().toJson());
//        }
//        if (property == PROPERTY.PRESENCE) {
//            res.add("presence", this.contains(target));
//
//        }
//        if (property == PROPERTY.SURFACE) {
//            res.add("surface", _surface.getStepLevel((int) target.getX(), (int) target.getY()));
//        }
//        return res;
//    }
    public JsonObject toJson() {
        JsonObject res = new JsonObject();
//        res.add("id", this.getId());
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

    @Override
    public String toString() {
        return toJson().toString();
    }

}
