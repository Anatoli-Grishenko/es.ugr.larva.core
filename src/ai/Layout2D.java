/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import data.OleTable;
import geometry.Point3D;
import world.Thing;
import world.ThingSet;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Layout2D extends ThingSet {

    /**
     * Basic constructor
     */
    public Layout2D() {
        super();
    }

    public int getDistance(String from, String to) {
        Thing tfrom = getThing(from), tto = getThing(to);
        return (int) (tfrom.getPosition().realDistanceTo(tto.getPosition()));
    }

    public boolean isCompatible(Thing t, String from, String to) {
        Thing tfrom = getThing(from), tto = getThing(to);
        if (t.getType().equals("ITT")) {
            if (tfrom != null && tto != null) {
                return tfrom.getBelongsTo().equals(tto.getBelongsTo());
            } else {
                return false;
            }
        } else {
            if(tto != null)
            return tto.getPosition().getZ() <= 220;
            else
                return false;
        }
    }

}
