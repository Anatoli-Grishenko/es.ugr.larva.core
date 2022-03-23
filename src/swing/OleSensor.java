/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import data.Ole;
import geometry.AngleTransporter;
import geometry.Point3D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import map2D.Palette;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public abstract class OleSensor extends JComponent {

    public static enum SensorType {
        LINEAR, MATRIX, BOOLEAN
    };

    public static enum ViewType {
        RAWINT, RAWDOUBLE, LINEARPB, ANGULARPB, HWHEEL, VWHEEL, DIAL, SEMIDIAL, LED
    }

    // Data
    protected double[][] allReadings;
    protected SensorType sType;
    protected int nColumns, nRows, nMarks;
    protected double minValue, maxValue, lengthValue, stepValue;
    protected HashMap<Double, ImageIcon> readingMarks;
    protected ViewType vType;
    protected OleDrawPane matrixViewer;
    protected boolean boolValue = false;
    protected ArrayList<String> bag;
    // View
    protected int mX, mY, mW, mH;
    protected double minVisual, maxVisual;
    protected Point3D center, origin;
    protected OleDrawPane parentPane;
    protected double stepVisual, lengthVisual, baseVisual = 0;
    protected Palette myPalette;
    protected boolean showScale = true, showScaleNumbers = true, showFrame = false,
            rotateText = false, autoRotate = false, counterClock = false;
    protected double mainRadius, markRadius, textRadius, labelRadius, dialRadius, barRadius;
    protected int stroke = 27;
    protected Font f, fRead;
    protected String sRead, sfRead, sfText, labels[];
    protected JTextPane jtBag;
    protected JScrollPane jsPane;

    public OleSensor(OleDrawPane parent, String name) {
        super();

        setName(name);
        parentPane = parent;
        setnRows(1);
        setnColumns(1);
        setCurrentValue(Perceptor.NULLREAD);
        sfRead = "Ubuntu Mono Regular";
        sfText = "Noto Sans Regular";
        bag = new ArrayList();
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
        }
    }

    public int getnRows() {
        return nRows;
    }

    public void setnRows(int nRows) {
        this.nRows = nRows;
        this.allReadings = new double[nRows][];
    }

    public double getCurrentValue() {
        return allReadings[0][0];
    }

    public void setCurrentValue(double currentValue) {
        if (currentValue <= getMinValue()) {
            if (!autoRotate) {
                currentValue = getMinValue();
            } else {
                currentValue = (this.lengthValue + currentValue) % this.lengthValue;
            }

        } else if (currentValue >= getMaxValue()) {
            if (!autoRotate) {
                currentValue = getMaxValue();
            } else {
                currentValue = currentValue % this.lengthValue;
            }

        } else {
            currentValue = currentValue;
        }
        allReadings[0][0] = currentValue;
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

    public void drawCircularRuler(Graphics2D g, Point3D center, double axisRadius, double markRadius1, double markRadius2, double textRadius, int fontSize) {
        Point3D p1, p2, ps;
        double iValue;
        String sValue;
        int textSize;
        if (fontSize >= 0) {
            textSize = fontSize;
        } else {
            textSize = (int) (Math.round(Math.abs(markRadius2 - markRadius1))) * 30 / 20;
        }
        if (showScale) {
            g.setColor(this.getForeground());
            if (axisRadius > 0) {
                this.oDrawArc(g, getCenter(), axisRadius, getMinVisual(), getMaxVisual());
            }
            if (!counterClock) {
                iValue = getMaxValue();
            } else {
                iValue = getMinValue();
            }
            if (this.isAutoRotate()) {
                if (!counterClock) {
                    baseVisual = 90 - getCurrentValue();
                } else {
                    baseVisual = getCurrentValue() - 90;
                }
            }
            for (double alpha = getMinVisual(); alpha < getMaxVisual() + stepVisual; alpha += stepVisual) {
                p1 = parentPane.getAngleT().alphaPoint(alpha + baseVisual, markRadius2, center);
                p2 = parentPane.getAngleT().alphaPoint(alpha + baseVisual, markRadius1, center);
                oDrawLine(g, p1, p2);
            }
        }
        if (showScaleNumbers) {
            g.setColor(this.getForeground());
            double maxScale;
            if (getMinVisual() % 360 == getMaxVisual() % 360) {
                maxScale = getMaxVisual();
            } else {
                maxScale = getMaxVisual() + stepVisual;
            }
            if (!counterClock) {
                iValue = getMaxValue();
            } else {
                iValue = getMinValue();
            }
            if (this.isAutoRotate()) {
                if (!counterClock) {
                    baseVisual = 90 - getCurrentValue();
                } else {
                    baseVisual = getCurrentValue() + 90;
                }
            }
            for (double alpha = getMinVisual(); alpha < maxScale; alpha += stepVisual) {
                ps = parentPane.getAngleT().alphaPoint(alpha + baseVisual, textRadius, center);
                sValue = String.format("%4d", (int) iValue);
                TextFactory tf = new TextFactory(g);
                if (labels == null) {
                    tf.setNdigits(-1).setValue((int) iValue);
                } else {
                    tf.setsText(labels[(int) (this.getnDivisions() * alpha / getMaxVisual())]);
                }
                tf.setPoint(ps).setFontSize(textSize)
                        .setHalign(SwingConstants.CENTER).setValign(SwingConstants.CENTER);
                if (rotateText) {
                    tf.setAngle((int) (90 - alpha - baseVisual));
                }
                tf.validate().draw();
//                iValue -= stepValue;
                if (!counterClock) {
                    iValue -= stepValue;
                } else {
                    iValue += stepValue;
                }
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
//                p1 = parentPane.getAngleT().alphaPoint(alpha, mainRadius, center);
//                p2 = parentPane.getAngleT().alphaPoint(alpha, markRadius, center);
//                oDrawLine(g, p1, p2);
//            }
//        }
//        if (showScaleNumbers) {
//            g.setColor(this.getForeground());
//            iValue = getMaxValue();
//            for (double alpha = getMinVisual(); alpha < getMaxVisual() + stepVisual; alpha += stepVisual) {
//                ps = parentPane.getAngleT().alphaPoint(alpha, textRadius, center);
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
        p1 = parentPane.getAngleT().alphaPoint(alpha1, radius, center);
        for (double alpha = alpha1 + step; alpha <= alpha2; alpha += step) {
            p2 = parentPane.getAngleT().alphaPoint(alpha, radius, center);
            oDrawLine(g, p1, p2);
            p1 = p2;
        }
    }

    public void oDrawArc(Graphics2D g, Point3D center, double radius, double alpha1, double alpha2, Palette p) {
        double step = 1;
        Point3D p1, p2;
        p1 = parentPane.getAngleT().alphaPoint(alpha1 + baseVisual, radius, center);
        for (double alpha = alpha1 + step; alpha <= alpha2; alpha += step) {
            p2 = parentPane.getAngleT().alphaPoint(alpha + baseVisual, radius, center);
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

    public void setBoolValue(boolean boolValue) {
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
            doc.insertString(doc.getLength(),s+"\n", aset);
        } catch (BadLocationException ex) {
            System.err.println("Exception "+ex.toString());
        }
        jtBag.setCaretPosition(doc.getLength());
    }

    public int getBagSize() {
        return this.bag.size();
    }
}
