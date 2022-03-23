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
import swing.TextFactory;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleLinear extends OleSensor {

    public OleLinear(OleDrawPane parent, String name) {
        super(parent, name);
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
//        g.setColor(this.getBackground());
//        g.fillRect(mX + 3, mY + 3, mW - 6, mH - 6);
//        g.setStroke(new BasicStroke(1));
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
                    tf = new TextFactory(g).setsText(what).setPoint(top).setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
                    g.setColor(this.getForeground());
                    tf.draw();
                    top.setY(top.getY()+(this.getBounds().height-6)/(this.getnColumns()+2));
                } else {                    
                    tf = new TextFactory(g).setValue((int)this.getAllReadings()[0][i-1],4).setPoint(top).setHalign(SwingConstants.CENTER).setValign(SwingConstants.BOTTOM).validate();
                    this.oDrawCounter(g, tf.getsText(),top, 
                        this.getBounds().width-6, SwingConstants.CENTER, SwingConstants.TOP);
                    top.setY(top.getY()+(this.getBounds().height-6)/(this.getnColumns()+2));
                }
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
