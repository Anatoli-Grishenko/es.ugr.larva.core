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
public class OleFoldable extends JPanel implements ActionListener {

    OleButton obControl;
    JLabel jlHeader;
    JPanel jpContent;
    boolean folded;
    String fold = "-", unfold = "+";

    public OleFoldable(Component parent, JLabel header) {
        super();
        jlHeader = header;
        obControl = new OleButton(this, "fold", unfold);
        obControl.setFlat();
        jpContent = new JPanel();
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        this.add(jlHeader, gc);
        gc.gridx++;
        gc.anchor = GridBagConstraints.EAST;
        obControl.hideButton();
        this.add(obControl, gc);
        gc.gridx = 0;
        gc.gridy++;
        gc.anchor = GridBagConstraints.WEST;
        this.add(jpContent, gc);
        fold();
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
//                obControl.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                obControl.hideButton();
//                obControl.setVisible(false);
            }
        });

    }

    public JPanel getFoldabelPane() {
        return this.jpContent;
    }

    public void fold() {
        folded = true;
        obControl.setText(unfold);
        obControl.setActionCommand("unfold");
        jpContent.setVisible(false);
    }

    public void unfold() {
        folded = false;
        obControl.setText(fold);
        obControl.setActionCommand("fold");
        jpContent.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("fold")) {
            fold();
        } else if (e.getActionCommand().equals("unfold")) {
            unfold();
        }
    }

}
