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
        setFocusPainted(false);
        setContentAreaFilled(true);
        setMargin(new Insets(1, 1, 1, 1));
//        addActionListener(oapp);
        setText(text);
        this.setFont(new Font("Arial", Font.BOLD, 20));
        this.setForeground(Color.WHITE);
        setBackground(Color.DARK_GRAY);
        this.setToolTipText(command);        
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
//                setForeground(Color.BLUE);
                setBackground(Color.GRAY);                
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setForeground(Color.WHITE);
                setBackground(Color.DARK_GRAY);
            }
        });
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
    }

}
