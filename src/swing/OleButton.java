/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
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

    public OleButton(String command, String text, OleApplication oapp) {
        setActionCommand(command);
        setFocusPainted(true);
        setContentAreaFilled(false);
        setMargin(new Insets(0, 0, 0, 0));
        setText(text);
        this.setFont(new Font("Arial", Font.BOLD, 20));
        this.setToolTipText(command);        
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {         
        setContentAreaFilled(true);
        setBackground(Color.GRAY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
            setContentAreaFilled(false);
                setBackground(oapp.getBackground());
            }
        });
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
    }

}
