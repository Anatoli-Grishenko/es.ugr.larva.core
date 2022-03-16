/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import tools.emojis;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleButton extends JButton {

    static final int PlainButton = 1;
    static final int RegularButton = 0;
    static final int IconButton = 2;
    static final int TextButton = 3;
    static final int EmojiButton = 3;
    String style, type, command;
    Color foreground;
    Component parent;

    public OleButton(ActionListener p, String command, String text) {
        super();
        parent = (Component) p;
        foreground = this.getForeground();
        setText(text);
        setRegular();        
        this.command = command;
        setActionCommand(this.command);
        setToolTipText(this.command);
        setFocusPainted(true);
        this.addActionListener(p);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseIn(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseOut(e);
            }
        });

    }

    public void hideButton() {
        this.setForeground(parent.getBackground());
    }

    public void unHideButton() {
        this.setForeground(foreground);
    }

    public void mouseIn(MouseEvent e) {
        if (getStyle().equals("flat")) {
            unHideButton();
            setContentAreaFilled(true);
            this.setBorderPainted(true);
        }
    }

    public void mouseOut(MouseEvent e) {
        if (getStyle().equals("flat")) {
            setContentAreaFilled(false);
            this.setBorderPainted(false);
        }
    }

    public OleButton setEmoji() {
        setType("emoji");
        setText(getText().trim());
        return this;
    }

    public OleButton setFlat() {
        setStyle("flat");
        return this;
    }

    public OleButton setRegular() {
        setStyle("regular");
        return this;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style=style;
        if (style.equals("flat")) {
            setMargin(new Insets(0, 0, 0, 0));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
        } else {
            setContentAreaFilled(true);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        if (type.equals("emoji")) {
            try {
                setText((String) emojis.class.getField(getText()).get(getText()));
            } catch (Exception ex) {
            }
        }
    }

}
