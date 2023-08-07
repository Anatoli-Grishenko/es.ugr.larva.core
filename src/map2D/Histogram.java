/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package map2D;

import java.awt.image.BufferedImage;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Histogram extends Map2DGrayscale {

    int values[];

    public Histogram(int width, int height) {
        super(width, height);
    }

    public Histogram setLevels(int levels) {
        values = new int[levels];
        for (int i = 0; i < levels; i++) {
            values[i] = 0;
        }
        return this;
    }

    public int getValue(int level) {
        if (level < 0 || level >= size()) {
            return 0;
        } else {
            return values[level];
        }
    }

    public Histogram setValue(int level, int count) {
        values[level] = count;
        return this;
    }

    public Histogram addValue(int level, int count) {
        values[level] += count;
        return this;
    }

    public Histogram render() {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setLevel(x, y, 0);
            }
        }
        for (int x = 0; x < values.length; x++) {
            for (int y = getHeight() - 1; y > getHeight() - 10; y--) {
                setLevel(x, y, (getMaxLevel() * x) / getWidth());
            }
        }
        for (int x = 0; x < values.length; x++) {
            for (int y = getHeight() - 10; y > getHeight() - 10 - ((values[x] * (getHeight() - 10)) / getMaxLevel()); y--) {
                setLevel(x, y, 255);
            }
        }
        return this;
    }

    public int size() {
        return values.length;
    }

    public Histogram normalize(int maxh) {
        int l = size()/3, min = getMin(), max = getMax();
        // Primero notmaliza
        min = getMin();
        max = getMax();
        for (int x = 0; x < values.length; x++) {
            setValue(x, (int) ((getValue(x) - min) *maxh*1.0 / (max - min)));
        }
        return this;
    }

    public Histogram pseudoNormalize() {
        int l = size()/3, min = getMin(), max = getMax();
        // Primero notmaliza
        min = getMin();
        max = getMax();
        for (int x = 0; x < values.length; x++) {
            setValue(x, (int) ((getValue(x) - min) *max*2.0 / (max - min)));
        }
        return this;
    }

    public Histogram Smooth() {
        int l = size()/3, min = getMin(), max = getMax();
        for (int x = 0; x < values.length; x++) {
            int sumx = 0, nx =0;
            for (int il = -l / 2; il < l / 2; il++) {
//                if (0 <= x+il && x+il < size()) {
//                sumx += getValue(x + il);
//                nx++;
//                }

                sumx += getValue(x + il);


//                sumx += getValue(Math.min(size() - 1, Math.max(x + il, 0)));
            }
            setValue(x, sumx/l);
        }
        return this;
    }

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
        int max = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > values[max]) {
                max = i;
            }
        }
        return max;
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

    public int getAvrg() {
        int min = 0;
        for (int i = 0; i < values.length; i++) {
                min += values[i];
        }
        return min/size();
    }

    @Override
    public String toString() {
        String res = "";
        for (int i = 0; i < size(); i++) {
            res += values[i] + "-";
        }
        return res;
    }

}
