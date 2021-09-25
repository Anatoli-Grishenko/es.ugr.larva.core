/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Image;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class SwingTools {
    public static void doSwingLater(Runnable what) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                what.run();
            });
        } else {
            what.run();
        }
    }

    public static void doSwingWait(Runnable what) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    what.run();
                });
            } catch (Exception ex) {
            }
        } else {
            what.run();
        }
    }
    
    public static ImageIcon toIcon(String image, int nw, int nh) {
        ImageIcon res;
        Image aux;
        aux =  new ImageIcon(image).getImage();
        res  = new ImageIcon(aux.getScaledInstance(nw, nh, Image.SCALE_SMOOTH));
        return res;
    }
    
}
