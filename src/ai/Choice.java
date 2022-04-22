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
    public static final double MIN_UTILITY=Integer.MIN_VALUE, MAX_UTILITY=Integer.MAX_VALUE;
    String name;
    double utility;
    boolean valid;

    public Choice(String label) {
        this.name = label;
        utility = Choice.MIN_UTILITY;
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
        this.utility = Math.max(utility, this.getUtility());
        return this;
    }
    
    public Choice setUtility(double utility) {
        this.utility = utility;
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
            res += "," + String.format("%03d", (int)this.getUtility());
        }
        if (valid) {
            res = "[+" + res + "+]";
        } else {
            res = "{-" + res + "-}";
        }
        return res;
    }

}
