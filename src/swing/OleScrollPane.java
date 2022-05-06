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
import java.util.function.Consumer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleScrollPane extends JScrollPane {

    public double DragSpeed = 0.05;
    OleDrawPane odPane;
    double zoom;
    int x1, x2, y1, y2;
    Rectangle view;
    Dimension reference;
    Consumer <MouseEvent> handlerClick, handlerMove, handlerDrag, 
            handlerPress, handlerRelease, handlerEnter, handlerExit;
    Consumer <MouseWheelEvent> handlerWheel;

    public OleScrollPane(OleDrawPane o) {
        super(o);
        odPane = o;
        reference = odPane.getPreferredSize();
        zoom = 1;

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handlerDrag.accept(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                handlerMove.accept(e);
            }
        });
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                handlerWheel.accept(e);
            }
        });
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handlerClick.accept(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                handlerPress.accept(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handlerRelease.accept(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                handlerEnter.accept(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                handlerExit.accept(e);
            }
        });
        this.setHandlerWheel((e) -> Wheel(e));
        setHandlerClick((e)->Clicked(e));
        setHandlerDrag((e)->Drag(e));
        setHandlerPress((e)->Pressed(e));
        setHandlerRelease((e)->Released(e));
        setHandlerEnter((e)->Entered(e));
        setHandlerExit((e)->Exited(e));                
    }

    public void clear() {
        odPane.clear();
    }

    public void reset(Dimension d) {
        reference = d;
    }

    public OleDrawPane getOleDrawPane() {
        return odPane;
    }

    public void setZoom(double z) {
        zoom = z;
        int nw = (int) (reference.getWidth() * getZoom()),
                nh = (int) (reference.getHeight() * getZoom());
        odPane.setPreferredSize(new Dimension(nw, nh));
        this.setViewportView(odPane);
    }

    public double getZoom() {
        return zoom;
    }

    public void zoomIn() {
        setZoom(getZoom() + .1);
    }

    public void zoomOut() {
        setZoom(getZoom() - 0.1);
    }

    protected void Drag(MouseEvent e) {
        x2 = e.getX();
        y2 = e.getY();
        view = getViewport().getViewRect();
        view.setLocation((int) (view.getX() + (x1 - x2) * DragSpeed), (int) (view.getY() + (y1 - y2) * DragSpeed));
        odPane.scrollRectToVisible(view);
    }

    protected void Wheel(MouseWheelEvent e) {

        int steps = e.getWheelRotation();
        if (steps < 0 && getZoom() > 0) {
            zoomOut();
        }
        if (steps > 0 && getZoom() < 30) {
            zoomIn();
        }
    }

    protected void Pressed(MouseEvent e) {
        x1 = e.getX();
        y1 = e.getY();
    }

    protected void Clicked(MouseEvent e) {
        x1 = e.getX();
        y1 = e.getY();
    }

    protected void Released(MouseEvent e) {
        x1 = e.getX();
        y1 = e.getY();
    }

    protected void Entered(MouseEvent e) {
        x1 = e.getX();
        y1 = e.getY();
    }

    protected void Exited(MouseEvent e) {
        x1 = e.getX();
        y1 = e.getY();
    }

    public void setHandlerClick(Consumer<MouseEvent> handlerClick) {
        this.handlerClick = handlerClick;
    }

    public void setHandlerMove(Consumer<MouseEvent> handlerMove) {
        this.handlerMove = handlerMove;
    }

    public void setHandlerDrag(Consumer<MouseEvent> handlreDrag) {
        this.handlerDrag = handlreDrag;
    }

    public void setHandlerWheel(Consumer<MouseWheelEvent> handlerWheel) {
        this.handlerWheel = handlerWheel;
    }

    public void setHandlerPress(Consumer<MouseEvent> handlerPress) {
        this.handlerPress = handlerPress;
    }

    public void setHandlerRelease(Consumer<MouseEvent> handlerRelease) {
        this.handlerRelease = handlerRelease;
    }

    public void setHandlerEnter(Consumer<MouseEvent> handlerEnter) {
        this.handlerEnter = handlerEnter;
    }

    public void setHandlerExit(Consumer<MouseEvent> handlerExit) {
        this.handlerExit = handlerExit;
    }
    
    public int getRealX(int xpane) {
        return (int) ((xpane+this.getHShift())/getZoom());
    }
    public int getRealY(int ypane) {
        return (int) ((ypane+this.getVShift())/getZoom());
    }
    
    public int getPaneX(int xreal) {
        return (int) ((xreal)*getZoom());
    }
    public int getPaneY(int yreal) {
        return (int) ((yreal)*getZoom());
    }
//    public int getPaneX(int xreal) {
//        return (int) (xreal*getZoom()-getHShift());
//    }
//    public int getPaneY(int yreal) {
//        return (int) (yreal*getZoom()-getVShift());
//    }
    
    public int getHShift(){
        return this.getHorizontalScrollBar().getValue();
    }
    
    public int getVShift(){
        return this.getVerticalScrollBar().getValue();
    }
}
