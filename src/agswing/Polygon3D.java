/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agswing;

import geometry.Point;
import java.awt.Polygon;
import java.util.ArrayList;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Polygon3D extends Object3D {

    protected ArrayList<Point> vertex;
    boolean filled;

    public Polygon3D() {
        vertex = new ArrayList();
    }

    public Polygon3D(Point p) {
        vertex = new ArrayList();
        setPosition(p);
    }

    public Polygon3D addVertex(Point p) {
        vertex.add(p);
        Point pc = this.getPosition().clone();
        int n = 0;
        for (int i = 0; i < size(); i++) {
            if (!this.getVertex(i).isEqualTo(this.getPosition())) {
                n++;
                pc.plus(this.getVertex(i));
            }
        }
        pc.scalar(1.0/n);
        setCenter(pc);

        return this;
    }

    public boolean isFilled() {
        return filled;
    }

    public Polygon3D setFilled(boolean filled) {
        this.filled = filled;
        return this;
    }

    public int size() {
        return vertex.size();
    }

    public Point getVertex(int n) {
        if (n <= vertex.size()) {
            return vertex.get(n);
        }
        return null;
    }
}
