/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import geometry.Point3D;
import java.util.Collections;
import java.util.HashMap;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public abstract class Search {

    DecisionSet closedNodes, openNodes;
    TimeHandler startTime;
    int maxSeconds, maxDepth;
    Choice source, target;

    public Search() {
        openNodes = new DecisionSet();
        closedNodes = new DecisionSet();
        this.setMaxSeconds(-1);
        this.setMaxDepth(-1);
    }

    public abstract boolean isValidChoice(Choice c);

    public abstract boolean isGoalChoice(Choice c);

    public abstract DecisionSet Next(Choice c);

    public Plan SearchLowest(Choice from, Choice to) {
        Plan result = new Plan(), subplan, bestSubplan = null;
        source=from;
        target=to;
        if (isGoalChoice(from)) {
            return new Plan();
        }
        if (!isValidChoice(from)) {
            return null;
        }
        result.add(from);
        DecisionSet children = Next(from);
        for (Choice os : children) {
            subplan = SearchLowest(os, target);
            if (subplan != null) {
                result.addAll(bestSubplan);
                return result;
            }
        }
        return null;
    }

    public void addClosedNode(Choice c) {
        if (!isClosedNode(c)) {          
            closedNodes.addChoice(c);
        }
    }

    public void removeClosedNode(Choice c) {
        if (isClosedNode(c)) {
            closedNodes.removeChoice(c);
        }
    }

    public void removeOpenNode(Choice c) {
        if (isOpenNode(c)) {
            openNodes.removeChoice(c);
        }
    }

    public void addOpenNode(Choice c) {
        if (!isOpenNode(c)) {
            openNodes.addChoice(c);
        }
    }

    public boolean isOpenNode(Choice c) {
        return openNodes.containsChoice(c);
    }

    public boolean isClosedNode(Choice c) {
        return closedNodes.containsChoice(c);
    }

    public TimeHandler getStartTime() {
        return startTime;
    }

    public void setStartTime(TimeHandler startTime) {
        this.startTime = startTime;
    }

    public int getMaxSeconds() {
        return maxSeconds;
    }

    public void setMaxSeconds(int maxSeconds) {
        this.maxSeconds = maxSeconds;
    }

    public int timeLeft(){
        return this.getMaxSeconds()-(int)this.getStartTime().elapsedTimeSecs(new TimeHandler());
    }
    public boolean isOver() {
        if (getStartTime() != null && this.getMaxSeconds() > 0
                &&  timeLeft()<=0) {
            return true;
        }
        return false;
    }
    public void initSearch() {
        this.setStartTime(new TimeHandler());
    }
    
    public int closedNodesSize() {
        return closedNodes.size();
    }
    public int openNodesSize() {
        return openNodes.size();
    }
    
    public Choice bestOpenNode() {
        openNodes.sortAscending();
        return openNodes.BestChoice();
    }
    
    public double Heuristic(Choice c) {
        return c.getPosition().realDistanceTo(target.getPosition());
    }
    
    public double Cost(Choice from, Choice to) {
        return from.getPosition().realDistanceTo(to.getPosition());
    }

    public DecisionSet getClosedNodes() {
        return closedNodes;
    }

    public void setClosedNodes(DecisionSet closedNodes) {
        this.closedNodes = closedNodes;
    }

    public DecisionSet getOpenNodes() {
        return openNodes;
    }

    public void setOpenNodes(DecisionSet openNodes) {
        this.openNodes = openNodes;
    }

    public Choice getSource() {
        return source;
    }

    public void setSource(Choice source) {
        this.source = source;
    }

    public Choice getTarget() {
        return target;
    }

    public void setTarget(Choice target) {
        this.target = target;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
    
}
