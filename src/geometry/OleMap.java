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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import map2D.Map2DColor;
import swing.OleApplication;
import swing.OleButton;
import swing.OleDrawPane;
import swing.OleSensor;
import swing.SwingTools;
import swing.TextFactory;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleMap extends OleSensor implements ActionListener {

    HashMap<String, ArrayList<Point3D>> Trails;
    int narrow = 37, margin = 22;
    OleButton obMap, obHud;
    boolean isMap;
    Polygon p;
    int cell;
    Point3D pCenterTop, pCenter, pGoal;

    public OleMap(OleDrawPane parent, String name) {
        super(parent, name);
        this.setLayout(null);
        Trails = new HashMap();
        isMap = false;
        obMap = new OleButton(this, "VMap", "VMap");
        obMap.setText("VMap");
        obMap.setBackground(Color.DARK_GRAY);
        obMap.setForeground(Color.WHITE);
        parentPane.add(obMap);
        obHud = new OleButton(this, "VHud", "VHud");
        obHud.setBackground(Color.DARK_GRAY);
        obHud.setForeground(Color.WHITE);
        parentPane.add(obHud);
        setnRows(1);
        setnColumns(3);
    }

    @Override
    public void validate() {
        super.validate();

        viewPort = SwingTools.doNarrow(this.getBounds(), narrow);
        screenPort = SwingTools.doNarrow(this.getBounds(), margin);
        viewPort.y += 16;
        screenPort.y += 16;
        shiftx = viewPort.x - screenPort.x;
        shifty = viewPort.y - screenPort.y;
        obMap.setBounds(screenPort.x, mY + 10, 75, 20);
        obHud.setBounds(screenPort.x + 80, mY + 10, 75, 20);
    }

    @Override
    public OleSensor layoutSensor(Graphics2D g) {
        obMap.setEnabled(!isMap);
        obHud.setEnabled(isMap);
        if (showFrame) {
            g.setColor(Color.GRAY);
            g.fillRect(mX, mY, mW, mH);
            g.setColor(Color.DARK_GRAY);
            g.fillRoundRect(mX + 3, mY + 3, mW - 6, mH - 6, 10, 10);
        }
        g.setColor(Color.BLACK);
        g.fill(screenPort);
        if (isMap) {
            if (map != null) {
                scale = viewPort.getWidth() / map.getMap().getWidth();
                setMinValue(0);
                setMaxValue(map.getWidth());
                setMinVisual(viewPort.x);
                setMaxVisual(viewPort.x + viewPort.width);
                lengthVisual = getMaxVisual() - getMinVisual();
                stepVisual = lengthVisual / nMarks;
                lengthValue = (maxValue - minValue);
                stepValue = lengthValue / nMarks;
                setnDivisions(10);
                drawLineRuler(g, screenPort, 10);
            }
        } else {
            g.draw(screenPort);
            g.setClip(screenPort);
            Point3D p1, p2;
            TextFactory tf;
            cell = screenPort.width / 8;
            center = new Point3D(screenPort.x + screenPort.width / 2, screenPort.y + screenPort.height - 2 * cell);
            pCenterTop = parentPane.getAngleT().alphaPoint(90, 5.5 * cell, center);
            pCenter = parentPane.getAngleT().alphaPoint(90, 5 * cell, center);
            pGoal = parentPane.getAngleT().alphaPoint((360 + (int) (this.getAllReadings()[0][1]) + 90) % 360, 5 * cell, center);
            g.setColor(Color.CYAN);
            g.setStroke(new BasicStroke(3));
            this.oDrawLine(g, pGoal, center);
            g.setStroke(new BasicStroke(1));
            tf=new TextFactory(g).setPoint(pGoal).setFontSize(20).setValue((int) (this.getAllReadings()[0][1]),3).//setAngle(90+(int) (this.getAllReadings()[0][1]-getCurrentValue()+45)).
                    setHalign(SwingConstants.CENTER).setValign(SwingConstants.BOTTOM);
            tf.draw();
            g.setColor(Color.WHITE);
            this.oDrawLine(g, pCenterTop, center);
//            p = new Polygon();
//            p.addPoint(pCenterTop.getXInt(), pCenterTop.getYInt() + cell / 2);
//            p.addPoint(pCenterTop.getXInt() + cell / 4, pCenterTop.getYInt() - cell / 6);
//            p.addPoint(pCenterTop.getXInt() - cell / 4, pCenterTop.getYInt() - cell / 6);
//            p.addPoint(pCenterTop.getXInt(), pCenterTop.getYInt() + cell / 2);
//            g.draw(p);
            minVisual = 45;
            maxVisual = 135;
            minValue = 0;
            maxValue = 360;
            this.oDrawArc(g, center, 5 * cell, 0, 180);
            this.oDrawArc(g, center, 4 * cell, 0, 180);
            this.oDrawArc(g, center, 3 * cell, 0, 180);
            this.oDrawArc(g, center, 2 * cell, 0, 180);
            p = new Polygon();
            p.addPoint(center.getXInt(), center.getYInt());
            p.addPoint(center.getXInt() + cell / 2, center.getYInt() + cell);
            p.addPoint(center.getXInt() - cell / 2, center.getYInt() + cell);
            p.addPoint(center.getXInt(), center.getYInt());
            g.draw(p);
            for (double alpha = 135; alpha >= 45; alpha -= 5) {
                p1 = parentPane.getAngleT().alphaPoint(alpha, 5 * cell, center);
                p2 = parentPane.getAngleT().alphaPoint(alpha, 4.9 * cell, center);
                this.oDrawLine(g, p1, p2);                
                if ((int)alpha % 15 == 0) {
                    tf = new TextFactory(g);
                    tf.setPoint(p2).setValue((360 + (int) (getCurrentValue() + alpha) - 90) % 360).setAngle(90 - (int) alpha).
                            setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
                    tf.draw();
                }
            }
            g.setClip(null);
//            setMinValue(0);
//            setMaxValue(map.getWidth());
//            setMinVisual(viewPort.x);
//            setMaxVisual(viewPort.x + viewPort.width);
//            lengthVisual = getMaxVisual() - getMinVisual();
//            stepVisual = lengthVisual / nMarks;
//            lengthValue = (maxValue - minValue);
//            stepValue = lengthValue / nMarks;

        }
//        g.setColor(this.getBackground());
//        g.fillRect(mX + 3, mY + 3, mW - 6, mH - 6);
//        g.setStroke(new BasicStroke(1));
        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {
        layoutSensor(g);

        if (isMap) {
            if (map != null) {
                g.drawImage(map.getMap(), viewPort.x, viewPort.y, viewPort.width, viewPort.height, null);
                g.setColor(Color.GREEN);
                Point3D ptrail;
                int diamond = 5;
                double xVP, yVP;
                for (String name : Trails.keySet()) {
                    ptrail = Trails.get(name).get(Trails.get(name).size() - 1);
                    xVP = viewX(ptrail.getX());
                    yVP = viewY(ptrail.getY());
                    p = new Polygon();
                    p.addPoint((int) (xVP) - diamond, (int) (yVP));
                    p.addPoint((int) (xVP), (int) (yVP) - diamond);
                    p.addPoint((int) (xVP) + diamond, (int) (yVP));
                    p.addPoint((int) (xVP), (int) (yVP) + diamond);
                    p.addPoint((int) (xVP) - diamond, (int) (yVP));
                    g.setStroke(new BasicStroke(2));
                    g.drawPolygon(p);
                    g.setStroke(new BasicStroke(1));
                    g.drawString(name, (int) viewX(ptrail.getX()), (int) viewY(ptrail.getY()) - diamond);
                }
            }
        } else {
            if (getCurrentValue() == Perceptor.NULLREAD) {
                sRead = "---";
            } else {
                sRead = String.format("%03d", (int) getCurrentValue());
            }
            this.oDrawCounter(g, sRead, pCenterTop, cell, SwingConstants.CENTER, SwingConstants.BOTTOM);
        }
        return this;
    }

    public void addTrail(String name, Point3D p) {
        if (Trails.get(name) == null) {
            Trails.put(name, new ArrayList());
        }
        Rectangle r = SwingTools.doNarrow(this.getBounds(), 6);
        Trails.get(name).add(new Point3D(p.getX(),
                p.getY()));
    }

    public double viewX(double x) {
        return viewPort.x + (x + 0.4) * scale;
    }

    public double viewY(double y) {
        return viewPort.y + (y + 0.4) * scale;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        isMap = !isMap;
    }

}
