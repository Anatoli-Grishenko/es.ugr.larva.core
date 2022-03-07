/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import data.Ole;
import data.OleConfig;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.ObjectInputFilter.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import tools.emojis;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public abstract class Ole3DApplication extends OleApplication {

    Ole3DPane osDiagram;

    public Ole3DApplication(OleConfig olecfg) {
        super(olecfg);
        oConfig = olecfg;
        init3D();
        setVisible(true);
    }

    public Ole3DApplication init3D() {
        if (oConfig.getOptions().getFieldList().contains("Menu")) {
            this.setJMenuBar(new OleMenuBar(this, oConfig));
        }

        Container mainPane = this.getContentPane();
        mainPane.removeAll();
        mainPane.setLayout(new BorderLayout());

        pMain = new JPanel();
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.X_AXIS));        
        mainPane.add(pMain, BorderLayout.CENTER);
        if (oConfig.getOptions().getFieldList().contains("ToolBar")) {
            mainPane.add(new OleToolBar(this, oConfig), BorderLayout.PAGE_START);
        }
        if (oConfig.getOptions().getBoolean("FrameStatus", false)) {
            pStatus = new JPanel();
            pStatus.setLayout(new FlowLayout(FlowLayout.LEFT));
            addLabel(pStatus, "Ready");
            mainPane.add(pStatus, BorderLayout.PAGE_END);
        }
        getMainPanel().removeAll();
        getMainPanel().setLayout(new BorderLayout());

        opDiagram = new OleDrawPane() {
            @Override
            public void OleDraw(Graphics2D g) {
                Draw(g);
            }
        };

//        opDiagram.setBorder(new EmptyBorder(0, 0, 0, 0));
//        opDiagram.setPreferredSize(getMainPanel().getPreferredSize());
        opDiagram.setBackground(Color.BLACK);
        opDiagram.setForeground(Color.WHITE);

        osDiagram = new Ole3DPane(opDiagram);

//        osDiagram.setBorder(new EmptyBorder(0, 0, 0, 0));
        osDiagram.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        osDiagram.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        osDiagram.setCamdistance(1000);
        getMainPanel().add(osDiagram, BorderLayout.CENTER);
        getMainPanel().validate();
        this.pack();
        return this;
    }

    public Ole3DPane get3DPane() {
        return osDiagram;
    }


    @Override
    public void Draw(Graphics2D g) {
        if (osDiagram != null) {;
            osDiagram.Draw3D(g);
        }
    }
}
