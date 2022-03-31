/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleRemote extends JDialog implements ActionListener {

    GridBagConstraints gc;
    OleButton obAux;
    OleApplication parentApp;
    String result;
    OleToolBar externalTB;
    JRootPane myPanel;
    int sizeButtons;

    public OleRemote(JFrame p, String title) {
        super(p, title, true);
        myPanel=this.getRootPane();
        myPanel.setLayout(new GridBagLayout());
        parentApp = (OleApplication) p;
        sizeButtons = 40;
        Layout();
    }

    public OleRemote(JFrame p, OleToolBar external) {
        parentApp = (OleApplication) p;
        externalTB = external;
        myPanel = external.getRootPane();
        parentApp = (OleApplication) p;
        myPanel.setLayout(new GridBagLayout());
        sizeButtons = 20;
        Layout();
    }

    public void Layout() {
        gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 1;
        gc.gridheight = 1;
        gc.weightx = 0.333;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.ipadx = 1;
        gc.ipady = 1;
        gc.gridx = 0;
        gc.gridy++;
        
//        obAux = new OleButton(parentApp, "Exit", "logout");
//        obAux.setIcon();
//        obAux.setPreferredSize(new Dimension(sizeButtons, sizeButtons));
//        obAux.addActionListener(this);
//        myPanel.add(obAux, gc);

        gc.gridx = 0;
        gc.gridy++;
        obAux = new OleButton(parentApp, "LEFT", "arrow_circle_left");
        obAux.setIcon();
        obAux.setPreferredSize(new Dimension(sizeButtons, sizeButtons));
        obAux.addActionListener(this);
        myPanel.add(obAux, gc);

        gc.gridx++;
        obAux = new OleButton(parentApp, "RIGHT", "arrow_circle_right");
        obAux.setIcon();
        obAux.setPreferredSize(new Dimension(sizeButtons, sizeButtons));
        obAux.addActionListener(this);

        obAux = new OleButton(parentApp, "UP", "arrow_circle_up");
        obAux.setIcon();
        obAux.setPreferredSize(new Dimension(sizeButtons, sizeButtons));
        obAux.addActionListener(this);
        myPanel.add(obAux, gc);

        gc.gridx++;
        obAux = new OleButton(parentApp, "DOWN", "arrow_circle_down");
        obAux.setIcon();
        obAux.setPreferredSize(new Dimension(sizeButtons, sizeButtons));
        obAux.addActionListener(this);
        myPanel.add(obAux, gc);
        myPanel.add(obAux, gc);

        gc.gridx++;
        obAux = new OleButton(parentApp, "MOVE", "vertical_align_top");
        obAux.setIcon();
        obAux.setPreferredSize(new Dimension(sizeButtons, sizeButtons));
        obAux.addActionListener(this);
        myPanel.add(obAux, gc);

        gc.gridx = 0;
        gc.gridy++;
        obAux = new OleButton(parentApp, "UP", "arrow_circle_up");
        obAux.setIcon();
        obAux.setPreferredSize(new Dimension(sizeButtons, sizeButtons));
        obAux.addActionListener(this);
        myPanel.add(obAux, gc);

        gc.gridx++;
        obAux = new OleButton(parentApp, "DOWN", "arrow_circle_down");
        obAux.setIcon();
        obAux.setPreferredSize(new Dimension(sizeButtons, sizeButtons));
        obAux.addActionListener(this);
        myPanel.add(obAux, gc);
        
        gc.gridx = 0;
        gc.gridy++;
        obAux = new OleButton(parentApp, "RECHARGE", "electrical_services");
        obAux.setIcon();
        obAux.setPreferredSize(new Dimension(sizeButtons, sizeButtons));
        obAux.addActionListener(this);
        myPanel.add(obAux, gc);

        gc.gridx++;
        obAux = new OleButton(parentApp, "CAPTURE", "person_pin_circle");
        obAux.setIcon();
        obAux.setPreferredSize(new Dimension(sizeButtons, sizeButtons));
        obAux.addActionListener(this);
        myPanel.add(obAux, gc);
        this.pack();

    }

    public String run() {
        this.setVisible(true);
        return this.result;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        result = e.getActionCommand();
        this.setVisible(false);
    }

}
