/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import swing.OleApplication;
import swing.OleDrawPane;
import swing.OleSensor;
import swing.SwingTools;
import swing.TextFactory;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleLabels extends OleSensor {

    JButton tbAux;

    public OleLabels(OleDrawPane parent, String name) {
        super(parent, name);
        this.setLayout(null);
        labelSet = new ArrayList();
        textSet = new ArrayList();
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

        int xx = 10, yy = 20, xwidth = 100;
        f = g.getFont();
        String label, txt;
        TextFactory tf;
        for (int i = 0; i < labelSet.size(); i++) {
            label = labelSet.get(i);
            txt = textSet.get(i);
            g.setColor(Color.GRAY);
            tf = new TextFactory(g).setX(xx+i*150).setY(yy).setsText(label)
                    .setHalign(SwingConstants.LEFT).setValign(SwingConstants.CENTER)
                    .setFontSize(14).validate();
            tf.draw();
            g.setColor(Color.WHITE);
            tf = new TextFactory(g).setX(xx+i*150 + 50).setY(yy).setsText(txt)
                    .setHalign(SwingConstants.LEFT).setValign(SwingConstants.CENTER)
                    .setTextStyle(Font.BOLD)
                    .setFontSize(14).validate();
            tf.draw();
        }
        g.setFont(f);
        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {
        layoutSensor(g);
        return this;
    }

}
