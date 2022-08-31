/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import static disk.Logger.trimString;
import jade.lang.acl.ACLMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static messaging.ACLMessageTools.isInitiator;
import tools.StringTools;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class SequenceDiagram {

    ArrayList<String> players, ACLMIDs, CIDs, bars;
    ArrayList<Sequence> sequences;
    ArrayList<Integer> open;
    int width = 36;
    String blackboard = "", template = "%-" + width + "s", nextline = "", owner, lastDiagram = "", lastSequence = "", style;
    boolean changes = false, redraw = false;
    String initiator = "", continuer = "", vert = "", horizInit = "", horizCont = "", left = "", right = "", leftInitInit = "", rightInitInit = "", leftInitCont = "", rightInitCont = "";
//    String initiator="O", continuer="+", vert="|", horizInit="-";
//    String initiator = "█", continuer = "╂", vert = "┃", horizInit = "─", left = "<", right = ">";
//    String initiator = "▌", continuer = "▌", vert = "▌", horizInit = "═", horizCont = "─",  left = "<", right = ">", leftInitInit = "╡", rightInitInit = "╞", leftInitCont = "┤", rightInitCont = "├";
//    String initiator = "│", continuer = "│", vert = "│", horizInit = "─", horizCont = "─",  left = "<", right = ">", leftInitInit = "┤", rightInitInit = "├", leftInitCont = "┤", rightInitCont = "├";

    public SequenceDiagram(String o) {
        if (owner == null) {
            owner = o;
        }
        clear();
    }

    public boolean hasChanges() {
        return changes;
    }

    public boolean needsRedraw() {
        return redraw;
    }

    public void setChanges(boolean changes) {
        this.changes = changes;
    }

    public String getOwner() {
        return owner;
    }

    public void clear() {
        players = new ArrayList();
        ACLMIDs = new ArrayList();
        CIDs = new ArrayList();
        sequences = new ArrayList();
        open = new ArrayList();
        bars = new ArrayList();
        blackboard = "";
        nextline = "";
    }

    public int size() {
        return sequences.size();
    }

    public int indexPlayer(String name) {
        if (players.contains(name)) {
            return players.indexOf(name);
        } else {
            return -1;
        }
    }

    public int getNPlayers() {
        return players.size();
    }

    public synchronized void addPlayer(String name) {
        if (name.length() > 0 && !players.contains(name)) {
            players.add(name);
            bars.add(vert);
            changes = true;
            redraw = true;
            open.add(0);
        }
    }

    public String getPlayer(int i) {
        if (0 <= i && i < size()) {
            return players.get(i);
        } else {
            return "";
        }
    }

    public ArrayList<String> getAllPlayers() {
        return players;
    }

    public ArrayList<String> getAllConversations() {
        return CIDs;
    }

    public void nextLine() {
        blackboard += nextline + "\n";
        nextline = "";
    }

    public String fillField(String fill, int ncolumns) {
        String res = "";
        int tope = width * ncolumns;
        for (; res.length() < tope - 1;) {
            res += fill;
        }
        return res;
    }

    public synchronized String printStatus(int nplayer) {
        if (nplayer >= 0) {
            if (open.get(nplayer) > 0) {
                return initiator;
            } else if (open.get(nplayer) <= Integer.MIN_VALUE / 2) {
                return " ";
            } else if (open.get(nplayer) <= 0) {
                return vert;
            } else {
                return " ";
            }
        } else {
            return " ";
        }
    }

    public synchronized String printEmptyPlayer(int nplayer) {
        String res = "";
        res += printStatus(nplayer) + this.fillField(" ", 1);
        return res;
    }

    public synchronized String printAllPlayers() {
        String res = "";
        for (int i = 0; i < players.size(); i++) {
            try {
                open.set(i, 0);
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
            if (players.get(i).equals(this.getOwner())) {
//               res += vert+StringTools.pasteBegin(fillField(" ", 1), players.get(i));
                res += printStatus(i) + StringTools.pasteBegin(fillField(" ", 1), this.rightInitCont + players.get(i));
            } else {
                res += printStatus(i) + StringTools.pasteBegin(fillField(" ", 1), players.get(i));

            }
//            nextline += vert + String.format(template, fillField((players.get(i).equals(this.getOwner()) ? this.rightInitCont : "") + players.get(i), ' ',""));
        }
        return res;
    }

    public synchronized String printAllEmptyPlayers(int from, int to) {
        String res = "";
        for (int i = from; i < to; i++) {
            res += printEmptyPlayer(i);
        }
        return res;
    }

    public synchronized String printField(Sequence s, String field) {
        String sfield = "";
        switch (field) {
            case "CID ":
                sfield = s.conversationid + " (" + printCIDOrder(s) + ")";
                break;
            case "RPW ":
                sfield = s.replywith;
                break;
            case "RPB ":
                sfield = s.replyby;
                break;
            case "IRT ":
                sfield = s.inreplyto;
                break;
            case "CONT":
                sfield = s.content;
                break;
            case "DATE":
                sfield = s.date;
                break;
            case "PERF":
                sfield = ACLMessage.getPerformative(s.performative);
                break;
        }
        if (sfield != null) {
            int isender = this.indexPlayer(s.sender);
            return this.printAllEmptyPlayers(0, isender)
                    + printStatus(isender) + StringTools.pasteBegin(this.fillField(" ", 1), "│" + field + ":" + sfield)
                    + this.printAllEmptyPlayers(isender + 1, players.size());
        } else {
            return "";
        }
    }

    public synchronized String printArrow(Sequence s) {
        String res = "", arrow, arrowl, arrowr, horiz;
        int from, to, ifrom = this.indexPlayer(s.sender), ito;
        if (s.performative < 0) {
            return res;
        }
        for (String ireceiver : s.receiver.split(",")) {
            ito = indexPlayer(ireceiver);
            from = Math.min(ifrom, ito);
            to = Math.max(ifrom, ito);
            if (style.equals("CONVERSATION")) {
                if (isInitiator(s.performative)) {
                    horiz = horizInit;
                } else {
                    horiz = horizCont;
//                    arrow = StringTools.pasteBegin(this.fillField(horizCont, to - from), vert);
                }
            } else {
                horiz = horizCont;
            }
            arrow = this.fillField(horiz, to - from);
            if (ifrom < ito) {
                if (style.equals("CONVERSATION")) {
                    arrow = StringTools.pasteBegin(arrow, "├" + printCIDOrder(s));
                }
                arrow = StringTools.pasteEnd(arrow, right);
                res += this.printAllEmptyPlayers(0, from)
                        + printStatus(from)
                        + arrow
                        + this.printAllEmptyPlayers(to, players.size()) + "\n";
//                arrowl = "├";
//                arrowr = "";
//                arrowr = "┤";
//                arrowl = horiz;
            } else {
                if (style.equals("CONVERSATION")) {
                    arrow = StringTools.pasteEnd(arrow, printCIDOrder(s));
                }
                arrow = StringTools.pasteBegin(arrow, left);
                res += this.printAllEmptyPlayers(0, from)
                        + printStatus(from)
                        + arrow
                        + StringTools.pasteBegin(this.printAllEmptyPlayers(to, players.size()), horiz + "┤") + "\n";
            }
        }
        return res;
    }

    public synchronized String printSequence(Sequence s) {
        String res = "";
        if (s.performative < 0) {
            if (this.indexPlayer(s.sender) >= 0) {
                open.set(this.indexPlayer(s.sender), Integer.MIN_VALUE / 2);
            }
            res += this.printField(s, "DATE") + "\n";
            return res;
        }
        res += this.printField(s, "DATE") + "\n";
        if (style.equals("CONVERSATION")) {
            res += this.printField(s, "PERF") + "\n";
            res += this.printField(s, "CID ") + "\n";
            res += this.printField(s, "RPW ") + "\n";
            res += this.printField(s, "IRT ") + "\n";
            if (s.replyby != null) {
                res += this.printField(s, "RPB ") + "\n";
            }
        }
        res += this.printField(s, "CONT") + "\n";
        res += this.printArrow(s);
        if (isInitiator(s.performative)) {
            for (String ireceiver : s.receiver.split(",")) {
                open.set(this.indexPlayer(s.sender), open.get(this.indexPlayer(s.sender)) + 1);
                open.set(this.indexPlayer(ireceiver), open.get(this.indexPlayer(ireceiver)) + 1);
            }
        } else if (s.performative >= 0) {
            for (String ireceiver : s.receiver.split(",")) {
                open.set(this.indexPlayer(s.sender), open.get(this.indexPlayer(s.sender)) - 1);
                open.set(this.indexPlayer(ireceiver), open.get(this.indexPlayer(ireceiver)) - 1);
            }
        }
        if (s.performative < 0) {
            open.set(this.indexPlayer(s.sender), -1);
        }
        return res;
    }

    public synchronized String printSequenceDiagram() {
//        this.blackboard = "|....+....+....+....+....+....+....+|....+....+....+....+....+....+....+|....+....+....+....+....+....+....+|....+....+....+....+....+....+....+|\n";
        blackboard = "";
        if (hasChanges()) {
            blackboard += printAllPlayers() + "\n";
            blackboard += printAllEmptyPlayers(0, players.size()) + "\n";
            for (int i = 0; i < size(); i++) {
                blackboard += this.printSequence(sequences.get(i));
                blackboard += printAllEmptyPlayers(0, players.size()) + "\n";
            }
            lastDiagram = blackboard;
            changes = false;
            redraw = false;
        }
        return lastDiagram;

    }

    public synchronized void addSequence(ACLMessage msg, String agentName) {
        if (msg != null) {
            msg = ACLMessageTools.secureACLM(msg);
            if (initiator.length() == 0) {
                if (msg.getConversationId().length() == 0) {
                    this.defineStyle("SIMPLE");
                } else {
                    this.defineStyle("CONVERSATION");
                }
            }
            if (ACLMIDs.contains(msg.getUserDefinedParameter("ACLMID"))) {
                return;
            }
            if (!CIDs.contains(msg.getConversationId())) {
                CIDs.add(msg.getConversationId());
            }
            ACLMIDs.add(msg.getUserDefinedParameter("ACLMID"));
            Sequence ns = new Sequence();
            ns.sender = msg.getSender().getLocalName().trim();
            ns.conversationid = msg.getConversationId();
            ns.replywith = msg.getReplyWith();
            ns.inreplyto = msg.getInReplyTo();
            ns.receiver = ACLMessageTools.getAllReceivers(msg).trim();
            ns.content = msg.getContent() == null ? "" : msg.getContent().trim();
            ns.date = TimeHandler.Now();
            ns.performative = msg.getPerformative();
            if (msg.getReplyByDate() == null) {
                ns.replyby = null;
            } else {
                ns.replyby = new TimeHandler().fromDate(msg.getReplyByDate()).toString();
            }
            addPlayer(ns.sender);
            for (String ireceiver : ns.receiver.split(",")) {
                addPlayer(ireceiver);
            }
            sequences.add(ns);
        } else {
            Sequence ns = new Sequence();
            ns.date = TimeHandler.Now();
            ns.sender = agentName;
            ns.performative = Integer.MIN_VALUE;
            sequences.add(ns);
        }
        changes = true;
    }

    public String printCIDOrder(Sequence s) {
        return String.format("%03d", CIDs.indexOf(s.conversationid));
    }

    public void defineStyle(String style) {
        this.style = style.toUpperCase();
        switch (this.style) {
            case "SIMPLE":
                initiator = "│";
                continuer = "│";
                vert = "│";
                horizInit = "·";
                horizCont = "─";
                left = "<";
                right = ">";
                leftInitInit = "┤";
                rightInitInit = "├";
                leftInitCont = "┤";
                rightInitCont = "├";
                break;
            case "CONVERSATION":
                initiator = "█";
                continuer = "█";
                vert = "│";
                horizInit = "─";
                horizCont = "·";
                left = "<";
                right = ">";
                leftInitInit = "╡";
                rightInitInit = "╞";
                leftInitCont = "┤";
                rightInitCont = "├";
                break;

        }
    }
}

class Sequence {

    String date, sender, receiver, content, replywith, inreplyto, conversationid, replyby;
    int performative;
}

//    public void printArrow(Sequence s) {
//        int from = indexPlayer(s.sender), to = indexPlayer(s.receiver), nreceivers = 0;
//        int source, target;
//        String head = ">", tail = initiator;
//        for (String ireceiver : s.receiver.split(",")) {
//            if (nreceivers > 0 && ireceiver.startsWith("S.M.")) {
//                continue;
//            }
//            if (nreceivers > 0) {
//                nextline += "\n";
//            }
//            to = indexPlayer(ireceiver.trim());
//            if (from > to) {
//                source = to;
//                target = from;
//                if (ACLMessageTools.isInitiator(s.performative)) {
//                    if (s.inreplyto.length() == 0) {
//                        head = this.printCIDOrder(s) + leftInitInit + initiator;
//                    } else {
//                        head = this.printCIDOrder(s) + leftInitCont + initiator;
//                    }
//                } else if (s.conversationid.length() > 0) {
//                    head = this.printCIDOrder(s) + horizCont + continuer;
//                } else {
//                    head = horizCont + leftInitCont;
//                }
//                tail = vert + left;
//                for (int i = 0; i < players.size(); i++) {
//                    if (i == source) {
//                        if (s.inreplyto.length() == 0) {
//                            nextline += fillField(tail, horizInit.charAt(0),"");
//                        } else {
//                            nextline += fillField(tail, horizCont.charAt(0),"");
//                        }
//                    } else if (i == target) {
//                        nextline += fillField(head, ' ') + "  ";
//                    } else if (source < i && i < target) {
//                        if (s.inreplyto.length() == 0) {
//                            nextline += horizInit + fillField("", horizInit.charAt(0),"");
//                        } else {
//                            nextline += horizCont + fillField("", horizCont.charAt(0),"");
//                        }
//                    } else {
//                        nextline += vert + fillField("   ", ' ',"");
//                    }
//                }
//            } else {
//                source = from;
//                target = to;
//                head = right + vert;
//                if (ACLMessageTools.isInitiator(s.performative)) {
//                    if (s.inreplyto.length() == 0) {
//                        tail = initiator + rightInitInit + this.printCIDOrder(s);
//                    } else {
//                        tail = initiator + rightInitCont + this.printCIDOrder(s);
//                    }
//                } else if (s.conversationid.length() > 0) {
//                    tail = continuer + this.printCIDOrder(s);
//                } else {
//                    tail = rightInitCont + horizCont;
//                }
//                for (int i = 0; i < players.size(); i++) {
//                    if (i == source) {
//                        if (s.inreplyto.length() == 0) {
//                            nextline += fillField(tail, horizInit.charAt(0));
//                        } else {
//                            nextline += fillField(tail, horizCont.charAt(0));
//                        }
//                    } else if (i == target) {
//                        nextline += fillField(head, ' ') + "  ";
//                    } else if (source < i && i < target) {
//                        if (s.inreplyto.length() == 0) {
//                            nextline += horizInit + fillField("" + horizInit.charAt(0), horizInit.charAt(0));
//                        } else {
//                            nextline += horizCont + fillField("" + horizCont.charAt(0), horizCont.charAt(0));
//                        }
//                    } else {
//                        nextline += vert + fillField("   ", ' ');
//                    }
//                }
//
//            }
//            nreceivers++;
//        }
//    }
//    public void getSequenceDiagram(String filename, String agentname) {
//        File f = new File(filename);
//        String line, player;
//        try {
//            Scanner sf = new Scanner(f);
//            addPlayer(agentname);
//            while (sf.hasNext()) {
//                line = sf.nextLine();
//                if (line.contains("RCV;")) {
//                    player = line.split(vert)[2].split(";")[1];
//                    addPlayer(player);
//                }
//            }
//        } catch (FileNotFoundException ex) {
//        }
//    }
//    public void addSequence(String sender, String receiver, String content) {
//
//        Sequence ns = new Sequence();
//        ns.sender = sender.trim();
//        ns.receiver = receiver.trim();
//        ns.content = content == null ? "" : content.trim();
//        ns.date = TimeHandler.Now();
//        sequences.add(ns);
//        addPlayer(ns.sender);
//        addPlayer(ns.receiver);
//    }
