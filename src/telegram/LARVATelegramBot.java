///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package telegram;
//
//import java.util.List;
//import java.util.function.Consumer;
//import javax.lang.model.SourceVersion;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.meta.api.objects.Update;
////import org.telegram.telegrambots.bots.TelegramLongPollingBot;
////import org.telegram.telegrambots.meta.api.objects.Update;
//
///**
// *
// * @author lcv
// */
//public class LARVATelegramBot extends TelegramLongPollingBot {
//
//    TelegramUpdates updateQueue;
//    String name, token;
//    Consumer<Update> callBackBot;
//
//    public LARVATelegramBot(LARVAAdminAgent a, String name, String token, TelegramUpdates q, Consumer<Update> hook) {
//        super();
//        owner = a;
//        updateQueue = q;
//        this.name = name;
//        this.token = token;
//        callBackBot = hook;
//    }
//
//    @Override
//    public String getBotUsername() {
//        return name;
//    }
//
//    @Override
//    public String getBotToken() {
//        return token;
//    }
//
//    @Override
//    public void onUpdateReceived(Update update) {
//        try {
//            System.out.println(getBotUsername()+"<---------TELEGRAM from " + update.getMessage().getChatId());
//        } catch (Exception ex) {
//            System.out.println(getBotUsername()+"<---------TELEGRAM from unaccessible chatid");
//        }
//        callBackBot.accept(update);
//        updateQueue.pushUpdate(update);
//        owner.defaultBehaviour.restart();
//    }
//
//    @Override
//    public void onUpdatesReceived(List<Update> l) {
//        for (Update u : l) {
//            onUpdateReceived(u);
//        }
//    }
//////
//////    @Override
//////    public SourceVersion getSupportedSourceVersion() {
//////        return SourceVersion.latest();
//////    }
////
////    public SourceVersion getSupportedSourceVersion() {
////        return SourceVersion.latest();
////    }
//
//}
///*
//BotFather
//Done! Congratulations on your new bot. You will find it at t.me/LARVATelegramer_bot. You can now add a description, about section and profile picture for your bot, 
//see /help for a list of commands. By the way, when you've finished creating your cool bot, ping our Bot Support if you want a better username for it. 
//Just make sure the bot is fully operational before you do this.
//
//Use this token to access the HTTP API:
//1393186120:AAEnh1Pi00lG7oO1sd4okGoxNLenwoFaRUw
//Keep your token secure and store it safely, it can be used by anyone to control your bot.
//
//For a description of the Bot API, see this page: https://core.telegram.org/bots/api
//
// */
//
// /*
//        if (privateDBA) {
//            return "1484821048:AAHAuoXdmcXFF21Gye_mpKlpEOd4fbrpFrs";
//        } else {
//            return "1393186120:AAEnh1Pi00lG7oO1sd4okGoxNLenwoFaRUw";
//        }
//
// */
