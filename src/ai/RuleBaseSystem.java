/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import java.util.ArrayList;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class RuleBaseSystem {

    protected ArrayList<Rule> myRules;
    protected int priority;
    protected String title;
    protected boolean active, debug = false;

    public RuleBaseSystem() {
        myRules = new ArrayList();
    }

    public void clear() {
        myRules.clear();
    }

    public int size() {
        return myRules.size();
    }

    public RuleBaseSystem addRule(Rule r) {
        myRules.add(r);
        return this;
    }

    public boolean isFirable() {
        return listFirablesRules().size() > 0;
    }

    public ArrayList<String> listAllRules() {
        ArrayList<String> res = new ArrayList();
        for (Rule r : myRules) {
            res.add(r.getLabel());
        }
        return res;
    }

    public ArrayList<String> listFirablesRules() {
        ArrayList<String> res = new ArrayList();
        for (Rule r : myRules) {
            if (r.isFirable()) {
                res.add(r.getLabel());
            }
        }
        return res;
    }

    public ArrayList<String> fireFirst() {
        ArrayList<String> out = new ArrayList();
        if (isActive()) {
            for (Rule r : myRules) {
                if (r.isFirable()) {
                    out.add(r.fire());
                    break;
                }
            }
        }
        return out;
    }

    public ArrayList<String> fireAll() {
        ArrayList<String> out = new ArrayList();
        if (isActive()) {
            for (Rule r : myRules) {
                if (r.isFirable()) {
                    out.add(r.fire());
                }
            }
        }
        return out;
    }

    public int getPriority() {
        return priority;
    }

    public RuleBaseSystem setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public RuleBaseSystem setTitle(String name) {
        this.title = name;
        return this;
    }

    public Rule getRule(int i) {
        if (0 <= i && i < size()) {
            return myRules.get(i);
        } else {
            return null;
        }
    }

    public Rule getRule(String label) {
        for (Rule r: myRules ) {
            if (r.getLabel().equals(label))
                return r;
        }
        return null;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDebug() {
        return debug;
    }

    public RuleBaseSystem setDebug(boolean debug) {
        this.debug = debug;
        for (Rule r : this.myRules) {
            r.setDebug(debug);
        }
        return this;
    }

}
