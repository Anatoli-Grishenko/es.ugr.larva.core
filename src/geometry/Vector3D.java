/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import JsonObject.JsonArray;
import JsonObject.JsonObject;

/**
 *
 * @author lcv
 */
public class Vector3D {


//    public static final Vector3D WINDROSE2D[] = new Vector3D[]{new Vector3D(new Point3D().define(1, 0)), new Vector3D(new Point3D().define(1, 1)),
//        new Vector3D(new Point3D().define(0, 1)), new Vector3D(new Point3D().define(-1, 1)), new Vector3D(new Point3D().define(-1, 0)),
//        new Vector3D(new Point3D().define(-1, -1)), new Vector3D(new Point3D().define(0, -1)), new Vector3D(new Point3D().define(1, -1))};
//    public static final int EAST = 0, SOUTHEAST = 1, SOUTH = 2, SOUTHWEST = 3, WEST = 4, NORTHWEST = 5, NORTH = 6, NORTHEAST = 7, UP = 8, DOWN = 10;
//    public static final Vector3D COMPASS[] = new Vector3D[]{WINDROSE2D[EAST], WINDROSE2D[SOUTHEAST], WINDROSE2D[SOUTH], WINDROSE2D[SOUTHWEST],
//        WINDROSE2D[WEST], WINDROSE2D[NORTHWEST], WINDROSE2D[NORTH], WINDROSE2D[NORTHEAST],
//        new Vector3D(new Point3D().define(0, 0, 1)), new Vector3D(new Point3D().define(0, 0, 1))};
    protected Point3D source, target, canonical;

    public Vector3D(Point3D t) {
        source = t.getOrigin();
        target = t.clone();
        update();
    }

    public Vector3D(Point3D s, Point3D t) {
        source = s.clone();
        target = t.clone();
        update();
    }

    public Vector3D(Vector3D origin) {
        source = origin.getSource().clone();
        target = origin.getTarget().clone();
        update();
    }

    protected Vector3D update() {
        canonical = getTarget().clone().minus(getSource());
        return this;
    }

    public Vector3D setSource(Point3D s) {
        source = s.clone();
        update();
        return this;
    }

    public Vector3D setTarget(Point3D t) {
        target = t.clone();
        update();
        return this;
    }

    public Point3D getSource() {
        return source;
    }

    public Point3D getTarget() {
        return target;
    }

    public Vector3D canonical() {
        return new Vector3D(getSource().getOrigin(), canonical);
    }
    
    public double modulo() {
        return getSource().realDistanceTo(getTarget());
    }

    public double moduloX() {
        return canonical.getX();
    }

    public double moduloY() {
        return canonical.getY();
    }

    public double moduloZ() {
        return canonical.getZ();
    }

// [-180, 180]
    public double angleXYTo(Vector3D other) {
        double angle;

        Vector3D v1 = this.to2D();
        Vector3D v2 = other.to2D();
        angle = Math.acos(v1.scalarProductTo(v2) / (v1.modulo() * v2.modulo()));
        if (v1.canonical().moduloX() * v2.canonical().moduloY() - v1.canonical().moduloY() * v2.canonical().moduloX() < 0) {
            angle = -angle;
        }
        angle = Math.toDegrees(angle);
        angle = 360-angle;
        if (angle >= 360)
            angle -=360;
        if (angle<0)
            angle+=360;
        return angle;
    }

// [-180, 180]
    public double angleXYTo(Point3D target) {
        Vector3D lookto = new Vector3D(getSource().to2D(), target.to2D());
        return angleXYTo(lookto);
    }

// [0-360)
//    public double angleXY() {
//        double a = -angleXYTo(new Vector3D(new Point3D(1,0)));
////    if (a>0) a = 180+a;
//        return a;
//    }

    public double scalarProductTo(Vector3D other) {
        return this.canonical().getTarget().times(other.canonical().getTarget()).sum();
    }

    public Vector3D scalar(double f) {
        setTarget(getSource().clone().plus(canonical().getTarget().scalar(f)));
        return this;
    }

    @Override
    public String toString() {
        return getSource().toString() + "->" + canonical.toString();
    }

    public JsonArray toJson() {
        return canonical().getTarget().toJson();
    }

    public Vector3D fromJson(JsonArray p) {
        Point3D t = new Point3D(0);
        t.fromJson(p);
        if (t != null) {
            source = t.getOrigin();
            target=t;
            update();
            return this;
        } else {
            return null;
        }

    }
    
    public Vector3D left90() {
        return new Vector3D(this.getSource(), this.getSource().plus(
                new Point3D(-this.canonical().getTarget().getY(), this.canonical().getTarget().getX())));
    }

    public Vector3D right90() {
        return new Vector3D(this.getSource(), this.getSource().plus(
                new Point3D(-this.canonical().getTarget().getY(), -this.canonical().getTarget().getX())));
    }

    public Vector3D to2D() {
        return new Vector3D(this.getSource().to2D(), this.getTarget().to2D());
    }

    @Override
    public Vector3D clone() {
        return new Vector3D(getSource(), getTarget());
    }

}
