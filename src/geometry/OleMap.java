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
import swing.OleDashBoard;
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

    protected HashMap<String, ArrayList<Point3D>> Trails;
    protected Polygon hudView[][];
    protected int narrow = 37, margin = 22;
    protected OleButton obMap, obHud;
    protected Polygon p;
    protected int cell, nLevels, nTiles;
    protected Point3D pCenterTopFixed, pVariableDown, pCenterFixed, pVariableTop, pDistance;
    protected TextFactory tf;
    protected double stepRadius, stepAngle;
    protected AngleTransporter at;

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
        at = parentPane.getAngleT();
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
            Point3D p0, p1, p2, p3, p4;
            double radius1, radius2, alpha1, alpha2;

            cell = screenPort.width / 8;
            center = new Point3D(screenPort.x + screenPort.width / 2, screenPort.y + screenPort.height - 2 * cell);
            pCenterTopFixed = at.alphaPoint(90, 5.7 * cell, center);
            pCenterFixed = at.alphaPoint(90, 5 * cell, center);
            if (getImage1() != null) {
                pDistance = at.alphaPoint(shiftAngularHud(), Math.min(nLevels, this.getAllReadings()[0][2]) * lengthVisual / nLevels, center);
            } else {
                pDistance = at.alphaPoint(shiftAngularHud(), lengthVisual, center);
            }
            this.lengthVisual = center.getY() - pCenterFixed.getY();
            pVariableTop = at.alphaPoint(shiftAngularHud(), 5.4 * cell, center);
            pVariableDown = at.alphaPoint(shiftAngularHud(), 5.1 * cell, center);
            g.setColor(Color.WHITE);
            this.oDrawLine(g, pCenterTopFixed, center);
            minVisual = 45;
            maxVisual = 135;
            minValue = 0;
            maxValue = 360;

            p = new Polygon();
            p.addPoint(center.getXInt(), center.getYInt());
            p.addPoint(center.getXInt() + cell / 5, center.getYInt() + cell / 5);
            p.addPoint(center.getXInt() - cell / 5, center.getYInt() + cell / 5);
            p.addPoint(center.getXInt(), center.getYInt());
            g.draw(p);
            for (double alpha = 135; alpha >= 45; alpha -= 5) {
                p1 = at.alphaPoint(alpha, 5 * cell, center);
                p2 = at.alphaPoint(alpha, 4.9 * cell, center);
                this.oDrawLine(g, p1, p2);
                if ((int) alpha % 15 == 0) {
                    tf = new TextFactory(g);
                    tf.setPoint(p2).setValue((360 + (int) (getCurrentValue() + alpha) - 90) % 360).setAngle(90 - (int) alpha).
                            setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
                    tf.draw();
                }
            }
            if (getImage1() != null) {
                nLevels = getImage1().getHeight();
                stepRadius = lengthVisual / (nLevels + 1);
                hudView = new Polygon[nLevels][];
                radius1 = stepRadius;
                g.setColor(Color.DARK_GRAY);
                nTiles = 1;
                for (int level = 0; level < nLevels; level++) {
                    radius2 = (level + 1) * stepRadius;

                    nTiles = 4 * (level) + 1;
                    alpha1 = 0; //90 - nTiles * stepAngle / 2;
                    alpha2 = 180; //90 + nTiles * stepAngle / 2;
                    stepAngle = 180.0 / nTiles;

//                nTiles=4*(nLevels-1)+1;
//                stepAngle = 180.0 / nTiles;
//                alpha1 = 90 - nTiles/2 * stepAngle / 2;
//                alpha2 = 90 + nTiles/2 * stepAngle / 2;
                    hudView[level] = new Polygon[nTiles];
//                    System.out.println("Level " + level + " " + nTiles + " tiles, radius: " + radius2 + " every  :" + stepAngle + "º");
                    for (int tile = 0; tile < nTiles; tile++) {
                        Polygon p = new Polygon();
                        if (level != 0) {
                            p0 = at.alphaPoint(alpha2 - tile * stepAngle, radius2, center);
                            p1 = at.alphaPoint(alpha2 - (tile + 1) * stepAngle, radius2, center);
                            p2 = at.alphaPoint(alpha2 - (tile + 1) * stepAngle, radius1, center);
                            p3 = at.alphaPoint(alpha2 - tile * stepAngle, radius1, center);
                            p.addPoint(p0.getXInt(), p0.getYInt());
                            p.addPoint(p1.getXInt(), p1.getYInt());
                            p.addPoint(p2.getXInt(), p2.getYInt());
                            p.addPoint(p3.getXInt(), p3.getYInt());
                            p.addPoint(p0.getXInt(), p0.getYInt());
                        } else {
                            p0 = at.alphaPoint(alpha2 - (tile+1) *5* stepAngle/5, radius2, center);
                            p.addPoint(p0.getXInt(), p0.getYInt());
                            p1 = at.alphaPoint(alpha2 - (tile + 1) * 4*stepAngle/5, radius2, center);
                            p.addPoint(p1.getXInt(), p1.getYInt());
                            p1 = at.alphaPoint(alpha2 - (tile + 1) * 3*stepAngle/5, radius2, center);
                            p.addPoint(p1.getXInt(), p1.getYInt());
                            p1 = at.alphaPoint(alpha2 - (tile + 1) * 2* stepAngle/5, radius2, center);
                            p.addPoint(p1.getXInt(), p1.getYInt());
                            p1 = at.alphaPoint(alpha2 - (tile + 1) * 1*stepAngle/5, radius2, center);
                            p.addPoint(p1.getXInt(), p1.getYInt());
                            p1 = at.alphaPoint(alpha2 - (tile + 1) * 0*stepAngle/5, radius2, center);
                            p.addPoint(p1.getXInt(), p1.getYInt());
                            p.addPoint(p0.getXInt(), p0.getYInt());
                        }
//                        g.draw(p);
//                    System.out.println(p0 + "/" + p1 + "/" + p2 + "/" + p3 + "/");
                        hudView[level][tile] = p;
                        g.setColor(Color.GRAY);
                        g.draw(hudView[level][tile]);
                    }
                    radius1 = radius2;
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

    protected int shiftAngularHud() {
        return ((int) ((270 + this.getAllReadings()[0][1]) * 90 / this.getAllReadings()[0][0]));
    }

    protected int valueAngularHud() {
        return ((int) (shiftAngularHud() - 90 + this.getAllReadings()[0][0]));
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {
        layoutSensor(g);

        if (isMap) {
            if (map != null) {
                g.drawImage(map.getMap(), viewPort.x, viewPort.y, viewPort.width, viewPort.height, null);
                g.setColor(Color.MAGENTA);
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
            String sCompass, sGoal, sDistance;
            double radius1 = 0, radius2 = 0, alpha1, alpha2;
            Point3D p0, p1, p2, p3;

            g.setClip(screenPort);
            Color cTile;
            int iGround;
            if (hudView != null && getImage1() != null) {
                for (int level = 0; level < nLevels; level++) {
                    nTiles = (level) * 4 + 1;
                    for (int tile = 0; tile < nTiles; tile++) {
                        cTile = getImage1().getColor(tile, level);
                        iGround = getImage1().getRawLevel(tile, level);

                        g.setColor(getImage1().getColor(tile, level));
                        g.fill(hudView[level][tile]);
                        if (iGround < 0) {
                            g.setColor(Map2DColor.BADVALUE);
                        } else if (iGround > ((OleDashBoard) this.parentPane).decoder.getAltitude()) {
                            g.setColor(Color.RED);
                        } else {
                            g.setColor(getImage1().getColor(tile, level));
                        }
                        g.setStroke(new BasicStroke(1));
                        g.draw(hudView[level][tile]);
                        g.setStroke(new BasicStroke(1));
                    }
                }
            }
            g.setColor(Color.WHITE);
            pDistance = at.alphaPoint(shiftAngularHud(), Math.min(nLevels, this.getAllReadings()[0][2]) * lengthVisual / nLevels, center);
            this.oDrawArc(g, center, 15.0 * lengthVisual / nLevels, 0, 180);
            this.oDrawArc(g, center, 10.0 * lengthVisual / nLevels, 0, 180);
            this.oDrawArc(g, center, 5.0 * lengthVisual / nLevels, 0, 180);
            this.oDrawArc(g, center, 2.0 * lengthVisual / nLevels, 0, 180);
            tf = new TextFactory(g).setX((int) (center.getX() + 2.0 * lengthVisual / nLevels)).setY(center.getYInt()).setFontSize(10)
                    .setsText("+1").setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
            tf.draw();
            tf = new TextFactory(g).setX((int) (center.getX() - 2.0 * lengthVisual / nLevels)).setY(center.getYInt()).setFontSize(10)
                    .setsText("+1").setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
            tf.draw();

            tf = new TextFactory(g).setX((int) (center.getX() + 5.0 * lengthVisual / nLevels)).setY(center.getYInt()).setFontSize(10)
                    .setsText("+5").setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
            tf.draw();
            tf = new TextFactory(g).setX((int) (center.getX() - 5.0 * lengthVisual / nLevels)).setY(center.getYInt()).setFontSize(10)
                    .setsText("+5").setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
            tf.draw();

            tf = new TextFactory(g).setX((int) (center.getX() + 10.0 * lengthVisual / nLevels)).setY(center.getYInt()).setFontSize(10)
                    .setsText("+10").setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
            tf.draw();
            tf = new TextFactory(g).setX((int) (center.getX() - 10.0 * lengthVisual / nLevels)).setY(center.getYInt()).setFontSize(10)
                    .setsText("+10").setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
            tf.draw();

            g.setColor(Color.MAGENTA);
            g.setStroke(new BasicStroke(3));
            this.oDrawLine(g, pDistance, center);
            g.setStroke(new BasicStroke(1));

            g.setColor(Color.CYAN);
            g.setStroke(new BasicStroke(3, 0, 0, 10, new float[]{10}, 0));
            this.oDrawLine(g, pVariableDown, center);
            g.setStroke(new BasicStroke(1));

            g.setColor(Color.WHITE);
            if (getCurrentValue() == Perceptor.NULLREAD) {
                sRead = "---";
            } else {
                sRead = String.format("[ %03dº ]", (int) getCurrentValue());
            }
            tf = new TextFactory(g);
            tf.setPoint(pCenterTopFixed).setsText(sRead).
                    setHalign(SwingConstants.CENTER).setValign(SwingConstants.BOTTOM).setFontSize(20).setTextStyle(Font.BOLD).validate();
            g.setColor(Color.WHITE);
            tf.draw();

            tf = new TextFactory(g);
            tf.setX(screenPort.x).setY(screenPort.y).setsText("Compass: " + sRead).
                    setHalign(SwingConstants.LEFT).setValign(SwingConstants.TOP).setFontSize(15).setTextStyle(Font.BOLD).validate();
            tf.draw();

            g.setColor(Color.MAGENTA);
            if (getCurrentValue() == Perceptor.NULLREAD) {
                sRead = "---";
            } else {
                sRead = String.format(" %03dm ", (int) (this.getAllReadings()[0][2]));
            }
            tf = new TextFactory(g).setPoint(pDistance).setFontSize(14).setsText(sRead).
                    setHalign(SwingConstants.RIGHT).setValign(SwingConstants.BOTTOM).setTextStyle(Font.BOLD).
                    setAngle(90 - shiftAngularHud()).validate();
            tf.draw();
            tf = new TextFactory(g);
            tf.setX(screenPort.x).setY(screenPort.y + 40).setsText("Distance: " + sRead).
                    setHalign(SwingConstants.LEFT).setValign(SwingConstants.TOP).setFontSize(15).setTextStyle(Font.BOLD).validate();
            tf.draw();

            g.setColor(Color.CYAN);
            if (getCurrentValue() == Perceptor.NULLREAD) {
                sRead = "---";
            } else {
                sRead = String.format(" %03dº ", valueAngularHud());
            }
            tf = new TextFactory(g).setPoint(pVariableTop).setFontSize(14).setsText(sRead).
                    setHalign(SwingConstants.LEFT).setValign(SwingConstants.BOTTOM).setTextStyle(Font.BOLD).
                    setAngle(90 - shiftAngularHud()).validate();
            tf.draw();
            tf = new TextFactory(g);
            tf.setX(screenPort.x).setY(screenPort.y + 20).setsText("Angle: " + sRead).
                    setHalign(SwingConstants.LEFT).setValign(SwingConstants.TOP).setFontSize(15).setTextStyle(Font.BOLD).validate();
            tf.draw();

            g.setClip(null);
//            this.oDrawCounter(g, sRead, pCenterTopFixed, cell, SwingConstants.CENTER, SwingConstants.BOTTOM);
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
