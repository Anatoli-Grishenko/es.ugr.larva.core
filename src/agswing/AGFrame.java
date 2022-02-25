package agswing;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author lcv
 */
public abstract class AGFrame extends JFrame implements ActionListener {


    public AGFrame() {
        super();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        myListener(e);
    }

    public void closeLARVAFrame() {
        setVisible(false);
//        dispose();
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));

    }
    
    public abstract void myListener(ActionEvent e);
    
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

}
