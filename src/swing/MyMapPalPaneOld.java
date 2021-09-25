///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package swing;
//
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.Font;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Insets;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.event.MouseWheelEvent;
//import java.awt.event.MouseWheelListener;
//import java.util.function.Consumer;
//import javax.swing.BorderFactory;
//import javax.swing.ImageIcon;
//import javax.swing.JMenuItem;
//import javax.swing.JPanel;
//import javax.swing.JPopupMenu;
//import javax.swing.JScrollPane;
//import javax.swing.ScrollPaneConstants;
//import javax.swing.border.Border;
//import javax.swing.border.EmptyBorder;
//import map2D.Palette;
//
///**
// *
// * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
// */
//public class MyMapPalPaneOld extends MyDrawPane {
//
//    protected Color map[][];
//    protected ImageIcon imgs[][];
//    protected MyDrawPane dpMap, dpPalette;
//    protected JScrollPane spMap;
//    protected Palette palMap;
//    protected int mapwidth, mapheight, zoom, x, y, width, height;
//    protected int palw = 35, cellw = 20;
//    protected MyPopup mPopup;
//
//    public MyMapPalPaneOld(Consumer<Graphics2D> function) {
//        super(function);
//        this.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
//        this.setVisible(false);
//        this.setBorder(BorderFactory.createLineBorder(Color.WHITE));
//        dpMap = new MyDrawPane(g -> paintMap(g));
//        dpMap.setVisible(false);
//        spMap = new JScrollPane(dpMap);
//        spMap.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
//        spMap.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        spMap.setVisible(false);
//        mapwidth = -1;
//        mapheight = -1;
//        zoom = 1;
//
//        spMap.addMouseWheelListener(new MouseWheelListener() {
//            @Override
//            public void mouseWheelMoved(MouseWheelEvent e) {
//                int steps = e.getWheelRotation() / Math.abs(e.getWheelRotation());
//                if (e.isControlDown() && 1 <= zoom + steps && zoom + steps < 20) {
//                    setZoom(zoom + steps);
//                }
//            }
//        });
//        spMap.addMouseListener(new MouseListener() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                System.out.println(new Object() {
//                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//                int x = (int) ((e.getX() - spMap.getViewport().getBounds().getX()) / zoom),
//                        y = (int) ((e.getY() - spMap.getViewport().getBounds().getY()) / zoom);
//                Color c = getColor(x, y);
//                mPopup = new MyPopup();
//                mPopup.setBg(c);
//                if (palMap != null) {
//                    if (palMap.getLevel(c) >= 0) {
//                        mPopup.setText("Level: " + palMap.getLevel(c) + " m ");
//                    } else {
//                        mPopup.setText("Unreadable");
//                    }
//                    mPopup.setVisible(true);
//                    mPopup.show(e.getComponent(), e.getX(), e.getY());
//                }
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                mPopup.setVisible(false);
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                System.out.println(new Object() {
//                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//                System.out.println(new Object() {
//                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//        });
//        spMap.setVisible(false);
//    }
//
//    public void setPreferredSize(Dimension d) {
//        super.setPreferredSize(d);
//        width = (int) d.getWidth();
//        height = (int) d.getHeight();
//    }
//
//    public Graphics2D getMapGraphics() {
//        return (Graphics2D) dpMap.getGraphics();
//    }
//
//    public void setImage(ImageIcon i, int x, int y) {
//        imgs[x][y] = i;
//        validate();
//        repaint();
//    }
//
//    public void setBounds(int x, int y, int w, int h) {
//        this.x = x;
//        this.y = y;
//        width = w;
//        height = h;
//        super.setBounds(x, y, width, height);
//        validate();
//        repaint();
//    }
//
//    protected void defLayout() {
//        this.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        this.setBackground(Color.BLACK);
//        dpMap.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        dpMap.setBackground(Color.BLACK);
//        spMap.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//        spMap.setBackground(Color.BLACK);
//        if (palMap == null) {
//            spMap.setPreferredSize(new Dimension((int) this.getBounds().getWidth(), (int) this.getBounds().getHeight()));
//            this.add(spMap);
//        } else {
//            dpPalette = new MyDrawPane(g -> paintPalette(g));
//            dpPalette.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
//            dpPalette.setBackground(Color.BLACK);
//            dpPalette.setPreferredSize(new Dimension(palw, (int) this.getBounds().getHeight()));
//            dpPalette.setVisible(true);
//            spMap.setPreferredSize(new Dimension((int) this.getBounds().getWidth() - palw, (int) this.getBounds().getHeight()));
//            this.add(dpPalette);
//            this.add(spMap);
//        }
//        this.setVisible(true);
//        spMap.setVisible(true);
//        dpMap.setVisible(true);
//        setZoom(1);
//    }
//
//    public MyMapPalPaneOld setMap(Color m[][], Palette p) {
//        mapwidth = (m[0].length);
//        mapheight = (m.length);
//        map = new Color[getMapWidth()][this.getMapHeight()];
//        imgs = new ImageIcon[getMapWidth()][this.getMapHeight()];
//        for (int i = 0; i < getMapWidth(); i++) {
//            for (int j = 0; j < getMapHeight(); j++) {
//                setColor(i, j, m[i][j]);
//            }
//        }
//        palMap = p;
//        setPainter(g -> paintMpMap(g));
//        defLayout();
//        setZoom(Math.max((int) spMap.getPreferredSize().getWidth() / mapwidth,
//                (int) spMap.getPreferredSize().getHeight()) / mapheight);
//        return this;
//    }
//
//    void paintMpMap(Graphics2D g) {
//        paintMap((Graphics2D) dpMap.getGraphics());
//        paintPalette((Graphics2D) dpPalette.getGraphics());
//    }
//
////    @Override
////    public void paintComponent(Graphics g) {
//////        super.paintComponent(g);
//////        Graphics2D g2D = (Graphics2D) g;
////        paintMap(((Graphics2D)this.dpMap.getGraphics()));
////    }
//    protected void paintPalette(Graphics2D g) {
////        int ph = 10, h = (int) this.getBounds().getHeight(),
////                pt = (2 * palw) / 3, n = (h - ph) / ph;
////        Font f = g.getFont();
////        g.setFont(new Font(f.getFamily(), f.getStyle(), f.getSize() - 2));
////        for (int i = 0, k = 0; i <= n; k++, i++) {
////            int c = (i * palMap.size()) / n;
////            if (c >= palMap.size()) {
////                c = palMap.size() - 1;
////            }
////            g.setColor(palMap.getColor(c));
////            g.fillRect(pt, k * ph, palw - pt - 2, ph);
////            g.setColor(Color.WHITE);
////            g.drawRect(pt, k * ph, palw - pt - 2, ph);
////            g.drawString(String.format("%03d", c), 1, k * ph + ph);
////        }
////        g.setFont(f);
//    }
//
//    protected void paintMap(Graphics2D g) {
//        if (map == null) {
//            return;
//        }
////        for (int y = 0; y < getMapHeight(); y++) {
////            for (int x = 0; x < getMapWidth(); x++) {
////                g.setColor(getColor(x, y));
////                int px = x * zoom, py = y * zoom;
////                g.fillRect(px, py, zoom, zoom);
////            }
////        }
////        g.setColor(Color.GREEN);
////        g.drawRect(getMapWidth() / 2 * zoom, getMapHeight() / 2 * zoom, zoom, zoom);
//        for (int y = 0; y < getMapHeight(); y++) {
//            for (int x = 0; x < getMapWidth(); x++) {
//                int px = x * zoom, py = y * zoom;
//                if (imgs[x][y] != null) {
//                    g.drawImage(imgs[x][y].getImage(),
//                            px - imgs[x][y].getIconWidth() / 2, py - imgs[x][y].getIconHeight() / 2, null);
//                }
//            }
//        }
//    }
//
//    void setZoom(int z) {
//        zoom = z;
//        dpMap.setPreferredSize(new Dimension(getMapWidth() * zoom, getMapHeight() * zoom));
//        spMap.setViewportView(dpMap);
//        spMap.validate();
//        spMap.repaint();
//    }
//
//    public void setColor(int x, int y, Color c) {
//        if (0 <= x && x < getMapWidth() && 0 <= y && y <= getMapHeight()) {
//            map[x][y] = c;
//        }
//    }
//
//    public Color getColor(int x, int y) {
//        if (0 <= x && x < getMapWidth() && 0 <= y && y <= getMapHeight()) {
//            return map[x][y];
//        }
//        return null;
//    }
//
//    public Color[][] getMap() {
//        return map;
//    }
//
//    public int getMapWidth() {
//        return mapwidth;
//    }
//
//    public int getMapHeight() {
//        return mapheight;
//    }
//
////    public void setMapWidth(int width) {
////        this.mapwidth = width;
////        dpMap.setPreferredSize(new Dimension(getMapWidth() * zoom, getMapHeight() * zoom));
////        this.setViewportView(dpMap);
////        this.validate();
////    }
////
////    public void setMapHeight(int height) {
////        this.mapheight = height;
////        dpMap.setPreferredSize(new Dimension(getMapWidth() * zoom, getMapHeight() * zoom));
////        this.setViewportView(dpMap);
////        this.validate();
////    }
////    public void mouseWheelMoved(MouseWheelEvent e) {
////        int steps = e.getWheelRotation();
////        if (1 <= zoom+steps && zoom+steps<10) {
////            setZoom(zoom+steps);
////        }
////    }
////    @Override
////    public void actionPerformed(ActionEvent e) {
////        System.out.println(new Object(){}.getClass().getEnclosingMethod().getName()+": "+e.getActionCommand()+" Not supported yet."); //To change body of generated methods, choose Tools | Templates.
////    }
////    @Override
////    public void mouseWheelMoved(MouseWheelEvent e) {
////        System.out.println(new Object(){}.getClass().getEnclosingMethod().getName()+": "+e.getWheelRotation()+" Not supported yet."); //To change body of generated methods, choose Tools | Templates.
////    }
//}
