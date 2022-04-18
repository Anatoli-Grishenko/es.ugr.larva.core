/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import java.util.ArrayList;
import world.SensorDecoder;
import world.World;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Environment {
   protected SensorDecoder Perceptions;
   protected World World;
   ArrayList <Environment> Memory;
   
   public Environment() {
       Perceptions = new SensorDecoder();
       Memory=new ArrayList();
       
   }
    public SensorDecoder getPerceptions() {
        return Perceptions;
    }
   
    public Environment setExternalPerceptions(String perceptions) {
        Perceptions.feedPerception(perceptions);
        return this;
    }
    
    
}
