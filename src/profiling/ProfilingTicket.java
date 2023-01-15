/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package profiling;

import data.Ole;
import tools.NetworkAccessPoint;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class ProfilingTicket {

    protected String owner = "", id = "", description = "", supertiket = "", request = "";
    protected String start = "", end = "";
    protected String accessPoint = "";
    protected int size = -1;
    protected ProfilingTicket[] compound = new ProfilingTicket[0];

    public ProfilingTicket() {
    }

    public ProfilingTicket(ProfilingTicket pt) {
        setSupertiket(pt.getId());
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

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(String accessPoint) {
        this.accessPoint = accessPoint;
    }

    public NetworkAccessPoint extractAccessPoint() {
        if (getAccessPoint() != null) {
            NetworkAccessPoint nap = new NetworkAccessPoint();
            Ole.oleToObject(new Ole(getAccessPoint()), nap, NetworkAccessPoint.class);
            return nap;
        } else {
            return null;
        }
    }

    public void addProfilingSubTicket(ProfilingTicket pt) {
        ProfilingTicket[] ptnew = new ProfilingTicket[compound.length + 1];
        int i = 0;
        for (ProfilingTicket apt : compound) {
            ptnew[i] = compound[i++];
        }
        compound = ptnew;

    }
}
