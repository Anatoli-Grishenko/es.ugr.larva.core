/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import Environment.Environment;
import ai.Decisor;
import ai.Choice;
import ai.DecisionSet;
import ai.Rule;
import ai.RuleBaseSystem;
import console.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Subsumption extends Decisor {

    protected HashMap<String, RuleBaseSystem> Titles;
    protected HashMap<Integer, RuleBaseSystem> Priorities;
    protected HashMap<String, String> Output;
    protected HashMap<String, ArrayList<String>> Inhibit;
    protected HashMap<String, ArrayList<String>> Supress;
    protected boolean debug;
    protected ArrayList<String> inhibitedModules, supressedModules, firableRules, firableModules;
    DecisionSet primaryDS;

    @Override
    public double getUtility(Environment e, Choice c) {
        for (String which : Titles.keySet()) {
            if (Titles.get(which).listAllRules().contains(c.getName())) {
                return Titles.get(which).getPriority();
            }
        }
        return 0;
    }

    @Override
    public boolean getElegibility(Environment e, Choice c) {
        for (String which : Titles.keySet()) {
            if (Titles.get(which).listAllRules().contains(c.getName())) {
                return Titles.get(which).isActive();
            }
        }
        return false;
    }

    @Override
    public DecisionSet MakeBestDecision(Environment E) {
//        DecisionSet result = new DecisionSet();
        this.fire();
//        
//        for (String s : this.getPlan()) {
//            String[] sdebug = s.split("\\|\\|");
//            result.add(new Choice(sdebug[3].replace("A>", "")).
//                    setUtility(Titles.get(sdebug[1].replace("M>", "")).getPriority()));
//        }
        return DecisionSet;
    }

    public Subsumption() {
        super();
        Titles = new HashMap();
        Priorities = new HashMap();
        Output = new HashMap();
        Inhibit = new HashMap();
        Supress = new HashMap();
        this.setDebug(false);
        primaryDS = new DecisionSet();
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

    public String getModuleOf(Rule r) {
        for (String module : Titles.keySet()) {
            if (Titles.get(module).listAllRules().contains(r.getClass())) {
                return module;
            }
        }
        return "";
    }

    public int size() {
        return Titles.size();
    }

    public Subsumption addLayer(String Title, int priority) {
        RuleBaseSystem r = new RuleBaseSystem().setPriority(priority).setTitle(Title).setDebug(this.isDebug());
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

    public void computeElegibility() {
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

    public Subsumption inhibit(String fromLayer, String toLayer) {
        Inhibit.get(fromLayer).add(toLayer);
        return this;
    }

    public Subsumption supress(String fromLayer, String toLayer) {
        Supress.get(fromLayer).add(toLayer);
        return this;
    }

    public Subsumption addRule(String Layer, Rule r) {
        getLayer(Layer).addRule(r);
        primaryDS.addChoice(new Choice(r.getLabel()));
        return this;
    }

    public void fire() {
        ArrayList<String> res = new ArrayList(), firables = new ArrayList(), inhibited = new ArrayList();
        ArrayList<Integer> sorted = new ArrayList(Priorities.keySet());
        Collections.sort(sorted);
        RuleBaseSystem rbs;
        Collections.reverse(sorted);
        this.Output.clear();
        computeElegibility();
        for (Choice c : DecisionSet) {
            c.setUtility(Choice.MIN_UTILITY).setValid(false);
        }
        for (int priority : sorted) {
            String module = Priorities.get(priority).getTitle();
            if (Titles.get(module).isFirable() && Titles.get(module).isActive()) {
                for (String firing : Titles.get(module).fireAll()) {
                    DecisionSet.getChoice(firing).setMaxUtility(priority).setValid(true);
                    if (debug) {
                        firing = "||M>" + Titles.get(module).getTitle() + firing;
                    }
//                    if (firing.length() > 0) {
                    Output.put(module, firing);
//                    }
                }
            }
        }
        Collections.sort(DecisionSet);
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

    public Rule getRule(String rulelabel) {
        for (String module : Titles.keySet()) {
            if (Titles.get(module).listAllRules().contains(rulelabel)) {
                return Titles.get(module).getRule(rulelabel);
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return DecisionSet.get(0).getUtility() == Choice.MIN_UTILITY;
    }

    @Override
    public String toString() {
        String res = "\n";
        computeElegibility();
        ArrayList<Integer> sorted = new ArrayList(Priorities.keySet());
        Collections.sort(sorted);
        RuleBaseSystem r;
        for (int p : sorted) {
            r = Priorities.get(p);
            if (r.isActive()) {
                res += Console.defText(Console.gray);
            } else {
                res += Console.defText(Console.lightred);
            }
            res += "  " + String.format("%4d", p) + " ";
            if (sorted.indexOf(p) == sorted.size() - 1) {
                res += Console.windowFrames[0].charAt(2);
            } else {
                res += Console.windowFrames[0].charAt(10);
            }
            res += String.format("%-10s", r.getTitle());
            res += " " + (Inhibit.get(r.getTitle()) == null ? "" : "\t[I]" + Inhibit.get(r.getTitle()) + "\t")
                    + (Supress.get(r.getTitle()) == null ? "\t" : "[S]" + Supress.get(r.getTitle())) + "\n";
            for (int i = 0; i < r.size(); i++) {
                if (!r.isActive()) {
                    res += Console.defText(Console.lightred);
                } else if (r.getRule(i).isFirable()) {
                    res += Console.defText(Console.lightgreen);
                } else {
                    res += Console.defText(Console.gray);
                }
                if (i < r.size() - 1) {
                    res += "       " + Console.windowFrames[0].charAt(5) + "   " + Console.windowFrames[0].charAt(10);
                } else {
                    res += "       " + Console.windowFrames[0].charAt(5) + "   " + Console.windowFrames[0].charAt(2);
                }
                res += " " + String.format("%-20s\t", r.getRule(i).getLabel())
                        + " " + (r.getRule(i).isFirable() ? "A\t" : "\t")
                        + " " + (inhibitedModules.contains(r.getTitle()) ? "I\t" : "\t")
                        + " " + (supressedModules.contains(r.getTitle()) ? "S\t" : "\t")
                        + "\n";
            }
        }
        res += "  A=" + DecisionSet + "\n";
        return res;
    }

//    public String toString() {
//        String res = "\n";
//        computeElegibility();
//        ArrayList<Integer> sorted = new ArrayList(Priorities.keySet());
//        Collections.sort(sorted);
//        RuleBaseSystem r;
//        for (int p : sorted) {
//            r = Priorities.get(p);
//            res += "\t" + p + " || " + String.format("%-20s\t", r.getTitle());
//            res += "\t" +(Inhibit.get(r.getTitle()) == null ? "\t" : "[I]" + Inhibit.get(r.getTitle()) + "\t")
//                    + (Supress.get(r.getTitle()) == null ? "\t" : "[S]" + Supress.get(r.getTitle())) + "\n";
//            for (int i = 0; i < r.size(); i++) {
//                res += "\t" +"\t|" + String.format("%-20s\t", r.getRule(i).getName())
//                        + " " + (r.getRule(i).isFirable() ? "*\t" : "\t")
//                        + " " + (inhibitedModules.contains(r.getTitle()) ? "I\t" : "\t")
//                        + " " + (supressedModules.contains(r.getTitle()) ? "S\t" : "\t")
//                        + "\n";
//            }
//        }
//        return res;
//    }
//
}
