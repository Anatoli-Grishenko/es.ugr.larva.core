/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import glossary.direction;
import static crypto.Keygen.getAlphaNumKey;
import static crypto.Keygen.getAlphaNumKey;


/**
 *
 * @author lcv
 */
public  class Entity {

    protected String _name, _key;
    protected Point _position, _size;
    protected int _sorientation; 


    public Entity(String name) {
        _name = name;
        _key = getAlphaNumKey(16);
        _sorientation = direction.EAST.ordinal();
       setPosition(new Point(0,0,0));
       setSize(new Point(1,1,0));
    }

            
    public String getName() {
        return _name;
    }

    public String getId() {
        return _key;
    }

    public final Entity setPosition(Point p) {
        _position = p.clone();
        return this;
    }
   public Point getPosition() {
        return _position;
    }

    public final Entity setSize(Point p) {
        _size = p.clone();
        return this;
    }

    public Point getSize() {
        return _size;
    }

    public int getDimension() {
        return _position.getDimension();
    }


    private Entity setVectorTo(Point target) {
//        _orientation = new Vector(getPosition(), target).canonical();
        return this;
    }

    public Entity setOrientation(int orientation) {
        _sorientation = orientation % 8;
        return this;
    }
    

    public int getOrientation() {
        return _sorientation;
    }
    
    public Vector getVector(){
        return new Vector(getPosition(),getPosition().clone().plus(Compass.SHIFT[_sorientation]));
    }
    
    public Entity move(Vector shift) {
        getPosition().plus(shift.canonical().getTarget());
        return this;
    }

    public Entity moveForward(int units) {
        return move(Compass.SHIFT[getOrientation()].clone().scalar(units));
    }
    
    public Entity moveUp(int units) {
        return move(Compass.SHIFT[direction.UP.ordinal()].clone().scalar(units));
    }
    
    public Entity moveDown(int units) {
        return move(Compass.SHIFT[direction.DOWN.ordinal()].clone().scalar(units));
    }
    
    private Entity RotateXY(double degrees) {
//        Vector orientation=getOrientation().canonical();
//        double radio=orientation.modulo();
//        orientation=new Vector()
//        setOrientation(getOrienta)
//        base.define(L.getPosition().getX()+radio*Math.cos(i*Math.PI/180),L.getPosition().getY()-radio*Math.sin(i*Math.PI/180));
        return this;
    }

    public static int rotateLeft(int sdirection) {
        return (sdirection + 7) % 8;
    }

    public static int rotateRight(int sdirection) {
        return (sdirection + 1) % 8;
    }

    public static int Opposite(int sdirection) {
        return (sdirection + 4) % 8;
    }

    public Entity rotateLeft() {
        return setOrientation(Entity.this.rotateLeft(getOrientation()));
    }

    public Entity rotateRight() {
        return setOrientation(Entity.this.rotateRight(getOrientation()));
    }

 
    public boolean contains(Point p) {
        if (p.getDimension() != getPosition().getDimension()) {
            return false;
        }
        boolean res = true;
        switch (p.getDimension()) {
            case 3:
                res &= getPosition().getZ() <= p.getZ() && p.getZ() <= getPosition().getZ() + _size.getZ();
            case 2:
                res &= getPosition().getY() <= p.getY() && p.getY() <= getPosition().getY() + _size.getY();
            case 1:
                res &= getPosition().getX() <= p.getX() && p.getX() <= getPosition().getX() + _size.getX();
                break;
        }
        return res;
    }
}
