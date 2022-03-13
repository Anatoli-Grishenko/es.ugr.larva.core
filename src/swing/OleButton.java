/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleButton extends JButton {

    static final int PlainButton = 1;
    static final int RegularButton = 0;
    static final int IconButton = 2;
    String style, type, text;
    Color foreground;
    Component parent;

    public OleButton(ActionListener p, String command, String text) {
        super();
        parent = (Component) p;
        foreground = this.getForeground();
        this.text = text;
        setText(text);
        type = "text";
        style = "regular";
        setActionCommand(command);
        setToolTipText(command);
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
//        this.setVisible(false);
        this.setForeground(parent.getBackground());
//        this.setText("   ");
    }

    public void unHideButton() {
//        this.setVisible(true);
        this.setForeground(foreground);
//        this.setText(text);
    }

    public void mouseIn(MouseEvent e) {
        if (style.equals("flat")) {
//            this.setFocusPainted(true);
            unHideButton();
            setContentAreaFilled(true);
            this.setBorderPainted(true);
        }
    }

    public void mouseOut(MouseEvent e) {
        if (style.equals("flat")) {
//            this.setFocusPainted(false);
            setContentAreaFilled(false);
            this.setBorderPainted(false);
        }
    }

    public OleButton setEmoji() {
        type = "emoji";
        setText(" " + getText().trim() + " ");
        setFont(new Font("Arial", Font.BOLD, 20));
        return this;
    }

    public OleButton setFlat() {
        style = "flat";
        setMargin(new Insets(0, 0, 0, 0));
        setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        return this;
    }

    public OleButton setRegular() {
        style = "regular";
        setContentAreaFilled(true);
//        setMargin(new Insets(1, 1, 1, 1));
//        setFocusPainted(true);
//        this.setBorderPainted(true);
        return this;
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
    }

}
