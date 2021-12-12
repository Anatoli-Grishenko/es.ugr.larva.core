/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agswing;

import static crypto.Keygen.getHexaKey;
import geometry.Point;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Comparator;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public abstract class Object3D implements Comparator<Object3D>, Comparable<Object3D>{
    protected String name;
    protected Point position, center;
    protected Color color;


    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position.clone();
        this.center = position.clone();
    }

    public Color getColor() {
        return color;
    }

    public Object3D setColor(Color color) {
        this.color = color;
        return this;
    }

    public Object3D(Point position, Color color) {
        this.position = position;
        this.color = color;
        this.name = getHexaKey();
    }
    
    public Object3D() {
        this.position = new Point(0,0,0);
        this.color = Color.WHITE;
        this.name = getHexaKey();
    }

    public String getName() {
        return name;
    }

    public Object3D setName(String name) {
        this.name = name;
        return this;
    }

    public Point getCenter() {
        return center;
    }

    public Object3D setCenter(Point center) {
        this.center = center;
        return this;
    }
    
    @Override
    public int compareTo(Object3D other) {
        return (int) (1000 * getCenter().fastDistanceXYTo(other.getCenter()));
    }
    
    @Override
    public int compare(Object3D one, Object3D other) {
        return one.compareTo(other);
    }
    
    
}
