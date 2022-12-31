/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.io.File;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public interface OleTools {

    public static void bootConfig(AutoOle ol) {
        if (new File(ol.getOptionsFolder() + ol.getOptionsFile()).exists()) {
            loadConfig(ol);
        } else {
            saveConfig(ol);
        }
    }

    public static boolean loadConfig(AutoOle ol) {
        OleConfig ocfg;
        ocfg = new OleConfig();
        ocfg.loadFile(ol.getOptionsFolder() + ol.getOptionsFile());
        Ole.fromOle2(ocfg, ol);
        return true;
    }

    public static boolean saveConfig(AutoOle ol) {
        Ole.toOle2(ol).saveAsFile(ol.getOptionsFolder(), ol.getOptionsFile(), true);
        return true;
    }
    
    public static boolean editConfig(AutoOle ol) {
        OleConfig ocfg= new OleConfig(Ole.toOle3(ol, false));
        ocfg = ocfg.edit(ol.getApplication());
        Ole.fromOle3(ocfg, ol, false);
        saveConfig(ol);
        return true;
    }

    public static boolean viewConfig(AutoOle ol) {
        OleConfig ocfg= (OleConfig) Ole.toOle3(ol, false);
        ocfg.view(ol.getApplication());
        return true;
    }
}
