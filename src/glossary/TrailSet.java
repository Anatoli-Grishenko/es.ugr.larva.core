/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glossary;

import geometry.SimpleVector3D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class TrailSet {

    int trailLength;
    ArrayList <String> names = new ArrayList();

    protected HashMap<String, ArrayList<SimpleVector3D>> Trails;

    public TrailSet() {
        Trails = new HashMap();
        setTrailLength(200);
    }

    public synchronized int getTrailLength() {
        return trailLength;
    }

    public synchronized void setTrailLength(int trailSize) {
        this.trailLength = trailSize;
    }

    public synchronized Set<String> getIndividuals() {
        return Trails.keySet();
    }

    public synchronized String getIndividual(int i) {
        return names.get(i);
    }

    protected synchronized ArrayList<SimpleVector3D> getTrailData(String name) {
        return Trails.get(name);
    }

    public synchronized SimpleVector3D getTrailData(String name, int pos) {
        if (0 <= pos && pos < lengthTrail(name)) {
            return Trails.get(name).get(pos);
        } else {
            return null;
        }
    }

    public synchronized void addTrailData(String name, SimpleVector3D s) {
        if (getTrailData(name) == null) {
            Trails.put(name, new ArrayList());
            names.add(name);
        }
        this.getTrailData(name).add(0,s);
        if (lengthTrail(name) > getTrailLength()) {
            Trails.get(name).remove(Trails.get(name).size() - 1);
        }

    }

    public synchronized int size() {
        return Trails.size();
    }

    public synchronized void clear() {
        Trails.clear();
    }

    public synchronized int lengthTrail(String name) {
        if (getTrailData(name) != null) {
            return getTrailData(name).size();
        } else {
            return -1;
        }
    }

}
