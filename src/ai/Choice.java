/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Choice implements Comparable {

    String label;
    double utility;
    boolean eligible;

    public Choice(String label) {
        this.label = label;
        utility = Double.MIN_VALUE;
    }

    public String getLabel() {
        return label;
    }

    public Choice setLabel(String label) {
        this.label = label;
        return this;
    }

    public double getUtility() {
        return utility;
    }

    public Choice setUtility(double utility) {
        this.utility = utility;
        return this;
    }

    public boolean isEligible() {
        return eligible;
    }

    public Choice setEligible(boolean eligible) {
        this.eligible = eligible;
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

        res = this.getLabel();
        if (getUtility() != Double.MIN_VALUE) {
            res += "," + String.format("%03d", (int)this.getUtility());
        }
        if (eligible) {
            res = "[+" + res + "+]";
        } else {
            res = "{-" + res + "-}";
        }
        return res;
    }

}
