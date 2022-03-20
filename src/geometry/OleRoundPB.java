/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.SwingConstants;
import swing.OleApplication;
import swing.OleDrawPane;
import swing.OleSensor;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleRoundPB extends OleSensor {

    public OleRoundPB(OleDrawPane parent, String name) {
        super(parent, name);
    }

    @Override
    public void validate() {
        super.validate();
        landMarks = new double[10];
        landMarks[0] = 0;
        landMarks[1] = 0.29;
        landMarks[2] = 0.35;
        landMarks[3] = 0.40;
        landMarks[4] = 0.42;
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
        this.drawCircularRuler(g, center, mW * landMarks[2], mW * landMarks[3], mW * landMarks[4], parentPane.getFont().getSize());
        g.setColor(this.getForeground());
        this.drawCircularSegment(g, center, mW * landMarks[1], getMinVisual(), getMaxVisual(), 23);
        g.setColor(this.getForeground());
        String sRead;
        if (getCurrentValue() == Perceptor.NULLREAD) {
            sRead = "----";
        } else {
            sRead = String.format("%04d", (int) getCurrentValue());
        }
        oDrawString(g, sRead, parentPane.getAngleT().alphaPoint(270, mW * 0.15, center),
                parentPane.getFont().getSize(), SwingConstants.CENTER, SwingConstants.TOP);

        oDrawString(g, getName(), parentPane.getAngleT().alphaPoint(270, mW * 0.05, center),
                parentPane.getFont().getSize(), SwingConstants.CENTER, SwingConstants.CENTER);
        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {

        layoutSensor(g);

        if (currentValue != Perceptor.NULLREAD) {
            g.setColor(Color.BLUE);
//            this.drawCircularSegment(g, center, mW * landMarks[2], getMinVisual(), getMaxVisual());
            this.drawCircularSegment(g, center, mW * landMarks[1], getMaxVisual() - this.getShiftVisual(), getMaxVisual(), 25);

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
