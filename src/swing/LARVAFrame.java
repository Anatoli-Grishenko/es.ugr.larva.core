/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;
import javax.swing.JFrame;

/**
 *
 * @author lcv
 */
public class LARVAFrame extends JFrame implements ActionListener, KeyListener {

    Consumer<ActionEvent> myListener;
    Consumer<KeyEvent> myKeyListener;

    public LARVAFrame(Consumer<ActionEvent> listener) {
        super();
        myListener = listener;
        this.setTitle("LARVA");

    }
    public LARVAFrame(String title, Consumer<ActionEvent> listener) {
        super();
        myListener = listener;
        this.setTitle(title);

    }

    public LARVAFrame() {
        super();
        this.setTitle("LARVA");

    }

    public void setListener(Consumer<ActionEvent> listener) {
        myListener = listener;
    }

    public void setKeyListener(Consumer<KeyEvent> myKeyListener) {
        this.myKeyListener = myKeyListener;
        this.addKeyListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        myListener.accept(e);
    }

    public void closeLARVAFrame() {
        setVisible(false);
//        dispose();
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));

    }

//    public void defaultListener(ActionEvent e) {
//
//    }

    @Override
    public void keyTyped(KeyEvent e) {
//        System.out.println("Typed: " + e.getKeyChar());
//        if (this.myKeyListener != null) {
//            myKeyListener.accept(e);
//        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("Pressed: " + e.getKeyChar());
        if (this.myKeyListener != null) {
            myKeyListener.accept(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
//        System.out.println("Released: " + e.getKeyChar());
//        if (this.myKeyListener != null) {
//            myKeyListener.accept(e);
//        }
    }

}
