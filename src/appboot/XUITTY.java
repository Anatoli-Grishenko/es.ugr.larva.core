/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appboot;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class XUITTY extends JEditorPane implements KeyListener {

    public static final String windowFrames[] = {"┌┐└┘─│┬┴┼┤├▄", "++++-|", "▁▂▃▄▅▆▇█", "▏▎▍▌▋▊▉█"};
    protected Consumer<KeyEvent> keyListener;
    
    public void textColor(int white) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setText(HTMLColor htmlColor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setKeyListener(Consumer <KeyEvent> kl) {
        keyListener = kl;
    }
    @Override
    public void keyTyped(KeyEvent e) {
        if (keyListener != null) {
            keyListener.accept(e);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static enum HTMLStyle {
        PLAIN, BOLD, ITALIC
    };

    public static enum HTMLColor {
        Orange, Green, Blue, Yellow, Purple, Red, Brown, Gray, LightGray, DarkGray, DarkSeaGreen, DodgerBlue, White, Black
    }
    Container cXui;
    String matrix[][];
    int width, height, cursorx, cursory;
    String body;
    Font f;
    HTMLColor color;
    String fill = "&nbsp;";
    boolean bold, italic;

    public XUITTY() {
        super();
        f = new Font("Monospaced", Font.PLAIN, 18);
        this.setFont(f);
        this.setBackground(Color.BLACK);
        this.setForeground(Color.WHITE);
        this.textColor(HTMLColor.White);
        this.setContentType("text/html");
        this.setEditable(false);
        this.setText("");
    }

    public int getXUIWidth() {
        return width;
    }

    public int getXUIHeight() {
        return height;
    }

    public void init(Container c) {
        cXui = c;
        this.setPreferredSize(c.getSize());
        cXui.removeAll();
        cXui.add(this);
        cXui.validate();
        bold = false;
        italic = false;
        width = this.getPreferredSize().width / f.getSize() * 3 / 2;
        height = this.getPreferredSize().height / f.getSize() - 1;
        matrix = new String[height][width];
        clearScreen();
        this.setText("");
        body = "";
    }

    public void render() {
        setText(toString());
    }

    public XUITTY setBold(boolean b) {
        bold = b;
        return this;
    }

    public XUITTY setItalic(boolean i) {
        italic = i;
        return this;
    }

    public void setPos(int x, int y, String c) {
        if (0 <= x && x < getWidth() && 0 <= y && y < getHeight()) {
            if (c.equals(" ")) {
                c = fill;
            }
            if (bold) {
                c = "<b>" + c + "</b>";
            }
            if (italic) {
                c = "<i>" + c + "</i>";
            }
            c = "<font color=\"" + color.name().toLowerCase() + "\">" + c + "</font>";
            matrix[y][x] = c;
        }
    }

    public XUITTY setCursorXY(int x, int y) {
        cursorx = x;
        cursory = y;
        return this;
    }

    public XUITTY clearScreen() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                matrix[y][x] = fill;
            }
        }
        setCursorXY(0, 0);
        body = "";
        return this;
    }

    public XUITTY textColor(HTMLColor c) {
        color = c;
        return this;
    }

    public XUITTY insertText(String s) {
        for (int i = 0; i < s.length() && cursorx < width; i++) {
            setPos(cursorx++, cursory, "" + s.charAt(i));
        }
        return this;
    }

    public XUITTY newline() {
        cursorx = 0;
        cursory++;
        return this;
    }

    public XUITTY print(String s) {
        return insertText(s);
    }

    public XUITTY println(String s) {
        print(s);
        return newline();
    }

    public String toString() {
        String res = "";
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                res += matrix[y][x];
            }
            res += "<br>";
        }
        return res;
    }

    public XUITTY doRectangle(int x, int y, int w, int h) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                setCursorXY(x + i, y + j);
                print(" ");
            }
        }
        print("");
        return this;
    }

    public XUITTY doRectangleFrame(int x, int y, int w, int h) {
        doRectangle(x, y, w, h);
        doFrame(x, y, x + w - 1, y + h - 1);
        return this;
    }

    public XUITTY doFrame(int x, int y, int x2, int y2) {
        for (int i = y; i <= y2; i++) {
            setCursorXY(x, i);
            print("" + this.windowFrames[0].charAt(5));
            setCursorXY(x2, i);
            print("" + this.windowFrames[0].charAt(5));
        }
        for (int j = x; j <= x2; j++) {
            setCursorXY(j, y);
            print("" + this.windowFrames[0].charAt(4));
            setCursorXY(j, y2);
            print("" + this.windowFrames[0].charAt(4));
        }

        setCursorXY(x, y);
        print("" + this.windowFrames[0].charAt(0));
        setCursorXY(x2, y);
        print("" + this.windowFrames[0].charAt(1));
        setCursorXY(x, y2);
        print("" + this.windowFrames[0].charAt(2));
        setCursorXY(x2, y2);
        print("" + this.windowFrames[0].charAt(3));

        return this;
    }

    public XUITTY doGrid(int x, int y, int x2, int y2) {
        for (int i = y; i <= y2; i++) {
            setCursorXY(x, i);
            print("" + this.windowFrames[0].charAt(5));
            setCursorXY(x2, i);
            print("" + this.windowFrames[0].charAt(5));
        }
        for (int j = x; j <= x2; j++) {
            setCursorXY(j, y);
            print("" + this.windowFrames[0].charAt(4));
            setCursorXY(j, y2);
            print("" + this.windowFrames[0].charAt(4));
        }
        setCursorXY(x, y);
        print("" + this.windowFrames[0].charAt(8));
        setCursorXY(x2, y);
        print("" + this.windowFrames[0].charAt(8));
        setCursorXY(x, y2);
        print("" + this.windowFrames[0].charAt(8));
        setCursorXY(x2, y2);
        print("" + this.windowFrames[0].charAt(8));
        return this;
    }

    public XUITTY doFrameTitle(String title, int x, int y, int w, int h) {
        doFrame(x, y, x + w - 1, y + h - 1);
        if (title.length() > 0) {
            setCursorXY(x + 1, y);
            print(title.substring(0, Math.min(title.length(), w - 2)));
        }
        return this;
    }

    public XUITTY printHRulerTop(int x, int y, int width, int each, int valmax) {
        int threshold = 0;
        for (int i = 0; i <= width; i++) {
            setCursorXY(i + x, y + 1);
            if ((i * valmax / width) >= threshold || i == width) {
                threshold += each;
                if (i == 0) {
                    print("" + windowFrames[0].charAt(2));
                } else if (i == width) {
                    print("" + windowFrames[0].charAt(3));
                } else {
                    print("" + windowFrames[0].charAt(7));
                }
                setCursorXY(i + x, y);
                print("" + (int) ((i * valmax) / width));
//                print(String.format("%-4d", (int) ((i * valmax) / width)));
            } else {
                print("" + windowFrames[0].charAt(4));
            }

        }
        return this;
    }

    public XUITTY printVRuler(int x, int y, int height, int each, int valmax) {
        int threshold = 0;
        for (int i = 0; i <= height; i++) {
            setCursorXY(x, y + i);
            if ((i * valmax / height) >= threshold || i == height) {
                threshold += each;
                if (i == 0) {
                    print("" + windowFrames[0].charAt(1));
                } else if (i == height) {
                    print("" + windowFrames[0].charAt(3));
                } else {
                    print("" + windowFrames[0].charAt(9));
                }
                setCursorXY(x - 5, y + i);
                print(String.format("%4d", (int) ((i * valmax) / height)));
            } else {
                print("" + windowFrames[0].charAt(5));
            }

        }
        return this;
    }

    
    public void printGrid(int x, int y, int x2, int y2, int level) {
        if (level > 0) {
            int pmx = (x + x2) / 2, pmy = (y + y2) / 2;
            printGrid(x, y, pmx, pmy, level - 1);
            printGrid(pmx, y, x2, pmy, level - 1);
            printGrid(x, pmy, pmx, y2, level - 1);
            printGrid(pmx, pmy, x2, y2, level - 1);
            doGrid(x, y, x2, y2);
        }
    }

//    public XUITTY printWRuler(int x, int y, int width, int height, int each, int hmax, int vmax) {
//        setBackground(black).setText(lightgreen);
//        printGrid(x, y, x + width - 1, y + height - 1, 4);
////        doRectangleFrame(x, y + 1, width, height);     
////        printHRulerTop(x, y, width, each, hmax);
////        printVRuler(x, y + 1, height - 10, each, vmax);
////        setCursorXY(x, y + 1).print("" + windowFrames[0].charAt(8));
//
//        return this;
//    }
}
