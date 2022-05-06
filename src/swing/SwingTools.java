/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import geometry.Point3D;
import geometry.SimpleVector3D;
import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class SwingTools {

    public static void doSwingLater(Runnable what) {
        if (!SwingUtilities.isEventDispatchThread()) {
//            System.out.println("WITHIN SWING");
            SwingUtilities.invokeLater(() -> {
                what.run();
            });
        } else {
//            System.out.println("WITHOUT SWING");
            what.run();
        }
    }

    public static void doSwingWait(Runnable what) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
//                System.out.println("WITHIN SWING");
                SwingUtilities.invokeAndWait(() -> {
                    what.run();
                });
            } catch (Exception ex) {
            }
        } else {
//            System.out.println("WITHOUT SWING");
            what.run();
        }
    }

    public static ImageIcon toIcon(String image, int nw, int nh) {
        ImageIcon res;
        Image aux;
        aux = new ImageIcon(image).getImage();
        res = new ImageIcon(aux.getScaledInstance(nw, nh, Image.SCALE_SMOOTH));
        return res;
    }

    public static void initLookAndFeel(String UI) {
        try {
            switch (UI.toUpperCase()) {
                case "LIGHT":
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    break;
                case "DARK":
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    break;
                case "PLAIN":
                default:
            }
        } catch (Exception ex) {
            System.err.println("Failed to initialize look-and-feel");
        }
    }

    public static void Info(String message) {
        JOptionPane.showMessageDialog(null,
                message, "LARVA Boot", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void Error(String message) {
        JOptionPane.showMessageDialog(null,
                message, "LARVA Boot", JOptionPane.ERROR_MESSAGE);
    }

    public static void Warning(String message) {
        JOptionPane.showMessageDialog(null,
                message, "LARVA Boot", JOptionPane.WARNING_MESSAGE);
    }

    public static String inputLine(String message) {
        String sResult = JOptionPane.showInputDialog(null, message, "LARVA Boot", JOptionPane.QUESTION_MESSAGE);
        return sResult;
    }

    public static String inputSelect(String message, String[] options, String value) {
        String res = (String) JOptionPane.showInputDialog(null, message, "LARVA Boot", JOptionPane.QUESTION_MESSAGE, null, options, value);
        return res;
    }

    public static boolean Confirm(String message) {
        boolean bResult = JOptionPane.showConfirmDialog(null,
                message, "LARVA Boot", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        return bResult;
    }

    public static void addLabel(Container con, String s) {
        JLabel l = new JLabel(s, SwingConstants.LEFT);
        con.add(l);
    }

    public static void addLabel(Container con, String s, Color col) {
        JLabel l = new JLabel(s, SwingConstants.LEFT);
        l.setForeground(col);
        con.add(l);
    }

    public static Color doLighter(Color c) {
        float ratio=1.5f;
        int r=c.getRed(), g=c.getGreen(), b=c.getBlue();
        r = (int) (r*ratio);
        g = (int)(g*ratio);
        b = (int)(b*ratio);
        return new Color(r,g,b);
    }
    
    public static Color doDarker(Color c) {
        float ratio=1.5f;
        int r=c.getRed(), g=c.getGreen(), b=c.getBlue();
        r = (int) (r/ratio);
        g = (int)(g/ratio);
        b = (int)(b/ratio);
        return new Color(r,g,b);
    }
    
    public static Rectangle doBroad(Rectangle r, int units) {
        Rectangle res= new Rectangle(r.x-units,r.y-units,r.width+2*units, r.height+2*units);
        return res;
    }
    
    public static Rectangle doNarrow(Rectangle r, int units) {
        Rectangle res= new Rectangle(r.x+units,r.y+units,r.width-2*units, r.height-2*units);
        return res;
    }
    
    public static Polygon TraceRegularPolygon(geometry.AngleTransporter at, SimpleVector3D sv, int npoints, int radius1) {
        return TraceRegularPolygon(at, sv, npoints, radius1, 0);
    }

    public static Polygon TraceRegularPolygon(geometry.AngleTransporter at, SimpleVector3D sv, int npoints, int radius1, int rotate) {
        int xsv = (int)sv.getSource().getX(), ysv = (int)sv.getSource().getY();
        Point3D pxsv = new Point3D(xsv, ysv), p1, pmid1, p2;
        double alpha;
        Polygon p = new Polygon();
        for (int np = 0; np < npoints; np++) {
            p1 = at.alphaPoint(360 / npoints * np - rotate, radius1, pxsv);
            p.addPoint(p1.getXInt(), p1.getYInt());
        }
        p1 = at.alphaPoint(0-rotate, radius1, pxsv);
        p.addPoint(p1.getXInt(), p1.getYInt());
        return p;
    }

    public static Polygon TraceRegularStar(geometry.AngleTransporter at, SimpleVector3D sv, int npoints, int radius1, int radius2) {
        int xsv = (int)sv.getSource().getX(), ysv = (int)sv.getSource().getY();
        Point3D pxsv = new Point3D(xsv, ysv), p1, pmid1, p2;
        double alpha = 0, increment = 360 / npoints;
        Polygon p = new Polygon();
        for (int np = 0; np < npoints; np++) {
            p1 = at.alphaPoint(alpha, radius1, pxsv);
            p.addPoint(p1.getXInt(), p1.getYInt());
            p1 = at.alphaPoint(alpha + increment / 2, radius2, pxsv);
            p.addPoint(p1.getXInt(), p1.getYInt());
            alpha += increment;
        }
        p1 = at.alphaPoint(0, radius1, pxsv);
        p.addPoint(p1.getXInt(), p1.getYInt());
        return p;
    }

    public static Polygon TraceCourse(SimpleVector3D sv, int length) {
        int xsv = (int)sv.getSource().getX(), ysv = (int)sv.getSource().getY(), 
                xsv2 = xsv + sv.canonical().getTarget().getXInt() * length, ysv2 = ysv + sv.canonical().getTarget().getYInt() * length;
        Polygon p = new Polygon();
        p.addPoint(xsv, ysv);
        p.addPoint(xsv2, ysv2);
        p.addPoint(xsv, ysv);
        return p;
    }

    public static Polygon transformPolygon(Polygon p, double scale, double shiftx, double shifty) {
        Polygon newpoly= new Polygon();
        for (int i=0; i<p.npoints; i++) {
            newpoly.addPoint((int)(shiftx+(p.xpoints[i])*scale),
            (int)(shifty+(p.ypoints[i])*scale));
        }
        return newpoly;
    }
}
