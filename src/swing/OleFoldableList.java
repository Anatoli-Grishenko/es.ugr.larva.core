/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleFoldableList extends JPanel {
    
    Component parent;
    GridBagLayout gbl;
    GridBagConstraints gc;
    int npanes;
    
    public OleFoldableList(Component p) {
        parent = p;
        gbl = new GridBagLayout();
        gbl.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.0, Double.MIN_VALUE};
        gbl.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Double.MIN_VALUE};
        this.setLayout(gbl);
        npanes = 0;
        this.validate();
    }
    
    public void addFoldable(OleFoldablePane ofpP) {
        gc = new GridBagConstraints(0, npanes++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
        gc.gridx = 0;
        gc.gridy = npanes++;
        gc.anchor = GridBagConstraints.SOUTH;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.weighty = 0;
        gc.gridwidth = 1;
        gc.gridheight = 1;
        gc.ipadx=3;
        gc.ipady=3;
        gbl.setConstraints(ofpP, gc);
        super.add(ofpP);
        this.validate();
    }
    
    @Override
    public Component add(Component ofpP) {
        super.add(ofpP);
        gc = new GridBagConstraints(0, npanes++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
        gc.gridx = 0;
        gc.gridy = npanes++;
        gc.anchor = GridBagConstraints.SOUTH;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.weighty = 0;
        gc.gridwidth = 1;
        gc.gridheight = 1;
        gc.ipadx=3;
        gc.ipady=3;
        gbl.setConstraints(ofpP, gc);
        this.revalidate();
        return this;
    }
}
