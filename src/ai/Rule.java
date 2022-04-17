/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Rule {

    String label;
    BooleanSupplier Condition;
    Supplier<String> Body;
    boolean debug = false;
    String outcome;

    public Rule(String label, BooleanSupplier Condition, Supplier<String> Body) {
        this.label = label;
        this.Condition = Condition;
        this.Body = Body;
    }

    public Rule(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Rule setLabel(String label) {
        this.label = label;
        return this;
    }

    public Rule setCondition(BooleanSupplier Condition) {
        this.Condition = Condition;
        return this;
    }

    public Supplier<String> getBody() {
        return Body;
    }

    public Rule setBody(Supplier<String> Body) {
        this.Body = Body;
        return this;
    }

    public boolean isFirable() {
        return Condition.getAsBoolean();
    }

    public String fire() {
        String outcome="";
        if (this.isFirable()) {
            if (debug) {
                outcome= "||R>" + this.getLabel() + "||A>" + Body.get();
            } else {
                outcome= Body.get();
            }
        }
        setOutcome(outcome);
        return outcome;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

}
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ai;
//
///**
// *
// * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
// */
//public class Rule {
//    String label;
//    Runnable Condition, Body;
//    public boolean condition;
//
//    public Rule(String label, Runnable Condition, Runnable Body) {
//        this.label = label;
//        this.Condition = Condition;
//        this.Body = Body;
//    }
//
//    public Rule(String label) {
//        this.label = label;
//    }
//
//    public String getLabel() {
//        return label;
//    }
//
//    public void setLabel(String label) {
//        this.label = label;
//    }
//
//    public Runnable getCondition() {
//        return Condition;
//    }
//
//    public void setCondition(Runnable Condition) {
//        this.Condition = Condition;
//    }
//
//    public Runnable getBody() {
//        return Body;
//    }
//
//    public void setBody(Runnable Body) {
//        this.Body = Body;
//    }
//    
//    public boolean isFirable() {
//        Condition.run();
//        return this.condition;
//    }
//    public String fire() {
//        if (this.isFirable()) {
//            Body.run();
//            return "Rule "+this.getLabel()+" fired";
//        }
//        return "Rule "+this.getLabel()+" cannot fire";
//    }   
//    
//}
