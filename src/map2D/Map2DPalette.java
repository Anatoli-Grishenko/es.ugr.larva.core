package map2D;

///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package Map2D;
//
//import ConsoleAnsi.ConsoleAnsi;
//import static ConsoleAnsi.ConsoleAnsi.black;
//import static ConsoleAnsi.ConsoleAnsi.gray;
//import static ConsoleAnsi.ConsoleAnsi.negColor;
//import static ConsoleAnsi.ConsoleAnsi.white;
//import Map2D.Map2DGrayscale;
//import java.util.ArrayList;
//
///**
// *
// * @author lcv
// */
//public class Map2DPalette extends Map2DGrayscale {
//
//    public Palette palette = new Palette().intoBW(256);
//    public boolean framed = false, legend = false;
//    public int ratiox = 1, ratioy = 1, ratio = 1;
//    ArrayList<Integer> xl = new ArrayList<>(), yl = new ArrayList<>(), al = new ArrayList<>();
//
//    public Map2DPalette(int width, int height, int level) {
//        super(width, height, level);
//    }
//
//    public void showMap(int origx, int origy, ConsoleAnsi c) {
//        c.setText(white);
//        c.setBackground(black);
////        c.doRectangleFrame(origx, origy, (getWidth() + 5) * ratiox, (getHeight() + 1) * ratioy + 1);
//        int midx = getWidth() / 2, midy = getHeight() / 2;
//        for (int x = 0; x < getWidth(); x++) {
//            for (int y = 0; y < getHeight(); y++) {
//                int level = getLevel(x, y);
//                if (x == midx && y == midy) {
//                    printCell(origx + x * ratio , origy + y * ratio , level, c);
//                } else {
//                    printCell(origx + x * ratio , origy + y * ratio , level, c);
//                }
//            }
//
//        }
//
//    }
//
//    public void printCell(int x, int y, int level, ConsoleAnsi c) {
//        int backgr;
//        if (level < 0) {
//            backgr = gray;
//        } else {
//            backgr = palette.getColor(level);
//        }
//        c.setBackground(backgr);
//        c.setText(negColor(backgr));
//        c.setCursorXY(x, y).print(" ");
//        //c.doRectangle(x, y, ratio, ratio);
//
////        if (!legend) {
////            c.setCursorXY(x, y + 2);
////            if (level == -1) {
////                c.print("XXX");
////            } else {
////                c.print(String.format("%03d", level));
////            }
////        }
//    }
//
//    public void addThermalSpot(int x, int y) {
//        // this.setLevel(x, y, 255);
//        xl.clear();
//        yl.clear();
//        al.clear();
//        xl.add(x);
//        yl.add(y);
//        al.add(0);
//        propagateThermal();
//    }
//
//    public void propagateThermal() {
//        int salto = 40;
//        if (xl.size() <= 0) {
//            return;
//        }
//        int ix = xl.get(0);
//        xl.remove(0);
//        int iy = yl.get(0);
//        yl.remove(0);
//        int il = al.get(0);
//        al.remove(0);
//
////        if (0 <= ix && ix < getWidth() && 0 <= iy && iy < getHeight() && (getLevel(ix, iy) == 255 || il < getLevel(ix, iy))) {
////            System.out.println("Propagate " + ix + "," + iy + "=" + il); //+);
//        int nil = Math.min(255, il + salto);
//        setLevel(ix, iy, il);
//
//        for (int i = -1; i <= 1; i++) {
//            for (int j = -1; j <= 1; j++) {
//                if (i != 0 || j != 0) {
//                    if (0 <= i + ix && ix + i < getWidth() && 0 <= iy + j && iy + j < getHeight() && nil < getLevel(ix + i, iy + j)) {
//                        setLevel(ix + i, iy + j, nil);
//                        xl.add(ix + i);
//                        yl.add(iy + j);
//                        al.add(nil);
//                    }
//                }
//            }
//        }
////        }
//        this.propagateThermal();
//    }
//
////    public void propagateThermal2(int x, int y, double level, double decre) {
////        double factor = 2, newdecre;
////        if (level > 255) {
////            return;
////        }
////        if (0 <= x && x < getWidth() && 0 <= y && y < getHeight() && (getLevel(x, y) == 255 || level < getLevel(x, y))) {
////            this.setLevel(x, y, (int) level);
////            System.out.println(x + "," + y + "-" + (int) level);
//////            System.out.println(""+x+" "+y+" "+level);
//////            propagateThermal(x-1,y-1,level-skip);
////////            propagateThermal(x-1,y+1,level-skip);
////////            propagateThermal(x+1,y-1,level-skip);
//////            propagateThermal(x+1,y+1,level-skip);
////
//////            newdecre = decre * 1.5;
//////            propagateThermal(x - 1, y, level - decre, newdecre);
//////            propagateThermal(x, y - 1, level - decre, newdecre);
//////            propagateThermal(x, y + 1, level - decre, newdecre);
//////            propagateThermal(x + 1, y, level - decre, newdecre);
////            propagateThermal(x - 1, y, level + factor, decre - 1);
////            propagateThermal(x, y - 1, level + factor, decre - 1);
////            propagateThermal(x, y + 1, level + factor, decre - 1);
////            propagateThermal(x + 1, y, level + factor, decre - 1);
////
////            propagateThermal(x - 1, y - 1, level + factor, decre - 1);
////            propagateThermal(x - 1, y + 1, level + factor, decre - 1);
////            propagateThermal(x + 1, y + 1, level + factor, decre - 1);
////            propagateThermal(x + 1, y - 1, level + factor, decre - 1);
////
//////            propagateThermal(x-1,y,level-skip);
//////            propagateThermal(x,y-1,level-skip);
//////            propagateThermal(x,y+1,level-skip);
//////            propagateThermal(x+1,y,level-skip);
//////            propagateThermal(x-1,y,level/factor);
//////            propagateThermal(x,y-1,level/factor);
//////            propagateThermal(x,y+1,level/factor);
//////            propagateThermal(x+1,y,level/factor);
////        }
////    }
////    public void propagateThermal(int x, int y, double level, double decre) {
////        double skip = 4;
////        double factor=0.7;
////        if (0<= x && x < getWidth() && 0<= y && y<getHeight() && level > getLevel(x,y)) {
////            this.setLevel(x, y, (int) level);
//////            propagateThermal(x-1,y-1,level-skip);
////////            propagateThermal(x-1,y+1,level-skip);
////////            propagateThermal(x+1,y-1,level-skip);
//////            propagateThermal(x+1,y+1,level-skip);
////
////            skip = (256-level)*factor;
////            propagateThermal(x-1,y,level-skip);
////            propagateThermal(x,y-1,level-skip);
////            propagateThermal(x,y+1,level-skip);
////            propagateThermal(x+1,y,level-skip);
////
//////            propagateThermal(x-1,y,level-skip);
//////            propagateThermal(x,y-1,level-skip);
//////            propagateThermal(x,y+1,level-skip);
//////            propagateThermal(x+1,y,level-skip);
////
//////            propagateThermal(x-1,y,level/factor);
//////            propagateThermal(x,y-1,level/factor);
//////            propagateThermal(x,y+1,level/factor);
//////            propagateThermal(x+1,y,level/factor);
////        }
////    }
//}
