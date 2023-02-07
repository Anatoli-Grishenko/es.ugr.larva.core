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
public class VAAT_ND extends DroidStarshipLevelA {
    
    @Override
    public void setup() {
        super.setup();
        this.LARVADFAddMyServices(new String[]{"TYPE VAAT"});        
    }
}
