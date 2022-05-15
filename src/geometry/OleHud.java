/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import com.eclipsesource.json.JsonObject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.SwingConstants;
import map2D.Map2DColor;
import swing.OleButton;
import swing.OleDashBoard;
import static swing.OleDashBoard.cAngle;
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
public class OleHud extends OleSensor {

    protected HashMap<String, ArrayList<SimpleVector3D>> Trails;
    protected Polygon hudView[][];
    protected int vpStrecht = 37, spStrecht = 22, twidth, theight, tx;
    protected double tscale;
    protected Polygon p;
    protected int cell, nLevels, nTiles, trailSize = 0, limitScale = 7, lastZ = -1;
    protected boolean showTrail;
    protected Point3D pCenterTopFixed, pVariableDown, pCenterFixed, pVariableTop, pDistance, pHead;
    protected TextFactory tf;
    protected double stepRadius, stepAngle;
    protected AngleTransporter at;
    protected SensorDecoder externalDecoder;
    protected ArrayList<Map2DColor> sprites;
    protected Map2DColor terrain;

    public OleHud(OleDrawPane parent, String name) {
        super(parent, name);
        this.setLayout(null);
        Trails = new HashMap();
        isMap = false;
        at = parentPane.getAngleT();
        externalDecoder = ((OleDashBoard) this.parentPane).decoder;
        setnRows(1);
        setnColumns(3);
        sprites = new ArrayList();
        try {
            for (int i = 0; i < 8; i++) {
                sprites.add(new Map2DColor().loadMapRaw(getClass().getResource("/resources/icons/explorer" + i + ".png").toString().replace("file:", "")));
            }
        } catch (IOException ex) {
        }
        hasGrid = false;
    }

    @Override
    public void validate() {
        super.validate();

        screenPort = SwingTools.doNarrow(this.getBounds(), spStrecht);
        viewPort = SwingTools.doNarrow(screenPort, vpStrecht);
        viewPort.x = screenPort.x + screenPort.width - viewPort.width;
        viewPort.y = screenPort.y;
        twidth = viewPort.width - 25;
        theight = 100; //screenPort.height-viewPort.height;
        tscale = theight / 256.0;

//        screenPort.y += 16;
        shiftx = viewPort.x - screenPort.x;
        shifty = viewPort.y - screenPort.y;
        this.setScaledCoordinates(false);
    }

    @Override
    public OleSensor layoutSensor(Graphics2D g) {
        if (showFrame) {
            g.setColor(OleDashBoard.cDeck);
            g.fillRect(mX, mY, mW, mH);
            g.setColor(OleDashBoard.cFrame);
            g.fillRoundRect(mX + 3, mY + 3, mW - 6, mH - 6, 10, 10);
        }
        g.setColor(Color.BLACK);
        g.fill(screenPort);
        if (externalDecoder.getWorldMap() != null) {
            scale = viewPort.getWidth() / this.externalDecoder.getWorldMap().getMap().getWidth();
        }
        g.setColor(Color.WHITE);
        g.draw(screenPort);
//        g.draw(viewPort);
        g.setClip(viewPort);
        Point3D p0, p1, p2, p3, p4;
        double radius1, radius2, alpha1, alpha2;

        cell = viewPort.width / 8;
        center = new Point3D(viewPort.x + viewPort.width / 2, viewPort.y + viewPort.height - 2 * cell);
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
        g.setStroke(new BasicStroke(2));

        this.oDrawArc(g, center, 5 * cell, 0, 180);
        g.setStroke(new BasicStroke(1));
        for (double alpha = 135; alpha >= 45; alpha -= 5) {
            p1 = at.alphaPoint(alpha, 5 * cell, center);
            p2 = at.alphaPoint(alpha, 4.9 * cell, center);
            this.oDrawLine(g, p1, p2);
            if ((int) alpha % 15 == 0) {
                tf = new TextFactory(g);
                tf.setPoint(p2).setFontSize(10).setValue((360 + (int) (externalDecoder.getCompass() + alpha) - 90) % 360).setAngle(90 - (int) alpha).
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
        String sCompass, sGoal, sDistance;
        double radius1 = 0, radius2 = 0, alpha1, alpha2;
        Point3D p0, p1, p2, p3;

        g.setClip(viewPort);
        Color cTile, cBackground, cStroke;
        int iGround, lTile, tTile, stroke;
        if (hudView != null && externalDecoder.getPolarVisual() != null) {
            for (int level = 0; level < nLevels; level++) {
                nTiles = (level) * 4 + 1;
                for (int tile = 0; tile < nTiles; tile++) {
                    lTile = externalDecoder.getPolarVisual()[tile][level];
                    tTile = externalDecoder.getPolarThermal()[tile][level];
                    stroke=1;
                    if (lTile >= 0) {
                        cTile = new Color(lTile, lTile, lTile);
                    } else {
                        cTile = OleDashBoard.cBad;
                    }
                    iGround = lTile;
//
                    if (tTile > 0) {
                        cStroke = OleDashBoard.cGoal;
                        cBackground = cTile;
                        stroke=2;
                    }else 
                        if (iGround < 0) {
                        cStroke = OleDashBoard.cBad;
                        cBackground = OleDashBoard.cBad;
                    } else if (iGround > externalDecoder.getMaxlevel() || iGround < externalDecoder.getMinlevel()) {
                        cStroke = OleDashBoard.cBad;
                        cBackground = OleDashBoard.cBad;
                    } else if (iGround > externalDecoder.getAltitude()) {
                        cStroke = OleDashBoard.cBad;
                        cBackground = cTile;
                        stroke=2;
                    } else {
                        cStroke = cTile;
                        cBackground = cTile;
                    }
                    if (this.isHasGrid()) {
                        cStroke = Color.DARK_GRAY;
                    }
                    g.setColor(cBackground);
                    g.fill(hudView[level][tile]);
                    g.setColor(cStroke);
                    g.setStroke(new BasicStroke(stroke));
                    g.draw(hudView[level][tile]);
                    g.setStroke(new BasicStroke(1));
                }
            }
        }
        g.setColor(OleDashBoard.cDial);
        g.setStroke(new BasicStroke(2));
        this.oDrawArc(g, center, 15.0 * lengthVisual / nLevels, 0, 180);
        this.oDrawArc(g, center, 10.0 * lengthVisual / nLevels, 0, 180);
        this.oDrawArc(g, center, 5.0 * lengthVisual / nLevels, 0, 180);
        this.oDrawArc(g, center, 2.0 * lengthVisual / nLevels, 0, 180);
        this.oDrawArc(g, center, 1.0 * lengthVisual / nLevels, 0, 180);
        g.setStroke(new BasicStroke(1));
        if (scale < limitScale) {
            p = new Polygon();
            p.addPoint(pHead.getXInt(), pHead.getYInt());
            p.addPoint(center.getXInt() - 10, center.getYInt());
            p.addPoint(center.getXInt() + 10, center.getYInt());
            p.addPoint(pHead.getXInt(), pHead.getYInt());
            g.draw(p);
        } else {
            g.drawImage(sprites.get(0).getMap(), center.getXInt() - 18, center.getYInt() - 12, 36, 36, null);

        }
        g.clip(screenPort);
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

        if (externalDecoder.getDistance() != Perceptor.NULLREAD && externalDecoder.getDistance() > 1) {
//            g.setColor(OleDashBoard.cDistance);
            g.setColor(OleDashBoard.cAngle);
            pVariableDown = at.alphaPoint(shiftAngularHud(), 5.1 * cell, center);
            g.setStroke(new BasicStroke(3));
//            this.oDrawLine(g, pDistance, pHead);
//            g.setStroke(new BasicStroke(1));
//            g.setStroke(new BasicStroke(3, 0, 0, 10, new float[]{10}, 0));
            this.oDrawLine(g, pVariableDown, pHead);
            g.setStroke(new BasicStroke(1));
        }

        g.setColor(OleDashBoard.cDial);
        if (externalDecoder.getCompass() == Perceptor.NULLREAD) {
            sRead = "---";
        } else {
            sRead = String.format("[ %03dº ]", (int) externalDecoder.getCompass());
        }
        tf = new TextFactory(g);
        tf.setPoint(pCenterTopFixed).setsText(sRead).
                setHalign(SwingConstants.CENTER).setValign(SwingConstants.BOTTOM).setFontSize(15).setTextStyle(Font.BOLD).validate();
        g.setColor(OleDashBoard.cDial);
        tf.draw();

        tf = new TextFactory(g);
        tf.setX(viewPort.x + 10).setY(center.getYInt() + 10).setFontSize(15).setTextStyle(Font.BOLD).
                setHalign(SwingConstants.LEFT).setValign(SwingConstants.TOP).
                setsText("Compass: " + sRead).validate();
        g.setColor(OleDashBoard.cDial);
        tf.draw();

        g.setColor(OleDashBoard.cDistance);
        pDistance = at.alphaPoint(shiftAngularHud(), Math.min(nLevels, externalDecoder.getDistance()) * lengthVisual / nLevels, center);
        if (externalDecoder.getDistance() == Perceptor.NULLREAD) {
            sRead = "---";
        } else {
            if (externalDecoder.getDistance() > 0) {
                sRead = String.format(" %03dm ", (int) (Math.round(externalDecoder.getDistance())));
            } else {
                sRead = "";
            }
        }
        tf = new TextFactory(g).setPoint(pDistance).setFontSize(14).setsText(sRead).
                setHalign(SwingConstants.RIGHT).setValign(SwingConstants.TOP).setTextStyle(Font.BOLD).
                setAngle(90 - shiftAngularHud()).validate();
        tf.draw();
        this.TraceRegularPolygon(new SimpleVector3D(pDistance, 0), 4, 3);
        tf = new TextFactory(g);
        tf.setX(viewPort.x + 10).setY(center.getYInt() + 25).setFontSize(15).setTextStyle(Font.BOLD).
                setHalign(SwingConstants.LEFT).setValign(SwingConstants.TOP).
                setsText("Distance: " + sRead).validate();
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
        tf.setX(viewPort.x + 10).setY(center.getYInt() + 40).setFontSize(15).setTextStyle(Font.BOLD).
                setHalign(SwingConstants.LEFT).setValign(SwingConstants.TOP).
                setsText("Angle: " + sRead).validate();
        tf.draw();

        if (externalDecoder.getMaxlevel() >= 0) {
            g.setColor(OleDashBoard.cBad);
            sRead = String.format(" %03dm ", externalDecoder.getMaxlevel());
            tf = new TextFactory(g);
            tf.setX(viewPort.x + viewPort.width - 175).setY(center.getYInt() + 10).setFontSize(15).setTextStyle(Font.BOLD).
                    setHalign(SwingConstants.LEFT).setValign(SwingConstants.TOP).
                    setsText("Max Flight: " + sRead).validate();

            tf.draw();
        }
        int d[][] = externalDecoder.getPolarVisual();
        int p = 0;
        g.setClip(null);
        if (d != null) {
//            for (int x = 0; x < d[0].length; x++) {
//                if (x > 0) {
//                    p = (2 * (x + 1) + 1) / 2;
//                }
//                this.addTerrain(tx + x, d[p][x], -1, externalDecoder.getMaxlevel());
//            }
            if (tx == 0 || externalDecoder.getGPSMemory(1).planeDistanceTo(externalDecoder.getGPS()) > 0) {
                this.addTerrain(1, (int) (externalDecoder.getAltitude() - externalDecoder.getGround()), (int) externalDecoder.getAltitude(), externalDecoder.getMaxlevel());
            } else {
                this.addTerrain(0, (int) (externalDecoder.getAltitude() - externalDecoder.getGround()), (int) externalDecoder.getAltitude(), externalDecoder.getMaxlevel());
            }
            g.drawImage(terrain.getMap(), viewPort.x + 25, viewPort.height + 50, null);
            g.setColor(Color.WHITE);
            g.drawRect(viewPort.x - 1 + 25, viewPort.height + 49, terrain.getWidth() + 2, terrain.getHeight() + 2);
        }
        return this;
    }

    public int getVPStrecht() {
        return vpStrecht;
    }

    public void setVPStretch(int narrow) {
        this.vpStrecht = narrow;
    }

    public int getSPStretcht() {
        return spStrecht;
    }

    public void setSPStretch(int margin) {
        this.spStrecht = margin;
    }

    public void resetTerrain() {
        terrain = new Map2DColor(twidth, theight, Color.BLACK);
        for (int i = 0; i < terrain.getWidth(); i++) {
            for (int j = 0; j < terrain.getHeight(); j++) {
                if (j % 16 == 0 || i % 25 == 0) {
                    terrain.setColor(i, j, Color.DARK_GRAY);
                } else {
                    terrain.setColor(i, j, Color.BLACK);
                }
            }
        }
        tx = 0;
        lastZ = -1;
    }

    public void addTerrain(int t, int visual, int z, int maxlevel) {
        Color c;
        tx = tx + t;
        int y1 = (int) (Math.min(z, lastZ) * tscale),
                y2 = (int) (Math.max(z, lastZ) * tscale);
        for (int y = 0; y < theight; y++) {
            if (y <= visual * tscale) {
                c = OleDashBoard.cGround;
            } else {
                c = Color.BLACK;
            }
            if ((int) (maxlevel * tscale) == y) {
                c = Color.RED;
            }
            if (z >= 0) {
                if (lastZ < 0 || t > 0) {
                    lastZ = z;
                }
                if (y >= y1 && y <= y2) {
                    c = OleDashBoard.cTrack;
                }
            }
            terrain.setColor(tx, terrain.getHeight() - 1 - y, c);
        }
    }
}
