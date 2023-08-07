/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram2;

import crypto.Keygen;
import data.OleFile;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import map2D.Map2DColor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import static org.telegram.telegrambots.meta.api.methods.ParseMode.HTML;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.meta.api.objects.Update;

/**
 *
 * @author lcv
 */
public class Bot extends TelegramLongPollingBot {

    protected FeedBase Feeds;
    protected String Name, Token;
    protected BotSession myTSession;
    protected Consumer<TelegramFeed> Listener;
    protected Semaphore waitUpdates;

    protected HashMap<Long, String> ContextKeyboard;
    protected HashMap<Long, KeyboardOption> ContextOption;
    protected HashMap<String, Keyboard> Keyboards;
    protected Keyboard Default;

    public Bot(String name, String token, Consumer<TelegramFeed> defListener) {
        super();
        this.Name = name;
        this.Token = token;
        Feeds = null;
        Listener = defListener;
        waitUpdates = null;
        init();
        this.start();
    }

    public Bot(String name, String token) {
        super();
        this.Name = name;
        this.Token = token;
        Feeds = new FeedBase();
        Listener = null;
        waitUpdates = new Semaphore(0);
        init();
        this.start();
    }

    protected void init() {
        ContextKeyboard = new HashMap();
        ContextOption = new HashMap();
        Keyboards = new HashMap();
        Default = null;
    }

    public void start() {
        try {
            // Se crea un nuevo Bot API
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            BotSession myTSession = telegramBotsApi.registerBot(this);
        } catch (Exception e) {
            System.err.println("Unable to create Telegram bot" + e.toString());
        }
    }

    public void shutDown() {
        myTSession.stop();
    }

    public boolean hasFeeds() {
        return Feeds != null;
    }

    @Override
    public String getBotUsername() {
        return Name;
    }

    @Override
    public String getBotToken() {
        return Token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        TelegramFeed feed = null;
        Long chatID;
        Keyboard k;
        KeyboardOption ko;

//        if (update.has.hasCallbackQuery()){
//            chatID=update.getCallbackQuery().getMessage().getChatId();
//            feed = new TelegramFeed(chatID);
//            feed.setMessage(update.getMessage().getText());
//       } else 
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatID = update.getMessage().getChatId();
            feed = new TelegramFeed("" + chatID);
            if (ContextKeyboard.get(chatID) != null) {
                k = Keyboards.get(ContextKeyboard.get(chatID));
                ko = k.getOption(update.getMessage().getText());
                if (ko != null) {
                    feed.setChoice(ko.getName(), k.getName());
                    if (ko.getNextMenu() != null) {
                        ContextOption.put(chatID, ko);
                        linkKeyboard(chatID, ko.getNextMenu());
                        replyToFeed(feed, "Listado de socios", null, null);
                    } else if (ko.getCallBackHandler() != null) {
                        ko.getCallBackHandler().accept(feed);
                        ContextOption.put(chatID, ko);
                    }
                    return;
                } else {
                    ko = ContextOption.get(chatID);
                    if (ko != null) {
                        feed.setChoice(ko.getName(), k.getName());
                    }
                }
            }
            feed.setMessage(update.getMessage().getText());
            if (feed != null) {
                if (Listener != null) {
                    Listener.accept(feed);
                } else if (Feeds != null) {
                    Feeds.pushFeed(feed);
                    waitToUpdates().release(1);
                } else {
                    System.err.println("Intern error");
                }
            }
        }

    }

    public TelegramFeed getFeed() {
        if (waitUpdates != null)
        try {
            waitUpdates.acquire(1);
            return Feeds.popFeed();
        } catch (Exception ex) {
            System.err.println("Exception " + ex.toString());
            System.exit(1);
        }
        return null;
    }

    public void replyToFeed(TelegramFeed feed, String message, OleFile doc, String photofilename) {
        SendMessage msgTelegram;
        SendPhoto picTelegram;
        Long cid = feed.getCid();
        if (photofilename != null) {
            try {
                InputFile fi = new InputFile();
                fi.setMedia(new File(photofilename));
                picTelegram = new SendPhoto();
                picTelegram.setChatId("" + cid);
                picTelegram.setPhoto(fi);
                picTelegram.setParseMode(ParseMode.HTML);
                picTelegram.setCaption(message);
                if (ContextKeyboard.get(cid) != null) {
                    picTelegram.setReplyMarkup(Keyboards.get(ContextKeyboard.get(feed.getCid())).getKeyboard());
                }
                this.execute(picTelegram);
            } catch (Exception ex) {
                System.err.println("Exception " + ex.toString());
            }
        } else if (doc != null) {

        } else if (message != null) {
            msgTelegram = new SendMessage();
            msgTelegram.setChatId("" + cid);
            msgTelegram.setText(message);
            msgTelegram.setParseMode(HTML);
//            if (feed.getKeyboardID() != null) {
            if (ContextKeyboard.get(cid) != null) {
                msgTelegram.setReplyMarkup(getKeyboard(ContextKeyboard.get(cid)).getKeyboard());
            }
            try {
                this.execute(msgTelegram);
            } catch (Exception ex) {
                System.err.println("Exception " + ex.toString());
            }
        }
    }

    public void sendFeed(Long cid, String message, OleFile doc, OleFile photo) throws Exception {
        SendMessage msgTelegram;
        SendPhoto picTelegram;
        SendAnimation gifTelegram;

        if (photo != null) {
            Map2DColor m2show = new Map2DColor();
            String k = Keygen.getHexaKey();
            photo.saveAsFile("./downloads/", k, true);

            InputFile fi = new InputFile();
            if (photo.getFileName().endsWith("png")) {
                fi.setMedia(new File("./downloads/" + k + ".png"));
                picTelegram = new SendPhoto();
                picTelegram.setChatId("" + cid);
                picTelegram.setPhoto(fi);
                picTelegram.setParseMode(ParseMode.HTML);
                picTelegram.setCaption(message);
                if (ContextKeyboard.get(cid) != null) {
                    picTelegram.setReplyMarkup(Keyboards.get(ContextKeyboard.get(cid)).getKeyboard());
                }
                this.execute(picTelegram);
            } else if (photo.getFileName().endsWith("gif")) {
                fi.setMedia(new File("./downloads/" + k + ".gif"));
                gifTelegram = new SendAnimation();
                gifTelegram.setChatId("" + cid);
                gifTelegram.setAnimation(fi);
                gifTelegram.setParseMode(ParseMode.HTML);
                gifTelegram.setCaption(message);
                if (ContextKeyboard.get(cid) != null) {
                    gifTelegram.setReplyMarkup(Keyboards.get(ContextKeyboard.get(cid)).getKeyboard());
                }
                this.execute(gifTelegram);
            }

        } else if (doc != null) {

        } else if (message != null) {
            msgTelegram = new SendMessage();
            msgTelegram.setChatId("" + cid);
            msgTelegram.setText(message);
            msgTelegram.setParseMode(HTML);
//            if (feed.getKeyboardID() != null) {
            if (ContextKeyboard.get(cid) != null) {
                msgTelegram.setReplyMarkup(getKeyboard(ContextKeyboard.get(cid)).getKeyboard());
            }
            try {
                this.execute(msgTelegram);
            } catch (Exception ex) {
                System.err.println("Exception sending "+message+ ex.toString());
            }
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> l) {
        for (Update u : l) {
            onUpdateReceived(u);
        }
    }

    public Semaphore waitToUpdates() {
        return waitUpdates;
    }

    public void addKeyBoard(Keyboard k) {
        Keyboards.put(k.getName(), k);
        if (Default == null) {
            setDefaultKeyboard(k.getName());
        }
    }

    public void linkKeyboard(Long cid, String kname) {
        if (Keyboards.keySet().contains(kname)) {
            ContextKeyboard.put(cid, kname);
        }
    }

    public void setDefaultKeyboard(String kname) {
        if (Keyboards.keySet().contains(kname)) {
            Default = Keyboards.get(kname);
        }
    }

    public Keyboard getLinkedKeyboard(Long cid) {
        return Keyboards.get(ContextKeyboard.get(cid));
    }

    public Keyboard getKeyboard(String kid) {
        return Keyboards.get(kid);
    }
}
