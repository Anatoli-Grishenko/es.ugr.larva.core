/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class OlePopUp extends JPopupMenu {
    
  
    public OlePopUp(Rectangle r) {
        super();
        this.setBounds(r);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        paintPopUp(g2d);
    }

    public void paintPopUp(Graphics2D g) {
        g.setColor(Color.red);
        g.fillRect(this.getBounds().x, this.getBounds().y, 
                this.getBounds().width, this.getBounds().height);
    }

}
