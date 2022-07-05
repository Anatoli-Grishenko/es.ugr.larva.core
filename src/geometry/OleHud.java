/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import com.eclipsesource.json.JsonObject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.SwingConstants;
import map2D.Map2DColor;
import map2D.Palette;
import swing.OleButton;
import swing.OleDashBoard;
import static swing.OleDashBoard.cAngle;
import swing.OleDrawPane;
import swing.OleFrame;
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
    protected Point3D hudPoints[][];
    protected int vpStrecht = 37, spStrecht = 22, twidth, theight, tx;
    protected double tscale;
    protected Polygon p;
    protected int cell, nLevels, nTiles, trailSize = 0, limitScale = 7, lastZ = -1, countTerrain, countHud;
    protected boolean showTrail;
    protected Point3D pCenterTopFixed, pVariableDown, pCenterFixed, pVariableTop,
            pDistance, pHead;
    protected TextFactory tf;
    protected double stepRadius, stepAngle;
    protected AngleTransporter at;
    protected ArrayList<Map2DColor> sprites;
    protected Map2DColor terrain;
    protected OleDashBoard myDash;
    protected Palette palette;

    public OleHud(OleDrawPane parent, String name) {
        super(parent, name);
        this.setPreferredSize(new Dimension(this.getBounds().width, this.getBounds().height));
        this.setLayout(null);
        Trails = new HashMap();
        isMap = false;
        at = parentPane.getAngleT();
        myDash = ((OleDashBoard) this.parentPane);
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
        palette = new Palette();
        palette.addWayPoint(0, Color.BLACK); // BW
        palette.addWayPoint(100, Color.WHITE);
        palette.fillWayPoints(256);
        this.resetTerrain();
//        this.addMouseListener(this);
    }

    @Override
    public void validate() {
        super.validate();

        screenPort = SwingTools.doNarrow(this.getBounds(), spStrecht);
        viewPort = SwingTools.doNarrow(screenPort, vpStrecht);
        viewPort.x = screenPort.x + screenPort.width - viewPort.width;
        viewPort.y = screenPort.y;
        twidth = screenPort.width - 20;
        theight = 180; //screenPort.height-viewPort.height;
        tscale = theight / 256.0;

//        screenPort.y += 16;
        shiftx = viewPort.x - screenPort.x;
        shifty = viewPort.y - screenPort.y;
        this.setScaledCoordinates(false);
    }

    @Override
    public void setCurrentValue(double d) {
        if (hudView != null && myDash.getMyDecoder().getPolarVisual() != null) {
            if (tx == 0 || (myDash.getMyDecoder().getGPSMemorySize() > 1 && myDash.getMyDecoder().getGPSMemory(1).planeDistanceTo(myDash.getMyDecoder().getGPS()) > 0)) {
                this.addTerrain(1, (int) (myDash.getMyDecoder().getAltitude() - myDash.getMyDecoder().getGround()), (int) myDash.getMyDecoder().getAltitude(), myDash.getMyDecoder().getMaxlevel());
            } else {
                this.addTerrain(0, (int) (myDash.getMyDecoder().getAltitude() - myDash.getMyDecoder().getGround()), (int) myDash.getMyDecoder().getAltitude(), myDash.getMyDecoder().getMaxlevel());
            }
        }
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
        if (myDash.getMyDecoder().getWorldMap() != null) {
            scale = viewPort.getWidth() / this.myDash.getMyDecoder().getWorldMap().getMap().getWidth();
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
        if (myDash.getMyDecoder().getPolarVisual() != null) {
            pDistance = at.alphaPoint(shiftAngularHud(), Math.min(nLevels, myDash.getMyDecoder().getDistance()) * lengthVisual / nLevels, center);
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
                tf.setPoint(p2).setFontSize(10).setValue((360 + (int) (myDash.getMyDecoder().getCompass() + alpha) - 90) % 360).setAngle(90 - (int) alpha).
                        setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
                tf.draw();
            }
        }
        if (myDash.getMyDecoder().getPolarVisual() != null) {
            nLevels = myDash.getMyDecoder().getPolarVisual()[0].length;
            stepRadius = lengthVisual / (nLevels + 1);
            hudView = new Polygon[nLevels][];
            hudPoints = new Point3D[nLevels][];
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
        return (int) (valueAngularHud() - myDash.getMyDecoder().getCompass() + 360 + 90) % 360;
//        return (int) (valueAngularHud() - this.getAllReadings()[0][0] + 90 + 360) % 360;
    }

    protected int valueAngularHud() {
        return (int) (myDash.getMyDecoder().getAbsoluteAngular()) % 360;
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
        if (hudView != null && myDash.getMyDecoder().getPolarVisual() != null) {
//            hudPoints = myDash.getMyDecoder().getPolarPoints();
            for (int level = nLevels - 1; level >= 0; level--) {
//            for (int level = 0; level < nLevels; level++) {
                nTiles = (level) * 4 + 1;
                for (int tile = 0; tile < nTiles; tile++) {
                    lTile = myDash.getMyDecoder().getPolarVisual()[tile][level];
                    tTile = myDash.getMyDecoder().getPolarCourse()[tile][level];
                    stroke = 1;
                    if (lTile >= 0) {
                        cTile = new Color(lTile, lTile, lTile);
                    } else {
                        cTile = OleDashBoard.cBad;
                    }
                    iGround = lTile;

                    stroke = 0;
                    cStroke = cTile;
                    cBackground = cTile;
//
                    if (iGround < 0) {
                        cStroke = OleDashBoard.cBad;
                        cBackground = OleDashBoard.cBad;
                    } else if (iGround > myDash.getMyDecoder().getMaxlevel() || iGround < myDash.getMyDecoder().getMinlevel()) {
                        cStroke = OleDashBoard.cBad;
                        cBackground = OleDashBoard.cBad;
                    } else if (myDash.getMyDecoder().getMaxslope() == 255) { // Airborne
                        if (iGround > myDash.getMyDecoder().getAltitude()) {
                            cStroke = OleDashBoard.cBad;
                            cBackground = cTile;
                            stroke = 2;
                        }
                    } else if (myDash.getMyDecoder().getMaxslope() < Math.abs(lTile - myDash.getMyDecoder().getPolarVisual()[0][0])) {
                        cStroke = OleDashBoard.cBad;
                        cBackground = OleDashBoard.cBad;
                        stroke = 0;
                    }
                    if (tTile > 0) {
                        cStroke = OleDashBoard.cAngle;
                        stroke = 3;
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
        g.setStroke(new BasicStroke(1));
        this.oDrawArc(g, center, 1.0 * lengthVisual / nLevels, 0, 180);
        g.setStroke(new BasicStroke(1));
//        if (scale < limitScale) {
        p = new Polygon();
        p.addPoint(pHead.getXInt(), pHead.getYInt());
        p.addPoint(center.getXInt() - 10, center.getYInt());
        p.addPoint(center.getXInt() + 10, center.getYInt());
        p.addPoint(pHead.getXInt(), pHead.getYInt());
        g.draw(p);
//        } else {
//            g.drawImage(sprites.get(0).getMap(), center.getXInt() - 18, center.getYInt() - 12, 36, 36, null);
//
//        }
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

        if (myDash.getMyDecoder().getDistance() != Perceptor.NULLREAD && myDash.getMyDecoder().getDistance() > 1) {
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
        if (myDash.getMyDecoder().getCompass() == Perceptor.NULLREAD) {
            sRead = "---";
        } else {
            sRead = String.format("[ %03dº ]", (int) myDash.getMyDecoder().getCompass());
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
        pDistance = at.alphaPoint(shiftAngularHud(), Math.min(nLevels, myDash.getMyDecoder().getDistance()) * lengthVisual / nLevels, center);
        if (myDash.getMyDecoder().getDistance() == Perceptor.NULLREAD) {
            sRead = "---";
        } else {
            if (myDash.getMyDecoder().getDistance() > 0) {
                sRead = String.format(" %03dm ", (int) (Math.round(myDash.getMyDecoder().getDistance())));
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
        if (myDash.getMyDecoder().getCompass() == Perceptor.NULLREAD) {
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

        if (myDash.getMyDecoder().getMaxlevel() >= 0) {
            g.setColor(OleDashBoard.cBad);
            sRead = String.format(" %03dm ", myDash.getMyDecoder().getMaxlevel());
            tf = new TextFactory(g);
            tf.setX(viewPort.x + viewPort.width - 175).setY(center.getYInt() + 10).setFontSize(15).setTextStyle(Font.BOLD).
                    setHalign(SwingConstants.LEFT).setValign(SwingConstants.TOP).
                    setsText("Max Flight: " + sRead).validate();

            tf.draw();
        }
//        int d[][] = myDash.getMyDecoder().getPolarVisual();
//        int p = 0;
        g.setClip(null);
//                this.addTerrain(1, (int) (myDash.getMyDecoder().getAltitude() - myDash.getMyDecoder().getGround()), (int) myDash.getMyDecoder().getAltitude(), myDash.getMyDecoder().getMaxlevel());
//            for (int x = 0; x < d[0].length; x++) {
//                if (x > 0) {
//                    p = (2 * (x + 1) + 1) / 2;
//                }
//                this.addTerrain(tx + x, d[p][x], -1, myDash.getMyDecoder().getMaxlevel());
//            }

        System.out.print("\n\nHud " + (this.countHud++) + "\n\n");
        g.drawImage(terrain.getMap(), screenPort.x + 10, viewPort.height - 90, null);
        g.setColor(Color.WHITE);
        g.drawRect(screenPort.x + 9, viewPort.height - 91, terrain.getWidth() + 2, terrain.getHeight() + 2);
        System.out.print("\n\nTerrain " + (this.countTerrain++) + "\n\n");
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
        if (twidth > 0 && theight > 0) {
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
        }
        tx = 0;
        lastZ = -1;
        this.countTerrain = 0;
        this.countHud = 0;
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
            terrain.setColor(tx, terrain.getHeight() - 1 - y, SwingTools.mergeColors(c, terrain.getColor(tx, terrain.getHeight() - 1 - y), (int) (y * 100 / (terrain.getHeight() * 0.50))));
        }
    }

    public void mouseClicked(MouseEvent e) {
        int lTile;
        String res = "";
        if (odpPopUp == null) {
            hudPoints = myDash.getMyDecoder().getPolarPoints();
            for (int level = nLevels - 1; level >= 0; level--) {
                nTiles = (level) * 4 + 1;
                for (int tile = 0; tile < nTiles; tile++) {
                    lTile = myDash.getMyDecoder().getPolarVisual()[tile][level];
                    if (hudView[level][tile].contains(new Point(e.getX(), e.getY()))) {
                        Point3D p = hudPoints[tile][level];
                        this.setPopUpData(p.getXInt(), p.getYInt());
                        odpPopUp = new OleDrawPane() {
                            @Override
                            public void OleDraw(Graphics2D g) {
                                paintPalette(g, palette, this.getBounds());
                            }

                        };
                        odpPopUp.setBounds(this.getBounds().x, this.getBounds().y, 35, this.getBounds().height);
                        myDash.add(odpPopUp);
                        odpPopUp.setVisible(true);
                        myDash.repaint();
                    }
                }
            }
        } else {
            odpPopUp.setVisible(false);
            myDash.remove(odpPopUp);
            odpPopUp = null;
        }
    }

//    public void mouseClicked(MouseEvent e) {
//        int lTile;
//        String res = "";
//        OleDrawPane odpPopUp;
//        hudPoints = myDash.getMyDecoder().getPolarPoints();
////        for (int y = 0; y < pmatrix[0].length; y++) {
////            for (int x = 0; x < pmatrix.length; x++) {
////                if (pmatrix[x][y] != null) {
////                    res += pmatrix[x][y].toString()+"  ";
////                }
////            }
////            res += "\n";
////        }
////        System.out.println("MINIHUD\n"+res);
////        hudPoints = myDash.getMyDecoder().getPolarPoints();
//        for (int level = nLevels - 1; level >= 0; level--) {
//            nTiles = (level) * 4 + 1;
//            for (int tile = 0; tile < nTiles; tile++) {
//                lTile = myDash.getMyDecoder().getPolarVisual()[tile][level];
//                if (hudView[level][tile].contains(new Point(e.getX(), e.getY()))) {
//                    Point3D p = hudPoints[tile][level];
////                    System.out.println("level: " + level + " tile:" + tile+" "+p.toString());
//                    this.of = new OleFrame("Palette") {
//                        @Override
//                        public void myActionListener(ActionEvent e) {
//                        }
//
//                        @Override
//                        public void myKeyListener(KeyEvent e) {
//                        }
//
//                        @Override
//                        public void keyTyped(KeyEvent e) {
//                        }
//
//                        @Override
//                        public void keyPressed(KeyEvent e) {
//                        }
//
//                        @Override
//                        public void keyReleased(KeyEvent e) {
//                        }
//
//                    };
//                    of.setContentPane(new OleDrawPane() {
//                        @Override
//                        public void OleDraw(Graphics2D g) {
//                            paintPalette(g, palette, p.getXInt(), p.getYInt());
//                        }
//
//                    });
//                    of.pack();
//                    of.setSize(50, 600);
//                    of.setLocation(this.getBounds().x, this.getBounds().y);
//                    of.setVisible(true);
//                }
//            }
//        }
//    }
//
//    public void mouseReleased(MouseEvent e) {
//        if (of != null) {
//            of.dispose();
//        }
//    }
}
