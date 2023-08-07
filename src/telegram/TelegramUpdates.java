/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram;

import static database.OleDataBase.BADRECORD;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 *
 * @author lcv
 */
public class TelegramUpdates {

    HashMap<Long, TelegramChat> updateQueue;
    Semaphore waitUpdate;

    public TelegramUpdates() {
        updateQueue = new HashMap<>();
        waitUpdate=new Semaphore(0);
    }

    public TelegramUpdates(Semaphore s) {
        updateQueue = new HashMap<>();
        waitUpdate=s;
    }

    public int size() {
        return updateQueue.keySet().size();
    }
    
    public synchronized void initChatID(long cid) {
        if (updateQueue.get(cid) == null) {
            updateQueue.put(cid, new TelegramChat());
        }
    }

    public  synchronized Set<Long> getAllChatIDRegistered() {
        return updateQueue.keySet();
    }

    protected synchronized boolean isEmptyChatID(long chatID) {
        return (getChatIDPendingUpdates(chatID) == null || getChatIDPendingUpdates(chatID).isEmpty());
    }

    public synchronized boolean isEmptyUserID(int userID) {
        return (getUserIDPendingUpdates(userID) == null || getUserIDPendingUpdates(userID).isEmpty());
    }

    public synchronized boolean isAllEmpty() {
        boolean res = true;
        for (long cid : updateQueue.keySet()) {
            res = res && getChatIDPendingUpdates(cid).isEmpty();
        }
        return res;
    }

    public synchronized long getChatIDUser(int userID) {
        for (long cid : updateQueue.keySet()) {
            if (this.updateQueue.get(cid).getUserID() == userID) {
                return cid;
            }
        }
        return BADRECORD;
    }

    public  synchronized  List<Long> getAllChatIDActive() {
        List<Long> res = new ArrayList<Long>();
        for (long cid : updateQueue.keySet()) {
            if (!this.isEmptyChatID(cid)) {
                res.add(cid);
            }
        }
        return res;
    }

    public  synchronized List<Long> getAllChatIDGroup(int groupID) {
        List<Long> res = new ArrayList<Long>();
        for (long cid : updateQueue.keySet()) {
            if (updateQueue.get(cid).getGroupID() == groupID) {
                res.add(cid);
            }
        }
        return res;
    }

    protected  synchronized ArrayList<Update> getChatIDPendingUpdates(long chatID) {
        if (updateQueue.get(chatID) == null) {
            return null;
        }
        return updateQueue.get(chatID).getPendingUpdates();
    }

    protected  synchronized ArrayList<Update> getUserIDPendingUpdates(int userID) {
        return this.getChatIDPendingUpdates(this.getChatIDUser(userID));
    }

    public synchronized void pushUpdate(Update update) {
        Long chatID;
        if (update.hasMessage()) {
            chatID = update.getMessage().getChatId();
            if (getChatIDPendingUpdates(chatID) == null) {
                updateQueue.put(chatID, new TelegramChat());
//                this.waitUpdate.release(1);
            }
            getChatIDPendingUpdates(chatID).add(update);
        } 
        else {
            chatID=update.getCallbackQuery().getMessage().getChatId();
//            updateQueue.put(chatID, new TelegramChat());
            getChatIDPendingUpdates(chatID).add(update);
        }
    }

    public synchronized Update popUpdateChatID(long chatID) {
        Update res = null;
        if (!isEmptyChatID(chatID)) {
            res = getChatIDPendingUpdates(chatID).get(0);
            getChatIDPendingUpdates(chatID).remove(0);
        }
        return res;
    }

    public synchronized Update popUpdateUserID(int userID) {
        return this.popUpdateChatID(this.getChatIDUser(userID));

    }

    public  synchronized  TelegramChat getChatData(long cid) {
        return this.updateQueue.get(cid);
    }

    public   synchronized  Semaphore getWaitUpdate() {
        return waitUpdate;
    }
    
    
    
    
}
