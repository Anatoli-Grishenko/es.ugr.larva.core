/** *
 * @file DBAMap
 * @author Luis Castillo, DBA, l.castillo@decsai.ugr.es
 */
package map2D;

import geometry.Point3D;
import geometry.SimpleVector3D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.imageio.ImageIO;
import swing.SwingTools;

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

    public static final Color BADVALUE = new Color(100, 0, 0);
    public static final int MAXLEVEL = 255, MINLEVEL = 0;
    protected BufferedImage _map;
    protected int _lmax, _lmin;
    protected double k = 2.261566516;

    /**
     *
     * Default builder
     */
    public Map2DColor() {
        _map = null;
        _lmax = _lmin = -1;
    }

    /**
     * Builder#1
     *
     * @param width Number con columns
     * @param height Number of rows
     */
    public Map2DColor(int width, int height) {
        _lmax = _lmin = -1;
        _map = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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
        _map = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setColor(x, y, new Color(level, level, level));
            }
        }
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
        _map = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setColor(x, y, c);
            }
        }
    }

    public Map2DColor(BufferedImage img) {
        _lmax = _lmin = -1;
        _map = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setColor(x, y, new Color(img.getRGB(x, y)));
            }
        }
    }

    public Map2DColor readFrom(byte[] stream) {
        try {
            InputStream in = new ByteArrayInputStream(stream);
            BufferedImage bufImage = ImageIO.read(in);
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
        _lmax = _lmin = -1;
        normalize();
//        this.getExtremeHeights();

        return this;
    }

    public Map2DColor loadMapRaw(String filename) throws IOException {
        File f;
        f = new File(filename);
        this._map = ImageIO.read(f);
        _lmax = _lmin = -1;

        return this;
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

        f = new File(filename);
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

    public BufferedImage getMap() {
        return _map;
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

    public Map2DColor toGrayScale() {
//        Map2DColor res = new Map2DColor(this.getWidth(), this.getHeight());
//        for (int x = 0; x < getWidth(); x++) {
//            for (int y = 0; y < getHeight(); y++) {
//                Color c =this.getColor(x, y);
//                res.setLevel(x,y, (c.getGreen()+c.getRed()+c.getBlue())*255/(3*255));
//            }
//        }
        return toGrayScale(1.0);
    }

    public Map2DColor toGrayScale(double scale) {
        Map2DColor res = new Map2DColor((int) (this.getWidth() * scale), (int) (this.getHeight() * scale));
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

    public Map2DColor reduce(int n) {
        Map2DColor res = new Map2DColor(this.getWidth(), this.getHeight());
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                res.setLevel(x, y, this.getRawLevel(x, y) >> n << n);
            }
        }
        return res;
    }

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

    public Map2DColor findEdges() {
        Map2DColor res = new Map2DColor(this.getWidth(), this.getHeight()),
                bw = this.toGrayScale();
        int leftPixel;
        int rightPixel;
        int bottomPixel;
        int rightColor;
        boolean black;
        int distance = 40;
        for (int row = 0; row < this.getHeight(); row++) {
            for (int col = 0;
                    col < this.getWidth(); col++) {
                black = false;
                leftPixel = getRawLevel(col, row);
                if (col < getHeight() - 1) {
                    rightPixel = getRawLevel(col + 1, row);
                    if (Math.abs(leftPixel - rightPixel) > distance) {
                        black = true;
                    }
                }
                if (row < getWidth() - 1) {
                    bottomPixel = getRawLevel(col, row);
                    if (Math.abs(leftPixel - bottomPixel) > distance) {
                        bottomPixel = getRawLevel(col, row + 1);
                        black = true;
                    }

                }
                if (black) {
                    res.setLevel(col, row, 0);
                } else {
                    res.setLevel(col, row, 255);
                }
            }
        }
        return res;
    }

}
