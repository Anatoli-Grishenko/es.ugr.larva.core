/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleProgressFrame extends OleFrame {
    JProgressBar pbMain;
    JLabel lMain;
    Container pMain;
    int w=200, h=50;

    public OleProgressFrame(String title){
        super(title);
        pMain= this.getContentPane();
        pMain.setLayout(new BorderLayout());
        setPreferredSize(new Dimension (w,h));
        pbMain= new JProgressBar(0,100);
        pbMain.setPreferredSize(new Dimension(w-20, 25));
        pbMain.setValue(0);
        lMain= new JLabel("Starting");
        pMain.add(pbMain);
        pMain.add(lMain);
        pMain.validate();
        this.pack();
        this.setVisible(true);
    }
    
    
    public void addProgress(String what, int value) {
        lMain.setText(what);
        pbMain.setValue(value);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
    }
    @Override
    public void itemStateChanged(ItemEvent e) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void myActionListener(ActionEvent e) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void myKeyListener(KeyEvent e) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyTyped(KeyEvent e) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyPressed(KeyEvent e) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void keyReleased(KeyEvent e) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
