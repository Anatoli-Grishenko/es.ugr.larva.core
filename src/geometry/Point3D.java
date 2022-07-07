/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import data.Ole;
import data.Ole.oletype;
import world.Perceptor;

/**
 *
 * @author lcv
 */
public class Point3D {

    public static final double _pNULL = 0;
    static final int NDIGITS = 4;
    static final String TEMPLATE = "%0" + NDIGITS + "d";

    private double _coord[];
    private int _dim;

    public Point3D() {
        clear();
        _dim = 3;
        setX(Integer.MIN_VALUE).setY(Integer.MIN_VALUE).setZ(Integer.MIN_VALUE);    
    }

    public Point3D(double x, double y, double z) {
        clear();
        _dim = 3;
        setX(x).setY(y).setZ(z);
    }

    public Point3D(double x, double y) {
        clear();
        _dim = 2;
        setX(x).setY(y);
    }

    public Point3D(double x) {
        clear();
        _dim = 1;
        setX(x);
    }

    public Point3D(JsonArray values) {
        clear();
        
        _dim = 3;
        setX(0).setY(0).setZ(Perceptor.NULLREAD);
        switch(values.size()) {
            case 3:
                setZ(values.get(2).asDouble());
            case 2:
                setY(values.get(1).asDouble());
            case 1:
                setX(values.get(0).asDouble());
        }               
    }

    public Point3D(double values[]) {
        clear();
        _dim = values.length;
        setX(values[0]).
                setY(values[1]).
                setZ(values[2]);
    }

    public Point3D(String spoint) {
        clear();
        String parts[] = spoint.split(",");
        _dim = parts.length;
        switch (_dim) {
            case 3:
                try {
                this.setZ(Double.parseDouble(parts[2]));
            } catch (Exception ex) {
                this.setZ(0);
            }
            case 2:
                try {
                this.setY(Double.parseDouble(parts[1]));
            } catch (Exception ex) {
                this.setY(0);
            }
            case 1:
                try {
                this.setX(Double.parseDouble(parts[0]));
            } catch (Exception ex) {
                this.setX(0);
            }
        }
    }

    public Point3D(Ole o) {
        if (o.getType().equals(oletype.OLEPOINT.name())) {
            clear();
            _dim = o.getInt("dim");
            setX(o.getDouble("X"));
            setY(o.getDouble("Y"));
            setZ(o.getDouble("Z"));
        }

    }
//    public Point3D fromOle(Ole o) {
//        if (o.getType().equals(ole.POINT.name())) {
//            switch() {
//                case 3:
//                    return new Point3D(o.getDouble("X"),o.getDouble("Y"), o.getDouble("Z"));
//                case 2:
//                    return new Point3D(o.getDouble("X"),o.getDouble("Y"));
//                default:
//                    return new Point3D(o.getDouble("X"));
//
//            }
//        } else
//            return new Point3D(0);
//    }
////    public Point3D(int dimension) {
//        _dim=dimension;
//        clear();
//    }

    public int getXInt() {
        return (int) (Math.round(_coord[0]));
    }

    public int getYInt() {
        return (int) (Math.round(_coord[1]));
    }

    public int getZInt() {
        return (int) (Math.round(_coord[2]));
    }

    public double getX() {
        return _coord[0];
    }

    public Point3D setX(double x) {
        _coord[0] = x;
        return this;
    }

    public double getY() {
        return _coord[1];
    }

    public Point3D setY(double y) {
        _coord[1] = y;
        return this;
    }

    public double getZ() {
        return _coord[2];
    }

    public Point3D setZ(double z) {
        _coord[2] = z;
        return this;
    }

    public int getDimension() {
        return _dim;
    }

    public Point3D clear() {
        _coord = new double[]{_pNULL, _pNULL, _pNULL};
        return this;
    }

    public Point3D define(String key) {
        _dim = (key.length() - 1) / (NDIGITS + 1);
        int ini, end;
        switch (_dim) {
            case 3:
                ini = key.length() - TEMPLATE.length();
                end = key.length();
                setZ(Integer.parseInt(key.substring(ini, end - 1)));
                key = key.substring(0, ini);
            case 2:
                ini = key.length() - TEMPLATE.length();
                end = key.length();
                setY(Integer.parseInt(key.substring(ini, end - 1)));
                key = key.substring(0, ini);
            case 1:
                ini = key.length() - TEMPLATE.length();
                end = key.length();
                setX(Integer.parseInt(key.substring(ini, end - 1)));
                break;
        }
        return this;
    }

    private Point3D define(double x, double y, double z) {
        clear();
        _dim = 3;
        return this.setX(x).setY(y).setZ(z);
    }

    private Point3D define(double x, double y) {
        clear();
        _dim = 2;
        return this.setX(x).setY(y);
    }

    private Point3D define(double x) {
        clear();
        _dim = 1;
        return this.setX(x);
    }

    public double planeDistanceTo(Point3D p) {
//        return this.gridDistanceTo(p);
        return this.to2D().realDistanceTo(p.to2D());
//        return this.approx_distance2(p.to2D());
    }

    public int gridDistanceTo(Point3D p) {
        return (int) Math.max(Math.abs(p.getX() - this.getX()), Math.abs(p.getY() - this.getY()));
    }

    public double realDistanceTo(Point3D p) {
        double res = 0;
        int mdim = (int) Math.min(_dim, p.getDimension());
        if (mdim == 1) {
            return Math.abs(getX() - p.getX());
        }
        switch (mdim) {
            case 3:
                res += Math.pow(getZ() - p.getZ(), 2);
            case 2:
                res += Math.pow(getY() - p.getY(), 2);
            case 1:
                res += Math.pow(getX() - p.getX(), 2);
                break;
        }
        return Math.sqrt(res);
    }

    public Point3D getOrigin() {
        Point3D res;
        switch (_dim) {
            case 3:
                res = new Point3D(0, 0, 0);
                break;
            case 2:
                res = new Point3D(0, 0);
                break;
            default:
                res = new Point3D(0);
                break;
        }
        return res;
    }

    public boolean isEqualTo(Point3D p) {
        boolean res = true;
        if (_dim != p.getDimension()) {
            return false;
        }
        switch (_dim) {
            case 3:
                res &= getZ() == p.getZ();
            case 2:
                res &= getY() == p.getY();
            case 1:
                res &= getX() == p.getX();
                break;
        }
        return res;
    }

    public boolean isProjection(Point3D p) {
        boolean res = true;
        switch ((int) Math.min(_dim, p.getDimension())) {
            case 3:
                res &= getZ() == p.getZ();
            case 2:
                res &= getY() == p.getY();
            case 1:
                res &= getX() == p.getX();
                break;
        }
        return res;
    }

    public Point3D plus(Point3D p) {
        switch ((int) Math.min(_dim, p.getDimension())) {
            case 3:
                setZ(getZ() + p.getZ());
            case 2:
                setY(getY() + p.getY());
            case 1:
                setX(getX() + p.getX());
                break;
        }
        return this;
    }

    public Point3D plus(Vector3D v) {
        return plus(v.canonical().getTarget());
    }

    public Point3D invert() {
        switch (_dim) {
            case 3:
                setZ(-getZ());
            case 2:
                setY(-getY());
            case 1:
                setX(-getX());
                break;
        }
        return this;
    }

    public Point3D minus(Point3D p) {
        return this.plus(p.clone().invert());
    }

    public Point3D times(Point3D p) {
        switch ((int) Math.min(_dim, p.getDimension())) {
            case 3:
                setZ(getZ() * p.getZ());
            case 2:
                setY(getY() * p.getY());
            case 1:
                setX(getX() * p.getX());
                break;
        }
        return this;
    }

    public Point3D scalar(double s) {
        switch (_dim) {
            case 3:
                setZ(s * getZ());
            case 2:
                setY(s * getY());
            case 1:
                setX(s * getX());
                break;
        }
        return this;

    }

    public double sum() {
        return getX() + getY() + getZ();
    }

    @Override
    public String toString() {
        String s = "";
        switch (_dim) {
            case 3:
                s = String.format("," + TEMPLATE, (int) getZ()) + s;
            case 2:
                s = String.format("," + TEMPLATE, (int) getY()) + s;
            case 1:
                s = String.format(TEMPLATE, (int) getX()) + s;
                break;
        }
        s = "" + s;
        return s;
    }

    public JsonArray toJson() {
        JsonArray res = new JsonArray();
        res.add(getX());
        res.add(getY());
        res.add(getZ());
        return res;
    }

    public double[] toArray() {
        double[] res = new double[3];
        res[0] = getX();
        res[1] = getY();
        res[2] = getZ();
        return res;
    }

    public Ole toOle() {
        Ole res = new Ole();
        res.setType(oletype.OLEPOINT.name());
        res.setField("dim", _dim);
        switch (_dim) {
            case 3:
                res.setField("Z", getZ());
            case 2:
                res.setField("Y", getY());
            case 1:
                res.setField("X", getX());
                break;
        }
        return res;
    }

    public Point3D fromJson(JsonArray jspa) {
        if (jspa.size() == 3) {
            return new Point3D(jspa.get(0).asDouble(), jspa.get(1).asDouble(), jspa.get(1).asDouble());
        } else if (jspa.size() == 2) {
            return new Point3D(jspa.get(0).asDouble(), jspa.get(1).asDouble());
        } else {
            return new Point3D(jspa.get(0).asDouble());
        }
    }

    public Point3D to3D() {
        return new Point3D(getX(), getY(), getZ());
    }

    public Point3D to2D() {
        return new Point3D(getX(), getY());
    }

    public Point3D to1D() {
        return new Point3D(getX());
    }

    public Point3D invertY() {
        setY(getY() - 1);
        return this;
    }

    @Override
    public Point3D clone() {
        Point3D res = new Point3D(getX(), getY(), getZ());
        res._dim = _dim;
        return res;
    }

    // Fast integer distance https://oroboro.com/fast-approximate-distance/
    private int approx_distance(double ddx, double ddy) {
        int dx, dy, min, max, approx;

        dx = Math.abs((int) Math.round(ddx));
        dy = Math.abs((int) Math.round(ddy));
        if (dx < dy) {
            min = dx;
            max = dy;
        } else {
            min = dy;
            max = dx;
        }

        approx = (max * 1007) + (min * 441);
        if (max < (min << 4)) {
            approx -= (max * 40);
        }

        // add 512 for proper rounding
        return ((approx + 512) >> 10);
    }

    private double approx_distance2(Point3D other) {
        double x = other.getX() - getX(), y = other.getY() - getY(), z = other.getZ() - getZ();
        double res = 0, root2 = 1.4142, root3 = 1.7320, factor2, max, min;

        if (getDimension() == 2 || other.getDimension() == 2) {
            max = x > y ? x : y;

            min = x < y ? x : y;

            if (min < 0.04142135 * max) {
                res = 0.99 * max + 0.197 * min;
            } else {
                res = 0.84 * max + 0.561 * min;
            }
        } else {
            res = this.realDistanceTo(other);
        }

        return res;
    }

    public java.awt.Point getAWTPoint() {
        return new java.awt.Point((int) this.getX(), (int) this.getY());
    }

}
