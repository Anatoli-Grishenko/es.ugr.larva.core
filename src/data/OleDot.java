/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleDot extends Ole {

    int pppResolution=150;
    
    public OleDot() {
        super();
        setType(oletype.OLEDOT.name());
        addField("class");
        addField("relation");
    }

    public OleDot(Ole o) {
        super(o);
        setType(oletype.OLEDOT.name());
        addField("class");
        addField("relation");
    }

    public void setResolution(int ppp) {
        pppResolution = ppp;
    }
    
    public void toDot(String dotfilename) {
        ArrayList<String> classtypes, classnames, relationnames, children, classmethods;
        ArrayList<Ole> relations;
        Ole oleformat;
        String parent, auxformat, classtypeformat, classmethod, relationformat, node;
        PrintStream out;
        try {
            out = new PrintStream(new File(dotfilename));
            out.println("digraph graphname {");
            //out.println("{ rank = sink ; }");
            out.println("     rankdir=\"BT\"\n"
                    + "dpi="+pppResolution+"\n"
//                    + "ratio=\"fill\";\n" 
                    + " size=\"16,11!\";\n"
                    + " margin=0;");
            classtypes = new ArrayList<String>(getArray("classtypes"));
//            System.out.println("CLASS TYPES: " + classtypes);
            // Generate classes
            for (String classtype : classtypes) {
                oleformat = getOle("format").getOle(classtype);
                if (getOle("class").getFieldList().contains(classtype)) {
                    classnames = new ArrayList<String>(getOle("class").getArray(classtype));
//                    System.out.println(classtype + ":" + classnames);
                    for (String classname : classnames) {
                        node = classname + " " + "[shape=plain, label=<"
                                + "<table border=\"0\" cellborder=\"1\" cellspacing=\"0\"><tr><td bgcolor=\"" + oleformat.getString("fillcolor", "white") + "\">"
                                + "<FONT face=\"" + oleformat.getString("face", "Arial") + "\" POINT-SIZE= \"" + oleformat.getInt("fontsize", 10) + "\"><b>" + classname + "</b></FONT></td></tr>\n";
                        classmethod = "";
                        if (getOle("methods").get(classname) != null) {
                            classmethods = new ArrayList<String>(getOle("methods").getArray(classname));
                            int i = 0;
                            for (String methodname : classmethods) {
                                methodname = methodname.replace("<", "&lt;");
                                methodname = methodname.replace(">", "&gt;");
                                node += "<tr><td align=\"left\">" + "<FONT face=\"" + oleformat.getString("face", "Courier New") + "\" POINT-SIZE= \"" + oleformat.getInt("fontsize", 18) * 3 / 4 + "\"><i>" + methodname + "</i></FONT>" + "</td></tr>\n";
                            }
                        }
                        node += "</table>>]";
                        out.println(classname + " " + node);
                    }
                }
            }
            // Generate extends
            if (get("relationtypes") != null) {
                relationnames = new ArrayList<String>(getArray("relationtypes"));
                for (String relationname : relationnames) {
                    relationformat = getOle("format").getString(relationname, "");
                    if (getOle("relation").getFieldList().contains(relationname)) {
                        relations = new ArrayList<Ole>(getOle("relation").getArray(relationname));
                        for (Ole orelation : relations) {
                            parent = orelation.getFieldList().get(0);
                            children = new ArrayList<String>(orelation.getArray(parent));
                            for (String child : children) {
                                out.println(child + " -> " + parent + " " + relationformat);
                            }
                        }
                    }
                }
            }
            out.println("}");
            out.close();
        } catch (FileNotFoundException ex) {
        }
    }

    public void recursiveTree(Ole classname, PrintStream output) {
        String parent, child;
        ArrayList<Ole> children;
        parent = classname.getFieldList().get(0);
        children = new ArrayList<Ole>(classname.getArray(parent));
    }
}
