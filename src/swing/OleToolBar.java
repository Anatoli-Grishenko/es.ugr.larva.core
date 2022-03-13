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
import java.util.List;
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

    public OleToolBar(OleApplication oapp, OleConfig olecfg) {
        super();
        setLayout(new FlowLayout(FlowLayout.LEFT));
        dicComponents = new HashMap();
        Ole oTool = olecfg.getTab("ToolBar"), ocontent;
        OleButton obAux;
        String content, style, type;
        if (olecfg.getOptions().getOle("ToolBar").isEmpty()) {
            style="flat";
            type="text";
        } else {
            style=olecfg.getProperties().getOle("ToolBar").getString("style","flat");
            type=olecfg.getProperties().getOle("ToolBar").getString("type","text");
        }
        oTool.getFieldList();
        for (String stool : oTool.getFieldList()) {
            ocontent = oTool.getOle(stool);
            content = "";
//            if (!ocontent.getString("button","").equals("")) {
//                try {
//                    content = (String) emojis.class.getField(ocontent.getField("button")).get(content);
//                    content = content.trim();
//                } catch (Exception ex) {
//                    content = "X";
//                }
//            }else if (!ocontent.getString("text","").equals("")) {
//                content = ocontent.getField("text");
//            }
            content = ocontent.getField("text");
            obAux = new OleButton(oapp, stool, content);
            switch(style) {
                case "regular":
                    obAux.setRegular();
                    break;
                case "flat":
                default:
                    obAux.setFlat();
                    break;
            }
            if (type.equals("emoji")) {
                try{
                obAux.setText((String) emojis.class.getField(ocontent.getField("text")).get(content));
                obAux.setEmoji();
                } catch (Exception ex) {
                }
                obAux.setEmoji();
            }
            this.add(obAux);
            dicComponents.put(stool, obAux);
        }
    }
    
    public List<String> getButtonList() {
        return (List<String>) dicComponents.keySet();
    }
    
    public OleButton getButton(String name) {
        return (OleButton) dicComponents.get(name);
    }

}
