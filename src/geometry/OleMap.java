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
public class OleMap extends OleSensor {

    protected HashMap<String, ArrayList<SimpleVector3D>> Trails;
    protected Polygon hudView[][];
    protected int narrow = 37, margin = 22;
    protected Polygon p;
    protected int cell, nLevels, nTiles, trailSize = 0, limitScale = 7;
    protected boolean showTrail;
    protected Point3D pCenterTopFixed, pVariableDown, pCenterFixed, pVariableTop, pDistance, pHead;
    protected TextFactory tf;
    protected double stepRadius, stepAngle;
    protected AngleTransporter at;
    protected SensorDecoder externalDecoder;
    protected ArrayList<Map2DColor> sprites;

    public OleMap(OleDrawPane parent, String name) {
        super(parent, name);
        this.setLayout(null);
        Trails = new HashMap();
        isMap = true;
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

        screenPort = this.getBounds(); //SwingTools.doNarrow(this.getBounds(), margin);
        viewPort = screenPort; //SwingTools.doNarrow(this.getBounds(), narrow);
//        viewPort.y += 16;
//        screenPort.y += 16;
        shiftx = viewPort.x - screenPort.x;
        shifty = viewPort.y - screenPort.y;
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
        if (map != null) {
            g.setClip(viewPort);
            if (externalDecoder.getWorldMap() != null) {
                scale = viewPort.getWidth() / this.externalDecoder.getWorldMap().getMap().getWidth();
                if (scale < limitScale) {
                    hasGrid = false;
                } else {
                    hasGrid = true;
                }
            }
            setMinValue(0);
            setMaxValue(map.getWidth());
            setMinVisual(viewPort.x);
            setMaxVisual(viewPort.x + viewPort.width);
            lengthVisual = getMaxVisual() - getMinVisual();
            stepVisual = lengthVisual / nMarks;
            lengthValue = (maxValue - minValue);
            stepValue = lengthValue / nMarks;
            stepValue2 = 1;
            setnDivisions(10);
            drawLineRuler(g, screenPort, 10);
            g.setClip(null);
        }
        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {
        layoutSensor(g);
        String label;
        if (map != null) {
            RescaleOp darken = new RescaleOp(0.5f, 0, null);
            g.drawImage(darken.filter(map.getMap(), null), viewPort.x, viewPort.y, viewPort.width, viewPort.height, null);
            SimpleVector3D ptrail, prevTrail, ptext, ppoint;
            double xVP, yVP;
            Color c;
            g.setClip(viewPort);
            drawLineRuler(g, screenPort, 10);
            for (String name : Trails.keySet()) {
                if (this.isShowTrail()) {

                    for (int i = 1; i < Trails.get(name).size(); i++) {
                        ptrail = Trails.get(name).get(i);
                        if ((this.externalDecoder.getNSteps() - i) % 5 == 0) {
//                            tf = new TextFactory(g).setsText(String.format("%03d", this.externalDecoder.getNSteps()))
//                                    .setPoint(viewP(ptrail.getSource())).setFontSize(10)
//                                    .setValign(SwingConstants.CENTER).setHalign(SwingConstants.CENTER).validate();
//                            tf.draw();
//                            this.oDrawCounter(g, String.format("%03d", this.externalDecoder.getNSteps()-i),
//                                    viewP(ptrail.getSource()),30, SwingConstants.CENTER, SwingConstants.CENTER);
                        }
                        c = map.getColor(ptrail);
                        g.setColor(new Color(0, (Trails.get(name).size() - i) * 255 / Trails.get(name).size(), 0));
                        g.fill(this.TraceRegularPolygon(ptrail, 4, 3));
//                        if (scale < 7) {
//                            g.fill(this.TraceRegularPolygon(ptrail, 4, 3));
//                        } else {
//
//                            p = this.TraceBot(ptrail, 8, 10);
//                            g.drawPolygon(p);
//                        }
                    }
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
                g.setStroke(new BasicStroke(2));
                if (scale < limitScale) {
                    p = this.TraceRegularPolygon(ptrail, 4, 5);
                    g.drawPolygon(p);
                    p = this.TraceCourse(ptrail, 20);
                    g.drawPolygon(p);
                } else {
                    g.drawImage(sprites.get(externalDecoder.getCompass() / 45).getMap(), viewX(ptrail.getSource().getXInt()) - 12, viewY(ptrail.getSource().getYInt()) - 12, (int) scale, (int) scale, null); //(int)scale*10,(int)scale*10, Image.SCALE_SMOOTH),  0,0,null);
//                                viewX(ptrail.getSource().getXInt())-(int)scale/2, viewY(ptrail.getSource().getYInt())-(int)scale/2, null);
//                        p = this.TraceBot(ptrail, 8, 10);
//                        g.drawPolygon(p);
                }

//                    p = this.TraceRegularStar(ptrail, 6, 30, 15);
//                    p = this.TraceRomboid(ptrail, 5);
                g.setStroke(new BasicStroke(1));
                this.traceLabel(g, ptrail, prevTrail, 25, name, viewPort);
            }
            for (int i = 0; i < jsaGoals.size(); i++) {
                paintGoalMap(g, jsaGoals.get(i).asObject());
            }
            g.setClip(null);
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
        if (Trails.get(name).size() > getTrailSize()) {
            Trails.get(name).remove(Trails.get(name).size() - 1);
        }

    }

    protected void paintGoalMap(Graphics2D g, JsonObject jsgoal) {
        SimpleVector3D p = new SimpleVector3D(new Point3D(jsgoal.getString("position", "")), Compass.NORTH);
        int diam1 = 6, diam2 = diam1 / 2;
        g.setColor(OleDashBoard.cGoal);
        g.draw(this.TraceRegularStar(p, 4, diam1, diam2));
    }

    public void traceLabel(Graphics2D g, SimpleVector3D sv, SimpleVector3D prevsv, int length, String name, Rectangle viewPort) {
        System.out.println("tracelabel");
        int halign, valign, incrx, incry;
        Point3D pLabel, pSource = viewP(sv.getSource()), pIncrement;
        if (pSource.getX() > viewPort.width / 2 && pSource.getY() < viewPort.height / 2) { // NE
            pLabel = at.alphaPoint(225, length, viewP(sv.getSource()));
            halign = SwingConstants.RIGHT;
            valign = SwingConstants.CENTER;
            pIncrement = new Point3D(-1, +1);
        } else if (pSource.getX() > viewPort.width / 2 && pSource.getY() > viewPort.height / 2) { //SE
            pLabel = at.alphaPoint(135, length, viewP(sv.getSource()));
            halign = SwingConstants.RIGHT;
            valign = SwingConstants.CENTER;
            pIncrement = new Point3D(-1, -1);
        } else if (pSource.getX() < viewPort.width / 2 && pSource.getY() < viewPort.height / 2) { // NW
            pLabel = at.alphaPoint(315, length, viewP(sv.getSource()));
            halign = SwingConstants.LEFT;
            valign = SwingConstants.CENTER;
            pIncrement = new Point3D(+1, +1);
        } else {
            pLabel = at.alphaPoint(45, length, viewP(sv.getSource())); // SW
            halign = SwingConstants.LEFT;
            valign = SwingConstants.CENTER;
            pIncrement = new Point3D(1, -1);
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
        this.oDrawLine(g, viewP(sv.getSource().clone().plus(new Point3D(4,0))), pLabel);
        g.setStroke(new BasicStroke(1));
    }

    public int getTrailSize() {
        return trailSize;
    }

    public void setTrailSize(int trailSize) {
        this.trailSize = trailSize;
    }

    public boolean isShowTrail() {
        return showTrail;
    }

    public void setShowTrail(boolean showTrail) {
        this.showTrail = showTrail;
    }

}
