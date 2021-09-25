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
public class RoundProgressBar {
    protected int x, y, w, h, cx, cy, thick, min, max, real, starta=-45, longa=270;
    protected Color undone, done, panel;
    protected String units="";
    
    
    public RoundProgressBar(int vmin, int vmax) {
        min = vmin;
        max = vmax;
        setBackground(Color.GRAY);
        setColor(Color.GREEN);
        setThick(10);
        setPreferredSize(100);
        setValue(min);
    }
    
    public RoundProgressBar setMaxValue(int vmax) {
        max = vmax;
        return this;
    }
    
    
    public RoundProgressBar setUnits(String u) {
        units=u;
        return this;
    }
    
    
    public RoundProgressBar setPreferredSize(int w) {
        this.w = w;
        this.h =w;
        cx = w/2;
        cy=h/2;
        return this;
    }
    
    public RoundProgressBar setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    public RoundProgressBar setThick(int t) {
        thick = t;
        return this;
    }
    
    public RoundProgressBar setBackground(Color b) {
        undone = b;
        return this;
    }
    
    public RoundProgressBar setPanelbckgr(Color b) {
        panel = b;
        return this;
    }
    
    public RoundProgressBar setColor(Color b) {
        done = b;
        return this;
    }
    
    public RoundProgressBar setValue(int v) {
        real = Math.min(Math.max(min, v), max);
        return this;
    }
    
    public RoundProgressBar showProgressBar(Graphics2D g) {
        Color fg=g.getColor(), bg=g.getBackground();
        g.setColor(done);
        g.fillArc(x, y, w, h, starta, longa);
        g.setColor(undone);
        g.fillArc(x, y, w, h, starta, (int)(longa*((max-real*1.0)/(max-min))));
        g.setColor(bg);
        g.fillArc(x+thick/2, y+thick/2, w-thick, h-thick, 0,360);
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        String label = String.format(" %04d %s", (int) real, units);
        g.drawString(label, x+cx-fm.stringWidth(label)/2,y+cy);
        return this;
    }
}
