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

    protected String name, key, type;
    protected SimpleVector3D position;
    protected Point3D size,center;
    protected Color color;
    protected double capacity=0, maxCapacity=0;
    protected double storage=0, maxStorage=0;
    protected boolean available;


    public Entity3D(String name) {
        this.name = name;
        key = getAlphaNumKey(16);
//        _sorientation = direction.EAST.ordinal();
       setPosition(new Point3D(0,0,0));
       setSize(new Point3D(1,1,0));
    }

     public Entity3D(Point3D position, Color color) {
         setPosition(position);
         this.color = color;
        name = getHexaKey();
    }
    
     public Entity3D(SimpleVector3D vposition, Color color) {
        position = vposition;
        this.color = color;
        name = getHexaKey();
    }
    
    public Entity3D() {
        position = new SimpleVector3D(new Point3D(0,0,0), SimpleVector3D.N);
        color = Color.WHITE;
        name = getHexaKey();
    } 

    public Point3D getCenter() {
        return center;
    }

    public Entity3D  setCenter(Point3D _center) {
        this.center = _center;
        return this;
    }
    
    public String getName() {
        return name;
    }

    public String getId() {
        return key;
    }

    public final Entity3D setPosition(Point3D p) {
        position = new SimpleVector3D(p,SimpleVector3D.N);
        center = position.getSource().clone();
        return this;
    }

    public final Entity3D setPosition(SimpleVector3D p) {
        position = p.clone();
        center = position.getSource().clone();
        return this;
    }

    public Color getColor() {
        return color;
    }

    public Entity3D setColor(Color _color) {
        this.color = _color;
        return this;
    }
    
   public Point3D getPosition() {
        return position.getSource();
    }

   public SimpleVector3D getVector() {
        return position;
    }

    public final Entity3D setSize(Point3D p) {
        size = p.clone();
        return this;
    }

    public Point3D getSize() {
        return size;
    }

    public Entity3D setName(String _name) {
        this.name = _name;
        return this;
    }

    public int getDimension() {
        return position.getSource().getDimension();
    }


    private Entity3D setVectorTo(Point3D target) {
//        _orientation = new Vector3D(getPosition(), target).canonical();
        return this;
    }

    public Entity3D setOrientation(int orientation) {
        this.position.setsOrient(orientation);
        return this;
    }
    

    public int getOrientation() {
        return position.getsOrient();
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
                res &= getPosition().getZ() <= p.getZ() && p.getZ() <= getPosition().getZ() + size.getZ();
            case 2:
                res &= getPosition().getY() <= p.getY() && p.getY() <= getPosition().getY() + size.getY();
            case 1:
                res &= getPosition().getX() <= p.getX() && p.getX() <= getPosition().getX() + size.getX();
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
        return capacity;
    }

    public void setCapacity(double _capacity) {
        this.capacity = _capacity;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String _key) {
        this.key = _key;
    }

    public String getType() {
        return type;
    }

    public void setType(String _type) {
        this.type = _type;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean _available) {
        this.available = _available;
    }

    public double getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(double _maxCapacity) {
        this.maxCapacity = _maxCapacity;
    }

    public double getStorage() {
        return storage;
    }

    public void setStorage(double _storage) {
        this.storage = _storage;
    }

    public double getMaxStorage() {
        return maxStorage;
    }

    public void setMaxStorage(double _maxStorage) {
        this.maxStorage = _maxStorage;
    }

  
    }
