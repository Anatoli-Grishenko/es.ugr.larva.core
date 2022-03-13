/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import map2D.Map2DColor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OlePerformeter extends JLabel {

    Map2DColor chart;
    final int scale = 5;
    Component parent;
    int maxValue, counter;
    Color background, grid, data;

    public OlePerformeter(Component p, int width, int height, int maxvalue) {
        super();
        parent = p;
        chart = new Map2DColor(width, height);
        setPreferredSize(new Dimension(width, height));
        maxValue = maxvalue;
        counter = 0;
        background = parent.getBackground();
        grid = Color.BLACK;
        data = OleApplication.DodgerBlue;
        init();
    }

    public void pushData(int value) {
        counter++;
        chart.shiftLeft(1);
        drawBackground(0);
        drawGrid(0);
        drawData(0, value);
        this.repaint();
    }

    protected void init() {
        for (int x = 0; x < chart.getWidth(); x++) {
            drawBackground(x);
            drawGrid(x);
        }
        this.repaint();
    }

    protected void drawBackground(int x) {
        for (int y = 0; y < chart.getHeight(); y++) {
            chart.setColor(chart.getWidth() - x - 1, y, background);
        }
    }

    protected void drawGrid(int x) {
//        for (int y = 0; y < chart.getHeight(); y++) {
//            if (y == 0|| y == chart.getHeight() - 1) { //|| y % scale == 0 
//                chart.setColor(chart.getWidth() - x - 1, y, grid);
//            }
//        }
        for (int y = 0; y < chart.getHeight(); y++) {
            if (y == 0|| y == chart.getHeight() - 1 || y % scale == 0) { 
                chart.setColor(chart.getWidth() - x - 1, y, grid);
            } else {
                if ((x + counter) % scale == 0) {
                    chart.setColor(chart.getWidth() - x - 1, y, grid);
                }
            }
        }
    }

    protected void drawData(int x, int value) {
        int maxy = value * chart.getHeight() / maxValue;
//        System.out.println("Perf:"+maxy);
        for (int y = 0; y < maxy; y++) {
            chart.setColor(chart.getWidth() - x - 1, chart.getHeight() - y - 1, data);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(chart.getMap(), 0, 0, null);
    }

//    @Override
//    public void paintIcon(Component c, Graphics g, int x, int y) {
//        g.drawImage(chart.getMap(), x, y, parent);
//    }
//
//    @Override
//    public int getIconWidth() {
//        return chart.getWidth();
//    }
//
//    @Override
//    public int getIconHeight() {
//        return chart.getHeight();
//    }

}
