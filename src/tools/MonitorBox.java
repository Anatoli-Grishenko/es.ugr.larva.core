/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import jade.lang.acl.ACLMessage;
import java.util.function.Consumer;
import java.util.function.Supplier;
import swing.OleApplication;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class MonitorBox {

    public Supplier<ACLMessage> doReceive;
    public Consumer<ACLMessage> doSend;
    public Consumer<ACLMessage> doExit;
    public Supplier<OleApplication> doApplication;

    public Supplier<ACLMessage> getDoReceive() {
        return doReceive;
    }

    public void setDoReceive(Supplier<ACLMessage> doReceive) {
        this.doReceive = doReceive;
    }

    public Consumer<ACLMessage> getDoSend() {
        return doSend;
    }

    public void setDoSend(Consumer<ACLMessage> doSend) {
        this.doSend = doSend;
    }

    public Consumer<ACLMessage> getDoExit() {
        return doExit;
    }

    public void setDoExit(Consumer<ACLMessage> doExit) {
        this.doExit = doExit;
    }

    public Supplier<OleApplication> getDoApplication() {
        return doApplication;
    }

    public void setDoApplication(Supplier<OleApplication> doApplication) {
        this.doApplication = doApplication;
    }

    

}
