/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public abstract class OleDrawPane extends JPanel {

    protected Graphics2D myg;

    public OleDrawPane() {
        super();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        myg = (Graphics2D) g;
        OleDraw(myg);
    }

    public void clear() {
        System.out.println("OleDrawPane clear");
        if (myg != null) {
            myg.setBackground(getBackground());
            myg.setColor(getBackground());
            myg.fillRect(0, 0, this.getSize().width, this.getSize().height);
            myg.setColor(this.getForeground());
        }
    }

    public Graphics2D canvas() {
        return myg;
    }

    abstract public void OleDraw(Graphics2D g);
}
