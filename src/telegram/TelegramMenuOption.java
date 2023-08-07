/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram;

import java.util.function.Consumer;

/**
 *
 * @author lcv
 */
public class TelegramMenuOption {
    public TelegramMenu nextMenu;
    public Consumer <TelegramChoice> callBackHandler;
    
    public TelegramMenuOption(TelegramMenu m) {
        nextMenu=m;
        callBackHandler=null;
    }

   
    public TelegramMenuOption(Consumer <TelegramChoice> handler) {
        nextMenu=null;
        callBackHandler = handler;
    }
    
    public TelegramMenuOption() {
        nextMenu=null;
        callBackHandler=null;
    }
    
    public boolean isExit() {
        return (this.nextMenu == null && this.callBackHandler ==null);
    }
}