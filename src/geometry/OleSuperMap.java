/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import Environment.Environment;
import com.eclipsesource.json.JsonObject;
import data.OleConfig;
import glossary.Sensors;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import map2D.Map2DColor;
import map2D.Palette;
import swing.OleDashBoard;
import swing.OleDialog;
import swing.OleDrawPane;
import swing.OleFrame;
import swing.OleScrollPane;
import swing.OleSensor;
import swing.SwingTools;
import swing.TextFactory;
import tools.TimeHandler;
import tools.emojis;
import world.Thing;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleSuperMap extends OleSensor implements ActionListener {

    protected HashMap<String, ArrayList<SimpleVector3D>> Trails;
    protected Polygon hudView[][];
    protected int narrow = 37, margin = 22;
    protected Polygon p;
    protected int cell, nLevels, nTiles, trailSize = 0;
    protected boolean showTrail;
    protected Point3D pCenterTopFixed, pVariableDown, pCenterFixed, pVariableTop, pDistance, pHead;
    protected TextFactory tf;
    protected double stepRadius, stepAngle;
    protected AngleTransporter at;
    protected ArrayList<Map2DColor> sprites;
    protected JButton jbAux;
    protected String sDisplay, sFocus;
    protected OleConfig displayCfg;
    protected Map2DColor mapView;
    protected Palette palette;
    protected Color cText, cOutline, cTrail, cGrid;

    public OleSuperMap(OleDrawPane parent, String name) {
        super(parent, name);
        this.setLayout(null);
        Trails = new HashMap();
        isMap = true;
        at = parentPane.getAngleT();
        myDash = ((OleDashBoard) this.parentPane);
        setnRows(1);
        setnColumns(3);
//        sprites = new ArrayList();
//        try {
//            for (int i = 0; i < 8; i++) {
//                sprites.add(new Map2DColor().loadMapRaw(getClass().getResource("/resources/icons/explorer" + i + ".png").toString().replace("file:", "")));
//            }
//        } catch (IOException ex) {
//        }
        hasGrid = false;
        String doptions = "{\"options\": {    \"Objects\": {        \"Objects to display\": {            \"Myself\": true,            \"Cities\": true,            \"Other agents\": true,   \"Trail\": true        }    },    \"Focus\": {        \"Keep focus on\": \"None\"    },    \"Type of map\" :{    		\"Display map\":\"None\"    }},\"properties\": {    \"Keep focus on\": {        \"select\": [\"Myself\", \"None\"]    },    \"Display map\":{\"select\":[\"Heightmap\",\"Real view\",\"Flat\", \"Satellite\"]}}\n"
                + "}";
        displayCfg = new OleConfig();
        displayCfg.set(doptions);
        if (new File("config/displayoptions.json").exists()) {
            displayCfg.loadFile("config/displayoptions.json");
        } else {
            displayCfg.saveAsFile("config/", "displayoptions.json", true);
        }
        palette = new Palette();
        palette.addWayPoint(0, new Color(0, 0, 128));
        palette.addWayPoint(5, new Color(0, 0, 1, 0));
        palette.addWayPoint(10, new Color(0, 160, 0));
        palette.addWayPoint(40, new Color(51, 60, 0));
        palette.addWayPoint(75, new Color(153, 79, 0));
        palette.addWayPoint(100, Color.WHITE);
        palette.fillWayPoints(256);

        this.setScaledCoordinates(false);
    }

    @Override
    public void validate() {
        super.validate();
        int border = 0;
        screenPort = new Rectangle((int) this.getBounds().getX(), (int) this.getBounds().getY(),
                (int) this.getBounds().getWidth(), (int) this.getBounds().getHeight()); //SwingTools.doNarrow(this.getBounds(), margin);
        viewPort = SwingTools.doNarrow(screenPort, 10); //SwingTools.doNarrow(this.getBounds(), narrow);
//        viewPort.y += 16;
//        screenPort.y += 16;
        shiftx = 0;
        shifty = 0;
        odPane = new OleDrawPane() {
            @Override
            public void OleDraw(Graphics2D g) {
                viewSuperSensor(g);
            }
        };
        odPane.setBounds(viewPort);
        odPane.setPreferredSize(SwingTools.RectangleToDimension(viewPort));
//        osPane = new OleScrollPane(odPane);
        osPane = new OleScrollPane(odPane) {
            @Override
            public void Clicked(MouseEvent e) {
                if (e.isControlDown() || e.isShiftDown()) {
                    if (odpPopUp == null) {
                        setPopUpData(osPane.getRealX(e.getX()), osPane.getRealY(e.getY()));
                        odpPopUp = new OleDrawPane() {
                            @Override
                            public void OleDraw(Graphics2D g) {
                                paintPalette(g, palette, this.getBounds());
                            }

                        };
                        odpPopUp.setBounds(this.getBounds().x + this.getBounds().width, 0, 35, this.getBounds().height);
                        myDash.add(odpPopUp, 0);
                        odpPopUp.setVisible(true);
                        myDash.repaint();
                    } else {
                        odpPopUp.setVisible(false);
                        myDash.remove(odpPopUp);
                        odpPopUp = null;
                    }
                } else {
                    super.Clicked(e);
                }
            }
        };
        osPane.setBounds(screenPort);
        osPane.setBackground(Color.BLACK);
        osPane.setPreferredSize(SwingTools.RectangleToDimension(screenPort));
        osPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        osPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        FlowLayout l = new FlowLayout(FlowLayout.LEFT);
        l.setHgap(0);
        l.setVgap(0);

        this.setLayout(l);
        JComboBox jcbDisplay;
        JLabel jl;

//        jcbDisplay = new JComboBox();
//        jcbDisplay.addItem("Me");
//        for (String s : myDash.getMyDecoder().getCapabilities()) {
//            jcbDisplay.addItem(s);
//        }
//        jcbDisplay.addActionListener(this);
//        jl = new JLabel("DSPLY");
//        jl.add(jcbDisplay);
//        jl.setBounds(new Rectangle(this.getBounds().x, this.getBounds().y, 75, 20));
//        this.parentPane.add(jl);
//        this.parentPane.add(jcbDisplay);
        jbAux = new JButton("DISPLAY");
//        jbAux.setPreferredSize(new Dimension(100, 20));
        jbAux.addActionListener(this);
        jbAux.setBounds(new Rectangle(this.getBounds().x, this.getBounds().height, 100, 20));
        jbAux.setMargin(new Insets(0, 0, 0, 0));
//        jbAux.setContentAreaFilled(true);
//        jbAux.setBorderPainted(true);
        jbAux.setFocusPainted(true);
        jbAux.setFont(parentPane.getFont().deriveFont(9.0f));
        this.parentPane.add(jbAux);

        jbAux = new JButton("FOCUS");
//        jbAux.setPreferredSize(new Dimension(100, 20));
        jbAux.addActionListener(this);
        jbAux.setBounds(new Rectangle(this.getBounds().x + 100, this.getBounds().height, 100, 20));
        jbAux.setMargin(new Insets(0, 0, 0, 0));
//        jbAux.setContentAreaFilled(true);
//        jbAux.setBorderPainted(true);
        jbAux.setFocusPainted(true);
        jbAux.setFont(parentPane.getFont().deriveFont(9.0f));
        this.parentPane.add(jbAux);
        jbAux = new JButton("MAPS");
//        jbAux.setPreferredSize(new Dimension(100, 20));
        jbAux.addActionListener(this);
        jbAux.setBounds(new Rectangle(this.getBounds().x + 200, this.getBounds().height, 100, 20));
        jbAux.setMargin(new Insets(0, 0, 0, 0));
//        jbAux.setContentAreaFilled(true);
//        jbAux.setBorderPainted(true);
        jbAux.setFocusPainted(true);
        jbAux.setFont(parentPane.getFont().deriveFont(9.0f));
        this.parentPane.add(jbAux);
//        odPane.validate();
        osPane.validate();
        this.parentPane.add(osPane);
    }

    @Override
    public OleSensor layoutSensor(Graphics2D g) {
//        if (showFrame) {
//            g.setColor(OleDashBoard.cDeck);
//            g.fillRect(mX, mY, mW, mH);
//            g.setColor(OleDashBoard.cFrame);
//            g.fillRoundRect(mX + 3, mY + 3, mW - 6, mH - 6, 10, 10);
//        }
//        g.setClip(viewPort);
        if (map != null) {
//            g.setClip(viewPort);
            if (myDash.getMyDecoder().getWorldMap() != null) {
//                scale = odPane.getPreferredSize().getWidth() / this.myDash.getMyDecoder().getWorldMap().getMap().getWidth();
                scale = osPane.getZoom();
                if (scale < limitScale) {
                    hasGrid = false;
                } else {
                    hasGrid = true;
                }
            }
            Color cbkp = this.getForeground();
            this.setForeground(cGrid);
            setMinValue(0);
            setMaxValue(map.getWidth());
            setMinVisual(0);
            setMaxVisual(odPane.getWidth());
            lengthVisual = getMaxVisual() - getMinVisual();
            stepVisual = lengthVisual / nMarks;
            lengthValue = (maxValue - minValue);
            stepValue = lengthValue / nMarks;
            stepValue2 = 1;
            setnDivisions(10);
            drawLineRuler(g, SwingTools.DimensionToRectangle(odPane.getPreferredSize()), 10);
            this.showTrail = this.displayCfg.getTab("Objects").getOle("Objects to display").getBoolean("Trail", false);
            this.setTrailSize(100);
            this.setForeground(cbkp);
//            g.setClip(null);
        }
        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {
        osPane.repaint();
        return this;
    }

//    public void Purge() {
//        TimeHandler tnow = new TimeHandler();
//        for (String sname : Trails.keySet()) {
//            if (this.myDash.getDecoderOf(sname).getLastRead().elapsedTimeSecsUntil(tnow) > 10) {
//                Trails.remove(sname);
//            }
//        }
//    }
    public OleSensor viewSuperSensor(Graphics2D g) {
//        TimeHandler tnow = new TimeHandler();
        Point3D focus = null;
        layoutSensor(g);
        String label;
        if (mapView != null) {
            if (!displayCfg.getTab("Focus").getString("Keep focus on", "None").equals("None")) {
                if (displayCfg.getTab("Focus").getString("Keep focus on", "None").equals("Myself")) {
                    focus = myDash.getMyDecoder().getGPS();
                }
            } else {
                focus = null;
            }
            // Paint map
            osPane.setFocusOn(focus);
//            RescaleOp darken = new RescaleOp(0.5f, 0, null);
            int nw = (int) (odPane.getPreferredSize().getWidth()),
                    nh = (int) (odPane.getPreferredSize().getHeight());
//            g.drawImage(darken.filter(mapView.getMap(), null), 0, 0, nw, nh, null);
            g.drawImage(mapView.getMap(), 0, 0, nw, nh, null);
            SimpleVector3D ptrail, prevTrail, ptext, ppoint;
            double xVP, yVP;
            Color c;

            // Paint rulers
            Color cbkp = this.getForeground();
            this.setForeground(cGrid);
            drawLineRuler(g, SwingTools.DimensionToRectangle(odPane.getPreferredSize()), 10);
            this.setForeground(cbkp);

            // Paint agents trails
            for (String name : Trails.keySet()) {
//                if (this.myDash.getDecoderOf(name).getLastRead().elapsedTimeSecsUntil(tnow) < 10000) {
                if (this.isShowTrail()) {
                    for (int i = 2; i < Trails.get(name).size(); i++) {
                        ptrail = Trails.get(name).get(i);
                        if (this.getTrailSize() > 25) {
                            c = map.getColor(ptrail);
                            g.setColor(new Color(0, (Trails.get(name).size() - i) * 255 / Trails.get(name).size(), 0));
                        } else {
                            g.setColor(Color.GREEN);
                        }
                        g.fill(this.TraceRegularPolygon(ptrail, 4, 3));
                    }
                }

                if (myDash.getDecoderOf(name).getAlive()) {
                    g.setColor(cTrail);
                } else {
                    g.setColor(Color.RED);
                }
                ptrail = myDash.getDecoderOf(name).getGPSVector();
                prevTrail = myDash.getDecoderOf(name).getGPSVectorMemory(1);
                g.setStroke(new BasicStroke(2));
//                if (myDash.getMyDecoder().getCourse()!=null || scale < limitScale) {
                p = this.TraceRegularPolygon(ptrail, 4, 5);
                g.drawPolygon(p);
                p = this.TraceCourse(ptrail, 15);
                g.drawPolygon(p);
//                } else {
//                    g.drawImage(sprites.get(myDash.getMyDecoder().getCompass() / 45).getMap(),
//                            (int) viewX(ptrail.getSource().getXInt() - 0.5),
//                            (int) viewY(ptrail.getSource().getYInt() - 0.5),
//                            (int) scale, (int) scale, null);
//                }

//                    p = this.TraceRegularStar(ptrail, 6, 30, 15);
//                    p = this.TraceRomboid(ptrail, 5);
                g.setStroke(new BasicStroke(1));
                this.traceLabel(g, ptrail, prevTrail, 25, name, viewPort);
                g.setColor(Color.WHITE);
                g.drawRect(0, 0,
                        (int) (map.getWidth() * scale),
                        (int) (map.getHeight() * scale)
                );
//                }
            }

            // Paint targets
            if (myDash.getMyDecoder().getDestination() != null) {
                Point3D pWaypoint;
                pWaypoint = myDash.getMyDecoder().getDestination();
                g.setColor(OleDashBoard.cAngle);
                g.setStroke(new BasicStroke(2));
                g.drawPolygon(this.TraceRegularPolygon(new SimpleVector3D(pWaypoint, Compass.NORTH), 4, 5));
                g.setStroke(new BasicStroke(1));
                for (int i = 0; i < myDash.getMyDecoder().getCourse().length; i++) {
                    pWaypoint = myDash.getMyDecoder().getCourse()[i];
                    g.drawPolygon(this.TraceRegularPolygon(new SimpleVector3D(pWaypoint, Compass.NORTH), 4, 2));
                }
                pWaypoint = myDash.getMyDecoder().getTarget();
                g.setStroke(new BasicStroke(2));
                g.setColor(Color.CYAN);
                g.drawPolygon(this.TraceRegularPolygon(new SimpleVector3D(pWaypoint, Compass.NORTH), 4, 5));
            }

            // PaintCities
            Color col = Color.WHITE;
            g.setColor(col);
            g.setStroke(new BasicStroke(1));

            for (Thing t : myDash.getMyDecoder().getFullCadastre().getAllThings()) {
                if (displayCfg.getTab("Objects").getOle("Objects to display").getBoolean("Cities", false)
                        && myDash.getMyDecoder().getFullCadastre() != null) {
                    tf = new TextFactory(g);
                    String name = t.getName();
                    g.setColor(col);
                    tf = new TextFactory(g);
                    if (cOutline != null) {
                        tf.setsText(name)
                                .setX(osPane.getPaneX(t.getPosition().getXInt())).setY(osPane.getPaneY(t.getPosition().getYInt()) - 10)
                                .setHalign(SwingConstants.CENTER).setValign(SwingConstants.CENTER)
                                .setForeGround(cText).setShadow(cOutline).setOutline(true)
                                .setFontSize(10).validate();
                    } else {
                        tf.setsText(name)
                                .setX(osPane.getPaneX(t.getPosition().getXInt())).setY(osPane.getPaneY(t.getPosition().getYInt()) - 10)
                                .setHalign(SwingConstants.CENTER).setValign(SwingConstants.CENTER)
                                .setForeGround(cText)
                                .setFontSize(10).validate();
                    }
                    tf.draw();

//                    tf = new TextFactory(g);
//                    tf.setsText(".")
//                            .setX(osPane.getPaneX(t.getPosition().getXInt())).setY(osPane.getPaneY(t.getPosition().getYInt())-5)
//                            .setHalign(SwingConstants.CENTER).setValign(SwingConstants.CENTER)
//                            .setForeGround(cText).setShadow(cOutline).setOutline(true)
//                            .setFontSize(10).validate();
//                    tf.draw();
                }
                g.setColor(cText);
                g.drawPolygon(this.TraceRegularPolygon(new SimpleVector3D(t.getPosition(), Compass.NORTH), 4, 2));
//                if (!t.isHasAirport() && !t.isHasPort()) {
//                    g.drawPolygon(this.TraceRegularPolygon(new SimpleVector3D(t.getPosition(), Compass.NORTH), 4, 2));
//                } else {
//                    if (t.isHasAirport()) {
//                        g.drawPolygon(this.TraceRegularPolygon(new SimpleVector3D(t.getPosition(), Compass.NORTH), 3, 5));
//                    }
//                }

            }
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
//        Rectangle r = SwingTools.doNarrow(this.getBounds(), 6);
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

//    protected void paintCity(Graphics2D g, Thing t) {
//        Point3D p3d = t.getPosition();
//        g.setColor(OleDashBoard.cAngle);
//        g.setStroke(new BasicStroke(1));
//        g.drawPolygon(this.TraceRegularPolygon(new SimpleVector3D(p3d, myDash.getMyDecoder().getCompass() / 45), 6, 3));
//        tf = new TextFactory(g);
//        tf.setsText(t.getName());
//        tf.setX(p3d.getXInt()).setY(p3d.getYInt() - 10)
//                .setHalign(SwingConstants.CENTER).setValign(SwingConstants.CENTER).setOutline(true)
//                .setFontSize(12).validate();
//        tf.draw();
//    }
    protected void paintPeople(Graphics2D g, Thing t) {
        Point3D p3d = t.getPosition();
        g.setColor(OleDashBoard.cDistance);
        g.setStroke(new BasicStroke(1));
        g.drawPolygon(this.TraceRegularPolygon(new SimpleVector3D(p3d, myDash.getMyDecoder().getCompass() / 45), 3, 3));
        tf = new TextFactory(g);
        tf.setsText(t.getName());
        tf.setX(p3d.getXInt()).setY(p3d.getYInt() - 10)
                .setHalign(SwingConstants.CENTER).setValign(SwingConstants.CENTER).setOutline(true)
                .setFontSize(12).validate();
        tf.draw();
    }

    public void traceLabel(Graphics2D g, SimpleVector3D sv, SimpleVector3D prevsv, int length, String name, Rectangle viewPort) {
//        System.out.println("tracelabel");
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

        if (myDash.getDecoderOf(name).getAlive()) {
            if (myDash.getDecoderOf(name).getType().equals("DEST")) {
                g.setColor(Color.RED);
            } else if (myDash.getDecoderOf(name).getType().equals("VAAT")
                    || myDash.getDecoderOf(name).getType().equals("BB1F")
                    || myDash.getDecoderOf(name).getType().equals("YV")) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(cTrail);
            }
        } else {
            g.setColor(Color.RED);
        }
        int fsize = 10;
        s = String.format("%s  - %s", name, myDash.getDecoderOf(name).getType());
        tf = new TextFactory(g);
        tf.setPoint(pLabel).setsText(s).setFontSize(fsize).setTextStyle(Font.BOLD).setHalign(halign).setValign(valign).validate();
        tf.draw();

        if (prevsv != null) {
            SimpleVector3D last = new SimpleVector3D(prevsv.getSource(), sv.getSource());
            if (last.canonical().getTarget().getZ() > 0) {
                climb = emojis.UPRIGHTARROW;//"+";
            } else if (last.canonical().getTarget().getZ() < 0) {
                climb = emojis.DOWNRIGHTARROW; //"-";
            } else {
                climb = emojis.RIGHTARROW; //" ";
            }
        } else {
            climb = emojis.RIGHTARROW; //" ";
        }
        s = String.format("%03d%s%s", (int) sv.getSource().getZInt(), climb, SimpleVector3D.Dir[sv.getsOrient()]);
        tf = new TextFactory(g);
        pLabel.setY(pLabel.getY() + fsize);
        tf.setPoint(pLabel).setsText(s).setsFontName(Font.MONOSPACED).setFontSize(fsize)
                .setTextStyle(Font.PLAIN).setHalign(halign).setValign(valign).validate();
        tf.draw();
        s = String.format("e.%03d%% pl.%02d", myDash.getDecoderOf(name).getEnergy() * 100 / myDash.getDecoderOf(name).getAutonomy(),
                myDash.getDecoderOf(name).getPayload());
        tf = new TextFactory(g);
        pLabel.setY(pLabel.getY() + fsize);
        tf.setPoint(pLabel).setsText(s).setsFontName(Font.MONOSPACED).setFontSize(fsize)
                .setTextStyle(Font.PLAIN).setHalign(halign).setValign(valign).validate();
        tf.draw();
        g.setStroke(new BasicStroke());
        this.oDrawLine(g, viewP(sv.getSource()), pLabel);
        g.setStroke(new BasicStroke(1));
        String goal;
        try {
            goal = myDash.getDecoderOf(name).getSensor(Sensors.CURRENTGOAL).get(0).asString();
        } catch (Exception ex) {
            goal = " XXX";
        }
        s = String.format("%s", goal);
        tf = new TextFactory(g);
        pLabel.setY(pLabel.getY() + fsize);
        tf.setPoint(pLabel).setsText(s).setsFontName(Font.MONOSPACED).setFontSize(fsize)
                .setTextStyle(Font.PLAIN).setHalign(halign).setValign(valign).validate();
        tf.draw();
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

    @Override
    public void actionPerformed(ActionEvent e) {
        OleDialog odlg;
        switch (e.getActionCommand()) {
            case "DISPLAY":
                odlg = new OleDialog(null, "Display Options");
                if (odlg.run(displayCfg, "Objects")) {
                    displayCfg = odlg.getResult();
                    displayCfg.saveAsFile("config/", "displayoptions.json", true);
                    prepareMap();
                }
                break;
            case "FOCUS":
                odlg = new OleDialog(null, "Display Options");
                if (odlg.run(displayCfg, "Focus")) {
                    displayCfg = odlg.getResult();
                    displayCfg.saveAsFile("config/", "displayoptions.json", true);
                    prepareMap();
                }
                break;
            case "MAPS":
                odlg = new OleDialog(null, "Display Options");
                if (odlg.run(displayCfg, "Type of map")) {
                    displayCfg = odlg.getResult();
                    displayCfg.saveAsFile("config/", "displayoptions.json", true);
                    prepareMap();
                }
                break;

        }
    }

    @Override
    public void setMap(Map2DColor map) {
        this.map = map;
        this.odPane.setPreferredSize(new Dimension(map.getWidth(), map.getHeight()));
        osPane.reset(odPane.getPreferredSize());
//        osPane.setZoom(odPane.getWidth() *1.0 / map.getWidth());
        prepareMap();

    }

    protected void prepareMap() {
        switch (displayCfg.getTab("Type of map").getString("Display map", "Heightmap")) {
            default:
            case "Heightmap":
                mapView = new Map2DColor(map.getWidth(), map.getHeight());
                for (int x = 0; x < map.getWidth(); x++) {
                    for (int y = 0; y < map.getHeight(); y++) {
                        mapView.setColor(x, y, map.getColor(x, y));
                    }
                }
                cText = Color.GREEN;
                cOutline = null;
                cTrail = cText;
                cGrid = Color.WHITE;
                break;
            case "Real view":
                mapView = new Map2DColor(map.getWidth(), map.getHeight());
                for (int x = 0; x < map.getWidth(); x++) {
                    for (int y = 0; y < map.getHeight(); y++) {
                        mapView.setColor(x, y, palette.getColor(map.getStepLevel(x, y)));
                    }
                }
                cText = OleDashBoard.cCompass;
                cOutline = Color.BLACK;
                cTrail = Color.GREEN;
                cGrid = Color.DARK_GRAY;
                break;
            case "Satellite":
                mapView = new Map2DColor(map.getWidth(), map.getHeight());
                for (int x = 0; x < map.getWidth(); x++) {
                    for (int y = 0; y < map.getHeight(); y++) {
                        if (map.getStepLevel(x, y) < 5) {
                            mapView.setColor(x, y, OleDashBoard.cSea);
                        } else {
                            mapView.setColor(x, y, OleDashBoard.cGround);
                        }
                    }
                }
                cText = Color.GREEN;
                cOutline = Color.BLACK;
                cTrail = OleDashBoard.cTrack;
                cGrid = Color.DARK_GRAY;
                break;
            case "Flat":
                mapView = new Map2DColor(map.getWidth(), map.getHeight());
                for (int x = 0; x < map.getWidth(); x++) {
                    for (int y = 0; y < map.getHeight(); y++) {
                        if (map.getStepLevel(x, y) < 5) {
                            mapView.setColor(x, y, new Color(204, 229, 255));
                        } else {
                            mapView.setColor(x, y, Color.WHITE);
                        }
                    }
                }
                cText = Color.BLACK;
                cOutline = Color.WHITE;
                cTrail = OleDashBoard.cTrack;
                cGrid = Color.GRAY;
                break;

        }
        this.setForeground(cText);
    }
}
