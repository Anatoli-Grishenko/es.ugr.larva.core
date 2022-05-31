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
public class AStarBehavioural extends  Search {

    String owner;
    boolean isInit, isSearch, isDone;
    Plan plan;
    DecisionSet children;
    Choice currentNode = null, newchild, oldchild;

    public AStarBehavioural(Map2DColor map, String owner) {
        super(map);
        this.owner = owner;
        isInit = isSearch = isDone = false;
    }

    public Plan SearchLowest(Choice from, Choice to) {

        if (!isInit) {
            this.initSearch();
            setSource(from);
            setTarget(to);
            from.setParent(null);
            from.setG(0);
            from.setH(Heuristic(from));
            from.setDepth(0);
            addOpenNode(from);
            System.out.println("Search starts at " + this.getStartTime().toString());
            String message = "Searching from " + from.toString() + " to " + to.toString()
                    + " maxslope " + maxslope + " levels [" + this.minlevel + ", " + this.maxlevel + "]";
            System.out.println(message);
            isInit = true;
            return null;
        } else if (!isSearch) {
            if (openNodesSize() > 0) {
                // Get the best node in open
                currentNode = getOpenNodes().popBestChoice();
//            System.out.println("Distance "+currentNode.getH());
//            this.view.setColor(currentNode.getPosition(), Color.RED);
//            this.app.getScollPane().repaint();
                if (this.isGoalChoice(currentNode) || timeOver()
                        || tooDeep(currentNode)) {
                    isSearch = true;
                }
//            System.out.println("Exploring " + currentNode + " at " + currentNode.getG() + " depth");
                children = Next(currentNode);
                // Heuristic
//            currentNode.setChildren(children);
                this.addClosedNode(currentNode);
//            System.out.println("Children: " + children);
                int i = 0;
                while (i < children.size()) {
                    newchild = children.getChoice(i++);
                    newchild.setG(currentNode.getG() + Cost(currentNode, newchild));
                    newchild.setH(Heuristic(newchild));

//                System.out.println("   Analyzing child " + newchild);
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
            return null;
        } else {
            System.out.println("Search ends at " + TimeHandler.Now() + " with " + this.closedNodesSize() + " nodes explored");
            if (isGoalChoice(currentNode)) {
                plan = this.getPlan(currentNode);
                System.out.println("Found a plan"); // with " + plan.size() + " steps");
                planSummary(plan);
                return plan;
            } else if (timeOver()) {
                plan = this.getPlan(currentNode);
                System.out.println("Time is over. No plan found until now");
                return new Plan();
            } else {
                plan = this.getPlan(currentNode);
                System.out.println("No more choices left. Search exhausted");
                return new Plan();
            }

        }
    }

    public void planSummary(Plan p) {
        int minlevel = 1000, maxlevel = -minlevel, minslope = 1000, maxslope = -minslope, level, slope, nsteps = p.size();
        for (int i = 0; i < p.size(); i++) {
            level = p.get(i).getPosition().getZInt();
            if (i > 0) {
                slope = level - p.get(i - 1).getPosition().getZInt();
                if (slope < minslope) {
                    minslope = slope;
                }
                if (slope > maxslope) {
                    maxslope = slope;
                }
            }
            if (level < minlevel) {
                minlevel = level;
            }
            if (level > maxlevel) {
                maxlevel = level;
            }
        }
        System.out.println("Plan from " + p.get(0).getPosition().toString() + " to " + p.get(p.size() - 1).getPosition().toString().toString()
                + "\nSteps: " + nsteps
                + "\nSlope: [" + minslope + "," + maxslope
                + "\nLevel: [" + minlevel + "," + maxlevel
        );
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
