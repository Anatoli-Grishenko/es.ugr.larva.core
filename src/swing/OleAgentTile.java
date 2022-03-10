/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import tools.emojis;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleAgentTile extends JPanel{
    public static enum Status {OFF, ON};
    final int width=150,height=10;
    OleApplication myParent;
    Status myStatus;
    String myName;
    Class myClass;
    JButton mybOn, mybOff;
    JLabel mylLabel, mylLabel2;
    
    public OleAgentTile(OleApplication parent, String name, Class c) {
        super();
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED) );
        myStatus=Status.OFF;
        myName = name;
        myParent=parent;
        myClass=c;
        mylLabel = new JLabel("---");
        mylLabel2 = new JLabel("<html><i>"+c.getSimpleName()+"</i></html>");
        mybOn = new JButton(emojis.ACTIVATE);
        mybOn.setActionCommand("Activate "+myName);
        mybOn.addActionListener(parent);
        mybOff = new JButton(emojis.DEACTIVATE);
        mybOff.setActionCommand("Deactivate "+myName);
        mybOff.addActionListener(parent);
        this.add(mylLabel);
        this.add(mylLabel2);
        this.add(mybOn);
        this.add(mybOff);
    }
    
    public void showSummary() {
        
        if (myStatus==Status.OFF){
            mybOn.setEnabled(true);
            mybOff.setEnabled(false);
            mylLabel.setForeground(OleApplication.Maroon);
        } else {
            mybOn.setEnabled(false);
            mybOff.setEnabled(true);
            mylLabel.setForeground(OleApplication.DodgerBlue);
        }
        mylLabel.setText(emojis.BLACKCIRCLE+" "+myName);
        this.validate();
    }
   
    
    public void doActivate() {
        myStatus = Status.ON;
        showSummary();
    }
    public void doDeactivate() {
        myStatus = Status.OFF;
        showSummary();
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public Class getMyClass() {
        return myClass;
    }

    public void setMyClass(Class myClass) {
        this.myClass = myClass;
    }
    
}
