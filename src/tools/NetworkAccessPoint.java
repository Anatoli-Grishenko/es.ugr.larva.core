/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import data.AutoOle;
import georeference.GeoCoord;
import glossary.Granada;
import swing.OleApplication;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class NetworkAccessPoint implements AutoOle {

    public enum District {
        EDUROAM, ALBAYCIN, BEIRO, CENTRO, CHANA, GENIL, NORTE, RONDA, ZAIDIN, OTROMUNICIPIO
    };
    protected District myDistrict = District.ALBAYCIN;
    protected Granada Municipio = Granada.Granada;
    protected String myGMaps = "";
    protected int utmX=0, utmY=0;
    protected double lattitude=0, longitude=0;
    protected String myISP = "", localIP = "", extIP = "", folder="";


    @Override
    public String getOptionsFile() {
        return "networkdata.json";
    }

    @Override
    public String getOptionsFolder() {
        return folder;
    }

    @Override
    public OleApplication getApplication() {
        return null;
    }

    public NetworkAccessPoint(String folder) {
        this.folder = folder;
    }

    public NetworkAccessPoint() {
        this.folder = "./client/";
    }

    public District getMyDistrict() {
        return myDistrict;
    }

    public void setMyDistrict(District myDistrict) {
        this.myDistrict = myDistrict;
    }

    public String getMyGMaps() {
        return myGMaps;
    }

    public void setMyGMaps(String myGMaps) {
        this.myGMaps = myGMaps;
    }

    public String getMyISP() {
        return myISP;
    }

    public void setMyISP(String myISP) {
        this.myISP = myISP;
    }

    public Granada getMunicipio() {
        return Municipio;
    }

    public void setMunicipio(Granada Municipio) {
        this.Municipio = Municipio;
    }

    public String getLocalIP() {
        return localIP;
    }

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
    }

    public String getExtIP() {
        return extIP;
    }

    public void setExtIP(String extIP) {
        this.extIP = extIP;
    }

    public int getUtmX() {
        return utmX;
    }

    public void setUtmX(int utmX) {
        this.utmX = utmX;
    }

    public int getUtmY() {
        return utmY;
    }

    public void setUtmY(int utmY) {
        this.utmY = utmY;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean validate() {
        boolean res = true;
        if (getMyGMaps()!= null && getMyGMaps().length()>0) {
            try {
                GeoCoord gc = new GeoCoord(GeoCoord.fromGooleMaps(getMyGMaps()));
                if ((int)gc.getLatitude()!= 37 || (int) gc.getLongitude() != -3 ) {
                    return false;
                } else {
                    setUtmX(gc.getXInt());
                    setUtmY(gc.getYInt());
                    setLattitude(gc.getLatitude());
                    setLongitude(gc.getLongitude());
                }
            } catch (Exception ex) {
                res= false;
            }
        }
        return res;
    }

}
