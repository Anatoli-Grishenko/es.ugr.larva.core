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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleBitmapPane extends JScrollPane {

    public double DragSpeed=0.05;
    OleDrawPane odPane;
    double zoom;
    int x1,x2,y1,y2;
    Rectangle view;
    Dimension reference;

    public OleBitmapPane(OleDrawPane o) {
        super(o);
        odPane = o;
        reference = odPane.getPreferredSize();
//        odPane.setAutoscrolls(true);
        zoom=1;
        
        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                x2 = e.getX();
                y2= e.getY();
                view = getViewport().getViewRect();
                view.setLocation((int)(view.getX()+(x1-x2)*DragSpeed), (int)(view.getY()+(y1-y2)*DragSpeed));
                odPane.scrollRectToVisible(view);
//                System.out.println(new Object() {
//                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                System.out.println(new Object() {
                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
//                System.out.println(new Object() {
//                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); 
//To change body of generated methods, choose Tools | Templates.
//                double steps = (e.getWheelRotation() / Math.abs(e.getWheelRotation()))/10.0;
//                System.out.println("Wheel rotation "+e.getWheelRotation());
//                double steps = e.getWheelRotation() /10.0;
//                if (e.isControlDown() && 0 < zoom + steps && zoom + steps < 30) {
//                    setZoom(zoom + steps);
////                }
//                }
                int steps = e.getWheelRotation();
                if (steps < 0 && getZoom()>0) {
                    zoomOut();
                }
                if (steps > 0 && getZoom()<30) {
                    zoomIn();
                }
            }
        });
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println(new Object() {
                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mousePressed(MouseEvent e) {
                x1 = e.getX();
                y1= e.getY();
//                System.out.println(new Object() {
//                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println(new Object() {
                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                System.out.println(new Object() {
                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseExited(MouseEvent e) {
                System.out.println(new Object() {
                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }

    public void clear() {
        System.out.println("JSCrollPane Clear");
        odPane.clear();
    }

    public void reset(Dimension d) {
        reference = d;
    }
    public JPanel getOleDrawPane() {
        return odPane;
    }
    
    public void setZoom(double z) {
        zoom = z;
        int nw = (int) (reference.getWidth()*getZoom()),
                    nh =(int) (reference.getHeight()*getZoom());
        odPane.setPreferredSize(new Dimension(nw,nh));
        this.setViewportView(odPane);
    }

    public double getZoom() {
        return zoom;
    }
    
    public void zoomIn()  {
        setZoom(getZoom()+.1);        
    }
    public void zoomOut()  {
        setZoom(getZoom()-0.1);
    }
}
