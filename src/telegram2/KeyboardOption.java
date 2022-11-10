/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram2;

import java.util.function.Consumer;

/**
 *
 * @author lcv
 */
public class KeyboardOption {
    protected String Name;
    protected String NextMenu;
    protected Consumer <TelegramFeed> CallBackHandler;
    
    public KeyboardOption(String name) {
        NextMenu=null;
        CallBackHandler=null;
        Name=name;
    }

    public KeyboardOption(String name, String next) {
        NextMenu=next;
        CallBackHandler=null;
        Name=name;
    }

   
    public KeyboardOption(String name, Consumer <TelegramFeed> handler) {
        Name=name;
        NextMenu=null;
        CallBackHandler = handler;
    }

    public String getName() {
        return Name;
    }

    public String getNextMenu() {
        return NextMenu;
    }

    public Consumer<TelegramFeed> getCallBackHandler() {
        return CallBackHandler;
    }    
    
}