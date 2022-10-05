/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class TelegramBackdoor extends OleFrame {

    JTextArea jtaBack;
    JTextField jtfInput;
    OleButton obSend;
    int sendH = 48;
    Consumer<String> callback;

    public TelegramBackdoor(String name, Consumer<String> c) {
        super(name);
        callback = c;
        this.setPreferredSize(new Dimension(500, 500));
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        this.getContentPane().setLayout(fl);
        this.getContentPane().removeAll();

        jtaBack = new JTextArea();
        jtaBack.setEditable(false);
        jtaBack.setWrapStyleWord(true);
        jtaBack.setCaretPosition(Math.max(jtaBack.getText().lastIndexOf("\n"), 0));

        Font f = this.getFont();
        f = new Font(Font.MONOSPACED, Font.PLAIN, f.getSize());
        jtaBack.setFont(f);
        jtaBack.setText("\n");
        JScrollPane jsPane = new JScrollPane(jtaBack);
        jsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jsPane.setPreferredSize(new Dimension(this.getPreferredSize().width, this.getPreferredSize().height - 100));

//        jtaBack = new JTextArea();
//        jtaBack.setEditable(false);
//        jtaBack.setFont(new Font("Free Mono Regular", Font.PLAIN, 12));
//        jtaBack.setPreferredSize(new Dimension(this.getPreferredSize().width, this.getPreferredSize().height - 100));
        jtfInput = new JTextField();
        obSend = new OleButton(this, "SENDTELE", "play_arrow");
//        jtaBack.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()-sendH));
//        jtfInput.setPreferredSize(new Dimension(this.getWidth() - sendH, sendH));
        obSend.setFlat();
        JButton jtbSend = new JButton("Send");
        jtbSend.setActionCommand("SendTELE");
        jtbSend.addActionListener(this);
        jtfInput.addActionListener(this);
        jtfInput.addKeyListener(this);
        jtfInput.setPreferredSize(new Dimension(this.getPreferredSize().width - 2 * jtbSend.getPreferredSize().width, sendH));
        jtfInput.setText("/help");
//        this.getContentPane().add(jtaBack);
        this.getContentPane().add(jsPane);
        this.getContentPane().add(jtfInput);
        this.getContentPane().add(jtbSend);
        this.getContentPane().validate();
//        this.pack();
//        this.repaint();
//        this.setVisible(false);
//        this.write("/help");
//        this.add(obSend);
    }

    @Override
    public void myActionListener(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "SendTELE":
                jtaBack.append(jtfInput.getText() + "\n");
                this.callback.accept(jtfInput.getText());
                break;
            default:
                jtaBack.append(e.getActionCommand() + "\n");
                this.callback.accept(e.getActionCommand());
        }
    }

    @Override
    public void myKeyListener(KeyEvent e) {
        SwingTools.Message("Tecla " + e.getKeyChar());
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public void write(String msg) {
        jtaBack.append(msg + "\n");
        jtaBack.setCaretPosition(Math.max(jtaBack.getText().lastIndexOf("\n"), 0));
    }
}
