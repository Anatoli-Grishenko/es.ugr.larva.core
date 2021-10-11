/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import geometry.Point;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import map2D.Map2DColor;
import map2D.Palette;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class MyMapPalPane extends MyDrawPane {

    protected Color map[][];
    protected Map2DColor m2dMap;
    protected ImageIcon imgs[][];
    protected MyDrawPane dpMap, dpPalette;
    protected JScrollPane spMap;
    protected Palette palMap;
    protected int mapwidth, mapheight, zoom, x, y, width, height, offsetimg = 0;
    protected int palw = 35, cellw = 20, shadow = 0;
    protected MyPopup mPopup;
    protected boolean ruler, trail, hotspot, redecorate = true, paintpalette = true;
    protected ArrayList<Point> thetrail;

    public MyMapPalPane(Consumer<Graphics2D> function) {
        super(function);
        this.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        this.setVisible(false);
        this.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        dpMap = new MyDrawPane(g -> paintMap(g));
        dpMap.setVisible(false);
        spMap = new JScrollPane(dpMap);
        spMap.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        spMap.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        spMap.setVisible(false);
        mapwidth = -1;
        mapheight = -1;
        zoom = 1;
        thetrail = new ArrayList();

        spMap.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int steps = e.getWheelRotation() / Math.abs(e.getWheelRotation());
                if (e.isControlDown() && 1 <= zoom + steps && zoom + steps < 30) {
                    setZoom(zoom + steps);
                }
            }
        });
        spMap.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
//                System.out.println(new Object() {
//                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int x = (int) ((e.getX() + spMap.getViewport().getViewPosition().getX()) / zoom),
                        y = (int) ((e.getY() + spMap.getViewport().getViewPosition().getY()) / zoom);
                mPopup = new MyPopup();
                if (0 <= x && x < getMapWidth() && 0 <= y && y < getMapHeight()) {
                    Color c = getColor(x, y);
                    mPopup.setBg(c);
                    if (palMap != null) {
                        mPopup.addText(String.format("x=%03d y=%03d", x, y));
                        mPopup.addText("Level: " + palMap.getLevel(c) + " m ");
                        mPopup.setVisible(true);
                        mPopup.show(e.getComponent(), e.getX(), e.getY());
                    }
                } else {
                    mPopup.addText("OUT OF BORDERS");
                    mPopup.setVisible(true);
                    mPopup.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mPopup.setVisible(false);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
//                System.out.println(new Object() {
//                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseExited(MouseEvent e) {
//                System.out.println(new Object() {
//                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        spMap.setVisible(false);
    }

    public void setPreferredSize(Dimension d) {
        super.setPreferredSize(d);
        width = (int) d.getWidth();
        height = (int) d.getHeight();
    }

    public void addRuler() {
        ruler = true;
//        offsetimg = 10;
    }

    public void addTrail() {
        trail = true;
        shadow = 0;
        offsetimg = 0;
    }

    public void addShadow(int s) {
        shadow = s;
        offsetimg = s;
        trail = false;
    }

    public void addHotSpot() {
        hotspot = true;
    }

    public Graphics2D getMapGraphics() {
        return (Graphics2D) dpMap.getGraphics();
    }

    public void setImage(ImageIcon i, int x, int y) {
        imgs[x][y] = i;
        m2dMap.setColor(x, y, Color.GREEN);
        validate();
        repaint();
    }

    public void setTrail(int x, int y, int z) {
        thetrail.add(new Point(x, y, z));
        redecorate = false;
        validate();
        repaint();
    }

    public void setBounds(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;
        super.setBounds(x, y, width, height);
        validate();
        repaint();
    }

    protected void defLayout() {
        this.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        this.setBackground(Color.BLACK);
        dpMap.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        dpMap.setBackground(Color.BLUE);
        spMap.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        spMap.setBackground(Color.BLACK);
        if (palMap == null) {
            spMap.setPreferredSize(new Dimension((int) this.getBounds().getWidth(), (int) this.getBounds().getHeight()));
            this.add(spMap);
        } else {
            dpPalette = new MyDrawPane(g -> paintPalette(g));
            dpPalette.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
            dpPalette.setBackground(Color.BLACK);
            dpPalette.setPreferredSize(new Dimension(palw, (int) this.getBounds().getHeight()));
            dpPalette.setVisible(true);
            spMap.setPreferredSize(new Dimension((int) this.getBounds().getWidth() - palw, (int) this.getBounds().getHeight()));
            this.add(dpPalette);
            this.add(spMap);
        }
        this.setVisible(true);
        spMap.setVisible(true);
        dpMap.setVisible(true);
        setZoom(1);
    }

    public MyMapPalPane setMap(Color m[][], Palette p) {
        this.removeAll();
        mapwidth = (m[0].length);
        mapheight = (m.length);
        map = new Color[getMapWidth()][this.getMapHeight()];
        m2dMap = new Map2DColor(getMapWidth(), this.getMapHeight());
        imgs = new ImageIcon[getMapWidth()][this.getMapHeight()];
        for (int i = 0; i < getMapWidth(); i++) {
            for (int j = 0; j < getMapHeight(); j++) {
                setColor(i, j, m[i][j]);
            }
        }
        palMap = p;
        setPainter(g -> paintMpMap(g));
        defLayout();
        setZoom(Math.max((int) spMap.getPreferredSize().getWidth() / getMapWidth(),
                (int) spMap.getPreferredSize().getHeight()) / getMapHeight());
        return this;
    }

    void paintMpMap(Graphics2D g) {
        if (paintpalette) {
            paintPalette((Graphics2D) dpPalette.getGraphics());
            paintpalette = false;
        }
        paintMap((Graphics2D) dpMap.getGraphics());
    }

    protected void paintPalette(Graphics2D g) {
        int ph = 10, h = (int) this.getBounds().getHeight(),
                pt = (2 * palw) / 3, n = (h - 2 * ph) / ph;
        Font f = g.getFont();
        g.setFont(new Font(f.getFamily(), f.getStyle(), f.getSize() - 2));
        g.setColor(Color.WHITE);
        g.drawString("x" + this.zoom, 0, ph);
        for (int i = 0, k = 0; i <= n; k++, i++) {
            int c = (i * palMap.size()) / n;
            if (c >= palMap.size()) {
                c = palMap.size() - 1;
            }
            g.setColor(palMap.getColor(c));
            g.fillRect(pt, ph + k * ph, palw - pt - 2, ph);
            g.setColor(Color.WHITE);
            g.drawRect(pt, ph + k * ph, palw - pt - 2, ph);
            g.drawString(String.format("%03d", c), 1, ph + k * ph + ph);
        }
        g.setFont(f);
    }

    protected void paintMap(Graphics2D g) {
        if (map == null || g == null) {
            return;
        }
        if (redecorate) {
            preDecorateMap(g);
            g.drawImage(m2dMap.getMap(), offsetimg, offsetimg, this.getMapWidth() * zoom, this.getMapHeight() * zoom, null);
            postDecorateMap(g);
        }
        if (trail) {
            this.paintTrail(g);
        } else {
            if (thetrail.size() > 1) {
                hideTrailPos(g, thetrail.size() - 2);
            }
            if (thetrail.size() > 0) {
                paintTrailPos(g, thetrail.size() - 1);
            }
        }
        if (shadow > 0) {
            Point p1, p2;
            Color c;
            int level;
            if (thetrail.size() > 0 && palMap != null) {
                if (thetrail.size() > 1) {
                    p1 = thetrail.get(thetrail.size() - 2);
                    level = shadow*(int) p1.getZ()/256;
                    hidePoint(g, p1);
                    p2 = p1.clone().minus(new Point(level, level, 0));
                    hidePoint(g, p2);
                }
                p1 = thetrail.get(thetrail.size() - 1);
                level = shadow*(int) p1.getZ()/256;
                p2 = p1.clone().minus(new Point(level, level, 0));
                paintPoint(g, p1, Color.BLACK);
                paintPoint(g, p2, Color.GREEN);
            }
        }
        redecorate = true;

    }

    protected void paintTrail(Graphics2D g) {
        for (int i = 0; i < thetrail.size(); i++) {
            paintTrailPos(g, i);
        }
    }

    protected void paintTrailPos(Graphics2D g, int pos) {
        Point p = thetrail.get(pos);
        paintPoint(g, p, new Color(0, 256 * pos / thetrail.size(), 0));
        framePoint(g, p, Color.GREEN);

    }

    protected void paintPoint(Graphics2D g, Point p, Color c) {
        g.setColor(c);
        g.fillRect(offsetimg + zoom * (int) p.getX(), offsetimg + zoom * (int) p.getY(), zoom, zoom);
    }

    protected void framePoint(Graphics2D g, Point p, Color c) {
        g.setColor(c);
        g.drawRect(offsetimg + zoom * (int) p.getX(), offsetimg + zoom * (int) p.getY(), zoom, zoom);
    }

    protected void hideTrailPos(Graphics2D g, int pos) {
        Point p = thetrail.get(pos);
        hidePoint(g, p);
    }

    protected void hidePoint(Graphics2D g, Point p) {
        Color c;
        if (0 <= p.getX() && p.getX() < getMapWidth() && 0 <= p.getY() && p.getY() < getMapHeight()) {
            c = this.m2dMap.getColor((int) p.getX(), (int) p.getY());
        } else {
            c = this.dpMap.getBackground();
        }
        g.setColor(c);
        g.setBackground(c);
        g.fillRect(offsetimg + zoom * (int) p.getX(), offsetimg + zoom * (int) p.getY(), zoom, zoom);

    }

    protected void preDecorateMap(Graphics2D g) {
        if (map == null) {
            return;
        }
        g.setBackground(Color.darkGray);
        g.clearRect(0, 0, dpMap.getWidth(), dpMap.getHeight());
    }

    protected void postDecorateMap(Graphics2D g) {
        if (map == null) {
            return;
        }
        if (ruler) {
            if (zoom > 5) {
                g.setColor(Color.DARK_GRAY);
                for (int i = 0; i < this.getMapWidth(); i++) {
                    g.drawLine(i * zoom + offsetimg, offsetimg, i * zoom + offsetimg, zoom * this.getMapHeight() + offsetimg);
                }
                for (int i = 0; i < this.getMapHeight(); i++) {
                    g.drawLine(offsetimg, offsetimg + i * zoom, zoom * this.getMapWidth() + offsetimg, offsetimg + i * zoom);
                }
            }
        }
        if (hotspot) {
            g.setColor(Color.GREEN);
            g.drawRect(offsetimg + zoom * (this.getMapWidth() / 2), offsetimg + zoom * (this.getMapHeight() / 2), zoom, zoom);
        }
    }

    void setZoom(int z) {
        zoom = z;
        dpMap.setPreferredSize(new Dimension(getMapWidth() * zoom + offsetimg * 2, getMapHeight() * zoom + offsetimg * 2));
        spMap.setViewportView(dpMap);
        spMap.validate();
        spMap.repaint();
    }

    public void setColor(int x, int y, Color c) {
        if (0 <= x && x < getMapWidth() && 0 <= y && y <= getMapHeight()) {
//            if (y%3==0)
//                c = Color.RED;
//            else if (y%3==1)
//                c = Color.GREEN;
//            else
//                c = Color.BLUE;
            map[x][y] = c;
            m2dMap.setColor(x, y, c);
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
