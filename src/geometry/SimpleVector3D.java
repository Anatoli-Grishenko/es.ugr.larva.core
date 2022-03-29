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
public class SimpleVector3D extends Vector3D {

    public static final int N = 0, NW = 1, W = 2, SW = 3, S = 4, SE = 5, E = 6, NE = 7;
    public static final String Dir[] = new String[]{"N", "NW", "W", "SW", "S", "SE", "E", "NE"};
    public static final int nextX[] = new int[]{0, -1, -1, -1, 0, 1, 1, 1};
    public static final int nextY[] = new int[]{-1, -1, 0, 1, 1, 1, 0, -1};

    protected int sOrient;

    public SimpleVector3D(Point3D t, int orientation) {
        super(t, new Point3D(t.getX() + nextX[orientation % 8], t.getY() + nextY[orientation % 8]));
        sOrient = orientation % 8;
    }

    public SimpleVector3D(int x, int y, int orient) {
        super(new Point3D(x, y), new Point3D(x, y).plus(new Point3D(nextX[orient % 8], nextY[orient % 8])));
        sOrient = orient % 8;
    }

    @Override
    public SimpleVector3D clone() {
        return new SimpleVector3D(this.getSource().clone(), this.getsOrient());
    }

    public static int right(int d) {
        return (d + 7) % 8;
    }

    public static int left(int d) {
        return (d + 1) % 8;
    }

    public int getsOrient() {
        return sOrient;
    }

    public void setsOrient(int sOrient) {
        this.sOrient = (sOrient + 8) % 8;
    }

    public SimpleVector3D myLeft() {
        return new SimpleVector3D(new Point3D(this.getSource().getX() + nextX[left(left(getsOrient()))], this.getSource().getY() + nextY[left(left(sOrient))]), getsOrient());
    }

    public SimpleVector3D myFrontLeft() {
        return new SimpleVector3D(new Point3D(this.getSource().getX() + nextX[left(getsOrient())], this.getSource().getY() + nextY[left(sOrient)]), getsOrient());
    }

    public SimpleVector3D myFront() {
        return new SimpleVector3D(new Point3D(this.getSource().getX() + nextX[getsOrient()], this.getSource().getY() + nextY[getsOrient()]), getsOrient());
    }

    public SimpleVector3D myFrontRight() {
        return new SimpleVector3D(new Point3D(this.getSource().getX() + nextX[right(getsOrient())], this.getSource().getY() + nextY[right(sOrient)]), getsOrient());
    }

    public SimpleVector3D myRight() {
        return new SimpleVector3D(new Point3D(this.getSource().getX() + nextX[right(right(getsOrient()))], this.getSource().getY() + nextY[right(right(sOrient))]), getsOrient());
    }

    public SimpleVector3D myRearRight() {
        return new SimpleVector3D(new Point3D(this.getSource().getX() + nextX[right(right(right(getsOrient())))], this.getSource().getY() + nextY[right(right(right(sOrient)))]), getsOrient());
    }

    public SimpleVector3D myRear() {
        return new SimpleVector3D(new Point3D(this.getSource().getX() + nextX[right(right(right(right(getsOrient()))))], 
                this.getSource().getY() + nextY[right(right(right(right(sOrient))))]), getsOrient());
    }

    
    public SimpleVector3D myRearLeft() {
        return new SimpleVector3D(new Point3D(this.getSource().getX() + nextX[left(left(left(getsOrient())))], 
                this.getSource().getY() + nextY[left(left(left(sOrient)))]), getsOrient());
    }

    public SimpleVector3D forward() {
        return myFront();
    }

    public SimpleVector3D backward() {
        return myRear();
    }

    @Override
    public String toString() {
        return getSource().toString();
    }

}
