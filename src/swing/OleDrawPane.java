/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.Consumer;
import javax.swing.JPanel;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public abstract class OleDrawPane extends JPanel {

    protected Graphics2D myg;
    protected Consumer<Graphics2D> drawer;
    public OleDrawPane() {
        super();
        drawer = (e)->OleDraw(e);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        myg = (Graphics2D) g;
        drawer.accept(myg);
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

    public void setOleDraw(Consumer <Graphics2D>draw) {
        drawer = draw;
    }
    abstract public void OleDraw(Graphics2D g);
}
