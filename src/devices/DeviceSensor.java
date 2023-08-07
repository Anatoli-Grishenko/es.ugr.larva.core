/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices;

import JsonObject.JsonArray;
import JsonObject.JsonObject;
import JsonObject.JsonValue;
import data.Ole;
import data.OleFile;
import data.Transform;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import map2D.Map2DColor;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class DeviceSensor {

    public static enum SensorType {
        POWER, PICTURE, MOVEMENT, AUDIO, GENERAL, LIGHT, CYCLE, ACTIVE
    };
    final protected String folder = "./readings/";
    protected SensorType type;
    protected String Id, name;
    protected String lastRead;
    protected String timeLastRead;
    protected String lastAlarm, timeLastAlarm;
    protected int history;
    protected boolean warn = false;
    protected ArrayList<String> readHistory;

    public DeviceSensor(String id) {
        setId(id);
        setType(SensorType.GENERAL);
        setName(id);
        setLastRead("");
        setTimeLastRead(TimeHandler.Now());
        setTimeLastWarn("");
        setLastAlarm("");
        setHistory(1);
        setReadHistory(new ArrayList());
    }

    public String read() throws Exception {
        setLastRead("");
        return "";
    }

    public String getId() {
        return Id;
    }

    public void setId(String name) {
        this.Id = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastRead() throws Exception {
        return lastRead;
    }

    public void setLastRead(String nextread) {
        this.lastRead = nextread;
//        if (getHistory() > 1) {
//            addread(nextread);
//        }
    }

    public void addread(String nextread) {
//        if (getReadHistory() != null) {
//            System.err.println(">>>>------" + getName() + "--------->" + getReadHistory().size());
//            if (getReadHistory().size() >= getHistory()) {
//                getReadHistory().remove(0);
//            }
//            getReadHistory().add(nextread);
//        }
    }

    public SensorType getType() {
        return type;
    }

    public void setType(SensorType type) {
        this.type = type;
    }

    public String getTimeLastRead() {
        return timeLastRead;
    }

    public void setTimeLastRead(String timeLastRead) {
        this.timeLastRead = timeLastRead;
    }

    public boolean isWarn() {
        return warn;
    }

    public boolean isWarnOn(String preRead, String nextRead) {
        return !preRead.equals(nextRead);
    }

    public boolean isWarnOff(String preRead, String nextRead) {
        return !preRead.equals(nextRead);
    }

    public String getLastAlarm() {
        return lastAlarm;
    }

    public void setLastAlarm(String lastAlarm) {
        this.lastAlarm = lastAlarm;
        setTimeLastWarn(TimeHandler.Now());
    }

    public String getTimeLastWarn() {
        return timeLastAlarm;
    }

    public void setTimeLastWarn(String timeLastAlarm) {
        this.timeLastAlarm = timeLastAlarm;
    }

    public Ole getStatus() throws Exception {
        Ole res = new Ole();
        res.setField("name", getName());
        res.setField("type", getType().name());
        res.setField("reading", getLastRead());
        res.setField("lastreadingtime", getTimeLastRead());
        res.setField("warn", isWarn());
        if (isWarn()) {
            res.setField("lastwarn", getLastAlarm());
            res.setField("lastwarntime", getTimeLastWarn());
        }
        return res;
    }

    public int getHistory() {
        return history;
    }

    public void setHistory(int history) {
        this.history = history;
        if (readHistory == null) {
            readHistory = new ArrayList();
        }
//        while (history > readHistory.size()) {
//            readHistory.add("");
//        }
//        while (history < readHistory.size()) {
//            readHistory.remove(readHistory.size() - 1);
//        }
    }

    public String getLastSequence() throws Exception {
        return "";
    }

    public ArrayList<String> getReadHistory() {
        return readHistory;
    }

    public JsonArray getJsonReadHistory() {
        return Transform.toJsonArray(getReadHistory());
    }

    public void setReadHistory(ArrayList<String> readHistory) {
        this.readHistory = readHistory;
    }

    public static boolean hasSensor(Ole sensorReadings, DeviceSensor.SensorType stype) {
        JsonArray jsa = sensorReadings.get("readings").asArray();
        JsonObject josReading;
        String reading, warn, dater, datew, name;
        JLabel jlaux;
        OleFile ofile;
        Map2DColor map;
        for (JsonValue jsV : jsa) {
            josReading = jsV.asObject();
            if (josReading.getString("type", "").equals(stype.name())) {
                return true;
            }
        }
        return false;
    }

    public static String getSensorReading(Ole sensorReadings, SensorType stype) {
        JsonArray jsa = sensorReadings.get("readings").asArray();
        JsonObject josReading;
        String reading, warn, dater, datew, name, rname;
        JLabel jlaux;
        OleFile ofile;
        Map2DColor map;
        if (hasSensor(sensorReadings, stype)) {
            for (JsonValue jsV : jsa) {
                josReading = jsV.asObject();
                if (josReading.getString("type", "").equals(stype.name())) {
                    return josReading.getString("reading", "");
                }
            }
        }
        return "";
    }

    public static String getSensorReadingTime(Ole sensorReadings, SensorType stype) {
        JsonArray jsa = sensorReadings.get("readings").asArray();
        JsonObject josReading;
        String reading, warn, dater, datew, name, rname;
        JLabel jlaux;
        OleFile ofile;
        Map2DColor map;
        if (hasSensor(sensorReadings, stype)) {
            for (JsonValue jsV : jsa) {
                josReading = jsV.asObject();
                if (josReading.getString("type", "").equals(stype.name())) {
                    return josReading.getString("lastreadingtime", "");
                }
            }
        }
        return "";
    }

}
