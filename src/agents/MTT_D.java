/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class MTT_D extends DroidStarshipLevelD {
    
    @Override
    public void setup() {
        super.setup();
        this.DFAddMyServices(new String[]{"TYPE MTT"});        
    }
}