/** *
 * @file DBAMap
 * @author Luis Castillo, DBA, l.castillo@decsai.ugr.es
 */
package map2D;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

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
public class Map2DColor_OLD {

    protected BufferedImage _toDraw, _toQuery;
    public double [][] _toQueryMatrix;
    protected int _lmax, _lmin;
    protected double k = 2.261566516;

    /**
     *
     * Default builder
     */
    public Map2DColor_OLD() {
        _toDraw = null;
        _toQuery = null;
        _lmax = _lmin = -1;
    }

    /**
     * Builder#1
     *
     * @param width Number con columns
     * @param height Number of rows
     */
    public Map2DColor_OLD(int width, int height) {
        _lmax = _lmin = -1;
        _toDraw = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        _toQuery = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        _toQueryMatrix = new double[width][height];
    }

    /**
     * Builder #3
     *
     * @param width Number con columns
     * @param height Number of rows
     * @param level Default grayscale level (0-255) in all the pixels of the
     * image
     */
    public Map2DColor_OLD(int width, int height, int level) {
        _lmax = _lmin = -1;
        _toDraw = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        _toQuery = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        _toQueryMatrix = new double[width][height];
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
     * @param c The default color
     * image
     */
    public Map2DColor_OLD(int width, int height, Color c) {
        _lmax = _lmin = -1;
        _toDraw = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        _toQuery = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        _toQueryMatrix = new double[width][height];
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setColor(x, y, c);
            }
        }
    }

    /**
     * *
     * Carga una imagen desde un archivo en una matriz bidimensional de píxeles,
     * cada píxel con un valor RGB tal que R=G=B (escala de grises)
     *
     * @param filename Nombre del fichero
     * @throws IOException Fallos de manejo del fichero
     */
//    public Map2DColor loadMap(String filename) throws IOException {
//        File f;
//
//        _toDraw = null;
//        f = new File(filename);
//        _toDraw = ImageIO.read(f);
//        _lmax = _lmin = -1;
//        this.getExtremeHeights();
//        for (int y = 0; y < getHeight(); y++) {
//            for (int x = 0; x < getWidth(); x++) {
////                this.setLevel(x, y, new Color(_toDraw.getRGB(x, y)).getGreen());
//                this.setColor(x, y, applyAlphaLevel(getLevel(x,y)));
//            }
//        }
//        return this;
//    }
    public Map2DColor_OLD loadMapRaw(String filename) throws IOException {
        File f;
        _toDraw = null;
        f = new File(filename);
        _toDraw = ImageIO.read(f);
        _toQuery = new BufferedImage(_toDraw.getWidth(), _toDraw.getHeight(), BufferedImage.TYPE_INT_RGB);
        _lmax = _lmin = -1;
        this.getExtremeHeights();
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                int level, r = new Color(_toDraw.getRGB(x, y)).getRed(),
                        g = new Color(_toDraw.getRGB(x, y)).getGreen(),
                        b = new Color(_toDraw.getRGB(x, y)).getBlue();
                if (r == g && g == b) {
                    level = new Color(_toDraw.getRGB(x, y)).getBlue();
                    level = this.correctAlphaLevel(level);
                    _toQuery.setRGB(x, y, new Color(level, level, level).getRGB());
                } else {
                    _toQuery.setRGB(x, y, _toDraw.getRGB(x, y));
                }
////                this.setLevel(x, y, new Color(_toDraw.getRGB(x, y)).getGreen());
//                this.setColor(x, y, applyAlphaLevel(getLevel(x,y)));
            }
        }
        return this;
    }

    protected Color applyAlphaLevel(int level) {
        int newlevel = correctAlphaLevel(level);
        return new Color(newlevel, newlevel, newlevel);
    }

    public int correctAlphaLevel(int level) {
        int newlevel, maxlevel = 255; //Color.WHITE.getBlue();
        level = (int) (Math.pow(level / (1.0 * maxlevel), k) * maxlevel);
        newlevel = (int) Math.round(level / 5.0 * 5) / 5 * 5;
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
    public Map2DColor_OLD saveMap(String filename) throws IOException {
        File f;

        f = new File(filename);
        ImageIO.write(_toDraw, "PNG", f);
        return this;
    }

    public Map2DColor_OLD saveMapCorrected(String filename) throws IOException {
        File f;

        f = new File(filename);
        ImageIO.write(_toQuery, "PNG", f);
        return this;
    }

    /**
     * Devuelve el ancho de la imagen cargada
     *
     * @return Ancho de la imagen
     */
    public int getWidth() {
        if (this.hasMap()) {
            return _toDraw.getWidth();
        } else {
            return -1;
        }
    }

    public BufferedImage getMap2Draw() {
        return _toDraw;
    }

    public BufferedImage getMap2Query() {
        return _toQuery;
    }

    /**
     * Devuelve el alto de la imagen cargada
     *
     * @return Alto de la imagen
     */
    public int getHeight() {
        if (this.hasMap()) {
            return _toDraw.getHeight();
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
    public int getLevel(int x, int y) {
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
            return new Color(_toQuery.getRGB(x, y)).getBlue();
        } else {
            return -1;
        }
    }

    public Color getColor(int x, int y) {
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
            return new Color(_toDraw.getRGB(x, y));
        } else {
            return Color.RED;
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
    public int getLevel(double x, double y) {
        return getLevel((int) x, (int) y);
    }

    /**
     * Define the grayscale level of a given point
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @param level the givel level for that point (0-255)
     * @return
     */
    public Map2DColor_OLD setLevel(int x, int y, int level) {
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
//            level = this.correctAlphaLevel(level);
            _toQuery.setRGB(x, y, new Color(level, level, level).getRGB());
//            _toDraw.setRGB(x, y,  this.applyAlphaLevel(level).getRGB());
        }
        return this;
    }

    public Map2DColor_OLD setColor(int x, int y, Color c) {
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
            this._toDraw.setRGB(x, y, c.getRGB());
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
    public Map2DColor_OLD setLevel(double x, double y, int level) {
        return setLevel((int) x, (int) y, level);
    }

    /**
     * Comprueba que hay una imagen ya cargada
     *
     * @return true si hay una imagen cargada, false en otro caso
     */
    public boolean hasMap() {
        return (_toDraw != null);
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
                    int level = this.getLevel(x, y);
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

}
