/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swing;

import appboot.LARVABoot;
import data.OleFile;
import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import javax.swing.ImageIcon;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class OleIconSet {

    protected HashMap<String, ImageIcon> regular, highlight, inactive;

    public OleIconSet(String lookandfeel) {
        regular = new HashMap();
        highlight = new HashMap();
        inactive = new HashMap();
        switch (lookandfeel) {
            case "Dark":
                loadFolder(regular, "white");
                loadFolder(highlight, "blue");
                loadFolder(inactive, "gray");
                break;
            case "Light":
            default:
                loadFolder(regular, "black");
                loadFolder(highlight, "blue");
                loadFolder(inactive, "gray");
                break;
        }
    }

    protected void loadFolder(HashMap<String, ImageIcon> m, String folder) {
        File folderinputs[], item, dir;
        dir = new File(getClass().getResource("/resources/icons/" + folder + "/").toString().replace("file:", ""));
        folderinputs = dir.listFiles();
        for (File f : folderinputs) {
            String name = f.getName(), resource;
            resource = getClass().getResource("/resources/icons/" + folder + "/" + name).toString().replace("file:", "");
            m.put(f.getName().replace(".png", ""), new ImageIcon(resource));
        }
    }

    public HashMap<String, ImageIcon> getRegular() {
        return regular;
    }

    public HashMap<String, ImageIcon> getHighlight() {
        return highlight;
    }

    public HashMap<String, ImageIcon> getInactive() {
        return inactive;
    }

    public ImageIcon getRegularIcon(String iconName, int w, int h) {
        if (regular.keySet().contains(iconName)) {
            return new ImageIcon(regular.get(iconName).getImage().getScaledInstance(w,h, Image.SCALE_SMOOTH));
        } else
            return null;
    }
    public ImageIcon getHighlightIcon(String iconName, int w, int h) {
        if (highlight.keySet().contains(iconName)) {
            return new ImageIcon(highlight.get(iconName).getImage().getScaledInstance(w,h, Image.SCALE_SMOOTH));
        } else
            return null;
    }
    
    public ImageIcon getInactiveIcon(String iconName, int w, int h) {
        if (inactive.keySet().contains(iconName)) {
            return new ImageIcon(inactive.get(iconName).getImage().getScaledInstance(w,h, Image.SCALE_SMOOTH));
        } else
            return null;
    }
    public ImageIcon getRegularIcon(String iconName, Dimension d) {
        if (regular.keySet().contains(iconName)) {
            return new ImageIcon(regular.get(iconName).getImage().getScaledInstance((int)d.getWidth(),(int) d.getHeight(), Image.SCALE_SMOOTH));
        } else
            return null;
    }
    public ImageIcon getHighlightIcon(String iconName, Dimension d) {
        if (highlight.keySet().contains(iconName)) {
            return new ImageIcon(highlight.get(iconName).getImage().getScaledInstance((int)d.getWidth(),(int) d.getHeight(), Image.SCALE_SMOOTH));
        } else
            return null;
    }
    
    public ImageIcon getInactiveIcon(String iconName, Dimension d) {
        if (inactive.keySet().contains(iconName)) {
            return new ImageIcon(inactive.get(iconName).getImage().getScaledInstance((int)d.getWidth(),(int) d.getHeight(), Image.SCALE_SMOOTH));
        } else
            return null;
    }
}
