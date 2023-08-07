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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

/**
 *
 * @author lcv
 */
public class TelegramMenu {
    protected ReplyKeyboardMarkup menu;
    protected List<KeyboardRow> buttons;
    protected KeyboardRow row;
    protected int ncols, col;
    protected String name;
    protected String description;
    
    public TelegramMenu(String n) {
        name = n;
        buttons = new ArrayList();
        row= new KeyboardRow();
        buttons.add(row);
        menu = new ReplyKeyboardMarkup();
        menu.setKeyboard(buttons);
        ncols =1;
        ncols=1;
        col = 0;
        description="no description provided";
    }
    
    public TelegramMenu setDescription(String d) {
        description = d;
        return this;        
    }
    
    public TelegramMenu setColumns(int nc) {
        ncols = nc;
        return this;
    }
    
   public TelegramMenu addButton(String text){
        KeyboardButton myButton = new KeyboardButton();
        myButton.setText(text);
        row.add(myButton);
        if (col%ncols==0) {
            row = new KeyboardRow();
            buttons.add(row);
        }
        col++;
        return this;
    }   
   public String getName() {
       return name;
   }

    public TelegramMenu setName(String name) {
        this.name = name;
        return this;
    }
   
   
   
   public String getDescription() {
       return description;
   }
   public ReplyKeyboardMarkup getKeyboard(){
       return this.menu;
   }
}

