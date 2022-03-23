/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import javax.swing.JPanel;
import world.SensorDecoder;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleDashBoard extends OleDrawPane {
    public final Color cDeck=Color.GRAY, cFrame=Color.DARK_GRAY, cGauge=new Color(0,15,0), 
            cGoal=Color.YELLOW, cPath=Color.PINK, cCompass=new Color(0,75,0), cLabels=SwingTools.doDarker(Color.WHITE); 

    protected HashMap<String, OleSensor> mySensorsVisual;
    protected Component myParent;
    protected SensorDecoder decoder;

    public OleDashBoard(Component parent) {
        myParent = parent;
        mySensorsVisual = new HashMap();
        decoder=new SensorDecoder();
        this.setLayout(null);
    }

    @Override
    public void OleDraw(Graphics2D g) {
        myg = g;        

//        myg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (String s : mySensorsVisual.keySet()) {
            mySensorsVisual.get(s).viewSensor(g);
        }
//        System.out.println("Repaint");
   }

    public void addSensor(OleSensor oles) {
        mySensorsVisual.put(oles.getName(), oles);
    }
}
