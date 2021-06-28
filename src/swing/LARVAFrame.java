/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import javax.swing.JFrame;

/**
 *
 * @author lcv
 */
public class LARVAFrame extends JFrame implements ActionListener{
    Consumer <ActionEvent> myListener;
    
    public LARVAFrame(Consumer<ActionEvent> listener) {
        super();
        myListener= listener;
        this.setTitle("LARVA");
    }
    @Override
    public void actionPerformed(ActionEvent e) {        
        myListener.accept(e);
    }
    
    
}
