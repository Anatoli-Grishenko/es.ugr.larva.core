///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package games;
//
//import geometry.Point3D;
//import java.awt.Graphics2D;
//
///**
// *
// * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
// */
//public abstract class GameObject {
//    protected String id;
//    protected double x,y;
//    int cellx, celly;
//    protected int plane;
//    protected int radius;
//    protected GameScene.Align horizontal, vertical;
////    protected Sprite sprite;
//
//    public GameObject(String id) {
//        this.id = id;
//        this.x = 0;
//        this.y = 0;
//        this.plane = 0;
//        this.radius = 0;
//    }
//    
//    public GameObject(String id, int x, int y, int plane, int radius) {
//        this.id = id;
//        this.x = x;
//        this.y = y;
//        this.plane = plane;
//        this.radius = radius;
//    }
//    
//    public abstract void showGameObject(GameScene gs);
//    
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public double getX() {
//        return x;
//    }
//
//    public void setX(double x) {
//        this.x = x;
//    }
//
//    public double getY() {
//        return y;
//    }
//
//    public void setY(double y) {
//        this.y = y;
//    }
//
//    public int getPlane() {
//        return plane;
//    }
//
//    public void setPlane(int plane) {
//        this.plane = plane;
//    }
//
//    public int getRadius() {
//        return radius;
//    }
//
//    public void setRadius(int radius) {
//        this.radius = radius;
//    }
//
//    public GameScene.Align getHorizontal() {
//        return horizontal;
//    }
//
//    public void setHorizontal(GameScene.Align horizontal) {
//        this.horizontal = horizontal;
//    }
//
//    public GameScene.Align getVertical() {
//        return vertical;
//    }
//
//    public void setVertical(GameScene.Align vertical) {
//        this.vertical = vertical;
//    }
//
////    public Sprite getSprite() {
////        return sprite;
////    }
////
////    public void setSprite(Sprite sprite) {
////        this.sprite = sprite;
////    }
//    
//    
//    
//}
