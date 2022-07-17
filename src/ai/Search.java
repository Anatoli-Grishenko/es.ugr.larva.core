/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import geometry.Compass;
import geometry.Point3D;
import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import map2D.Map2DColor;
import swing.OleApplication;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Search {

    public static enum PathType {
        FLATROAD, ROAD, FULLTERRAIN, AIRBORNE, MARINE
    }

    protected DecisionSet closedNodes, openNodes;
    protected TimeHandler startTime;
    protected int maxSeconds, maxDepth;
    protected Choice source, target;
    protected Map2DColor map;
    protected Map2DColor view;
    protected OleApplication app;
    protected int maxslope, minlevel, maxlevel, maxdistance, softGoal = 5;
    protected PathType type;

    public Search() {
        openNodes = new DecisionSet();
        closedNodes = new DecisionSet();
        this.setMaxSeconds(-1);
        this.setMaxDepth(-1);
    }

    public Search(Map2DColor map) {
        openNodes = new DecisionSet();
        closedNodes = new DecisionSet();
        this.setMaxSeconds(-1);
        this.setMaxDepth(-1);
        this.map = map;
    }

    public Plan SearchLowest(Choice from, Choice to) {
        Plan plan, subplan, bestSubplan = null;
        Choice currentNode = null;
        DecisionSet children;

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
        while (openNodesSize() > 0) {
            currentNode = getOpenNodes().popBestChoice();
            this.view.setColor(currentNode.getPosition(), Color.RED);
            if (this.isGoalChoice(currentNode) || timeOver()
                    || tooDeep(currentNode)) {
                break;
            }
            children = Next(currentNode);
            getOpenNodes().clear();
            getOpenNodes().addAll(children);
            getClosedNodes().add(currentNode);
        }
        System.out.println("Search ends at " + TimeHandler.Now() + " with " + this.closedNodesSize() + " nodes explored");
        if (app != null) {
            app.closeProgress(message);
        }
        if (isGoalChoice(currentNode)) {
            plan = this.getPlan(currentNode);
            System.out.println("Found a plan with " + plan.size() + " steps");
            return plan;
        } else if (timeOver()) {
            plan = this.getPlan(currentNode);
            System.out.println("Time is over. No plan found until now");
            return null;
        } else {
            plan = this.getPlan(currentNode);
            System.out.println("No more choices left. Search exhausted");
            return null;
        }
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
            openNodes.addChoiceMinor(c);
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

    public int timeLeft() {
        return this.getMaxSeconds() - (int) this.getStartTime().elapsedTimeSecsUntil(new TimeHandler());
    }

    public boolean timeOver() {
        if (getStartTime() != null && this.getMaxSeconds() > 0
                && timeLeft() <= 0) {
            return true;
        }
        return false;
    }

    public void initSearch() {
        this.setStartTime(new TimeHandler());
        this.getOpenNodes().clear();
        this.getClosedNodes().clear();
    }

    public int closedNodesSize() {
        return closedNodes.size();
    }

    public int openNodesSize() {
        return openNodes.size();
    }

    public Choice bestOpenNode() {
        return openNodes.BestChoice();
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

    public boolean tooDeep(Choice c) {
        return c.getDepth() > this.getMaxDepth();
    }

    public Plan getPlan(Choice goal) {
        Plan plan = new Plan();
        while (goal != null) {
            if (plan.size() == 0) {
                plan.add(goal);
            } else {
                plan.add(0, goal);
            }
//            System.out.println("Backplan to " + goal);
            goal = goal.getParent();
        }
        return plan;
    }

    public void setMap(Map2DColor map) {
        this.map = map;
    }

    public double Cost(Choice from, Choice to) {
        if (type == PathType.AIRBORNE) {
            return 1 + getDeviation(from.getParent(), from, to) * 1;
        } else if (type == PathType.MARINE) {
            return 1 + getDeviation(from.getParent(), from, to) * 1;
        } else if (type == PathType.FULLTERRAIN) {
//            return 1;
            return 1 + getDeviation(from.getParent(), from, to) * 2+ Math.abs(from.getPosition().getZ() - to.getPosition().getZ()) / 5;
        } else {
//            return 1 +Math.abs(from.getPosition().getZ() - to.getPosition().getZ()) / 5;
            return 1 + getDeviation(from.getParent(), from, to) * 2 + Math.abs(from.getPosition().getZ() - to.getPosition().getZ()) / 5;
        }
    }

    public double Heuristic(Choice c) {
        if (type == PathType.AIRBORNE) {
            return c.getPosition().planeDistanceTo(getTarget().getPosition());
        } else if (type == PathType.MARINE) {
            return c.getPosition().planeDistanceTo(getTarget().getPosition());
        } else {
            return c.getPosition().planeDistanceTo(getTarget().getPosition());
        }
    }

    public void setView(Map2DColor m) {
        view = m;
    }

    public void setApp(OleApplication a) {
        app = a;
    }

    protected double deltaZ(Point3D p1, Point3D p2) {
        return Math.abs(p1.getZ() - p2.getZ());
    }

    protected double deltaZ(Choice p1, Choice p2) {
        return deltaZ(p1.getPosition(), p2.getPosition());
    }

    public boolean isSloppy(Choice parent, Choice child) {
//        if (parent.getParent() != null && parent.getParent().getName().equals(this.getSource().getName())) {
//            return false;
//        }
//        if (type == PathType.MARINE && this.isCloseTo(getSource(), child, softGoal)) {
//            return false;
//        }
//        if (type == PathType.MARINE && this.isCloseTo(child, getTarget(), softGoal)) {
//            return false;
//        }
//        if (type == PathType.AIRBORNE) {
//            return false;
//        }
//        if (type == PathType.MARINE) {
//            return deltaZ(parent, child) > 5;
//        }
        return deltaZ(parent, child) > maxslope;
    }

    public boolean isforbidden(Choice child) {
        if (type == PathType.MARINE && getSource().inProximity(child)) {
            return false;
        }
        if (type == PathType.MARINE && child.inProximity(getTarget())) {
            return false;
        }
        if (type == PathType.MARINE) {
            return child.getPosition().getZ() >= 5 && !isGoalChoice(child);
        } else if (type == PathType.AIRBORNE) {
            return child.getPosition().getZ() > this.maxlevel;
        } else {
            return child.getPosition().getZ() < minlevel || 
                    child.getPosition().getZ() > maxlevel;
        }
    }

    public boolean isBadLevel(Choice child) {
        if (type == PathType.MARINE && getSource().inProximity(child)) {
            return false;
        }
        if (type == PathType.MARINE && child.inProximity(getTarget())) {
            return false;
        }
        return child.getPosition().getZ() > this.getMaxlevel() 
                || child.getPosition().getZ() < this.getMinlevel();
    }

    public boolean isValidChoice(Choice c) {
        boolean out = c.getPosition().getX() < 0 || c.getPosition().getY() < 0
                || c.getPosition().getX() >= map.getWidth() || c.getPosition().getY() >= map.getHeight(),
                cicle = (c.getParent() != null && c.getParent().getParent() != null && c.equals(c.getParent().getParent())),
                slopy = false, level;
        if (c.getParent() != null) {
            slopy = isSloppy(c, c.getParent());
        }
        level = isBadLevel(c);
        return !out && !cicle && !slopy && !level && !isforbidden(c) || c.getDepth() > this.getMaxDepth();
    }

    public boolean isGoalChoice(Choice c) {
        return c.equals(getTarget());
    }

    public DecisionSet Next(Choice c) {
        DecisionSet succesors = new DecisionSet();
        for (int dir = Compass.NORTH; dir <= Compass.NORTHEAST; dir++) {
            Point3D res = c.getPosition().clone().plus(Compass.VECTOR[dir].canonical().scalar(1));
            res.setZ(map.getStepLevel(res));
            Choice offspring = new Choice(res);
            if (!offspring.isEqualTo(c) && !offspring.isEqualTo(c.getParent())) {
                offspring.setParent(c);
                offspring.setG(0);
                offspring.setH(Heuristic(offspring));
                if (isValidChoice(offspring)) {
                    succesors.addChoiceMinor(offspring);
                }
            }
        }
        return succesors;
    }

    public int getMaxslope() {
        return maxslope;
    }

    public void setMaxslope(int maxslope) {
        this.maxslope = maxslope;
    }

    public int getMinlevel() {
        return minlevel;
    }

    public void setMinlevel(int minlevel) {
        this.minlevel = minlevel;
    }

    public int getMaxlevel() {
        return maxlevel;
    }

    public void setMaxlevel(int maxlevel) {
        this.maxlevel = maxlevel;
    }

    public PathType getType() {
        return type;
    }

    public void setType(PathType type) {
        this.type = type;
    }



    public int getDeviation(Choice c1, Choice c2, Choice c3) {
        if (c1 == null || c2 == null) {
            return 0;
        }
        int x1 = c1.getPosition().getXInt(), x2 = c2.getPosition().getXInt(), x3 = c3.getPosition().getXInt(),
                y1 = c1.getPosition().getYInt(), y2 = c2.getPosition().getYInt(), y3 = c3.getPosition().getYInt();
        if (x2 + (x2 - x1) == x3 && y2 + (y2 - y1) == y3) {
            return 0;
        } else {
            return (int) (1 * c1.getPosition().planeDistanceTo(c3.getPosition()));
        }
    }

    public double getMaxDeviation(Choice c) {
        double res = 0;
        int n = 0;
        Choice aux = c;
        while (aux != null) {
            if (c.getParent() != null && c.getParent().getParent() != null) {
                if (getDeviation(c.getParent().getParent(), c.getParent(), c) > res) {
                    res = getDeviation(c.getParent().getParent(), c.getParent(), c);
                }
            }
            aux = aux.getParent();
        }
        return res;
    }

    public double getSoftDeviation(Choice c) {
        double res = 0;
        int n = 0;
        Choice aux = c;
        while (aux != null) {
            if (this.getDeviation(c.getParent().getParent(), c.getParent(), c) > 0) {
                res += Math.pow(getDeviation(c.getParent().getParent(), c.getParent(), c), 2);
                n++;
            }
        }
        if (n > 0) {
            return res / n;
        } else {
            return 0;
        }
    }
}
