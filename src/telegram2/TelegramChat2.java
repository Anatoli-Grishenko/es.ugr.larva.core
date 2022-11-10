/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram2;

import static database.OleDataBase.BADRECORD;
import java.util.ArrayList;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 *
 * @author lcv
 */
public class TelegramChat2 {


    private ArrayList<TelegramFeed> pendingUpdates;

    public TelegramChat2() {
        resetChat();
    }
    
    public void resetChat() {
        pendingUpdates = new ArrayList<>();
    }
    
    public ArrayList<TelegramFeed> getPendingFeeds() {
        return pendingUpdates;
    }}
