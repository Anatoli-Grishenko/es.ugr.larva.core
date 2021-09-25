/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class MyPlainButton extends JButton {

    String image;

    public MyPlainButton(String name, String image, LARVAFrame myFrame) {
        super();
        setActionCommand(name);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setMargin(new Insets(0, 0, 0, 0));
        this.image = image;
        on();
        addActionListener(myFrame);
        if (image.length() > 0) {
            setIcon(SwingTools.toIcon("./images/neg/neg-" + image, 32, 32));
            setRolloverIcon(SwingTools.toIcon("./images/blue/blue-" + image, 32, 32));
            setPressedIcon(SwingTools.toIcon("./images/blue/blue-" + image, 32, 32));
            setDisabledIcon(SwingTools.toIcon("./images/black/" + image, 32, 32));
        } else {
            setText(name);
        }
        this.setToolTipText(name);

    }

    public MyPlainButton off() {
        this.setEnabled(false);
        return this;
    }

    public MyPlainButton on() {
        this.setEnabled(true);
        return this;
    }

}
