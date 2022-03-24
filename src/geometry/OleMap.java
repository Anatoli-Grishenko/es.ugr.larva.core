/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.SwingConstants;
import map2D.Map2DColor;
import swing.OleApplication;
import swing.OleDrawPane;
import swing.OleSensor;
import swing.SwingTools;
import swing.TextFactory;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleMap extends OleSensor {

    HashMap<String, ArrayList<Point3D>> Trails;

    public OleMap(OleDrawPane parent, String name) {
        super(parent, name);
        Trails = new HashMap();
    }

    @Override
    public void validate() {
        super.validate();
    }

    @Override
    public OleSensor layoutSensor(Graphics2D g) {
        if (showFrame) {
            g.setColor(Color.GRAY);
            g.fillRect(mX, mY, mW, mH);
            g.setColor(Color.DARK_GRAY);
            g.fillRoundRect(mX + 3, mY + 3, mW - 6, mH - 6, 10, 10);
        }
//        g.setColor(this.getBackground());
//        g.fillRect(mX + 3, mY + 3, mW - 6, mH - 6);
//        g.setStroke(new BasicStroke(1));
        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {
        layoutSensor(g);

        if (map != null) {
            Rectangle r = SwingTools.doNarrow(this.getBounds(), 6);
            g.drawImage(map.getMap(), r.x, r.y, r.width, r.height, null);
            g.setColor(Color.GREEN);
            Point3D ptrail;
            Polygon p;
            int pw = 5;
            for (String name : Trails.keySet()) {
                p = new Polygon();
                ptrail = Trails.get(name).get(Trails.get(name).size() - 1);
                p.addPoint(ptrail.getXInt() - pw, ptrail.getYInt());
                p.addPoint(ptrail.getXInt(), ptrail.getYInt() - pw);
                p.addPoint(ptrail.getXInt() + pw, ptrail.getYInt());
                p.addPoint(ptrail.getXInt(), ptrail.getYInt() + pw);
                p.addPoint(ptrail.getXInt() - pw, ptrail.getYInt());
                g.setStroke(new BasicStroke(2));
                g.drawPolygon(p);
                g.setStroke(new BasicStroke(1));
                g.drawString(name, ptrail.getXInt(), ptrail.getYInt()-pw);
            }
        }
        return this;
    }

    public void addTrail(String name, Point3D p) {
        if (Trails.get(name) == null) {
            Trails.put(name, new ArrayList());
        }
        Rectangle r = SwingTools.doNarrow(this.getBounds(), 6);
        Trails.get(name).add(new Point3D(r.x + p.getX() / map.getWidth() * r.width,
                r.y + p.getY() / map.getHeight() * r.height));
    }

}
