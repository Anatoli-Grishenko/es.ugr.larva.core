/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import ai.Choice;
import ai.DecisionSet;
import ai.Plan;
import ai.Search;
import geometry.Compass;
import geometry.Point3D;
import geometry.SimpleVector3D;
import java.awt.Color;
import map2D.Map2DColor;
import swing.OleApplication;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Greedy extends Search {

    public Greedy(Map2DColor map) {
        super(map);
    }

    @Override
    public Plan SearchLowest(Choice from, Choice to) {
        Plan plan = new Plan(), subplan;
        DecisionSet children;
        Choice currentNode = null;

        this.initSearch();
        // Defines the search nodes
        setSource(from);
        setTarget(to);
        // Heuristics
        from.setUtility(Heuristic(from));
        addOpenNode(from);
        System.out.println("Search starts at " + this.getStartTime().toString());
        while (openNodesSize() > 0) {
            currentNode = getOpenNodes().popBestChoice();
            System.out.println("Exploring " + currentNode);
            plan.add(currentNode);
            if (this.isGoalChoice(currentNode) || timeOver()
                    || tooDeep(currentNode)) {
                break;
            }
            children = Next(currentNode);
//            for (Choice child : children) {
//                child.setUtility(child.getPosition().planeDistanceTo(to.getPosition())+
//                        Math.abs(currentNode.getPosition().getZ()-child.getPosition().getZ()));
//            }
//            children.sortAscending();
            this.view.setColor(currentNode.getPosition(), Color.RED);
            currentNode.setChildren(children);
            System.out.println("Next..." + children);
            System.out.println(this.timeLeft() + " secs left");
//            while (children.size() > 0 && isClosedNode(children.BestChoice())) {
//                children.removeChoice(0);
//            }
            this.setOpenNodes(children);
        }
        System.out.println("Seacrh ends at " + TimeHandler.Now() + " with " + this.closedNodesSize() + " nodes explored");
        if (isGoalChoice(currentNode)) {
            System.out.println("Found a plan with " + plan.size() + " steps");
            return plan;
        } else if (timeOver()) {
            System.out.println("Time is over. No plan found until now");
            return null;
        } else {
            System.out.println("No more choices left. Search exhausted");
            return null;
        }
    }
}
