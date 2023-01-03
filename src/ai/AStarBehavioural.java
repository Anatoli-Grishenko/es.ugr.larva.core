/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import geometry.Point3D;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import map2D.Map2DColor;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class AStarBehavioural extends Search {

    boolean isInit, isSearch, isDone;
    Plan plan;
    DecisionSet children;
    Choice currentNode = null, newchild, oldchild, start, end;
//    ACLMessage request;
    final int nIterations = 50;
    int iterations;

    public AStarBehavioural(Map2DColor map, PathType path, int minlevel, int maxlevel, int maxslope, Choice from, Choice to) {
        super(map);
//        request = r;
        isInit = isSearch = isDone = false;
        setView(null);
        setApp(null);
        setMaxSeconds(20);
        setMaxDepth(2000);
        setMinlevel(minlevel);
        setMaxlevel(maxlevel);
        setMaxslope(maxslope);
        setType(path);
        start = from;
        end = to;
    }

    public Plan SearchLowest() {

        if (!isInit) {
            this.initSearch();
            setSource(start);
            setTarget(end);
            start.setParent(null);
            start.setG(0);
            start.setH(Heuristic(start));
            start.setDepth(0);
            addOpenNode(start);
//            System.out.println("Search starts at " + this.getStartTime().toString());
//            String message = "Searching from " + start.toString() + " to " + end.toString()
//                    + " maxslope " + maxslope + " levels [" + this.minlevel + ", " + this.maxlevel + "]";
//            System.out.println(message);
            isInit = true;
            return null;
        } else if (!isSearch) {
            iterations=0;
            while (iterations++ < nIterations && !isSearch) {
                if (openNodesSize() > 0) {
                    // Get the best node in open
                    currentNode = getOpenNodes().popBestChoice();
                    if (this.isGoalChoice(currentNode) || timeOver()
                            || tooDeep(currentNode)) {
                        isSearch = true;
                    }
                    children = Next(currentNode);
                    this.addClosedNode(currentNode);
//            System.out.println("Children: " + children);
                    int i = 0;
                    while (i < children.size()) {
                        newchild = children.getChoice(i++);
                        newchild.setG(currentNode.getG() + Cost(currentNode, newchild));
                        newchild.setH(Heuristic(newchild));
                        if (getOpenNodes().containsChoice(newchild)) {
                            oldchild = getOpenNodes().getChoice(newchild);
                            if (newchild.getG() < oldchild.getG()) {
                                oldchild.setG(newchild.getG());
                                oldchild.setParent(currentNode);
                                currentNode.getChildren().add(oldchild);
                                getOpenNodes().reOrder(oldchild);
                            }
                        } else if (getClosedNodes().containsChoice(newchild)) {
                            oldchild = getClosedNodes().getChoice(newchild);
                            if (newchild.getG() < oldchild.getG()) {
                                oldchild.setG(newchild.getG());
                                oldchild.setParent(currentNode);
                                currentNode.getChildren().add(oldchild);
                                downPropagate(oldchild);
                            }
                        } else {
                            newchild.setParent(currentNode);
                            newchild.setG(currentNode.getG() + Cost(currentNode, newchild));
                            newchild.setH(Heuristic(newchild));
                            getOpenNodes().addChoiceMinor(newchild);
                            currentNode.getChildren().add(newchild);
                        }
                    }
                }
            }
            return null;
        } else {
            if (isGoalChoice(currentNode)) {
                plan = this.getPlan(currentNode);
                return plan;
            } else if (timeOver()) {
                plan = this.getPlan(currentNode);
                return new Plan();
            } else {
                plan = this.getPlan(currentNode);
                return new Plan();
            }
        }
    }

    public void downPropagate(Choice c) {
        for (Choice os : c.getChildren()) {
            if (c.getG() < os.getG()) {
                os.setParent(c);
                os.setG(c.getG() + Cost(c, os));
                if (getOpenNodes().containsChoice(os)) {
                    getOpenNodes().reOrder(os);
                } else if (getClosedNodes().containsChoice(os)) {
                    downPropagate(os);
                }
            }
        }
    }
}
