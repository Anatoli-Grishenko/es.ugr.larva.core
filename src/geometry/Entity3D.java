/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import static crypto.Keygen.getAlphaNumKey;
import static crypto.Keygen.getHexaKey;
import java.awt.Color;
import java.util.Comparator;


/**
 *
 * @author lcv
 */
public  class Entity3D implements Comparator<Entity3D>, Comparable<Entity3D>{

    protected String _name, _key, _type;
    protected SimpleVector3D _vector;
    protected Point3D _size,_center;
    protected Color _color;
    protected double _capacity=0, _maxCapacity=0;
    protected boolean _available;


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

    @Override
    public int compareTo(Entity3D other) {
        return (int) (1000 * getCenter().planeDistanceTo(other.getCenter()));
    }
    
    @Override
    public int compare(Entity3D one, Entity3D other) {
        return one.compareTo(other);
    }

    public double getCapacity() {
        return _capacity;
    }

    public void setCapacity(double _capacity) {
        this._capacity = _capacity;
    }

    public String getKey() {
        return _key;
    }

    public void setKey(String _key) {
        this._key = _key;
    }

    public String getType() {
        return _type;
    }

    public void setType(String _type) {
        this._type = _type;
    }

    public boolean isAvailable() {
        return _available;
    }

    public void setAvailable(boolean _available) {
        this._available = _available;
    }

    public double getMaxCapacity() {
        return _maxCapacity;
    }

    public void setMaxCapacity(double _maxCapacity) {
        this._maxCapacity = _maxCapacity;
    }

  
    }
