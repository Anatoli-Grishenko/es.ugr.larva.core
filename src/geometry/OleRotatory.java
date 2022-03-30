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
import swing.OleDrawPane;
import swing.OleSensor;
import swing.TextFactory;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleRotatory extends OleSensor {

    public OleRotatory(OleDrawPane parent, String name) {
        super(parent, name);
        this.baseVisual = 90;
        dialVisual = 0;
        baseValue=90;
        this.autoRotate = true;
        this.counterClock = false;
        circular=true;
        this.showScale=false;
        this.showScaleNumbers=true;
        this.rotateText=true; }

    @Override
    public void validate() {
        super.validate();
        lengthVisual = this.at.getAngularDistance(minVisual, maxVisual);
        stepVisual = lengthVisual / nMarks;
        lengthValue = (maxValue - minValue);
        stepValue = lengthValue / nMarks;
        mainRadius = mW * 0.46;
        markRadius = mW * 0.39;
        textRadius = mW * 0.33;
    }

    @Override

    public OleSensor layoutSensor(Graphics2D g) {
        Point3D p1, p2;
        if (isHidden())
            return this;
        currentVisual=getCurrentValue()+baseValue;
        if (showFrame) {
            g.setColor(Color.GRAY);
            g.fillRect(mX, mY, mW, mH);
            g.setColor(Color.DARK_GRAY);
            g.fillRoundRect(mX + 3, mY + 3, mW - 6, mH - 6, 10, 10);
        }
        if (this.isAutoRotate()) {
            dialVisual = 0+baseValue;
            baseVisual=3*baseValue+currentVisual; //getCurrentValue();
        } else {
            dialVisual = currentVisual;
            baseVisual = 0;
        }
        f=g.getFont();
        //Black background
        g.setColor(this.getBackground());
        this.oFillArc(g, center, mainRadius, 0, 360);
        //Ruler
        g.setColor(this.getForeground());
        this.drawCircularRuler(g, center, mainRadius, mainRadius, markRadius, textRadius, -1);
        g.setColor(this.getForeground());
        // Counter
        f = parentPane.getFont();
        g.setFont(f.deriveFont(Font.BOLD));
        if (getCurrentValue() == Perceptor.NULLREAD) {
            sRead = "----";
        } else {
            sRead = String.format("%04d", (int) getCurrentValue());
        }
        g.setFont(f);
        oDrawCounter(g, sRead, at.alphaPoint(90, 0, center),
                (int) (0.5 * mW), SwingConstants.CENTER, SwingConstants.BOTTOM);
        // Dial
        p1 = at.alphaPoint(dialVisual, mainRadius, center);
        p2 = at.alphaPoint(90, 0, center);
        g.setColor(Color.RED);
        stroke = 3;
        g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        this.oDrawLine(g, p1, p2);
        g.setStroke(new BasicStroke(1));
        
//        stroke=1;
//        g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//        this.oDrawArc(g, getCenter(), mainRadius - stroke / 2, getMinVisual(), getMaxVisual());
//        g.setStroke(new BasicStroke(1));
        g.setColor(this.getForeground());
        oDrawString(g, getName(), at.alphaPoint(270, dialRadius, center),
                parentPane.getFont().getSize(), SwingConstants.CENTER, SwingConstants.TOP);
//
//        g.setFont(f);
//        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//        g.setColor(this.getForeground());
//        oDrawArc(g, center, mainRadius, 0.0, 360);
//        g.setStroke(new BasicStroke(1));
        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {
        if (isHidden())
            return this;
        Point3D p1, p2, p3, p4;
        layoutSensor(g);
        g.setColor(this.getForeground());
        if (getCurrentValue() != Perceptor.NULLREAD) {
//            p1 = at.alphaPoint(this.getStartAngle() - this.getShiftVisual(), labelRadius, center);
//            p3 = at.alphaPoint(this.getStartAngle() - this.getShiftVisual(), dialRadius, center);
//            p2 = at.alphaPoint(this.getStartAngle() - this.getShiftVisual() + 90, dialRadius, center);
//            p4 = at.alphaPoint(this.getStartAngle() - this.getShiftVisual() - 90, dialRadius, center);
//            oFillTrapezoid(g, p1, p2, p3, p4);
        }
        return this;
    }

}
