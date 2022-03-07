///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package games;
//
//import static crypto.Keygen.getHexaKey;
//import java.awt.Image;
//import javax.swing.ImageIcon;
//import map2D.Map2DColor;
//
///**
// *
// * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
// */
//public class Sprite {
//
//    protected ImageIcon sprite;
//
//    public Sprite() {
//        sprite = null;
//    }
//
//    public boolean isValid() {
//        return sprite != null;
//    }
//
//    public Sprite(String folder, String name) {
//        this.spLoad(folder, name);
//    }
//
//    public final boolean spLoad(String folder, String name) {
//        try {
//            sprite = new ImageIcon(folder + "/" + name );
//            return true;
//        } catch (Exception ex) {
//            return false;
//        }
//    }
//
//    public int spWidth() {
//        if (isValid()) {
//            return sprite.getIconWidth();
//        } else {
//            return -1;
//        }
//    }
//
//    public int spHeight() {
//        if (isValid()) {
//            return sprite.getIconHeight();
//        } else {
//            return -1;
//        }
//    }
//
//    public Image getSprite() {
//        return sprite.getImage();
//    }
//}
