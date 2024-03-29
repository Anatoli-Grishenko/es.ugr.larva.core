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
public class OleBag extends OleSensor {

    JButton tbAux;

    public OleBag(OleDrawPane parent, String name) {
        super(parent, name);
        this.setLayout(null);
        jtBag = new JTextPane();
        jtBag.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        jtBag.setFont(new Font("Free Mono Regular", Font.PLAIN, 12));
    }

    @Override
    public void clear() {
        super.clear();
        this.bag.clear();
        jtBag.setText("");
    }

    @Override
    public void validate() {
        jsPane = new JScrollPane(jtBag);
        jtBag.setBackground(Color.BLACK);
        jsPane.setBounds(6, 30, (int) this.getBounds().getWidth() - 12,
                (int) this.getBounds().getHeight() - 35);
        this.add(jsPane);
        parentPane.add(this);
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
        Point3D top = new Point3D(this.getBounds().x + this.getBounds().width / 2, this.getBounds().y + 3);
        g.setColor(Color.WHITE);
        TextFactory tf = new TextFactory(g).setsText(getName()).setFontSize(12).setPoint(top).setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
        tf.draw();
        if (getDescription() != null) {
            g.setColor(Color.WHITE);
            top = new Point3D(this.getBounds().x + this.getBounds().width / 2, this.getBounds().y + 15);
            tf = new TextFactory(g).setsText(getDescription()).setFontSize(12).setPoint(top).setHalign(SwingConstants.CENTER).setValign(SwingConstants.TOP).validate();
            tf.draw();
        }
        return this;
    }

    @Override
    public OleSensor viewSensor(Graphics2D g) {
        layoutSensor(g);
//        jsPane.repaint();
        jtBag.repaint();
        return this;
    }

}
