/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram;

/**
 *
 * @author lcv
 */
public class TelegramChoice {
    public long cid;
    public String choice;
    
    public TelegramChoice(long mycid, String mychoice) {
        cid = mycid;
        choice = mychoice;
    }
}
