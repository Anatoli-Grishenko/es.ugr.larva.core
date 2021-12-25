/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agswing;

import geometry.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Scene3D {

    protected HashMap<String, Object3D> scene;

    public Scene3D() {
        scene = new HashMap();
    }

    public void addObject3D(Object3D o) {
        scene.put(o.getName(), o);
    }

    public Object3D getObject(String name) {
        return scene.get(name);
    }

    public ArrayList<Object3D> getAllObjects(Point reference) {
        ArrayList<Object3D> res = new ArrayList();
        for (String s : scene.keySet()) {
            res.add(getObject(s));
        }
        if (reference != null) {
            res.sort(new Comparator<Object3D>() {
                @Override
                public int compare(Object3D o1, Object3D o2) {
                    if (o1.getCenter().realDistanceTo(reference) > o2.getCenter().realDistanceTo(reference)) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });
        }
        return res;
    }

    public int size() {
        return scene.size();
    }
    
    public Scene3D clearAll() {
        scene = new HashMap();
        return this;
    }
}
