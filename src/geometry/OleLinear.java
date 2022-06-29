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
public class OleLinear extends OleSensor {

    public OleLinear(OleDrawPane parent, String name) {
        super(parent, name);
        this.minValue=0;
        this.maxValue=Integer.MAX_VALUE;
    }

    @Override
    public void validate() {
        super.validate();
    }

    @Override
    public OleSensor layoutSensor(Graphics2D g) {
        if (showFrame) {
            g.setColor(Color.GRAY);
            g.fillRect(mX, mY, mW, mH);
            g.setColor(Color.DARK_GRAY);
            g.fillRoundRect(mX + 3, mY + 3, mW - 6, mH - 6, 10, 10);
        }
        f = g.getFont();
        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {
        layoutSensor(g);

        if (getCurrentValue() != Perceptor.NULLREAD) {
            TextFactory tf;
            Point3D top = new Point3D(this.getBounds().x+this.getBounds().width/2,this.getBounds().y+3);
            String what;
            for (int i = 0; i < getnColumns()+1; i++) {
                if (i==0) {
                    what=this.getName();
                    tf = new TextFactory(g).setsText(what).setFontSize(12).setPoint(top).setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
                    g.setColor(this.getForeground());
                    tf.draw();
                    top.setY(top.getY()+(this.getBounds().height-6)/(this.getnColumns()+2));
                } else {                    
                    tf = new TextFactory(g).setFontSize(12).setValue((int)this.getAllReadings()[0][i-1],4).setPoint(top).setHalign(SwingConstants.CENTER).setValign(SwingConstants.BOTTOM).validate();
                    this.oDrawCounter(g, tf.getsText(),top, 
                        this.getBounds().width-6, SwingConstants.CENTER, SwingConstants.TOP);
                    top.setY(top.getY()+(this.getBounds().height-6)/(this.getnColumns()+2));
                }
            }

        } else {
            g.setColor(OleDashBoard.cBad);
            TextFactory tf = new TextFactory(g);
            tf.setPoint(center).setsFontName(Font.MONOSPACED).setFontSize(64)
                    .setHalign(SwingConstants.CENTER).setValign(SwingConstants.CENTER)
                    .setsText(emojis.WARNING).validate();
            tf.draw();                    
        }
        g.setFont(f);
        return this;
    }


}
