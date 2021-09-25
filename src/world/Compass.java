/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import geometry.Point;
import geometry.Vector;



/**
 *
 * @author lcv
 */
public class Compass extends Vector {
    public static final int NORTH = 0, NORTHEAST = NORTH+1,EAST = NORTH+2, SOUTHEAST = NORTH+3, SOUTH = NORTH+4, SOUTHWEST = NORTH+5, WEST = NORTH+6, NORTHWEST = NORTH+7,  UP = 8, DOWN = 9;
    public static final Vector[] VECTOR = new Vector[]{new Vector(new Point(0, -1)), new Vector(new Point(1, -1)),new Vector(new Point(1, 0)), new Vector(new Point(1, 1)),
        new Vector(new Point(0, 1)), new Vector(new Point(-1, 1)), new Vector(new Point(-1, 0)),
        new Vector(new Point(-1, -1)),  new Vector(new Point(0,0, 1)), new Vector(new Point(0, 0, -1))};

    
    public Compass(Point t) {
        super(t);
    }
    
    
    
}
