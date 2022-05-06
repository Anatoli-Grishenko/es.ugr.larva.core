/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import ai.Choice;
import java.util.ArrayList;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Plan extends ArrayList<Choice>{
    
    public Plan() {
        super();
    }
    public double Sum() {
        double res=0;
        for (Choice c:this) {
            res += c.getUtility();
        }
        return res;
    }
    
    public double Max() {
        double res=Choice.MIN_UTILITY;
        for (Choice c:this) {
            if (c.getUtility()>res)
                res=c.getUtility();
        }
        return res;
    }
    public double Min() {
        double res=Choice.MAX_UTILITY;
        for (Choice c:this) {
            if (c.getUtility()<res)
                res=c.getUtility();
        }
        return res;
    }
}
