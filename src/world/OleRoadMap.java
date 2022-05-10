/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import ai.Plan;
import ai.Search.PathType;
import data.Ole;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleRoadMap extends Ole{
    
    public OleRoadMap(String name, ArrayList <String> nodes) {
        super();
        this.setType("OleRoadMap");
        this.setField("name", name);
        Collections.sort(nodes);
        this.setField("nodes", new ArrayList());
        for (String s : nodes) {
            this.addToField("nodes", s);
        }
        
    }
    
    public void addRoad(String from, String to, Plan path){        
        ArrayList <String> mypath = path.toStringArrayList();
        this.setField (from+"-"+to, new ArrayList(mypath));
        this.setField (to+"-"+from, new ArrayList(mypath));
    }
    
            
}
