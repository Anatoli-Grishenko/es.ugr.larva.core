/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import ai.Choice;
import ai.Plan;
import ai.Search.PathType;
import data.Ole;
import geometry.Point3D;
import java.util.ArrayList;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleRoad extends Ole {

    public OleRoad(String location1, String location2, PathType p) {
        super();
        this.setType("OleRoad");
        this.setField("roadType", p.toString());
        this.setField("roadLabel", location1 + "-" + location2);
        this.setField("location1", location1);
        this.setField("location2", location2);
        this.setField("path", new ArrayList());
    }

    public OleRoad(Ole newroad) {
        super(newroad);
        this.setType("OleRoad");
    }
    
    public OleRoad addPathStep(Point3D p) {
        this.addToField("path", p.toString());
        return this;
    }
}
