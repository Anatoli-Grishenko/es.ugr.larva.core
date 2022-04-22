/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import Environment.Environment;
import ai.DecisionSet;
import ai.Choice;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public abstract class Decisor {

    protected DecisionSet DecisionSet;

    public Decisor() {
        DecisionSet = new DecisionSet();
    }

    public DecisionSet getDecisionSet() {
        return DecisionSet;
    }

    public Decisor setDecisionSet(DecisionSet DecisionSet) {
        this.DecisionSet = DecisionSet;
        return this;
    }

    public Decisor addChoice(Choice c) {
        DecisionSet.add(c);
        return this;
    }
    
    public abstract double getUtility(Environment e, Choice c);

    public abstract boolean getElegibility(Environment e, Choice c);

    public abstract DecisionSet MakeBestDecision(Environment e);
//    {
//        for (Choice ch : DecisionSet) {
//            ch.setUtility(this.getUtility(e, ch));
//            ch.setEligible(this.getElegibility(e, ch));
//        }
//        return DecisionSet.sortAscending().extractEligibles();
//    }


}
