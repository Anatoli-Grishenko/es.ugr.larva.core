package agswing;

import geometry.Point;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.function.Consumer;
import swing.MyDrawPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class AG3DDrawPane extends AGDrawPane {

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

    /* dimensiones del modo */
    public AG3DDrawPane(int w, int h) {
        super();
        myScene = new Scene3D();

        this.setSize(w, h);
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println(new Object() {
                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println(new Object() {
                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int steps = e.getWheelRotation() / Math.abs(e.getWheelRotation());
                if (e.isControlDown()) {
                    if (steps < 0) {
                        setCamdistance(getCamdistance() * 0.9);
                    } else {
                        setCamdistance(getCamdistance() * 1.1);
                    }
//                    if (getCamdistance() + steps * 10 > 0) {
//                        setCamdistance(getCamdistance() + steps * 10);
//                    }
                } else if (e.isShiftDown()) {
                    setCamdeviation(getCamdeviation() + steps * 5);
                } else {
                    setCamelevation(getCamelevation() + steps * 5);
                }
            }
        });
    }

    protected void setDefaults() {
        this.SetPuertoVision(0, 0, this.getSize().width, this.getSize().height, this.getSize().width, this.getSize().height);
        this.setCamdeviation(45);
        this.setCamelevation(45);
        this.setCamrotation(0);
        this.setCamdistance(5000);

    }

    @Override
    public void activate(Graphics g) {
        super.activate(g);
        setDefaults();
    }

    protected Point calculate3D(double x, double y, double z) {
        return calculate3D(new Point(x, y, z));
    }

    protected Point calculate3D(Point p3d) {
        Point res = new Point(0, 0);
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
        res = new Point(x2d, y2d);
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
        myg.drawLine(posx, posy, x, y);
        moveTo(x, y);
    }

    public void AG2DLine(Point p1, Point p2) {
        myg.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
    }

    public void AG2DLine(int x1, int y1, int x2, int y2) {
        myg.drawLine(x1, y1, x2, y2);
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
        Point p2d;

        p2d = calculate3D(new Point(x, y, z));
        moveTo((int) p2d.getX(), (int) p2d.getY());
    }

    public void LineaA(double x, double y, double z) {
        Point p2d;

        p2d = calculate3D(new Point(x, y, z));
        lineTo((int) p2d.getX(), (int) p2d.getY());
    }

    public void drawLine3D(double xi, double yi, double zi,
            double xf, double yf, double zf) {
        Point p1, p2;
        p1 = calculate3D(xi, yi, zi);
        p2 = calculate3D(xf, yf, zf);
        myg.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
    }

    public void drawLine3D(Point p1, Point p2) {
        drawLine3D(p1.getX(), p1.getY(), p1.getZ(), p2.getX(), p2.getY(), p2.getZ());
    }

    public void draw3D(Object3D o3d) {
        Point p1, p2;
        p1 = o3d.getPosition();
        if (o3d instanceof Line3D) {
            Line3D l3d = (Line3D) o3d;
            p2 = l3d.getEnd();
            myg.setColor(o3d.getColor());
            this.drawLine3D(p1, p2);
        } else if (o3d instanceof Polygon3D) {
            Polygon3D p3d = (Polygon3D) o3d;
            Point p2d = calculate3D(p3d.getPosition()), p2c = calculate3D(p3d.getVertex(2));
            myg.setColor(p3d.getColor());
            double radius = Math.abs(p2d.getX() - p2c.getX());
            if (true) {
                Polygon p = new Polygon();
                p.addPoint((int) p2d.getX(), (int) p2d.getY());
                for (int i = 0; i < p3d.size(); i++) {
                    p2d = calculate3D(p3d.getVertex(i));
                    p.addPoint((int) p2d.getX(), (int) p2d.getY());
                }
                if (p3d.isFilled()) {
                    myg.fillPolygon(p);
                } else {
                    myg.drawPolygon(p);
                }
            } else {
//                myg.drawLine((int)p2d.getX(),(int)p2d.getY(),(int)p2d.getX(),(int)p2d.getY());
                myg.fillOval((int) p2c.getX(), (int) p2c.getY(), 3, 3); //(int) radius, (int) radius);
            }
        } else if (o3d instanceof String3D) {
            String3D s3d = (String3D) o3d;
            myg.setColor(s3d.getColor());
            Point pt = calculate3D(s3d.getCenter());
            myg.drawString(s3d.getContent(), (int) pt.getX(), (int) pt.getY());        
        } else if (o3d instanceof Circle3D) {
            Circle3D c3d = (Circle3D) o3d;
            myg.setColor(c3d.getColor());
            Point pt = calculate3D(c3d.getCenter()); //, pr=calculate3D(c3d.getCenter().setX(pt.getX()+c3d.getRadius()));
            int radius3d=5; //(int) pt.fastDistanceXYTo(pr);
            myg.fillArc((int) pt.getX(), (int) pt.getY(),radius3d, radius3d, 0, 360);
        }
//        Point pt = calculate3D(o3d.getCenter());
//        myg.setColor(Color.WHITE);
//        myg.drawString(o3d.getName(), (int) pt.getX(), (int) pt.getY());
    }

    public void clearScene3D() {
        myScene.clearAll();
    }
    public void addObject3D(Object3D o) {
        myScene.addObject3D(o);
    }

    public void drawScene() {
        Point reference = new Point(getCamX(), getCamY(), getCamZ());
        if (myScene != null) {
//            for (Object3D o : myScene.getAllObjects(null)) {
            for (Object3D o : myScene.getAllObjects(reference)) {
//                System.out.println(o.getName());
                o.setName(String.format("%3.2f", o.getCenter().realDistanceTo(reference)));
                this.draw3D(o);
            }
        }
    }

    public void Poligono(Point vertices[], boolean transparente) {
        int puntosx[], puntosy[], i;
        Point p2D;

        puntosx = new int[vertices.length];
        puntosy = new int[vertices.length];
        for (i = 0; i < vertices.length; i++) {
            p2D = calculate3D(vertices[i]);
            puntosx[i] = (int) p2D.getX();
            puntosy[i] = (int) p2D.getY();
        }
        if (transparente) {
            myg.drawPolygon(puntosx, puntosy, vertices.length);
        } else {
            myg.fillPolygon(puntosy, puntosy, vertices.length);
        }
    }

    @Override
    public void AGDraw(Graphics2D g) {
        g.drawString(String.format("desv=%3.2fº elev=%3.2fº  rot=%3.2fº dist=%3.2f cam=%s",
                this.getCamdeviation(), this.getCamelevation(), this.getCamrotation(), this.getCamdistance(),
                new Point(this.getCamX(), this.getCamY(), this.getCamZ()).toString()),
                10, 25);
        if (myScene != null && myScene.size() > 0) {
            g.drawString(String.format("size=%d objects", myScene.size()), 10, 50);
            this.drawScene();
        }
    }

}
//package agswing;
//
//import geometry.Point;
//import java.awt.Color;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Polygon;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.event.MouseWheelEvent;
//import java.awt.event.MouseWheelListener;
//import java.util.function.Consumer;
//import swing.MyDrawPane;
//
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
///**
// *
// * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
// */
//public class AG3DDrawPane extends AGDrawPane {
//
//    protected double d = 1200.0;
//    /* factor de perspectiva angular */
//    protected double r1 = 0.0;//5.68319;         /*  ngulo de desviaci¢n en radianes */
//    protected double r2 = -2.4;//6.28319;        /*  ngulo de rotaci¢n en radianes */
//    protected double r3 = 1.2;//5.79778;         /*  ngulo de elevaci¢n en radianes */
//    protected double mx = 0.0, my = 0.0, /* Desplaz. del encuadre */
//            mz = -350.0;
//    /* Distancia de la c mara al 0,0,0 */
//    protected double camx, camy, camz, camelevation = 0, camdeviation = 0, camrotation = 0, camdistance = 100;
//
//    protected double sr1 = 0.0, sr2 = 0.0, sr3 = 0.0;
//    /* factores de rotaci¢n de seno */
//    protected double cr1 = 0.0, cr2 = 0.0, cr3 = 0.0;
//    /* factores de rotaci¢n de coseno */
//
//    protected int maxx = 638, minx = 1, maxy = 198, miny = 1;/* recorte del puerto de visi¢n */
//    protected double pantalla_x, pantalla_y;
//
//    protected int posx, posy;
//
//    Scene3D myScene;
//
//    /* dimensiones del modo */
//    public AG3DDrawPane(int w, int h) {
//        super();
//        myScene = new Scene3D();
//
//        this.setSize(w, h);
//        this.addMouseListener(new MouseListener() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                System.out.println(new Object() {
//                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//                System.out.println(new Object() {
//                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                System.out.println(new Object() {
//                }.getClass().getEnclosingMethod().getName() + ":  Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
//        this.addMouseWheelListener(new MouseWheelListener() {
//            @Override
//            public void mouseWheelMoved(MouseWheelEvent e) {
//                int steps = e.getWheelRotation() / Math.abs(e.getWheelRotation());
//                if (e.isControlDown()) {
//                    if (steps < 0) {
//                        setCamdistance(getCamdistance() * 0.9);
//                    } else {
//                        setCamdistance(getCamdistance() * 1.1);
//                    }
////                    if (getCamdistance() + steps * 10 > 0) {
////                        setCamdistance(getCamdistance() + steps * 10);
////                    }
//                } else if (e.isShiftDown()) {
//                    setCamdeviation(getCamdeviation() + steps * 5);
//                } else {
//                    setCamelevation(getCamelevation() + steps * 5);
//                }
//            }
//        });
//    }
//
//    protected void setDefaults() {
//        this.SetPuertoVision(0, 0, this.getSize().width, this.getSize().height, this.getSize().width, this.getSize().height);
//        this.setCamdeviation(45);
//        this.setCamelevation(45);
//        this.setCamrotation(0);
//        this.setCamdistance(5000);
//
//    }
//
//    @Override
//    public void activate(Graphics g) {
//        super.activate(g);
//        setDefaults();
//    }
//
//    protected Point calculate3D(double x, double y, double z) {
//        return calculate3D(new Point(x, y, z));
//    }
//
//    protected Point calculate3D(Point p3d) {
//        Point res = new Point(0, 0);
//        double xa, ya, za, xa2, ya2, za2, x2d, y2d;
//
//        xa2 = p3d.getY();
//        ya2 = p3d.getX();
//        za2 = p3d.getZ();
//        xa2 = (-1) * xa2;
//        xa = cr1 * xa2 - sr1 * za2;
//        za = sr1 * xa2 + cr1 * za2;
//        xa2 = cr2 * xa + sr2 * ya2;
//        ya = cr2 * ya2 - sr2 * xa;
//        za2 = cr3 * za - sr3 * ya;
//        ya2 = sr3 * za + cr3 * ya;
//        xa2 = xa2 + mx;
//        ya2 = ya2 + my;
//        za2 = za2 + mz;
//        x2d = d * xa2 / za2;
//        x2d += this.getSize().width / 2;
////        x2d += 399;
//        y2d = d * ya2 / za2;
////        y2d += 299;
//        y2d += this.getSize().height / 2;
//        xa = pantalla_x / this.getSize().width;
//        ya = pantalla_y / this.getSize().height;
//        x2d *= xa;
//        y2d *= ya;
//        res = new Point(x2d, y2d);
//        posx = 0;
//        posy = 0;
//        return res;
//    }
//
//    public void SetPuertoVision(int xi, int yi, int xf, int yf, int resx, int resy) {
//
//        maxx = xf;
//        minx = xi;
//        maxy = yf;
//        miny = yi;
//        pantalla_x = resx;
//        pantalla_y = resy;
//        this.repaint();
//    }
//
//    public void SetPosicionEncuadre(double camx, double camy) {
//
//        mx = camx;
//        my = camy;
//        this.repaint();
//    }
//
//    protected void calculateCamera() {
//        double auxelevation = (int) (270 - camelevation + 360) % 360, auxdeviation = camdeviation;
//
//        camz = camdistance * Math.sin(auxelevation * Math.acos(-1) / 180);
//        double prov = camdistance * Math.cos(auxelevation * Math.acos(-1) / 180);
//        camx = prov * Math.acos(auxdeviation * Math.acos(-1) / 180);
//        camy = prov * Math.cos(auxdeviation * Math.acos(-1) / 180);
//        r1 = camrotation * Math.acos(-1) / 180;
//        r2 = (auxdeviation - 180) * Math.acos(-1) / 180;
//        r3 = (auxelevation - 180) * Math.acos(-1) / 180;
//        sr1 = Math.sin(r1);
//        sr2 = Math.sin(r2);
//        sr3 = Math.sin(r3);
//        cr1 = Math.cos(r1);
//        cr2 = Math.cos(r2);
//        cr3 = Math.cos(r3);
//        mz = -camdistance;
//        this.repaint();
//
//    }
//
//    public void setCamPolar(double elevacion,
//            double desviacion,
//            double rrotacion,
//            double distancia) {
//        setCamelevation(elevacion);
//        setCamdeviation(desviacion);
//        setCamrotation(rrotacion);
//        setCamdistance(distancia);
//
//    }
//
//    public double getCamelevation() {
////        return 90 - (r3 * 180 / Math.acos(-1));
//        return camelevation;
//    }
//
//    public double getCamdeviation() {
////        return r2 * 180 / Math.acos(-1) + 180;
//        return camdeviation;
//    }
//
//    public double getCamrotation() {
////        return r1 * 180 / Math.acos(-1);
//        return camrotation;
//    }
//
//    public double getCamdistance() {
//        return -mz;
//    }
//
//    public void setCamelevation(double camelevation) {
//        this.camelevation = (int) (360 + camelevation) % 360;
//        this.calculateCamera();
//    }
//
//    public void setCamdeviation(double camdeviation) {
//        this.camdeviation = (int) (360 + camdeviation) % 360;
//        this.calculateCamera();
//    }
//
//    public void setCamrotation(double camrotation) {
//        this.camrotation = (int) (360 + camrotation) % 360;
//        this.calculateCamera();
//    }
//
//    public void setCamdistance(double camdistance) {
//        this.camdistance = camdistance;
//        this.calculateCamera();
//    }
//
//    public void moveTo(int x, int y) {
//        posx = x;
//        posy = y;
//    }
//
//    public void lineTo(int x, int y) {
//        myg.drawLine(posx, posy, x, y);
//        moveTo(x, y);
//    }
//
//    public void AG2DLine(Point p1, Point p2) {
//        myg.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
//    }
//
//    public void AG2DLine(int x1, int y1, int x2, int y2) {
//        myg.drawLine(x1, y1, x2, y2);
//    }
//
//    public void setCamPosition(double x, double y, double z, double rrotacion) {
//        double prov;
//
//        camx = x;
//        camy = y;
//        camz = z;
//        r1 = rrotacion * Math.acos(-1) / 180;
//        r2 = Math.atan2(x, y);
//        prov = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
//        r3 = Math.atan2(z, prov);
//        sr1 = Math.sin(r1);
//        sr2 = Math.sin(r2);
//        sr3 = Math.sin(r3);
//        cr1 = Math.cos(r1);
//        cr2 = Math.cos(r2);
//        cr3 = Math.cos(r3);
//        mz = -Math.sqrt(Math.pow(prov, 2) + Math.pow(z, 2));
//    }
//
//    public double getCamX() {
//        double prov = camdistance * Math.cos(camelevation * Math.acos(-1) / 180);
//        return prov * Math.cos(camdeviation * Math.acos(-1) / 180);
//    }
//
//    public double getCamY() {
//        double prov = camdistance * Math.cos(camelevation * Math.acos(-1) / 180);
//        return prov * Math.sin(camdeviation * Math.acos(-1) / 180);
//    }
//
//    public double getCamZ() {
//        return camdistance * Math.sin(camelevation * Math.acos(-1) / 180);
//    }
//
//    public void SetPerspectiva(double p) {
//        d = p;
//    }
//
//    public void MoverA(double x, double y, double z) {
//        Point p2d;
//
//        p2d = calculate3D(new Point(x, y, z));
//        moveTo((int) p2d.getX(), (int) p2d.getY());
//    }
//
//    public void LineaA(double x, double y, double z) {
//        Point p2d;
//
//        p2d = calculate3D(new Point(x, y, z));
//        lineTo((int) p2d.getX(), (int) p2d.getY());
//    }
//
//    public void drawLine3D(double xi, double yi, double zi,
//            double xf, double yf, double zf) {
//        Point p1, p2;
//        p1 = calculate3D(xi, yi, zi);
//        p2 = calculate3D(xf, yf, zf);
//        myg.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
//    }
//
//    public void drawLine3D(Point p1, Point p2) {
//        drawLine3D(p1.getX(), p1.getY(), p1.getZ(), p2.getX(), p2.getY(), p2.getZ());
//    }
//
//    public void draw3D(Object3D o3d) {
//        Point p1, p2;
//        p1 = o3d.getPosition();
//        if (o3d instanceof Line3D) {
//            Line3D l3d = (Line3D) o3d;
//            p2 = l3d.getEnd();
//            myg.setColor(o3d.getColor());
//            this.drawLine3D(p1, p2);
//        } else if (o3d instanceof Polygon3D) {
//            Polygon3D p3d = (Polygon3D) o3d;
//            Point p2d = calculate3D(p3d.getPosition()), p2c = calculate3D(p3d.getVertex(2));
//            myg.setColor(p3d.getColor());
//            double radius = Math.abs(p2d.getX() - p2c.getX());
//            if (true) {
//                Polygon p = new Polygon();
//                p.addPoint((int) p2d.getX(), (int) p2d.getY());
//                for (int i = 0; i < p3d.size(); i++) {
//                    p2d = calculate3D(p3d.getVertex(i));
//                    p.addPoint((int) p2d.getX(), (int) p2d.getY());
//                }
//                if (p3d.isFilled()) {
//                    myg.fillPolygon(p);
//                } else {
//                    myg.drawPolygon(p);
//                }
//            } else {
////                myg.drawLine((int)p2d.getX(),(int)p2d.getY(),(int)p2d.getX(),(int)p2d.getY());
//                myg.fillOval((int) p2c.getX(), (int) p2c.getY(), 3, 3); //(int) radius, (int) radius);
//            }
//        } else if (o3d instanceof String3D) {
//            String3D s3d = (String3D) o3d;
//            myg.setColor(s3d.getColor());
//            Point pt = calculate3D(s3d.getCenter());
//            myg.drawString(s3d.getContent(), (int) pt.getX(), (int) pt.getY());
//        }
////        Point pt = calculate3D(o3d.getCenter());
////        myg.setColor(Color.WHITE);
////        myg.drawString(o3d.getName(), (int) pt.getX(), (int) pt.getY());
//    }
//
//    public void addObject3D(Object3D o) {
//        myScene.addObject3D(o);
//    }
//
//    public void drawScene() {
//        Point reference = new Point(getCamX(), getCamY(), getCamZ());
//        if (myScene != null) {
////            for (Object3D o : myScene.getAllObjects(null)) {
//            for (Object3D o : myScene.getAllObjects(reference)) {
////                System.out.println(o.getName());
//                o.setName(String.format("%3.2f", o.getCenter().realDistanceTo(reference)));
//                this.draw3D(o);
//            }
//        }
//    }
//
//    public void Poligono(Point vertices[], boolean transparente) {
//        int puntosx[], puntosy[], i;
//        Point p2D;
//
//        puntosx = new int[vertices.length];
//        puntosy = new int[vertices.length];
//        for (i = 0; i < vertices.length; i++) {
//            p2D = calculate3D(vertices[i]);
//            puntosx[i] = (int) p2D.getX();
//            puntosy[i] = (int) p2D.getY();
//        }
//        if (transparente) {
//            myg.drawPolygon(puntosx, puntosy, vertices.length);
//        } else {
//            myg.fillPolygon(puntosy, puntosy, vertices.length);
//        }
//    }
//
//    @Override
//    public void AGDraw(Graphics2D g) {
//        g.drawString(String.format("desv=%3.2fº elev=%3.2fº  rot=%3.2fº dist=%3.2f cam=%s",
//                this.getCamdeviation(), this.getCamelevation(), this.getCamrotation(), this.getCamdistance(),
//                new Point(this.getCamX(), this.getCamY(), this.getCamZ()).toString()),
//                10, 25);
//        if (myScene != null && myScene.size() > 0) {
//            g.drawString(String.format("size=%d objects", myScene.size()), 10, 50);
//            this.drawScene();
//        }
//    }
//
//}
