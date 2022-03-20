/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.WriterConfig;
import data.Ole;
import static data.Ole.oletype.BADVALUE;
import data.OleFile;
import data.Transform;
import geometry.Point3D;
import geometry.Vector3D;
import glossary.sensors;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.mail.Session;
import map2D.Map2DColor;
import map2D.Palette;
import tools.TimeHandler;
import static world.Perceptor.NULLREAD;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class SensorDecoder {

    public static Color cBad = new Color(100, 0, 0);
    protected HashMap<String, JsonArray> indexperception;
    protected JsonArray lastPerception;
    protected Map2DColor hMap;
    protected int maxlevel;
    protected boolean ready, filterreading;
    protected String name, sessionID, commitment;

    public SensorDecoder() {
        clear();
    }

    public boolean setWorldMap(String content, int maxlevel) {
        this.maxlevel = maxlevel;
        OleFile mapa = new OleFile();
        try {
            mapa.set(content);
            mapa.saveFile("./maps/");
            String name = mapa.getFileName();
            hMap = new Map2DColor();
            hMap.loadMapRaw("./maps/" + name);
            File toremove= new File("./maps/" + name);
            if (toremove.exists())
                toremove.delete();
            return true;
        } catch (IOException ex) {
        }
        return false;
    }

    public int getMaxlevel() {
        return maxlevel;
    }

    public void setMaxlevel(int maxlevel) {
        this.maxlevel = maxlevel;
    }

    public String getStatus() {
        return this.getSensor(glossary.sensors.STATUS.name().toLowerCase()).get(0).asString();
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public Map2DColor getWorldMap() {
        return hMap;
    }
  
    public boolean hasSensor(String sensorname) {
        return isReady() && this.indexperception.keySet().contains(sensorname.toLowerCase());
    }
    protected JsonArray getSensor(String sensorname) {
        if (isReady()) {
            if (this.indexperception.keySet().contains(sensorname)) {
                return indexperception.get(sensorname);
            }
        }
        return null;
    }

    public void setSensor(String sensorname, JsonArray reading) {
        indexperception.put(sensorname, reading);
        lastPerception.add(new JsonObject().add("sensor", sensorname).add("data",reading));
    }

    public void clear() {
        indexperception = new HashMap();
        lastPerception = new JsonArray();
        ready = false;
        
    }


    public boolean getAlive() {
        if (isReady() && hasSensor("ALIVE")) {
            return getSensor("alive").get(0).asInt() > 0;
        }
        return false;
    }

    public boolean getOnTarget() {
        if (isReady() && hasSensor("ONTARGET")) {
            return getSensor("ontarget").get(0).asInt() > 0;
        }
        return false;
    }

    protected double[] fromJsonArray(JsonArray jsa) {
        double res[] = new double[jsa.size()];
        for (int i = 0; i < jsa.size(); i++) {
            res[i] = jsa.get(i).asDouble();
        }
        ready = true;
        return res;
    }

    public boolean isReady() {
        return ready;
    }

    public double[] getGPS() {
        double[] res = new double[3];
        if (isReady() && hasSensor("GPS")) {
            res = fromJsonArray(getSensor("gps").get(0).asArray());
        }
        return res;
    }

    public int getPayload() {
        if (isReady() && hasSensor("PAYLOAD")) {
            return getSensor("payload").get(0).asInt();
        }
        return -1;
    }

    public int getCompass() {
        if (isReady() && hasSensor("COMPASS")) {
            int v = (int) getSensor("compass").get(0).asDouble();
            v = 360+90-v;   
            return v%360;            
        }
        return -1;
    }

    public int getAltitude() {
        if (isReady() && hasSensor("ALTITUDE")) {
            return (int) getSensor("altitude").get(0).asDouble();
        }
        return -1;
    }

    public double getDistance() {
        if (isReady() && hasSensor("DISTANCE")) {
            return (int) getSensor("distance").get(0).asDouble();
        }
        return -1;
    }

    public double getAngular() {
        if (isReady() && hasSensor("ANGULAR")) {
            double v = getSensor("angular").get(0).asDouble();
            v = 360+90-v;   
            if (v>=360)
                return v-360;            
            else 
                return v;
        }
        return -1;
//        if (isReady() && hasSensor("ANGULAR")) {
//            int v = (int) getSensor("angular").get(0).asDouble();
//            v = 360+90-v;   
//            return v%360;            
//        }
//        return -1;
    }

    public double getAngular(Point3D p) {
        if (isReady() && hasSensor("ANGULAR")) {
            Vector3D Norte = new Vector3D(new Point3D(0, 0), new Point3D(0, -10));
            Point3D me = new Point3D(getGPS()[0], getGPS()[1], getGPS()[2]);
            Vector3D Busca = new Vector3D(me, p);

            
            int v = (int) Norte.angleXYTo(Busca);;
            v = 360+90-v;   
            return v%360;            
        }
        return -1;
    }

    public double getEnergy() {
        if (isReady() && hasSensor("ENERGY")) {
            return (int) getSensor("energy").get(0).asDouble();
        }
        return -1;
    }

    public double getEnergyBurnt() {
        if (isReady() && hasSensor("ENERGYBURNT")) {
            return (int) getSensor("energyburnt").get(0).asDouble();
        }
        return -1;
    }

    public String[] getTrace() {
        if (isReady() && hasSensor("TRACE")) {
            return Transform.toArray(new ArrayList(Transform.toArrayList(getSensor("trace"))));
        }
        return new String[0];
    }
    
    public String[] getCargo() {
        if (isReady() && hasSensor("CARGO")) {
            return Transform.toArray(new ArrayList(Transform.toArrayList(getSensor("cargo"))));
        }
        return new String[0];
    }
    
    public int getNSteps() {
        if (isReady() && hasSensor("NUMSTEPS")) {
            return (int) getSensor("numsteps").get(0).asDouble();
        }
        return -1;
        
    }

    public int[][] getVisualData() {
        JsonArray jsaReading=null;
        jsaReading = getSensor("visual");
       if (jsaReading ==null)
            jsaReading = getSensor("visualhq");
        if (isReady() && jsaReading != null) {
            int range = jsaReading.size();
            int[][] res = new int[range][range]; //jsaVisual.size(), jsaVisual.size());
            for (int i = 0; i < res.length; i++) {
                for (int j = 0; j < res[0].length; j++) {
                        res[i][j] = jsaReading.get(i).asArray().get(j).asInt();
                }
            }

            return res;
        }
        return new int[0][0];
    }

    public int[][] getLidarData() {
        JsonArray jsaReading=null;
        jsaReading = getSensor("lidar");
       if (jsaReading ==null)
            jsaReading = getSensor("lidarhq");
        if (isReady() && jsaReading != null) {
            int range = jsaReading.size();
            int[][] res = new int[range][range]; //jsaVisual.size(), jsaVisual.size());
            for (int i = 0; i < res.length; i++) {
                for (int j = 0; j < res[0].length; j++) {
                        res[i][j] = jsaReading.get(i).asArray().get(j).asInt();
                }
            }

            return res;
        }
        return new int[0][0];
    }

    public int[][] getThermalData() {
        JsonArray jsaReading=null;
        jsaReading = getSensor("thermal");
       if (jsaReading ==null)
            jsaReading = getSensor("thermalhq");
        if (isReady() && jsaReading != null) {
            int range = jsaReading.size();
            int[][] res = new int[range][range]; //jsaVisual.size(), jsaVisual.size());
            for (int i = 0; i < res.length; i++) {
                for (int j = 0; j < res[0].length; j++) {
                        res[i][j] = (int)jsaReading.get(i).asArray().get(j).asDouble();
                }
            }

            return res;
        }
        return new int[0][0];
    }

    public JsonObject toJson() {
        return new JsonObject().add("perceptions", lastPerception);
    }

    public Ole toOle() {
        return new Ole(toJson());
    }

    public void fromJson(JsonArray jsareading) {
        clear();
        for (int i = 0; i < jsareading.size(); i++) {
            JsonObject jsosensor = jsareading.get(i).asObject();
            String name = jsosensor.getString("sensor", "");
            setSensor(name, jsosensor.get("data").asArray());
        }
        ready = true;
    }

    public void fromOle(ArrayList<Ole> oreading) {
        clear();
        for (Ole osensor : oreading) {
            String sensorname = osensor.getField("sensor");
            setSensor(sensorname, Transform.toJsonArray(new ArrayList(osensor.getArray("data"))));
        }
        ready = true;
    }

    public String getName() {
        if (this.isReady()) {
            return name;
        } else {
            return "";
        }
    }

    public String getSession() {
        if (this.isReady()) {
            return sessionID;
        } else {
            return "";
        }
    }

    public void feedPerception(String content) {
        JsonObject jsoperception = Json.parse(content).asObject();
        name = jsoperception.getString("name", "unknown");
        sessionID = jsoperception.getString("sessionID", "unknown");
        commitment = jsoperception.getString("commitment", "");
        fromJson(jsoperception.get("perceptions").asArray());
    }

    public String getCommitment() {
        return commitment;
    }

    public void setCommitment(String commitment) {
        this.commitment = commitment;
    }
    
    
    public String [] getSensorList() {
        return this.indexperception.keySet().toArray(new String[this.indexperception.keySet().size()]);
    }
}
