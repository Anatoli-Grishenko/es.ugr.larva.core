/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram2;

import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

/**
 *
 * @author lcv
 */
public class Keyboard {

    protected ReplyKeyboardMarkup menu;
    protected List<KeyboardRow> buttons;
    protected ArrayList<KeyboardOption> Options;
    protected KeyboardRow row;
    protected int ncols, col;
    protected String name;
    protected String description;
    protected String parent;

    public Keyboard(String name, String parent, String description) {
        this.name = name;
        buttons = new ArrayList();
        Options = new ArrayList();
        row = new KeyboardRow();
        buttons.add(row);
        menu = new ReplyKeyboardMarkup();
        menu.setKeyboard(buttons);
        ncols = 1;
        ncols = 1;
        col = 0;
        this.description = description;
        this.parent = parent;
        if (parent != null) {
            setColumns(1);
            KeyboardOption ksBack = new KeyboardOption("Atrás", parent);
            addButton(ksBack);
        }
    }

    public Keyboard setColumns(int nc) {
        ncols = nc;
        return this;
    }

    public Keyboard addButton(KeyboardOption ksel) {
        KeyboardButton myButton = new KeyboardButton();
        myButton.setText(ksel.getName());
        row.add(myButton);
        Options.add(ksel);
        if (col % ncols == 0) {
            row = new KeyboardRow();
            buttons.add(row);
        }
        col++;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ReplyKeyboardMarkup getKeyboard() {
        return this.menu;
    }

    public KeyboardOption getOption(String option) {
        for (KeyboardOption ko : Options) {
            if (ko.getName().equals(option)) {
                return ko;
            }
        }
        return null;
    }
}
