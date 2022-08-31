/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

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
}
