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
public class VAAT extends DroidShip {

    @Override
    public void setup() {
        super.setup();
        this.DFAddMyServices(new String[]{
            "TYPE VAAT",
            "QUERY-IF <name>",
            "QUERY-REF <name>",
            "QUERY-REF <city>",
            "REQUEST MOVETO <x> <y>",
            "REQUEST MOVEIN <city>",
            "REQUEST MOVEBY <agent>",
            "REQUEST BOARD <name>",
            "REQUEST DEBARK <name>"});
        this.allowParking = true;
    }
}
