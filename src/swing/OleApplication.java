/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

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
public abstract class OleApplication extends OleFrame {

    JPanel pMain, pStatus, pToolBar;
    JProgressBar pbMain;
    JLabel lMain, lProgress;
    OleFrame ofProgress;
    JTextArea jtaProgress;
    boolean debug;
    HashMap<String,Component> dicComponents;
    ArrayList<String> listComponents;

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

        Container aux = this.getContentPane();
        aux.setLayout(new BorderLayout());

        pMain = new JPanel();
        pMain.setLayout(new BoxLayout(pMain, BoxLayout.X_AXIS));
        pMain.setBackground(Color.WHITE);
        pMain.setBorder(new EmptyBorder(0, 0, 0, 0));
//        addLabel(pMain, " ", Color.BLACK);
        this.getContentPane().add(pMain, BorderLayout.CENTER);

        if (oConfig.getOptions().getBoolean("FrameStatus", false)) {
            pStatus = new JPanel();
            pStatus.setLayout(new FlowLayout(FlowLayout.LEFT));
//            pStatus.setLayout(new BoxLayout(pStatus, BoxLayout.X_AXIS));
            pStatus.setBackground(Color.GRAY);
            pStatus.setBorder(new EmptyBorder(0, 0, 0, 0));
            addLabel(pStatus, "Ready", Color.BLACK);
            this.getContentPane().add(pStatus, BorderLayout.SOUTH);
        }
        this.pack();
        return this;
    }

    public JPanel getMainPanel() {
        return this.pMain;
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
        SwingTools.doSwingWait(() -> {
            Graphics2D gpanel = (Graphics2D) pStatus.getGraphics();
            gpanel.setColor(pStatus.getBackground());
            gpanel.fillRect(0, 0, getWidth(), getHeight());
            pStatus.removeAll();
        });
    }

    public void showStatus(String message) {
        if (pStatus != null) {
            cleanStatus();
            addLabel(pStatus, "   " + message, Color.BLACK);
            pStatus.validate();
        }
    }

    public void showInfo(String message) {
        if (pStatus != null) {
            cleanStatus();
            addLabel(pStatus, "   ", Color.BLACK);
            addLabel(pStatus, emojis.INFO, Color.BLUE);
            addLabel(pStatus, " " + message, Color.BLACK);
            pStatus.validate();
        }
    }

    public void showWarning(String message) {
        if (pStatus != null) {
            cleanStatus();
            addLabel(pStatus, "   ", Color.BLACK);
            addLabel(pStatus, emojis.WARNING, Color.ORANGE);
            addLabel(pStatus, " " + message, Color.BLACK);
            pStatus.validate();
        }
    }

    public void showError(String message) {
        if (pStatus != null) {
            cleanStatus();
            addLabel(pStatus, "   ", Color.BLACK);
            addLabel(pStatus, emojis.ERROR, Color.RED);
            addLabel(pStatus, " " + message, Color.BLACK);
            pStatus.validate();
        }
    }

    public void showProgressFrame(String what, int value, int max) {
        closeProgress("");
        ofProgress = new OleFrame(this.getTitle()) {
            @Override
            public void itemStateChanged(ItemEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void myActionListener(ActionEvent e) {
                if(e.getActionCommand().equals("Colse")) {
                    dispose();
                }
            }

            @Override
            public void myKeyListener(KeyEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void keyTyped(KeyEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void keyPressed(KeyEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void keyReleased(KeyEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        Container ofContent = ofProgress.getContentPane();
//        ofContent.setLayout(new FlowLayout(FlowLayout.LEFT));
        ofContent.setLayout(new BoxLayout(ofContent,BoxLayout.Y_AXIS));
        ofContent.setPreferredSize(new Dimension(500, 100));
        pbMain = new JProgressBar(0, max);
        pbMain.setSize(new Dimension(500,25));
        pbMain.setValue(value);
        lMain = new JLabel(what);
        lMain.setForeground(Color.BLACK);
        jtaProgress = new JTextArea();
        jtaProgress.setRows(5);
        ofContent.add(lMain);
        ofContent.add(pbMain);
        ofContent.add(new JScrollPane(jtaProgress));
//        ofContent.add(lMain, BorderLayout.PAGE_START);
//        ofContent.add(pbMain, BorderLayout.LINE_START);
//        ofContent.add(new JScrollPane(jtaProgress), BorderLayout.PAGE_END);
        ofContent.validate();
        ofProgress.pack();
        ofProgress.setVisible(true);
        debug = false;
        if (debug) {
            debug = Confirm("Start " + what);
        }
//        Info("START " + what);
    }

    public void showProgress(String what, int value, int max) {
        closeProgress("");
        cleanStatus();
        SwingTools.doSwingWait(() -> {
            pbMain = new JProgressBar(0, max);
            pbMain.setSize(new Dimension((int) (pStatus.getPreferredSize().getWidth() / 2), (int) (pStatus.getPreferredSize().getHeight() * 3 / 4)));
            pbMain.setValue(value);
            lMain = new JLabel(what);
            lMain.setForeground(Color.BLACK);
            lProgress = new JLabel("Starting");
            lProgress.setForeground(Color.BLACK);
            pStatus.add(lMain);
            pStatus.add(pbMain);
            pStatus.add(lProgress);
            pStatus.validate();
            debug = false;
            if (debug) {
                debug = Confirm("Start " + what);
            }
        });
    }

    public void showProgress(String what, int value) {
        SwingTools.doSwingWait(() -> {
            String toadd = what; //"(" + pbMain.getValue() + "/" + pbMain.getMaximum() + ") " + what;
            if (ofProgress == null) {
                lProgress.setText(toadd);
            } else {
                jtaProgress.append(toadd+"\n");
            }

            if (value <= pbMain.getMaximum()) {
                pbMain.setValue(value);
//        pStatus.validate();
                if (debug) {
                    debug = Confirm("Continue " + what);
                }
            } else {
                closeProgress(what);
            }
        });
    }

    public void showProgress(String what) {
        SwingTools.doSwingWait(() -> {
            showProgress(what, pbMain.getValue() + 1);

        });
    }

    public void closeProgress(String what) {
//        if (ofProgress != null) {
//            ofProgress.dispose();
//            ofProgress = null;
//        }
    }
    
    public void addTaskBar() {
        dicComponents = new HashMap();
        listComponents = new ArrayList();
    }
    
    public void addToTaskBar(String command, Component c) {
        listComponents.add(command);
        dicComponents.put(command,c);
    }
    
    public void showTaskBar() {
        this.cleanStatus();
        for (String s : listComponents) {
            pStatus.add(dicComponents.get(s));
        }
    }
    
    public Component getTaskBarItem(String item) {
        return dicComponents.get(item);
    }

}
