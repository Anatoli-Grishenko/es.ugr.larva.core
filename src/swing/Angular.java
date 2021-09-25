/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class Angular {

    protected int x, y, w, h, cx, cy, thick, real, starta = -45, longa = 270;
    protected Color undone, done, panel;

    public Angular() {
        setBackground(Color.GRAY);
        setColor(Color.GREEN);
        setThick(10);
        setPreferredSize(100);
        setValue(0);
    }

    public Angular setPreferredSize(int w) {
        this.w = w;
        this.h = w;
        cx = w / 2;
        cy = h / 2;
        return this;
    }

    public Angular setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Angular setThick(int t) {
        thick = t;
        return this;
    }

    public Angular setBackground(Color b) {
        undone = b;
        return this;
    }

    public Angular setColor(Color b) {
        done = b;
        return this;
    }

    public Angular setValue(int v) {
//        v = (v<0?(360+90+v)%360:(360+90-v)%360);
//        v = 360+90-v;
//        real = v%360;
        real = v;
        starta = v - 5;
        longa = 10;
        return this;
    }

    public Angular showAngle(Graphics2D g) {
        Color fg = g.getColor(), bg = g.getBackground();
        g.setColor(undone);
        g.fillArc(x, y, w, h, 0, 360);
        g.setColor(done);
        g.fillArc(x, y, w, h, starta, longa);
        g.setColor(bg);
        g.fillArc(x + thick / 2, y + thick / 2, w - thick, h - thick, 0, 360);
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        String label = String.format(" %03dº", (int) real);
        g.drawString(label, x + cx - fm.stringWidth(label) / 2, y);
        return this;
    }
}
