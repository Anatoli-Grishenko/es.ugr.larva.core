/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import data.Ole;
import data.OleConfig;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import tools.emojis;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleToolBar extends JPanel {

    OleFrame parent;
    HashMap<String, Component> dicComponents;
    ArrayList<String> listComponents;

    public OleToolBar(OleApplication oapp, OleConfig olecfg) {
        super();
        setLayout(new FlowLayout(FlowLayout.LEFT));
//        setBorder(new EmptyBorder(0, 0, 0, 0));
//        setBackground(Color.DARK_GRAY);
        dicComponents = new HashMap();
        listComponents = new ArrayList();
        Ole oTool = olecfg.getTab("ToolBar"), ocontent;
        OleButton obAux;
        String content;
        oTool.getFieldList();
        for (String stool : oTool.getFieldList()) {
            ocontent = oTool.getOle(stool);
            content = "";
            if (!ocontent.getString("button","").equals("")) {
                try {
                    content = (String) emojis.class.getField(ocontent.getField("button")).get(content);
                    content = content.trim();
                } catch (Exception ex) {
                    content = "X";
                }
            }else if (!ocontent.getString("text","").equals("")) {
                content = ocontent.getField("text");
            }
            obAux = new OleButton(stool, content, oapp);
            obAux.addActionListener(oapp);
            this.add(obAux);
        }

    }

}
