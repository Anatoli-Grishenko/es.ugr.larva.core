/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package games;

import static disk.fileutils.listFiles;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.HashMap;
import javax.swing.ImageIcon;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class GameCharacter extends GameObject {

    protected HashMap<String, Sprite> facets;
    protected String currentFacet = "";

    public GameCharacter(String id, String folder) {
        super(id);
        facets = new HashMap();
        Sprite aux;
        for (String iconname : listFiles(folder, "png")) {
            aux = new Sprite();
            if (aux.spLoad(folder, iconname)) {
                facets.put(FilenameUtils.removeExtension(iconname), aux);
            }
            if (currentFacet.equals("")) {
                currentFacet = FilenameUtils.removeExtension(iconname);
            }
        }
    }

    @Override
    public void showGameObject(GameScene gs) {
        double x = getX() * gs.getCell(), y = getY() * gs.getCell();
        //System.out.println("Object: " + getId());
        Sprite sp = facets.get(currentFacet);
        gs.getG().drawImage(sp.getSprite(), (int)x, (int)y, sp.spWidth(), sp.spHeight(), null);
    }

    public String getCurrentFacet() {
        return currentFacet;
    }

    public void setCurrentFacet(String currentFacet) {
        this.currentFacet = currentFacet;
    }

}
