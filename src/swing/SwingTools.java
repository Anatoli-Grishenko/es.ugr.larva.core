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
import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.Insets;
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

}
