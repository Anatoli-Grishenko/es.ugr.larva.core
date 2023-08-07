/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram;

import crypto.Keygen;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

/**
 *
 * @author lcv
 */
public class TelegramMenuBar {

    protected HashMap<String, TelegramMenuOption> options;
    protected HashMap<String, TelegramMenu> theMenu;
    protected HashMap<Long, TelegramMenu> lastMenu;
    TelegramMenu rootMenu;

    public TelegramMenuBar() {
        options = new HashMap();
        theMenu = new HashMap();
        lastMenu = new HashMap();
    }

    public TelegramMenu addMenu(String name) {
        TelegramMenu m = new TelegramMenu(name);
        theMenu.put(name, m);
        return m;
    }

    public TelegramMenu getMenu(String name) {
        return theMenu.get(name);
    }

//    public TelegramMenuBar addSubMenu(String parent, String name) {
//        TelegramMenu parentMenu = theMenu.get(parent);
//        if (parent == null)
//            return null;
//        TelegramMenu m = new TelegramMenu(name);
//        theMenu.put(name, m);
//        return this;
//    }
    public TelegramMenuBar addOption(TelegramMenu m, String option, Consumer<TelegramChoice> r) {
        String code = m.getName() + " " + option + " " + Keygen.getAlphaNumKey(5);
        options.put(option, new TelegramMenuOption(r));
        m.addButton(option);
        return this;
    }

    public TelegramMenuBar addOption(TelegramMenu m, String option, TelegramMenu subm) {
        String code = m.getName() + " " + option + " " + Keygen.getAlphaNumKey(5);
        options.put(option, new TelegramMenuOption(subm));
        m.addButton(option);
        return this;
    }

    public TelegramMenuBar addOption(TelegramMenu m, String option) {
        String code = m.getName() + " " + option + " " + Keygen.getAlphaNumKey(5);
        options.put(option, new TelegramMenuOption());
        m.addButton(option);
        return this;
    }

    public TelegramMenuOption getOption(String code) {
        return this.options.get(code);
    }

    public TelegramMenu getRootMenu() {
        return rootMenu;
    }

    public void setRootMenu(TelegramMenu rootMenu) {
        this.rootMenu = rootMenu;
    }

    public TelegramMenu getLastMenu(long cid) {
        if (lastMenu.get(cid)==null) {
            setLastMenu(cid, rootMenu);
        }
        return lastMenu.get(cid);
    }

    public void setLastMenu(long cid, TelegramMenu lastMenu) {
        this.lastMenu.put(cid, lastMenu);
    }

    public boolean isMenu(String option) {
        return theMenu.containsKey(option);
    }

    public boolean isOption(String option) {
        return options.containsKey(option);
    }

    public boolean isValidChoice(String option) {
        return isOption(option) || isMenu(option);
    }

    public TelegramMenu processChoice(long cid, String c) {
        TelegramMenu res = null;
        if (theMenu.containsKey(c)) {
            res = theMenu.get(c);
        }
        if (options.containsKey(c)) {
            if (options.get(c).nextMenu != null) {
                res = options.get(c).nextMenu;
            } else if (options.get(c).callBackHandler != null) {
                TelegramChoice tc = new TelegramChoice(cid, c);                
                options.get(c).callBackHandler.accept(tc);
                res = this.getLastMenu(cid);
            } else {
                res = null;
            }
        }
        return res;
    }
}
