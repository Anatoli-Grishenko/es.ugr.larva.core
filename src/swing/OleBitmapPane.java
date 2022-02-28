/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleBitmapPane extends JScrollPane {

    OleDrawPane odPane;
    protected Graphics2D myg;

    public OleBitmapPane(OleDrawPane o) {
        super(o);
        odPane = o;
    }

//    @Override
//    public void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        System.out.println("superpaintcomponent");
//        myg = (Graphics2D) g;
//        odPane.OleDraw(myg);
//    }

    public void clear() {
        System.out.println("JSCrollPane Clear");
        odPane.clear();
    }

    public JPanel getOleDrawPane() {
        return odPane;
    }

//    public abstract void OleDrawScroll(Graphics2D g);

}
