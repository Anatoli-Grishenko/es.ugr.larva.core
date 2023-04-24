/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package basher;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author lcv
 */
public class Basher {

    ArrayList<String> _lines;

    public Basher() {
        _lines = new ArrayList<>();
    }

    public Basher(String filename) throws FileNotFoundException, IOException {
        _lines = new ArrayList<>();
        FileInputStream fstream = new FileInputStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine;
        //Read File Line By Line
        while ((strLine = br.readLine()) != null) {
            _lines.add(strLine);
        }
        //Close the input stream
        fstream.close();

    }

    public Basher(String line, String ignore) {
        _lines = new ArrayList<>();
        _lines.add(line);
    }

    public Basher(Basher copy) {
        _lines = new ArrayList<>();
        for (String s : copy._lines) {
            _lines.add(s);
        }
    }

    public ArrayList<String> getList() {
        return _lines;
    }

    public Basher grep(String search) {
        Basher res = new Basher();
        for (String line : _lines) {
            if (line.indexOf(search) >= 0) {
                res._lines.add(line);
            }
        }
        return res;
    }

    public Basher grepr(String regexp) {
        Basher res = new Basher();
        for (String line : _lines) {
            if (line.matches(regexp)) {
                res._lines.add(line);
            }
        }
        return res;
    }

    public Basher grepv(String search) {
        Basher res = new Basher();
        for (String line : _lines) {
            if (line.indexOf(search) < 0) {
                res._lines.add(line);
            }
        }
        return res;
    }

    public Basher grepvr(String regex) {
        Basher res = new Basher();
        for (String line : _lines) {
            if (!line.matches(regex)) {
                res._lines.add(line);
            }
        }
        return res;
    }

    public Basher sed(String regexp, String replace) {
        Basher res = new Basher();
        Basher aux = new Basher(this).grepr(".*" + regexp + ".*");
        for (String line : aux._lines) {
            res._lines.add(line.replaceFirst(regexp, replace));
        }
        return res;
    }

    public Basher sedg(String regexp, String replace) {
        Basher res = new Basher();
        Basher aux = new Basher(this).grepr(".*" + regexp + ".*");
        for (String line : aux._lines) {
            res._lines.add(line.replaceAll(regexp, replace));
        }
        return res;
    }

    public int wc() {
        return _lines.size();
    }

    public Basher head(int n) {
        Basher res = new Basher();
        for (int i = 0; i < n; i++) {
            res._lines.add(_lines.get(i));
        }
        return res;
    }

    public String isolateJsonKeyString(String key) {
        return this.sed("^.*" + key + "\":\"", "").sed("\".*$", "").toString().replace("\"", "");
    }

    public String toString() {
        String res = "";
        for (String s : _lines) {
            if (res.equals("")) {
                res += s;
            } else {
                res += "\n" + s;
            }
        }
        return res;
    }
}
