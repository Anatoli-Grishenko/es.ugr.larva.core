/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import data.Ole;
import data.Ole.oletype;
import data.OleConfig;
import java.awt.MenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleMenuBar extends JMenuBar {

    JMenu mMenu;
    JMenuItem miItem;
    OleFrame parent;

    public OleMenuBar(OleFrame of, OleConfig ocfg) {
        parent = of;
        this.removeAll();
        Ole oMenu = ocfg.getTab("Menu");
        for (String smenu : oMenu.getFieldList()) {
            if (oMenu.getFieldType(smenu).toUpperCase().equals(oletype.STRING.name())) {
                miItem = new JMenuItem(smenu);
               miItem.setActionCommand(oMenu.getField(smenu));
                miItem.addActionListener(parent);
                this.add(miItem);
            } else if (oMenu.getFieldType(smenu).toUpperCase().equals(oletype.OLE.name())) {
                this.add(addMenu(smenu, new OleConfig(oMenu.getOle(smenu))));
            }
        }
    }

    public JMenu addMenu(String name, OleConfig omenu) {
        Ole menuOptions;
        JMenu mAux = new JMenu(name);
        for (String smenu : omenu.getFieldList()) {
            if (omenu.getFieldType(smenu).toUpperCase().equals(oletype.STRING.name())) {
                miItem = new JMenuItem(smenu);
                miItem.setActionCommand(omenu.getField(smenu));
                miItem.addActionListener(parent);
                mAux.add(miItem);
            } else if (omenu.getFieldType(smenu).toUpperCase().equals(oletype.OLE.name())) {
                mAux.add(addMenu(smenu, new OleConfig(omenu.getOle(smenu))));
            }
        }
        return mAux;
    }
}
