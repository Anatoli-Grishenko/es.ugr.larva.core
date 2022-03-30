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
public class OleRoundPB extends OleSensor {

    public OleRoundPB(OleDrawPane parent, String name) {
        super(parent, name);
    }

    @Override
    public void validate() {
        super.validate();
        lengthVisual = this.parentPane.getAngleT().getAngularDistance(minVisual, maxVisual);
        stepVisual = lengthVisual / nMarks;
        lengthValue = (maxValue - minValue);
        stepValue = lengthValue / nMarks;
        mainRadius = mW * 0.46;
        markRadius = mW * 0.38;
        textRadius = mW * 0.36;
        labelRadius = mW * 0.31;
        barRadius = mW * 0.25;
        dialRadius = mW * 0.05;
    }

    @Override
    public OleSensor layoutSensor(Graphics2D g) {
        if (isHidden()) {
            return this;
        }

        if (showFrame) {
            g.setColor(Color.GRAY);
            g.fillRect(mX, mY, mW, mH);
            g.setColor(Color.DARK_GRAY);
            g.fillRoundRect(mX + 3, mY + 3, mW - 6, mH - 6, 10, 10);
        }
        g.setColor(this.getBackground());
        this.oFillArc(g, center, mainRadius, 0, 360);
        g.setColor(this.getForeground());
        this.drawCircularRuler(g, center, 0, labelRadius, mainRadius, markRadius, -1);

        g.setColor(Color.DARK_GRAY);
        if (!showScale) {
            this.drawCircularSegment(g, center, mainRadius - stroke / 2, getMinVisual(), getMaxVisual(), stroke - 2, null);
            stroke = 15;
        } else {
            stroke = 15;
//            this.drawCircularSegment(g, center, barRadius, getMinVisual(), getMaxVisual(), stroke - 2, null);
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
        oDrawCounter(g, sRead, parentPane.getAngleT().alphaPoint(90, dialRadius, center),
                (int) (0.5 * mW), SwingConstants.CENTER, SwingConstants.TOP);
//        oDrawString(g, sRead, parentPane.getAngleT().alphaPoint(90, dialRadius, center),
//                parentPane.getFont().getSize(), SwingConstants.CENTER, SwingConstants.TOP);
        g.setFont(f);
        if (alertLimit != Perceptor.NULLREAD) {
            if (this.isAlertValue()) {
                g.setColor(Color.RED);
            } else {
                g.setColor(this.getBackground());
            }
            g.fillArc(mX + 10, mY + 10, 10, 10, 0, 360);
            g.setColor(this.getForeground());
            g.setStroke(new BasicStroke(2));
            g.drawArc(mX + 10, mY + 10, 10, 10, 0, 360);
            g.setStroke(new BasicStroke(1));
        }

        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {
        if (isHidden()) {
            return this;
        }

        layoutSensor(g);

        if (getCurrentValue() != Perceptor.NULLREAD) {
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
