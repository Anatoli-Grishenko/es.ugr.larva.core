/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import glossary.direction;

/**
 *
 * @author lcv
 */
public class Compass {

    public static final Vector3D[] SHIFT = new Vector3D[]{new Vector3D(new Point3D(0, -1)), new Vector3D(new Point3D(-1, -1)), new Vector3D(new Point3D(-1, 0)), new Vector3D(new Point3D(-1, 1)),
        new Vector3D(new Point3D(0, 1)), new Vector3D(new Point3D(1, 1)), new Vector3D(new Point3D(1, 0)),
        new Vector3D(new Point3D(1, -1)), new Vector3D(new Point3D(0, 0, 1)), new Vector3D(new Point3D(0, 0, -1))};
    public static final int[] ANGLE = {0, 45, 90, 135, 180, 225, 270, 315, 0, 0};
    public static final String NAME[] = new String[]{"N", "NW", "W", "SW", "S", "SE", "E", "NE", ""};
    public static final int NORTH = 0, NORTWEST = NORTH + 1, WEST = NORTH + 2, SOUTHWEST = NORTH + 3, SOUTH = NORTH + 4, SOUTHEAST = NORTH + 5, EAST = NORTH + 6, NORTHEAST = NORTH + 7, UP = 8, DOWN = 9;
    public static final Vector3D[] VECTOR = new Vector3D[]{new Vector3D(new Point3D(0, -1)), new Vector3D(new Point3D(-1, -1)), new Vector3D(new Point3D(-1, 0)), new Vector3D(new Point3D(-1, 1)),
        new Vector3D(new Point3D(0, 1)), new Vector3D(new Point3D(1, 1)), new Vector3D(new Point3D(1, 0)),
        new Vector3D(new Point3D(1, -1)), new Vector3D(new Point3D(0, 0, 1)), new Vector3D(new Point3D(0, 0, -1))};

    public static int getIndex(direction d) {
        return d.ordinal();
    }

    public static String getName(direction d) {
        return d.name();
    }

    public static int getAngle(direction d) {
        if (d.ordinal() < ANGLE.length) {
            return ANGLE[d.ordinal()];
        }
        return -1;
    }

    public static Vector3D getVector(direction d) {
        if (d.ordinal() < SHIFT.length) {
            return SHIFT[d.ordinal()];
        } else {
            return new Vector3D(new Point3D(0, 0));
        }
    }
}
