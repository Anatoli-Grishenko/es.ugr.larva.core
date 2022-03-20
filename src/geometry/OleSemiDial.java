/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.SwingConstants;
import swing.OleDrawPane;
import swing.OleSensor;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleSemiDial extends OleSensor {

    public OleSemiDial(OleDrawPane parent, String name) {
        super(parent, name);
    }

    @Override
    public void validate() {
        super.validate();
        landMarks = new double[10];
        landMarks[0] = 0;
        landMarks[1] = 0.05;
        landMarks[2] = 0.35;
        landMarks[3] = 0.40;
        landMarks[4] = 0.45;
        landMarks[5] = 0.50;
        lengthVisual = this.parentPane.getAngleT().getAngularDistance(minVisual, maxVisual);
        stepVisual = lengthVisual / nMarks;
        lengthValue = (maxValue - minValue);
        stepValue = lengthValue / nMarks;
    }

    @Override
    public OleSensor layoutSensor(Graphics2D g) {
        g.setColor(this.getForeground());

        g.setColor(Color.BLACK);
        this.oFillArc(g, center, mW * 0.5, 0, 360);
        g.setColor(this.getForeground());
        
        oDrawArc(g, center, mW * 0.5, 0.0, 360);
        this.drawCircularRuler(g, center, mW * landMarks[5], mW * landMarks[4], mW * landMarks[3], -1);
        this.oFillArc(g, center, mW * landMarks[1], 0, 360);
        String sRead;
        if (getCurrentValue() == Perceptor.NULLREAD) {
            sRead = "----";
        } else {
            sRead = String.format("%04d", (int) getCurrentValue());
        }
        oDrawString(g, sRead, parentPane.getAngleT().alphaPoint(270, mW * landMarks[2], center),
                parentPane.getFont().getSize(), SwingConstants.CENTER, SwingConstants.TOP);
        oDrawString(g, getName(), parentPane.getAngleT().alphaPoint(270, mW * landMarks[1], center),
                parentPane.getFont().getSize(), SwingConstants.CENTER, SwingConstants.TOP);
        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {

        Point3D p1, p2, p3,p4;
        layoutSensor(g);
        g.setColor(this.getForeground());
        if (currentValue != Perceptor.NULLREAD) {
            p1 = parentPane.getAngleT().alphaPoint(this.getStartAngle() - this.getShiftVisual(), mW * (landMarks[2]), center);
            p3 = parentPane.getAngleT().alphaPoint(this.getStartAngle() - this.getShiftVisual(), mW * (landMarks[1]), center);
            p2= parentPane.getAngleT().alphaPoint(this.getStartAngle() - this.getShiftVisual()+45, mW * (landMarks[1]), center);
            p4= parentPane.getAngleT().alphaPoint(this.getStartAngle() - this.getShiftVisual()-45, mW * (landMarks[1]), center);
            oFillTrapezoid(g,p1,p2,p3,p4);
//            this.oDrawLine(g, p1, p3);
//            this.oDrawLine(g, p3,p2);
//            this.oDrawLine(g, p2,p4);
//            this.oDrawLine(g, p4,p1);
        }
        return this;
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

}
