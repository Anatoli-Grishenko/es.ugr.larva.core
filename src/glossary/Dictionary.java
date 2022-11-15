/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glossary;

import crypto.Keygen;
import static crypto.Keygen.getWordo;
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
    
    public static final int ALL = Integer.MAX_VALUE;
    protected HashMap<String, Dictionary> lexicon;
    protected final String delim = ".";
    protected String root, name;
    protected long nWords = 0;
    
    public Dictionary() {
        lexicon = new HashMap();
        root = "";
    }
    
    public Dictionary(String root) {
        lexicon = new HashMap();
        this.root = root.toUpperCase();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public long getnWords() {
        return nWords;
    }
    
    public void setnWords(long nWords) {
        this.nWords = nWords;
    }
    
    public void load(String filename) {
        try {
            name = filename;
            Scanner s = new Scanner(new File(filename));
            String line;
            while (s.hasNext()) {
                line = s.nextLine();
                this.addWord(line);
                nWords++;
            }
            System.out.println("Word count:" + nWords);
        } catch (Exception ex) {
            
        }
    }
    
    public String findFirstWord() {
        String wordo;
        ArrayList<String> words;
        do {
            wordo = Keygen.getAlphaKey(2);
            words = completeWord(wordo, 25);
        } while (words.size() == 0);
        return words.get((int) (Math.random() * words.size()));
    }
    
    public String findNextWord(String word) {
        ArrayList<String> words = findNextWords(word, 100);
        if (words.size() > 0) {
            return words.get((int) (Math.random() * words.size()));
        } else {
            return null;
        }

//        ArrayList<String> words;
//        String next;
//        int n = word.length();
//        do {
//            try {
//                words = completeWord(word.substring(word.length() - n), 10);
//                if (words.size() > 1) {
//                    do {
//                        next = words.get((int) (Math.random() * words.size()));
//                    } while (next.equals(word));
//                    return next;
//                }
//            } catch (Exception ex) {
//
//            }
//            n--;
//            if (n == 0) {
//                return null;
//            }
//        } while (true);
    }
    
    public ArrayList<String> findNextWords(String word, int many) {
        ArrayList<String> res = new ArrayList(), words = new ArrayList();
        String next;
        int n = word.length();
        do {
            try {
                words = completeWord(word.substring(word.length() - n), 10);
                if (words.size() > 1) {
                    do {
                        next = words.get((int) (Math.random() * words.size()));
                    } while (next.equals(word));
                    res.add(next);
                    if (res.size() == many) {
                        Collections.shuffle(res);
                        return res;
                    }
                }
            } catch (Exception ex) {
                
            }
            n--;
            if (n == 0) {
                return res;
            }
        } while (true);
    }
    
    public int checkWords(String prev, String next) {
        prev = prev.toUpperCase();
        next=next.toUpperCase();
        int res = -1, max = (int) (Math.min(prev.length(), next.length()));
        if (findWord(prev) && findWord(next)) {
            for (int i = 0; i < max; i++) {
                if (prev.endsWith(next.substring(0, i))) {
                    res = i;
                }
            }
        }
        return res;
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
        if (max < 0) {
            return new ArrayList();
        }
        ArrayList<String> keys = new ArrayList(lexicon.keySet()), res = new ArrayList();
        Collections.sort(keys);
        for (String sk : keys) {
            if (lexicon.get(sk) != null) {
                if (sk.equals(delim)) {
                    res.add(this.root);
                    max--;
                } else {
                    res.addAll(lexicon.get(sk).preOrder(max - res.size()));
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
