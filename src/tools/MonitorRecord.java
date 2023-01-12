/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import georeference.GeoCoord;
import glossary.Granada;
import tools.NetworkAccessPoint.District;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class MonitorRecord {

    protected final String sep = "\t";
    protected District district;
    protected Granada town;
    protected String id, owner, ip, gMapOwner = "", gMapCenter = "", date, larva;
    protected int serie, size, realSize, latency, loop;
    protected double distance;
    protected boolean zipped;

    public MonitorRecord() {
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public Granada getTown() {
        return town;
    }

    public void setTown(Granada town) {
        this.town = town;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getgMapOwner() {
        return gMapOwner;
    }

    public void setgMapOwner(String gMapOwner) {
        this.gMapOwner = gMapOwner;
    }

    public String getgMapCenter() {
        return gMapCenter;
    }

    public void setgMapCenter(String gMapCenter) {
        this.gMapCenter = gMapCenter;
    }

    public int getSerie() {
        return serie;
    }

    public void setSerie(int serie) {
        this.serie = serie;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public int getLoop() {
        return loop;
    }

    public void setLoop(int loop) {
        this.loop = loop;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getDistance() {
        GeoCoord g1, g2;

        try {
            g1 = new GeoCoord(GeoCoord.fromGooleMaps(this.getgMapCenter()));
            g2 = new GeoCoord(GeoCoord.fromGooleMaps(this.getgMapOwner()));
            return g1.planeDistanceTo(g2);
        } catch (Exception ex) {
            return -1;
        }
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String toTsv() {
        String res = "";
        res
                += getLarva() + sep
                + getOwner() + sep
                + getDate() + sep
                + getId() + sep
                + getSize() + sep
                + getRealSize() + sep
                + isZipped() + sep
                + getSerie() + sep
                + getTown().name() + sep
                + getDistrict().name() + sep
                + getDistance() + sep
                + getLoop() + sep
                + getLatency() + sep;
        return res;
    }

    public String getLarva() {
        return larva;
    }

    public void setLarva(String larva) {
        this.larva = larva;
    }

    public boolean isZipped() {
        return zipped;
    }

    public void setZipped(boolean zipped) {
        this.zipped = zipped;
    }

    public int getRealSize() {
        return realSize;
    }

    public void setRealSize(int realSize) {
        this.realSize = realSize;
    }

}
