/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disk;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class FileTable {

    public static enum Printer {
        TREE, /// Like bash tree
        LIST, /// like bash ls -l
        CSV, /// Like LIST bue generates a CSV for spreadaheets
        PLAIN, /// just basenames
        RAW /// Single one pass listing without any grouping
    };

    public static final String FIELD_SEP = ",";

    HashMap<String, FileRecord> fTable;
    ArrayList <FileRecord> roots;

    public FileTable() {
        fTable = new HashMap();
        roots = new ArrayList();
    }

    public void clear() {
        fTable.clear();
        roots.clear();
    }

    public boolean readFolder(String from) {
        return read(null, from, null, null, false);
    }

    public boolean readFolder(String from, ArrayList<String> exclude, ArrayList<String> include) {
        return read(null, from, exclude, include, false);
    }

    public boolean readTree(String from) {
        return read(null, from, null, null, false);
    }

    public boolean readTree(String from, ArrayList<String> exclude, ArrayList<String> include) {
        return read(null, from, exclude, include, false);
    }

    public boolean read(FileRecord parent, String from, ArrayList<String> exclude, ArrayList<String> include, boolean tree) {
        File f;
        File files[];
        FileRecord fr;
        if (exclude == null) {
            exclude = new ArrayList();
        }
        if (include == null) {
            include = new ArrayList();
        }
//        if (exclude.contains(from)) {
//            return true;
//        }
        try {
            f = new File(from);
            if (f.exists()) {
//                System.out.println(from);
                fr = new FileRecord(f);
                fr.setParent(parent);
                if (parent == null) {
                    fr.setCWD(from);
                } else {
                    fr.setCWD(parent.getCWD());
                }
                this.add(fr);
                if (f.isDirectory() && (parent == null || tree)) {
                    files = f.listFiles();
                    ArrayList<String> children = new ArrayList();
                    for (File sf : files) {
                        children.add(sf.getAbsolutePath());
                    }
                    Collections.sort(children);
                    for (String schildren : children) {
                        if (tree) {
                            if (!read(fr, schildren, exclude, include, tree)) {
                                return false;
                            }
                        } else {
                            if (!read(fr, schildren, exclude, include, false)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    public int size() {
        return fTable.size();
    }

    public long memory() {
        long res = 0;
        for (FileRecord fr : fTable.values()) {
            if (!fr.isDirectory()) {
                res += fr.getSize();
            }
        }
        return res;
    }

    public void add(FileRecord fr) {
       
        System.out.println("Added " + fr.getName()
                + "\n" + this.getSummary(true, Printer.TREE)
                + "\n" + this.getSummary(true, Printer.LIST)
        );
    fTable.put(fr.getName(), fr);
 }

    public FileTable getDirectories() {
        FileTable res = new FileTable();
        for (FileRecord fr : fTable.values()) {
            if (fr.isDirectory()) {
                res.add(fr);
            }
        }
        return res;
    }

    public FileTable getFiles() {
        FileTable res = new FileTable();
        for (FileRecord fr : fTable.values()) {
            if (fr.isFile()) {
                res.add(fr);
            }
        }
        return res;
    }

    public FileTable getFilesbyDirectory(String directory) {
        FileTable res = new FileTable();
        if (directory.endsWith(".")) {
            directory = directory.substring(0, directory.length() - 1);
        }
//        if (!directory.endsWith("/")) {
//            directory += "/";
//        }
        for (FileRecord fr : this.ToArrayList()) {
            if (fr.isFile() && fr.getPath().equals(directory)) {
                res.add(fr);
            }
        }
        return res;
    }

    public ArrayList<FileRecord> ToArrayList() {
        ArrayList<FileRecord> res = new ArrayList();
        ArrayList<String> names = new ArrayList(fTable.keySet());
        Collections.sort(names);
        for (String n : names) {
            res.add(fTable.get(n));
        }
        return res;
    }

    public FileRecord findFileRecord(String other) {
        FileRecord res = null;
        if (fTable.keySet().contains(other)) {
            res = fTable.get(other);
        }
        return res;
    }

    public FileRecord findFileRecord(FileRecord other, boolean absolute) {
        FileRecord res = null;
        for (FileRecord fthis : fTable.values()) {
            if (absolute) {
                if (fthis.getName().equals(other.getName())) {
                    res = fTable.get(fthis.getName());
                    break;
                }
            } else {
                if (fthis.getBaseName().equals(other.getBaseName()) && fthis.getRelativePath().equals(other.getRelativePath())) {
                    res = fTable.get(fthis.getName());
                    break;
                }
            }
        }
        return res;
    }

    public FileTable findFileRecordRegex(String regexp) {
        FileTable res = null;
        for (String fname : fTable.keySet()) {
            if (fname.matches(regexp)) {
                if (res == null) {
                    res = new FileTable();
                }
                res.add(fTable.get(fname));
            }
        }
        return res;
    }

    public FileTable getIntersection(FileTable other) {
        FileTable res = null;
        for (FileRecord fr : fTable.values()) {
            if (other.findFileRecord(fr, false) != null) {
                if (res == null) {
                    res = new FileTable();
                }
                res.add(fr);
            }
        }
        return res;
    }

    public FileTable getUnion(FileTable other) {
        FileTable res = new FileTable();

        for (FileRecord fr : fTable.values()) {
            res.add(fr);
        }
        for (FileRecord fr : other.ToArrayList()) {
            if (this.findFileRecord(fr, false) == null) {
                res.add(fr);
            }
        }
        return res;
    }

    public FileTable getUpdatableRecords(FileTable other) {
        FileTable res = new FileTable(), ftAux;
        if (other == null) {
            return res;
        }
        ftAux = this.getIntersection(other);
        if (ftAux != null) {
            for (FileRecord fr : ftAux.ToArrayList()) {
                if (fr.isNewerThan(other.findFileRecord(fr, false))) {
                    res.add(fr);
                }
            }
        }
        return res;
    }

    public FileTable getNewerRecords(FileTable other) {
        FileTable res = new FileTable();
        for (FileRecord fr : fTable.values()) {
            if (other.findFileRecord(fr, false) == null) {
                if (res == null) {
                    res = new FileTable();
                }
                res.add(fr);
            }
        }
        return res;
    }

    public String getSummary(boolean addFiles, Printer printer) {
        // "┌┐└┘─│┬┴┼┤├
        String summaryBody = "", elbow = "├─────", elbow2 = "└─────";
        FileTable ftByDirectory;
        for (FileRecord fr : ToArrayList()) {
            if (fr.isDirectory() && addFiles) {
                if (fr.getParent() == null) {
                    if (printer == Printer.TREE) {
                        summaryBody += getIndentation(fr.getRelativeDepth()) + elbow + fr.getName() + "\n"; // " + ftByDirectory.size() + " files\n";
                    } else {
                        summaryBody += fr.toString(printer) + "\n";
                    }
                } else {
                    if (printer == Printer.TREE) {
                        summaryBody += getIndentation(fr.getRelativeDepth()) + elbow + fr.getBaseName() + "\t(" + FileRecord.getMemory(fr.getSize(), FileRecord.Sizes.Bytes) + ")\n";
                    } else {
                        summaryBody += fr.toString(printer) + "\n";
                    }
                }
                ftByDirectory = getFilesbyDirectory(fr.getName());
                for (FileRecord frf : ftByDirectory.ToArrayList()) {
                    if (printer == Printer.TREE) {
                        summaryBody += getIndentation(frf.getRelativeDepth()) + elbow + frf.getBaseName() + "\t(" + FileRecord.getMemory(frf.getSize(), FileRecord.Sizes.Bytes) + ")\n";
                    } else {
                        summaryBody += frf.toString(printer) + "\n";
                    }
                }
            }
        }
        return summaryBody;
    }

    public String printTree() {
        String res="";
        return res;
    }
            
    public static String getIndentation(int depth) {
        String sep = "\t", res = "";
        for (int i = 0; i < depth; i++) {
            res += "│" + sep;
        }
        return res;
    }

    @Override
    public String toString() {
        return getSummary(true, Printer.LIST);
    }

    public String toString(Printer printer) {
        return getSummary(true, printer);
    }

}
