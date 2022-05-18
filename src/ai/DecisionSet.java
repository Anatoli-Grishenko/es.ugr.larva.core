/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import ai.Choice;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DecisionSet extends ArrayList<Choice>{
    HashMap <String, Choice> index;
    
    public DecisionSet() {
        super();
        index = new HashMap();
    }
    
    public DecisionSet addChoice(Choice c) {
        this.add(c);
        index.put(c.getName(),c);
        return this;
    }
    public DecisionSet addChoiceMinor(Choice c) {
        index.put(c.getName(),c);
        int i=0;
        for (; i<size() && c.getUtility()>=getChoice(i).getUtility(); i++);
        this.add(i,c);
        return this;
    }
    public DecisionSet addChoiceMajor(Choice c) {
        index.put(c.getName(),c);
        int i=0;
        for (; i<size() && c.getUtility()<=getChoice(i).getUtility(); i++);
        this.add(i,c);
        return this;
    }
    public Choice getChoice(String label) {
        return index.get(label);
    }
    
    public boolean containsChoice(Choice c) {
        return index.containsKey(c.getName());
    }
    
    public int findChoice(Choice c) {
        for (int i=0; i<size(); i++) {
            if (get(i).getName().equals(c.getName())) {
                return i;
            }
        }
        return -1;
    }
    public DecisionSet sort(){        
        if (Choice.isIncreasing())
            sortAscending();
        else
            sortDescending();
        return this;
    }
    
    public DecisionSet sortAscending(){
        Collections.sort(this);     
        Collections.reverse(this);
        return this;
    }
    
    public DecisionSet sortDescending(){
        Collections.sort(this);        
        return this;
    }
    
    public DecisionSet extractEligibles(){
        DecisionSet res = new DecisionSet();
        for (Choice c : this){
            if (c.isValid())
                res.addChoice(c);
        }
        return res;
    }
    
    public Choice BestChoice(){
        return this.get(0);
    }
    
    public Choice SecondBestChoice(){
        return this.get(1);
    }
    
    public Choice getChoice(int i) {
        if (0<= i && i<size()) {
            return get(i);
        } else
            return null;
    }

    public Choice getChoice(Choice c) {
        return getChoice(c.getName());
//        return getChoice(findChoice(c));
    }
    public DecisionSet removeChoice(int i){
        Choice c = getChoice(i);
        if (c != null) {
            index.remove(c.getName());
            this.remove(i);
        }
        return this;
    }
    public DecisionSet removeChoice(Choice c){
        int i=findChoice(c);
        if (i >=0 ) {
            removeChoice(i);
        }
        return this;
    }
    
    public Choice popBestChoice() {
        Choice res = this.BestChoice();
        this.removeChoice(0);
        return res;
    }
    
    public DecisionSet reOrder(Choice c) {
        int pos = this.findChoice(c);
        this.removeChoice(pos);
        this.addChoiceMinor(c);
        return this;
    }
    
}
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ai;
//
//import ai.Choice;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//
///**
// *
// * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
// */
//public class DecisionSet extends ArrayList<Choice>{
//    HashMap <String, Choice> index;
//    
//    public DecisionSet() {
//        super();
//        index = new HashMap();
//    }
//    
//    public DecisionSet addChoice(Choice c) {
//        this.add(c);
//        index.put(c.getName(),c);
//        return this;
//    }
//    public Choice getChoice(String label) {
//        for (Choice mc : this) {
//            if (mc.getName().equals(label))
//                return mc;
//        }
//        return null;
//    }
//    
//    public boolean contains(Choice c) {
//        for (Choice mc : this) {
//            if (mc.getName().equals(c.getName()))
//                return true;
//        }
//        return false;
//    }
//    
//    public DecisionSet sortAscending(){
//        Collections.sort(this);     
//        return this;
//    }
//    
//    public DecisionSet sortDescending(){
//        Collections.sort(this);        
//        Collections.reverse(this);
//        return this;
//    }
//    
//    public DecisionSet extractEligibles(){
//        DecisionSet res = new DecisionSet();
//        for (Choice c : this){
//            if (c.isValid())
//                res.add(c);
//        }
//        return res;
//    }
//    
//    public Choice BestChoice(){
//        return this.get(0);
//    }
//    
//    public Choice SecondBestChoice(){
//        return this.get(1);
//    }
//    
//}
