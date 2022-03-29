/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class AngleTransporter {

    Point3D points[];
    double resolution;
    int nValues;

    public AngleTransporter(double resolution) {
        this.resolution = resolution;
        nValues = (int) (360 / this.resolution) + 1;
        points = new Point3D[nValues];
        double start = 0;
        while (start < 360) {
            points[getPosition(start)] = new Point3D(0, 0);
            points[getPosition(start)].setX(Math.cos(start * Math.PI / 180)).
                    setY((Math.sin(start* Math.PI / 180)));
            start += resolution;
        }
    }

//    public Point3D alphaPoint(double alpha, double radius) {
//        Point3D res = points[getPosition(alpha)].clone().scalar(radius);
//        return res;
//    }

    public Point3D alphaPoint(double alpha, double radius, Point3D center) { 
        Point3D res = new Point3D(center.getX()+radius*points[getPosition(alpha)].getX(),
        center.getY()-radius*points[getPosition(alpha)].getY());
        return res;
    }

    protected int getPosition(double angle) {
        angle = (int) normalizeAngle(angle);
        return ((int) (angle / resolution)) % nValues;
    }

    public double normalizeAngle(double angle) {
        while (angle < 0) {
            angle += 360;
        }
        while (angle >= 360) {
            angle -= 360;
        }
        return angle;
    }
    
    public double inRads(double angle) {
        return angle/180.0*Math.PI;
    }
    
    public double getAngularDistance(double alpha1, double alpha2) {
//        double a1 = normalizeAngle(alpha1), a2 = normalizeAngle(alpha2);
//        double start = Math.min(a1, a2), end = Math.max(a1, a2);
        return alpha2-alpha1;
    }
}
