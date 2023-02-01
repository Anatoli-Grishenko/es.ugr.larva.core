/*
 * To change this license header, choose License Headers in Prject Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import crypto.Keygen;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class NetworkCookie {

    protected String ID = "", payload = "",
            owner = "", accessPoint = "", description = "", replyID = "";
    String tUpstream, tArrive, tSendBack, tReceive;
    protected boolean zipped = false;
    int size = -1, serie = -1, scale = -1, realSize = -1;

    public NetworkCookie() {
        setID(Keygen.getAlphaKey(16));
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String gettUpstream() {
        return tUpstream;
    }

    public void settUpstream(String tUpstream) {
        this.tUpstream = tUpstream;
    }

    public String gettArrive() {
        return tArrive;
    }

    public void settArrive(String tArrive) {
        this.tArrive = tArrive;
    }

    public String gettSendBack() {
        return tSendBack;
    }

    public void settSendBack(String tSendBack) {
        this.tSendBack = tSendBack;
    }

    public String gettReceive() {
        return tReceive;
    }

    public void settReceive(String tReceive) {
        this.tReceive = tReceive;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isZipped() {
        return zipped;
    }

    public void setZipped(boolean zipped) {
        this.zipped = zipped;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSerie() {
        return serie;
    }

    public void setSerie(int serie) {
        this.serie = serie;
    }

    public long getLatencyUp() {
        try{
        return new TimeHandler(this.gettUpstream()).elapsedTimeMilisecsUntil(new TimeHandler(this.gettArrive()));
        } catch(Exception ex) {
        return new TimeHandler(this.gettUpstream()).elapsedTimeMilisecsUntil(new TimeHandler(this.gettArrive()));            
        }
    }

    public long getLatencyDown() {
        return new TimeHandler(this.gettSendBack()).elapsedTimeMilisecsUntil(new TimeHandler(this.gettReceive()));
    }

    public long getLatencyServer() {
        return new TimeHandler(this.gettArrive()).elapsedTimeMilisecsUntil(new TimeHandler(this.gettSendBack()));
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(String accessPoint) {
        this.accessPoint = accessPoint;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getRealSize() {
        return realSize;
    }

    public void setRealSize(int realSize) {
        this.realSize = realSize;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReplyID() {
        return replyID;
    }

    public void setReplyID(String replyID) {
        this.replyID = replyID;
    }


}
