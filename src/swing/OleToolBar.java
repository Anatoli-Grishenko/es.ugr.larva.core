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
import java.awt.Font;
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

    public OleToolBar(OleApplication oapp, int hgap) {
        super();
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        fl.setHgap(hgap);
        setLayout(fl);

        dicComponents = new HashMap();
        parent = oapp;
    }

    public OleToolBar(OleApplication oapp, OleConfig olecfg) {
        super();
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        fl.setHgap(12);
        setLayout(fl);
        dicComponents = new HashMap();
        parent = oapp;
        Ole oTool = olecfg.getTab("ToolBar"), ocontent;
        OleButton obAux;
        String content, style, type;
        if (olecfg.getOptions().getOle("ToolBar").isEmpty()) {
            style = "flat";
            type = "text";
        } else {
            style = olecfg.getProperties().getOle("ToolBar").getString("style", "flat");
            type = olecfg.getProperties().getOle("ToolBar").getString("type", "text");
        }
        oTool.getFieldList();
        for (String stool : oTool.getFieldList()) {
            ocontent = oTool.getOle(stool);
            content = ocontent.getField("text");
            obAux = new OleButton(oapp, stool, content);
            switch (style) {
                case "regular":
                    obAux.setRegular();
                    break;
                case "flat":
                    obAux.setFlat();
                    break;
                case "extraflat":
                default:
                    obAux.setExtraFlat();
                    break;
            }
            if (type.equals("emoji")) {
                obAux.setFont(new Font("Arial", Font.BOLD, 20));
                obAux.setEmoji();
                obAux.setText(" " + obAux.getText().trim() + " ");
            }
            if (type.equals("icon")) {
                obAux.setIcon();
            }
            addButton(obAux);
        }
    }

    public List<String> getButtonList() {
        return new ArrayList<String>(dicComponents.keySet());
    }

    public OleButton getButton(String name) {
        return (OleButton) dicComponents.get(name);
    }

    public OleToolBar addButton(OleButton ob) {
        this.add(ob);
        dicComponents.put(ob.getCommand(), ob);
        this.validate();
        return this;
    }

    public OleToolBar removeButton(String name) {
        OleButton ob = getButton(name);
        this.remove(ob);
        dicComponents.remove(name);
        this.validate();
        return this;
    }

    public OleToolBar removeAllButtons() {
        for (String name : getButtonList()) {
            OleButton ob = getButton(name);
            this.remove(ob);
        }
        dicComponents.clear();
        return this;
    }
}
