/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package games;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import map2D.Map2DColor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class GameScene {

    public static enum Align {
        TOP, BOTTOM, CENTER, LEFT, RIGHT
    };

    protected HashMap<String, GameObject> things;
    protected HashMap<Integer, ArrayList<GameObject>> planes;
    protected int speed = 25, zoom = 1, step = 5, cell = 10;
    protected Graphics2D g;

    public GameScene() {
        clear();
    }

    public void addGameObject(GameObject go) {
        ////System.out.println("Adding Object: "+go.getId());

        things.put(go.getId(), go);
        if (planes.get(go.getPlane()) == null) {
            planes.put(go.getPlane(), new ArrayList());
        }
        planes.get(go.getPlane()).add(go);
    }

    public void remmoveGameObject(GameObject go) {
        ////System.out.println("Adding Object: "+go.getId());

        things.remove(go.getId());
        if (planes.get(go.getPlane()) == null) {
            planes.put(go.getPlane(), new ArrayList());
        }
        planes.get(go.getPlane()).add(go);
    }

    public void clear() {
        things = new HashMap();
        planes = new HashMap();

    }

    public void showScene() {
//        for (int i = 0; i < 100; i++) {
        ////System.out.println("Scene: ");
        ArrayList<Integer> sorted = new ArrayList(planes.keySet());
        Collections.sort(sorted);
        for (int i : sorted) {
            if (planes.get(i) != null) {
                ArrayList<GameObject> plane = planes.get(i);
                for (GameObject go : plane) {
                    ////System.out.println("Scene: "+go.getId());
                    go.showGameObject(this);
                }
            }
        }
        GameObject lb = this.getGameObject("ladybug");
        if (lb != null) {
            int cx = (int) lb.getX(), cy = (int) lb.getY();
            this.getG().setColor(Color.GREEN);
            this.getG().drawString("X:" + cx + " Y:" + cy, 0, 0);
            System.out.println("Ladybug en X:" + cx + " Y:" + cy);
        }
    }

    public GameObject getGameObject(String id) {
        return things.get(id);
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Graphics2D getG() {
        return g;
    }

    public void setG(Graphics2D g) {
        this.g = g;
    }

    public int getCell() {
        return cell * step;
    }

    public void setCell(int cell) {
        this.cell = cell;
    }

}
