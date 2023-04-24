/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import crypto.Keygen;
import geometry.Point3D;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import world.Perceptor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class LabelFactory {

    Font myFont;
    Color myColor;
    HashMap<String, JLabel> mapLabels = new HashMap();
    String tempCode;
    OleApplication myApp;

    public LabelFactory(OleApplication oapp) {
        myApp = oapp;
    }

    public LabelFactory newLabel(String code, String text) {
        JLabel jlaux = new JLabel(text);
        if (myFont != null) {
            jlaux.setFont(myFont);
        }
        mapLabels.put(code, jlaux);
        tempCode = code;
        return this;
    }

    public LabelFactory getCode(String code) {
        tempCode = code;
        return this;
    }

    public LabelFactory setFont(Font f) {
        if (mapLabels.get(tempCode) != null) {
            JLabel jlaux = validate();
            jlaux.setFont(f);
        } else {
            myFont = f;
        }
        return this;
    }

    public LabelFactory setForeground(Color fg) {
        JLabel jlaux = validate();
        jlaux.setForeground(fg);
        return this;
    }

    public LabelFactory setIcon(String iconname) {
        JLabel jlaux = validate();
        jlaux.setIcon(myApp.getIconSet().getRegularIcon(iconname, myFont.getSize() * 2, myFont.getSize() * 2));
        return this;
    }

    public String getText() {
        JLabel jlaux = validate();
        return jlaux.getText();
    }
    public LabelFactory setText(String text) {
        JLabel jlaux = validate();
        jlaux.setText(text);
        return this;
    }

    public JLabel validate() {
        if (tempCode == null) {
            tempCode = Keygen.getHexaKey(6);
            mapLabels.put(tempCode, new JLabel());
        }
        return mapLabels.get(tempCode);
    }

}
