/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import geometry.Point3D;
import java.util.ArrayList;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Choice implements Comparable {

    public static final double MIN_UTILITY = Integer.MIN_VALUE, MAX_UTILITY = Integer.MAX_VALUE;
    public static boolean increasing = true;
    String name;
    double utility, g, h;
    boolean valid;
    Point3D position;
    Choice parent;
    DecisionSet children;
    int depth;

    public Choice(String label) {
        this.name = label;
        if (isIncreasing()) {
            utility = Choice.MAX_UTILITY;
            h = Choice.MAX_UTILITY;
        } else {
            h = Choice.MIN_UTILITY;
            utility = Choice.MIN_UTILITY;
        }
        g = 0;
        depth = 0;
        children = new DecisionSet();
    }

    public Choice(Point3D p) {
        this.name = p.toString();
        position = p;
        if (isIncreasing()) {
            utility = Choice.MAX_UTILITY;
            h = Choice.MAX_UTILITY;
        } else {
            h = Choice.MIN_UTILITY;
            utility = Choice.MIN_UTILITY;
        }
        g = 0;
        depth = 0;
        children = new DecisionSet();
    }

    public boolean isEqualTo(Choice other) {
        if (other == null) {
            return false;
        } else {
            return this.getName().equals(other.getName());
        }
    }

    public String getName() {
        return name;
    }

    public Choice setName(String label) {
        this.name = label;
        return this;
    }

    public double getUtility() {
        return utility;
    }

    public Choice setMaxUtility(double utility) {
        this.utility = Math.max(utility, this.getUtility());
        return this;
    }

    public Choice setMinUtility(double utility) {
        this.utility = Math.min(utility, this.getUtility());
        return this;
    }

    public Choice setUtility(double utility) {
        if (getName().equals("IDLE")) {
            if (isIncreasing()) {
                utility = Choice.MAX_UTILITY;
            } else {
                utility = Choice.MIN_UTILITY;
            }
        } else {
            this.utility = utility;
        }
        return this;
    }

    public boolean isValid() {
        return valid;
    }

    public Choice setValid(boolean eligible) {
        this.valid = eligible;
        return this;
    }

    @Override
    public int compareTo(Object o) {
        Choice other = (Choice) o;
        if (other.getUtility() < this.getUtility()) {
            return -1;
        } else if (other.getUtility() > this.getUtility()) {
            return 1;
        } else {
            return 0;
        }

    }

    public String toString() {
        String res = "";

        res = this.getName();
        if (getUtility() != Choice.MIN_UTILITY) {
            res += "," + String.format("%5.2f", this.getUtility());
        }
        if (valid) {
            res = "[+" + res + "+]";
        } else {
            res = "{-" + res + "-}";
        }
        return res;
    }

    public Choice getParent() {
        return parent;
    }

    public void setParent(Choice parent) {
        this.parent = parent;
        if (parent != null) {
            setDepth(parent.getDepth() + 1);
        }
    }

    public DecisionSet getChildren() {
        return children;
    }

    public void setChildren(DecisionSet children) {
        this.children = children;
    }

    public Point3D getPosition() {
        return position;
    }

    public void setPosition(Point3D position) {
        this.position = position;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
        this.setUtility(this.getG() + this.getH());
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
        this.setUtility(this.getG() + this.getH());
    }

    public int calculateDepth() {
        int d = 0;
        Choice c = this;
        while (c.getParent() != null) {
            d++;
            c = c.getParent();
        }
        return d;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean equals(Choice c) {
        return this.getName().equals(c.getName());
    }

    public static boolean isIncreasing() {
        return increasing;
    }

    public static void setIncreasing() {
        increasing = true;
    }

    public static void setDecreasing() {
        increasing = false;
    }
}
