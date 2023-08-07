/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram2;

import data.OleFile;

/**
 *
 * @author lcv
 */
public class TelegramFeed {
    protected String cid;
    protected String choice, 
            message, 
            keyboardID,
            context;
    protected OleFile oFile;
    
    public TelegramFeed(String mycid) {
        cid = mycid;
        choice=null;
        message=null;
        keyboardID=null;
        oFile=null;
    }

    public TelegramFeed(Long mycid) {
        cid = ""+mycid;
        choice=null;
        message=null;
        keyboardID=null;
        oFile=null;
    }

    public long getCid() {
        return Long.parseLong(cid);
    }

    public String getCidS() {
        return cid;
    }

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice, String keyboardID) {
        this.choice = choice;
        this.keyboardID = keyboardID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getKeyboardID() {
        return keyboardID;
    }
 
    public OleFile getoFile() {
        return oFile;
    }

    public void setoFile(OleFile oFile) {
        this.oFile = oFile;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }


}
