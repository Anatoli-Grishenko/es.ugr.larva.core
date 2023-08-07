///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package ai;
//
//import ai.Search.PathType;
//import JsonObject.JsonArray;
//import geometry.Point3D;
//import glossary.Sensors;
//import jade.core.behaviours.Behaviour;
//import jade.lang.acl.ACLMessage;
//import map2D.Map2DColor;
//import tools.emojis;
//import world.Thing;
//import world.liveBot;
//import static zip.ZipTools.zipToString;
//
///**
// *
// * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
// */
//public class BehaviouralSearch extends Behaviour {
//
//    AStarBehavioural search;
//    ACLMessage r;
//    Map2DColor map;
//    Search.PathType path;
//    int minlevel, maxlevel, maxslope;
//    Choice from, to;
//    int levels;
//    liveBot Agent;
//    Plan result;
//
//    public BehaviouralSearch(liveBot a, Map2DColor m, PathType p, Choice cStart, Choice cTarget, Plan result) {
//        super();
//        Agent = a;
//        map = m;
//        path = p;
//        minlevel = Agent.Raw().getMinlevel();
//        maxlevel = Agent.Raw().getMaxlevel();
//        maxslope = Agent.Raw().getMaxslope();
//        from = cStart;
//        to = cTarget;
//        search = new AStarBehavioural(map, path, minlevel, maxlevel,
//                maxslope, from, to);
//        levels = 0;
//    }
//
//    @Override
//    public void action() {
//        result = search.SearchLowest();
//    }
//
//    @Override
//    public boolean done() {
//        if (result != null) {
//            if (result.size() == 0) {
//                RespondError(incoming, ACLMessage.FAILURE, "Failure : a course could not be found: ");
//                NotifyUser(currentSession.getUserID(), emojis.WARNING + lastLoggedMessage);
//                System.out.println("A* failed from " + from.getName() + " to " + to.getName());
//            } else {
//                Point3D lastPoint = null, nextPoint;
//                JsonArray jsares = new JsonArray();
//                Point3D pdest;
//                Thing tdest;
//                pdest = to.getPosition();
//
//                int i = 0, istep = agent.Raw().getRange() * 2;
//                lastPoint = agent.Raw().getGPS();
//                nextPoint = lastPoint;
//                for (Choice c : result) {
//                    if (c.getPosition().planeDistanceTo(lastPoint) < istep) {
//                        nextPoint = c.getPosition();
//                    } else {
//                        jsares.add(nextPoint.toJson());
//                        lastPoint = nextPoint;
//                    }
//                    i++;
//                }
//                if (lastPoint == null || !lastPoint.isEqualTo(pdest)) {
//                    jsares.add(pdest.toJson());
//                }
//                agent.Raw().encodeSensor(Sensors.COURSE, jsares);
//                agent.Raw().activateCourse();
//                resetMovements(agent);
////                                agent.Raw().setCityDestination(city);
//                System.out.println("A* sucessfull from " + from.getName() + " to " + to.getName());
//                RespondSuccess(incoming, ACLMessage.INFORM, zipToString(getPerceptions(agent).toString()));
//                minimalupdateXUIAgents(incoming.getSender().getLocalName());
//            }
//        }
//        return result != null;
//    }
//}
//
//}
