/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import geometry.Circle3D;
import geometry.Entity3D;
import geometry.Line3D;
import geometry.Point3D;
import geometry.Polygon3D;
import geometry.Scene3D;
import geometry.String3D;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JScrollPane;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Ole3DPane extends JScrollPane {
    public double DragSpeed = 0.05;
    OleDrawPane odPane;
    int x1, x2, y1, y2;
    Rectangle view;
    Dimension reference;
    
    protected double d = 1200.0;
    /* factor de perspectiva angular */
    protected double r1 = 0.0;//5.68319;         /*  ngulo de desviaci¢n en radianes */
    protected double r2 = -2.4;//6.28319;        /*  ngulo de rotaci¢n en radianes */
    protected double r3 = 1.2;//5.79778;         /*  ngulo de elevaci¢n en radianes */
    protected double mx = 0.0, my = 0.0, /* Desplaz. del encuadre */
            mz = -350.0;
    /* Distancia de la c mara al 0,0,0 */
    protected double camx, camy, camz, camelevation = 0, camdeviation = 0, camrotation = 0, camdistance = 100;

    protected double sr1 = 0.0, sr2 = 0.0, sr3 = 0.0;
    /* factores de rotaci¢n de seno */
    protected double cr1 = 0.0, cr2 = 0.0, cr3 = 0.0;
    /* factores de rotaci¢n de coseno */

    protected int maxx = 638, minx = 1, maxy = 198, miny = 1;/* recorte del puerto de visi¢n */
    protected double pantalla_x, pantalla_y;

    protected int posx, posy;

    Scene3D myScene;

    public Ole3DPane(OleDrawPane o) {

        super(o);
        odPane = o;
        reference = odPane.getPreferredSize();

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Drag(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Wheel(e);
            }
        });
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Clicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Pressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Released(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                Entered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Exited(e);
            }
        });
        setDefaults();
    }

    protected void setDefaults() {
        odPane.setOleDraw((e)->Draw3D(e));
        this.SetPuertoVision(0, 0, 800,600,800,600);
        this.setCamdeviation(45);
        this.setCamelevation(45);
        this.setCamrotation(0);
//        this.setCamdistance(5);
        myScene = new Scene3D();
    }
    protected void Drag(MouseEvent e) {
        x2 = e.getX();
        y2 = e.getY();
        view = getViewport().getViewRect();
        view.setLocation((int) (view.getX() + (x1 - x2) * DragSpeed), (int) (view.getY() + (y1 - y2) * DragSpeed));
        odPane.scrollRectToVisible(view);
    }

    protected void Wheel(MouseWheelEvent e) {
        int steps = e.getWheelRotation() / Math.abs(e.getWheelRotation());
        if (e.isControlDown()) {
            if (steps < 0) {
                setCamdistance(getCamdistance() * 0.9);
            } else {
                setCamdistance(getCamdistance() * 1.1);
            }
        } else if (e.isShiftDown()) {
            setCamdeviation(getCamdeviation() + steps * 5);
        } else {
            setCamelevation(getCamelevation() + steps * 5);
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
    



    protected Point3D calculate3D(double x, double y, double z) {
        return calculate3D(new Point3D(x, y, z));
    }

    protected Point3D calculate3D(Point3D p3d) {
        Point3D res = new Point3D(0, 0);
        double xa, ya, za, xa2, ya2, za2, x2d, y2d;

        xa2 = p3d.getY();
        ya2 = p3d.getX();
        za2 = p3d.getZ();
        xa2 = (-1) * xa2;
        xa = cr1 * xa2 - sr1 * za2;
        za = sr1 * xa2 + cr1 * za2;
        xa2 = cr2 * xa + sr2 * ya2;
        ya = cr2 * ya2 - sr2 * xa;
        za2 = cr3 * za - sr3 * ya;
        ya2 = sr3 * za + cr3 * ya;
        xa2 = xa2 + mx;
        ya2 = ya2 + my;
        za2 = za2 + mz;
        x2d = d * xa2 / za2;
        x2d += this.getSize().width / 2;
//        x2d += 399;
        y2d = d * ya2 / za2;
//        y2d += 299;
        y2d += this.getSize().height / 2;
        xa = pantalla_x / this.getSize().width;
        ya = pantalla_y / this.getSize().height;
        x2d *= xa;
        y2d *= ya;
        res = new Point3D(x2d, y2d);
        posx = 0;
        posy = 0;
        return res;
    }

    public void SetPuertoVision(int xi, int yi, int xf, int yf, int resx, int resy) {

        maxx = xf;
        minx = xi;
        maxy = yf;
        miny = yi;
        pantalla_x = resx;
        pantalla_y = resy;
        this.repaint();
    }

    public void SetPosicionEncuadre(double camx, double camy) {

        mx = camx;
        my = camy;
        this.repaint();
    }

    protected void calculateCamera() {
        double auxelevation = (int) (270 - camelevation + 360) % 360, auxdeviation = camdeviation;

        camz = camdistance * Math.sin(auxelevation * Math.acos(-1) / 180);
        double prov = camdistance * Math.cos(auxelevation * Math.acos(-1) / 180);
        camx = prov * Math.acos(auxdeviation * Math.acos(-1) / 180);
        camy = prov * Math.cos(auxdeviation * Math.acos(-1) / 180);
        r1 = camrotation * Math.acos(-1) / 180;
        r2 = (auxdeviation - 180) * Math.acos(-1) / 180;
        r3 = (auxelevation - 180) * Math.acos(-1) / 180;
        sr1 = Math.sin(r1);
        sr2 = Math.sin(r2);
        sr3 = Math.sin(r3);
        cr1 = Math.cos(r1);
        cr2 = Math.cos(r2);
        cr3 = Math.cos(r3);
        mz = -camdistance;
        this.repaint();

    }

    public void setCamPolar(double elevacion,
            double desviacion,
            double rrotacion,
            double distancia) {
        setCamelevation(elevacion);
        setCamdeviation(desviacion);
        setCamrotation(rrotacion);
        setCamdistance(distancia);

    }

    public double getCamelevation() {
//        return 90 - (r3 * 180 / Math.acos(-1));
        return camelevation;
    }

    public double getCamdeviation() {
//        return r2 * 180 / Math.acos(-1) + 180;
        return camdeviation;
    }

    public double getCamrotation() {
//        return r1 * 180 / Math.acos(-1);
        return camrotation;
    }

    public double getCamdistance() {
        return -mz;
    }

    public void setCamelevation(double camelevation) {
        this.camelevation = (int) (360 + camelevation) % 360;
        this.calculateCamera();
    }

    public void setCamdeviation(double camdeviation) {
        this.camdeviation = (int) (360 + camdeviation) % 360;
        this.calculateCamera();
    }

    public void setCamrotation(double camrotation) {
        this.camrotation = (int) (360 + camrotation) % 360;
        this.calculateCamera();
    }

    public void setCamdistance(double camdistance) {
        this.camdistance = camdistance;
        this.calculateCamera();
    }

    public void moveTo(int x, int y) {
        posx = x;
        posy = y;
    }

    public void lineTo(int x, int y) {
        odPane.canvas().drawLine(posx, posy, x, y);
        moveTo(x, y);
    }

    public void AG2DLine(Point3D p1, Point3D p2) {
        odPane.canvas().drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
    }

    public void AG2DLine(int x1, int y1, int x2, int y2) {
        odPane.canvas().drawLine(x1, y1, x2, y2);
    }

    public void setCamPosition(double x, double y, double z, double rrotacion) {
        double prov;

        camx = x;
        camy = y;
        camz = z;
        r1 = rrotacion * Math.acos(-1) / 180;
        r2 = Math.atan2(x, y);
        prov = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        r3 = Math.atan2(z, prov);
        sr1 = Math.sin(r1);
        sr2 = Math.sin(r2);
        sr3 = Math.sin(r3);
        cr1 = Math.cos(r1);
        cr2 = Math.cos(r2);
        cr3 = Math.cos(r3);
        mz = -Math.sqrt(Math.pow(prov, 2) + Math.pow(z, 2));
    }

    public double getCamX() {
        double prov = camdistance * Math.cos(camelevation * Math.acos(-1) / 180);
        return prov * Math.cos(camdeviation * Math.acos(-1) / 180);
    }

    public double getCamY() {
        double prov = camdistance * Math.cos(camelevation * Math.acos(-1) / 180);
        return prov * Math.sin(camdeviation * Math.acos(-1) / 180);
    }

    public double getCamZ() {
        return camdistance * Math.sin(camelevation * Math.acos(-1) / 180);
    }

    public void SetPerspectiva(double p) {
        d = p;
    }

    public void MoverA(double x, double y, double z) {
        Point3D p2d;

        p2d = calculate3D(new Point3D(x, y, z));
        moveTo((int) p2d.getX(), (int) p2d.getY());
    }

    public void LineaA(double x, double y, double z) {
        Point3D p2d;

        p2d = calculate3D(new Point3D(x, y, z));
        lineTo((int) p2d.getX(), (int) p2d.getY());
    }

    public void drawLine3D(double xi, double yi, double zi,
            double xf, double yf, double zf) {
        Point3D p1;
        Point3D p2;
        p1 = calculate3D(xi, yi, zi);
        p2 = calculate3D(xf, yf, zf);
        odPane.canvas().drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
    }

    public void drawLine3D(Point3D p1, Point3D p2) {
        drawLine3D(p1.getX(), p1.getY(), p1.getZ(), p2.getX(), p2.getY(), p2.getZ());
    }

    public void draw3D(Entity3D o3d) {
        Point3D p1;
        Point3D p2;
        p1 = o3d.getPosition();
        if (o3d instanceof Line3D) {
            Line3D l3d = (Line3D) o3d;
            p2 = l3d.getEnd();
            odPane.canvas().setColor(o3d.getColor());
            this.drawLine3D(p1, p2);
        } else if (o3d instanceof Polygon3D) {
            Polygon3D p3d = (Polygon3D) o3d;
            Point3D p2d = calculate3D(p3d.getPosition());
            Point3D p2c = calculate3D(p3d.getVertex(2));
            odPane.canvas().setColor(p3d.getColor());
            double radius = Math.abs(p2d.getX() - p2c.getX());
            if (true) {
                Polygon p = new Polygon();
                p.addPoint((int) p2d.getX(), (int) p2d.getY());
                for (int i = 0; i < p3d.size(); i++) {
                    p2d = calculate3D(p3d.getVertex(i));
                    p.addPoint((int) p2d.getX(), (int) p2d.getY());
                }
                if (p3d.isFilled()) {
                    odPane.canvas().fillPolygon(p);
                } else {
                    odPane.canvas().drawPolygon(p);
                }
            } else {
//                odPane.canvas().drawLine((int)p2d.getX(),(int)p2d.getY(),(int)p2d.getX(),(int)p2d.getY());
                odPane.canvas().fillOval((int) p2c.getX(), (int) p2c.getY(), 3, 3); //(int) radius, (int) radius);
            }
        } else if (o3d instanceof String3D) {
            String3D s3d = (String3D) o3d;
            odPane.canvas().setColor(s3d.getColor());
            Point3D pt = calculate3D(s3d.getCenter());
            odPane.canvas().drawString(s3d.getContent(), (int) pt.getX(), (int) pt.getY());        
        } else if (o3d instanceof Circle3D) {
            Circle3D c3d = (Circle3D) o3d;
            odPane.canvas().setColor(c3d.getColor());
            Point3D pt = calculate3D(c3d.getCenter()); //, pr=calculate3D(c3d.getCenter().setX(pt.getX()+c3d.getRadius()));
            int radius3d=5; //(int) pt.planeDistanceTo(pr);
            odPane.canvas().fillArc((int) pt.getX(), (int) pt.getY(),radius3d, radius3d, 0, 360);
        }
//        Point3D pt = calculate3D(o3d.getCenter());
//        odPane.canvas().setColor(Color.WHITE);
//        odPane.canvas().drawString(o3d.getName(), (int) pt.getX(), (int) pt.getY());
    }

    public void clearScene3D() {
        myScene.clearAll();
    }
    public void addEntity3D(Entity3D o) {
        myScene.addEntity3D(o);
        this.repaint();
    }

    public void drawScene() {
        Point3D reference = new Point3D(getCamX(), getCamY(), getCamZ());
        if (myScene != null) {
//            for (Entity3D o : myScene.getAllObjects(null)) {
            for (Entity3D o : myScene.getAllObjects(reference)) {
//                System.out.println(o.getName());
                o.setName(String.format("%3.2f", o.getCenter().realDistanceTo(reference)));
                this.draw3D(o);
            }
        }
    }

    public void Poligono(Point3D vertices[], boolean transparente) {
        int puntosx[], puntosy[], i;
        Point3D p2D;

        puntosx = new int[vertices.length];
        puntosy = new int[vertices.length];
        for (i = 0; i < vertices.length; i++) {
            p2D = calculate3D(vertices[i]);
            puntosx[i] = (int) p2D.getX();
            puntosy[i] = (int) p2D.getY();
        }
        if (transparente) {
            odPane.canvas().drawPolygon(puntosx, puntosy, vertices.length);
        } else {
            odPane.canvas().fillPolygon(puntosy, puntosy, vertices.length);
        }
    }

    public void Draw3D(Graphics2D g) {
        g.drawString(String.format("desv=%3.2fº elev=%3.2fº  rot=%3.2fº dist=%3.2f cam=%s",
                this.getCamdeviation(), this.getCamelevation(), this.getCamrotation(), this.getCamdistance(),
                new Point3D(this.getCamX(), this.getCamY(), this.getCamZ()).toString()),
                10, 25);
        if (myScene != null && myScene.size() > 0) {
            g.drawString(String.format("size=%d objects", myScene.size()), 10, 50);
            this.drawScene();
        }
    }

}

