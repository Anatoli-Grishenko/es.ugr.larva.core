/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package games;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class GameLabel extends GameObject {

    String label;
    Color color;

    public GameLabel(String id, String l) {
        super(id);
        setLabel(l);
        setColor(Color.WHITE);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    @Override
    public void showGameObject(GameScene gs) {
        //System.out.println("Object: " + getId());
        double x = getX() * gs.getCell(), y = getY() * gs.getCell();
        gs.getG().setColor(getColor());
        gs.getG().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        gs.getG().drawString(getLabel(), (int)x, (int)y);
    }

}
