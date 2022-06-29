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
public class OleDiode extends OleSensor {

    public OleDiode(OleDrawPane parent, String name) {
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
        markRadius = mW * 0.41;
        textRadius = mW * 0.38;
        labelRadius = mW * 0.35;
        dialRadius = mW * 0.05;
    }

    @Override

    public OleSensor layoutSensor(Graphics2D g) {

        if (showFrame) {
            g.setColor(Color.GRAY);
            g.fillRect(mX, mY, mW, mH);
            g.setColor(Color.DARK_GRAY);
            g.fillRoundRect(mX + 3, mY + 3, mW - 6, mH - 6, 10, 10);
        }
        g.setColor(this.getBackground());
        g.fillRect(mX + 3, mY + 3, mW - 6, mH - 6);
        g.setColor(Color.WHITE);
        oDrawString(g, getName(), parentPane.getAngleT().alphaPoint(270,0, center),
                parentPane.getFont().getSize(), SwingConstants.CENTER, SwingConstants.CENTER);
        
//        TextFactory tf;
//        tf = new TextFactory(g).setsText(getName()).setsFontName("Courier New").setTextStyle(Font.PLAIN).setValign(SwingConstants.CENTER).setHalign(SwingConstants.CENTER).
//                setWidth(mW-10).setPoint(center).validate();
//        tf.draw();
        g.setStroke(new BasicStroke(1));
        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {

        Point3D p1, p2, p3, p4;
        layoutSensor(g);
        g.setStroke(new BasicStroke(1));
        if (this.isBoolValue()) {
            g.setColor(this.getForeground());
        } else {
            g.setColor(Color.BLACK);
        }
        g.fillRect(mX + 3, mY + 3, mW - 6, mH - 6);
        if (this.isBoolValue()) {
            g.setColor(Color.BLACK);
        } else {
            g.setColor(Color.DARK_GRAY);
        }
        Font f = g.getFont();
        g.setFont(f.deriveFont(12f));
        oDrawString(g, getName(), parentPane.getAngleT().alphaPoint(270,0, center),
                parentPane.getFont().getSize(), SwingConstants.CENTER, SwingConstants.CENTER);
        g.setFont(f);        return this;
    }

}
