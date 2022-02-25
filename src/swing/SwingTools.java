/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
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
                System.out.println("WITHIN SWING");
                SwingUtilities.invokeAndWait(() -> {
                    what.run();
                });
            } catch (Exception ex) {
            }
        } else {
            System.out.println("WITHOUT SWING");
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

    public static void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize look-and-feel");
        }
    }

    public static void initFlatLaf() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
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

    public static  String inputLine(String message) {
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

}
