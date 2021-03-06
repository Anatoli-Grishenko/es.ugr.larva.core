/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleFoldablePane extends JPanel implements ActionListener {

    OleButton obControl;
    JLabel jlHeader;
    OleFoldableList jpContent;
    boolean folded;
//    String fold = "do_disturb_on", unfold = "control_point";
    String fold = "-", unfold = "+";

    public OleFoldablePane(Component parent, JLabel header) {
        super();
        jlHeader = header;
        obControl = new OleButton(this, "fold", unfold);
        obControl.setFlat();
        obControl.setType("text");
        jpContent = new OleFoldableList(this);
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        GridBagConstraints gc = new GridBagConstraints();
        gc.ipadx=3;
        gc.ipady=3;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 0.9;
        this.add(jlHeader, gc);
        gc.gridx++;
        gc.anchor = GridBagConstraints.CENTER;
        gc.weightx = 0.1;
        this.add(obControl, gc);
        gc.gridx = 0;
        gc.gridy++;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        this.add(jpContent, gc);
        doDeactivate();
        doFold();
        this.validate();
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                obControl.unHideButton();
                doActivate();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                obControl.hideButton();
                doDeactivate();
            }
        });

    }

    public OleFoldableList getFoldablePane() {
        return this.jpContent;
    }

    public void setFoldablePane(OleFoldableList p) {
        jpContent = p;
        jpContent.validate();
    }

    public JLabel getHeader() {
        return this.jlHeader;
    }

    public void doFold() {
        folded = true;
        obControl.setText(unfold);
        obControl.setActionCommand("unfold");
        jpContent.setVisible(false);
        jpContent.revalidate();
    }

    public void doUnfold() {
        folded = false;
        obControl.setText(fold);
        obControl.setActionCommand("fold");
        jpContent.setVisible(true);        
        jpContent.revalidate();
    }

    public void doActivate() {
        this.setBorder(BorderFactory.createLineBorder(OleApplication.DodgerBlue));
    }

    public void doDeactivate() {
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("fold")) {
            doFold();
        } else if (e.getActionCommand().equals("unfold")) {
            doUnfold();
        }
    }

}
