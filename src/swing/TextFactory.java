/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import geometry.Point3D;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import javax.swing.SwingConstants;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class TextFactory {

    int x, y, width, fontSize, valign, halign, angle, textStyle, height, ndigits, value;
    String sText, sFontName;

    Graphics2D g;
    FontMetrics fm;
    Font newFont, oldFont;
    Rectangle bounds;

    public TextFactory(Graphics2D g) {
        this.g = g;
        oldFont = g.getFont();
        fm = g.getFontMetrics();
        x = 0;
        y = 0;
        width = -1;
        sText = "";
        sFontName = oldFont.getFontName();
        fontSize = oldFont.getSize();
        sFontName = oldFont.getName();
        valign = SwingConstants.TOP;
        halign = SwingConstants.LEFT;
        textStyle = oldFont.getStyle();
        angle = 0;
    }

    public TextFactory setValue(int value, int ndigits) {
        String sFormat, sRes;
        if (ndigits <= 0) {
            ndigits = 4;
        }
        sFormat = "%0" + ndigits + "d";
        sText = String.format(sFormat, value);
        return this;
    }

    public TextFactory validate() {
        String format, sread;
        boolean fixed = false;
        if (width > 0) {
            newFont = new Font(sFontName, textStyle, fontSize);
            fm = g.getFontMetrics(newFont);
            double d = fm.stringWidth(sText) * 1.0 / sText.length();
            fontSize = (int) (width / (sText.length()));
            newFont = new Font(sFontName, textStyle, fontSize);
            fm = g.getFontMetrics(newFont);
            width = fm.stringWidth(sText);
            height = fontSize * (fm.getAscent() + fm.getDescent()) / fm.getAscent();
        } else {
            newFont = new Font(sFontName, textStyle, fontSize);
            fm = g.getFontMetrics(newFont);
            width = fm.stringWidth(sText);
            height = (int) (fontSize * (fm.getAscent() + fm.getDescent() * 1.0) / fm.getAscent());
        }
        if (angle != 0) {
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.rotate(Math.toRadians(angle), width / 2, -height / 2);
            newFont = newFont.deriveFont(affineTransform);
        }
        if (halign == SwingConstants.RIGHT) {
            x -= width;
        } else if (halign == SwingConstants.CENTER) {
            x -= width / 2;
        }
        if (valign == SwingConstants.TOP) {
            y += fontSize;
        } else if (valign == SwingConstants.CENTER) {
            y += fontSize / 2;
        }
        g.setFont(newFont);
        bounds = getStringBounds(g, sText, x, y);
        return this;
    }

    public void draw() {
        if (bounds == null) {
            validate();
        }
        g.setFont(newFont);
        g.drawString(sText, x, y);
        g.setFont(newFont);
    }

    protected Rectangle getStringBounds(Graphics2D g2, String str,
            float x, float y) {
        FontRenderContext frc = g2.getFontRenderContext();
        GlyphVector gv = g2.getFont().createGlyphVector(frc, str);
        return gv.getPixelBounds(null, x, y);
    }

    public int getX() {
        return x;
    }

    public TextFactory setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public TextFactory setY(int y) {
        this.y = y;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public TextFactory setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getFontSize() {
        return fontSize;
    }

    public TextFactory setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public int getValign() {
        return valign;
    }

    public TextFactory setValign(int valign) {
        this.valign = valign;
        return this;
    }

    public int getHalign() {
        return halign;
    }

    public TextFactory setHalign(int halign) {
        this.halign = halign;
        return this;
    }

    public int getAngle() {
        return angle;
    }

    public TextFactory setAngle(int angle) {
        this.angle = angle;
        return this;
    }

    public int getTextStyle() {
        return textStyle;
    }

    public TextFactory setTextStyle(int textStyle) {
        this.textStyle = textStyle;
        return this;
    }

    public String getsText() {
        return sText;
    }

    public TextFactory setsText(String sText) {
        this.sText = sText;
        return this;
    }

    public String getsFontName() {
        return sFontName;
    }

    public TextFactory setsFontName(String sFontName) {
        this.sFontName = sFontName;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getNdigits() {
        return ndigits;
    }

    public TextFactory setNdigits(int ndigits) {
        this.ndigits = ndigits;
        return this;
    }

    public int getValue() {
        return value;
    }

    public TextFactory setValue(int value) {
        this.value = value;
        if (value != Perceptor.NULLREAD) {
            String format;
            if (getNdigits() > 0) {
                format = "%0" + getNdigits() + "d";
            } else {
                format = "%d";
            }
            this.setsText(String.format(format, value));
        }
        else  {
            setsText("---");
        }
        return this;
    }

    public TextFactory setPoint(Point3D p) {
        setX(p.getXInt());
        setY(p.getYInt());
        return this;
    }
}
