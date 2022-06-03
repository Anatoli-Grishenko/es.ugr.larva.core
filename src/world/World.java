/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import ai.AStar;
import ai.Choice;
import ai.Greedy;
import ai.Mission;
import ai.MissionSet;
import ai.Plan;
import ai.Search;
import ai.Search.PathType;
import geometry.Point3D;
import geometry.Vector3D;
import geometry.Compass;
import map2D.Map2DGrayscale;
import ontology.Ontology;
//import Perceptor.ATTACH;
//import Perceptor.OPERATION;
//import Perceptor.SELECTION;
//import Thing.PROPERTY;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;
import data.Ole;
import data.OleConfig;
import data.Transform;
import geometry.PolarSurface;
import geometry.SimpleVector3D;
import glossary.Roles;
import glossary.Sensors;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import map2D.Map2DColor;
import world.Perceptor.ATTACH;
import world.Perceptor.OPERATION;
import world.Perceptor.SELECTION;
import world.Thing.PROPERTY;

/**
 *
 * @author lcv
 */
public class World {

    protected Thing _environment;
    protected ThingSet _population;
    protected HashMap<PROPERTY, ArrayList<Thing>> _visibility;
    protected Ontology _ontology;
    protected String name, spalette;
    protected boolean _godmode = false, debug = false;
    protected String _surfaceName;
    protected OleConfig cfg;
    protected JsonArray oThings;
    protected String[][] filter, filterHQ, filterDLX;
    static int range = 41, cx = range / 2, cy = cx;
    int maxflight;
    protected Point3D destination, start;
    MissionSet _missions;

    public World(String name) {
        this.name = name;
        _population = new ThingSet();
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

    public Point3D placeAtMap(Point3D initial) {
        Point3D res = initial;
        int width = this.getEnvironment().getSurface().getWidth(),
                height = this.getEnvironment().getSurface().getHeight();
        if (0 <= res.getX() && res.getX() < width && 0 <= res.getY() && res.getY() < height) {
            res.setZ(this.getEnvironment().getSurface().getStepLevel(res.getX(), res.getY()));
            return res;
        } else {
            return new Point3D(0, 0, this.getEnvironment().getSurface().getStepLevel(0, 0));
        }

    }

    public Point3D placeAtMap(String where, Point3D initial) {
        Point3D res = new Point3D(0, 0, 0);
        int width = this.getEnvironment().getSurface().getWidth(),
                height = this.getEnvironment().getSurface().getHeight();
        switch (where) {
            case "choice":
                res.setX(initial.getX());
                res.setY(initial.getY());
                break;
            case "middle":
                res.setX(width / 2);
                res.setY(height / 2);
                break;
            case "N":
                res.setX(width / 2);
                res.setY(1);
                break;
            case "S":
                res.setX(width / 2);
                res.setY(height - 2);
                break;
            case "E":
                res.setX(width - 2);
                res.setY(height / 2);
                break;
            case "W":
                res.setX(1);
                res.setY(height / 2);
                break;
            case "NE":
                res.setX(width - 2);
                res.setY(1);
                break;
            case "SE":
                res.setX(width - 2);
                res.setY(height - 2);
                break;
            case "SW":
                res.setX(1);
                res.setY(height - 2);
                break;
            case "NW":
                res.setX(1);
                res.setY(1);
                break;
            default:
                res.setX((int) (Math.random() * width));
                res.setY((int) (Math.random() * height));
        }
        if (0 <= res.getX() && res.getX() < width && 0 <= res.getY() && res.getY() < height) {
            res.setZ(this.getEnvironment().getSurface().getStepLevel(res.getX(), res.getY()));
            return res;
        } else {
            return new Point3D(0, 0, this.getEnvironment().getSurface().getStepLevel(0, 0));
        }

    }

    public void saveConfig(String worldconfigfilename) {
        JsonArray jsa = new JsonArray();
        JsonObject jso = null;
        for (Thing t : _population.getAllThings()) {
//            jso = new JsonObject();
//            jso.add("name", t.getName());
//            jso.add("type", t.getType());
//            jso.add("origin", "choice");
//            jso.add("surface-location", new JsonArray().
//                    add(t.getPosition().getXInt()).add(t.getPosition().getYInt()));
//            if (t.getType().equals("people")) {
//                jso.add("properties", new JsonArray().add("position").add("presence"));
//                jso.add("properties", new JsonArray().add("position").add("presence"));
//            } else {
//                jso.add("properties", new JsonArray());
//                jso.add("hasport", t.isHasPort());
//                jso.add("hasheliport", t.isHasHeliport());
//                jso.add("hasairport", t.isHasAirport());
//            }
            jsa.add(t.toJson());
        }
        cfg.get("world").asObject().set("things", jsa);
        cfg.saveAsFile(".", worldconfigfilename, true);
    }

    public String loadConfig(String worldconfigfilename) {
        Ole ocfg = new Ole().loadFile(worldconfigfilename);
        cfg = new OleConfig(ocfg);
        Ole oaux;
        if (!cfg.isEmpty()) {
            try {
                this.name = cfg.getField("name");
                oaux = cfg.getOle("types");
                for (String oclass : oaux.getFieldList()) {
                    this.getOntology().add(oclass, oaux.getField(oclass));
                }
                this.setOntology(this.getOntology());
                oaux = cfg.getOle("world");
                Thing e = setEnvironment(name).getEnvironment();
                e.setPosition(new Point3D(0, 0, 0));
                e.setOrientation(Compass.NORTH);
                Map2DColor terrain = new Map2DColor(10, 10, 0);
                setSurfaceName(oaux.getField("surface"));
                spalette = oaux.getField("palette");
                terrain.loadMapRaw(getSurfaceName());
                e.setSurface(terrain);
                e.setSize(new Point3D(this._environment._surface.getWidth(),
                        this._environment._surface.getHeight(), 0));
                this.removeAllThings();
                oThings = oaux.get("things").asArray();
                for (JsonValue jsvthing : oaux.get("things").asArray()) {
                    JsonObject jsothing = jsvthing.asObject();
                    ArrayList<String> properties = new ArrayList(Transform.toArrayListString(jsothing.get("properties").asArray()));
                    PROPERTY[] props = new PROPERTY[properties.size()];
                    for (int i = 0; i < properties.size(); i++) {
                        props[i] = PROPERTY.valueOf(properties.get(i).toUpperCase());
                    }
                    e = new Thing("");
                    e.fromJson(jsothing);
                    this.addThing(e, props);
//                    System.out.println("Found thing " + e.getName() + "\n" + e.toString());
                    e.setPosition(placeAtMap(e.getPosition()));
//                    System.out.println("Found thing " + e.getName() + "\n" + e.toString());
                }
                Point3D p;
                JsonArray jsap;
                if (ocfg.getFieldList().contains("start")) {
                    jsap = ocfg.get("start").asArray();
                    p = new Point3D(jsap);
                    p = this.placeAtMap(p);
                    start = p;
                } else {
                    start = null;
                }
                if (ocfg.getFieldList().contains("destination")) {
                    jsap = ocfg.get("destination").asArray();
                    p = new Point3D(jsap);
                    p = this.placeAtMap(p);
                    destination = p;
                } else {
                    destination = null;
                }
                if (ocfg.getFieldList().contains("missions")) {
                    _missions = new MissionSet(ocfg.get("missions").asArray());
                }

            } catch (Exception ex) {
                System.err.println(ex.toString());
                return ex.toString();
            }
//            printSummary();
            return "ok";
        }
        return "Empty configuration";
    }

    public OleConfig getConfig() {
        return this.cfg;
    }

    public World setOntology(Ontology o) {
        _ontology = o;
        this._population.setOntology(_ontology);
        return this;
    }

    public Ontology getOntology() {
        return _ontology;
    }

    public World setEnvironment(String name) {
        _environment = addThing(name, "ENVIRONMENT",
                new PROPERTY[]{PROPERTY.SURFACE, PROPERTY.POSITION, PROPERTY.ORIENTATION});
        _environment.setSize(new Point3D(1, 1, 0));
        _environment.setOrientation(Compass.NORTH);
        return this;
    }

    public Thing getEnvironment() {
        return _environment;
    }

    public MissionSet getMissions() {
        return _missions;
    }
    public Thing addThing(Thing i, PROPERTY[] visible) {
//        if (i.getPosition() == null) {
//            if (!i.getType().equals("ENVIRONMENT")) {
//                i.setPosition(getEnvironment().getPosition());
//                i.setOrientation(getEnvironment().getOrientation());
//            }
//        }
//        if (i.getSize() == null) {
//            i.setSize(new Point3D(1, 1, 0));
//        }
        _population.addThing(i);
        for (int ch = 0; ch < visible.length; ch++) {
            addVisibility(visible[ch], i);
        }
        return i;
    }

    public void removeAllThings() {
        _population.clear();
//        for (PROPERTY p : _visibility.keySet()) {
//            _visibility.put(p, new ArrayList());
//        }
    }

    public void removeThing(Thing i) {
        _population.removeThing(i);
        for (PROPERTY p : PROPERTY.values()) {
            if (this._visibility.get(p) != null
                    && this._visibility.get(p).contains(i)) {
                this._visibility.get(p).remove(i);
            }
        }
    }

    public Thing addThing(String name, String type, PROPERTY[] visible) {
        Thing i = new Thing(name, this);
        i.setType(type);
        return addThing(i, visible);
    }

    public Thing getThing(String id) {
        return _population.getThing(name);
    }

    public boolean findThing(String id) {
        return _population.getAllNames().contains(id);
    }

    public ArrayList<Thing> splitListAllThings(String type) {
        return _population.splitListByType(type);
    }

    public ThingSet splitSetAllThings(String type) {
        return _population.splitSetByType(type);
    }

    public World addVisibility(PROPERTY c, Thing t) {
        if (_visibility.get(c) == null) {
            _visibility.put(c, new ArrayList<>());
        }
        _visibility.get(c).add(t);
        return this;
    }

    public ThingSet getDetectableList(Perceptor p) {
        PROPERTY property = p.getProperty();
        String type = p.getType();
        Thing who = p.getOwner();
        ThingSet detectable = new ThingSet();

        // Intern sensors only perceive oneself
        if (p.getSelection() == SELECTION.INTERN) {
            detectable.addThing(who);
            return detectable;
        }
        // An Thing is detectable if the propoerty of the sensor is
        // visible on it and is the detected type
        for (Thing t : _visibility.get(property)) {
            if (!t.equals(who) && _ontology.matchTypes(t.getType(), type)) {
                detectable.addThing(t);
            }
        }
        // Sometimes only the closest Thing is detected
        if (p.getSelection() == SELECTION.CLOSEST && detectable.size() > 1) {
            Point3D mypos = p.getOwner().getPosition();
            Point3D yourpos;
            double shortest = Double.MAX_VALUE, distance;
            Thing best = null;
//            for (int i = 0; i < detectable.size(); i++) {
            for (Thing ti : detectable.getAllThings()) {
                yourpos = ti.getPosition();
                distance = mypos.planeDistanceTo(yourpos);
                if (distance < shortest) {
                    shortest = distance;
                    best = ti;
                }
            }
            detectable.clear();
            detectable.addThing(best);
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
        ArrayList<Thing> detectable = getDetectableList(p).getAllThings();
        Point3D point;
        Point3D pini, pend;
        SimpleVector3D vectororientation;
        point = who.getPosition();
        vectororientation = who.getVector();
        int range = p.getRange(), orientation = who.getOrientation();
        Point3D prange;
        Point3D observable;
        PolarSurface ps = new PolarSurface(vectororientation);
        ps.setRadius(range / 2 + 1);
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
        int presence;
        for (double sy = y1; sy < y1 + range; sy++) {
            rowreading = new JsonArray();
            for (double sx = x1; sx < x1 + range; sx++) {
                xyreading = new JsonArray();
                // For every position xy check all the potentially detected objects
                observable = new Point3D(sx, sy, this.getEnvironment().getSurface().getStepLevel(sx, sy));
                for (Thing t : detectable) {
                    // Start reading properties
//                    if (property == PROPERTY.SURFACE || property==PROPERTY.PRESENCE) {
//                        observable = new Point3D(sx, sy, t.getSurface().getStepLevel(sx, sy));
//                    } else {
//                        observable = new Point3D(sx, sy);
//                    }
                    if (observable.planeDistanceTo(t.getPosition()) <= p.getSensitivity()) {
                        if (operation == OPERATION.QUERY) {
//                            if (property == PROPERTY.ENERGY) {
//                                partialres = new JsonObject().add("value", t.Raw().getEnergy());
//                            }
//                            if (property == PROPERTY.STATUS) {
//                                partialres = new JsonObject().add("value", t.Raw().getAlive());
//                            }
//                            if (property == PROPERTY.ONTARGET) {
//                                partialres = new JsonObject().add("value", t.Raw().getOnTarget());
//                            }
//                            if (property == PROPERTY.PAYLOAD) {
//                                partialres = new JsonObject().add("value", t.Raw().getPayload());
//                            }
                            if (property == PROPERTY.POSITION) {
                                partialres = new JsonObject().add("value", t.getPosition().toJson());
                            }
                            if (property == PROPERTY.ORIENTATION) {
                                partialres = new JsonObject().add("value", t.getVector().toJson());
                            }
                            if (property == PROPERTY.PRESENCE) {
                                if (t.getPosition().to2D().isEqualTo(observable.to2D())) {
                                    partialres = new JsonObject().add("value", 255);
                                } else {
                                    partialres = new JsonObject().add("value", 0);
                                }

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
                                partialres = new JsonObject().add("value", observable.planeDistanceTo(t.getPosition()));
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
                                    partialres = new JsonObject().add("value", Compass.VECTOR[Compass.NORTH].angleXYTo(new Vector3D(observable, t.getPosition())));
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
//                        if (p.getName().toUpperCase().startsWith("THERMAL")) {
//                            xyreading.add(p.getSensitivity());
//                        } else {
//                            xyreading.add(Perceptor.NULLREAD);
//                        }
                        xyreading.add(Perceptor.NULLREAD);
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
                    if (xyreading.get(0).isNumber()) { /// 
                        int max = xyreading.get(0).asInt();
                        for (JsonValue v : xyreading) {
                            if (v.asInt() > max) {
                                max = v.asInt();
                            }
                        }
                        rowreading.add(max);

//                        double min = xyreading.get(0).asDouble();
//                        for (JsonValue v : xyreading) {
//                            if (v.asDouble() < min) {
//                                min = v.asDouble();
//                            }
//                        }
//                            rowreading.add(min);
//                        if (p.getAttachment() == ATTACH.ZENITAL) {
//                            rowreading.add(min);
//                        } else {
//                            if (filterReading((int) (sx - x1), (int) (sy - y1), range, orientation)) {
//                                rowreading.add(min);
//                            } else {
//                                rowreading.add(Perceptor.NULLREAD);
//                            }
//                        }
                    } else {
                        rowreading.add(xyreading);
                    }
                    //coordinates.add(new JsonObject().add("xy", new Point3D(sx, sy).toJson()));
                }
            }
            if (rowreading.size() == 1) {
                allreadings.add(rowreading.get(0));
            } else {
                allreadings.add(rowreading);
                coordinates.add(new Point3D(Math.round(x1), Math.round(sy)).toJson());
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

    public Thing getThingByName(String name) {
        return _population.getThing(name);
    }

    public ArrayList<String> getAllThingsNear(String from, String type, int radius) {
        ArrayList<String> res = new ArrayList();
        Thing tfrom;
        tfrom = this.getThingByName(from);
        for (Thing tto : _population.splitListByType(type)) {
            if (tfrom.getPosition().realDistanceTo(tto.getPosition()) <= radius) {
                res.add(tto.getName());
            }
        }
        Collections.sort(res);
        return res;
    }

    public Point3D getClosestDocking(Point3D ptentative) {
        Point3D res = ptentative.clone(), res2;
        Map2DColor map = getEnvironment().getSurface();
        int k = 10, cx = 0, cy = 0, n = 0;
        // COG
        for (int x = 0; x <= k; x++) {
            for (int y = 0; y <= k; y++) {
                res.setX(ptentative.getX() - x + k / 2.0);
                res.setY(ptentative.getY() - y + k / 2.0);
                res = this.placeAtMap(res);
                if (res.getZ() < 5) {
                    cx += res.getXInt();
                    cy += res.getYInt();
                    n++;
                }
            }
        }
        if (n > 0) {
            res.setX(cx / n);
            res.setY(cy / n);
            res = this.placeAtMap(res);
        }

        // Approach
        res2 = res.clone();
        while (res.getZ() == 0) {
            res2 = res.clone();
            if (res.getX() > ptentative.getX()) {
                res.setX(res.getX() - 1);
            } else if (res.getX() < ptentative.getX()) {
                res.setX(res.getX() + 1);
            }
            if (res.getY() > ptentative.getY()) {
                res.setY(res.getY() - 1);
            } else if (res.getY() < ptentative.getY()) {
                res.setY(res.getY() + 1);
            }
            res = this.placeAtMap(res);
        }
        return res2;
    }
//    public Point3D getClosestDocking(Point3D ptentative) {
//        Point3D res = ptentative.clone();
//        Map2DColor map = getEnvironment().getSurface();
//        for (int k = 0; k < 10; k++) {
//            for (int x = 0; x <= k; x++) {
//                for (int y = 0; y <= k; y++) {
//                    res.setX(ptentative.getX() - x + k / 2.0);
//                    res.setY(ptentative.getY() - y + k / 2.0);
//                    res=this.placeAtMap(res);
//                    if (res.getZ()< 5) {
//                        return res;
//                    }
//                }
//            }
//        }
//        return null;
//    }

    public liveBot registerAgent(String name, String type, Sensors[] attach) {
        liveBot liveagent = null;
        try {
            // Already registered?
            liveagent = new liveBot(name, this);
            liveagent.setType(type);

            // PERCEIVE
            Perceptor p;

            // The agent really perceives everything except HQ's
            // Later on, this list filters out any sensor not included in capabilities
            for (Sensors allsensor : attach) {
                switch (allsensor) {
                    case COMPASS:
                        p = new Perceptor(glossary.Sensors.COMPASS.name().toLowerCase(), liveagent);
                        p.setWhatPerceives(Thing.PROPERTY.ORIENTATION, "ENVIRONMENT", Perceptor.SELECTION.CLOSEST);
                        p.setHowPerceives(Perceptor.OPERATION.ANGLE, 1).setSensitivity(10000);
                        liveagent.addSensor(p);
                        break;
                    case GPS:
                        p = new Perceptor(glossary.Sensors.GPS.name().toLowerCase(), liveagent);
                        p.setWhatPerceives(Thing.PROPERTY.POSITION, "", Perceptor.SELECTION.INTERN);
                        p.setHowPerceives(Perceptor.OPERATION.QUERY, 1).setSensitivity(10000);
                        liveagent.addSensor(p);
                        break;
                    case LIDAR:
                        p = new Perceptor(glossary.Sensors.LIDAR.name().toLowerCase(), liveagent);
                        p.setWhatPerceives(Thing.PROPERTY.SURFACE, "ENVIRONMENT", Perceptor.SELECTION.CLOSEST);
                        p.setHowPerceives(Perceptor.OPERATION.DISTANCE, liveagent.Raw().getRange());
                        p.setAttacment(Perceptor.ATTACH.ZENITAL).setSensitivity(10000);
                        liveagent.addSensor(p);
                        break;
                    case VISUAL:
                        p = new Perceptor(glossary.Sensors.VISUAL.name().toLowerCase(), liveagent);
                        p.setWhatPerceives(Thing.PROPERTY.SURFACE, "ENVIRONMENT", Perceptor.SELECTION.CLOSEST);
                        p.setHowPerceives(Perceptor.OPERATION.QUERY, liveagent.Raw().getRange());
                        p.setAttacment(Perceptor.ATTACH.ZENITAL).setSensitivity(10000);
                        liveagent.addSensor(p);
                        break;
                    case THERMAL:
                        p = new Perceptor(glossary.Sensors.THERMAL.name().toLowerCase(), liveagent);
                        p.setWhatPerceives(Thing.PROPERTY.PRESENCE, "PEOPLE", Perceptor.SELECTION.ALL);
                        p.setHowPerceives(Perceptor.OPERATION.QUERY, liveagent.Raw().getRange());
                        p.setAttacment(Perceptor.ATTACH.ZENITAL).setSensitivity(10000);
                        liveagent.addSensor(p);
                        break;
                    case GROUND:
                        p = new Perceptor(glossary.Sensors.GROUND.name().toLowerCase(), liveagent);
                        p.setWhatPerceives(Thing.PROPERTY.SURFACE, "ENVIRONMENT", Perceptor.SELECTION.CLOSEST);
                        p.setHowPerceives(Perceptor.OPERATION.DISTANCE, 1).setSensitivity(10000);
                        p.setRange(1);
                        liveagent.addSensor(p);
                        break;
                }
            }
            liveagent.Raw().encodeSensor(Sensors.NAME, liveagent.getName());
            liveagent.Raw().encodeSensor(Sensors.ENERGY, liveagent.Raw().getAutonomy());
            addThing(liveagent, new PROPERTY[]{PROPERTY.POSITION, PROPERTY.PRESENCE});
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
        return liveagent;
    }

//    public void checkStatus(liveBot agent) {
//        boolean single = false, multiple = true, crashtotoher,
//                crashtoground, crashtolevel, crashtoenergy, crashtoborder;
//        if (agent == null) {
//            return;
//        }
//        agent.Raw().setEnergy((int) Math.max(agent.Raw().getEnergy(), 0));
//        crashtoenergy = agent.Raw().getEnergy() < 1;
//        if (crashtoenergy) {
//            agent.Raw().setStatus("Energy exhausted. ");
//        }
//        crashtoground = agent.getPosition().getZ() < getEnvironment().getSurface().getStepLevel(agent.getPosition().getX(), agent.getPosition().getY());
//        if (crashtoground) {
//            agent.Raw().setStatus("Crash onto the ground. ");
//        }
//        crashtoborder = agent.getPosition().getX() < 0 || agent.getPosition().getX() >= getEnvironment().getSurface().getWidth()
//                || agent.getPosition().getY() < 0 || agent.getPosition().getY() >= getEnvironment().getSurface().getHeight();
//        if (crashtoborder) {
//            agent.Raw().setStatus("Crash onto world's boundaries. ");
//        }
//        crashtolevel = agent.getPosition().getZ() > agent.Raw().getMaxlevel()
//                || agent.getPosition().getZ() < agent.Raw().getMinlevel();
//        if (crashtolevel) {
//            agent.Raw().setStatus("Out ot operational altitude margins ");
//        }
//        single = (!(crashtoenergy
//                || crashtolevel
//                || crashtoborder
//                //             || getEnvironment().getSurface().getStepLevel(agent.getPosition().getx, agent.getPosition().gety) == Sensor.
//                || crashtoground));
//        agent.Raw().setAlive(single);
//        agent.Raw().setOntarget(isGoal(agent));
//        agent.setCurrentDistance((int) agent.Raw().getDistance());
//        if (agent.getInitialDistance() < 0) {
//            agent.setInitialDistance(agent.getCurrentDistance());
//        }
//        
//    }
//
//    public boolean isGoal(liveBot agent) {
//        return agent.Raw().getGPSMemory().isEqualTo(agent.Raw().getTarget());
//    }
//    
    public String locateAgent(liveBot agent, String city) {
        String res = this.setBaseIn(agent, city);
        if (res.length() > 0) {
            return res;
        }
        agent.readPerceptions();
        return "";
    }

    public void locateAgent(liveBot agent, Point3D pbase) {
        if (pbase.getX() < 0 || pbase.getY() < 0) {
            if (getStart() != null) {
                agent.setPosition(getStart());
            } else {
            }
        } else {
            agent.setPosition(this.placeAtMap(pbase));
        }

        agent.setOrientation(Compass.NORTH);
        if (getDestination() != null) {
            agent.Raw().cleanCourse();
            agent.Raw().addCourse(getDestination());
            agent.Raw().activateCourse();
        }
        agent.readPerceptions();
    }

    public boolean execAgent(liveBot agent, String action) {
        boolean res;
        if (agent == null) {
            return false;
        }
        if (agent.getCapabilities().indexOf(action) >= 0 && agent.Raw().getAlive()) {
            agent.Raw().setStatus("");
            glossary.capability enumaction = glossary.capability.valueOf(action);
            switch (enumaction) {
                case MOVE:
                    agent.moveForward(1);
                    if (!getOntology().matchTypes(agent.getType(), "AIRBORNE")) {
                        agent.getPosition().setZ(getEnvironment().getSurface().getStepLevel(agent.getPosition()));
//                        agent.setPosition(new SimpleVector3D(this.placeAtMap("choice", agent.getPosition()),agent.getOrientation()));
                    }
//                    agent.Raw().setEnergy(agent.Raw().getEnergy() - agent.Raw().getBurnratemove());
//                    agent.Raw().setEnergyburnt(agent.Raw().getEnergyburnt() + agent.Raw().getBurnratemove());

                    res = true;
                    break;
                case LEFT:
                    agent.rotateLeft();
//                    agent.Raw().setEnergy(agent.Raw().getEnergy() - agent.Raw().getBurnratemove());
//                    agent.Raw().setEnergyburnt(agent.Raw().getEnergyburnt() + agent.Raw().getBurnratemove());
                    res = true;
                    break;
                case RIGHT:
                    agent.rotateRight();
//                    agent.Raw().setEnergy(agent.Raw().getEnergy() - agent.Raw().getBurnratemove());
//                    agent.Raw().setEnergyburnt(agent.Raw().getEnergyburnt() + agent.Raw().getBurnratemove());
                    res = true;
                    break;
                case DOWN:
                    agent.moveDown(5);
//                    agent.Raw().setEnergy(agent.Raw().getEnergy() - agent.Raw().getBurnratemove());
//                    agent.Raw().setEnergyburnt(agent.Raw().getEnergyburnt() + agent.Raw().getBurnratemove());
                    res = true;
                    break;
//                case CAPTURE:
//                    double x = agent.getPosition().getX(),
//                     y = agent.getPosition().getY(),
//                     z = agent.getPosition().getZ(),
//                     terrainz = getEnvironment().getSurface().getStepLevel(x, y);
//                    if (z - terrainz <= 5) {
//                        agent.moveDown((int) (z - terrainz));
//                                            agent.burnEnergylevel((int) getBurnRate(agent.getRole())); //* (int) (z - terrainz);
//                        agent.addEnergyBurnt ((int) getBurnRate(agent.getRole()));
//                        agent.addNumSteps(1);
//                        res = true;
//                        break;
//                    } else {
//                        agent.Raw().setStatus("Too high to perform touchdown";
//                        res = true;
//                        break;
//                    }
                case UP:
                    int nups;
                    nups = 5;
                    agent.moveUp(nups);
//                    agent.Raw().setEnergy(agent.Raw().getEnergy() - agent.Raw().getBurnratemove());
//                    agent.Raw().setEnergyburnt(agent.Raw().getEnergyburnt() + agent.Raw().getBurnratemove());
                    res = true;
                    break;
                case RECHARGE:
                    if (agent.getPosition().getZ() == getEnvironment().getSurface().getStepLevel(agent.getPosition().getX(),
                            agent.getPosition().getY())) {

                        agent.Raw().setEnergy(agent.Raw().getAutonomy());
                        res = true;

                        break;
                    } else {
                        agent.Raw().addStatus(agent.Raw().getStatus() + "Recharge failed for it is only possible at ground level");
                        res = true;
                        break;
                    }
                case RESCUE:
                case CAPTURE:
//                    String whatname = isGoal(agent);
//                    if (!whatname.equals((""))) {
//                        Thing what = getThing(whatname);
//                        agent.addToSensor(Sensors.CARGO, whatname);
//                        removeThing(what);
//                        agent.Raw().setStatus(agent.Raw().getStatus() +"Captured " + what.getName());
//                        res = true;
//                        break;
//                    } else {
//                        agent.Raw().setStatus(agent.Raw().getStatus() +"Nothing to capture here");
//                        res = true;
//                        break;
//                    }
                default:
                    break;
            }
            agent.Raw().addTrace(action);
            agent.Raw().setGround((int) (agent.getPosition().getZ()) - getEnvironment().getSurface().getStepLevel(agent.getPosition().getX(), agent.getPosition().getY()));
            agent.readPerceptions();
            agent.checkStatus();
            if (agent.Raw().getAlive()) {
                if (agent.Raw().getTarget() != null
                        && agent.Raw().getTarget().to2D().isEqualTo(agent.Raw().getGPS().to2D())
                        && !agent.Raw().getTarget().isEqualTo(agent.Raw().getDestination())) {
                    agent.Raw().nextCourse();
                    agent.checkStatus();
                }
            }
            if (agent.Raw().getStatus().length() > 0) {
                agent.Raw().addTrace(agent.Raw().getStatus());
            }
//            if (agent.Raw().getTarget() == null) {
//                agent.Raw().setTarget(new Point3D(0, 0, 0));
//            }
            return true;
        } else {
            if (agent.getCapabilities().indexOf(action) < 0) {
                agent.Raw().addStatus("Action " + action + " not within its capabilities");
            }
            if (!agent.Raw().getAlive()) {
                agent.Raw().addStatus("Agent is dead");
            }
            return false;
        }
    }

    public boolean isAppropriate(liveBot agent, String location) {
        Thing tdest;
        Point3D pdest;
        int level;
        boolean land;

        // General
        if (this.getThingByName(location) == null) {
            return false;
        }
        tdest = this.getThingByName(location);
        pdest = tdest.getPosition();
        level = this.getEnvironment().getSurface().getStepLevel(pdest);

        if (this.getOntology().matchTypes(agent.getType(), "AIRPLANE")) {
            if (!tdest.isHasAirport()) {
                return false;
            }
        } else if (agent.getType().equals("FLYINGBOAT")) {
            if (!tdest.isHasAirport() && !tdest.isHasPort()) {
                return false;
            }
        } else if (this.getOntology().matchTypes(agent.getType(), "MARINE")) {
            if (!tdest.isHasPort()) {
                return false;
            }
        } else if (agent.Raw().getMinlevel() > level || agent.Raw().getMaxlevel() < level) {
            return false;
        }
        return true;
    }

    public String setBaseIn(liveBot agent, String city) {
        Thing tdest;
        Point3D pdest;
        ArrayList<Point3D> res;
        if (getThingByName(city) == null) {
            return "City " + city + " does not exist";
        }
        if (!this.isAppropriate(agent, city)) {
            return "City " + city + " could not be selected for agent " + agent.getName();
        }
        tdest = this.getThingByName(city);
        pdest = tdest.getPosition();
        if (agent.getType().equals("CORSAIR")) {
            agent.setPosition(this.placeAtMap(this.getClosestDocking(pdest)));
        } else {
            agent.setPosition(pdest);
        }
        agent.Raw().setCityBase(city);
        return "";
    }

    public String setCourseIn(liveBot agent, String city) {
        Thing tdest;
        Point3D pdest;
        ArrayList<Point3D> res;
        JsonArray jsares = new JsonArray();
        if (getThingByName(city) == null) {
            return "City " + city + " does not exist";
        }
        if (!this.isAppropriate(agent, city)) {
            return "City " + city + " could not be selected for agent " + agent.getName();
        }
        tdest = this.getThingByName(city);
        pdest = tdest.getPosition();
        AStar a;
        a = new AStar(this.getEnvironment().getSurface());
        a.setView(null);
        a.setApp(null);
        a.setMaxSeconds(30);
        a.setMaxDepth(1000);
        a.setMinlevel(agent.Raw().getMinlevel());
        a.setMaxlevel(agent.Raw().getMaxlevel());
        a.setMaxslope(agent.Raw().getMaxslope());
        if (this.getOntology().matchTypes(agent.getType(), "AIRBORNE")) {
            a.setType(PathType.AIRBORNE);
        } else if (this.getOntology().matchTypes(agent.getType(), "MARINE")) {
            a.setType(PathType.MARINE);
            pdest = this.getClosestDocking(pdest);
        } else if (this.getOntology().matchTypes(agent.getType(), "HUMMER")) {
            a.setType(PathType.FULLTERRAIN);
        } else {
            a.setType(PathType.ROAD);
        }
        res = new ArrayList();
        Plan path;
        path = a.SearchLowest(new Choice(agent.Raw().getGPS()), new Choice(pdest));
        if (path != null) {
            int i = 0, istep = agent.Raw().getRange() / 2;
            for (Choice c : path) {
                if ((++i) % istep == 0 || i == path.size() - 1) { // 
                    jsares.add(c.getPosition().toJson());
                }
            }
            agent.Raw().encodeSensor(Sensors.COURSE, jsares);
            agent.Raw().activateCourse();
            agent.Raw().setCityDestination(city);
            return "";
        } else {
            return "Could not find a path to " + city + " for agent " + agent.getName();
        }
    }

    public String setCourseTo(liveBot agent, int tx, int ty) {
        Thing tdest;
        Point3D pdest;
        ArrayList<Point3D> res;
        JsonArray jsares = new JsonArray();
        pdest = this.placeAtMap(new Point3D(tx, ty, 0));
        jsares.add(pdest.toJson());
        agent.Raw().encodeSensor(Sensors.COURSE, jsares);
        agent.Raw().activateCourse();
        return "";
    }

public Point3D getDestination() {
        return destination;
    }

    public void setDestination(Point3D destination) {
        this.destination = destination;
    }

    public Point3D getStart() {
        return start;
    }

    public void setStart(Point3D start) {
        this.start = start;
    }

    public void printSummary() {
        System.out.println("World " + getName());
        System.out.println("Types " + getOntology().toString());
        ArrayList<String> types = this._population.indexType.getKeys();
        System.out.println("Existing types: " + types);
        for (String stype : types) {
            System.out.println("Existing names of type " + stype + " : " + this._population.splitSetByType(stype).getAllNames());
        }
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
//        Point3D point, pini, pend;
//        Vector3D orientation;
//        point = who.getPosition();
//        orientation = who.getVector();
//        int range = p.getRange();
//        double x1, y1, x2, y2;
//        if (range == 1) { // single rangle
//            x1 = x2 = point.getX();
//            y1 = y2 = point.getY();
//        } else {        // multiple range
//            Vector3D vp1, vp1p2;
//            if (p.getAttachment() == ATTACH.FRONTAL) {
//                vp1 = Compass.VECTOR[Entity3D.rotateLeft(Entity3D.rotateLeft(who.getOrientation()))].clone().scalar(range / 2);
//                vp1p2 = Compass.VECTOR[Entity3D.rotateRight(who.getOrientation())].clone().scalar(range - 1);
//            } else if (p.getAttachment() == ATTACH.LEFT) {
//                vp1 = Compass.VECTOR[who.getOrientation()].clone().scalar(range / 2);
//                vp1p2 = Compass.VECTOR[Entity3D.rotateLeft(Entity3D.rotateLeft(Entity3D.rotateLeft(who.getOrientation())))].clone().scalar(range - 1);
//            } else if (p.getAttachment() == ATTACH.RIGHT) {
//                vp1 = Compass.VECTOR[Entity3D.Opposite(who.getOrientation())].clone().scalar(range / 2);
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
//                Point3D observable = new Point3D(sx, sy);
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
//                                    partialres = new JsonObject().add("value", Compass.VECTOR[Compass.NORTH].angleXYTo(new Vector3D(observable, t.getPosition())));
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
//            return res.add("sensor", p.getName()).add("data", allreadings).add("range_from", new Point3D(x1, y1).toJson()).add("range_to", new Point3D(x2, y2).toJson());
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
