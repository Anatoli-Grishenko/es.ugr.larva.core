/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package map2D;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import static crypto.Keygen.getHexaKey;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import swing.OleDashBoard;
import static world.Perceptor.NULLREAD;

/**
 *
 * @author lcv
 */
public class Palette {

    protected HashMap<Integer, Color> palette;
    protected ArrayList<WayPoint> wayPoints;
    protected String name = "Palette" + getHexaKey(4);
    protected boolean debug = false;
    protected double alpha;

    protected void debugPing(String message) {
        if (debug) {
            System.err.println(name + ": " + message);
        }
    }

    public Palette() {
        palette = new HashMap<>();
        wayPoints = new ArrayList();
    }

    public int size() {
        return palette.size();
    }

    public Palette addWayPoint(int perc, Color c) {
        wayPoints.add(new WayPoint(perc, c));
        return this;
    }

    public Palette fillWayPointsPerc(int nlevels) {
        int i = 0, iwp = 0;
        int r1, r2, g1, g2, b1, b2;
        double s, s1, s2, r, g, b;
        r1 = wayPoints.get(iwp).color.getRed();
        g1 = wayPoints.get(iwp).color.getGreen();
        b1 = wayPoints.get(iwp).color.getBlue();
        s1 = wayPoints.get(iwp).percentage;
        r2 = wayPoints.get(iwp + 1).color.getRed();
        g2 = wayPoints.get(iwp + 1).color.getGreen();
        b2 = wayPoints.get(iwp + 1).color.getBlue();
        s2 = wayPoints.get(iwp + 1).percentage;
        while (i < nlevels) {
            s = (i * 100.0) / nlevels;
            if (s1 <= s && s <= s2) {
                r = Math.round(r1 + (r2 - r1) * (s - s1) / (s2 - s1));
                g = Math.round(g1 + (g2 - g1) * (s - s1) / (s2 - s1));
                b = Math.round(b1 + (b2 - b1) * (s - s1) / (s2 - s1));
                palette.put(i, new Color((int) r, (int) g, (int) b));
                i++;
            } else {
                r1 = r2;
                g1 = g2;
                b1 = b2;
                s1 = s2;
                iwp++;
                r2 = wayPoints.get(iwp + 1).color.getRed();
                g2 = wayPoints.get(iwp + 1).color.getGreen();
                b2 = wayPoints.get(iwp + 1).color.getBlue();
                s2 = wayPoints.get(iwp + 1).percentage;
            }
        }
        return this;
    }

    public Palette fillWayPointsLevel(int nlevels) {
        return fillWayPointsLevel(nlevels, 1);
    }

    public Palette fillWayPointsLevel(int nlevels, double alpha01) {
        int i = 0, iwp = 0;
        int r1, r2, g1, g2, b1, b2;
        double s, s1, s2, r, g, b;
        r1 = wayPoints.get(iwp).color.getRed();
        g1 = wayPoints.get(iwp).color.getGreen();
        b1 = wayPoints.get(iwp).color.getBlue();
        s1 = wayPoints.get(iwp).percentage;
        r2 = wayPoints.get(iwp + 1).color.getRed();
        g2 = wayPoints.get(iwp + 1).color.getGreen();
        b2 = wayPoints.get(iwp + 1).color.getBlue();
        s2 = wayPoints.get(iwp + 1).percentage;
        while (i < nlevels) {
            s = i;
            if (s1 <= s && s <= s2) {
                r = Math.round(r1 + (r2 - r1) * (s - s1) / (s2 - s1));
                g = Math.round(g1 + (g2 - g1) * (s - s1) / (s2 - s1));
                b = Math.round(b1 + (b2 - b1) * (s - s1) / (s2 - s1));
                Color calpha;
                if (alpha01 < 0) {
                    calpha = new Color(
                            (int) (Math.min(255, Math.max(0, r * alpha01))),
                            (int) (Math.min(255, Math.max(0, g * alpha01))),
                            (int) (Math.min(255, Math.max(0, b * alpha01)))
                    );
                } else {
                    calpha = new Color(
                            (int) (Math.min(255, Math.max(0, r / alpha01))),
                            (int) (Math.min(255, Math.max(0, g / alpha01))),
                            (int) (Math.min(255, Math.max(0, b / alpha01)))
                    );
                }
                palette.put(i, calpha);
                i++;
            }
            else {
                r1 = r2;
                g1 = g2;
                b1 = b2;
                s1 = s2;
                iwp++;
                r2 = wayPoints.get(iwp + 1).color.getRed();
                g2 = wayPoints.get(iwp + 1).color.getGreen();
                b2 = wayPoints.get(iwp + 1).color.getBlue();
                s2 = wayPoints.get(iwp + 1).percentage;
            }
        }
        return this;
    }

    public Palette intoTerrain(int nlevels) {
        palette.clear();
        for (int i = 0; i < nlevels; i++) {
            palette.put(i, getTerrainColor(i, 0, nlevels));
        }
        return this;
    }

    public Palette intoBW(int nlevels) {
        palette.clear();
        for (int i = 0; i < nlevels; i++) {
            palette.put(i, getBWColor(nlevels - i + 1, 0, nlevels));
        }
        return this;
    }

    public Palette intoBWInv(int nlevels) {
        palette.clear();
        for (int i = 0; i < nlevels; i++) {
            palette.put(i, getBWColor(i, 0, nlevels));
        }
        return this;
    }

    public Color getColor(int level) {
        if (level != NULLREAD && 0 <= level && level < palette.size()) {
            return palette.get(level);
        } else {
            return OleDashBoard.cBad;
        }
    }

    public int getLevel(Color c) {
        for (int ml : palette.keySet()) {
            if (c.equals(palette.get(ml))) {
                return ml;
            }
        }
        return -1;
    }

    public Palette intoThermal(int nlevels) {
        palette.clear();
        for (int i = 0; i < nlevels; i++) {
            palette.put(i, getThermalColor(nlevels - i - 1, 0, nlevels));
        }
        return this;
    }

    protected Color getBWColor(int value, int minvalue, int maxvalue) { // Value [0,1], getcolor ANSICOLOR
        double r = 0, g = 0, b = 0;
        double scale = 1 - Math.min(1, Math.max((value - minvalue) * 1.0 / (maxvalue - minvalue), 0));
        r = g = b = scale;
        return new Color((int) (r * 255), (int) (g * 255), (int) (b * 255));

    }

    protected Color getThermalColor(int value, int minvalue, int maxvalue) { // Value [0,1], getcolor ANSICOLOR
        double r = 0, g = 0, b = 0;
        double scale = 1 - Math.min(1, Math.max((value - minvalue) * 1.0 / (maxvalue - minvalue), 0)); // 1 si es minima, 0 si es máxima
        scale = Math.pow(scale, 0.75);
        if (0.75 <= scale && scale <= 1) {
            b = (1 - scale) / 0.25;
        } else if (0.5 <= scale && scale <= 0.75) {
            g = (0.75 - scale) / 0.25;
            b = (scale - 0.5) / 0.25;
        } else if (0.25 <= scale && scale <= 0.5) {
            r = (0.5 - scale) / 0.25;
            g = (scale - 0.25) / 0.25;
        } else {//if (0.75 <= scale && scale <=1) {
            r = 1;
            g = (0.25 - scale) / 0.25;
            b = (0.25 - scale) / 0.25;
        }
        return new Color((int) (r * 255), (int) (g * 255), (int) (b * 255));
    }

    protected Color getTerrainColor(int value, int minvalue, int maxvalue) { // Value [0,1], getcolor ANSICOLOR
        double r = 0, g = 0, b = 0;
        double scale = Math.min(1, Math.max((value - minvalue) * 1.0 / (maxvalue - minvalue), 0)); // 1 si es minima, 0 si es máxima
        if (0.75 <= scale && scale <= 1) { // 255,255,255 - 115,77, 38
            r = 255 - (1 - scale) / 0.25 * (255 - 115);
            g = 255 - (1 - scale) / 0.25 * (255 - 77);
            b = 255 - (1 - scale) / 0.25 * (255 - 38);;
        } else if (0.25 <= scale && scale <= 0.75) {
            r = 115 - (0.75 - scale) / 0.5 * (115 - 51);
            g = 77 - (0.75 - scale) / 0.5 * (77 - 26);
            b = 38 - (0.75 - scale) / 0.5 * (38);;
        } else {
            r = 51 - (0.25 - scale) / 0.25 * (51);
            g = 26 + (scale) / 0.25 * (38 - 26);
            b = 0;
        }
        return new Color((int) r, (int) g, (int) b);
    }

    public Color inverse(int level) {
        return getColor(256 - level);
    }

    @Override
    public String toString() {
        JsonArray jsa = new JsonArray();
        for (int i = 0; i < palette.size(); i++) {
            Color c = palette.get(i);
            jsa.add(new JsonArray().add(c.getRed()).add(c.getGreen()).add(c.getBlue()));
        }
        return jsa.toString();
    }

    public Palette fromString(String spalette) {
        JsonArray jsa = Json.parse(spalette).asArray();
        palette.clear();
        for (int i = 0; i < jsa.size(); i++) {
            JsonArray jsacolor = jsa.get(i).asArray();
            palette.put(i, new Color(jsacolor.get(0).asInt(), jsacolor.get(1).asInt(), jsacolor.get(2).asInt()));
        }
        return this;
    }

    public void paintPalette(Graphics2D g, Rectangle r) {
        int cw = r.width - 4, ch = 2;
        Font f = g.getFont();
        g.setFont(new Font(f.getFamily(), f.getStyle(), 10));
        g.setColor(Color.WHITE);
        for (int i = 0; i <= size(); i++) {
            g.setColor(getColor(size() - i));
            g.fillRect(r.x, r.y + i * ch, r.width, ch);
            g.setColor(Color.WHITE);
            if (i % 20 == 0 || i == size()) {
                g.drawString(String.format("%03d", size() - i), r.x + r.width, r.y + i * ch + 10);
            }
        }
        g.setFont(f);
    }
}

class WayPoint {

    int percentage;
    Color color;

    public WayPoint(int p, Color c) {
        percentage = p;
        color = c;
    }
}
