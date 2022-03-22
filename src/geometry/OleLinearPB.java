/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
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
public class OleLinearPB extends OleSensor {

    public OleLinearPB(OleDrawPane parent, String name) {
        super(parent, name);
    }

    @Override
    public void validate() {
        super.validate();
        lengthVisual = this.parentPane.getAngleT().getAngularDistance(minVisual, maxVisual);
        stepVisual = lengthVisual / nMarks;
        lengthValue = (maxValue - minValue);
        stepValue = lengthValue / nMarks;
        mainRadius = mW * 0.5;
        markRadius = mW * 0.42;
        textRadius = mW * 0.4;
        labelRadius = mW * 0.35;
        barRadius = mW * 0.29;
        dialRadius = mW * 0.05;
    }

    @Override
    public OleSensor layoutSensor(Graphics2D g) {
  

        g.setColor(this.getBackground());
        this.oFillArc(g, center, mainRadius, 0, 360);
        g.setColor(this.getForeground());
        this.drawCircularRuler(g, center, mainRadius, labelRadius, textRadius, markRadius, -1);

        g.setColor(Color.DARK_GRAY);
        if (!showScale || !showScaleNumbers) {
            this.drawCircularSegment(g, center, mainRadius - stroke / 2, getMinVisual(), getMaxVisual(), stroke - 2,null);
            stroke=15;
        } else {
            stroke=27;
            this.drawCircularSegment(g, center, barRadius, getMinVisual(), getMaxVisual(), stroke - 2, null);
        }

        if (getCurrentValue() == Perceptor.NULLREAD) {
            sRead = "----";
        } else {
            sRead = String.format("%04d", (int) getCurrentValue());
        }
        g.setColor(this.getForeground());
        f = parentPane.getFont();
        g.setFont(f.deriveFont(Font.BOLD));
        oDrawString(g, getName(), parentPane.getAngleT().alphaPoint(270, labelRadius, center),
                parentPane.getFont().getSize(), SwingConstants.CENTER, SwingConstants.TOP);
        oDrawString(g, sRead, parentPane.getAngleT().alphaPoint(90, dialRadius, center),
                parentPane.getFont().getSize(), SwingConstants.CENTER, SwingConstants.TOP);
        g.setFont(f);
        if (showScale) {
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(this.getForeground());
            oDrawArc(g, center, mainRadius, 0.0, 360);
            g.setStroke(new BasicStroke(1));
        }
        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {
        layoutSensor(g);

        if (currentValue != Perceptor.NULLREAD) {
            g.setColor(this.getForeground());
            if (!showScale || !showScaleNumbers) {                
                this.drawCircularSegment(g, center, mainRadius - stroke / 2, getMaxVisual() - this.getShiftVisual(), getMaxVisual(), stroke, myPalette);
            } else {
                this.drawCircularSegment(g, center, barRadius, getMaxVisual() - this.getShiftVisual(), getMaxVisual(), stroke, myPalette);
            }

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
