/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agswing;

import geometry.Point;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Circle3D extends Object3D{
    String content;
    double radius;
    
    public Circle3D(Point p, double r) {
        super();
        setPosition(p);
        setRadius(r);
    }

    public String getContent() {
        return content;
    }

    public Circle3D setContent(String content) {
        this.content = content;
        return this;
    }

    public double getRadius() {
        return radius;
    }

    public Circle3D setRadius(double radius) {
        this.radius = radius;
        return this;
    }
    
    
    
}
