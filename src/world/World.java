/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import geometry.Point;
import geometry.Vector;
import geometry.Compass;
import map2D.Map2DGrayscale;
import ontology.Ontology;
import world.Perceptor.ATTACH;
import world.Perceptor.OPERATION;
import world.Perceptor.SELECTION;
import world.Thing.PROPERTY;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;
import data.Ole;
import data.OleConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import map2D.Map2DColor;

/**
 *
 * @author lcv
 */
public class World {

    protected Thing _environment;
    protected HashMap<String, Thing> _population;
    protected HashMap<PROPERTY, ArrayList<Thing>> _visibility;
    protected Ontology _ontology;
    protected String name, spalette;
    protected boolean _godmode = false, debug = false;
    protected String _surfaceName;
    protected OleConfig cfg;
    protected String[][] filter, filterHQ, filterDLX;
    static int range = 41, cx = range / 2, cy = cx;
    int maxflight;

    public World(String name) {
        this.name = name;
        _population = new HashMap();
        _visibility = new HashMap<>();
        _ontology = new Ontology().add("THING", Ontology.ROOT).add("ENVIRONMENT", "THING").add("OBJECT", "THING");
        filter = new String[range][range];
        String N = "0", NE = "1", E = "2", SE = "3", S = "4", SW = "5", W = "6", NW = "7";
        for (int x = 0; x < range; x++) {
            for (int y = 0; y < range; y++) {
                filter[x][y] = "--";
            }
        }
        for (int x = 0; x < range; x++) {
            for (int y = 0; y < range; y++) {
                if (isInside(x, y) && halfSup(x, y) && halfSupInv(x, y) || (x == y && x == range / 2)) //N
                {
                    filter[x][y] += "." + N + ".";
                }
                if (isInside(x, y) && top(x, y) && right(x, y) || (x == y && x == range / 2)) //NE
                {
                    filter[x][y] += "." + NE + ".";
                }
                if (isInside(x, y) && halfSup(x, y) && halfInfInv(x, y) || (x == y && x == range / 2)) //E
                {
                    filter[x][y] += "." + E + ".";
                }
                if (isInside(x, y) && bottom(x, y) && right(x, y) || (x == y && x == range / 2)) //SE
                {
                    filter[x][y] += "." + SE + ".";
                }
                if (isInside(x, y) && halfInf(x, y) && halfInfInv(x, y) || (x == y && x == range / 2)) //S
                {
                    filter[x][y] += "." + S + ".";
                }
                if (isInside(x, y) && bottom(x, y) && left(x, y) || (x == y && x == range / 2)) // SW
                {
                    filter[x][y] += "." + SW + ".";
                }
                if (isInside(x, y) && halfInf(x, y) && halfSupInv(x, y) || (x == y && x == range / 2)) //W
                {
                    filter[x][y] += "." + W + ".";
                }
                if (isInside(x, y) && top(x, y) && left(x, y) || (x == y && x == range / 2)) //NW
                {
                    filter[x][y] += "." + NW + ".";
                }
            }
        }
    }

    protected boolean filterReading(int x, int y, int range, int orientation) {
        return filter[cx + x - range / 2][cy + y - range / 2].contains("." + orientation + ".");
    }

    public String getSurfaceName() {
        return _surfaceName;
    }

    public void setSurfaceName(String surface) {
        this._surfaceName = surface;
    }

    public String getName() {
        return this.name;
    }

    public Point placeAtMap(String where, ArrayList<Double> pos) {
        Point res = new Point(0, 0, 0);
        int width = this.getEnvironment().getSurface().getWidth(),
                height = this.getEnvironment().getSurface().getHeight();
        switch (where) {
            case "choice":
                res.setX(pos.get(0));
                res.setY(pos.get(1));
                break;
            case "middle":
                res.setX(width / 2);
                res.setY(height / 2);
                break;
            case "N":
                res.setX(width / 2);
                res.setY(0);
                break;
            case "S":
                res.setX(width / 2);
                res.setY(height);
                break;
            case "E":
                res.setX(width - 1);
                res.setY(height / 2);
                break;
            case "W":
                res.setX(0);
                res.setY(height / 2);
                break;
            case "NE":
                res.setX(width - 1);
                res.setY(0);
                break;
            case "SE":
                res.setX(width - 1);
                res.setY(height - 1);
                break;
            case "SW":
                res.setX(0);
                res.setY(height - 1);
                break;
            case "NW":
                res.setX(0);
                res.setY(0);
                break;
            default:
                do {
                    res.setX((int) (Math.random() * width));
                    res.setY((int) (Math.random() * height));
                } while (this.getEnvironment().getSurface().getStepLevel(res.getX(), res.getY()) > this.maxflight);
        }
        if (0 <= res.getX() && res.getX() < width && 0 <= res.getY() && res.getY() < height) {
            res.setZ(this.getEnvironment().getSurface().getStepLevel(res.getX(), res.getY()));
            return res;
        } else {
            return new Point(0, 0, this.getEnvironment().getSurface().getStepLevel(0, 0));
        }

    }

    public String loadConfig(String worldconfigfilename) {
        Ole ocfg = new Ole().loadFile("./LARVA/worlds/" + worldconfigfilename + ".worldconf.json");
        cfg = new OleConfig(ocfg);
        Ole oaux;
        if (!cfg.isEmpty()) {
            try {
                this.name = cfg.getField("name");
                oaux = cfg.getOle("drones");
                this.maxflight = oaux.getInt("maxflight");
                oaux = cfg.getOle("types");
                for (String oclass : oaux.getFieldList()) {
                    this.getOntology().add(oclass, oaux.getField(oclass));
                }
                oaux = cfg.getOle("world");
                Thing e = setEnvironment(oaux.getField(name)).getEnvironment();
                e.setPosition(new Point(0, 0, 0));
                e.setOrientation(Compass.NORTH);
                Map2DColor terrain = new Map2DColor(10, 10, 0);
                setSurfaceName("./LARVA/worlds/" + oaux.getField("surface"));
                spalette = oaux.getField("palette");
                terrain.loadMapNormalize(getSurfaceName());
                e.setSurface(terrain);
                e.setSize(new Point(this._environment._surface.getWidth(),
                        this._environment._surface.getHeight(), 0));

                for (Ole othing : new ArrayList<Ole>(oaux.getArray("things"))) {
                    ArrayList<String> properties = othing.getArray("properties");
                    PROPERTY[] props = new PROPERTY[properties.size()];
                    for (int i = 0; i < properties.size(); i++) {
                        props[i] = PROPERTY.valueOf(properties.get(i).toUpperCase());
                    }
                    e = new Thing(othing.getField("name"), this);
                    e.setType(othing.getField("type"));
                    this.addThing(e, props);
                    ArrayList<Double> auxp = othing.getArray("surface-location");
                    String where = (othing.getField("origin").equals("")) ? "choice" : othing.getField("origin");
                    Point eposition = this.placeAtMap(where, auxp);
                    e.setPosition(eposition);
//                    if (auxp.get(0) < 0 || auxp.get(1) < 0) {
//                        int rx, ry;
//                        boolean valid = true;
//                        do {
//                            rx = (int) (Math.random() * terrain.getWidth());
//                            ry = (int) (Math.random() * terrain.getHeight());
//                            for (Thing myt : this._population.values()) {
//                                if (!myt.getName().equals(this.getName()) && myt.getType().equals("PEOPLE") && myt.getPosition().to2D().fastDistanceXYTo(new Point(rx, ry)) < 5) {
//                                    valid = false;
//                                }
//                            }
//                        } while (!valid);
//                        e.placeAtSurface(new Point(rx, ry));
//                    } else {
//                        e.placeAtSurface(new Point(auxp.get(0), auxp.get(1), 0));
//                    }
                }
            } catch (Exception ex) {
                System.err.println(ex.toString());
                return ex.toString();
            }
            return "ok";
        }
        return "Empty configuration";
    }

    public OleConfig getConfig() {
        return this.cfg;
    }

    public World setOntology(Ontology o) {
        _ontology = o;
        return this;
    }

    public Ontology getOntology() {
        return _ontology;
    }

    public World setEnvironment(String name) {
        _environment = addThing(name, "ENVIRONMENT",
                new PROPERTY[]{PROPERTY.SURFACE, PROPERTY.POSITION, PROPERTY.ORIENTATION});
        _environment.setSize(new Point(1, 1, 0));
        _environment.setOrientation(Compass.NORTH);
        return this;
    }

    public Thing getEnvironment() {
        return _environment;
    }

    public Thing addThing(Thing i, PROPERTY[] visible) {
        if (i.getPosition() == null) {
            if (!i.getType().equals("ENVIRONMENT")) {
                i.setPosition(getEnvironment().getPosition());
                i.setOrientation(getEnvironment().getOrientation());
            }
        }
        if (i.getSize() == null) {
            i.setSize(new Point(1, 1, 0));
        }
        _population.put(i.getId(), i);
        for (int ch = 0; ch < visible.length; ch++) {
            addVisible(visible[ch], i);
        }
        return i;
    }

    public void removeThing(Thing i) {
        _population.remove(i.getId());
        for (PROPERTY p : PROPERTY.values()) {
            if (this._visibility.get(p) != null
                    && this._visibility.get(p).contains(i)) {
                this._visibility.get(p).remove(i);
            }
        }
    }

    public Thing addThing(String name, PROPERTY[] visible) {
        return addThing(name, _ontology.getRootType(), visible);
    }

    public Thing addThing(String name, String type, PROPERTY[] visible) {
        Thing i = new Thing(name, this);
        i.setType(type);
        return addThing(i, visible);
    }

    public Thing getThing(String id) {
        return _population.get(id);
    }

    public Thing getThingByName(String name) {
        Thing res = null;
        for (Thing t : _population.values()) {
            if (t.getName().equals(name)) {
                res = t;
            }
        }
        return res;
    }

    public boolean findThing(String id) {
        return _population.containsKey(id);
    }

    public Set<String> listThings() {
        return _population.keySet();
    }

    public ArrayList<String> getAllThings(String type) {
        ArrayList<String> list = new ArrayList<>();
        for (String s : _population.keySet()) {
            Thing t = this.getThing(s);
            if (this.getOntology().isSubTypeOf(t.getType(), type)) {
                list.add(s);
            }
        }
        return list;
    }

    public World addVisible(PROPERTY c, Thing t) {
        if (_visibility.get(c) == null) {
            _visibility.put(c, new ArrayList<>());
        }
        _visibility.get(c).add(t);
        return this;
    }

    public ArrayList<Thing> getDetectableList(Perceptor p) {
        PROPERTY property = p.getProperty();
        String type = p.getType();
        Thing who = p.getOwner();
        ArrayList<Thing> detectable = new ArrayList<>();

        // Intern sensors only perceive oneself
        if (p.getSelection() == SELECTION.INTERN) {
            detectable.add(who);
            return detectable;
        }
        // An Thing is detectable if the propoerty of the sensor is
        // visible on it and is the detected type
        for (Thing t : _visibility.get(property)) {
            if (!t.equals(who) && _ontology.matchTypes(t.getType(), type)) {
                detectable.add(t);
            }
        }
        // Sometimes only the closest Thing is detected
        if (p.getSelection() == SELECTION.CLOSEST && detectable.size() > 1) {
            Point mypos = p.getOwner().getPosition(), yourpos;
            double shortest = Double.MAX_VALUE, distance;
            Thing best = null, ti;
            for (int i = 0; i < detectable.size(); i++) {
                ti = detectable.get(i);
                yourpos = ti.getPosition();
                distance = mypos.fastDistanceXYTo(yourpos);
                if (distance < shortest) {
                    shortest = distance;
                    best = ti;
                }
            }
            detectable.clear();
            detectable.add(best);
        }
        return detectable;
    }

    public JsonObject getPerception(Perceptor p) {
        JsonObject res = new JsonObject(), owned, detected, partialres = null;
        JsonArray allreadings = new JsonArray(), xyreading, rowreading, coordinates = new JsonArray();
        Thing who = p.getOwner();
        String sname = p.getName();
        PROPERTY property = p.getProperty();
        OPERATION operation = p.getOperation();
        ATTACH attachment = p.getAttachment();
        ArrayList<Thing> detectable = getDetectableList(p);
        Point point, pini, pend;
        Vector vectororientation;
        point = who.getPosition();

        vectororientation = who.getVector();
        int range = p.getRange(), orientation = who.getOrientation();
        Point prange, observable;
        double x1, y1, x2, y2, incrx;
        if (range == 1) { // single rangle
            x1 = point.getX();
            y1 = point.getY();
            incrx = 0;
        } else {        // multiple range
            x1 = point.getX() - range / 2;
            y1 = point.getY() - range / 2;
            incrx = 0;
        }
        // Scans the world within the selected range (1x1 | nxn) and detects readings
        for (double sy = y1; sy < y1 + range; sy++) {
            rowreading = new JsonArray();
            for (double sx = x1; sx < x1 + range; sx++) {
                xyreading = new JsonArray();
                // For every position xy check all the potentially detected objects
                for (Thing t : detectable) {
                    // Start reading properties
                    observable = new Point(sx, sy, this.getEnvironment().getSurface().getStepLevel(sx, sy));
//                    if (property == PROPERTY.SURFACE || property==PROPERTY.PRESENCE) {
//                        observable = new Point(sx, sy, t.getSurface().getStepLevel(sx, sy));
//                    } else {
//                        observable = new Point(sx, sy);
//                    }
                    if (observable.fastDistanceXYTo(t.getPosition()) <= p.getSensitivity()) {
                        if (operation == OPERATION.QUERY) {
                            if (property == PROPERTY.ENERGY) {
                                partialres = new JsonObject().add("value", t.getEnergy());
                            }
                            if (property == PROPERTY.STATUS) {
                                partialres = new JsonObject().add("value", t.getAlive());
                            }
                            if (property == PROPERTY.ONTARGET) {
                                partialres = new JsonObject().add("value", t.getOnTarget());
                            }
                            if (property == PROPERTY.PAYLOAD) {
                                partialres = new JsonObject().add("value", t.getPayload());
                            }
                            if (property == PROPERTY.POSITION) {
                                partialres = new JsonObject().add("value", t.getPosition().toJson());
                            }
                            if (property == PROPERTY.ORIENTATION) {
                                partialres = new JsonObject().add("value", t.getVector().toJson());
                            }
                            if (property == PROPERTY.PRESENCE) {
                                partialres = new JsonObject().add("value", (t.contains(observable) ? 100 : 0));

                            }
                            if (property == PROPERTY.SURFACE) {
                                int value = t.getSurface().getStepLevel(observable.getX(), observable.getY());
                                if (value != -1) {
                                    partialres = new JsonObject().add("value", value);
                                } else {
                                    partialres = new JsonObject().add("value", Perceptor.NULLREAD);
                                }
                            }
                        }
                        if (operation == OPERATION.DISTANCE) {
                            if (property == PROPERTY.POSITION) {
                                partialres = new JsonObject().add("value", observable.realDistanceTo(t.getPosition().to2D()));
                            }
                            if (property == PROPERTY.SURFACE) {
                                int value = t.getSurface().getStepLevel(observable.getX(), observable.getY());
                                if (value >= 0) { //(value != -1) {
                                    partialres = new JsonObject().add("value", who.getPosition().getZ() - value);
                                } else {
                                    partialres = new JsonObject().add("value", Perceptor.NULLREAD);
                                }
                            }
                        }
                        if (operation == OPERATION.ANGLE) {
                            if (property == PROPERTY.POSITION) {
                                if (point.to2D().isEqualTo(t.getPosition().to2D())) {
                                    partialres = new JsonObject().add("value", 0);
                                } else if (attachment == ATTACH.FRONTAL) {
                                    partialres = new JsonObject().add("value", vectororientation.angleXYTo(t.getPosition()));
                                } else {
                                    partialres = new JsonObject().add("value", Compass.VECTOR[Compass.NORTH].angleXYTo(new Vector(observable, t.getPosition())));
                                }
                            }
                            if (property == PROPERTY.ORIENTATION) {
                                partialres = new JsonObject().add("value", Compass.VECTOR[Compass.NORTH].angleXYTo(who.getVector()));
                            }

                        }
                        if (_godmode || p.getName().contains("_GOD_")) {
                            xyreading.add(partialres.merge(new JsonObject().add("name", t.getName())));
                        } else {
                            xyreading.add(partialres.get("value"));
                        }
                    } else {
                        if (p.getName().toUpperCase().startsWith("THERMAL")) {
                            xyreading.add(p.getSensitivity());
                        } else {
                            xyreading.add(Perceptor.NULLREAD);
                        }
                    }
                }
                if (xyreading.size() == 0) {
                    xyreading.add(Perceptor.NULLREAD);
                }
                if (xyreading.size() == 1) {
                    if (p.getAttachment() == ATTACH.ZENITAL) {
                        rowreading.add(xyreading.get(0));
                    } else {
                        if (filterReading((int) (sx - x1), (int) (sy - y1), range, orientation)) {
                            rowreading.add(xyreading.get(0));
                        } else {
                            rowreading.add(Perceptor.NULLREAD);
                        }
                    }
                } else {
                    if (xyreading.get(0).isNumber()) {
//                        int max = xyreading.get(0).asInt();
//                        for (JsonValue v : xyreading) {
//                            if (v.asInt() > max) {
//                                max = v.asInt();
//                            }
//                        }
//                        rowreading.add(max);
                        double min = xyreading.get(0).asDouble();
                        for (JsonValue v : xyreading) {
                            if (v.asDouble() < min) {
                                min = v.asDouble();
                            }
                        }
                        if (p.getAttachment() == ATTACH.ZENITAL) {
                            rowreading.add(min);
                        } else {
                            if (filterReading((int) (sx - x1), (int) (sy - y1), range, orientation)) {
                                rowreading.add(min);
                            } else {
                                rowreading.add(Perceptor.NULLREAD);
                            }
                        }
                    } else {
                        rowreading.add(xyreading);
                    }
                    //coordinates.add(new JsonObject().add("xy", new Point(sx, sy).toJson()));
                }
            }
            if (rowreading.size() == 1) {
                allreadings.add(rowreading.get(0));
            } else {
                allreadings.add(rowreading);
                coordinates.add(new Point(Math.round(x1), Math.round(sy)).toJson());
            }
            x1 += incrx;
        }
        if (p.getRange() == 1) {
            return res.add("sensor", p.getName()).add("data", allreadings);
        } else {
            return res.add("sensor", p.getName()).add("data", allreadings);
        }
    }

    public String getSpalette() {
        return spalette;
    }

    boolean isInside(int x, int y) {
        return (Math.pow(x - cx, 2) + Math.pow(y - cy, 2)) <= Math.pow(cx, 2);
    }

    boolean mainD(int x, int y) {
        return x == y;
    }

//    boolean y(int x, int y) {
//        return x==cx;
//    }
//    boolean x(int x, int y) {
//        return y==cy;
//    }
//    
    boolean right(int x, int y) {
        return x >= cx;
    }

    boolean left(int x, int y) {
        return x <= cx;
    }

    boolean top(int x, int y) {
        return y <= cy;
    }

    boolean bottom(int x, int y) {
        return y >= cy;
    }

    boolean invD(int x, int y) {
        return x == range - y;
    }

    boolean halfSup(int x, int y) {
        return x > y || mainD(x, y);
    }

    boolean halfInf(int x, int y) {
        return mainD(x, y) || !halfSup(x, y);
    }

    boolean halfSupInv(int x, int y) {
        return x < range - y || invD(x, y);
    }

    boolean halfInfInv(int x, int y) {
        return invD(x, y) || !halfSupInv(x, y);
    }

}
//    public JsonObject oldgetPerception(Perceptor p) {
//        JsonObject res = new JsonObject(), owned, detected, partialres = null;
//        JsonArray allreadings = new JsonArray(), xyreading;
//        Thing who = p.getOwner();
//        PROPERTY property = p.getProperty();
//        OPERATION operation = p.getOperation();
//        ATTACH attachment = p.getAttachment();
//        ArrayList<Thing> detectable = getDetectableList(p);
//        Point point, pini, pend;
//        Vector orientation;
//        point = who.getPosition();
//        orientation = who.getVector();
//        int range = p.getRange();
//        double x1, y1, x2, y2;
//        if (range == 1) { // single rangle
//            x1 = x2 = point.getX();
//            y1 = y2 = point.getY();
//        } else {        // multiple range
//            Vector vp1, vp1p2;
//            if (p.getAttachment() == ATTACH.FRONTAL) {
//                vp1 = Compass.VECTOR[Entity.rotateLeft(Entity.rotateLeft(who.getOrientation()))].clone().scalar(range / 2);
//                vp1p2 = Compass.VECTOR[Entity.rotateRight(who.getOrientation())].clone().scalar(range - 1);
//            } else if (p.getAttachment() == ATTACH.LEFT) {
//                vp1 = Compass.VECTOR[who.getOrientation()].clone().scalar(range / 2);
//                vp1p2 = Compass.VECTOR[Entity.rotateLeft(Entity.rotateLeft(Entity.rotateLeft(who.getOrientation())))].clone().scalar(range - 1);
//            } else if (p.getAttachment() == ATTACH.RIGHT) {
//                vp1 = Compass.VECTOR[Entity.Opposite(who.getOrientation())].clone().scalar(range / 2);
//                vp1p2 = Compass.VECTOR[who.getOrientation()].clone().scalar(range - 1);
//            } else { //if (p.getAttachment() == ATTACH.ZENITAL) {
//                vp1 = Compass.VECTOR[Compass.NORTHWEST].clone().scalar(range / 2);
//                vp1p2 = Compass.VECTOR[Compass.SOUTHEAST].clone().scalar(range - 1);
//            }
//            pini = point.clone().plus(vp1);
//            pend = pini.clone().plus(vp1p2);
//            x1 = (int) (Math.round(Math.min(pini.getX(), pend.getX())));
//            y1 = (int) (Math.round(Math.min(pini.getY(), pend.getY())));
//            x2 = (int) (Math.round(Math.max(pini.getX(), pend.getX())));
//            y2 = (int) (Math.round(Math.max(pini.getY(), pend.getY())));
//        }
//        for (double sy = y1; sy <= y2; sy++) {
//            for (double sx = x1; sx <= x2; sx++) {
//                Point observable = new Point(sx, sy);
//                xyreading = new JsonArray();
//                for (Thing t : detectable) {
//                    if (observable.fastDistanceTo(t.getPosition()) < p.getSensitivity()) {
//                        if (operation == OPERATION.QUERY) {
//                            if (property == PROPERTY.POSITION) {
//                                partialres = new JsonObject().add("value", t.getPosition().toJson());
//                            }
//                            if (property == PROPERTY.ORIENTATION) {
//                                partialres = new JsonObject().add("value", t.getVector().toJson());
//                            }
//                            if (property == PROPERTY.PRESENCE) {
//                                partialres = new JsonObject().add("value", t.contains(observable));
//
//                            }
//                            if (property == PROPERTY.SURFACE) {
//                                int value = t.getSurface().getStepLevel((int) observable.getX(), (int) observable.getY());
//                                if (value != -1) {
//                                    partialres = new JsonObject().add("value", value);
//                                } else {
//                                    partialres = new JsonObject().add("value", Perceptor.NULLREAD);
//                                }
//                            }
//                        }
//                        if (operation == OPERATION.DISTANCE) {
//                            if (property == PROPERTY.POSITION) {
//                                partialres = new JsonObject().add("value", observable.realDistanceTo(t.getPosition()));
//                            }
//                            if (property == PROPERTY.SURFACE) {
//                                int value = t.getSurface().getStepLevel((int) observable.getX(), (int) observable.getY());
//                                if (value != -1) {
//                                    partialres = new JsonObject().add("value", who.getPosition().getZ() - value);
//                                } else {
//                                    partialres = new JsonObject().add("value", Perceptor.NULLREAD);
//                                }
//                            }
//                        }
//                        if (operation == OPERATION.ANGLE) {
//                            if (property == PROPERTY.POSITION) {
//                                if (point.to2D().isEqualTo(t.getPosition().to2D())) {
//                                    partialres = new JsonObject().add("value", 0);
//                                } else if (attachment == ATTACH.FRONTAL) {
//                                    partialres = new JsonObject().add("value", orientation.angleXYTo(t.getPosition()));
//                                } else {
//                                    partialres = new JsonObject().add("value", Compass.VECTOR[Compass.NORTH].angleXYTo(new Vector(observable, t.getPosition())));
//                                }
//                            }
//                            if (property == PROPERTY.ORIENTATION) {
//                                partialres = new JsonObject().add("value", Compass.VECTOR[Compass.NORTH].angleXYTo(who.getVector()));
//                            }
//
//                        }
//                        if (_godmode) {
//                            xyreading.add(partialres.merge(new JsonObject().add("name", t.getName())));
//                        } else {
//                            xyreading.add(partialres.get("value"));
//                        }
//                    }
//                }
//                if (xyreading.size() == 1) {
//                    allreadings.add(xyreading.get(0));
//                } else {
//                    allreadings = xyreading;
//                }
//            }
//        }
//        if (p.getRange() == 1) {
//            return res.add("sensor", p.getName()).add("data", allreadings);
//        } else {
//            return res.add("sensor", p.getName()).add("data", allreadings).add("range_from", new Point(x1, y1).toJson()).add("range_to", new Point(x2, y2).toJson());
//        }
//    }
//    // Handling rotations
//    public static double interpolate(double values[], double angle) {
//        double res = 0;
//        int prev = (int) Math.floor(angle / 45), next = (int) Math.round(angle / 45);
//        prev = prev % 8;
//        next = next % 8;
//        return (values[prev] + values[next]) / 2;
//    }
