///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package geometry;
//
//import glossary.direction;
//
///**
// *
// * @author lcv
// */
//public class Compass {
//
//    public static final Vector[] SHIFT = new Vector[]{new Vector(new Point(0, -1)), new Vector(new Point(1, -1)), new Vector(new Point(1, 0)), new Vector(new Point(1, 1)),
//        new Vector(new Point(0, 1)), new Vector(new Point(-1, 1)), new Vector(new Point(-1, 0)),
//        new Vector(new Point(-1, -1)), new Vector(new Point(0, 0, 1)), new Vector(new Point(0, 0, -1))};
//    public static final int[] ANGLE = {0, 45, 90, 135, 180, 225, 270, 315, 0, 0};
//
//    public static int getIndex(direction d) {
//        return d.ordinal();
//    }
//
//    public static String getName(direction d) {
//        return d.name();
//    }
//
//    public static int getAngle(direction d) {
//        if (d.ordinal() < ANGLE.length) {
//            return ANGLE[d.ordinal()];
//        }
//        return -1;
//    }
//
//    public static Vector getVector(direction d) {
//        if (d.ordinal() < SHIFT.length)
//        return SHIFT[d.ordinal()];
//        else
//            return new Vector(new Point(0,0));
//    }
//}
