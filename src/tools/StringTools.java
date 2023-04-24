/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.ArrayList;
import tools.plainTable.Align;
import tools.plainTable.Overflow;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class StringTools {

    public static String pasteBegin(String original, String begin) {
//        return original;
//            return begin + original.substring(begin.length(), original.length());
        if (begin.length() < original.length()) {
            return begin + original.substring(begin.length(), original.length());
        } else {
            return begin;
        }
    }

    public static String pasteEnd(String original, String end) {
//        return original;
//            return original.substring(0, original.length() - end.length()) + end;
        if (end.length() < original.length()) {
            return original.substring(0, original.length() - end.length()) + end;
        } else {
            return end;
        }
//    }
    }

    public static String cut(String s, Align a, int width) {
        String res, aux;
        if (s.length() <= width) {
            return s;
        } else {
            if (a == Align.LEFT) {
                return s.substring(0, width);
            } else if (a == Align.RIGHT) {
                return s.substring(s.length() - width);
            } else if (a == Align.CENTER) {
                return s.substring(s.length() / 2 - width / 2, s.length() / 2 + width / 2);
            } else {
                return s;
            }
        }
    }

    public static String fitRow(String s, Align a, int width) {
        String res, aux;
        if (s.length() == width) {
            return s;
        } else if (s.length() > width) {
            return cut(s, a, width);
        } else {
            switch (a) {
                case LEFT:
                    return fitRow(s + " ", a, width);
                case RIGHT:
                    return fitRow(" " + s, a, width);
                default:
                    return fitRow(" " + s + " ", a, width);
            }
        }
    }

    public static ArrayList<String> fitMultiRow(String s, Align a, int width, Overflow o) {
        ArrayList<String> res = new ArrayList();
        //double lines = s.length()*1.0/width;
        //int rows = (int)(Math.ceiling(lines));
        //res = new String [rows];
        if (s.length() <= width || o == Overflow.WRAP) {
            res.add(fitRow(s, a, width));
        } else {
            res.add(cut(s, Align.LEFT, width));
            res.addAll(fitMultiRow(s.substring(width), a, width, o));
        }
        return res;
    }

    public static String repeatString(String r, int times) {
        String res = "";
        for (int i = 0; i < times; i++) {
            res += r;
        }
        return res;
    }


}
