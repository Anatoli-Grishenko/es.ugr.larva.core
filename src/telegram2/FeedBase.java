/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram2;

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
public class FeedBase {

    HashMap<Long, ArrayList<TelegramFeed>> queue;
    ArrayList<Long>timeline;
//    Semaphore waitUpdate;

    public FeedBase() {
        queue = new HashMap<>();
        timeline = new ArrayList();
//        waitUpdate = new Semaphore(0);
    }

//    public FeedBase(Semaphore s) {
//        queue = new HashMap<>();
//        waitUpdate = s;
//    }

    public synchronized void initCID(long cid) {
        if (queue.get(cid) == null) {
            queue.put(cid, new ArrayList<TelegramFeed>());
        }
    }

    public synchronized Set<Long> getAllCIDRegistered() {
        return queue.keySet();
    }

    protected synchronized boolean isKnownCID(long chatID) {
        return (getCIDPendingFeeds(chatID) != null);
    }

    protected synchronized boolean isEmptyCID(long chatID) {
        return (getCIDPendingFeeds(chatID) == null || getCIDPendingFeeds(chatID).isEmpty());
    }

    public synchronized boolean isAllEmpty() {
        boolean res = true;
        for (long cid : queue.keySet()) {
            res = res && getCIDPendingFeeds(cid).isEmpty();
        }
        return res;
    }

    public synchronized List<Long> getAllCIDActive() {
        List<Long> res = new ArrayList<Long>();
        for (long cid : queue.keySet()) {
            if (!this.isEmptyCID(cid)) {
                res.add(cid);
            }
        }
        return res;
    }

    protected synchronized ArrayList<TelegramFeed> getCIDPendingFeeds(long chatID) {
        if (queue.get(chatID) == null) {
            return null;
        }
        return queue.get(chatID);
    }

    public synchronized void pushFeed(TelegramFeed feed) {
        timeline.add(feed.getCid());
        initCID(feed.getCid());
        getCIDPendingFeeds(feed.getCid()).add(feed);
//        waitToUpdates().release(1);
    }

    private synchronized TelegramFeed popFeed(long chatID) {
        TelegramFeed res = null;
        if (!isEmptyCID(chatID)) {
            res = getCIDPendingFeeds(chatID).get(0);
            getCIDPendingFeeds(chatID).remove(0);
        }
        return res;
    }

    public synchronized TelegramFeed popFeed() {
        TelegramFeed res = null;
        Long cid;
        if (timeline.isEmpty())
            return res;
        cid = timeline.get(0);
        timeline.remove(0);
        if (!isEmptyCID(cid)) {
            res = getCIDPendingFeeds(cid).get(0);
            getCIDPendingFeeds(cid).remove(0);
        }
        return res;
    }

//    public synchronized Semaphore waitToUpdates() {
//        return waitUpdate;
//    }
//
}
