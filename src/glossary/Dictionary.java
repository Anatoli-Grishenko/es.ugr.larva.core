/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glossary;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Dictionary {
    public static final int ALL=Integer.MAX_VALUE;
    protected HashMap<String, Dictionary> lexicon;
    protected final String delim = ".";
    protected String root;

    public Dictionary() {
        lexicon = new HashMap();
        root = "";
    }

    public Dictionary(String root) {
        lexicon = new HashMap();
        this.root = root.toUpperCase();
    }

    public void load(String filename) {
        try {
            Scanner s = new Scanner(new File(filename));
            String line;
            while (s.hasNext()) {
                line = s.nextLine();
                this.addWord(line);
            }
        } catch (Exception ex) {

        }
    }

    public void addWord(String word) {
        if (word.length() == 0) {
            lexicon.put(delim, new Dictionary(this.root));
            return;
        }
        word = word.toUpperCase();
        String first = word.substring(0, 1),
                rest = (word.length() > 1 ? word.substring(1, word.length()) : "");
        if (lexicon.get(first) == null) {
            lexicon.put(first, new Dictionary(this.root + first));
        }
        lexicon.get(first).addWord(rest);
    }

    public boolean findWord(String word) {
        if (word.length() == 0) {
            if (lexicon.get(delim) != null) {
                return true;
            } else {
                return false;
            }
        }
        word = word.toUpperCase();
        String first = word.substring(0, 1),
                rest = (word.length() > 1 ? word.substring(1, word.length()) : "");
        if (lexicon.get(first) != null) {
            return lexicon.get(first).findWord(rest);
        } else {
            return false;
        }
    }

    public ArrayList<String> completeWord(String word, int max) {
        if (word.length() == 0) {
            return this.preOrder(max);
        }
        word = word.toUpperCase();
        String first = word.substring(0, 1),
                rest = (word.length() > 1 ? word.substring(1, word.length()) : "");
        if (lexicon.get(first) != null) {
            return lexicon.get(first).completeWord(rest, max);
        } else {
            return new ArrayList();
        }
    }

    public String toString() {
        return preOrderS("", "");
    }

    public ArrayList<String> preOrder(int max) {
        if (max < 0)
            return new ArrayList();
        ArrayList<String> keys = new ArrayList(lexicon.keySet()), res = new ArrayList();
        Collections.sort(keys);
        for (String sk : keys) {
            if (lexicon.get(sk) != null) {
                if (sk.equals(delim)) {
                    res.add(this.root);
                    max--;
                } else {
                    res.addAll(lexicon.get(sk).preOrder(max-res.size()));
                }
            }
        }
        return res;
    }

    public String preOrderS(String res, String current) {
        ArrayList<String> keys = new ArrayList(lexicon.keySet());
        Collections.sort(keys);
        for (String sk : keys) {
            if (lexicon.get(sk) != null) {
                if (sk.equals(delim)) {
                    res += "\n" + current;
                } else {
                    res = lexicon.get(sk).preOrderS(res, current + sk);
                }
            }
        }
        return res;
    }
}
