/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disk;

import basher.Basher;
import static disk.FileTable.FIELD_SEP;
import disk.FileTable.Printer;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class FileRecord {

    public static enum Sizes {
        Bytes, KBytes, MBytes, GBytes, TBytes
    }
    boolean directory, allExecute, allWrite, allRead,
            groupExecute, groupWrite, groupRead,
            userExecute, userWrite, userRead;
    int privileges;
    int size;
    TimeHandler date;
    String name, cwd = "";
    FileRecord parent = null;
    ArrayList<FileRecord> children = new ArrayList();
    int nChild = 0;

    public FileRecord() {

    }

    public FileRecord(File f) {
        Path path = Paths.get(f.getAbsolutePath());
        setName(f.getAbsolutePath());
        setDirectory(f.isDirectory());
        try {
            setSize((int) Files.size(path));
        } catch (Exception ex) {
            setSize(0);
        }
        setDate(new TimeHandler());
    }

    public boolean isDirectory() {
        return directory;
    }

    public boolean isFile() {
        return !directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public boolean isAllExecute() {
        return allExecute;
    }

    public void setAllExecute(boolean allExecute) {
        this.allExecute = allExecute;
    }

    public boolean isAllWrite() {
        return allWrite;
    }

    public void setAllWrite(boolean allWrite) {
        this.allWrite = allWrite;
    }

    public boolean isAllRead() {
        return allRead;
    }

    public void setAllRead(boolean allRead) {
        this.allRead = allRead;
    }

    public boolean isGroupExecute() {
        return groupExecute;
    }

    public void setGroupExecute(boolean groupExecute) {
        this.groupExecute = groupExecute;
    }

    public boolean isGroupWrite() {
        return groupWrite;
    }

    public void setGroupWrite(boolean groupWrite) {
        this.groupWrite = groupWrite;
    }

    public boolean isGroupRead() {
        return groupRead;
    }

    public void setGroupRead(boolean groupRead) {
        this.groupRead = groupRead;
    }

    public boolean isUserExecute() {
        return userExecute;
    }

    public void setUserExecute(boolean userExecute) {
        this.userExecute = userExecute;
    }

    public boolean isUserWrite() {
        return userWrite;
    }

    public void setUserWrite(boolean userWrite) {
        this.userWrite = userWrite;
    }

    public boolean isUserRead() {
        return userRead;
    }

    public void setUserRead(boolean userRead) {
        this.userRead = userRead;
    }

    public int getPrivileges() {
        return privileges;
    }

    public void setPrivileges(int privileges) {
        this.privileges = privileges;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public TimeHandler getDate() {
        return date;
    }

    public void setDate(TimeHandler date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getBaseName() {
        return getFolder(this.getDepth());
    }

    public String getPath() {
        return getName().replace("/" + getBaseName(), "");
    }

    public String getRelativePath() {
        return getName().replace(getCWD(), "");
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileRecord getParent() {
        return parent;
    }

    public void setParent(FileRecord parent) {
        this.parent = parent;
        if (parent != null) {
            if (parent.getChildren().isEmpty()) {
                this.setnChild(0);
            } else {
                this.setnChild(parent.getChildren().get(parent.getChildren().size() - 1).getnChild() + 1);
            }
            parent.addChild(this);
        }

    }

    public ArrayList<FileRecord> getChildren() {
        return children;
    }

    public void addChild(FileRecord fr) {
        children.add(fr);
    }

    public int getnChild() {
        return nChild;
    }

    public void setnChild(int nChild) {
        this.nChild = nChild;
    }

    public int getDepth() {
        int count = 0, pos = 0;
        while (getName().indexOf("/", pos) >= 0 && pos >= 0) {
            count++;
            pos = getName().indexOf("/", pos + 1);
        }
        return count;
    }

    public int getRootDepth() {
        if (getParent() == null) {
            return getDepth();
        } else {
            return getParent().getRootDepth();
        }
    }

    public int getRelativeDepth() {
        return getDepth() - getRootDepth();
    }

    public String getFolder() {
        return getFolder(getDepth() - 1);
    }

    public String getFolder(int level) {
        String res = null;
        int count = 0, pos = 0;
        if (level > getDepth() || level < 0) {
            return res;
        }
        String folders[] = getName().split("/");
        return folders[level];

    }

    public boolean isNewerThan(FileRecord other) {
        boolean res = false;
        if (other != null) {
            res = getDate().isBeforeEq(other.getDate());
        }
        return res;
    }

    public static String getMemory(long size, Sizes s) {
        Sizes[] list = Sizes.values();
        if (size < 1024 || s == list[list.length - 1]) {
            return "" + size + " " + s.name();
        } else {
            Sizes nextSize;
            int i;
            for (i = 0; i < list.length; i++) {
                if (list[i] == s) {
                    break;
                }
            }
            nextSize = list[i + 1];
            return getMemory(size / 1024, nextSize);
        }
    }

    public String getCWD() {
        return cwd;
    }

    public void setCWD(String basePath) {
        this.cwd = basePath;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String toString(Printer printer) {
        if (printer == Printer.TREE) {
            return getName() + "\t" + FileRecord.getMemory(getSize(), FileRecord.Sizes.Bytes);
        } else if (printer == Printer.CSV) {
            return "" + isDirectory() + FIELD_SEP + getName() + FIELD_SEP + FileRecord.getMemory(getSize(), FileRecord.Sizes.Bytes) + FIELD_SEP + getDate();
        } else if (printer == Printer.LIST) {
            return "" + (isDirectory() ? "d" : "-") + "\t" + FileRecord.getMemory(getSize(), FileRecord.Sizes.Bytes) + "\t" + getDate() + "\t" + getName();
        } else if (printer == Printer.PLAIN) {
            return "" + getName();
        } else {
            return getName();
        }
    }
}
