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
public class Map2DColor {

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
     * @param c The default color
     * image
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
        this.getExtremeHeights();

        return this;
    }

    public Map2DColor loadMapRaw(String filename) throws IOException {
        File f;
        f = new File(filename);
        this._map = ImageIO.read(f);
        _lmax = _lmin = -1;
        this.getExtremeHeights();

        return this;
    }

    public  Map2DColor shiftLeft(int pix) {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth()-pix; x++) {
                _map.setRGB(x, y, _map.getRGB(x+pix, y));
            }
        }
        return this;
    }

    protected Map2DColor normalize() {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                _map.setRGB(x, y, this.applyAlphaLevel(getColor(x,y)).getRGB());
            }
        }
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
            return new Color(_map.getRGB(x, y)).getGreen();
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
    public int getStepLevel(int x, int y) {
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
            return getRawLevel(x,y)/5*5;
        } else {
            return -1;
        }
    }

    public Color getColor(int x, int y) {
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
            return new Color(_map.getRGB(x, y));
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
            _map.setRGB(x, y, new Color(level, level, level).getRGB());
//            _map.setRGB(x, y,  this.applyAlphaLevel(level).getRGB());
        }
        return this;
    }

    public Map2DColor setColor(int x, int y, Color c) {
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
            this._map.setRGB(x, y, c.getRGB());
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

}
