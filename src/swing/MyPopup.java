/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class MyPopup extends JPopupMenu {

    Color bg = Color.WHITE, fg = Color.BLACK;
    ArrayList<String> text;

    public MyPopup() {
        super();
        setBackground(Color.WHITE);
        setFg(Color.BLACK);
        add(new JMenuItem("           "));
        add(new JMenuItem("           "));
        add(new JMenuItem("           "));
        text = new ArrayList();
//        add(new JMenuItem("           "));
//        add(new JMenuItem("           "));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(bg);
        g2d.fillRect(10, 10, 15, 15);
        g2d.setColor(fg);
        for (int i=0; i< text.size(); i++)
            g2d.drawString(text.get(i), 10, 36+i*16);

    }

    public void addText(String t) {
        text.add(t);
    }
    

    public Color getBg() {
        return bg;
    }

    public void setBg(Color bg) {
        this.bg = bg;
    }

    public Color getFg() {
        return fg;
    }

    public void setFg(Color fg) {
        this.fg = fg;
    }

}
