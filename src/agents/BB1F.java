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
public class BB1F extends DroidStarshipLevelA {
    
    @Override
    public void setup() {
        super.setup();
        this.DFAddMyServices(new String[]{"TYPE BB1F"});        
        this.logger.offEcho();
        onMission=false;
        this.openRemote();
//        this.closeRemote();
        onMission = false;
        allowCFP = true;
        allowREQUEST = true;
        allowParking=false;
    }
}

