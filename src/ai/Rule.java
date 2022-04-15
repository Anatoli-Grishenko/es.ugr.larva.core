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
    public boolean condition, debug = false;

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

    public void setLabel(String label) {
        this.label = label;
    }

    public void setCondition(BooleanSupplier Condition) {
        this.Condition = Condition;
    }

    public Supplier<String> getBody() {
        return Body;
    }

    public void setBody(Supplier<String> Body) {
        this.Body = Body;
    }

    public boolean isFirable() {
        return Condition.getAsBoolean();
    }

    public String fire() {
        if (this.isFirable()) {
            if (debug) {
                return "||R>" + this.getLabel() + "||A>" + Body.get();
            } else {
                return Body.get();
            }
        }
        return "";
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
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
