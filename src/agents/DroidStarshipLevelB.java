/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import geometry.Point3D;
import jade.lang.acl.ACLMessage;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DroidStarshipLevelB extends DroidStarshipLevelA {

    
    @Override
    public void setup() {
        super.setup();
        inNegotiation = false;
        logger.onEcho();
        reaction=null;
    }
}
