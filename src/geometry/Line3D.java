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
public class Line3D extends Entity3D{
    protected Point3D end;

    public Point3D getEnd() {
        return end;
    }

    public Line3D setEnd(Point3D end) {
        this.end = end.clone();
        this.center =this.getPosition().clone().plus(end).scalar(0.5);
        return this;
    }

    public Line3D(Point3D start, Point3D end) {
        super();
        this.setPosition(start);
        this.setEnd(end);               
    }    
}
