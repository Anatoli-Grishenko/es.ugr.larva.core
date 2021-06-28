/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

/**
 *
 * @author lcv
 */
public class Vector {

//    public static final Vector WINDROSE2D[] = new Vector[]{new Vector(new Point().define(1, 0)), new Vector(new Point().define(1, 1)),
//        new Vector(new Point().define(0, 1)), new Vector(new Point().define(-1, 1)), new Vector(new Point().define(-1, 0)),
//        new Vector(new Point().define(-1, -1)), new Vector(new Point().define(0, -1)), new Vector(new Point().define(1, -1))};
//    public static final int EAST = 0, SOUTHEAST = 1, SOUTH = 2, SOUTHWEST = 3, WEST = 4, NORTHWEST = 5, NORTH = 6, NORTHEAST = 7, UP = 8, DOWN = 10;
//    public static final Vector COMPASS[] = new Vector[]{WINDROSE2D[EAST], WINDROSE2D[SOUTHEAST], WINDROSE2D[SOUTH], WINDROSE2D[SOUTHWEST],
//        WINDROSE2D[WEST], WINDROSE2D[NORTHWEST], WINDROSE2D[NORTH], WINDROSE2D[NORTHEAST],
//        new Vector(new Point().define(0, 0, 1)), new Vector(new Point().define(0, 0, 1))};
    protected Point _source, _target, _canonical;

    public Vector(Point t) {
        _source = t.getOrigin();
        _target = t.clone();
        update();
    }

    public Vector(Point s, Point t) {
        _source = s.clone();
        _target = t.clone();
        update();
    }

    protected Vector update() {
        _canonical = getTarget().clone().minus(getSource());
        return this;
    }

    public Vector setSource(Point s) {
        _source = s.clone();
        update();
        return this;
    }

    public Vector setTarget(Point t) {
        _target = t.clone();
        update();
        return this;
    }

    public Point getSource() {
        return _source;
    }

    public Point getTarget() {
        return _target;
    }

    public Vector canonical() {
        return new Vector(getSource().getOrigin(), _canonical);
    }
    
    public double modulo() {
        return getSource().realDistanceTo(getTarget());
    }

    public double moduloX() {
        return _canonical.getX();
    }

    public double moduloY() {
        return _canonical.getY();
    }

    public double moduloZ() {
        return _canonical.getZ();
    }

// [-180, 180]
    public double angleXYTo(Vector other) {
        double angle;

        Vector v1 = this.to2D();
        Vector v2 = other.to2D();
//        if (v1.getSource().isEqualTo(v2.getTarget()))
//            return 0;
        angle = Math.acos(v1.scalarProductTo(v2) / (v1.modulo() * v2.modulo()));
        if (v1.canonical().moduloX() * v2.canonical().moduloY() - v1.canonical().moduloY() * v2.canonical().moduloX() < 0) {
            angle = -angle;
        }
        return Math.toDegrees(angle);
    }

// [-180, 180]
    public double angleXYTo(Point target) {
        Vector lookto = new Vector(getSource().to2D(), target.to2D());
        return angleXYTo(lookto);
    }

// [0-360)
//    public double angleXY() {
//        double a = -angleXYTo(new Vector(new Point(1,0)));
////    if (a>0) a = 180+a;
//        return a;
//    }

    public double scalarProductTo(Vector other) {
        return this.canonical().getTarget().times(other.canonical().getTarget()).sum();
    }

    public Vector scalar(double f) {
        setTarget(getSource().clone().plus(canonical().getTarget().scalar(f)));
        return this;
    }

    @Override
    public String toString() {
        return getSource().toString() + "->" + _canonical.toString();
    }

    public JsonArray toJson() {
        return canonical().getTarget().toJson();
    }

    public Vector fromJson(JsonArray p) {
        Point t = new Point(0);
        t.fromJson(p);
        if (t != null) {
            _source = t.getOrigin();
            _target=t;
            update();
            return this;
        } else {
            return null;
        }

    }

    public Vector to2D() {
        return new Vector(this.getSource().to2D(), this.getTarget().to2D());
    }

    @Override
    public Vector clone() {
        return new Vector(getSource(), getTarget());
    }

}
