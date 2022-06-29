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
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import javax.swing.SwingConstants;
import map2D.Palette;
import swing.OleDashBoard;
import swing.OleDrawPane;
import swing.OleSensor;
import swing.TextFactory;
import tools.emojis;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleSemiDial extends OleSensor {

    public OleSemiDial(OleDrawPane parent, String name) {
        super(parent, name);
        baseValue = 0;
        baseVisual = 0;
        this.counterClock = false;
        circular = false;
    }

    @Override
    public void validate() {
        super.validate();

        lengthVisual = this.parentPane.getAngleT().getAngularDistance(minVisual, maxVisual);
        stepVisual = lengthVisual / (nMarks);
        lengthValue = (maxValue - minValue);
        stepValue = lengthValue / (nMarks);
        mainRadius = mW * 0.46;
        markRadius = mW * 0.41;
        textRadius = mW * 0.38;
        if (this.simplifiedDial) {
            textRadius *= 0.8;
        }
        labelRadius = mW * 0.35;
        dialRadius = mW * 0.05;
        if (this.myPalette == null && this.simplifiedDial) {
            myPalette = new Palette();
            myPalette.addWayPoint(0, Color.WHITE);
            myPalette.addWayPoint(100, Color.WHITE);
            myPalette.fillWayPoints(255);
        }
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
        if (simplifiedDial) {
            g.setStroke(new BasicStroke(2));
            this.drawSimplifiedCircularRuler(g, center, mainRadius, mainRadius, markRadius, textRadius, -1);
            g.setStroke(new BasicStroke(1));
            if (getCurrentValue() == Perceptor.NULLREAD) {
                sRead = "----";
            } else {
                sRead = String.format("%04d", (int) getCurrentValue());
            }
            g.setColor(this.getForeground());
            f = parentPane.getFont();
            f.deriveFont(12f);
            //g.setFont(f.deriveFont(Font.BOLD));
//            Point3D target = parentPane.getAngleT().alphaPoint(0, labelRadius, center);
//        oDrawString(g, sRead, parentPane.getAngleT().alphaPoint(270, labelRadius, center),
//                parentPane.getFont().getSize(), SwingConstants.CENTER, SwingConstants.BOTTOM);
            oDrawString(g, getName(), parentPane.getAngleT().alphaPoint(-30, mainRadius, center),
                    parentPane.getFont().getSize(), SwingConstants.RIGHT, SwingConstants.TOP);
            oDrawCounter(g, sRead, parentPane.getAngleT().alphaPoint(-10, mainRadius, center),
                    (int) (0.5 * mW), SwingConstants.RIGHT, SwingConstants.TOP);
//            if (alertLimit != Perceptor.NULLREAD) {
//                if (this.isAlertValue()) {
//                    g.setColor(Color.RED);
//                } else {
//                    g.setColor(this.getBackground());
//                }
//                g.fillArc(mX + 10, mY + 10, 10, 10, 0, 360);
//                g.setColor(this.getForeground());
//                g.setStroke(new BasicStroke(2));
//                g.drawArc(mX + 10, mY + 10, 10, 10, 0, 360);
//                g.setStroke(new BasicStroke(1));
//            }
        } else {
            g.setColor(this.getBackground());
            this.oFillArc(g, center, mainRadius, 0, 360);
            if (myPalette != null) {
                if (showScaleNumbers) {
                    stroke = (int) (mainRadius - textRadius);
                } else {
                    stroke = (int) (mainRadius - textRadius) * 2;
                }
                g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                this.oDrawArc(g, getCenter(), mainRadius - stroke / 2, getMinVisual(), getMaxVisual(), myPalette);
                g.setStroke(new BasicStroke(1));
                if (Math.abs(this.getEndAngle() - this.getStartAngle()) <= 180) {
                    g.setColor(Color.WHITE);
                    this.oFillArc(g, center, mainRadius, 181, 359);
                }
            }

            this.drawCircularRuler(g, center, mainRadius, mainRadius, markRadius, textRadius, -1);
            g.setColor(this.getForeground());
            this.oFillArc(g, center, dialRadius, 0, 360);
            if (getCurrentValue() == Perceptor.NULLREAD) {
                sRead = "----";
            } else {
                sRead = String.format("%04d", (int) getCurrentValue());
            }
            g.setColor(this.getForeground());
            f = parentPane.getFont();
            g.setFont(f.deriveFont(Font.BOLD).deriveFont(12f));
            Point3D target = parentPane.getAngleT().alphaPoint(270, labelRadius, center);
//        oDrawString(g, sRead, parentPane.getAngleT().alphaPoint(270, labelRadius, center),
//                parentPane.getFont().getSize(), SwingConstants.CENTER, SwingConstants.BOTTOM);
            oDrawString(g, getName(), parentPane.getAngleT().alphaPoint(270, dialRadius, center),
                    12, SwingConstants.CENTER, SwingConstants.TOP);
            oDrawCounter(g, sRead, parentPane.getAngleT().alphaPoint(270, labelRadius, center),
                    (int) (0.5 * mW), SwingConstants.CENTER, SwingConstants.BOTTOM);

            g.setFont(f);
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(this.getForeground());
            oDrawArc(g, center, mainRadius, 0.0, 360);

        }
        g.setStroke(new BasicStroke(1));
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

        Point3D p1, p2, p3, p4;
        layoutSensor(g);
        g.setColor(this.getForeground());
        if (getCurrentValue() != Perceptor.NULLREAD) {
            p1 = parentPane.getAngleT().alphaPoint(this.getStartAngle() - this.getShiftVisual(), textRadius, center);
            p3 = parentPane.getAngleT().alphaPoint(this.getStartAngle() - this.getShiftVisual(), dialRadius, center);
            p2 = parentPane.getAngleT().alphaPoint(this.getStartAngle() - this.getShiftVisual() + 90, dialRadius, center);
            p4 = parentPane.getAngleT().alphaPoint(this.getStartAngle() - this.getShiftVisual() - 90, dialRadius, center);
            if (simplifiedDial) {
                g.setStroke(new BasicStroke(2));
                this.oDrawLine(g, p1, p3);
                g.setStroke(new BasicStroke(1));
            } else {
                oFillTrapezoid(g, p1, p2, p3, p4);
//            this.oDrawLine(g, p3,p2);
//            this.oDrawLine(g, p2,p4);
//            this.oDrawLine(g, p4,p1);
            }
        } else {
            g.setColor(OleDashBoard.cBad);
            TextFactory tf = new TextFactory(g);
            tf.setPoint(center).setsFontName(Font.MONOSPACED).setFontSize(64)
                    .setHalign(SwingConstants.CENTER).setValign(SwingConstants.CENTER)
                    .setsText(emojis.WARNING).validate();
            tf.draw();
        }
        return this;
    }

}
