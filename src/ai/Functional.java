/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import Environment.Environment;
import java.util.Collections;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Functional extends Decisor {

    @Override
    public double getUtility(Environment e, Choice c) {
        switch (c.getName().toUpperCase()) {
            case "LEFT":
                if (e.getThermalLeft() < e.getThermalFront()) {
                    return 1000;
                } else {
                    return Choice.MAX_UTILITY;
                }
            case "RIGHT":
                if (e.getThermalRight() < e.getThermalFront()) {
                    return 1000;
                } else {
                    return Choice.MAX_UTILITY;
                }
            case "MOVE":
                return e.getThermalFront();
            case "DOWN":                
            case "UP":
                return Choice.MAX_UTILITY;
            case "CAPTURE":
                if (getElegibility(e, c)) {
                    return Choice.MIN_UTILITY;
                } else {
                    return Choice.MAX_UTILITY;
                }
            case "HALT":
                if (getElegibility(e, c)) {
                    return Choice.MIN_UTILITY;
                } else {
                    return Choice.MAX_UTILITY;
                }
            case "RECHARGE":
                if (getElegibility(e, c)) {
                    return Choice.MIN_UTILITY;
                } else {
                    return Choice.MAX_UTILITY;
                }
        }
        return Choice.MAX_UTILITY;
    }

    @Override
    public boolean getElegibility(Environment e, Choice c) {
        switch (c.getName().toUpperCase()) {
            case "LEFT":
            case "RIGHT":
                return true;
            case "MOVE":
                return e.getLidarFront()>=0;
            case "DOWN":
                return e.getGround() > 0;
            case "UP":
                return e.getAltitude() < e.getMaxlevel();
            case "CAPTURE":
                return e.getOntarget();
            case "HALT":
                return e.getCargo() != null && e.getCargo().length > 0;
            case "RECHARGE":
                return e.getEnergy() < 400 && e.getGround() == 0;
        }
        return false;
    }

    @Override
    public DecisionSet MakeBestDecision(Environment e) {
        for (Choice c : DecisionSet) {
            c.setUtility(this.getUtility(e, c));
            c.setValid(this.getElegibility(e, c));
        }
        Collections.sort(DecisionSet);
        Collections.reverse(DecisionSet);
        return DecisionSet;
    }

}
