/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package map2D;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import static map2D.Map2DColor.PIX_MAX;
import swing.SwingTools;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Histogram extends Map2DGrayscale {

    int values[];
    ArrayList<Integer> stops;
    HashMap<Integer, Integer> left, right;

    public Histogram(int width, int height) {
        super(width, height);
        stops = new ArrayList();
    }

    public Histogram setLevels(int levels) {
        values = new int[levels];
        for (int i = 0; i < levels; i++) {
            values[i] = 0;
        }
        return this;
    }

//    public double getInterpolatedValue(double myLevel) {
////        int left = (int) (Math.floor(myLevel)), right = (int) (Math.ceil(myLevel)),
////                leftlevel = getValue(left), rightlevel = getValue(right);
////        if (left == right) {
////            return getValue(left);
////        } else {
////            return leftlevel + (rightlevel-leftlevel) * (right - myLevel)* 1.0 /(right-left)  ;
////        }
//
//        int left = getLeftBorder((int)myLevel), right = getRightBorder((int)myLevel),
//                leftlevel = getValue(left), rightlevel = getValue(right);
//        if (left == right) {
//            return getValue(left);
//        } else {
//            return leftlevel + (rightlevel-leftlevel) * (myLevel -left)* 1.0 /(right-left)  ;
//        }
//
////    int ilevel = (int) (myLevel), left = getLeftBorder(ilevel), right = getRightBorder(ilevel),
////                leftlevel = getValue(left), rightlevel = getValue(right);
////        return leftlevel + (rightlevel - myLevel) * (rightlevel - leftlevel) * 1.0 / (right - left);
//    }
    public int getValue(int level) {
        if (level < 0) {
            return getValue(0);
        } else if (level >= size()) {
            return getValue(size() - 1);
        } else {
            return values[level];
        }
    }

    public double getDValue(double level) {
        if (level < 0) {
            return getValue(0);
        } else if (level >= size()) {
            return getValue(size() - 1);
        } else {
            int leftstop = left.get((int)level),
                    rightstop = right.get((int)level),
                    left = stops.get(leftstop),
                    right = stops.get(rightstop),
                    leftvalue = values[left],
                    rightvalue = values[right];
            if (left == right) {
                return values[(int) left];
            } else {
                return leftvalue + (level - left) / (right - left) * (rightvalue - leftvalue);
            }
        }
    }

    public void adjustStops() {
        stops.clear();
        stops.add(0);
        for (int i = 0; i < size(); i++) {
            if (values[i] != stops.get(stops.size() - 1)) {
                stops.add(i);
            }
        }
        left = new HashMap();
        right = new HashMap();
        for (int i = 0; i < size(); i++) {
            left.put(i, getLeftStop(i));
            right.put(i, (int) Math.min(left.get(i), stops.size() - 1));
        }
    }

    public int getLeftStop(double level) {
        int i = 0;
        while (i < stops.size() && stops.get(i) <= level) {
            i++;
        }
        return (i - 1);
    }

    public Histogram setValue(int level, int count) {
        values[level] = count;
        return this;
    }

    public Histogram addValue(int level, int count) {
        return setValue(level, getValue(level) + count);
    }

//    public Histogram push(double ratio) {
//        int max = getMax(), posmax = getWhichMax(), min = (int) (getMin() * (1 + ratio));
//        double delta = (max - min) * 1.0 / size();
//        for (int i = 0; i < size(); i++) {
//            values[i] = (int) (Math.max(0, max - (Math.abs(max - i) * delta)));
//        }
//        return this;
//    }
    public Histogram push(double ratio) {
        int max = getMax();
        for (int i = 0; i < size(); i++) {
            int v1 = values[i], v2;
            values[i] = (int) (values[i] + (max - values[i])*ratio);
            v2 = values[i];
//            System.out.println(v1+"->"+v2);
        }
        return this;
    }

    public Histogram render() {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setLevel(x, y, 0);
            }
        }
        for (int x = 0; x < getWidth(); x++) {
            for (int y = getHeight() - 1; y > getHeight() - 10; y--) {
                setLevel(x, y, (PIX_MAX * x) / getWidth());
            }
        }
        int max = getMax();
        for (int x = 0; x < getWidth(); x++) {
//            for (int y = getHeight() - 10; y > getHeight() - 10 - ((values[x] * (getHeight() - 10)) / (2*getMax())); y--) {
            for (int y = getHeight() - 10; y > getHeight() - 10 - ((values[x] * (getHeight() - 10)) / max); y--) {
                if (x == size() / 2) {
                    setLevel(x, y, PIX_MAX * (y % 2));
                } else if (x == getWhichMax()) {
                    setLevel(x, y, 128 * ((y + 1) % 2));
                } else {
                    setLevel(x, y, PIX_MAX);
                }
            }
        }
        return this;
    }

    public Histogram norender() {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setLevel(x, y, 0);
            }
        }
        for (int x = 0; x < getWidth(); x++) {
            for (int y = getHeight() - 1; y > getHeight() - 10; y--) {
                setLevel(x, y, (PIX_MAX * x) / getWidth());
            }
        }
        int min = getMin();
        for (int x = 0; x < getWidth(); x++) {
//            for (int y = getHeight() - 10; y > getHeight() - 10 - ((values[x] * (getHeight() - 10)) / (2*getMax())); y--) {
            for (int y = getHeight() - 10; y > getHeight() - 10 - (values[x] - min); y--) {
                if (x == size() / 2) {
                    setLevel(x, y, PIX_MAX * (y % 2));
                } else if (x == getWhichMax()) {
                    setLevel(x, y, 128 * ((y + 1) % 2));
                } else {
                    setLevel(x, y, PIX_MAX);
                }
            }
        }
        return this;
    }

    public int size() {
        return values.length;
    }

    public Histogram normalize(int maxh) {
        int min, max;
        // Primero notmaliza
        min = getMin();
        max = getMax();
        for (int x = 0; x < values.length; x++) {
            setValue(x, (int) ((getValue(x) - min) * maxh * 1.0 / (max - min)));
        }
        return this;
    }

//    public Histogram pseudoNormalize() {
//        int min, max;
//        // Primero notmaliza
//        min = getMin();
//        max = getMax();
//        for (int x = 0; x < values.length; x++) {
//            setValue(x, (int) ((getValue(x) - min) * max * 1.0 / (max - min)));
//        }
//        return this;
//    }
// 
    public Histogram Smooth(int l) {
        int min = getMin(), max = getMax();
        for (int x = 0; x < size(); x++) {
            int sumx = 0, nx = 0;
            for (int il = -l / 2; il < l / 2; il++) {
                if (getValue(x + il) >= 0) {
                    sumx += getValue(x + il);
                    nx++;
                }

            }
            setValue(x, (int) (Math.round(sumx * 1.0 / nx)));
        }
        return this;
    }

//    public Histogram Flatten() {
//        for (int x = 0; x < size(); x++) {
//            setValue(x, (int) (getInterpolatedValue(x * 1.0)));
//        }
//        return this;
//    }
    public int getMax() {
        int max = values[0];
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    public int getWhichMax() {
        int max1 = 0, max2 = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > values[max1]) {
                max1 = i;
            } else if (max1 > 0 && max2 == 0 && values[i] < values[max1]) {
                max2 = i;
            }
        }
        return (max1 + max2) / 2;
    }

    public int getMin() {
        int min = values[0];
        for (int i = 0; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    public int getWichAvrg() {
        int min = 0, med = getSum() / 2;
        int i;
        for (i = 0; i < values.length && min < med; i++) {
            min += values[i];
        }
        return i;
    }

    public int getAvrg() {
        return getSum() / size();
    }

    public int getSum() {
        int min = 0;
        for (int i = 0; i < values.length; i++) {
            min += values[i];
        }
        return min;
    }

    @Override
    public String toString() {
        String res = "";
        res = "(" + size() + ")";
        for (int i = 0; i < size(); i++) {
            res += values[i] + "-";
        }
        return res;
    }

}
