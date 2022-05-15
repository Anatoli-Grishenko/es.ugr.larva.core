/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glossary;

/**
 *
 * @author lcv
 */
//
//public enum Sensors {GPS,COMPASS, LIDAR, GROUND, VISUAL, ENERGY,
//            PAYLOAD, ONTARGET, ALIVE, DISTANCE, ANGULAR, TRACE, STATUS, CARGO};
public enum Sensors {
    // Self
    NAME, TEAM,
    // Sensors
    GPS, COMPASS, LIDAR, GROUND, VISUAL, ENERGY,
    PAYLOAD, ONTARGET, ALIVE, DISTANCE, ANGULAR, THERMAL, POTENTIAL,
    AWACS, WORLD, TERRAIN, TRACE, STATUS, CARGO, COINS,
    DISTANCEHQ, ANGULARHQ, THERMALHQ, VISUALHQ, LIDARHQ,
    DISTANCEDLX, ANGULARDLX, THERMALDLX, VISUALDLX, LIDARDLX, NUMSTEPS,
    // Memory
    RANGE, ENERGYBURNT, COURSE, TARGET, DESTINATION, PEOPLE, SESSIONID, TIME,
    // Parameters
    MAXLEVEL, MINLEVEL, MAXSLOPE, MAXCARGO, AUTONOMY,
    BURNRATEMOVE, BURNRATEREAD,
    // Behaviour
    COMMITMENT, CAPABILITIES
};
