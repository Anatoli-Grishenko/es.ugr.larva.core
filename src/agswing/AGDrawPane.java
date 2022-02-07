package agswing;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.Consumer;
import javax.swing.JPanel;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public abstract class AGDrawPane extends JPanel {

    protected Graphics2D myg;
    protected Color background, foreground;

    public AGDrawPane() {
        super();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (myg == null) {
            activate(g);
        }
        myg = (Graphics2D) g;
        AGDraw((Graphics2D) g);
    }

    public void activate(Graphics g) {
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public void clear() {
        if (myg != null) {
            myg.setBackground(background);
            myg.setColor(background);
            myg.fillRect(0, 0, this.getSize().width, this.getSize().height);
            myg.setColor(foreground);
        }
    }

    abstract public void AGDraw(Graphics2D g);
}
