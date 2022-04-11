/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import geometry.Point3D;
import java.util.ArrayList;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class TracePositions {

    ArrayList<Point3D> myTrace;

    public TracePositions() {
        myTrace = new ArrayList();
    }

    public int size() {
        return myTrace.size();
    }

    public TracePositions clear() {
        myTrace.clear();
        return this;
    }

    public TracePositions addPosition(Point3D p) {
        myTrace.add(p);
        return this;
    }

    public TracePositions addUniquePosition(Point3D p) {
        this.removePosition(this.findPosition(p));
        myTrace.add(p);
        return this;
    }

    public boolean containsPosition(Point3D p) {
        return findPosition(p) >= 0;
    }

    public int findPosition(Point3D p) {
        int i = 0, pos = -1;
        for (Point3D mp : myTrace) {
            if (mp.isEqualTo(p)) {
                pos = i;
            }
            i++;
        }
        return pos;
    }

    public TracePositions removePosition(int i) {
        if (0 <= i && i < size()) {
            myTrace.remove(i);
        }
        return this;
    }

    public Point3D getPosition(int i) {
        if (0 <= i && i < size()) {
            return myTrace.get(i);
        } else {
            return null;
        }
    }

    public Point3D getLastPosition(int i) {
        if (0 <= i && i < size() - 1) {
            return myTrace.get(size() - 1 - i);
        } else {
            return myTrace.get(size()-1);
        }

    }
}
