/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class MessageBox {
    
    protected HashMap <String, ACLMessage> masterQueue;
    protected HashMap <String, ArrayList<String>> conversationQueue;
    protected HashMap <String, ArrayList<String>> senderQueue;
    protected HashMap <String, ArrayList<String>> dateQueue;
    protected HashMap <String, ArrayList<String>> replyQueue;
    
    public MessageBox() {
        masterQueue = new HashMap();
        senderQueue = new HashMap();
        dateQueue = new HashMap();
        replyQueue = new HashMap();
    }
    
}
