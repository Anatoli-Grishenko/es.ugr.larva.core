/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import map2D.Map2DColor;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleDot extends Ole {

    int pppResolution = 150, width = 16, height = 11;
    boolean fill = false;

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

    public boolean isFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public static void exportTo(String dotFile, String layoutmanager, String type) {
        String exportFile = dotFile.replace("dot/", "export/").replace(".dot", "." + type);
        String tmpFile = dotFile.replace("dot/", "export/").replace(".mod", ".tmp");
        File f = new File(exportFile);
        if (f.exists()) {
            f.delete();
        }
        String command;
        ProcessBuilder pb;
        Process ps = null;
        try {
            command = "/usr/bin/" + layoutmanager + " -T" + type + " " + FileSystems.getDefault().getPath(dotFile).normalize().toAbsolutePath().toString()
                    + "  -o " + FileSystems.getDefault().getPath(exportFile).normalize().toAbsolutePath().toString();
            pb = new ProcessBuilder().command(command.split(" "));
            pb.directory(FileSystems.getDefault().getPath("./").normalize().toAbsolutePath().toFile());
            ps = pb.start();
            ps.waitFor(5000, TimeUnit.MILLISECONDS);

        } catch (Exception ex) {
            System.err.println("Error generating output to " + exportFile + "\n" + ex.toString());
        }
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
                    + "dpi=" + pppResolution + "\n"
                    + (isFill() ? "ratio=\"fill\";\n" : "")
                    + " size=\"" + getWidth() + "," + getHeight() + "!\";\n"
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
                                + "<FONT face=\"" + oleformat.getString("face", "Arial") + "\" POINT-SIZE=\"" + oleformat.getInt("fontsize", 10) + "\"><b>" + classname + "</b></FONT></td></tr>\n";
//                                + "<FONT face=\"" + oleformat.getString("face", "Arial") + "\" POINT-SIZE= \"" + oleformat.getInt("fontsize", 10) + "\"><b>" + classname + "</b></FONT></td></tr>\n";
                        classmethod = "";
                        if (getOle("publicmethods").get(classname) != null) {           // [+]<i>" + methodname + "</i>
                            classmethods = new ArrayList<String>(getOle("publicmethods").getArray(classname));
                            int i = 0;
                            Collections.sort(classmethods);
                            node += "<tr><td><table border=\"0\" cellborder=\"0\" cellspacing=\"0\">";
                            for (String methodname : classmethods) {
                                if (methodname.length() < 5) {
                                    continue;
                                }
                                System.out.print("[+]"+methodname + "---");
                                methodname = methodname.replace("<", "&lt;");
                                methodname = methodname.replace(">", "&gt;");
                                System.out.print(methodname + "---");
                                methodname = highlight(methodname);
                                System.out.println(methodname);
                                node += "<tr><td align=\"left\">"
                                        + "<FONT face=\"" + oleformat.getString("face", "Courier New") + "\" "
                                        + "POINT-SIZE= \"" + oleformat.getInt("fontsize", 18) * 3 / 4 + "\">"
                                        + "[+]<i>" + methodname + "</i></FONT></td></tr>\n";
                            }
                            node += "</table></td></tr>\n";
                        }
                        if (getOle("protectedmethods").get(classname) != null) {
                            classmethods = new ArrayList<String>(getOle("protectedmethods").getArray(classname));
                            int i = 0;
                            Collections.sort(classmethods);
                            node += "<tr><td><table border=\"0\" cellborder=\"0\" cellspacing=\"0\">";
                            for (String methodname : classmethods) {
                                if (methodname.length() < 5) {
                                    continue;
                                }
                                System.out.print("(-)"+methodname + "---");
                                methodname = methodname.replace("<", "&lt;");
                                methodname = methodname.replace(">", "&gt;");
                                System.out.print(methodname + "---");
                                methodname = highlight(methodname);
                                System.out.println(methodname);
                                node += "<tr><td align=\"left\">" + "<FONT face=\"" + oleformat.getString("face", "Courier New") + "\" POINT-SIZE= \"" + oleformat.getInt("fontsize", 18) * 3 / 4 + "\">(-)<i>" + methodname + "</i></FONT>" + "</td></tr>\n";
                            }
                            node += "<tr><td> </td></tr></table></td></tr>\n";
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
//    public void toDot(String dotfilename) {
//        ArrayList<String> classtypes, classnames, relationnames, children, classmethods;
//        ArrayList<Ole> relations;
//        Ole oleformat;
//        String parent, auxformat, classtypeformat, classmethod, relationformat, node;
//        PrintStream out;
//        try {
//            out = new PrintStream(new File(dotfilename));
//            out.println("digraph graphname {");
//            //out.println("{ rank = sink ; }");
//            out.println("     rankdir=\"BT\"\n"
//                    + "dpi=" + pppResolution + "\n"
//                    + (isFill()? "ratio=\"fill\";\n" :"")
//                    + " size=\""+getWidth()+","+getHeight()+"!\";\n"
//                    + " margin=0;");
//            classtypes = new ArrayList<String>(getArray("classtypes"));
////            System.out.println("CLASS TYPES: " + classtypes);
//            // Generate classes
//            for (String classtype : classtypes) {
//                oleformat = getOle("format").getOle(classtype);
//                if (getOle("class").getFieldList().contains(classtype)) {
//                    classnames = new ArrayList<String>(getOle("class").getArray(classtype));
////                    System.out.println(classtype + ":" + classnames);
//                    for (String classname : classnames) {
//                        node = classname + " " + "[shape=plain, label=<"
//                                + "<table border=\"0\" cellborder=\"1\" cellspacing=\"0\"><tr><td bgcolor=\"" + oleformat.getString("fillcolor", "white") + "\">"
//                                + "<FONT face=\"" + oleformat.getString("face", "Arial") + "\" POINT-SIZE=\"" + oleformat.getInt("fontsize", 10) + "\"><b>" + classname + "</b></FONT></td></tr>\n";
////                                + "<FONT face=\"" + oleformat.getString("face", "Arial") + "\" POINT-SIZE= \"" + oleformat.getInt("fontsize", 10) + "\"><b>" + classname + "</b></FONT></td></tr>\n";
//                        classmethod = "";
//                        if (getOle("publicmethods").get(classname) != null) {
//                            classmethods = new ArrayList<String>(getOle("publicmethods").getArray(classname));
//                            int i = 0;
//                            Collections.sort(classmethods);
//                            for (String methodname : classmethods) {
//                                methodname = methodname.replace("<", "&lt;");
//                                methodname = methodname.replace(">", "&gt;");
//                                methodname = highlight(methodname);
//                                node += "<tr><td align=\"left\">"+"<FONT face=\"" + oleformat.getString("face", "Courier New") + "\" POINT-SIZE= \"" + oleformat.getInt("fontsize", 18) * 3 / 4 + "\">[+]<i>" + methodname + "</i></FONT>" + "</td></tr>\n";
//                            }
//                        }
//                        if (getOle("protectedmethods").get(classname) != null) {
//                            classmethods = new ArrayList<String>(getOle("protectedmethods").getArray(classname));
//                            int i = 0;
//                            Collections.sort(classmethods);
//                            for (String methodname : classmethods) {
//                                methodname = methodname.replace("<", "&lt;");
//                                methodname = methodname.replace(">", "&gt;");
//                                methodname = highlight(methodname);
//                                node += "<tr><td align=\"left\">"+"<FONT face=\"" + oleformat.getString("face", "Courier New") + "\" POINT-SIZE= \"" + oleformat.getInt("fontsize", 18) * 3 / 4 + "\">(-)<i>" + methodname + "</i></FONT>" + "</td></tr>\n";
//                            }
//                        }
//                        node += "</table>>]";
//                        out.println(classname + " " + node);
//                    }
//                }
//            }
//            // Generate extends
//            if (get("relationtypes") != null) {
//                relationnames = new ArrayList<String>(getArray("relationtypes"));
//                for (String relationname : relationnames) {
//                    relationformat = getOle("format").getString(relationname, "");
//                    if (getOle("relation").getFieldList().contains(relationname)) {
//                        relations = new ArrayList<Ole>(getOle("relation").getArray(relationname));
//                        for (Ole orelation : relations) {
//                            parent = orelation.getFieldList().get(0);
//                            children = new ArrayList<String>(orelation.getArray(parent));
//                            for (String child : children) {
//                                out.println(child + " -> " + parent + " " + relationformat);
//                            }
//                        }
//                    }
//                }
//            }
//            out.println("}");
//            out.close();
//        } catch (FileNotFoundException ex) {
//        }
//    }

    public void recursiveTree(Ole classname, PrintStream output) {
        String parent, child;
        ArrayList<Ole> children;
        parent = classname.getFieldList().get(0);
        children = new ArrayList<Ole>(classname.getArray(parent));
    }

    public static String highlight(String s) {
        String methodname = "";
        boolean bdo = false;
        for (int i = s.length() - 1; i >= 0;) {
            if (bdo) {
                methodname = "" + s.charAt(i) + methodname;
                i--;
                if (i==0 || s.charAt(i) == ' ') {
                    break;
                }
            } else {
                if (s.charAt(i) == '(') {
                    bdo = true;
                    i--;
                    while (s.charAt(i) == ' ' && i > 0) {
                        i--;
                    }
                } else {
                    i--;
                }
            }
        }
        methodname = methodname.trim();
        return s.replace(methodname, "<b>" + methodname + "</b>");
    }
}
