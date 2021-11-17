/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.Consumer;
import javax.swing.JPanel;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class MyDrawPane extends JPanel {

    Consumer<Graphics2D> painter;
    protected Graphics2D myg;

    public MyDrawPane(Consumer<Graphics2D> function) {
        setPainter(function);
    }

    public void setPainter(Consumer<Graphics2D> function) {
        painter = function;
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        if (painter != null) {
            if (myg == null)
                myg = g2D;
            painter.accept(g2D);
        }
    }
}
