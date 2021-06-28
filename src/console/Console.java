package console;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import static crypto.Keygen.getAlphaNumKey;
import static crypto.Keygen.getAlphaNumKey;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author lcv
 */
public class Console {

    protected Process _pconsole;
    protected String _consoleinputfile, _tty;
    protected PrintStream _outTo;
    protected InputStream _inFrom;

    public static final int bpp = 10, bmax = (int) Math.pow(2, bpp), base = bmax - 1;
    public static final String windowFrames[] = {"┌┐└┘─│┬┴┼┤├▄", "++++-|", "▁▂▃▄▅▆▇█", "▏▎▍▌▋▊▉█"};
    public static final String ESC = "\033";
    public static final int black = defColor(0, 0, 0),
            red = defColor(0.5, 0, 0),
            green = defColor(0, 0.5, 0),
            brown = defColor(0.5, 0.5, 0),
            blue = defColor(0, 0, 0.5),
            magenta = defColor(0.5, 0, 0.5),
            cyan = defColor(0, 0.5, 0.5),
            gray = defColor(0.5, 0.5, 0.5),
            lightgray = defColor(0.75, 0.75, 0.75),
            lightred = defColor(1, 0, 0),
            lightgreen = defColor(0, 1, 0),
            yellow = defColor(1, 1, 0),
            lightblue = defColor(0, 0, 1),
            lightmagenta = defColor(1, 0, 1),
            lightcyan = defColor(0, 1, 1),
            white = defColor(1, 1, 1),
            graphite = defColor(0.3, 0.3, 0.3);

    int _bgconsole = black, _fgconsole = white, _defbg = -1, _deftext = -1, _cursorx = 1, _cursory = 1, _width, _height;
    String _title;

    public Console(String title) {
        _pconsole = openInternalConsole(getAlphaNumKey());
        _height = 25;
        _width = 80;
    }

    public Console(String title, int rows, int columns) {

        _pconsole = openExternalConsole(title, rows, columns, 14);

    }

    public Console(String title, int rows, int columns, int fontsize) {

        if (fontsize < 0) {
            _pconsole = openExternalConsoleArial(title, rows, columns, -fontsize);
        } else {
            _pconsole = openExternalConsole(title, rows, columns, fontsize);
        }

    }

    public Console(String title, String json) {
        _pconsole = linkExternalConsole(json);
    }

    public Process openExternalConsole(String title, int columns, int rows, int fontsize) {
        Process ps = null;
        boolean readtty = false;
        this._consoleinputfile = title.replace(" ", "_").substring(0, Math.min(8, title.length())) + ".tty";
        _height = rows;
        _width = columns;

        File filetty = new File(this._consoleinputfile);
        if (filetty.exists()) {
            filetty.delete();
        }
        try {
            ps = new ProcessBuilder(new String[]{
                "xterm", "-sb", "-rightbar", "-title", title, "-fa", "fixed", "-fs", "" + fontsize, "-geometry", "" + _width + "x" + _height,
                "-e", "tty > " + _consoleinputfile + "; while [ true ]; do sleep 1000; done"}).start();

        } catch (Exception ex) {
            System.err.println(ex);
            System.exit(1);
        }
        while (!readtty) {
            try {
                _tty = new Scanner(new File(_consoleinputfile)).useDelimiter("\\Z").next();
                _title = title;
                _inFrom = new FileInputStream(new File(tty()));
                _outTo = new PrintStream(tty());
                readtty = true;
            } catch (Exception ex) {
                try {
                    Thread.sleep(500);
                } catch (Exception ex2) {
                }
            }
        }
        return ps;
    }

    public Process openExternalConsoleArial(String title, int columns, int rows, int fontsize) {
        Process ps = null;
        boolean readtty = false;
        this._consoleinputfile = title.replace(" ", "_").substring(0, Math.min(8, title.length())) + ".tty";
        _height = rows;
        _width = columns;

        File filetty = new File(this._consoleinputfile);
        if (filetty.exists()) {
            filetty.delete();
        }
        try {
            ps = new ProcessBuilder(new String[]{
                "xterm", "-title", title, "-fa", "Arial", "-fs", "" + fontsize, "-geometry", "" + _width + "x" + _height,
                "-e", "tty > " + _consoleinputfile + "; while [ true ]; do sleep 1000; done"}).start();

        } catch (Exception ex) {
            System.err.println(ex);
            System.exit(1);
        }
        while (!readtty) {
            try {
                _tty = new Scanner(new File(_consoleinputfile)).useDelimiter("\\Z").next();
                _title = title;
                _inFrom = new FileInputStream(new File(tty()));
                _outTo = new PrintStream(tty());
                readtty = true;
            } catch (Exception ex) {
                try {
                    Thread.sleep(500);
                } catch (Exception ex2) {
                }
            }
        }
        return ps;
    }

//    public Process openInternalConsole(String title) {
//        Process ps = null;
//        boolean readtty = false;
//        this._consoleinputfile = title + ".tty";
//        try {
//            ps = new ProcessBuilder(new String[]{
//                "xterm", "-e", "tty > " + _consoleinputfile}).start();
//        } catch (Exception ex) {
//            System.err.println(ex);
//            System.exit(1);
//        }
//        while (!readtty) {
//            try {
//                _tty = new Scanner(new File(_consoleinputfile)).useDelimiter("\\Z").next();
//                readtty = true;
//            } catch (Exception ex) {
//                try {
//                    Thread.sleep(500);
//                } catch (Exception ex2) {
//                }
//            }
//
//        }
//        ps.destroy();
////            System.out.println("Internal Console open on " + _tty);
//        _title = title;
//        _outTo = System.out;
//        _inFrom = System.in;
//
////        } catch (Exception ex) {
////            System.err.print("Error opening Console " + ex.toString());
////        }
//        return null;
//    }
    public Process openInternalConsole(String title) {
        Process ps = null;
        boolean readtty = false;
        this._consoleinputfile = title + ".tty";
        _tty = "";
        _title = title;
        _outTo = System.out;
        _inFrom = System.in;
        return null;
    }

    public Process linkExternalConsole(String json) {
//        System.out.println("Link to external Console open on " + _tty);
        JsonArray ct = new JsonArray().add(1).add(1).add(1),
                cb = new JsonArray().add(0).add(0).add(0);
        JsonObject config;
        int ctext, cbackg;

        try {
            config = Json.parse(json).asObject();
            ct = config.get("text").asArray();
            cb = config.get("background").asArray();
            _tty = config.get("tty").asString();
            ctext = Console.defColor(ct.get(0).asDouble(), ct.get(1).asDouble(), ct.get(2).asDouble());
            cbackg = Console.defColor(cb.get(0).asDouble(), cb.get(1).asDouble(), cb.get(2).asDouble());
            setText(ctext).setBackground(cbackg).clearScreen().captureStdInOut();
            _outTo = new PrintStream(_tty);
            _inFrom = System.in;
        } catch (Exception ex) {
            System.err.print("Error opening console " + ex.toString());
        }
        return null;
    }

    public Console print(String s) {
        try {
            this.out().append(s);
        } catch (Exception ex) {
            System.err.println("Console " + _title + "Error while printing text" + ex.toString());
            System.exit(1);
        }
        _cursorx += s.length();
        return this;
    }

    public Console println(String s) {
        print(s).print("\n");
        _cursorx = 1;
        _cursory++;
        return this;
    }

    public String readLine() {
        return new Scanner(this.in()).nextLine();
    }

    public int readInt() {

        return Integer.parseInt(readLine());
    }

    public double readDouble() {

        return Double.parseDouble(readLine());
    }

    public void waitToClose() {
        if (_pconsole != null) {
            String message = "PRESS [INTRO] TO CLOSE THIS WINDOW";
            print(defText(white) + defBackground(red) + defCursorXY((_width - message.length()) / 2, _height) + message).readLine();
            close();
        }
    }

    public void close() {
        if (_pconsole != null) {
            _pconsole.destroy();
            File filetty = new File(this._consoleinputfile);
            if (filetty.exists()) {
                filetty.delete();
            }
        }
        resetStdInOut();
    }

    public PrintStream out() {
        return _outTo;
    }

    public String tty() {
        return _tty;
    }

    public InputStream in() {
        return _inFrom;
    }

    public Console captureStdInOut() {
        System.setIn(this.in());
        System.setOut(this.out());
        return this;
    }

    public Console resetStdInOut() {
        System.setIn(System.in);
        System.setOut(System.out);
        return this;
    }

    public String toJson() {
        JsonObject res = new JsonObject();
        res.add("tty", _tty).
                add("text", new JsonArray().add(Console.getRed(_fgconsole))
                        .add(Console.getGreen(_fgconsole))
                        .add(Console.getBlue(_fgconsole))).
                add("background", new JsonArray().add(Console.getRed(_bgconsole))
                        .add(Console.getGreen(_bgconsole))
                        .add(Console.getBlue(_bgconsole)));

        return res.toString();
    }

    public String toString() {
        return toJson();
    }

    public int getWidth() {
        return this._width;
    }

    public int getHeight() {
        return this._height;
    }

    /**
     * Encodes a RGB color as an integer value with 4 bits per pixel. Each RGB
     * value is specified as a real number in the range from 0 (darker) to 1
     * (brighter).
     *
     * @param r Red value
     * @param g Green Value
     * @param b Blue Value
     * @return An integer value which can be used to reference the color Since
     * only 4 bpp are used, that is, 16 possible values for each RGB, rounding
     * differences might appear.
     */
    public static int defColor(double r, double g, double b) {
        int ir = (int) (r * base) % bmax,
                ig = (int) (g * base) % bmax,
                ib = (int) (b * base) % bmax;
        int res = ir + (ig << bpp) + (ib << (2 * bpp));
        return res;
    }

    public static int negColor(double r, double g, double b) {
        return defColor((1 - r) * (1 - r), (1 - g) * (1 - g), (1 - b) * (1 - b));
    }

    public static int negColor(int color) {
        return defColor(1 - getRed(color), 1 - getGreen(color), 1 - getBlue(color));
    }

    public static String defJsonColor(double r, double g, double b) {
        int color = defColor(r, g, b);

        return new JsonObject().add("color", color).toString();
    }

    /**
     * Given an integer value which encodes a color, it returns its Red
     * component
     *
     * @param color Encoding a 4bpp RGB color
     * @return A real number in the range from 0 (darker) to 1 (brighter)
     */
    public static double getRed(int color) {
        return (color & base) * 1.0 / base;
    }

    /**
     * Given an integer value which encodes a color, it returns its Green
     * component
     *
     * @param color Encoding a 4bpp RGB color
     * @return A real number in the range from 0 (darker) to 1 (brighter)
     */
    public static double getGreen(int color) {
        return ((color & (base << bpp)) >> bpp) * 1.0 / base;
    }

    /**
     * Given an integer value which encodes a color, it returns its Blue
     * component
     *
     * @param color Encoding a 4bpp RGB color
     * @return A real number in the range from 0 (darker) to 1 (brighter)
     */
    public static double getBlue(int color) {
        return ((color & (base << (2 * bpp))) >> (2 * bpp)) * 1.0 / base;
    }

    /**
     * Returns the current color of text as an encoded color (4 bpp)
     *
     * @return An integer that encodes the RGB color with 4 bits per pixel
     */
    public int getText() {
        return _fgconsole;
    }

    /**
     * Returns the current color of the background as an encoded color (4 bpp)
     *
     * @return An integer that encodes the RGB color with 4 bits per pixel
     */
    public int getBackground() {
        return _bgconsole;
    }

    public Console resetColors() {
        if (_defbg >= 0) {
            setBackground(_defbg);
        }
        if (_deftext >= 0) {
            setText(_deftext);
        }
        return this;
    }

    /**
     * Defines the color of the background
     *
     * @param color To be assigned to the background of the terminal
     */
    public Console setBackground(int color) {

        print(defBackground(color));
        if (_defbg < 0) {
            _defbg = color;
        }
        this._bgconsole = color;
        return this;
    }

    public static String defBackground(int color) {
        //    bg=color;
        int r = (int) (255 * getRed(color)),
                g = (int) (255 * getGreen(color)),
                b = (int) (255 * getBlue(color));
        String s = "[48;2;" + r + ";" + g + ";" + b + "m";
        s = ESC + s;
        return s;
    }

    /**
     * Defines the color of the text in the terminal
     *
     * @param color The color to be assigned to the text
     */
    public Console setText(int color) {

        print(defText(color));
        if (_deftext < 0) {
            _deftext = color;
        }
        _fgconsole = color;
        return this;
    }

    public static String defText(int color) {
        int r = (int) (255 * getRed(color)),
                g = (int) (255 * getGreen(color)),
                b = (int) (255 * getBlue(color));

        String s = "[38;2;" + r + ";" + g + ";" + b + "m";
        s = ESC + s;
        return s;
    }

    /**
     * Clears the screen with the existing background color.
     */
    public static String defclearScreen() {
        String s = "[2J";
        s = ESC + s;
        return s + defCursorXY(1, 1);
    }

    public Console clearScreen() {
        print(defclearScreen());
        return this;
    }

    /**
     * Locates the cursor, and therfore any further cout operation, in a
     * position of the screen of the terminal given by @p x and @p y
     *
     * @param x Horizontal coordinate from top-left corner, which is x=1
     * @param y vertical coordinate from top-left corner, which is y=1
     */
    public static String defCursorXY(int x, int y) {
        String s = "[" + y + ";" + x + "f";
        s = ESC + s;
        return s;
    }

    public Console setCursorXY(int x, int y) {
        print(defCursorXY(x, y));
        return this;
    }

    /**
     * Shows the cursor in the screen
     */
    public Console setCursorOn() {
        String s = "[?25h";
        s = ESC + s;
        print(s);
        return this;
    }

    /**
     * Hides the cursor in the screen
     */
    public Console setCursorOff() {
        String s = "[?25l";
        s = ESC + s;
        print(s);
        return this;
    }

    /**
     * Fills a rectangle in the screen
     *
     * @param x Top ledt
     * @param y Top left
     * @param w Width of the rectangle, specified as text-columns in the
     * terminal
     * @param h Height of the rectangle, specified as text-rows in the terminal
     */
    public Console doRectangle(int x, int y, int w, int h) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                setCursorXY(x + i, y + j);
                print(" ");
//                if (i == w / 2 ) { //|| j == h / 2) {
//                    print("+");
//                } else {
//                    print(".");
//                }
            }
        }
        return this;
    }

    public Console doRectangleFrame(int x, int y, int w, int h) {
        doRectangle(x, y, w, h);
        doFrame(x, y, x + w - 1, y + h - 1);
//        for (int i = 0; i < h; i++) {
//            setCursorXY(x, y + i);
//            print("" + this.windowFrames[0].charAt(5));
//            setCursorXY(x + w - 1, y + i);
//            print("" + this.windowFrames[0].charAt(5));
//        }
//        for (int j = 0; j < w; j++) {
//            setCursorXY(x + j, y);
//            print("" + this.windowFrames[0].charAt(4));
//            setCursorXY(x + j, y + h - 1);
//            print("" + this.windowFrames[0].charAt(4));
//        }
//        setCursorXY(x, y);
//        print("" + this.windowFrames[0].charAt(0));
//        setCursorXY(x + w - 1, y);
//        print("" + this.windowFrames[0].charAt(1));
//        setCursorXY(x, y + h - 1);
//        print("" + this.windowFrames[0].charAt(2));
//        setCursorXY(x + w - 1, y + h - 1);
//        print("" + this.windowFrames[0].charAt(3));

        return this;
    }

    public Console doFrame(int x, int y, int x2, int y2) {
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

    public Console doGrid(int x, int y, int x2, int y2) {
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

    public Console doFrameTitle(String title, int x, int y, int w, int h) {
        doFrame(x, y, x + w - 1, y + h - 1);
        if (title.length() > 0) {
            setCursorXY(x + 1, y);
            print(title.substring(0, Math.min(title.length(), w - 2)));
        }
        return this;
    }

    public Console doProgressBar(int x, int y, int width, double value, double max) {

        setBackground(gray);
        doRectangle(x, y, width, 1);
        setBackground(lightblue);
        doRectangle(x, y, (int) Math.round(value / max * width), 1);
        resetColors();
        setCursorXY(x + width, y);
//        print("" + (int) (100 * progress) + "% completed");
        if (value < 0) {
            print("XXX");
        } else {
            print(String.format("%05.1f", value));
        }
        return this;
    }

    public void printMinibar(int x, int y, int level, int front, int back) {
        int bx = x, by = y;
        level = level / 5;
        while (level > 0) {
            setCursorXY(bx, by);
            if (level >= 8) {
                setText(back);
                setBackground(front);
                print(" ");
                by--;
            } else {
                setText(front);
                setBackground(back);
                print("" + Console.windowFrames[2].charAt(level));
            }
            level = level - 8;
        }
    }

    public void printHMinibar(int x, int y, double level, double maxlevel, int width, int front, int back) {
        int bx = x, by = y;
        int ilevel = (int) Math.ceil(Math.min(level, maxlevel) / maxlevel * width * 8);
        while (ilevel > 0) {
            setCursorXY(bx, by);
            if (ilevel >= 8) {
                setText(back);
                setBackground(front);
                print(" ");
                bx++;
            } else {
                setText(front);
                setBackground(back);
                print("" + Console.windowFrames[3].charAt(ilevel));
            }
            ilevel = ilevel - 8;
        }
    }

//    public Console printHRuler(int x, int y, int width, int each, int valmax) {
//        int threshold = 0;
//        for (int i = x; i <= x + width; i++) {
//            setCursorXY(i, y);
//            if ((i - x) * valmax > threshold || i == width) {
//                threshold += each;
//                if (i - x == 0) {
//                    print("" + windowFrames[0].charAt(0));
//                } else if (i == width) {
//                    print("" + windowFrames[0].charAt(1));
//                } else {
//                    print("" + windowFrames[0].charAt(6));
//                }
//                setCursorXY(i, y + 1);
//                print("" + ((i - x) * valmax) / width);
//            } else {
//                print("" + windowFrames[0].charAt(4));
//            }
//
//        }
//        return this;
//    }
    public Console printHRulerTop(int x, int y, int width, int each, int valmax) {
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

    public Console printVRuler(int x, int y, int height, int each, int valmax) {
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

    public Console printWRuler(int x, int y, int width, int height, int each, int hmax, int vmax) {
        setBackground(black).setText(lightgreen);
        printGrid(x, y, x + width - 1, y + height - 1, 4);
//        doRectangleFrame(x, y + 1, width, height);     
//        printHRulerTop(x, y, width, each, hmax);
//        printVRuler(x, y + 1, height - 10, each, vmax);
//        setCursorXY(x, y + 1).print("" + windowFrames[0].charAt(8));

        return this;
    }

//    public Console printHRuler(int x, int y, int width, int each, int valmax) {
//        for (int i = x; i <= x + width; i++) {
//            setCursorXY(i, y);
//            if ((i - x) % each == 0 || i == width) {
//                if (i - x == 0) {
//                    print("" + windowFrames[0].charAt(0));
//                } else if (i == width) {
//                    print("" + windowFrames[0].charAt(1));
//                } else {
//                    print("" + windowFrames[0].charAt(6));
//                }
//                setCursorXY(i, y + 1);
//                print("" + ((i - x) * valmax) / width);
//            } else {
//                print("" + windowFrames[0].charAt(4));
//            }
//
//        }
//        return this;
//    }
//
    public char doMessage(String message, String allowed) {
        int cb = getBackground(), ct = getText();
        setText(cb).setBackground(ct);
        this.doRectangle(1, _height, _width, 1);
        this.setCursorXY(1, this._height);
        this.print(message);
        char option;
        String line;
        do {
            line = this.readLine();
            option = (line.length() > 0) ? line.toUpperCase().charAt(0) : ' ';
        } while (!allowed.contains("" + option));
        return option;
    }

    public boolean doConfirm(String message) {
        boolean res;
        setText(green);
        setBackground(white);
        res = doMessage(message + " (Y/N)", "YN") == 'Y';
        resetColors();
        return res;
    }

    public void doPressReturn(String message) {
        setText(red);
        setBackground(white);
        doMessage(message, " ");
        resetColors();
    }

    public Console doTextArea(int px, int py, int w, int h, ArrayList<String> lines) {
        resetColors();
//        this.showFrame(toConsole, cellx * w, celly * h + 2);
        setBackground(black).setText(lightgreen);
        doRectangle(px, py, w, h);
        int begin = (Math.max(0, lines.size() - h));
        for (int i = 0; i < (int) (Math.min(h, lines.size())); i++) {
//        for (int i = (int) (Math.max(0, this.stream.size() - h)); i < this.stream.size() - 1; i++) {
            setCursorXY(px + 1, py + i);
            String s = lines.get(begin + i);
            print(s.substring(0, (int) (Math.min(s.length(), w - 1))));
        }
        resetColors();
        return this;
    }

    public Console doRadioColor(boolean radio) {
        if (radio) {
            setBackground(lightgreen).print(" ");
        } else {
            setBackground(black).print(" ");
        }
        resetColors();
        return this;
    }

}
