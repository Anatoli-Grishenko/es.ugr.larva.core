/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agswing;

import geometry.Point;
import java.awt.Graphics2D;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public  class Line3D  extends Object3D {
    
    protected Point end;

    public Point getEnd() {
        return end;
    }

    public Object3D setEnd(Point end) {
        this.end = end.clone();
        this.center =this.getPosition().clone().plus(end).scalar(0.5);
        return this;
    }

    public Line3D(Point start, Point end) {
        super();
        this.setPosition(start);
        this.setEnd(end);               
    }


    
}
