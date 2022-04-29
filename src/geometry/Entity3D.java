/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import glossary.direction;
import static crypto.Keygen.getAlphaNumKey;
import static crypto.Keygen.getAlphaNumKey;
import static crypto.Keygen.getHexaKey;
import java.awt.Color;
import java.util.Comparator;


/**
 *
 * @author lcv
 */
public  class Entity3D implements Comparator<Entity3D>, Comparable<Entity3D>{

    protected String _name, _key;
    protected SimpleVector3D _vector;
    protected Point3D _size,_center;
    protected Color _color;


    public Entity3D(String name) {
        _name = name;
        _key = getAlphaNumKey(16);
//        _sorientation = direction.EAST.ordinal();
       setPosition(new Point3D(0,0,0));
       setSize(new Point3D(1,1,0));
    }

     public Entity3D(Point3D position, Color color) {
         setPosition(position);
         _color = color;
        _name = getHexaKey();
    }
    
     public Entity3D(SimpleVector3D vposition, Color color) {
        _vector = vposition;
        _color = color;
        _name = getHexaKey();
    }
    
    public Entity3D() {
        _vector = new SimpleVector3D(new Point3D(0,0,0), SimpleVector3D.N);
        _color = Color.WHITE;
        _name = getHexaKey();
    } 

    public Point3D getCenter() {
        return _center;
    }

    public Entity3D  setCenter(Point3D _center) {
        this._center = _center;
        return this;
    }
    
    public String getName() {
        return _name;
    }

    public String getId() {
        return _key;
    }

    public final Entity3D setPosition(Point3D p) {
        _vector = new SimpleVector3D(p,SimpleVector3D.N);
        _center = _vector.getSource().clone();
        return this;
    }

    public final Entity3D setPosition(SimpleVector3D p) {
        _vector = p.clone();
        _center = _vector.getSource().clone();
        return this;
    }

    public Color getColor() {
        return _color;
    }

    public Entity3D setColor(Color _color) {
        this._color = _color;
        return this;
    }
    
   public Point3D getPosition() {
        return _vector.getSource();
    }

   public SimpleVector3D getVector() {
        return _vector;
    }

    public final Entity3D setSize(Point3D p) {
        _size = p.clone();
        return this;
    }

    public Point3D getSize() {
        return _size;
    }

    public Entity3D setName(String _name) {
        this._name = _name;
        return this;
    }

    public int getDimension() {
        return _vector.getSource().getDimension();
    }


    private Entity3D setVectorTo(Point3D target) {
//        _orientation = new Vector3D(getPosition(), target).canonical();
        return this;
    }

    public Entity3D setOrientation(int orientation) {
        this._vector.setsOrient(orientation);
        return this;
    }
    

    public int getOrientation() {
        return _vector.getsOrient();
    }
    
//    public Vector3D getVector(){
//        return new Vector3D(getPosition(),getPosition().clone().plus(Compass.SHIFT[_sorientation]));
//    }
    
    public Entity3D move(Vector3D shift) {
        getVector().plus(shift.canonical().getTarget());
        return this;
    }

    public Entity3D moveForward(int units) {
        return move(this.getVector().canonical().clone().scalar(units));
    }
    
    public Entity3D moveUp(int units) {
        return move(Compass.SHIFT[direction.UP.ordinal()].clone().scalar(units));
    }
    
    public Entity3D moveDown(int units) {
        return move(Compass.SHIFT[direction.DOWN.ordinal()].clone().scalar(units));
    }
    
    private Entity3D RotateXY(double degrees) {
//        Vector3D orientation=getOrientation().canonical();
//        double radio=orientation.modulo();
//        orientation=new Vector3D()
//        setOrientation(getOrienta)
//        base.define(L.getPosition().getX()+radio*Math.cos(i*Math.PI/180),L.getPosition().getY()-radio*Math.sin(i*Math.PI/180));
        return this;
    }

    public static int rotateLeft(int sdirection) {
        return (sdirection + 1) % 8;
    }

    public static int rotateRight(int sdirection) {
        return (sdirection + 7) % 8;
    }

    public static int Opposite(int sdirection) {
        return (sdirection + 4) % 8;
    }

    public Entity3D rotateLeft() {
        return setOrientation(Entity3D.this.rotateLeft(getOrientation()));
    }

    public Entity3D rotateRight() {
        return setOrientation(Entity3D.this.rotateRight(getOrientation()));
    }

 
    public boolean contains(Point3D p) {
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

    public int compareTo(Entity3D other) {
        return (int) (1000 * getCenter().fastDistanceXYTo(other.getCenter()));
    }
    
    public int compare(Entity3D one, Entity3D other) {
        return one.compareTo(other);
    }
    }
