/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import data.OleConfig;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public abstract class OleFrame extends JFrame implements ActionListener, KeyListener, ItemListener {

    OleConfig oConfig;

    public OleFrame(String title) {
        super(title);
        setVisible(true);
    }

    public OleFrame(OleConfig olecfg) {
        super(olecfg.getOptions().getString("FrameTitle", "Untitled"));
        oConfig = olecfg;
        setVisible(true);
    }

    public OleFrame init() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        return this;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        myActionListener(e);
    }

    @Override
    public abstract void itemStateChanged(ItemEvent e);

    public abstract void myActionListener(ActionEvent e);

    public abstract void myKeyListener(KeyEvent e);

    public void Info(String message) {
        JOptionPane.showMessageDialog(this,
                message, "Alert", JOptionPane.INFORMATION_MESSAGE);
    }

    public void Warning(String message) {
        JOptionPane.showMessageDialog(this,
                message, "Alert", JOptionPane.WARNING_MESSAGE);
    }

    public void Error(String message) {
        JOptionPane.showMessageDialog(this,
                message, "Alert", JOptionPane.ERROR_MESSAGE);
    }

    public String inputLine(String message) {
        String sResult = JOptionPane.showInputDialog(this, message, "Input", JOptionPane.QUESTION_MESSAGE);
        return sResult;
    }

    public String inputSelect(String message, String[] options, String value) {
        String res = (String) JOptionPane.showInputDialog(null, message, "Select", JOptionPane.QUESTION_MESSAGE, null, options, value);
        return res;
    }

    public boolean Confirm(String message) {
        boolean bResult = JOptionPane.showConfirmDialog(this,
                message, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        return bResult;
    }

//    @Override
//    public void keyTyped(KeyEvent e) {
//        myKeyListener(e);
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        myKeyListener(e);
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//        myKeyListener(e);
//    }

}
