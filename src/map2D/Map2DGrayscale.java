/** *
 * @file DBAMap
 * @author Luis Castillo, DBA, l.castillo@decsai.ugr.es
 */
package map2D;

import geometry.Point3D;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBIntrospector;
import jdk.jfr.Unsigned;
import static map2D.Map2DColor.PIX_MAX;
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
        _map = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
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
        _map = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
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
        _map = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setLevel(x, y, c.getRGB());
            }
        }
    }

  public Map2DGrayscale getMonoFlat2(Histogram horiz, Histogram vert, double phi) {
        Histogram base;
        Map2DGrayscale res = new Map2DGrayscale(getWidth(), getHeight()), aux;
        double dlevel, distances[][];
        double cx = getWidth() / 2, //horiz.getWhichMax(),
                cy = getHeight() / 2, //vert.getWhichMax(),
                maxl = Math.sqrt(cx * cx + cy * cy);
        int xtraMatrix = 25;
        System.out.println("MonoFlat");
        horiz.adjustStops();
        vert.adjustStops();
        distances = new double[getWidth() + 2 * xtraMatrix][getHeight() + 2 * xtraMatrix];
        for (int x = 0; x < distances.length; x++) {
            for (int y = 0; y < distances[0].length; y++) {
                distances[x][y] = -1;
            }
        }
        for (double y = 0; y < getHeight()/2; y++) {
            System.out.print((int) (y * 100 / getHeight()) + "%    ");
            for (double x = 0; x < getWidth()/2; x++) {
                double n = 0, tope = 3, rx, ry, dr;
                for (double shifty = -tope / 2; shifty <= tope / 2; shifty++) {
                    for (double shiftx = -tope / 2; shiftx <= tope / 2; shiftx++) {
                        rx = x;
                        ry = y;
                        rx += shiftx;
                        ry += shifty;
                        /// desde aqui
                        dr = Math.sqrt((rx - cx) * (rx - cx) + (ry - cy) * (ry - cy)) * phi;
                        distances[xtraMatrix + (int) rx][xtraMatrix + (int) ry] = dr;
                        if (rx < cx) {
                            rx = cx - dr;
                        } else if (rx > cx) {
                            rx = cx + dr;
                        } else {
                            rx = cx;
                        }
                        if (ry < cy) {
                            ry = cy - dr;
                        } else if (ry > cy) {
                            ry = cy + dr;
                        } else {
                            ry = cy;
                        }
                        /// Hasta aqui
                        n = n + (horiz.getDValue(rx) + vert.getDValue(ry)) / 2;
                    }
                }
                dlevel = n / (tope * tope);

                int ilevel = (int) Math.max(0, Math.min(PIX_MAX, Math.round(dlevel)));
                res.setColor((int) x, (int) y, new Color(ilevel, ilevel, ilevel));
                res.setColor(res.getWidth()-(int) x, (int) y, new Color(ilevel, ilevel, ilevel));
                res.setColor(res.getWidth()-(int) x, res.getHeight()-(int) y, new Color(ilevel, ilevel, ilevel));
                res.setColor((int) x, res.getHeight()-(int) y, new Color(ilevel, ilevel, ilevel));
            }
        }

//        res.showAndWait("11111");
        for (int k = 1; k < 2; k++) {
            aux = res;
            res = new Map2DGrayscale(getWidth(), getHeight());
            for (double y = 0; y < getHeight(); y++) {
                System.out.print((int) (y * 100 / getHeight()) + "%    ");
                for (double x = 0; x < getWidth(); x++) {
                    double sum = 0, count = 0, tope = 3, rx, ry, dr, draux;
                    dr = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy)) * phi;
//                    dr = distances[xtraMatrix + (int) x][xtraMatrix + (int) y];
                    count = 0;
                    sum = 0;
                    for (double shifty = -tope / 2; shifty <= tope / 2; shifty++) {
                        for (double shiftx = -tope / 2; shiftx <= tope / 2; shiftx++) {
                            rx = x;
                            ry = y;
                            rx += shiftx;
                            ry += shifty;
                            draux = Math.sqrt((rx - cx) * (rx - cx) + (ry - cy) * (ry - cy)) * phi;
//                            draux = distances[xtraMatrix + (int) rx][xtraMatrix + (int) ry];
                            if (draux < dr) {
//                                rx = cx - dr;
//                                ry = cy - dr;
                                // Desde aqui
                                if (rx < cx) {
                                    rx = cx - draux;
                                } else if (rx > cx) {
                                    rx = cx + draux;
                                } else {
                                    rx = cx;
                                }
                                if (ry < cy) {
                                    ry = cy - draux;
                                } else if (ry > cy) {
                                    ry = cy + draux;
                                } else {
                                    ry = cy;
                                }
                                // Hasta aqui
                                sum = sum + aux.getLevel(rx, ry);
                                count++;
                            }
                        }
                    }
                    if (x == cx && y == cy) {
                        dlevel = (horiz.getDValue(x)
                                + vert.getDValue(y)) / 2;
                    } else {
                        dlevel = (sum / count
                                + horiz.getDValue(x)
                                + vert.getDValue(y)) / 2;
                    }

                    int ilevel = (int) Math.max(0, Math.min(PIX_MAX, Math.round(dlevel)));
                    res.setLevel(x, y, ilevel);
                }
            }
        }
        return res;
    }
//
//  
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
//        System.out.println("MAP=" + _map.getType());
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
                int level = getLevel(x, y);
                Color c = new Color(level, level, level);
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
            Color c = new Color(_map.getRGB(x, y));
            return (c.getGreen() + c.getRed() + c.getBlue()) / 3;
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

    public int getLevel(Point3D p) {
        return getLevel(p.getXInt(), p.getYInt());
    }

//    public int adjustLevel(int level, int maxlevel, int newmaxlevel) {
//        level = Math.max(0, Math.min(level, maxlevel));
//        return (int) (level * 1.0 / maxlevel * newmaxlevel);
//    }
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
//        level = adjustLevel(level, PIX_MAX, 255);
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

    public Map2DGrayscale setLevel(Point3D p, int level) {
        return setLevel(p.getXInt(), p.getYInt(), level);
    }

    /**
     * Comprueba que hay una imagen ya cargada
     *
     * @return true si hay una imagen cargada, false en otro caso
     */
    public boolean hasMap() {
        return (_imap != null);
    }

    public Map2DGrayscale push(double ratio) {
        int max = getMaxHeight();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                setLevel(x,y, (int) (getLevel(x,y) + (max - getLevel(x,y)) * ratio));
            }
        }
        return this;
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

    public Map2DGrayscale showAndWait(String title) {
        try {
            JLabel label = new JLabel(new ImageIcon(resize(512, -1).getImage()));
            JOptionPane.showMessageDialog(null, label, title, JOptionPane.PLAIN_MESSAGE, null);
        } catch (Exception ex) {
            SwingTools.Error("Sorry. The image cannot be displayed at this moment");
        }
        return this;
    }

    public Map2DGrayscale show(String title) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                showAndWait(title);
            }
        });
        t.start();
        return this;
    }

    public Histogram getHistogram() {
        Histogram res = new Histogram(PIX_MAX, PIX_MAX);
        res.setLevels(PIX_MAX + 1);
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
        double error = 25, m;
        Histogram res = new Histogram(getWidth(), PIX_MAX);
        res.setLevels(getWidth());
        boolean exit = false, exit2 = false;
        for (int i = 0; i < res.size(); i++) {
            res.setValue(i, 0);
        }
        for (int x = 0; x < res.size(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int level;
                level = this.getLevel(x, y);
                res.addValue(x, level);
            }
        }
        for (int x = 0; x < res.size(); x++) {
            res.setValue(x, res.getValue(x) / getHeight());
        }
        res.normalize(res.getMax());
        res.Smooth(getHeight() / 3);
        res.norender();
        res.push(0.5);
        res.adjustStops();
        return res;
    }

    public Histogram getVerticalStack() {
        int thres = 0;
        double error = 25, m;

        Histogram res = new Histogram(getHeight(), PIX_MAX);
        res.setLevels(getHeight());
        for (int i = 0; i < res.size(); i++) {
            res.setValue(i, 0);
        }
        for (int y = 0; y < res.size(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                int level;
                level = this.getLevel(x, y);
                res.addValue(y, level);
            }
        }
        for (int y = 0; y < res.size(); y++) {
            res.setValue(y, res.getValue(y) / getWidth());
        }
        res.normalize(res.getMax());
        res.Smooth(getHeight() / 3);
        res.norender();
        res.push(0.5);
        res.adjustStops();
        return res;
    }

    public Map2DGrayscale setAlpha(int alphastep) {
        double step = 0.05, dlevel;
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                dlevel = getLevel(x, y) * 1.0 / PIX_MAX;
                dlevel = (dlevel * (1 + alphastep * step)) * PIX_MAX;
                setLevel(x, y, (int) dlevel);
            }
        }
        return this;
    }

//    public Map2DGrayscale getMonoFlat(Histogram horiz, Histogram vert) {
//        Histogram base;
//        Map2DGrayscale res = new Map2DGrayscale(getWidth(), getHeight());
//        double dlevel, dlevelx, dlevely;
//        double xcenter = horiz.getWhichMax(),
//                yxcenter = vert.getWhichMax(),
//                rcenter=getWidth()/2,
//                rycenter=getHeight()/2,
//                rx, ry, len, signx, signy, px, py, ratio = 1.61803;//Math.sqrt(2);
//        System.out.println("Monoflatting");
////        horiz.showAndWait("H");
////        vert.showAndWait("V");
//        System.out.println(horiz.toString());
//        System.out.println(vert.toString());
//        for (int y = getHeight()/2; y <=ycenter; y++) {
//            System.out.print(".");
//            for (int x = 0; x <= xcenter; x++) {
//                rx = x - xcenter;
//                ry = y - ycenter;
//                len = Math.sqrt(rx * rx + ry * ry) / ratio;
//                if (rx != 0) {
//                    signx = rx / Math.abs(rx);
//                } else {
//                    signx = 0;
//                }
//                if (ry != 0) {
//                    signy = ry / Math.abs(ry);
//                } else {
//                    signy = 0;
//                }
//                px = (xcenter + signx * len) / ratio;
//                py = (ycenter + signy * len) / ratio;
//                dlevelx = horiz.getInterpolatedValue(px);
//                dlevely = vert.getInterpolatedValue(py);
//                dlevel = (dlevelx + dlevely) / 2;
//                int ilevel = (int) dlevel; //Math.max(0, Math.min(PIX_MAX - 1, Math.round(dlevel)));
//                res.setColor(x, y, new Color(ilevel, ilevel, ilevel));
//                System.out.print("("+px+") "+dlevel+"-");
//            }
//            System.out.println("");
//        }
//
//        return res;
//    }
//    public Map2DGrayscale getMonoFlat(Histogram horiz, Histogram vert, double phi) {
//        Histogram base;
//        Map2DGrayscale res = new Map2DGrayscale(getWidth(), getHeight()), 
//                closed= new Map2DGrayscale(getWidth(), getHeight());
//        ArrayList <String> open=new ArrayList();
//        double dlevel;
//        double cx = horiz.getWhichMax(), cy = vert.getWhichMax(),
//                maxl = Math.sqrt(cx * cx + cy * cy);
//        System.out.println("MonoFlat");
//        horiz.adjustStops();
//        vert.adjustStops();
//
//        double dr, rx, ry;
//        String spt, spaux;
//        Point3D pt;
//        pt = new Point3D(cx, cy, 0);
//        spt = pt.toString();
//        open.add(spt);
//        int tope = 3, count;
//        double dx, dy;
//        double levelres, sum;
//        Point3D paux;
//        while (!open.isEmpty()) {
//            pt = new Point3D(open.get(0));
//            spt = pt.toString();
//            dx = pt.getXInt(); //cx - (getWidth() * phi / 2) + x * phi;
//            dy = pt.getYInt(); //cy - (getHeight() * phi / 2) + y * phi;
//            count = 1;
//            sum = (horiz.getDValue(dx) + vert.getDValue(dy)) / 2;
//            System.out.println("OPEN= " + open.size() + "\n CURRENT= " + spt+"\n");
//            for (int shifty = -tope / 2; shifty <= tope / 2; shifty++) {
//                for (int shiftx = -tope / 2; shiftx <= tope / 2; shiftx++) {
//                    paux = new Point3D(dx + shiftx, dy + shifty, 0);
//                    spaux = paux.toString();
//                    if (closed.getLevel(paux)!= 0) {
//                        count++;
//                        sum += horiz.getDValue(paux.getXInt()) + vert.getDValue(paux.getYInt());
//                    } else {
//                        int border = 0;
//                        if (!open.contains(spaux)) {
//                            open.add(spaux);
//                        }
//                    }
//                }
//            }
//            levelres = sum / count;
//            res.setLevel(pt, (int) Math.round(levelres));
//            open.remove(0);
//            closed.setLevel(pt, 255);
////            closed.showAndWait("TMP");    
//        }
//        return res;
//    }
//    public Map2DGrayscale getMonoFlat(Histogram horiz, Histogram vert, double phi) {
//        Histogram base;
//        Map2DGrayscale res = new Map2DGrayscale(getWidth(), getHeight());
//        double dlevel;
////        double cx = getWidth() / 2, cy = getHeight() / 2, 
//        double cx = horiz.getWhichMax(), cy = vert.getWhichMax(),
//                maxl = Math.sqrt(cx * cx + cy * cy);
////        System.out.println(horiz.toString());
////        System.out.println(vert.toString());
//        System.out.println("MonoFlat");
//        horiz.adjustStops();
//        vert.adjustStops();
////        horiz.norender().show("H");
////        vert.norender().showAndWait("V");
//
//        Map2DGrayscale already = new Map2DGrayscale(getWidth(), getHeight(), 0);
//        double dr, rx, ry;
//        for (int radius = 0; radius < getWidth()/2; radius++) {
//            System.out.println("radius "+radius);
//            for (double y = cy - radius; y <= cy + radius; y+=1) {
//                for (double x = cx - radius; x <= cx + radius; x+=1) {
//                    rx = x*phi;
//                    ry = y *phi;
//                    dr = Math.sqrt((rx - cx) * (rx - cx) + (ry - cy) * (ry - cy));
//                    if (Math.abs(Math.round(dr) - radius*phi) <0.5) {
//                        already.setLevel((int)x, (int)y, 255);
//                        dlevel = (horiz.getDValue(cx + dr) + vert.getDValue(cy + dr))/2;
//                        int ilevel = (int) Math.max(0, Math.min(PIX_MAX, dlevel)); 
//                        res.setLevel((int) x, (int) y,ilevel);
//                    }
//                }
//            }
//        }
////        already.showAndWait("ff");
//        return res;
//    }
//
//    public Map2DGrayscale getMonoFlat(Histogram horiz, Histogram vert, double phi) {
//        Histogram base;
//        Map2DGrayscale res = new Map2DGrayscale(getWidth(), getHeight()), aux;
//        double dlevel;
//        double cx = getWidth() / 2, // horiz.getWhichMax(), 
//                cy = getHeight() / 2, //vert.getWhichMax(),
//                maxl = Math.sqrt(cx * cx + cy * cy);
//        System.out.println("MonoFlat");
//        horiz.adjustStops();
//        vert.adjustStops();
//        for (double y = 0; y <= getHeight(); y++) {
//            System.out.print((int) (y * 100 / getHeight()) + "%    ");
//            for (double x = 0; x <= getWidth(); x++) {
//                double n = 0, tope = 5, rx, ry, dr;
//                for (double shifty = -tope / 2; shifty <= tope / 2; shifty++) {
//                    for (double shiftx = -tope / 2; shiftx <= tope / 2; shiftx++) {
////                        rx = (1 - phi) * getWidth() / 2 + rx * phi;
////                        ry = (1 - phi) * getHeight() / 2 + ry * phi;
//                        rx = x;
//                        ry = y;
//                        rx += shiftx;
//                        ry += shifty;
//                        dr = Math.sqrt((rx - cx) * (rx - cx) + (ry - cy) * (ry - cy)) * phi;
//                        if (rx < cx) {
//                            rx = cx - dr;
//                        } else if (rx > cx) {
//                            rx = cx + dr;
//                        } else {
//                            rx = cx;
//                        }
//                        if (ry < cy) {
//                            ry = cy - dr;
//                        } else if (ry > cy) {
//                            ry = cy + dr;
//                        } else {
//                            ry = cy;
//                        }
////                        rx = x + shiftx;
////                        ry = y + shifty;
//                        n = n + horiz.getDValue(rx) + vert.getDValue(ry);
//                    }
//                }
//                dlevel = n / (tope * tope);
//
//                int ilevel = (int) Math.max(0, Math.min(PIX_MAX, dlevel)); //adjustLevel((int) dlevel, horiz.getMax() + vert.getMax(), PIX_MAX); //int) Math.min(PIX_MAX, Math.round(dlevel));
//                res.setColor((int) x, (int) y, new Color(ilevel, ilevel, ilevel));
//            }
//        }
//        aux = res;
//        res = new Map2DGrayscale(getWidth(), getHeight());
//        for (double y = 0; y <= getHeight() / 2; y++) {
//            System.out.print((int) (y * 100 / getHeight()) + "%    ");
//            for (double x = 0; x <= getWidth() / 2; x++) {
//                double sum = 0, count = 0, tope = 3, rx, ry, dr, draux;
//                dr = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy)) * phi;
//                count = 0;
//                sum = 0;
//                for (double shifty = -tope / 2; shifty <= tope / 2; shifty++) {
//                    for (double shiftx = -tope / 2; shiftx <= tope / 2; shiftx++) {
////                        rx = (1 - phi) * getWidth() / 2 + rx * phi;
////                        ry = (1 - phi) * getHeight() / 2 + ry * phi;
//                        rx = x;
//                        ry = y;
//                        rx += shiftx;
//                        ry += shifty;
//                        draux = Math.sqrt((rx - cx) * (rx - cx) + (ry - cy) * (ry - cy)) * phi;
//                        if (draux < dr) {
//                            rx = cx - dr;
//                            ry = cy - dr;
//                            sum = sum + aux.getLevel(rx, ry);
//                            count++;
//                        }
//                    }
//                }
//                dlevel = (sum / count +
//                        horiz.getDValue(x) 
//                        + vert.getDValue(y))/2;
//
//                int ilevel = (int) Math.max(0, Math.min(PIX_MAX, dlevel)); //adjustLevel((int) dlevel, horiz.getMax() + vert.getMax(), PIX_MAX); //int) Math.min(PIX_MAX, Math.round(dlevel));
//                Color c = new Color(ilevel, ilevel, ilevel);
//                res.setColor((int) x, (int) y, c);
//                res.setColor(getWidth()-1-(int) x, (int) y, c);
//                res.setColor(getWidth()-1-(int) x, getHeight()-1-(int) y, c);
//                res.setColor((int) x, getHeight()-1-(int) y, c);
//            }
//        }
//
//        return res;
//    }
  
    public Map2DGrayscale getMonoFlat(Histogram horiz, Histogram vert, double phi) {
        Histogram base;
        Map2DGrayscale res = new Map2DGrayscale(getWidth(), getHeight());
        double dlevel;
//        double cx = getWidth() / 2, cy = getHeight() / 2, 
        double cx = horiz.getWhichMax(), cy = vert.getWhichMax(),
                maxl = Math.sqrt(cx * cx + cy * cy);
//        System.out.println(horiz.toString());
//        System.out.println(vert.toString());
        System.out.println("MonoFlat");
        horiz.adjustStops();
        vert.adjustStops();
//        horiz.norender().show("H");
//        vert.norender().showAndWait("V");
        for (double y = 0; y < getHeight(); y++) {
            System.out.print((int) (y * 100 / getHeight()) + "%    ");
            for (double x = 0; x < getWidth(); x++) {

//                res.setLevel(x, y, 
//                        (int)(PIX_MAX*Math.sqrt(Math.pow(horiz.getValue(x)/horiz.getMax(),2)+ Math.pow(vert.getValue(y)/vert.getMax(),2)))); 
//                dlevel = Math.sqrt(horiz.getValue(x)*vert.getValue(y));
//                    dlevel = (horiz.getValue(x)+vert.getValue(y))/2;
//                int tope = 3;
//                double dx, dy;
//                dx = x; //cx - (getWidth() * phi / 2) + x * phi;
//                dy = y; //cy - (getHeight() * phi / 2) + y * phi;
////                System.out.println(x+"-"+y+"-->"+dx+"-"+dy);
//                for (int shifty = -tope / 2; shifty <= tope / 2; shifty++) {
//                    for (int shiftx = -tope / 2; shiftx <= tope / 2; shiftx++) {
//                        n = n + horiz.getInterpolatedValue(dx + shiftx) + vert.getInterpolatedValue(dy + shifty);
//                    }
//                }
//                dlevel = n / (tope * tope);

                
                
//                double n = 0, tope = 5, rx, ry, dr;
//                for (double shifty = -tope / 2; shifty <= tope / 2; shifty++) {
//                    for (double shiftx = -tope / 2; shiftx <= tope / 2; shiftx++) {
////                        rx = (1 - phi) * getWidth() / 2 + rx * phi;
////                        ry = (1 - phi) * getHeight() / 2 + ry * phi;
//                        rx=x;
//                        ry=y;
//                        rx += shiftx;
//                        ry += shifty;
//                        dr = Math.sqrt((rx - cx) * (rx - cx) + (ry - cy) * (ry - cy))*0.5;
//                        if (rx <= cx) {
//                            rx = cx - dr;
//                        } else if (rx > cx) {
//                            rx = cx + dr;
//                        } else {
//                            rx = cx;
//                        }
//                        if (ry <= cy) {
//                            ry = cy - dr;
//                        } else if (ry > cy) {
//                            ry = cy + dr;
//                        } else {
//                            ry = cy;
//                        }
////                        rx = x + shiftx;
////                        ry = y + shifty;
//                        n = n + (horiz.getDValue(rx) + vert.getDValue(ry))/2;
//                    }
//                }
//                dlevel = n / (tope * tope);


//ESTA
                double n = 0, tope = 5, rx, ry;
                for (double shifty = -tope / 2; shifty <= tope / 2; shifty++) {
                    for (double shiftx = -tope / 2; shiftx <= tope / 2; shiftx++) {
                        rx = x + shiftx;
                        ry = y + shifty;
                        
                        rx = (1 - phi) * getWidth() / 2 + rx * phi;
                        ry = (1 - phi) * getHeight() / 2 + ry * phi;
                        n = n + horiz.getDValue(rx) + vert.getDValue(ry);
                    }
                }
                dlevel = n / (tope * tope);

//
//                dlevel = (horiz.getValue(x) + vert.getValue(y) + horiz.getValue(x - 1) + vert.getValue(y - 1) + horiz.getValue(x + 1) + vert.getValue(y + 1)
//                        + horiz.getValue(x - 1) + vert.getValue(y) + horiz.getValue(x) + vert.getValue(y - 1) + horiz.getValue(x + 1) + vert.getValue(y)
//                        + horiz.getValue(x) + vert.getValue(y + 1)
//                        + horiz.getValue(x - 1) + vert.getValue(y + 1) + horiz.getValue(x + 1) + vert.getValue(y - 1)) / 9.0
//                        * (maxl - Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy)) / 10) / (maxl);
//                    dlevel = Math.min(horiz.getValue(x), vert.getValue(y));
//                dlevel = (int)Math.min(255,horiz.getValue(x)+vert.getValue(y));
//                res.setLevel(x, y, 
//                        (int)(horiz.getValue(x)*vert.getValue(y)/(horiz.PIX_MAX*vert.PIX_MAX)));
//                double realx = xmin+x*1.0/getWidth()*xlong, 
//                        realy = ymin+y*1.0/getHeight()*ylong;
//                dlevel = (Math.sqrt(Math.pow(horiz.getInterpolatedValue(realx), 2) + Math.pow(vert.getInterpolatedValue(realy), 2)));
//                dlevel = Math.round(Math.sqrt(Math.pow(horiz.getValue(x), 2) + Math.pow(vert.getValue(y), 2)));
///                dlevel = (horiz.getValue(x)+vert.getValue(y))/2.0;
//                res.setLevel(x, y, (int) ((Math.sqrt(horiz.getValue(x) * vert.getValue(y)))));
//                res.setLevel(x, y, (int)((Math.sqrt(horiz.getValue(x) * vert.getValue(y)))/
//                        (Math.sqrt(horiz.getMax() * vert.getValue(y))));
//                dlevel = (Math.min(horiz.getValue(x) , vert.getValue(y)));
//                dlevel = (Math.max(horiz.getValue(x) , vert.getValue(y)));
//                res.setLevel(x, y, (int) (Math.max(horiz.getValue(x) , vert.gçetValue(y))));
//                double dr, dx, dy, rx ,ry;
//                double n = 0, tope = 3;
//                for (double shifty = -tope / 2; shifty <= tope / 2; shifty++) {
//                    for (double shiftx = -tope / 2; shiftx <= tope / 2; shiftx++) {
//                        rx =x+shiftx;
//                        ry =y+shifty;
//                        dr = Math.sqrt((rx - cx) * (rx - cx) + (ry - cy) * (ry - cy)) * phi;
//                        if (rx < cx) {
//                            dx = cx - dr;
//                        } else if (rx > cx) {
//                            dx = cx + dr;
//                        } else {
//                            dx = cx;
//                        }
//                        if (ry < cy) {
//                            dy = cy - dr;
//                        } else if (ry > cy) {
//                            dy = cy + dr;
//                        } else {
//                            dy = cy;
//                        }
//                        n = n + horiz.getDValue(dx) + vert.getDValue(dy);
//                    }
//                }
//                dlevel = n / (tope * tope);
//                dr = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy)) * phi;
//                if (x < cx) {
//                    dx = cx - dr;
//                } else if (x > cx) {
//                    dx = cx + dr;
//                } else {
//                    dx = cx;
//                }
//                if (y < cy) {
//                    dy = cy - dr;
//                } else if (y > cy) {
//                    dy = cy + dr;
//                } else {
//                    dy = cy;
//                }
////                System.out.println(x + "-" + y + "--" + dr + "-->" + dx + "-" + dy);
//                dlevel = (horiz.getDValue(dx) + vert.getDValue(dy)) / 2;
                dlevel *= (maxl - Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy)) / 20) / (maxl);
//                dlevel *= (maxl-getWidth()*Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy)))/maxl;
                int ilevel = (int) Math.max(0, Math.min(PIX_MAX, dlevel)); //adjustLevel((int) dlevel, horiz.getMax() + vert.getMax(), PIX_MAX); //int) Math.min(PIX_MAX, Math.round(dlevel));
//                System.out.print(ilevel+"-_");
                res.setColor((int) x, (int) y, new Color(ilevel, ilevel, ilevel));
                //                res.setLevel(x, y, ilevel);
            }
        }
        return res;
    }

}
