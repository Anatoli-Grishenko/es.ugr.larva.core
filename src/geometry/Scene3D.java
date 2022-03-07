/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geometry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Scene3D {
    protected HashMap<String, Entity3D> scene;

    public Scene3D() {
        scene = new HashMap();
    }

    public void addEntity3D(Entity3D o) {
        scene.put(o.getName(), o);
    }

    public Entity3D getObject(String name) {
        return scene.get(name);
    }

    public ArrayList<Entity3D> getAllObjects(Point3D reference) {
        ArrayList<Entity3D> res = new ArrayList();
        for (String s : scene.keySet()) {
            res.add(getObject(s));
        }
//        if (reference != null) {
//            res.sort(new Comparator<Entity3D>() {
//                @Override
//                public int compare(Entity3D o1, Entity3D o2) {
//                    if (o1.getCenter().realDistanceTo(reference) > o2.getCenter().realDistanceTo(reference)) {
//                        return -1;
//                    } else {
//                        return 1;
//                    }
//                }
//            });
//        }
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
