/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class TimeTable {

    protected ArrayList<Assignment> Assignments;
    protected HashMap<String, Assignment> Last;

    public TimeTable() {
        Assignments = new ArrayList<>();
        Last = new HashMap<>();
    }

    public int getNAgents() {
        return Last.keySet().size();
    }

    public ArrayList<String> getAllAgents() {
        return new ArrayList(Last.keySet());
    }

    public int getNOccurrences(String resource) {
        return getAllOCurrences(resource).size();
    }

    public Assignment getLast(String name) {
        return Last.get(name);
    }

    public int getMakespan() {
        Assignment asmax = null;
        int makespan = 0;
        for (Assignment as : this.Assignments) {
            if (as.getTend() > makespan) {
                asmax = as;
                makespan = as.getTend();
            }
        }
        return asmax.getTend();
    }

    public int getNCaptures() {
        int captures = 0;
        for (Assignment as : this.Assignments) {
            if (as.getAction().startsWith("CAPTURE")) {
                captures += as.getCargo();
            }
        }
        return captures;
    }

    public ArrayList<Assignment> getAllOCurrences(String resource) {
        Assignment aux = Last.get(resource);
        ArrayList<Assignment> res = new ArrayList();
        if (aux != null) {
            res.add(aux);
            while (!aux.getPre().isEmpty()) {
                if (aux.getPre().get(0).getAgent().equals(resource)
                        || aux.getPre().get(0).getBackup().equals(resource)) {
                    aux = aux.getPre().get(0);
                    res.add(0, aux);
                } else {
                    aux = aux.getPre().get(1);
                    res.add(0, aux);
                }
            }
        }
        return res;
    }

    public void addAssignment(Assignment a) {
        Assignments.add(a);
        Assignment prev;
        int anchor, dif;
        if (Last.get(a.getAgent()) != null) {
            prev = Last.get(a.getAgent());
            anchor = prev.getTend();

            dif = anchor - a.getTini();
            if (dif > 0) {
                a.setTend(anchor + (a.getTend() - a.getTini()));
                a.setTini(anchor);
                a.getPre().add(prev);
                prev.getPost().add(a);
            }
            if (a.getBackup() != null) {
                prev = Last.get(a.getBackup());
                anchor = prev.getTend();
                dif = anchor - a.getTini();
                a.getPre().add(prev);
                if (dif > 0) {
                    a.setTini(a.getTini() + dif);
                    a.setTend(a.getTend() + dif);
                }
                prev.getPost().add(a);
            }
        }
//        System.out.println("Adding " + a.toString());
        Last.put(a.getAgent(), a);
    }

    public void addAssignmentMTT(Assignment a) {
        Assignments.add(a);
        Assignment prev;
        int anchor, dif, dur = a.getTend() - a.getTini();
        if (Last.get(a.getAgent()) != null) {
            prev = Last.get(a.getAgent());
            anchor = prev.getTend();
            a.setTini((int) (Math.max(anchor, a.getTini())));
            a.setTend(a.getTini() + dur);
            if (a.getBackup() != null) {
                prev = Last.get(a.getBackup());
                anchor = prev.getTend();
                a.setTini((int) (Math.max(anchor, a.getTini())));
                a.setTend(a.getTini() + dur);
            }
        }
        System.out.println("Adding " + a.toString());
        Last.put(a.getAgent(), a);
    }

    public String getSolution(String who) {
        String res = "";
        Assignment aux;
        ArrayList<Assignment> border = new ArrayList();
        ArrayList<String> Candidates = new ArrayList();
        if (who == null || who.length() == 0) {
            Candidates = new ArrayList(Last.keySet());
        } else {
            Candidates.add(who);
        }
        for (String sname : Candidates) {
            border.add(getAllOCurrences(sname).get(0));
        }
        while (!border.isEmpty()) {
            aux = border.get(0);
            for (Assignment as : border) {
                if (aux.getTini() > as.getTini()) {
                    aux = as;
                }
            }
            res += aux.toString();
            border.remove(aux);
            if (aux.getPost().size() > 0) {
                border.add(aux.getPost().get(0));
            }
            if (aux.getPost().size() > 1) {
                border.add(aux.getPost().get(1));
            }
        }
        return res;
    }

    public Mission getFullMission(String who) {
        String res = "";
        Assignment aux;
        ArrayList<Assignment> border = new ArrayList();
        ArrayList<String> Candidates = new ArrayList();
        Mission m = new Mission(who);
        Candidates.add(who);
        for (String sname : Candidates) {
            border.add(getAllOCurrences(sname).get(0));
        }
        while (!border.isEmpty()) {
            aux = border.get(0);
            for (Assignment as : border) {
                if (aux.getTini() > as.getTini()) {
                    aux = as;
                }
            }
            if (!aux.getAction().toUpperCase().startsWith("STARTING")) {
                m.addGoal(aux.getAction());
                res += aux.toString();
            }
            border.remove(aux);
            if (aux.getPost().size() > 0) {
                border.add(aux.getPost().get(0));
            }
            if (aux.getPost().size() > 1) {
                border.add(aux.getPost().get(1));
            }
        }
        return m;
    }

    public MissionSet getMissionSet(String who) {
        String res = "";
        Assignment aux;
        ArrayList<Assignment> border = new ArrayList();
        ArrayList<String> Candidates = new ArrayList();
        Mission m = new Mission(who);
        MissionSet ms = new MissionSet();
        Candidates.add(who);
        for (String sname : Candidates) {
            border.add(getAllOCurrences(sname).get(0));
        }
        m = new Mission(ms.size() + " " + who);
        while (!border.isEmpty()) {
            aux = border.get(0);
            for (Assignment as : border) {
                if (aux.getTini() > as.getTini()) {
                    aux = as;
                }
            }
            if (!aux.getAction().toUpperCase().startsWith("STARTING")) {
                m.addGoal(aux.getAction());
            }
            if (aux.getAction().startsWith("TRANSFER")) {
                ms.addMission(m.toString());
                m = new Mission(who + "(" + ms.size() + ")");
            }

            res += aux.toString();
            border.remove(aux);
            if (aux.getPost().size() > 0) {
                border.add(aux.getPost().get(0));
            }
            if (aux.getPost().size() > 1) {
                border.add(aux.getPost().get(1));
            }
        }
        return ms;
    }

    @Override
    public String toString() {
        return getSolution("") + "\n\nNMAKESPAN: " + this.getMakespan() + " (t.u.)\nCAPTURES: " + this.getNCaptures();
    }

    public Assignment getEarlierAssignment(int time) {
        Assignment res = Last.get(new ArrayList(Last.keySet()).get(0));
        int min = res.getTend();
        for (String sres : Last.keySet()) {
            if (Last.get(sres).getTini() >= time && Last.get(sres).getTend() < min) {
                res = Last.get(sres);
                min = res.getTend();
            }
        }
        return res;
    }

    public Assignment getAssignment(String Id) {
        for (Assignment res : getAllOCurrences("")) {
            if (res.getId().equals(Id)) {
                return res;
            }
        }
        return null;
    }

}
