/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import ai.Choice;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DecisionSet extends ArrayList<Choice>{

    public DecisionSet() {
        super();
    }
    
    public DecisionSet addChoice(Choice c) {
        this.add(c);
        return this;
    }
    public Choice getChoice(String label) {
        for (Choice mc : this) {
            if (mc.getLabel().equals(label))
                return mc;
        }
        return null;
    }
    
    public boolean contains(Choice c) {
        for (Choice mc : this) {
            if (mc.getLabel().equals(c.getLabel()))
                return true;
        }
        return false;
    }
    
    public DecisionSet sortAscending(){
        Collections.sort(this);     
        return this;
    }
    
    public DecisionSet sortDescending(){
        Collections.sort(this);        
        Collections.reverse(this);
        return this;
    }
    
    public DecisionSet extractEligibles(){
        DecisionSet res = new DecisionSet();
        for (Choice c : this){
            if (c.isEligible())
                res.add(c);
        }
        return res;
    }
    
    
}
