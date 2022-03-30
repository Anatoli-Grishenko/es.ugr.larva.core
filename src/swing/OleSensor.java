/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import com.eclipsesource.json.JsonArray;
import geometry.AngleTransporter;
import geometry.Point3D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import map2D.Map2DColor;
import map2D.Palette;
import world.Perceptor;

/**
 *
 * Fvalidate * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public abstract class OleSensor extends JComponent {

    public static enum SensorType {
        LINEAR, MATRIX, BOOLEAN
    };

    public static enum ViewType {
        RAWINT, RAWDOUBLE, LINEARPB, ANGULARPB, HWHEEL, VWHEEL, DIAL, SEMIDIAL, LED
    }

    // Data
    protected double[][] allReadings, Memory1, Memory2, Memory3;
    protected SensorType sType;
    protected int nColumns, nRows, nMarks;
    protected double minValue, maxValue, lengthValue, stepValue, baseValue;
    protected HashMap<Double, ImageIcon> readingMarks;
    protected ViewType vType;
    protected OleDrawPane matrixViewer;
    protected boolean boolValue = false, hidden = false;
    protected ArrayList<String> bag;
    // View
    protected int mX, mY, mW, mH;
    protected double minVisual, maxVisual, stepVisual, lengthVisual, baseVisual = 0, dialVisual, currentVisual;
    protected Point3D center, origin;
    protected OleDrawPane parentPane;
    protected Palette myPalette;
    protected boolean showScale = true, showScaleNumbers = true, showFrame = false,
            rotateText = false, autoRotate = false, counterClock = false, circular = false,
            alertValue = false, alertBelow;
    protected double mainRadius, markRadius, textRadius, labelRadius, dialRadius, barRadius;
    protected int stroke = 27, alertLimit = Perceptor.NULLREAD;
    protected Font f, fRead;
    protected String sRead, sfRead, sfText, labels[], externalSensorName;
    protected JTextPane jtBag;
    protected JScrollPane jsPane;
    protected Map2DColor map, image1, image2, image3;
    protected double scale, shiftx, shifty;
    protected Rectangle screenPort, viewPort;
    protected boolean isMap;
    protected AngleTransporter at;
    protected ArrayList<String> labelSet, textSet;
    public JsonArray jsaGoals = new JsonArray();

    public OleSensor(OleDrawPane parent, String name) {
        super();

        setName(name);
        this.attachToExternalSensor(name.toLowerCase());
        parentPane = parent;
        setnRows(1);
        setnColumns(1);
        setCurrentValue(Perceptor.NULLREAD);
        sfRead = "Ubuntu Mono Regular";
        sfText = "Noto Sans Regular";
        bag = new ArrayList();
        baseValue = 0;
        at = parentPane.getAngleT();
    }

    @Override
    public void validate() {
        super.validate();

        mX = this.getBounds().x;
        mY = this.getBounds().y;
        mW = this.getBounds().width;
        mH = this.getBounds().height;
        origin = new Point3D(mX, mY);
        center = new Point3D(mX + mW / 2, mY + mH / 2);
    }

    public abstract OleSensor layoutSensor(Graphics2D g);

    public abstract OleSensor viewSensor(Graphics2D g);

    public double[][] getAllReadings() {
        return allReadings;
    }

    public void setAllReadings(double[][] allReadings) {
        this.allReadings = allReadings;
    }

    public SensorType getsType() {
        return sType;
    }

    public void setsType(SensorType sType) {
        this.sType = sType;
    }

    public int getnColumns() {
        return nColumns;
    }

    public void setnColumns(int nColumns) {
        this.nColumns = nColumns;
        for (int i = 0; i < getnRows(); i++) {
            allReadings[i] = new double[nColumns];
            Memory1[i] = new double[nColumns];
            Memory2[i] = new double[nColumns];
            Memory3[i] = new double[nColumns];
        }
    }

    public int getnRows() {
        return nRows;
    }

    public void setnRows(int nRows) {
        this.nRows = nRows;
        this.allReadings = new double[nRows][];
        this.Memory1 = new double[nRows][];
        this.Memory2 = new double[nRows][];
        this.Memory3 = new double[nRows][];
    }

    public double getCurrentValue() {
        return allReadings[0][0];
    }

    public void setCurrentValue(double currentValue[]) {
        for (int i = 0; i < currentValue.length; i++) {
            if (allReadings[0].length > i) {
                allReadings[0][i] = currentValue[i];
            }
        }
    }

    public void setCurrentValue(double currentValue[][]) {
        for (int j = 0; j < currentValue.length; j++) {
            for (int i = 0; i < currentValue[j].length; i++) {
                if (allReadings[0].length > i && allReadings.length > j) {
                    allReadings[j][i] = currentValue[j][i];
                }
            }
        }
    }

    public double validateValue(double value) {
        if (value <= minValue) {
            if (circular) {
                while (value < minValue) {
                    value += lengthValue;
                }
            } else {
                value = minValue;
            }
        }
        if (value >= maxValue) {
            if (circular) {
                while (value >= maxValue) {
                    value -= lengthValue;
                }
            } else {
                value = maxValue;
            }
        }
        return value;
    }

    public void setCurrentValue(double currentValue) {
        allReadings[0][0] = validateValue(currentValue);
        if (this.alertBelow) {
            if (getCurrentValue() < alertLimit) {
                this.setAlertValue((true));
            } else {
                this.setAlertValue((false));
            }
        }
        if (!this.alertBelow) {
            if (getCurrentValue() > alertLimit) {
                this.setAlertValue((true));
            } else {
                this.setAlertValue((false));
            }
        }

//        System.out.println("Settig value to "+currentValue);
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public int getnMarks() {
        return nMarks;
    }

    public void setnMarks(int nMarks) {
        this.nMarks = nMarks;
    }

    public HashMap<Double, ImageIcon> getReadingMarks() {
        return readingMarks;
    }

    public void setReadingMarks(HashMap<Double, ImageIcon> readingMarks) {
        this.readingMarks = readingMarks;
    }

    public ViewType getvType() {
        return vType;
    }

    public void setvType(ViewType vType) {
        this.vType = vType;
    }

    public OleDrawPane getMatrixViewer() {
        return matrixViewer;
    }

    public void setMatrixViewer(OleDrawPane matrixViewer) {
        this.matrixViewer = matrixViewer;
    }

    public double getMinVisual() {
        return minVisual;
    }

    public void setMinVisual(double minVisual) {
        this.minVisual = minVisual;
    }

    public double getMaxVisual() {
        return maxVisual;
    }

    public void setMaxVisual(double maxVisual) {
        this.maxVisual = maxVisual;
    }

    public Point3D getCenter() {
        return center;
    }

    public void setCenter(Point3D center) {
        this.center = center;
    }

    public double getShiftValue() {
        double d = (this.getCurrentValue() - this.getMinValue()) / this.getMaxValue();;
//        System.out.println("Shift value "+d);
        return d;
    }

    public double getShiftVisual() {
        double d = getShiftValue() * this.lengthVisual;
//        System.out.println("Shift visual "+d);
        return d;
    }

    public void drawLineRuler(Graphics2D g, Rectangle vPort, int fontSize) {
        Point3D p1, p2, p3, p4;
        TextFactory tf;
        int mark = 5;

        g.setColor(Color.WHITE);
        p1 = new Point3D(vPort.x, vPort.y);
        p2 = new Point3D(vPort.x + vPort.width, vPort.y);
        p3 = new Point3D(vPort.x, vPort.y + vPort.height);
        p4 = new Point3D(vPort.x + vPort.width, vPort.y + vPort.height);
        this.oDrawLine(g, p1, p2);
        this.oDrawLine(g, p3, p4);
        for (double alpha = getMinValue(); alpha < getMaxValue() + stepValue; alpha += stepValue) {
            p1 = new Point3D(shiftx + vPort.x + alpha * scale, vPort.y);
            p2 = new Point3D(shiftx + vPort.x + alpha * scale, vPort.y + mark);
            p3 = new Point3D(shiftx + vPort.x + alpha * scale, vPort.y + vPort.height);
            p4 = new Point3D(shiftx + vPort.x + alpha * scale, vPort.y + vPort.height - mark);
            oDrawLine(g, p1, p2);
            oDrawLine(g, p3, p4);
            if (alpha > getMinValue() && alpha < getMaxValue()) {
                tf = new TextFactory(g);
                tf.setPoint(p2).setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).setFontSize(10);
                tf.setValue((int) alpha, 3);
                tf.draw();
                tf = new TextFactory(g);
                tf.setPoint(p4).setHalign(SwingConstants.CENTER).setValign(SwingConstants.BOTTOM).setFontSize(10);
                tf.setValue((int) alpha, 3);
                tf.draw();
            }
        }
        p1 = new Point3D(vPort.x, vPort.y);
        p2 = new Point3D(vPort.x, vPort.y + vPort.height);
        p3 = new Point3D(vPort.x + vPort.width, vPort.y);
        p4 = new Point3D(vPort.x + vPort.width, vPort.y + vPort.height);
        this.oDrawLine(g, p1, p2);
        this.oDrawLine(g, p3, p4);
        for (double alpha = getMinValue(); alpha < getMaxValue() + stepValue; alpha += stepValue) {
            p1 = new Point3D(vPort.x, shifty + vPort.y + alpha * scale);
            p2 = new Point3D(vPort.x + mark, shifty + vPort.y + alpha * scale);
            p3 = new Point3D(vPort.x + vPort.width, shifty + vPort.y + alpha * scale);
            p4 = new Point3D(vPort.x + vPort.width - mark, shifty + vPort.y + alpha * scale);
            oDrawLine(g, p1, p2);
            oDrawLine(g, p3, p4);
            if (alpha > getMinValue() && alpha < getMaxValue()) {
                tf = new TextFactory(g);
                tf.setPoint(p2).setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).setFontSize(10);
                tf.setValue((int) alpha, 3).setAngle(-90);
                tf.draw();
                tf = new TextFactory(g);
                tf.setPoint(p4).setHalign(SwingConstants.RIGHT).setValign(SwingConstants.BOTTOM).setFontSize(10);
                tf.setValue((int) alpha, 3).setAngle(-90);
                tf.draw();
            }
        }
    }

    public void drawCircularRuler(Graphics2D g, Point3D center, double axisRadius, double markRadius1, double markRadius2, double textRadius, int fontSize) {
        Point3D p1, p2, ps;
        String sValue;
        int textSize;
        double endScale, initScale = 0, initValue = 0, endValue = 0, stepScale = 0, iScale = 0, iVisual = 0, maxMarks = 0;

        g.setColor(this.getForeground());
        oDrawArc(g, center, axisRadius, minVisual, maxVisual);
        if (fontSize >= 0) {
            textSize = fontSize;
        } else {
            textSize = (int) (Math.round(Math.abs(markRadius2 - markRadius1))) * 30 / 20;
        }
        if (circular) {
            initValue = initScale = getMinValue();
            endValue = endScale = getMaxValue();
            stepScale = stepValue;
            maxMarks = nMarks;
        } else {
            maxMarks = nMarks + 1;
            if (counterClock) {
                initScale = getMinVisual();
                initValue = getMaxValue();
                stepScale = stepVisual;
                stepValue = -stepValue;
            } else {
                initScale = getMaxVisual();
                initValue = getMinValue();
                stepScale = -stepVisual;
                stepValue = stepValue;
            }

        }
//        if (getMinVisual() % 360 == getMaxVisual() % 360) {
//            maxScale += stepScale;
//        }
        g.setColor(this.getForeground());

        for (int mark = 0; mark < maxMarks; mark++) {
            if (showScale) {
//                if (axisRadius > 0) {
//                    this.oDrawArc(g, getCenter(), axisRadius, getMinVisual(), getMaxVisual());
//                }
                p1 = at.alphaPoint(initScale + mark * stepScale, markRadius2, center);
                p2 = at.alphaPoint(initScale + mark * stepScale, markRadius1, center);
                oDrawLine(g, p1, p2);
            }
            if (showScaleNumbers) {
                if (!showScale) {
                    textRadius = markRadius2;
                }
                iScale = (initValue + mark * stepValue);
                iVisual = (initScale + mark * stepScale + baseValue - baseVisual);
//                System.out.println(iVisual);
                ps = at.alphaPoint(iVisual, textRadius, center);
                sValue = String.format("%4d", (int) iScale);
                TextFactory tf = new TextFactory(g);
                if (labels == null) {
                    tf.setsText(sValue);
                } else {
                    tf.setsText(labels[mark]);
                }
                tf.setPoint(ps).setFontSize(textSize)
                        .setHalign(SwingConstants.CENTER).setValign(SwingConstants.CENTER);
                if (rotateText) {
                    tf.setAngle((int) validateValue(-iVisual + baseValue));
                }
                g.setColor(this.getForeground());
                tf.validate().draw();
//                iValue -= stepValue;
//                if (!counterClock) {
//                    iValue -= stepValue;
//                } else {
//                    iValue += stepValue;
//                }
            }
        }
    }

//    public void drawCircularRuler(Graphics2D g, Point3D center, double mainRadius, double markRadius, double textRadius, int fontSize) {
//        Point3D p1, p2, ps;
//        double iValue;
//        String sValue;
//        int textSize;
//        if (fontSize >= 0) {
//            textSize = fontSize;
//        } else {
//            textSize = (int) (Math.round(Math.abs(mainRadius - markRadius))) * 30 / 20;
//        }
//        if (showScale) {
//            g.setColor(this.getForeground());
//            this.oDrawArc(g, getCenter(), mainRadius, getMinVisual(), getMaxVisual());
//            iValue = getMaxValue();
//            for (double alpha = getMinVisual(); alpha < getMaxVisual() + stepVisual; alpha += stepVisual) {
//                p1 = at.alphaPoint(alpha, mainRadius, center);
//                p2 = at.alphaPoint(alpha, markRadius, center);
//                oDrawLine(g, p1, p2);
//            }
//        }
//        if (showScaleNumbers) {
//            g.setColor(this.getForeground());
//            iValue = getMaxValue();
//            for (double alpha = getMinVisual(); alpha < getMaxVisual() + stepVisual; alpha += stepVisual) {
//                ps = at.alphaPoint(alpha, textRadius, center);
//                sValue = String.format("%4d", (int) iValue);
//                oDrawString(g, sValue, ps, textSize, SwingConstants.CENTER, SwingConstants.CENTER); //SwingConstants.CENTER, SwingConstants.CENTER);
//                iValue -= stepValue;
//            }
//        }
//    }
//
    public void drawCircularSegment(Graphics2D g, Point3D center, double radius1, double alpha1, double alpha2, float stroke, Palette p) {

        if (p == null) {
            g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            this.oDrawArc(g, getCenter(), radius1, alpha1, alpha2);
            g.setStroke(new BasicStroke(1));
        } else {
            g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(p.getColor((int) ((this.getMaxVisual() - alpha1) * this.getMaxValue() / (this.getMaxVisual() - this.getMinVisual()))));
//            g.setColor(p.getColor((int) ((alpha1-this.getMinVisual()) * this.getMaxValue() / (this.getMaxVisual() - this.getMinVisual()))));
            this.oDrawArc(g, getCenter(), radius1, alpha1, alpha2);
            g.setStroke(new BasicStroke(1));

        }
    }

    public void oDrawLine(Graphics2D g, Point3D p1, Point3D p2) {
        g.drawLine(p1.getXInt(), p1.getYInt(), p2.getXInt(), p2.getYInt());
    }

    public void oDrawString(Graphics2D g, String s, Point3D p, int fontSize, int hAlign, int vAlign) {
        int x, y, fSize, fWidth, fVAlign, fHAlign;
        Font f = g.getFont();
        FontMetrics metrics;
        if (fontSize < 0) {
            fSize = f.getSize();
        } else {
            fSize = fontSize;
            f = f.deriveFont((float) (1.0 * fSize));
            g.setFont(f);
        }
        metrics = g.getFontMetrics(f);
        if (hAlign < 0) {
            fHAlign = SwingConstants.LEFT;
        } else {
            fHAlign = hAlign;
        }
        if (vAlign < 0) {
            fVAlign = SwingConstants.TOP;
        } else {
            fVAlign = vAlign;
        }
        fWidth = metrics.stringWidth(s);
        x = p.getXInt();
        y = p.getYInt(); // invertY
        if (fHAlign == SwingConstants.RIGHT) {
            x -= fWidth;
        } else if (fHAlign == SwingConstants.CENTER) {
            x -= fWidth / 2;
        }
        if (fVAlign == SwingConstants.TOP) {
            y += fSize;
        } else if (fVAlign == SwingConstants.CENTER) {
            y += fSize / 2;
        }
        g.drawString(s, x, y);
    }

    public void oDrawText(Graphics2D g, String s, Point3D p, int width, int halign, int valign) {
        TextFactory tf = new TextFactory(g).setsText(sRead).setValign(valign).
                setHalign(halign).setX(p.getXInt()).setY(p.getYInt())
                .setWidth(width).validate();
        Rectangle r = tf.getBounds();
        Rectangle r2 = new Rectangle((int) r.getX() - 2, (int) r.getY() - 2,
                (int) r.getWidth() + 4, (int) r.getHeight() + 4);
        g.setColor(Color.BLACK);
        g.fill(r2);
        g.setColor(Color.WHITE);
        tf.draw();
    }

    public void oDrawCounter(Graphics2D g, String s, Point3D p, int width, int halign, int valign) {
        TextFactory tf = new TextFactory(g).setsText(s).setsFontName("Ubuntu Mono Regular").setTextStyle(Font.PLAIN)
                .setValign(valign).setHalign(halign).
                setX(p.getXInt()).setY(p.getYInt())
                .setWidth(width).validate();
        Rectangle r = tf.getBounds();
        Rectangle r2 = new Rectangle((int) r.getX() - 2, (int) r.getY() - 2,
                (int) r.getWidth() + 4, (int) r.getHeight() + 4);
        g.setColor(Color.BLACK);
        g.fill(r2);
        g.setColor(Color.WHITE);
        g.draw(r2);
        tf.draw();
    }

    public void oDrawArc(Graphics2D g, Point3D center, double radius, double alpha1, double alpha2) {
        double step = 1;
        Point3D p1, p2;
        p1 = at.alphaPoint(alpha1, radius, center);
        for (double alpha = alpha1 + step; alpha <= alpha2; alpha += step) {
            p2 = at.alphaPoint(alpha, radius, center);
            oDrawLine(g, p1, p2);
            p1 = p2;
        }
    }

    public void oDrawArc(Graphics2D g, Point3D center, double radius, double alpha1, double alpha2, Palette p) {
        double step = 1;
        Point3D p1, p2;
        p1 = at.alphaPoint(alpha1 + baseVisual, radius, center);
        for (double alpha = alpha1 + step; alpha <= alpha2; alpha += step) {
            p2 = at.alphaPoint(alpha + baseVisual, radius, center);
            g.setColor(p.getColor((int) ((this.getMaxVisual() - alpha) * this.getMaxValue() / (this.getMaxVisual() - this.getMinVisual()))));
            oDrawLine(g, p1, p2);
            p1 = p2;
        }
    }

    public void oFillArc(Graphics2D g, Point3D center, double radius, double alpha1, double alpha2) {
        g.fillArc((int) (center.getX() - radius), (int) (center.getY() - radius), (int) (radius * 2), (int) (radius * 2), (int) alpha1, (int) (alpha2 - alpha1));
    }

    public void oFillTrapezoid(Graphics2D g, Point3D p1, Point3D p2, Point3D p3, Point3D p4) {
        Polygon p = new Polygon();
        p.addPoint(p1.getXInt(), p1.getYInt());
        p.addPoint(p2.getXInt(), p2.getYInt());
        p.addPoint(p3.getXInt(), p3.getYInt());
        p.addPoint(p4.getXInt(), p4.getYInt());
        p.addPoint(p1.getXInt(), p1.getYInt());
        g.fillPolygon(p);
    }

    public void setPalette(Palette p) {
        myPalette = p;
    }

    public void showScale(boolean scale) {
        this.showScale = scale;
    }

    public void showScaleNumbers(boolean scalenumbers) {
        this.showScaleNumbers = scalenumbers;
    }

    public void showFrame(boolean frame) {
        this.showFrame = frame;
    }

    public int getStartAngle() {
        return (int) getMaxVisual();
    }

    public void setStartAngle(int startAngle) {
        this.setMaxVisual(startAngle);
    }

    public int getEndAngle() {
        return (int) getMinVisual();
    }

    public void setEndAngle(int endAngle) {
        setMinVisual(endAngle);
    }

    public int getnDivisions() {
        return getnMarks();
    }

    public void setnDivisions(int nDvisions) {
        setnMarks(nDvisions);
    }

    public boolean isAutoRotate() {
        return autoRotate;
    }

    public void setAutoRotate(boolean autoRotate) {
        this.autoRotate = autoRotate;
    }

    public void setCounterClock(boolean c) {
        counterClock = c;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public boolean isBoolValue() {
        return boolValue;
    }

    public void setCurrentValue(boolean boolValue) {
        this.boolValue = boolValue;
    }

    public ArrayList<String> getBag() {
        return this.bag;
    }

    public void addToBag(String s) {
        bag.add(s);
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset1 = sc.addAttribute(SimpleAttributeSet.EMPTY,
                StyleConstants.Foreground, this.getForeground()), aset2 = sc.addAttribute(SimpleAttributeSet.EMPTY,
                StyleConstants.Family, "Tlwg Mono Regular");
        AttributeSet aset = sc.addAttributes(aset1, aset2);
        StyledDocument doc = jtBag.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), s + "\n", aset);
        } catch (BadLocationException ex) {
            System.err.println("Exception " + ex.toString());
        }
        jtBag.setCaretPosition(doc.getLength());
    }

    public int getBagSize() {
        return this.bag.size();
    }

    public void attachToExternalSensor(String externalSensor) {
        this.externalSensorName = externalSensor;
    }

    public String getExternalSensor() {
        return this.externalSensorName;
    }

    public Map2DColor getMap() {
        return map;
    }

    public void setMap(Map2DColor map) {
        this.map = map;
    }

    public boolean isMap() {
        return isMap;
    }

    public void setIsMap(boolean b) {
        isMap = b;
    }

    public double[][] getMemory1() {
        return Memory1;
    }

    public void setMemory1(double[][] Memory1) {
        this.Memory1 = Memory1;
    }

    public double[][] getMemory2() {
        return Memory2;
    }

    public void setMemory2(double[][] Memory2) {
        this.Memory2 = Memory2;
    }

    public double[][] getMemory3() {
        return Memory3;
    }

    public void setMemory3(double[][] Memory3) {
        this.Memory3 = Memory3;
    }

    public Map2DColor getImage1() {
        return image1;
    }

    public void setImage1(Map2DColor image1) {
        this.image1 = image1;
    }

    public Map2DColor getImage2() {
        return image2;
    }

    public void setImage2(Map2DColor image2) {
        this.image2 = image2;
    }

    public Map2DColor getImage3() {
        return image3;
    }

    public void setImage3(Map2DColor image3) {
        this.image3 = image3;
    }

    public void addLabel(String label, String value) {
        if (labelSet != null) {
            labelSet.add(label.substring(0, Math.min(12, label.length())));
            textSet.add(value.substring(0, Math.min(20, value.length())));
        }
    }

    public boolean containsLabel(String label) {
        return labelSet.contains(label);
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isAlertValue() {
        return alertValue;
    }

    public void setAlertValue(boolean alertValue) {
        this.alertValue = alertValue;
    }

    public void setAlertLimitBelow(int value) {
        this.alertBelow = true;
        alertLimit = value;
    }

    public void setAlertLimitAbove(int value) {
        this.alertBelow = false;
        alertLimit = value;
    }

    public JsonArray getJsaGoals() {
        return jsaGoals;
    }

    public void setJsaGoals(JsonArray jsaGoals) {
        this.jsaGoals = jsaGoals;
    }

}
