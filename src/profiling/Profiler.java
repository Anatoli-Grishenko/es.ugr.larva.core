/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package profiling;

import crypto.Keygen;
import data.Ole;
import jade.lang.acl.ACLMessage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import static profiling.fakeData.FAKESEQUENCES;
import tools.NetworkCookie;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Profiler {

    protected HashMap<String, ProfilingTicket> Tickets;
    protected ArrayList<ProfilingTicket> Stack;
    protected String owner, Record = "";
    protected ProfilingTicket ptMaster;
    protected boolean active = false;
    protected int minDepth = 0, maxDepth = -1;
    protected String sep = "\t", tsvFileName = "./profiler.tsv";
    private PrintStream ps = null;
    protected Semaphore sWrite = new Semaphore(1);

    public static final String MONITORMARK = "PROFILERCOOKIE";

    public static boolean isProfiler(ACLMessage msg) {
        if (msg != null) {
            return msg.getUserDefinedParameter(MONITORMARK) != null;
        } else {
            return false;
        }
    }

    public static NetworkCookie extractProfiler(ACLMessage msg) {
        NetworkCookie nc = new NetworkCookie();
        if (isProfiler(msg)) {
            Ole ocookie = new Ole(msg.getUserDefinedParameter(MONITORMARK));
            nc = (NetworkCookie) Ole.oleToObject(ocookie, NetworkCookie.class);
        }
        return nc;
    }

    public static ACLMessage injectProfiler(ACLMessage msg, NetworkCookie nc) {
        if (nc == null)
            return msg;
        if (msg.getReplyWith() != null) {
            nc.setReplyID(msg.getReplyWith());
        }
        Ole onc = Ole.objectToOle(nc);
        msg.addUserDefinedParameter(MONITORMARK, onc.toPlainJson().toString());
        return msg;
    }

    public static String getfakePayload(int size) {
        String res = FAKESEQUENCES[(int) (Math.random() * FAKESEQUENCES.length)];
        while (res.length() < size) {
            res = res + FAKESEQUENCES[(int) (Math.random() * FAKESEQUENCES.length)];
        }
        return res;
    }

    public Profiler() {
        Tickets = new HashMap<>();
        Stack = new ArrayList<>();
        ptMaster = new ProfilingTicket();
        clear();

    }

    public void clear() {
        Tickets.clear();
        Stack.clear();
        ptMaster = new ProfilingTicket();
        ptMaster.setDescription("MASTER");
        ptMaster.setDepth(minDepth);
        addTicket(ptMaster);
    }

    public void close() {
        while (!Stack.isEmpty()) {
            toc(getTail().getDescription());
        }
        setActive(false);
    }

    public String getTsvFileName() {
        return tsvFileName;
    }

    public void setTsvFileName(String tsvFileName) {
        this.tsvFileName = tsvFileName;
    }

    public HashMap<String, ProfilingTicket> getTickets() {
        return Tickets;
    }

    public void setTickets(HashMap<String, ProfilingTicket> Tickets) {
        this.Tickets = Tickets;
    }

    public ArrayList<ProfilingTicket> getStack() {
        return Stack;
    }

    public void setStack(ArrayList<ProfilingTicket> Stack) {
        this.Stack = Stack;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getMinDepth() {
        return minDepth;
    }

    public void setMinDepth(int minDepth) {
        this.minDepth = minDepth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    protected void addTicket(ProfilingTicket pt) {
        Tickets.put(pt.getId(), pt);
        if (!Stack.isEmpty()) {
            int depth = getTail().getDepth();
            pt.setSupertiket(getTail().getId());
            pt.setDepth(++depth);
            if (depth > getMaxDepth()) {
                setMaxDepth(depth);
            }
        }
        Stack.add(pt);
    }

    public void sWait(String t) {
        System.out.println(getOwner() + " watiting " + t);
        try {
            sWrite.acquire();
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
    }

    public void sGo() {
        sWrite.release();
        System.out.println(getOwner() + " release");
    }

    public ProfilingTicket tic(String description, String series) {
//          TimeHandler th = new TimeHandler();
        if (!isActive()) {
            return null;
        }
//        sWait("tic-"+description+"-"+series);
        ProfilingTicket pt = new ProfilingTicket();
        pt.setOwner(getOwner());
        pt.setDescription(description);
        pt.setSeries(series);
        pt.setStart(TimeHandler.NetNow());
        addTicket(pt);
//        Tickets.put(pt.getId(), pt);
//        if (!Stack.isEmpty()) {
//            int depth = getTail().getDepth();
//            pt.setSupertiket(getTail().getId());
//            pt.setDepth(++depth);
//            if (depth > getMaxDepth()) {
//                setMaxDepth(depth);
//            }
//        }
//        Stack.add(pt);
//        sGo();

        return pt;
    }

    public ProfilingTicket tic(String description) {
        ProfilingTicket ptaux;
//        sWait("tic-"+description+"-");
        ptaux = tic(description, "");
//        sGo();
        return ptaux;
    }

    public ProfilingTicket toc(String description) {
//        TimeHandler th = new TimeHandler();
        if (!isActive()) {
            return null;
        }
//        sWait("toc-" + description);
        ProfilingTicket pt = getTail();
        if (pt != null && pt.getDescription().equals(description)) {
            pt.setEnd(TimeHandler.NetNow());
            saveTicket(pt);
            removeTail();
        }
//        sGo();
        return pt;
    }

    public String getHeader(ProfilingTicket pt) {
        if (pt.getSupertiket().length() == 0) {
            return getOwner() + sep + pt.getDescription() + sep + pt.getSeries();
        } else {
            return getHeader(getTicket(pt.getSupertiket())) + sep + pt.getDescription() + sep + pt.getSeries();
        }

    }

    public void saveTicket(ProfilingTicket pt) {
        if (!isActive()) {
            return;
        }
//        sWait("save");
        String prefix = getHeader(pt);
        String spt;
        spt = pt.getDepth() + sep + pt.getElapsedTimeMilisecs() + sep + prefix;
        if (ps == null) {
            try {
                ps = new PrintStream(new File(getTsvFileName()));
            } catch (FileNotFoundException ex) {
            }
        }
        ps.println(spt);
        Record += spt + "\n";
//        System.out.println(spt);
//        sGo();
    }

    public ProfilingTicket getHead() {
        if (!Stack.isEmpty()) {
            return Stack.get(0);
        } else {
            return null;
        }
    }

    public ProfilingTicket getTail() {
        if (!Stack.isEmpty()) {
            return Stack.get(Stack.size() - 1);
        } else {
            return null;
        }
    }

    public void removeTail() {
        if (!Stack.isEmpty()) {
            Stack.remove(Stack.size() - 1);
        }
    }

    public void profileThis(String description, String series, Runnable r) {
        if (isActive()) {
            tic(description, series);
            r.run();
            toc(description);
        } else {
            r.run();
        }
    }

    public void profileThis(String description, Runnable r) {
        profileThis(description, "", r);
    }

    public ProfilingTicket getTicket(String id) {
        return Tickets.get(id);
    }

    public ArrayList<ProfilingTicket> getTickets(ProfilingTicket pparent) {
        ArrayList<ProfilingTicket> res = new ArrayList<>();
        for (ProfilingTicket pt : Tickets.values()) {
            if (pt.getSupertiket().equals(pparent.getId())) {
                res.add(pt);
            }
        }
        return res;
    }

    public ArrayList<ProfilingTicket> getTickets(int depth) {
        ArrayList<ProfilingTicket> res = new ArrayList<>();
        for (ProfilingTicket pt : Tickets.values()) {
            if (pt.getDepth() == depth) {
                res.add(pt);
            }
        }
        return res;
    }

    public ArrayList<ProfilingTicket> getTickets(String description) {
        ArrayList<ProfilingTicket> res = new ArrayList<>();
        for (ProfilingTicket pt : Tickets.values()) {
            if (pt.getDescription().equals(description)) {
                res.add(pt);
            }
        }
        return res;
    }

    @Override
    public String toString() {
        String res = "";
        for (int d = getMinDepth(); d <= getMaxDepth(); d++) {
            for (ProfilingTicket pt : getTickets(d)) {
                res += pt.getDepth() + "-" + pt.getDescription() + "-" + pt.getSeries() + " " + pt.getElapsedTimeMilisecs() + "\n";
            }
        }
        return res;
    }

    public String prettyPrint() {
        return "PROFILING +" + getOwner() + "\n"
                + RprettyPrint(ptMaster);
    }

    public String RprettyPrint(ProfilingTicket pparent) {
        String res = "", indent = "";
        for (int i = 0; i < pparent.getDepth(); i++) {
            indent += "\t";
        }
        res = res + indent + pparent.getDepth()
                + "-" + pparent.getDescription()
                + "-" + pparent.getSeries()
                + " " + pparent.getElapsedTimeMilisecs() + "\n";
        for (ProfilingTicket pt : getTickets(pparent)) {
            res += RprettyPrint(pt);
        }
        return res;
    }

    public void saveTSV() {
        PrintStream ps = null;
        try {
            ps = new PrintStream(new File(getTsvFileName()));
            ps.println(RprettyPrintTSV(ptMaster, getOwner()));
        } catch (FileNotFoundException ex) {
        } finally {
            ps.close();
        }

    }

    public String prettyPrintTSV() {
        return RprettyPrintTSV(ptMaster, getOwner());
    }

    public String RprettyPrintTSV(ProfilingTicket pparent, String prefix) {
        String res = "", myprefix;
        myprefix = pparent.getDescription()
                + sep + pparent.getSeries();
        res = res + pparent.getDepth()
                + sep + pparent.getElapsedTimeMilisecs()
                + sep + prefix + sep + myprefix + "\n";

        for (ProfilingTicket pt : getTickets(pparent)) {
            res = res + RprettyPrintTSV(pt, prefix + sep + myprefix);
        }
        return res;
    }

    public void setSeries(String series) {
        if (isActive()) {
            getTail().setSeries(series);
        }
    }

    public void saveAll(String filename) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter(filename, true);
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);
            pw.println(Record);
            pw.flush();
        } catch (IOException ex) {
        } finally {
            try {
                pw.close();
                bw.close();
                fw.close();
            } catch (IOException io) {
            }
        }
    }
}
