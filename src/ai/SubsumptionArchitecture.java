/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
    protected boolean debug;
    protected ArrayList<String> plan, inhibitedModules, supressedModules, firableRules, firableModules;

    public SubsumptionArchitecture() {
        Titles = new HashMap();
        Priorities = new HashMap();
        Output = new HashMap();
        Inhibit = new HashMap();
        Supress = new HashMap();
    }

    public ArrayList<String> getInhibitedModules() {
        return inhibitedModules;
    }

    public ArrayList<String> getSupressedModules() {
        return supressedModules;
    }

    public ArrayList<String> getFirableRules() {
        return firableRules;
    }

    public ArrayList<String> getFirableModules() {
        return firableModules;
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
            if (Titles.get(layer).listFirablesRules().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public void deactivateModule(String module) {
        if (Titles.get(module) != null) {
            Titles.get(module).setActive(false);
        }
    }

    public void arrangeModules() {
        ArrayList<String> result = new ArrayList(), aux;
        ArrayList<Integer> sorted = new ArrayList(Priorities.keySet());
        Collections.sort(sorted);
        RuleBaseSystem rbs;
        Collections.reverse(sorted);
        firableRules = new ArrayList();
        for (int priority : sorted) {
            Priorities.get(priority).setActive(true);
            aux = Priorities.get(priority).listFirablesRules();
            if (aux.size() > 0) {
                for (String rulename : aux) {
                    firableRules.add("" + priority + "::" + Priorities.get(priority).getTitle() + "::" + rulename);
                }
            }
        }
        inhibitedModules = new ArrayList();
        supressedModules = new ArrayList();
        for (int priority : sorted) {
            rbs = Priorities.get(priority);
            if (rbs.isActive()) {
                if (Inhibit.get(rbs.getTitle()) != null) {
                    for (String inhibit : Inhibit.get(rbs.getTitle())) {
                        inhibitedModules.add(inhibit);
                        deactivateModule(inhibit);
                    }
                }
                if (rbs.isFirable() && Supress.get(rbs.getTitle()) != null) {
                    for (String inhibit : Supress.get(rbs.getTitle())) {
                        supressedModules.add(inhibit);
                        deactivateModule(inhibit);
                    }
                }
            }
        }
    }

    public SubsumptionArchitecture inhibit(String fromLayer, String toLayer) {
        Inhibit.get(fromLayer).add(toLayer);
        return this;
    }

    public SubsumptionArchitecture supress(String fromLayer, String toLayer) {
        Supress.get(fromLayer).add(toLayer);
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
        plan = new ArrayList();
        arrangeModules();
        for (int priority : sorted) {
            String module = Priorities.get(priority).getTitle();
            if (Titles.get(module).isFirable() && Titles.get(module).isActive()) {
                for (String firing : Titles.get(module).fireAll()) {
                    if (debug) {
                        firing = "||M>" + Titles.get(module).getTitle() + firing;
                    }
//                    if (firing.length() > 0) {
                        Output.put(module, firing);
                        plan.add(firing);
//                    }
                }
            }
        }
        return plan;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        for (RuleBaseSystem rbs : this.Titles.values()) {
            rbs.setDebug(debug);
        }
    }

    public ArrayList<String> getPlan() {
        return plan;
    }

    public String getPlan(int i) {
        if (i > plan.size()) {
            return "";
        }
        if (this.isDebug()) {
            String splan[] = plan.get(i).split("\\|\\|");
            if (splan.length < 1) {
                return "";
            }
            if (splan.length < 3) {
                return getPlan(i + 1);
            }
            return splan[3].replace("A>", "");
        } else {
            return plan.get(i);
        }
    }

    public boolean isEmpty() {
        return plan == null || plan.size() < 1;
    }

    @Override
    public String toString() {
        String res = "\n";
        arrangeModules();
        ArrayList<Integer> sorted = new ArrayList(Priorities.keySet());
        Collections.sort(sorted);
        RuleBaseSystem r;
        for (int p : sorted) {
            r = Priorities.get(p);
            res += "" + p + " || " + String.format("%-20s\t", r.getTitle());
            res += (Inhibit.get(r.getTitle()) == null ? "\t" : "[I]" + Inhibit.get(r.getTitle()) + "\t")
                    + (Supress.get(r.getTitle()) == null ? "\t" : "[S]" + Supress.get(r.getTitle())) + "\n";
            for (int i = 0; i < r.size(); i++) {
                res += "\t|" + String.format("%-20s\t", r.getRule(i).getLabel())
                        + " " + (r.getRule(i).isFirable() ? "*\t" : "\t")
                        + " " + (inhibitedModules.contains(r.getTitle()) ? "I\t" : "\t")
                        + " " + (supressedModules.contains(r.getTitle()) ? "S\t" : "\t")
                        + "\n";
            }
        }
        return res;
    }
}
