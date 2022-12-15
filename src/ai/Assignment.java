/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import crypto.Keygen;
import java.util.ArrayList;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Assignment {

    protected String Agent, Action, Backup, City, Id;
    protected int tini, tend, cargo;
    protected ArrayList<Assignment> pre, post;
    protected boolean Over=false;

    public Assignment(String agent, String backup, String Action,
            String city, int tini, int tend, int cargo) {
        this.Agent = agent;
        this.Action = Action;
        this.tini = tini;
        this.tend = tend;
        this.cargo = cargo;
        pre = new ArrayList();
        post = new ArrayList();
        Id = Keygen.getHexaKey(4);
    }

    public String getAgent() {
        return Agent;
    }

    public void setAgent(String Agent) {
        this.Agent = Agent;
    }

    public String getAction() {
        return Action;
    }

    public void setAction(String Action) {
        this.Action = Action;
    }

    public int getTini() {
        return tini;
    }

    public void setTini(int tini) {
        this.tini = tini;
    }

    public int getTend() {
        return tend;
    }

    public void setTend(int tend) {
        this.tend = tend;
    }

    public String getBackup() {
        return Backup;
    }

    public void setBackup(String Backup) {
        this.Backup = Backup;
    }

    public ArrayList<Assignment> getPre() {
        return pre;
    }

    public void setPre(ArrayList<Assignment> pre) {
        this.pre = pre;
    }

    public ArrayList<Assignment> getPost() {
        return post;
    }

    public void setPost(ArrayList<Assignment> post) {
        this.post = post;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String City) {
        this.City = City;
    }

    public boolean isOver() {
        return Over;
    }

    public void setOver(boolean Over) {
        this.Over = Over;
    }

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public int getCargo() {
        return cargo;
    }

    public void setCargo(int cargo) {
        this.cargo = cargo;
    }

    @Override
    public String toString() {        
        return "["+getTini()+","+getTend() + "] " + getAgent() + " " + getAction() + "("+(getTend() - getTini()) + " t.u.)\n";
    }

}
