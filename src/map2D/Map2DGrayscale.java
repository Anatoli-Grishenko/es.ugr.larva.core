/** *
 * @file DBAMap
 * @author Luis Castillo, DBA, l.castillo@decsai.ugr.es
 */
package map2D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jdk.jfr.Unsigned;
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
public class Map2DGrayscale {

    protected BufferedImage _map;
    protected int _lmax, _lmin;
    protected int _imap[][];

    /**
     *
     * Default builder
     */
    public Map2DGrayscale() {
        _map = null;
        _imap = null;
        _lmax = _lmin = -1;
    }

    /**
     * Builder#1
     *
     * @param width Number con columns
     * @param height Number of rows
     */
    public Map2DGrayscale(int width, int height) {
        _lmax = _lmin = -1;
        _imap = new int[height][width];
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
    public Map2DGrayscale(int width, int height, int level) {
        _lmax = _lmin = -1;
        _imap = new int[height][width];
        _map = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setLevel(x, y, level);
            }
        }
    }

    /**
     * Builder #3
     *
     * @param width Number con columns
     * @param height Number of rows
     * @param c Default color image
     */
    public Map2DGrayscale(int width, int height, Color c) {
        _lmax = _lmin = -1;
        _imap = new int[height][width];
        _map = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setLevel(x, y, c.getRGB());
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
    public Map2DGrayscale loadMap(String filename) throws IOException {
        File f;
        double k = 2.261566516;

        _map = null;
        f = new File(filename);
        _map = ImageIO.read(f);
        _lmax = _lmin = -1;
        _imap = new int[_map.getHeight()][_map.getWidth()];
        this.getExtremeHeights();
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                int oldlevel, newlevel;
                oldlevel = (int) (Math.pow(new Color(_map.getRGB(x, y)).getGreen() / 255.0, k) * 255);
                newlevel = (int) Math.floor(oldlevel / 5.0) * 5;
                this.setColor(x, y, new Color(newlevel, newlevel, newlevel));
            }
        }
        System.out.println("MAP=" + _map.getType());
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
    public Map2DGrayscale saveMap(String filename) throws IOException {
        File f;

        f = new File(filename);
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                Color c = new Color(getLevel(x, y), getLevel(x, y), getLevel(x, y));
                _map.setRGB(x, y, c.getRGB());
            }
        }
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
        int max = 0;
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (getLevel(x, y) > max) {
                    max = getLevel(x, y);
                }
            }
        }
        return max;
    }

    public int getAvrgHeight() {
        return getMaxHeight();
//        double max = 0;
//        for (int y = 0; y < getHeight(); y++) {
//            for (int x = 0; x < getWidth(); x++) {
//                max += getLevel(x, y);
//            }
//        }
//        return (int) ((max * 1.0) / (getWidth() * 1.0 * getHeight()));
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
            return new Color(_map.getRGB(x, y)).getGreen();
//            return _imap[y][x];
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
    public Map2DGrayscale setLevel(int x, int y, int level) {
//        System.out.print(level+"-");
        level = Math.max(0, Math.min(level, 255));
        if (this.hasMap() && 0 <= x && x < this.getWidth() && 0 <= y && y < this.getHeight()) {
            _imap[y][x] = level;
            this._map.setRGB(x, y, new Color(level, level, level).getRGB());
        }
        return this;
    }

    public Map2DGrayscale setColor(int x, int y, Color c) {
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
    public Map2DGrayscale setLevel(double x, double y, int level) {
        return setLevel((int) x, (int) y, level);
    }

    /**
     * Comprueba que hay una imagen ya cargada
     *
     * @return true si hay una imagen cargada, false en otro caso
     */
    public boolean hasMap() {
        return (_imap != null);
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

    public BufferedImage getImage() {
        return _map;
    }

    public void setMap(BufferedImage _map) {
        this._map = _map;
    }

    public Map2DGrayscale resize(int width, int height) {
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
        Map2DGrayscale res = new Map2DGrayscale(realw, realh);
        BufferedImage _map2 = new BufferedImage(realw, realh, res.getMap().getType());
        Image mapres = _map.getScaledInstance(realw, realh, Image.SCALE_SMOOTH);
        Graphics2D g2d = _map2.createGraphics();
        g2d.drawImage(mapres, 0, 0, null);
        g2d.dispose();
        Map2DColor aux = new Map2DColor();
        aux.setMap(_map2);
        res.setMap(aux.getGrayscaleImage());

        return res;
    }

    public void showAndWait(String title) {
        try {
            JLabel label = new JLabel(new ImageIcon(resize(512, -1).getImage()));
            JOptionPane.showMessageDialog(null, label, title, JOptionPane.PLAIN_MESSAGE, null);
        } catch (Exception ex) {
            SwingTools.Error("Sorry. The image cannot be displayed at this moment");
        }
    }

    public void show(String title) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                showAndWait(title);
            }
        });
        t.start();

    }

    public Histogram getHistogram() {
        Histogram res = new Histogram(getMaxLevel(), 200);
        res.setLevels(getMaxLevel());
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int level = this.getLevel(x, y);
                res.addValue(level, 1);
            }
        }
//        System.out.println(res.toString());
        res.render();
//        System.out.println(res.toString());
        return res;
    }

    public Histogram getHorizontalStack() {
        int thres = 0;
//        for (int i = 0; i < getWidth(); i++) {
//            thres += getLevel(i, 0);
//        }
//        thres = thres / getWidth();
        thres= (int)(this.getHistogram().getAvrg());
        Histogram res = new Histogram(getWidth(), 200);
        res.setLevels(getWidth());
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int level;
                level = this.getLevel(x, y);
//                level = Math.min(thres, this.getLevel(x, y));
                res.addValue(x, level);
            }
        }
        for (int x = 0; x < getWidth(); x++) {
            res.setValue(x, res.getValue(x) / getHeight());
        }
//        System.out.println(res.toString());
        res.normalize(getAvrgHeight());
//res.pseudoNormalize();
        
//        System.out.println(res.toString());
        res.Smooth();
//        System.out.println(res.toString());
        res.render();
        return res;
    }

    public Histogram getVerticalStack() {
        int thres = 0;
//        for (int i = 0; i < getHeight(); i++) {
//            thres += getLevel(0, i);
//        }
//        thres = thres / getHeight();
        thres= (int)(this.getHistogram().getAvrg() );
        Histogram res = new Histogram(getHeight(), 200);
        res.setLevels(getHeight());
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                int level;
                level = this.getLevel(x, y);
//                level = Math.min(thres, this.getLevel(x, y));
                res.addValue(y, level);
            }
        }
        for (int y = 0; y < getHeight(); y++) {
            res.setValue(y, res.getValue(y) / getWidth());
        }
        res.normalize(getAvrgHeight());
//res.pseudoNormalize();
        res.Smooth();
        res.render();
        return res;
    }

    public Map2DGrayscale getMonoFlat(Histogram horiz, Histogram vert) {
        Histogram  base;
        horiz = getHorizontalStack(); //.normalize(getAvrgHeight());
        vert = getVerticalStack(); //.normalize(getAvrgHeight());
        Map2DGrayscale res = new Map2DGrayscale(getWidth(), getHeight());
        double dlevel;
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
//                res.setLevel(x, y, 
//                        (int)(getMaxLevel()*Math.sqrt(Math.pow(horiz.getValue(x)/horiz.getMax(),2)+ Math.pow(vert.getValue(y)/vert.getMax(),2)))); 

                dlevel = Math.sqrt(horiz.getValue(x)*vert.getValue(y));

//                dlevel = (horiz.getValue(x)+vert.getValue(y))/2.0;

//                dlevel = (horiz.getValue(x)+vert.getValue(y));

//                res.setLevel(x, y, 
//                        (int)(horiz.getValue(x)*vert.getValue(y)/(horiz.getMaxLevel()*vert.getMaxLevel())));

//                        dlevel = (Math.sqrt(Math.pow(horiz.getValue(x),2)+ Math.pow(vert.getValue(y),2))); 

//                Math.sqrt(Math.pow(horiz.getMax(),2)+ Math.pow(vert.getMax(),2))*getMaxLevel()));

//                res.setLevel(x, y, (int) ((Math.sqrt(horiz.getValue(x) * vert.getValue(y)))));
                
//                res.setLevel(x, y, (int)((Math.sqrt(horiz.getValue(x) * vert.getValue(y)))/
//                        (Math.sqrt(horiz.getMax() * vert.getValue(y))));

//                dlevel = (Math.min(horiz.getValue(x) , vert.getValue(y)));

//                res.setLevel(x, y, (int) (Math.max(horiz.getValue(x) , vert.getValue(y))));
                int ilevel = (int) Math.round(dlevel);
//                System.out.print(ilevel+"-_");
                res.setColor(x, y, new Color(ilevel, ilevel, ilevel));
//                res.setLevel(x, y, ilevel);

            }
        }
//        res.getHistogram().render().showAndWait("????");
        return res;
    }

    public int getMaxLevel() {
        return 256; //(int) Math.pow(2, 8) - 1;
    }

}
