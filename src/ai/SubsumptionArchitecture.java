/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class SubsumptionArchitecture {

    protected HashMap<String, RuleBaseSystem> Titles;
    protected HashMap<Integer, RuleBaseSystem> Priorities;
    protected HashMap<String, String> Output;
    protected HashMap<String, ArrayList<String>> Inhibit;
    protected HashMap<String, ArrayList<String>> Supress;

    public SubsumptionArchitecture() {
        Titles = new HashMap();
        Priorities = new HashMap();
        Output = new HashMap();
        Inhibit = new HashMap();
        Supress = new HashMap();
    }

    public int size() {
        return Titles.size();
    }

    public SubsumptionArchitecture addLayer(String Title, int priority) {
        RuleBaseSystem r = new RuleBaseSystem().setPriority(priority).setTitle(Title);
        Titles.put(Title, r);
        Priorities.put(priority, r);
        Inhibit.put(Title, new ArrayList());
        Supress.put(Title, new ArrayList());
        return this;
    }

    public RuleBaseSystem getLayer(String name) {
        return Titles.get(name);
    }

    public RuleBaseSystem getLayer(int priority) {
        return Priorities.get(priority);
    }

    public boolean isFirable() {
        for (String layer : Titles.keySet()) {
            if (Titles.get(layer).listFirables().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> listFirableRules() {
        ArrayList<String> result = new ArrayList(), aux;
        ArrayList<Integer> sorted = new ArrayList(Priorities.keySet());
        Collections.sort(sorted);
//        Collections.reverse(sorted);
        for (int priority : sorted) {
            aux = Priorities.get(priority).listFirables();
            if (aux.size() > 0) {
                for (String rulename : aux) {
                    result.add("" + priority + "::" + Priorities.get(priority).getTitle() + "::" + rulename);
                }
            }
        }
        return result;
    }

    public void inhibitModule(String module) {
        Titles.get(module).setActive(false);
//        for (String other : Inhibit.get(module)) {
//            inhibitModule(other);
//        }
    }

    public ArrayList<String> listInhibitedModules() {
        ArrayList<String> result = new ArrayList(), aux;
        ArrayList<Integer> sorted = new ArrayList(Priorities.keySet());
        Collections.sort(sorted);
        RuleBaseSystem rbs;
        Collections.reverse(sorted);
        for (int priority : sorted) {
            Priorities.get(priority).setActive(true);
        }
        for (int priority : sorted) {
            rbs = Priorities.get(priority);
            if (rbs.isFirable() && rbs.isActive()) {
                if (Inhibit.get(rbs.getTitle()) != null) {
                    for (String inhibit : Inhibit.get(rbs.getTitle())) {
                        inhibitModule(inhibit);
                    }
                }
            }
        }
        Collections.reverse(sorted);
        for (int priority : sorted) {
            rbs = Priorities.get(priority);
            if (!rbs.isActive()) {
                result.add(rbs.getTitle());
            }
        }
        return result;
    }

    public ArrayList<String> listFirableModules() {
        ArrayList<String> result = new ArrayList(), aux, inhibited;
        ArrayList<Integer> sorted = new ArrayList(Priorities.keySet());
        Collections.sort(sorted);
        RuleBaseSystem rbs;
//        Collections.reverse(sorted);
        inhibited = this.listInhibitedModules();
        for (int priority : sorted) {
            rbs = Priorities.get(priority);
            if (rbs.isFirable() && !inhibited.contains(rbs.getTitle())) {
                result.add(rbs.getTitle());
            }
        }
        return result;
    }

    public SubsumptionArchitecture inhibit(String fromLayer, String toLayer) {
        Inhibit.get(fromLayer).add(toLayer);
        return this;
    }

    public SubsumptionArchitecture addRule(String Layer, Rule r) {
        getLayer(Layer).addRule(r);
        return this;
    }

    public ArrayList<String> fire() {
        ArrayList<String> res = new ArrayList(), firables = new ArrayList(), inhibited = new ArrayList();
        ArrayList<Integer> sorted = new ArrayList(Priorities.keySet());
        Collections.sort(sorted);
        RuleBaseSystem rbs;
        Collections.reverse(sorted);
        this.Output.clear();
        for (int priority : sorted) {
            Priorities.get(priority).setActive(true);
        }
        for (int priority : sorted) {
            String firable = Priorities.get(priority).getTitle();
            if (Titles.get(firable).isFirable()) {
                String firing = Titles.get(firable).fireAll().get(0);
                for (String inhibit : Inhibit.get(firable)) {
                    inhibitModule(inhibit);
                }
                if (firing.length() > 0) {
                    Output.put(firable, firing);
                    res.add(Output.get(firable));
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        String res = "\n";
        ArrayList<Integer> sorted = new ArrayList(Priorities.keySet());
        Collections.sort(sorted);
        ArrayList<String> inhibitedModules = this.listInhibitedModules();
//        Collections.reverse(sorted);
        RuleBaseSystem r;
        for (int p : sorted) {
            r = Priorities.get(p);
            res += "" + p + " || " + String.format("%-20s\t", r.getTitle()) + (Inhibit.get(r.getTitle()) == null ? "" : "[I]" + Inhibit.get(r.getTitle())) + "\n";
            for (int i = 0; i < r.size(); i++) {
                res += "\t|" + String.format("%-20s\t", r.getRule(i).getLabel())
                        + " " + (r.getRule(i).isFirable() ? "*\t" : "\t")
                        + " " + (inhibitedModules.contains(r.getTitle()) ? "X\t" : "\t")
                        + "\n";
            }
        }
        return res;
    }
}
