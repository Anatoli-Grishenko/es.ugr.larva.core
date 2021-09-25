/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class MyMapPane extends JScrollPane {

    protected Color map[][];
    protected MyDrawPane dpMap;
    protected int mapwidth, mapheight, zoom;
    JPopupMenu jpmMenu;

    public MyMapPane() {
        super();
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dpMap = new MyDrawPane(g -> paintMap(g));
        dpMap.setBackground(Color.BLACK);
        mapwidth = (int) this.getPreferredSize().getWidth() - 2;
        mapheight = (int) this.getPreferredSize().getHeight() - 2;
        zoom = 1;
        setZoom(zoom);
        this.setViewportView(dpMap);
        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int steps = e.getWheelRotation()/Math.abs(e.getWheelRotation());
                if (e.isControlDown() && 1 <= zoom + steps && zoom + steps < 10) {
                    setZoom(zoom + steps);
                }
            }
        });
        this.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
        System.out.println(new Object(){}.getClass().getEnclosingMethod().getName()+":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Color c = getColor(e.getX(),e.getY());
                int r=c.getRed(), g=c.getGreen(), b=c.getBlue();
                jpmMenu = new JPopupMenu();
                if (r == g && g == b)
                    jpmMenu.add(new JMenuItem("Level: "+r+" m "));
                else
                    jpmMenu.add(new JMenuItem("Color: "+r+" m "));
                jpmMenu.setVisible(true);
                jpmMenu.show(e.getComponent(), e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                jpmMenu.setVisible(false);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
        System.out.println(new Object(){}.getClass().getEnclosingMethod().getName()+":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseExited(MouseEvent e) {
        System.out.println(new Object(){}.getClass().getEnclosingMethod().getName()+":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        this.setVisible(false);
    }

    public MyMapPane setMap(Color m[][]) {
        mapwidth = (m[0].length);
        mapheight = (m.length);
        map = new Color[getMapWidth()][this.getMapHeight()];
        for (int i = 0; i < getMapWidth(); i++) {
            for (int j = 0; j < getMapHeight(); j++) {
                setColor(i, j, m[i][j]);
            }
        }
        setVisible(true);
        setZoom(1);
        return this;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        this.paintMap(g2D);
    }

    protected void paintMap(Graphics2D g) {
        if (map == null) {
            return;
        }
//        g.setColor(Color.RED);
//        g.fillRect(0, 0, getMapWidth() * zoom , getMapHeight() * zoom );
        for (int y = 0; y < getMapHeight(); y++) {
            for (int x = 0; x < getMapWidth(); x++) {
                g.setColor(getColor(x, y));
                int px = x * zoom, py = y * zoom;
                g.fillRect(px, py, zoom, zoom);
            }
        }
    }

    void setZoom(int z) {
        zoom = z;
        dpMap.setPreferredSize(new Dimension(getMapWidth() * zoom, getMapHeight() * zoom));
        this.setViewportView(dpMap);
        this.validate();
        this.repaint();
    }

    public void setColor(int x, int y, Color c) {
        if (0 <= x && x < getMapWidth() && 0 <= y && y <= getMapHeight()) {
            map[x][y] = c;
        }
    }

    public Color getColor(int x, int y) {
        if (0 <= x && x < getMapWidth() && 0 <= y && y <= getMapHeight()) {
            return map[x][y];
        }
        return null;
    }

    public Color[][] getMap() {
        return map;
    }

    public int getMapWidth() {
        return mapwidth;
    }

    public int getMapHeight() {
        return mapheight;
    }

//    public void setMapWidth(int width) {
//        this.mapwidth = width;
//        dpMap.setPreferredSize(new Dimension(getMapWidth() * zoom, getMapHeight() * zoom));
//        this.setViewportView(dpMap);
//        this.validate();
//    }
//
//    public void setMapHeight(int height) {
//        this.mapheight = height;
//        dpMap.setPreferredSize(new Dimension(getMapWidth() * zoom, getMapHeight() * zoom));
//        this.setViewportView(dpMap);
//        this.validate();
//    }
//    public void mouseWheelMoved(MouseWheelEvent e) {
//        int steps = e.getWheelRotation();
//        if (1 <= zoom+steps && zoom+steps<10) {
//            setZoom(zoom+steps);
//        }
//    }
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        System.out.println(new Object(){}.getClass().getEnclosingMethod().getName()+": "+e.getActionCommand()+" Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//    @Override
//    public void mouseWheelMoved(MouseWheelEvent e) {
//        System.out.println(new Object(){}.getClass().getEnclosingMethod().getName()+": "+e.getWheelRotation()+" Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
}
