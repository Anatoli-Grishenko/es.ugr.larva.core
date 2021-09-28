/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import static disk.Logger.trimString;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class SequenceDiagram {

    ArrayList<String> players;
    ArrayList<Sequence> sequences;
    int width = 35;
    String blackboard = "", template = "%-" + width + "s", nextline = "";

    public SequenceDiagram() {
        players = new ArrayList();
        sequences = new ArrayList();
    }

    public int size() {
        return sequences.size();
    }
    
    public int getNPlayers() {
        return players.size();
    }

    public void addPlayer(String name) {
        if (!players.contains(name)) {
            players.add(name);
        }
    }

    public String getPlayer(int i) {
        if (0 <= i && i < size()) {
            return players.get(i);
        } else {
            return "";
        }
    }

    public int indexPlayer(String name) {
        if (players.contains(name)) {
            return players.indexOf(name);
        } else {
            return -1;
        }
    }

    public void nextLine() {
        blackboard += nextline + "\n";
        nextline = "";
    }

    public void printPlayers() {
        for (int i = 0; i < players.size(); i++) {
            nextline += "|" + String.format(template, fillField(players.get(i), ' '));
        }
    }

    public String fillField(String base, char fill) {
        String res = "";

        for (int i = 0; i < width; i++) {
            if (i < base.length()) {
                res += "" + base.charAt(i);
            } else {
                res += "" + fill;
            }
        }
        return res;
    }

    public void printEmptyPlayers() {
        for (int i = 0; i < players.size(); i++) {
            nextline += "|" + String.format(template, fillField("", ' '));
        }
    }

    public void printDate(Sequence s) {
        for (int i = 0; i < players.size(); i++) {
            if (i != indexPlayer(s.sender)) {
                nextline += "|" + String.format(template, fillField("", ' '));
            } else {
                nextline += "|" + String.format(template, fillField(s.date, ' '));
            }
        }
    }
    public void printContent(Sequence s) {
        for (int i = 0; i < players.size(); i++) {
            if (i != indexPlayer(s.sender)) {
                nextline += "|" + String.format(template, fillField("", ' '));
            } else {
                nextline += "|" + String.format(template, fillField(s.content, ' '));
            }
        }
    }

    public void printArrow(Sequence s) {
        int from = indexPlayer(s.sender), to = indexPlayer(s.receiver);
        String head = ">", tail = "*";
        if (from > to) {
            int aux = from;
            from = to;
            to = aux;
            head = "*";
            tail = "<";
        }
        for (int i = 0; i < players.size(); i++) {
            if (i == from) {
                nextline += fillField(tail, '-')+"-";
            } else if (i == to) {
                nextline += fillField(head, ' ')+' ';
            } else if (from < i && i < to) {
                nextline += "-"+fillField("", '-');
            } else {
                nextline += "|" + fillField("", ' ');
            }
        }
    }

    public void getSequenceDiagram(String filename, String agentname) {
        File f = new File(filename);
        String line, player;
        try {
            Scanner sf = new Scanner(f);
            addPlayer(agentname);
            while (sf.hasNext()) {
                line = sf.nextLine();
                if (line.contains("RCV;")) {
                    player = line.split("|")[2].split(";")[1];
                    addPlayer(player);
                }
            }
        } catch (FileNotFoundException ex) {
        }
    }

    public void addSequence(String sender, String receiver, String content) {

        Sequence ns = new Sequence();
        ns.sender = sender.trim();
        ns.receiver = receiver.trim();
        ns.content = content == null ? "" : content.trim();
        ns.date = TimeHandler.Now();
        sequences.add(ns);
        addPlayer(ns.sender);
        addPlayer(ns.receiver);
    }

    public String printSequenceDiagram() {
        this.blackboard = "\n";
        this.nextline = "";

        printPlayers();
        nextLine();
        printEmptyPlayers();
        nextLine();
        for (int i = 0; i < size(); i++) {
            this.printDate(sequences.get(i));
            nextLine();
            this.printContent(sequences.get(i));
            nextLine();
            this.printArrow(sequences.get(i));
            nextLine();
            this.printEmptyPlayers();
            nextLine();
        }
        return this.blackboard;
    }
}

class Sequence {

    String date, sender, receiver, content;
}
