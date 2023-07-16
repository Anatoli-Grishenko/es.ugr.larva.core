/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package profiling;

import crypto.Keygen;
import data.Ole;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class ProfilingTicket {

    protected String owner = "", id = "", description = "", series="", supertiket = "";
    protected String start = "", end = "";
    protected String payload = "";
    protected int size = -1, depth=-1;

    public ProfilingTicket() {
        init();
    }

    public ProfilingTicket(ProfilingTicket pt) {
        init();
        setSupertiket(pt.getId());
    }

    protected void init() {
        setId(Keygen.getAlphaKey(16));        
        setStart(TimeHandler.Now());
    }
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSupertiket() {
        return supertiket;
    }

    public void setSupertiket(String supertiket) {
        this.supertiket = supertiket;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    
    
    public NetworkData decodeNetworkData() {
        if (getPayload() != null) {
            NetworkData nap = new NetworkData();
            nap = (NetworkData) Ole.oleToObject(new Ole(getPayload()), NetworkData.class);
            return nap;
        } else {
            return null;
        }
    }
    public void encodeNetworkData(NetworkData nd) {
        setPayload(Ole.objectToOle(nd).toPlainJson().toString());
    }
    
    public int getElapsedTimeMilisecs() {
        return (int)(new TimeHandler(getStart()).elapsedTimeMilisecsUntil(new TimeHandler(getEnd())));
    }
}
