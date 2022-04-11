/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import static geometry.SimpleVector3D.nextX;
import static geometry.SimpleVector3D.nextY;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import map2D.Map2DColor;
import swing.OleApplication;
import swing.OleButton;
import swing.OleDashBoard;
import swing.OleDrawPane;
import swing.OleSensor;
import swing.SwingTools;
import swing.TextFactory;
import tools.emojis;
import world.Perceptor;
import world.SensorDecoder;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleMap extends OleSensor implements ActionListener {

    protected HashMap<String, ArrayList<SimpleVector3D>> Trails;
    protected Polygon hudView[][];
    protected int narrow = 37, margin = 22;
    protected OleButton obMap, obHud;
    protected Polygon p;
    protected int cell, nLevels, nTiles, trailSize = 200;
    protected Point3D pCenterTopFixed, pVariableDown, pCenterFixed, pVariableTop, pDistance, pHead;
    protected TextFactory tf;
    protected double stepRadius, stepAngle;
    protected AngleTransporter at;
    protected SensorDecoder externalDecoder;

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
        externalDecoder = ((OleDashBoard) this.parentPane).decoder;
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
            g.setColor(OleDashBoard.cDeck);
            g.fillRect(mX, mY, mW, mH);
            g.setColor(OleDashBoard.cFrame);
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
            pHead = at.alphaPoint(90, 10, center);
            pCenterFixed = at.alphaPoint(90, 5 * cell, center);
            if (externalDecoder.getPolarVisual() != null) {
                pDistance = at.alphaPoint(shiftAngularHud(), Math.min(nLevels, externalDecoder.getDistance()) * lengthVisual / nLevels, center);
            } else {
                pDistance = at.alphaPoint(shiftAngularHud(), lengthVisual, center);
            }
            this.lengthVisual = center.getY() - pCenterFixed.getY();
            pVariableTop = at.alphaPoint(shiftAngularHud(), 5.4 * cell, center);
            pVariableDown = at.alphaPoint(shiftAngularHud(), 5.1 * cell, center);
            g.setColor(OleDashBoard.cCompass);
            this.oDrawLine(g, pCenterTopFixed, center);
            minVisual = 45;
            maxVisual = 135;
            minValue = 0;
            maxValue = 360;

//            p = new Polygon();
//            p.addPoint(center.getXInt(), center.getYInt());
//            p.addPoint(center.getXInt() + cell / 5, center.getYInt() + cell / 5);
//            p.addPoint(center.getXInt() - cell / 5, center.getYInt() + cell / 5);
//            p.addPoint(center.getXInt(), center.getYInt());
//            g.draw(p);
            for (double alpha = 135; alpha >= 45; alpha -= 5) {
                p1 = at.alphaPoint(alpha, 5 * cell, center);
                p2 = at.alphaPoint(alpha, 4.9 * cell, center);
                this.oDrawLine(g, p1, p2);
                if ((int) alpha % 15 == 0) {
                    tf = new TextFactory(g);
                    tf.setPoint(p2).setValue((360 + (int) (externalDecoder.getCompass() + alpha) - 90) % 360).setAngle(90 - (int) alpha).
                            setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
                    tf.draw();
                }
            }
            if (externalDecoder.getPolarVisual() != null) {
                nLevels = externalDecoder.getPolarVisual()[0].length;
                stepRadius = lengthVisual / (nLevels + 1);
                hudView = new Polygon[nLevels][];
                radius1 = stepRadius;
                g.setColor(OleDashBoard.cDeck);
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
                            p0 = at.alphaPoint(alpha2 - (tile + 1) * 5 * stepAngle / 5, radius2, center);
                            p.addPoint(p0.getXInt(), p0.getYInt());
                            p1 = at.alphaPoint(alpha2 - (tile + 1) * 4 * stepAngle / 5, radius2, center);
                            p.addPoint(p1.getXInt(), p1.getYInt());
                            p1 = at.alphaPoint(alpha2 - (tile + 1) * 3 * stepAngle / 5, radius2, center);
                            p.addPoint(p1.getXInt(), p1.getYInt());
                            p1 = at.alphaPoint(alpha2 - (tile + 1) * 2 * stepAngle / 5, radius2, center);
                            p.addPoint(p1.getXInt(), p1.getYInt());
                            p1 = at.alphaPoint(alpha2 - (tile + 1) * 1 * stepAngle / 5, radius2, center);
                            p.addPoint(p1.getXInt(), p1.getYInt());
                            p1 = at.alphaPoint(alpha2 - (tile + 1) * 0 * stepAngle / 5, radius2, center);
                            p.addPoint(p1.getXInt(), p1.getYInt());
                            p.addPoint(p0.getXInt(), p0.getYInt());
                        }
//                        g.draw(p);
//                    System.out.println(p0 + "/" + p1 + "/" + p2 + "/" + p3 + "/");
                        hudView[level][tile] = p;
                        g.setColor(OleDashBoard.cFrame);
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
        return (int) (valueAngularHud() - externalDecoder.getCompass() + 360 + 90) % 360;
//        return (int) (valueAngularHud() - this.getAllReadings()[0][0] + 90 + 360) % 360;
    }

    protected int valueAngularHud() {
        return (int) (externalDecoder.getAbsoluteAngular()) % 360;
//        return ((int) (270 + this.getAllReadings()[0][1]) % 360);
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {
        layoutSensor(g);
        String label;
        if (isMap) {
            if (map != null) {
                RescaleOp darken = new RescaleOp(0.5f, 0, null);
                g.drawImage(darken.filter(map.getMap(), null), viewPort.x, viewPort.y, viewPort.width, viewPort.height, null);
                SimpleVector3D ptrail, prevTrail, ptext, ppoint;
                double xVP, yVP;
                Color c;
                for (String name : Trails.keySet()) {
                    for (int i = 2; i < Trails.get(name).size(); i++) {
                        ptrail = Trails.get(name).get(i);
                        c = map.getColor(ptrail);
//                        c = new Color(c.getRed(),255,c.getBlue());
//                        g.setColor(new Color(0, (Trails.get(name).size() - i) *c.getRed()/ Trails.get(name).size(), 0));
                        g.setColor(SwingTools.doDarker(OleDashBoard.cTrack));
                        g.fill(this.TraceRegularPolygon(ptrail, 4, 3));
//                        g.fillArc(viewX(ptrail.getSource().getX())-3, viewY(ptrail.getSource().getY())-3, 6, 6, 0, 360);
                    }
                    g.setColor(OleDashBoard.cTrack);
                    ptrail = externalDecoder.getGPSVector();
                    prevTrail = externalDecoder.getPreviousGPSVector();
//                    ptrail = Trails.get(name).get(0);
//                    if (Trails.get(name).size() > 1) {
//                        prevTrail = Trails.get(name).get(1);
//                    } else {
//                        prevTrail = Trails.get(name).get(0);
//                    }

                    p = this.TraceRegularPolygon(ptrail, 4, 5);
//                    p = this.TraceRegularStar(ptrail, 6, 30, 15);
//                    p = this.TraceRomboid(ptrail, 5);
                    g.setStroke(new BasicStroke(1));
                    g.drawPolygon(p);
                    p = this.TraceCourse(ptrail, 20);
                    g.drawPolygon(p);
                    g.setStroke(new BasicStroke(2));
                    g.setStroke(new BasicStroke(1));
                    this.traceLabel(g, ptrail, prevTrail, 25, name, viewPort);
                }
                for (int i = 0; i < jsaGoals.size(); i++) {
                    paintGoalMap(g, jsaGoals.get(i).asObject());
                }

            }
        } else {
            String sCompass, sGoal, sDistance;
            double radius1 = 0, radius2 = 0, alpha1, alpha2;
            Point3D p0, p1, p2, p3;

            g.setClip(screenPort);
            Color cTile, cBackground, cStroke;
            int iGround, lTile;
            if (hudView != null && externalDecoder.getPolarVisual() != null) {
                for (int level = 0; level < nLevels; level++) {
                    nTiles = (level) * 4 + 1;
                    for (int tile = 0; tile < nTiles; tile++) {
                        lTile = externalDecoder.getPolarVisual()[tile][level];
                        if (lTile >= 0) {
                            cTile = new Color(lTile, lTile, lTile);
                        } else {
                            cTile = OleDashBoard.cBad;
                        }
                        iGround = lTile;

                        if (iGround < 0) {
                            cStroke = OleDashBoard.cBad;
                            cBackground = OleDashBoard.cBad;
                        } else if (iGround > externalDecoder.getMaxlevel()) {
                            cStroke = OleDashBoard.cBad;
                            cBackground = OleDashBoard.cBad;
                        } else if (iGround > externalDecoder.getAltitude()) {
                            cStroke = OleDashBoard.cBad;
                            cBackground = cTile;
                        } else {
                            cStroke = cTile;
                            cBackground = cTile;
                        }
                        g.setColor(cBackground);
                        g.fill(hudView[level][tile]);
                        g.setColor(cStroke);
                        g.setStroke(new BasicStroke(1));
                        g.draw(hudView[level][tile]);
                        g.setStroke(new BasicStroke(1));
                    }
                }
            }
            g.setColor(OleDashBoard.cDial);
            pDistance = at.alphaPoint(shiftAngularHud(), Math.min(nLevels, externalDecoder.getDistance()) * lengthVisual / nLevels, center);
            this.oDrawArc(g, center, 15.0 * lengthVisual / nLevels, 0, 180);
            this.oDrawArc(g, center, 10.0 * lengthVisual / nLevels, 0, 180);
            this.oDrawArc(g, center, 5.0 * lengthVisual / nLevels, 0, 180);
            this.oDrawArc(g, center, 2.0 * lengthVisual / nLevels, 0, 180);
            this.oDrawArc(g, center, 1.0 * lengthVisual / nLevels, 0, 180);
            p = new Polygon();
            p.addPoint(pHead.getXInt(), pHead.getYInt());
            p.addPoint(center.getXInt() - 10, center.getYInt());
            p.addPoint(center.getXInt() + 10, center.getYInt());
            p.addPoint(pHead.getXInt(), pHead.getYInt());
            g.draw(p);
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

            g.setColor(OleDashBoard.cDistance);
            if (externalDecoder.getDistance() != Perceptor.NULLREAD) {
                g.setStroke(new BasicStroke(3));
                this.oDrawLine(g, pDistance, pHead);
                g.setStroke(new BasicStroke(1));
            }
            g.setColor(OleDashBoard.cAngle);
            g.setStroke(new BasicStroke(3, 0, 0, 10, new float[]{10}, 0));
            this.oDrawLine(g, pVariableDown, pHead);
            g.setStroke(new BasicStroke(1));

            g.setColor(OleDashBoard.cDial);
            if (externalDecoder.getCompass() == Perceptor.NULLREAD) {
                sRead = "---";
            } else {
                sRead = String.format("[ %03dº ]", (int) externalDecoder.getCompass());
            }
            tf = new TextFactory(g);
            tf.setPoint(pCenterTopFixed).setsText(sRead).
                    setHalign(SwingConstants.CENTER).setValign(SwingConstants.BOTTOM).setFontSize(20).setTextStyle(Font.BOLD).validate();
            g.setColor(OleDashBoard.cDial);
            tf.draw();

            tf = new TextFactory(g);
            tf.setX(screenPort.x).setY(screenPort.y).setsText("Compass: " + sRead).
                    setHalign(SwingConstants.LEFT).setValign(SwingConstants.TOP).setFontSize(15).setTextStyle(Font.BOLD).validate();
            tf.draw();

            g.setColor(OleDashBoard.cDistance);
            if (externalDecoder.getDistance() == Perceptor.NULLREAD) {
                sRead = "---";
            } else {
                sRead = String.format(" %03dm ", (int) (externalDecoder.getDistance()));
            }
            tf = new TextFactory(g).setPoint(pDistance).setFontSize(14).setsText(sRead).
                    setHalign(SwingConstants.RIGHT).setValign(SwingConstants.BOTTOM).setTextStyle(Font.BOLD).
                    setAngle(90 - shiftAngularHud()).validate();
            tf.draw();
            tf = new TextFactory(g);
            tf.setX(screenPort.x).setY(screenPort.y + 40).setsText("Distance: " + sRead).
                    setHalign(SwingConstants.LEFT).setValign(SwingConstants.TOP).setFontSize(15).setTextStyle(Font.BOLD).validate();
            tf.draw();

            g.setColor(OleDashBoard.cAngle);
            if (externalDecoder.getCompass() == Perceptor.NULLREAD) {
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

            if (externalDecoder.getMaxlevel() >= 0) {
                g.setColor(OleDashBoard.cBad);
                sRead = String.format(" %03dm ", externalDecoder.getMaxlevel());
                tf = new TextFactory(g);
                tf.setX(screenPort.x + screenPort.width - 150).setY(screenPort.y).setsText("MaxFlight: " + sRead).
                        setHalign(SwingConstants.LEFT).setValign(SwingConstants.TOP).setFontSize(15).setTextStyle(Font.BOLD).validate();
                tf.draw();
            }
            g.setClip(null);
//            this.oDrawCounter(g, sRead, pCenterTopFixed, cell, SwingConstants.CENTER, SwingConstants.BOTTOM);
        }

        return this;
    }

    public void clearTrail() {
        Trails.clear();
    }

    public void addTrail(String name, SimpleVector3D p) {
        if (Trails.get(name) == null) {
            Trails.put(name, new ArrayList());
        }
        Rectangle r = SwingTools.doNarrow(this.getBounds(), 6);
        Trails.get(name).add(0, p);
        if (Trails.get(name).size() > trailSize) {
            Trails.get(name).remove(Trails.get(name).size() - 1);
        }

    }

    public Point3D viewP(Point3D p) {
        return new Point3D(viewX(p.getX()), viewY(p.getY()));
    }

    public int viewX(double x) {
        return (int) (viewPort.x + (x + 0.4) * scale);
    }

    public int viewY(double y) {
        return (int) (viewPort.y + (y + 0.4) * scale);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        isMap = !isMap;
    }

    protected void paintGoalMap(Graphics2D g, JsonObject jsgoal) {
        SimpleVector3D p = new SimpleVector3D(new Point3D(jsgoal.getString("position", "")), Compass.NORTH);
        int diam1 = 5, diam2 = 2;
        g.setColor(OleDashBoard.cGoal);
        g.draw(this.TraceRegularStar(p, 4, diam1, diam2));
//        g.drawOval((int) viewX(p.getSource().getX()) - diam2,
//                (int) (viewY(p.getSource().getY())) - diam2,
//                diam1, diam1);
//        g.drawOval((int)(viewPort.x+viewPort.width*p.getX()/map.getWidth()), 
//                (int)(viewPort.y+viewPort.height*p.getY()/map.getHeight()), 
//                diam1, diam1);

    }

    public Polygon TraceRomboid(SimpleVector3D sv, int length) {
        int xsv = viewX(sv.getSource().getX()), ysv = viewY(sv.getSource().getY());
        p = new Polygon();
        p.addPoint(xsv - length, ysv);
        p.addPoint(xsv, ysv - length);
        p.addPoint(xsv + length, ysv);
        p.addPoint(xsv, ysv + length);
        p.addPoint(xsv - length, ysv);
        return p;
    }

    public Polygon TraceRegularPolygon(SimpleVector3D sv, int npoints, int radius1) {
        int xsv = viewX(sv.getSource().getX()), ysv = viewY(sv.getSource().getY());
        Point3D pxsv = new Point3D(xsv, ysv), p1, pmid1, p2;
        double alpha;
        p = new Polygon();
        for (int np = 0; np < npoints; np++) {
            p1 = at.alphaPoint(360 / npoints * np, radius1, pxsv);
            p.addPoint(p1.getXInt(), p1.getYInt());
        }
        p1 = at.alphaPoint(0, radius1, pxsv);
        p.addPoint(p1.getXInt(), p1.getYInt());
        return p;
    }

    public Polygon TraceRegularStar(SimpleVector3D sv, int npoints, int radius1, int radius2) {
        int xsv = viewX(sv.getSource().getX()), ysv = viewY(sv.getSource().getY());
        Point3D pxsv = new Point3D(xsv, ysv), p1, pmid1, p2;
        double alpha = 0, increment = 360 / npoints;
        p = new Polygon();
        for (int np = 0; np < npoints; np++) {
            p1 = at.alphaPoint(alpha, radius1, pxsv);
            p.addPoint(p1.getXInt(), p1.getYInt());
            p1 = at.alphaPoint(alpha + increment / 2, radius2, pxsv);
            p.addPoint(p1.getXInt(), p1.getYInt());
            alpha += increment;
        }
        p1 = at.alphaPoint(0, radius1, pxsv);
        p.addPoint(p1.getXInt(), p1.getYInt());
        return p;
    }

//    public Polygon TraceTriangle(SimpleVector3D sv, int length) {
//        int xsv = viewX(sv.getSource().getX()), ysv = viewY(sv.getSource().getY());
//        Point3D p;
//        Polygon t=new Polygon();
//        p = at.alphaPoint(sv.getsOrient()*45, length, sv.getSource());
//        t.addPoint(viewX(p.getX()), viewY(p.getY()));
//        p = at.alphaPoint(sv.getsOrient()*45, length, sv.getSource());
//        t.addPoint(viewX(p.getX()), viewY(p.getY()));
//        return p;
//    }
    public Polygon TraceCourse(SimpleVector3D sv, int length) {
        int xsv = viewX(sv.getSource().getX()), ysv = viewY(sv.getSource().getY()), xsv2 = xsv + sv.canonical().getTarget().getXInt() * length, ysv2 = ysv + sv.canonical().getTarget().getYInt() * length;
        p = new Polygon();
        p.addPoint(xsv, ysv);
        p.addPoint(xsv2, ysv2);
        p.addPoint(xsv, ysv);
        return p;
    }

//    public void traceLabel(Graphics2D g, SimpleVector3D sv, int length, String name) {
//        int xl = viewX(sv.getSource().getXInt()) + length, yl = viewY(sv.getSource().getYInt()), halign, valign;
//        int n = 0;
//        String s, climb;
//        JLabel jl;
//        halign = SwingConstants.LEFT;
//        valign = SwingConstants.CENTER;
//        yl = viewY(sv.getSource().getYInt());
//        s = name;
//        if (sv.canonical().getTarget().getZ() > 0) {
//            climb = emojis.UPRIGHTARROW; //"+";
//        } else if (sv.canonical().getTarget().getZ() < 0) {
//            climb = emojis.DOWNRIGHTARROW;
//        } else {
//            climb = " ";
//        }
//        s = s+"\n"+String.format("%03d %2s %s", (int) sv.getSource().getZInt(), SimpleVector3D.Dir[sv.getsOrient()], climb);
//        jl=new JLabel(s);
//        jl.setFont(g.getFont().deriveFont(Font.BOLD));
//        
//        g.setStroke(new BasicStroke(2));
//        g.drawLine(viewX(sv.getSource().getX()), viewY(sv.getSource().getY()), xl, yl);
//        g.setStroke(new BasicStroke(1));
//    }
    public void traceLabel(Graphics2D g, SimpleVector3D sv, SimpleVector3D prevsv, int length, String name, Rectangle viewPort) {
        System.out.println("tracelabel");
        int halign, valign, incrx, incry;
        Point3D pLabel, pSource = viewP(sv.getSource()), pIncrement;
        if (pSource.getX() > viewPort.width / 2 && pSource.getY() < viewPort.height / 2) { // NE
            pLabel = at.alphaPoint(225, length, viewP(sv.getSource()));
            halign = SwingConstants.RIGHT;
            valign = SwingConstants.CENTER;
            pIncrement=new Point3D(-1,+1);
        } else if (pSource.getX() > viewPort.width / 2 && pSource.getY() > viewPort.height / 2) { //SE
            pLabel = at.alphaPoint(135, length, viewP(sv.getSource()));
            halign = SwingConstants.RIGHT;
            valign = SwingConstants.CENTER;
            pIncrement=new Point3D(-1,-1);
        } else if (pSource.getX() < viewPort.width / 2 && pSource.getY() < viewPort.height / 2) { // NW
            pLabel = at.alphaPoint(315, length, viewP(sv.getSource()));
            halign = SwingConstants.LEFT;
            valign = SwingConstants.CENTER;
            pIncrement=new Point3D(+1,+1);
        } else {
            pLabel = at.alphaPoint(45, length, viewP(sv.getSource())); // SW
            halign = SwingConstants.LEFT;
            valign = SwingConstants.CENTER;
            pIncrement=new Point3D(1,-1);
        }
        int n = 0;
        TextFactory tf;
        String s, climb;
        s = name;

        g.setColor(OleDashBoard.cTrack);
        tf = new TextFactory(g);
        tf.setPoint(pLabel).setsText(s).setFontSize(14).setTextStyle(Font.BOLD).setHalign(halign).setValign(valign).validate();
        tf.draw();
        

        SimpleVector3D last = new SimpleVector3D(prevsv.getSource(), sv.getSource());
        if (last.canonical().getTarget().getZ() > 0) {
            climb = emojis.UPRIGHTARROW;//"+";
        } else if (last.canonical().getTarget().getZ() < 0) {
            climb = emojis.DOWNRIGHTARROW; //"-";
        } else {
            climb = emojis.RIGHTARROW; //" ";
        }
        s = String.format("%03d%s%s", (int) sv.getSource().getZInt(), climb, SimpleVector3D.Dir[sv.getsOrient()]);
        tf = new TextFactory(g);
        pLabel.setY(pLabel.getY() + 14);
        tf.setPoint(pLabel).setsText(s).setsFontName(Font.MONOSPACED).setFontSize(14).setTextStyle(Font.BOLD).setHalign(halign).setValign(valign).validate();
        tf.draw();
        g.setStroke(new BasicStroke(2));
        this.oDrawLine(g, viewP(sv.getSource()), pLabel);
        g.setStroke(new BasicStroke(1));
    }
}
