/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import agents.AgentReport;
import java.awt.BorderLayout;
import java.awt.Color;
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
    JButton mybOn, mybOff;
    JLabel mylLabel, mylLabel2;
    AgentReport myReport;
    
    public OleAgentTile(OleApplication parent, AgentReport report) {
        super();
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED) );
        myStatus=Status.OFF;
        myParent=parent;
        myReport = report;
        mylLabel = new JLabel("---");
        mylLabel2 = new JLabel("<html><i>"+myReport.getClassName()+"</i></html>");
        mybOn = new JButton(emojis.ACTIVATE);
        mybOn.setActionCommand("Activate "+myReport.getAgentName());
        mybOn.addActionListener(parent);
        mybOff = new JButton(emojis.DEACTIVATE);
        mybOff.setActionCommand("Deactivate "+myReport.getAgentName());
        mybOff.addActionListener(parent);
        this.add(mylLabel);
        this.add(mylLabel2);
        this.add(mybOn);
        this.add(mybOff);
    }

    public AgentReport getMyReport() {
        return myReport;
    }

    public void setMyReport(AgentReport myReport) {
        this.myReport = myReport;
    }
    
    public void showSummary() {
        
        if (myStatus==Status.OFF){
            mybOn.setEnabled(true);
            mybOff.setEnabled(false);
            mylLabel.setForeground(Color.BLACK);
        } else {
            mybOn.setEnabled(false);
            mybOff.setEnabled(true);
            mylLabel.setForeground(OleApplication.DodgerBlue);
        }
        mylLabel.setText(emojis.BLACKCIRCLE+" "+myReport.getAgentName());
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
        return myReport.getAgentName();
    }

    public Class getMyClass() {
        return myReport.getAgentClass();
    }
    
}
