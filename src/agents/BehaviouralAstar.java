///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package agents;
//
//import ai.AStarBehavioural;
//import ai.Choice;
//import ai.Plan;
//import ai.Search;
//import jade.core.behaviours.Behaviour;
//import jade.lang.acl.ACLMessage;
//import map2D.Map2DColor;
//
///**
// *
// * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
// */
//public class BehaviouralAstar  extends Behaviour {
//    AStarBehavioural search;
//    Plan result;
//    public BehaviouralAstar(ACLMessage r, Map2DColor map, Search.PathType path, int minlevel, int maxlevel, int maxslope, Choice from, Choice to) {
//        search = new AStarBehavioural(r,map,path, minlevel, maxlevel, maxslope, from, to);
//    }
//    @Override
//    public void action() {
//        result = search.SearchLowest();
//    }
//
//    @Override
//    public boolean done() {        
//        if (result != null) {
//            
//        }
//        return result != null;
//    }
//    
//}
