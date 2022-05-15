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

    public static final int N = 0, NW = 1, W = 2, SW = 3, S = 4, SE = 5, E = 6, NE = 7, ST = 8;
    public static final String Dir[] = new String[]{"N", "NW", "W", "SW", "S", "SE", "E", "NE", ""};
    public static final int nextX[] = new int[]{0, -1, -1, -1, 0, 1, 1, 1, 0};
    public static final int nextY[] = new int[]{-1, -1, 0, 1, 1, 1, 0, -1, 0};
    public static final int inverse[][] = new int[][]{{1, 2, 3}, {0, 8, 4}, {7, 6, 5}};

    protected int sOrient;

    public SimpleVector3D(Point3D t, int orientation) {
        super(t, new Point3D(t.getX() + nextX[(8+orientation) % 8], t.getY() + nextY[(8+orientation) % 8], t.getZ()));
        sOrient = orientation % 8;
    }

    public SimpleVector3D(int x, int y, int orient) {
        super(new Point3D(x, y, 0), new Point3D(x, y, 0).plus(new Point3D(nextX[orient % 8], nextY[orient % 8],0)));
        sOrient = orient % 8;
    }

    public SimpleVector3D(Point3D s, Point3D t) {
        super(s, t);
       int aux=getInverseOrientation();
       if (aux !=8) {           
           sOrient=aux;
       }
    }

    public int getInverseOrientation() {
        int r = this.canonical().getTarget().getXInt() + 1, c = this.canonical().getTarget().getYInt() + 1;
        if (0 <= r && r < 3 && 0 <= c && c < 3) {
            return inverse[r][c];
        } else {
            return 8;
        }
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

    public void setsOrient(int orient) {
        this.sOrient = (orient + 8) % 8;
        setTarget(new Point3D(getSource().getX() + nextX[sOrient % 8], getSource().getY() + nextY[sOrient % 8], getSource().getZ()));
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

    public SimpleVector3D plus(Point3D p) {
        this.getSource().plus(p);
        this.getTarget().plus(p);
        return this;
    }

    public SimpleVector3D minus(Point3D p) {
        this.getSource().minus(p);
        this.getTarget().minus(p);
        return this;
    }

    @Override
    public String toString() {
        return getSource().toString() + "--(" + this.getsOrient() + "|" +Compass.NAME[this.getsOrient()]+"|"+ this.canonical().getTarget().toString() + ")-->" + getTarget().toString();
    }

   public boolean isEqualTo(SimpleVector3D other)  {
       return other.getSource().isEqualTo(getSource())
               && other.getTarget().isEqualTo(getTarget());
   }
}
