/** *
 * @file DBAMap
 * @author Luis Castillo, DBA, l.castillo@decsai.ugr.es
 */
package map2D;

import data.OleFile;
import geometry.Point3D;
import geometry.SimpleVector3D;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.apache.commons.imaging.formats.tiff.TiffImagingParameters;
import swing.SwingTools;
import tools.StringTools;
import static tools.StringTools.cureFilename;

/**
 *
 * @author Luis Castillo Vidal @ DBA It fixes Java's VM grayscale bitmap issue
 * regarding 3rd party grayscale imagers such as Gimp
 *
 * Java's ImageIO is known to be broken on images with a grayscale palette with
 * auto (not documented) gamma correction
 * https://stackoverflow.com/questions/32583772/reading-grayscale-png-image-files-without-distortion/32590785#32590785
 *
 *  * Java ImageIO Grayscale PNG Issue * javax.imageio.ImageIO reading incorrect
 * RGB values on grayscale images * My batch jpg resizer works with color
 * images, but grayscale ones become washed out * Wrong brightness converting
 * image to grayscale in Java * Oracle: JDK-5051418 : Grayscale TYPE_CUSTOM
 * BufferedImages are rendered lighter than TYPE_BYTE_GRAY * Oracle: JDK-6467250
 * : BufferedImage getRGB(x,y) problem
 */
public class Map2DColor {

    public static enum Channel {
        RED, GREEN, BLUE
    };
    public static final Color BADVALUE = new Color(100, 0, 0);
    public static final int MAXLEVEL = 255, MINLEVEL = 0;
    protected BufferedImage _map;
    protected int _lmax, _lmin;
    protected double k = 2.261566516;
    protected Color penColor;
    protected Font font;
    public static int PIX_TYPE = BufferedImage.TYPE_INT_RGB,
            BIT_MASK = 0xff,
            BIT_SHIFT = 8,
            BIT_LENGTH = BIT_SHIFT,
            PIX_MAX = (int) Math.pow(2, BIT_LENGTH) - 1,
            PIX_MIN = 0;
    Graphics myG;

    /**
     *
     * Default builder
     */
    public Map2DColor() {
        _map = null;
        _lmax = _lmin = -1;
        penColor = Color.GREEN;
        font = new Font("Monospaced", 12, PLAIN);
    }

    /**
     * Builder#1
     *
     * @param width Number con columns
     * @param height Number of rows
     */
    public Map2DColor(int width, int height) {
        _lmax = _lmin = -1;
        _map = new BufferedImage(width, height, PIX_TYPE);
//        myG = this._map.getGraphics();
        penColor = Color.GREEN;
        font = new Font("Arial", 12, Font.BOLD);
    }

    /**
     * Builder #3
     *
     * @param width Number con columns
     * @param height Number of rows
     * @param level Default grayscale level (0-255) in all the pixels of the
     * image
     */
    public Map2DColor(int width, int height, int level) {
        _lmax = _lmin = -1;
        _map = new BufferedImage(width, height, PIX_TYPE);
//        myG = this._map.getGraphics();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setColor(x, y, new Color(level, level, level));
            }
        }
        penColor = Color.GREEN;
        font = new Font("Monospaced", 12, PLAIN);
    }

    /**
     * Builder #3
     *
     * @param width Number con columns
     * @param height Number of rows
     * @param c The default color image
     */
    public Map2DColor(int width, int height, Color c) {
        _lmax = _lmin = -1;
        _map = new BufferedImage(width, height, PIX_TYPE);
//        myG = this._map.getGraphics();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setColor(x, y, c);
            }
        }
        penColor = Color.GREEN;
        font = new Font("Monospaced", 12, PLAIN);
    }

    public Map2DColor(BufferedImage img) {
        _lmax = _lmin = -1;
        _map = new BufferedImage(img.getWidth(), img.getHeight(), PIX_TYPE);
//        myG = this._map.getGraphics();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setColor(x, y, new Color(img.getRGB(x, y)));
            }
        }
        penColor = Color.GREEN;
        font = new Font("Monospaced", 12, PLAIN);
    }

    public Map2DColor(Map2DGrayscale red, Map2DGrayscale green, Map2DGrayscale blue) {
        _lmax = _lmin = -1;
        _map = new BufferedImage(red.getWidth(), red.getHeight(), PIX_TYPE);
//        myG = this._map.getGraphics();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setColor(x, y, new Color(red.getLevel(x, y), green.getLevel(x, y),
                        blue.getLevel(x, y)));
            }
        }
        penColor = Color.GREEN;
        font = new Font("Monospaced", 12, PLAIN);
    }

    public Map2DColor readFrom(byte[] stream) {
        try {
            InputStream in = new ByteArrayInputStream(stream);
            BufferedImage bufImage = ImageIO.read(in);
            penColor = Color.GREEN;
            font = new Font("Monospaced", 12, PLAIN);
            return new Map2DColor(bufImage);
        } catch (Exception ex) {
            return new Map2DColor();
        }
    }

    /**
     * *
     * Carga una imagen desde un archivo en una matriz bidimensional de píxeles,
     * cada píxel con un valor RGB tal que R=G=B (escala de grises)
     *
     * @param filename Nombre del fichero
     * @throws IOException Fallos de manejo del fichero
     * @return A coy of the same instance
     */
//    public Map2DColor loadMap(String filename) throws IOException {
//        File f;
//
//        _map = null;
//        f = new File(filename);
//        _map = ImageIO.read(f);
//        _lmax = _lmin = -1;
//        this.getExtremeHeights();
//        for (int y = 0; y < getHeight(); y++) {
//            for (int x = 0; x < getWidth(); x++) {
////                this.setLevel(x, y, new Color(_map.getRGB(x, y)).getGreen());
//                this.setColor(x, y, applyAlphaLevel(getStepLevel(x,y)));
//            }
//        }
//        return this;
//    }
    public Map2DColor loadMapNormalize(String filename) throws IOException {
        File f;
        f = new File(filename);
        this._map = ImageIO.read(f);
//        myG = this._map.getGraphics();
        _lmax = _lmin = -1;
        normalize();
//        this.getExtremeHeights();

        return this;
    }

    public Map2DColor loadMapRaw(String filename) throws IOException {
        File f;
        f = new File(filename);
        this._map = ImageIO.read(f);
//        myG = this._map.getGraphics();
        _lmax = _lmin = -1;
        System.out.println("MAP=" + _map.getType());
        return this;
    }

    public Map2DColor loadFIT(String filename) throws IOException {
        final TiffImagingParameters params = new TiffImagingParameters();

        try {
            this._map = new TiffImageParser().getBufferedImage(new File(filename), params);
//            myG = this._map.getGraphics();

            return this;
        } catch (ImageReadException ex) {
            return null;
        }
    }

    public void setMap(BufferedImage _map) {
        this._map = _map;
//        myG = this._map.getGraphics();
    }

    public Map2DColor shiftLeft(int pix) {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth() - pix; x++) {
                _map.setRGB(x, y, _map.getRGB(x + pix, y));
            }
        }
        return this;
    }

    protected Map2DColor normalize() {
//        for (int y = 0; y < getHeight(); y++) {
//            for (int x = 0; x < getWidth(); x++) {
//                _map.setRGB(x, y, this.applyAlphaLevel(getColor(x, y)).getRGB());
//            }
//        }
        return this;
    }

    protected Color applyAlphaLevel(Color c) {
        int newlevel = correctAlphaLevel(c.getBlue());
        return new Color(newlevel, newlevel, newlevel);
    }

    public int correctAlphaLevel(int level) {
        int newlevel, maxlevel = 255; //Color.WHITE.getBlue();
        level = (int) (Math.pow(level / (1.0 * maxlevel), k) * maxlevel);
        newlevel = (int) Math.round(level);
        return newlevel;
    }

    public Map2DColor setAlpha(int alphastep) {
        double step = 0.05, rlevel, glevel, blevel;
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                Color C = getColor(x, y);
                rlevel = C.getRed() / 255.0;
                glevel = C.getGreen() / 255.0;
                blevel = C.getBlue() / 255.0;

//                if ((alphastep<0 && getRawLevel(x, y) > 180) || 
//                        (alphastep>0 && getRawLevel(x,y)<70)) {
//                if ((alphastep<0 && (rlevel + blevel + glevel) / 3 > 0.7) || 
//                        (alphastep>0 && (rlevel + blevel + glevel) / 3 < 0.3)) {
                rlevel = Math.min(255, rlevel * (1 + alphastep * step) * 255);
                blevel = Math.min(255, blevel * (1 + alphastep * step) * 255);
                glevel = Math.min(255, glevel * (1 + alphastep * step) * 255);
                setColor(x, y, new Color((int) rlevel, (int) glevel, (int) blevel));
//                }
            }
        }
        return this;
    }

    /**
     * Guarda la matriz bidimensional en un fichero cuyo formato viene dado por
     * la extensión indicada en el nombre del fichero
     *
     * @param filename El fichero a grabar
     * @return
     * @throws IOException Errores de ficheros
     */
    public Map2DColor saveMap(String filename) throws IOException {
        File f;

        f = new File(cureFilename(filename));
//        for (int y = 0; y < getHeight(); y++) {
//            for (int x = 0; x < getWidth(); x++) {
//                Color c = new Color(getStepLevel(x, y), getStepLevel(x, y), getStepLevel(x, y));
//                _map.setRGB(x, y, c.getRGB());
//            }
//        }
        ImageIO.write(_map, "PNG", f);
        return this;
    }

//    /**
//     * It gets the image encapsulated in a JsonFile ready to be used. First,
//     * extracts the encapsulated PNG file. Second, it loads the PNG into the
//     * class taking into account Java's Grayscale gamma correction
//     *
//     * @param fromJsonFile
//     * @return
//     */
//    public boolean fromJson(JsonObject fromJsonFile) {
//        try {
//            FileUtils.JsonToFile(fromJsonFile, "./");
//            this.loadMap("./" + fromJsonFile.getString("filename", "default.png"));
//        } catch (Exception ex) {
//            return false;
//        }
//        return true;
//    }
    /**
     * Carga en la matriz bidimensional la imagen que devuelve el SUBSCRIBE de
     * la práctica 3 {"map":[...]}
     *
     * @param map Array JSON que contiene la imagen comprimida para que su envío
     * entre agentes sea más eficiente
     * @throws IOException Errores de manejo de fiheros temporales
     */
//    public void fromJson(JsonArray map) throws IOException {
//        String auxfilename = "./tmp.png";
//        byte[] data = new byte[map.size()];
//        for (int i = 0; i < data.length; i++) {
//            data[i] = (byte) map.get(i).asInt();
//        }
//        try {
//            FileOutputStream fos = new FileOutputStream(auxfilename);
//            fos.write(data);
//            fos.close();
//            loadMap(auxfilename);
//        } catch (Exception ex) {
//            System.err.println("*** Error " + ex.toString());
//            _map = null;
//        }
//    }
    /**
     * Devuelve el ancho de la imagen cargada
     *
     * @return Ancho de la imagen
     */
    public int getWidth() {
        if (this.hasMap()) {
            return _map.getWidth();
        } else {
            return -1;
        }
    }

    public BufferedImage getColorImage() {
        return _map;
    }

    public BufferedImage getGrayscaleImage() {
//        BufferedImage mapgr = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_GRAY);
//        for (int x = 0; x < getWidth(); x++) {
//            for (int y = 0; y < getHeight(); y++) {
//                Color c = this.getColor(x, y);
//                mapgr.setRGB(x, y, _map.getRGB(x, y));
//            }
//        }
//        return mapgr;
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = img.getGraphics();
        g.drawImage(this._map, 0, 0, null);
        g.dispose();
        return img;
    }

//    public Map2DColor getChannel(Channel channel) {
//        Map2DColor mapgr = new Map2DColor(getWidth(), getHeight());
//        for (int x = 0; x < getWidth(); x++) {
//            for (int y = 0; y < getHeight(); y++) {
//                Color c = this.getColor(x, y);
//                if (channel == Channel.RED) {
//                    mapgr.setLevel(x, y, c.getRed());
//                } else if (channel == Channel.GREEN) {
//                    mapgr.setLevel(x, y, c.getGreen());
//                } else {
//                    mapgr.setLevel(x, y, c.getBlue());
//                }
//            }
//        }
//        return mapgr;
//    }
    public Map2DGrayscale getChannel(Channel channel) {
        Map2DGrayscale mapgr = new Map2DGrayscale(getWidth(), getHeight());
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Color c = this.getColor(x, y);
                if (channel == Channel.RED) {
                    mapgr.setLevel(x, y, c.getRed());
                } else if (channel == Channel.GREEN) {
                    mapgr.setLevel(x, y, c.getGreen());
                } else {
                    mapgr.setLevel(x, y, c.getBlue());
                }
            }
        }
        return mapgr;
    }

    public Map2DColor toGrayscale() {
        Map2DColor gray = new Map2DColor(getWidth(), getHeight());
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = img.getGraphics();
        g.drawImage(this._map, 0, 0, null);
        g.dispose();
        gray.setMap(img);
        return gray;
    }

    public Map2DColor resize(int width, int height) {
        int realw, realh;
        if (width <= 0) {
            realh = height;
            realw = (int) (getWidth() * height * 1.0 / getHeight());
        } else if (height <= 0) {
            realw = width;
            realh = (int) (getHeight() * width * 1.0 / getWidth());
        } else {
            realw = width;
            realh = height;
        }
        Map2DColor res = new Map2DColor(realw, realh);
        BufferedImage _map2 = new BufferedImage(realw, realh, res.getColorImage().getType());
        Image mapres = _map.getScaledInstance(realw, realh, Image.SCALE_SMOOTH);
        Graphics2D g2d = _map2.createGraphics();
        g2d.drawImage(mapres, 0, 0, null);
        g2d.dispose();
        res.setMap(_map2);
        return res;
    }

    /**
     * Devuelve el alto de la imagen cargada
     *
     * @return Alto de la imagen
     */
    public int getHeight() {
        if (this.hasMap()) {
            return _map.getHeight();
        } else {
            return -1;
        }
    }

    /**
     * Devuelve la mínima altura existente en el mapa
     *
     * @return La altura mínima
     */
    public int getMinHeight() {
        if (this.hasMap()) {
            return _lmin;
        } else {
            return -1;
        }
    }

    /**
     * Devuelve la máxima altura existente en el mapa
     *
     * @return La altura máxima
     */
    public int getMaxHeight() {
        if (this.hasMap()) {
            return _lmax;
        } else {
            return -1;
        }
    }

    /**
     *
     * Devuelve la altura del mapa en las coordenadas especificadas
     *
     * @param x Coordenada del mapa
     * @param y Coordenada del mapa
     * @return Altura del terreno en (x,y)
     */
    public int getRawLevel(int x, int y) {
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
            Color cAux = new Color(_map.getRGB(x, y));
//            if (cAux.getRed() > 0 && cAux.getGreen() == 0 && cAux.getBlue() == 0) {
//                return -1;
//            } else {
//                return cAux.getGreen();
//            }
            return cAux.getGreen();
        } else {
            return -1;
        }
    }

    public int getRawLevel(SimpleVector3D p) {
        return getRawLevel(p.getSource().getXInt(), p.getSource().getYInt());
    }

    /**
     *
     * Devuelve la altura del mapa en las coordenadas especificadas
     *
     * @param x Coordenada del mapa
     * @param y Coordenada del mapa
     * @return Altura del terreno en (x,y)
     */
    public int getStepLevel(int x, int y) {
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
            Color cAux = new Color(_map.getRGB(x, y));
            if (cAux.getRed() > 0 && cAux.getGreen() == 0 && cAux.getBlue() == 0) {
                return -1;
            } else {
                return getRawLevel(x, y) / 5 * 5;
            }
        } else {
            return -1;
        }
    }

    public int getStepLevel(SimpleVector3D p) {
        return getStepLevel(p.getSource().getXInt(), p.getSource().getYInt());
    }

    public Color getColor(int x, int y) {
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
            return new Color(_map.getRGB(x, y));
        } else {
            return Map2DColor.BADVALUE;
        }
    }

    public Color getColor(SimpleVector3D p) {
        if (this.hasMap() && 0 <= p.getSource().getXInt() && p.getSource().getXInt() < this.getWidth()
                && 0 <= p.getSource().getYInt() && p.getSource().getYInt() < this.getHeight()) {
            return new Color(_map.getRGB(p.getSource().getXInt(), p.getSource().getYInt()));
        } else {
            return Map2DColor.BADVALUE;
        }
    }

    /**
     *
     * Devuelve la altura del mapa en las coordenadas especificadas
     *
     * @param x Coordenada del mapa
     * @param y Coordenada del mapa
     * @return Altura del terreno en (x,y)
     */
    public int getStepLevel(double x, double y) {
        return Map2DColor.this.getStepLevel((int) x, (int) y);
    }

    public int getRawLevel(double x, double y) {
        return Map2DColor.this.getRawLevel((int) x, (int) y);
    }

    /**
     * Define the grayscale level of a given point
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @param level the givel level for that point (0-255)
     * @return
     */
    public Map2DColor setLevel(int x, int y, int level) {
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
            if (level >= 0) {
                _map.setRGB(x, y, new Color(level, level, level).getRGB());
            } else {
                _map.setRGB(x, y, Map2DColor.BADVALUE.getRGB());
            }
        }
        return this;
    }

    public Map2DColor setColor(int x, int y, Color c) {
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
            this._map.setRGB(x, y, c.getRGB());
        }
        return this;
    }

    public Map2DColor setColor(SimpleVector3D p, Color c) {
        if (this.hasMap() && 0 <= p.getSource().getXInt() && p.getSource().getXInt() < this.getWidth()
                && 0 <= p.getSource().getYInt() && p.getSource().getYInt() < this.getHeight()) {
            this._map.setRGB(p.getSource().getXInt(), p.getSource().getYInt(), c.getRGB());
        }
        return this;
    }

    /**
     * Define the grayscale level of a given point
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @param level the givel level for that point (0-255)
     * @return
     */
    public Map2DColor setLevel(double x, double y, int level) {
        return setLevel((int) x, (int) y, level);
    }

    public Map2DColor setLevel(SimpleVector3D p, int level) {
        return setLevel((int) p.getSource().getX(), (int) p.getSource().getY(), level);
    }

    /**
     * Comprueba que hay una imagen ya cargada
     *
     * @return true si hay una imagen cargada, false en otro caso
     */
    public boolean hasMap() {
        return (_map != null);
    }

    /**
     * Computes the max a min level present in the image
     */
    private void getExtremeHeights() {
        if (this.hasMap()) {
            _lmin = 256;
            _lmax = -1;
            for (int x = 0; x < getWidth(); x++) {
                for (int y = 0; y < getHeight(); y++) {
                    int level = this.getStepLevel(x, y);
                    if (level > _lmax) {
                        _lmax = level;
                    }
                    if (level < _lmin) {
                        _lmin = level;
                    }
                }
            }

        }
    }

    public int getStepLevel(Point3D p) {
        return getStepLevel(p.getXInt(), p.getYInt());
    }

    public Map2DColor setLevel(Point3D p, int level) {
        return setLevel((int) p.getX(), (int) p.getY(), level);
    }

    public int getRawLevel(Point3D p) {
        return getRawLevel(p.getXInt(), p.getYInt());
    }

    public Map2DColor setColor(Point3D p, Color c) {
        return setColor(p.getXInt(), p.getYInt(), c);
    }

    public Map2DColor toLevelCurve(int step) {
        Map2DColor res = new Map2DColor(this.getWidth(), this.getHeight());
        ArrayList<Integer> neighbors;
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                neighbors = new ArrayList();
                for (int xx = 0; xx < 3; xx++) {
                    for (int yy = 0; yy < 3; yy++) {
                        neighbors.add(this.getCurveLevel(x - xx + 1, y - yy + 1, step));
                    }
                }
                Collections.sort(neighbors);
                if (getCurveLevel(x, y, step) == neighbors.get(6)) {
                    if (getStepLevel(x, y) < 1) {
                        res.setColor(x, y, new Color(0, 0, 50));
                    } else {
                        res.setColor(x, y, Color.BLACK);
                    }
                } else {
                    res.setColor(x, y, new Color(0, 128, 0));
                }
            }
        }
        return res;
    }

    public int getCurveLevel(int x, int y, int step) {
        int l = this.getStepLevel(x, y);
        if (l < 1) {
            return l;
        } else {
            return (l + 4) / step;
        }
    }

    public Map2DGrayscale toGrayScale() {
//        Map2DColor res = new Map2DColor(this.getWidth(), this.getHeight());
//        for (int x = 0; x < getWidth(); x++) {
//            for (int y = 0; y < getHeight(); y++) {
//                Color c =this.getColor(x, y);
//                res.setLevel(x,y, (c.getGreen()+c.getRed()+c.getBlue())*255/(3*255));
//            }
//        }
        return toGrayScale(1.0);
    }

    public Map2DColor alpha(double alpha01) {
        Color calpha;
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Color c = this.getColor((int) (x), (int) y);
                if (alpha01 < 0) {
                    calpha = new Color(
                            (int) (Math.min(255, Math.max(0, c.getRed() * alpha01))),
                            (int) (Math.min(255, Math.max(0, c.getGreen() * alpha01))),
                            (int) (Math.min(255, Math.max(0, c.getBlue() * alpha01)))
                    );
                } else {
                    calpha = new Color(
                            (int) (Math.min(255, Math.max(0, c.getRed() / alpha01))),
                            (int) (Math.min(255, Math.max(0, c.getGreen() / alpha01))),
                            (int) (Math.min(255, Math.max(0, c.getBlue() / alpha01)))
                    );
                }
                setColor(x, y, calpha);

            }
        }
        return this;
    }

    public Map2DGrayscale toGrayScale(double scale) {
        Map2DGrayscale res = new Map2DGrayscale((int) (this.getWidth() * scale), (int) (this.getHeight() * scale));
        for (int x = 0; x < getWidth() * scale; x++) {
            for (int y = 0; y < getHeight() * scale; y++) {
                Color c = this.getColor((int) (x / scale), (int) (y / scale));
                res.setLevel(x, y, (c.getGreen() + c.getRed() + c.getBlue()) * 255 / (3 * 255));
            }
        }
        return res;
    }

    public Map2DColor scale(double scale) {
        Map2DColor res = new Map2DColor((int) (this.getWidth() * scale), (int) (this.getHeight() * scale));
        for (int x = 0; x < getWidth() * scale; x++) {
            for (int y = 0; y < getHeight() * scale; y++) {
                Color c = this.getColor((int) (x / scale), (int) (y / scale));
                res.setColor(x, y, c);
            }
        }
        return res;
    }

    public Map2DColor absDiff(Map2DColor other) {
        Map2DColor res = new Map2DColor(this.getWidth(), this.getHeight());
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                res.setLevel(x, y, (int) (Math.abs((this.getRawLevel(x, y) - other.getRawLevel(x, y)))));
            }
        }
        return res;
    }

    public Map2DColor highLight(Map2DColor other) {
        Map2DColor res = new Map2DColor(this.getWidth(), this.getHeight());
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                Color c = this.getColor(x, y), c2;
                if (other.getRawLevel(x, y) > 128) {
                    c2 = new Color(c.getRed(), 255, c.getBlue());
                    res.setColor(x, y, c2);
                } else {
                    res.setColor(x, y, c);
                }
            }
        }
        return res;
    }

//    public Map2DColor reduce(int n) {
//        Map2DColor res = new Map2DColor(this.getWidth(), this.getHeight());
//        for (int x = 0; x < getWidth(); x++) {
//            for (int y = 0; y < getHeight(); y++) {
//                res.setLevel(x, y, this.getRawLevel(x, y) >> n << n);
//            }
//        }
//        return res;
//    }
    public Map2DColor threshold(int level) {
        Map2DColor res = new Map2DColor(this.getWidth(), this.getHeight());
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (this.getRawLevel(x, y) > level) {
                    res.setLevel(x, y, 255);
                } else {
                    res.setLevel(x, y, 1);
                }
            }
        }
        return res;
    }

    public Map2DGrayscale findEdges() {
        Map2DGrayscale res = new Map2DGrayscale();
//        Map2DColor res = new Map2DColor(this.getWidth(), this.getHeight()),
//                bw = this.toGrayScale();
//        int leftPixel;
//        int rightPixel;
//        int bottomPixel;
//        int rightColor;
//        boolean black;
//        int distance = 40;
//        for (int row = 0; row < this.getHeight(); row++) {
//            for (int col = 0;
//                    col < this.getWidth(); col++) {
//                black = false;
//                leftPixel = getRawLevel(col, row);
//                if (col < getHeight() - 1) {
//                    rightPixel = getRawLevel(col + 1, row);
//                    if (Math.abs(leftPixel - rightPixel) > distance) {
//                        black = true;
//                    }
//                }
//                if (row < getWidth() - 1) {
//                    bottomPixel = getRawLevel(col, row);
//                    if (Math.abs(leftPixel - bottomPixel) > distance) {
//                        bottomPixel = getRawLevel(col, row + 1);
//                        black = true;
//                    }
//
//                }
//                if (black) {
//                    res.setLevel(col, row, 0);
//                } else {
//                    res.setLevel(col, row, 255);
//                }
//            }
//        }
        return res;
    }

    @Override
    public String toString() {
        OleFile of = new OleFile();
        try {
            saveMap("export-map2dcolor.png");
            of.loadFile("export-map2dcolor.png");
            return of.toPlainJson().toString();
        } catch (IOException ex) {
            return null;
        }

    }

    public Map2DColor fromString(String serial) {
        OleFile of = new OleFile();
        try {
            of.set(serial);
            of.saveFile("./");
            this.loadMapRaw("./" + of.getFileName());
            return this;
        } catch (IOException ex) {
            return null;
        }

    }

    public Map2DColor drawText(int x, int y, String text) {
        myG = this._map.getGraphics();
        myG.setColor(getPenColor());
        myG.setFont(myG.getFont().deriveFont(BOLD));
        myG.drawString(text, x, y);
        myG.dispose();
        return this;
    }

    public Color getPenColor() {
        return penColor;
    }

    public void setPenColor(Color penColor) {
        this.penColor = penColor;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void showAndWait(String title) {
        try {
            JLabel label = new JLabel(new ImageIcon(resize(512, -1).getColorImage()));
            JOptionPane.showMessageDialog(null, label, title, JOptionPane.PLAIN_MESSAGE, null);
        } catch (Exception ex) {
            SwingTools.Error("Sorry. The image cannot be displayed at this moment");
        }
    }

    public Map2DColor soften(int iterations, int width) {
        Color cinit = null, caux = null;
        Map2DColor aux = new Map2DColor(getWidth(), getHeight());
        double rinit, raux, cx = getWidth() / 2, cy = getHeight() / 2;
        System.out.println("Softening ...");
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                cinit = getColor(x, y);
                rinit = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
                caux = null;
                for (int fx = x - width / 2; fx <= x + width / 2; fx++) {
                    for (int fy = y - width / 2; fy <= y + width / 2; fy++) {
                        raux = Math.sqrt((fx - cx) * (fx - cx) + (fy - cy) * (fy - cy));
                        if (0 <= fx && fx < getWidth() && 0 <= fy && fy < getHeight() && raux <= rinit) {
                            if (caux == null) {
                                caux = getColor(fx, fy);
                            }
                            caux = SwingTools.blend(caux, getColor(fx, fy), 0.5f);
                        }
                    }
                }
                setColor(x, y, SwingTools.blend(cinit, caux, 0.5f));
            }
        }
        for (int i = 1; i < iterations; i++) {
            System.out.println("Softening ..." + i);
            for (int x = 0; x < getWidth(); x++) {
                for (int y=0; y < getHeight(); y++) {
                    aux.setColor(x,y,getColor(x,y));
                }
            }
            for (int x = 0; x < getWidth(); x++) {
                for (int y = 0; y < getHeight(); y++) {
                    cinit = aux.getColor(x, y);
                    rinit = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
                    caux = null;
                    for (int fx = x - width / 2; fx <= x + width / 2; fx++) {
                        for (int fy = y - width / 2; fy <= y + width / 2; fy++) {
                            raux = Math.sqrt((fx - cx) * (fx - cx) + (fy - cy) * (fy - cy));
                            if (0 <= fx && fx < getWidth() && 0 <= fy && fy < getHeight() && raux <= rinit) {
                                if (caux == null) {
                                    caux = aux.getColor(fx, fy);
                                }
                                caux = SwingTools.blend(caux, aux.getColor(fx, fy), 0.5f);
                            }
                        }
                    }
                    setColor(x, y, SwingTools.blend(cinit, caux, 0.5f));
                }
            }
        }
        return this;

    }

//    public Map2DColor soften(int iterations, int width) {
//        Color c;
//        for (int i = 0; i < iterations; i++) {
//            System.out.println("Softening ..."+i);
//            for (int x = 0; x < getWidth(); x++) {
//                for (int y = 0; y < getHeight(); y++) {
//                    c = getColor(x, y);
//                    for (int fx = x - width / 2; fx <= x + width / 2; fx++) {
//                        for (int fy = y - width / 2; fy <= y + width / 2; fy++) {
//                            if (0<= fx && fx < getWidth() && 0 <= fy && fy <getHeight()) {
//                                c = SwingTools.blend(c, getColor(fx,fy), 0.5f);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return this;
//
//    }
    public void show(String title) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                showAndWait(title);
            }
        });
        t.start();

    }
}
