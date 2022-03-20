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
import java.awt.RenderingHints;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
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
    protected double currentValue, minValue, maxValue, lengthValue, stepValue;
    protected HashMap<Double, ImageIcon> readingMarks;
    protected ViewType vType;
    protected OleDrawPane matrixViewer;

    // View
    protected int mX, mY, mW, mH;
    protected double minVisual, maxVisual;
    protected double landMarks[];
    protected Point3D center, origin;
    protected OleDrawPane parentPane;
    protected double stepVisual, lengthVisual;

    public OleSensor(OleDrawPane parent, String name) {
        super();

        setName(name);
        parentPane = parent;
        this.currentValue = Perceptor.NULLREAD;
    }

    @Override
    public void validate() {
        super.validate();

        mX = this.getBounds().x;
        mY = this.getBounds().y;
        mW = this.getBounds().width;
        mH = this.getBounds().height;
        origin= new Point3D(mX,mY);
        center = new Point3D(mX+mW / 2, mY+mH / 2);
    }


//    public double invertY(double y) {
//        return mH - y;
//    }

//    @Override
//    protected void paintComponent(Graphics g) {
//        myG = (Graphics2D) g;
//        super.paintComponent(g);
////        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        validate();
//        viewSensor();
//    }
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
    }

    public int getnRows() {
        return nRows;
    }

    public void setnRows(int nRows) {
        this.nRows = nRows;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        if (currentValue < getMinValue()) {
            this.currentValue = getMinValue();
        } else if (currentValue > getMaxValue()) {
            this.currentValue = getMaxValue();
        } else {
            this.currentValue = currentValue;
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

    public void drawCircularRuler(Graphics2D g,Point3D center, double mainRadius, double markRadius, double textRadius, int fontSize) {
        Point3D p1, p2, ps;
        double iValue;
        String sValue;

//        validate();
        int textSize;
        if (fontSize>=0)
            textSize= fontSize;
        else
            textSize= (int) (Math.round(Math.abs(mainRadius - markRadius)))*30/20;
        this.oDrawArc(g, getCenter(), mainRadius, getMinVisual(), getMaxVisual());
        iValue = getMaxValue();
        for (double alpha = getMinVisual(); alpha < getMaxVisual() + stepVisual; alpha += stepVisual) {
            p1 = parentPane.getAngleT().alphaPoint(alpha, mainRadius, center);
            p2 = parentPane.getAngleT().alphaPoint(alpha, markRadius, center);
            oDrawLine(g, p1, p2);
            ps = parentPane.getAngleT().alphaPoint(alpha, textRadius, center);
            sValue = String.format("%4d", (int) iValue);
            oDrawString(g, sValue, ps, textSize, SwingConstants.CENTER, SwingConstants.CENTER); //SwingConstants.CENTER, SwingConstants.CENTER);
            iValue -= stepValue;
        }
    }

    public void drawCircularSegment(Graphics2D g,Point3D center, double radius1,double alpha1,double alpha2, float stroke) {
        Point3D p1, p2, ps;
        double iValue;
        String sValue;

//        validate();
        g.setStroke(new BasicStroke(stroke,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        this.oDrawArc(g, getCenter(), radius1, alpha1,alpha2);
//        iValue = getMaxValue();
//        for (double alpha = getMinVisual(); alpha < getMaxVisual() + stepVisual; alpha += 1) {
//            p1 = parentPane.getAngleT().alphaPoint(alpha, radius1, center);
//            p2 = parentPane.getAngleT().alphaPoint(alpha, radius2, center);
//            oDrawLine(g, p1, p2);            
//            iValue -= stepValue;
//        }
        g.setStroke(new BasicStroke(1));
    }

//    protected int invertY(int y) {
//        return (int) (this.getSize().getHeight() - y);
//    }

    public void oDrawLine(Graphics2D g,Point3D p1, Point3D p2) {
        g.drawLine(p1.getXInt(), p1.getYInt(), p2.getXInt(), p2.getYInt());
    }

    public void oDrawString(Graphics2D g,String s, Point3D p, int fontSize, int hAlign, int vAlign) {
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

    public void oDrawArc(Graphics2D g,Point3D center, double radius, double alpha1, double alpha2) {
        double step = 1;
        Point3D p1, p2;
        p1 = parentPane.getAngleT().alphaPoint(alpha1, radius, center);
        for (double alpha = alpha1 + step; alpha <= alpha2; alpha += step) {
            p2 = parentPane.getAngleT().alphaPoint(alpha, radius, center);
            oDrawLine(g, p1, p2);
//            System.out.println(center.toString()+" "+p1+" " +p2);
            p1 = p2;
        }
    }

    public void oFillArc(Graphics2D g,Point3D center, double radius, double alpha1, double alpha2) {
        g.fillArc((int) (center.getX() - radius), (int) (center.getY() - radius), (int) (radius * 2), (int) (radius * 2), (int) alpha1, (int) (alpha2 - alpha1));
    }
    
    public void oFillTrapezoid(Graphics2D g,Point3D p1,Point3D p2,Point3D p3,Point3D p4) {
        Polygon p = new Polygon();
        p.addPoint(p1.getXInt(), p1.getYInt());
        p.addPoint(p2.getXInt(), p2.getYInt());
        p.addPoint(p3.getXInt(), p3.getYInt());
        p.addPoint(p4.getXInt(), p4.getYInt());
        p.addPoint(p1.getXInt(), p1.getYInt());
        g.fillPolygon(p);
    }
}
