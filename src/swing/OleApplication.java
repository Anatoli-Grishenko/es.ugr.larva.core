/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import data.OleConfig;
import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import tools.emojis;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public abstract class OleApplication extends OleFrame {

    JPanel pMain, pStatus;

    public OleApplication(OleConfig olecfg) {
        super(olecfg);
        oConfig = olecfg;
        setSize(800, 600);
        this.setPreferredSize(new Dimension(800, 600));
//        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    @Override
    public OleApplication init() {
        super.init();
        if (oConfig.getOptions().getFieldList().contains("Menu")) {
            this.setJMenuBar(new OleMenuBar(this, oConfig));
        }

//        if (oConfig.getOptions().getBoolean("FrameStatus", false)) {
//            pStatus = new JPanel();
//            pStatus.setLayout(new FlowLayout(FlowLayout.LEFT));
//            pStatus.setLayout(new BoxLayout(pStatus, BoxLayout.X_AXIS));
//            pStatus.setAlignmentX(LEFT_ALIGNMENT);
//            pStatus.setBackground(Color.DARK_GRAY);
//            pStatus.setForeground(Color.WHITE);
//            pStatus.setBorder(new EmptyBorder(new Insets(2, 2, 2, 2)));
//            pStatus.setPreferredSize(new Dimension(getWidth(), 64));
//            pStatus.setSize(new Dimension(getWidth(), 64));
//            pMain.setPreferredSize(new Dimension(getWidth(), getHeight() - pStatus.getHeight() - 
//                    this.getJMenuBar().getHeight()));
//            pMain.setSize(new Dimension(getWidth(), getHeight() - pStatus.getHeight() - 
//                    this.getJMenuBar().getHeight()));
//        } else {
//            pMain.setPreferredSize(new Dimension(getWidth(), getHeight()));
//            pMain.setSize(new Dimension(getWidth(), getHeight()));
//        }
        Container aux = this.getContentPane();
        aux.setLayout(new BorderLayout());

        pMain = new JPanel();
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.PAGE_AXIS));
        pMain.setBackground(Color.WHITE);
        pMain.setBorder(new EmptyBorder(0, 0, 0, 0));
        addLabel(pMain, " ", Color.BLACK);
        this.getContentPane().add(pMain, BorderLayout.CENTER);

        if (oConfig.getOptions().getBoolean("FrameStatus", false)) {
            pStatus = new JPanel();
            pStatus.setLayout(new BoxLayout(pStatus, BoxLayout.X_AXIS));
            pStatus.setBackground(Color.GRAY);
            pStatus.setBorder(new EmptyBorder(0, 0, 0, 0));
            addLabel(pStatus, "Ready", Color.BLACK);
            this.getContentPane().add(pStatus, BorderLayout.SOUTH);
        }
        this.pack();
        return this;
    }

    protected void addLabel(Container con, String s, Color col) {
        JLabel l = new JLabel(s, SwingConstants.LEFT);
        l.setForeground(col);
        con.add(l);
    }

    public LayoutManager defLayout(Container c) {
        LayoutManager lm;

//        lm = new FlowLayout(FlowLayout.LEFT);
//        ((FlowLayout) lm).setHgap(0);
//        ((FlowLayout) lm).setVgap(0);
        lm = new BoxLayout(c, BoxLayout.PAGE_AXIS);
        return lm;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        myActionListener(e);
    }

    @Override
    public abstract void itemStateChanged(ItemEvent e);

    @Override
    public abstract void myActionListener(ActionEvent e);

    @Override
    public abstract void myKeyListener(KeyEvent e);

    @Override
    public void keyTyped(KeyEvent e) {
        myKeyListener(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        myKeyListener(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        myKeyListener(e);
    }

    public void cleanStatus() {
        Graphics2D gpanel = (Graphics2D) pStatus.getGraphics();
        gpanel.setColor(pStatus.getBackground());
        gpanel.fillRect(0, 0, getWidth(), getHeight());
    }

    public void showStatus(String message) {
        if (pStatus != null) {
            cleanStatus();
            Graphics2D gpanel = (Graphics2D) pStatus.getGraphics();
            pStatus.removeAll();
            addLabel(pStatus, "   " + message, Color.BLACK);
            pStatus.validate();
        }
    }

    public void showInfo(String message) {
        if (pStatus != null) {
            cleanStatus();
            pStatus.removeAll();
            addLabel(pStatus, "   ", Color.BLACK);
            addLabel(pStatus, emojis.INFO, Color.BLUE);
            addLabel(pStatus, " " + message, Color.BLACK);
            pStatus.validate();
        }
    }

    public void showWarning(String message) {
        if (pStatus != null) {
            cleanStatus();
            pStatus.removeAll();
            addLabel(pStatus, "   ", Color.BLACK);
            addLabel(pStatus, emojis.WARNING, Color.ORANGE);
            addLabel(pStatus, " " + message, Color.BLACK);
            pStatus.validate();
        }
    }

    public void showError(String message) {
        if (pStatus != null) {
            cleanStatus();
            pStatus.removeAll();
            addLabel(pStatus, "   ", Color.BLACK);
            addLabel(pStatus, emojis.ERROR, Color.RED);
            addLabel(pStatus, " " + message, Color.BLACK);
            pStatus.validate();
        }
    }

}
