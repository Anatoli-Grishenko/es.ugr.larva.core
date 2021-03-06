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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
public class OleAgentTile extends OleFoldablePane {

    public static enum Status {
        OFF, ON
    };
    final int width = 150, height = 10, icon = 24;
    OleApplication myParent;
    Status myStatus;
    OleButton mybOn, mybOff, mybSwitch, mybConfig;
    JLabel mylLabel, mylLabel2;
    AgentReport myReport;
    OlePerformeter olpTime, olpInbox, olpOutbox;
    OleToolBar oltbMain, oltbExternal;
    boolean showPerformance = false, hasconfig = false;

    public OleAgentTile(OleApplication parent, AgentReport report, boolean hasConfigFile) {
        super(parent, new JLabelRobot(parent, report.getAgentName()));

        myStatus = Status.OFF;
        myParent = parent;
        myReport = report;
        mylLabel = this.getHeader();
        mylLabel2 = new JLabel();
        if (myReport.getOwnerName() == null) {
            mylLabel2.setText("Unidentified");
            mylLabel2.setIcon(myParent.getIconSet().getRegularIcon("no_accounts", new Dimension(icon, icon)));
        } else {
            mylLabel2.setText(myReport.getOwnerName().substring(0, 10));
            mylLabel2.setIcon(myParent.getIconSet().getRegularIcon("account_circle", new Dimension(icon, icon)));
        }
        mybOn = new OleButton(parent, "Activate " + myReport.getAgentName(), "play_arrow");
        mybOn.setExtraFlat();
        mybOn.setBorderPainted(true);
        mybOn.setContentAreaFilled(true);
        mybOn.setIcon(new Dimension(icon, icon));

        mybOff = new OleButton(parent, "Deactivate " + myReport.getAgentName(), "eject");
        mybOff.setExtraFlat();
        mybOff.setBorderPainted(true);
        mybOff.setContentAreaFilled(true);
        mybOff.setIcon(new Dimension(icon, icon));
        this.hasconfig = hasConfigFile;
        if (this.hasconfig) {
            mybConfig = new OleButton(parent, "Configure " + myReport.getAgentName(), "settings");
            mybConfig.setExtraFlat();
            mybConfig.setBorderPainted(true);
            mybConfig.setContentAreaFilled(true);
            mybConfig.setIcon(new Dimension(icon, icon));
        }
        if (showPerformance) {
            olpTime = new OlePerformeter(this, 100, icon, 2000);
            olpInbox = new OlePerformeter(this, 100, icon, 5);
            olpOutbox = new OlePerformeter(this, 100, icon, 5);
        }
        OleFoldableList ofpAgent = new OleFoldableList(this);
        this.getFoldablePane().add(ofpAgent);
        this.getFoldablePane().add(mylLabel2);

        oltbMain = new OleToolBar(parent, 5);
        oltbMain.setPreferredSize(new Dimension(100, 24));
        oltbExternal = new OleToolBar(parent, 5);
        oltbExternal.setPreferredSize(new Dimension(100, 24));
        oltbMain.addButton(mybOn);
        oltbMain.addButton(mybOff);
        if (hasconfig) {
            oltbMain.addButton(mybConfig);
        }

        this.getFoldablePane().add(oltbMain);
        this.getFoldablePane().add(oltbExternal);

        if (showPerformance) {
            JLabel jlAux;
            jlAux = new JLabel("CPU Load");
            jlAux.setIcon(myParent.getIconSet().getRegularIcon("speed", new Dimension(icon, icon)));
            this.getFoldablePane().add(jlAux);
            this.getFoldablePane().add(olpTime);
            jlAux = new JLabel("Messages IN");
            jlAux.setIcon(myParent.getIconSet().getRegularIcon("message", new Dimension(icon, icon)));
            this.getFoldablePane().add(jlAux);
            this.getFoldablePane().add(olpInbox);
            jlAux = new JLabel("Messages OUT");
            jlAux.setIcon(myParent.getIconSet().getRegularIcon("comment", new Dimension(icon, icon)));
            this.getFoldablePane().add(jlAux);
            this.getFoldablePane().add(olpOutbox);
        }
        this.validate();
    }

    public AgentReport getMyReport() {
        return myReport;
    }

    public void setMyReport(AgentReport myReport) {
        this.myReport = myReport;
    }

    public void showSummary() {

        if (myStatus == Status.OFF) {
            mybOn.setEnabled(true);
            mybOff.setEnabled(false);
            if (hasconfig) {
                this.mybConfig.setEnabled(true);
            }
            mylLabel.setForeground(Color.BLACK);
        } else {
            mybOn.setEnabled(false);
            mybOff.setEnabled(true);
            if (hasconfig) {
                this.mybConfig.setEnabled(false);
            }
            mylLabel.setForeground(OleApplication.DodgerBlue);
        }
        mylLabel.setText(myReport.getAgentName());
        this.validate();
    }

    public void doActivateAgent() {
        myStatus = Status.ON;
        showSummary();
    }

    public void doDeactivateAgent() {
        myStatus = Status.OFF;
        showSummary();
    }

    public String getMyName() {
        return myReport.getAgentName();
    }

    public Class getMyClass() {
        return myReport.getAgentClass();
    }

    public void updateReportReadings() {
        if (showPerformance) {
            olpTime.pushData(myReport.getLastCycle());
            olpInbox.pushData(myReport.getInBox());
            olpOutbox.pushData(myReport.getOutBox());
            myReport.clearData();
        }
    }

    public void updateReport() {
        if (myReport.getOwnerName() == null) {
            mylLabel2.setText(" Unidentified");
            mylLabel2.setIcon(myParent.getIconSet().getRegularIcon("no_accounts", new Dimension(icon, icon)));
            mylLabel2.validate();
        } else {
            mylLabel2.setText(myReport.getOwnerName().substring(0, 10));
            mylLabel2.setIcon(myParent.getIconSet().getRegularIcon("account_circle", new Dimension(icon, icon)));
            mylLabel2.validate();
        }
        this.validate();
    }

    public OleToolBar getExternalToolBar() {
        return this.oltbExternal;
    }
}

class JLabelRobot extends JLabel {

    JLabelRobot(OleApplication parent, String text) {
        super(text);
        setIcon(parent.getIconSet().getRegularIcon("adb", new Dimension(24, 24)));
    }
}

///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package swing;
//
//import agents.AgentReport;
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.LayoutManager;
//import javax.swing.BorderFactory;
//import javax.swing.Box;
//import javax.swing.BoxLayout;
//import javax.swing.JButton;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.border.BevelBorder;
//import tools.emojis;
//
///**
// *
// * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
// */
//public class OleAgentTile extends JPanel{
//    public static enum Status {OFF, ON};
//    final int width=150,height=10;
//    OleApplication myParent;
//    Status myStatus;
//    JButton mybOn, mybOff,mybSwitch;
//    JLabel mylLabel, mylLabel2;
//    AgentReport myReport;
//    OlePerformeter olpTime, olpInbox,olpOutbox;
//    
//    public OleAgentTile(OleApplication parent, AgentReport report) {
//        super();
//        
//        this.setLayout(new GridBagLayout());
//        GridBagConstraints gc = new GridBagConstraints();
//        gc.gridx=0;
//        gc.gridy=0;
//        gc.gridwidth=1;
//        gc.gridheight=1;
//        
////        this.setLayout(new FlowLayout(FlowLayout.CENTER));
////        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
//        this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED) );
//        myStatus=Status.OFF;
//        myParent=parent;
//        myReport = report;
//        mylLabel = new JLabel("---");
//        mylLabel2 = new JLabel("<html><i>"+myReport.getClassName()+"</i></html>");
//        mybOn = new JButton(emojis.ACTIVATE);
//        mybOn.setActionCommand("Activate "+myReport.getAgentName());
//        mybOn.addActionListener(parent);
//        mybOff = new JButton(emojis.DEACTIVATE);
//        mybOff.setActionCommand("Deactivate "+myReport.getAgentName());
//        mybOff.addActionListener(parent);
//        mybSwitch = new JButton(emojis.DEACTIVATE);
//        mybSwitch.setActionCommand("Deactivate "+myReport.getAgentName());
//        mybSwitch.addActionListener(parent);
//        olpTime = new OlePerformeter(this,100,26, 2000);
//        olpInbox = new OlePerformeter(this,100,26, 5);
//        olpOutbox = new OlePerformeter(this,100,26, 5);
//        this.add(mylLabel, gc);
//        gc.gridy++;
//        this.add(mylLabel2,gc);
//        gc.gridy++;
//        this.add(mybOn, gc);
//        gc.gridy++;
//        this.add(mybOff, gc);
//        gc.gridy++;
//        this.add(olpTime, gc);
//        gc.gridy++;
//        this.add(olpInbox, gc);
//        gc.gridy++;
//        this.add(olpOutbox, gc);
////        this.add(new JLabel("Luisillo"), gc);
//    }
//
//    public AgentReport getMyReport() {
//        return myReport;
//    }
//
//    public void setMyReport(AgentReport myReport) {
//        this.myReport = myReport;
//    }
//    
//    public void showSummary() {
//        
//        if (myStatus==Status.OFF){
//            mybOn.setEnabled(true);
//            mybOff.setEnabled(false);
//            mylLabel.setForeground(Color.BLACK);
//        } else {
//            mybOn.setEnabled(false);
//            mybOff.setEnabled(true);
//            mylLabel.setForeground(OleApplication.DodgerBlue);
//        }
//        mylLabel.setText(emojis.BLACKCIRCLE+" "+myReport.getAgentName());
//        this.validate();
//    }
//   
//    
//    public void doActivate() {
//        myStatus = Status.ON;
//        showSummary();
//    }
//    public void doDeactivate() {
//        myStatus = Status.OFF;
//        showSummary();
//    }
//
//    public String getMyName() {
//        return myReport.getAgentName();
//    }
//
//    public Class getMyClass() {
//        return myReport.getAgentClass();
//    }
//    
//    public void updateReportReadings() {
//        olpTime.pushData(myReport.getLastCycle());
//        olpInbox.pushData(myReport.getInBox());
//        olpOutbox.pushData(myReport.getOutBox());
//        myReport.clearData();
//    }
//}
