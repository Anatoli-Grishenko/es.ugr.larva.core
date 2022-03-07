///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package games;
//
//import java.awt.Graphics2D;
//import map2D.Map2DColor;
//
///**
// *
// * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
// */
//public class GameBitmap extends GameObject {
//
//    protected Map2DColor bitmap;
//    
//    protected int width, height;
//
//    public GameBitmap(String folder, String filename) {
//        super(filename);
//        loadBitmap(folder, filename);
//    }
//
//    public void loadBitmap(String folder, String filename) {
//        bitmap = new Map2DColor();
//        try {
//            bitmap.loadMapRaw(folder + "/" + filename + ".png");
//        } catch (Exception ex) {
//        };
//    }
//
//    @Override
//    public void showGameObject(GameScene gs) {
//        double x, y;
//                int w, h;
////                System.out.println("Object: "+getId());
//        // Falta alinear
//        x = getX()*gs.getCell();
//        y = getY()*gs.getCell();
//        w = getWidth()*gs.getCell();
//        h = getHeight()*gs.getCell();
//        
//        gs.getG().drawImage(bitmap.getMap(), (int) x, (int) y, w, h, null);
//    }
//
//    
//    // Rotate left right down up;
//
//    public int getWidth() {
//        return width;
//    }
//
//    public void setWidth(int width) {
//        this.width = width;
//    }
//
//    public int getHeight() {
//        return height;
//    }
//
//    public void setHeight(int height) {
//        this.height = height;
//    }
//
//}
