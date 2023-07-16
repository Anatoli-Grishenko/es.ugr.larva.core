/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AutoConfiguration;

import data.OleFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Sensor {

    public static enum SensorType {
        POWER, PICTURE, MOVEMENT, AUDIO, GENERAL
    };
    SensorType type;
    String name;
    String lastRead;
    String timeLastRead;

    public Sensor(String name) {
        setName(name);
        setType(SensorType.GENERAL);
    }

    public String read() {
        timeLastRead = TimeHandler.Now();
        return "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastRead() {
        return lastRead;
    }

    public void setLastRead(String lastRead) {
        this.lastRead = lastRead;
        switch (getType()) {
            case PICTURE:
                OleFile of = new OleFile();
                if (getLastRead().length() > 0) {
                    of.set(getLastRead());
                    of.saveFile("./readings/");
//                    of.saveAsFile("./readings/", getName() + ".png", false);
                }
                break;
            default:
            case POWER:
                PrintStream ps;

                try {
                    ps = new PrintStream(new File("./readings/" + getName() + ".txt"));
                    ps.println(getLastRead());
                } catch (FileNotFoundException ex) {
                }
                break;

        }

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

    public int getTimeSinceLastRead() {
        if (getLastRead() == null) {
            return -1;
        } else {
            return (int) new TimeHandler(getLastRead()).elapsedTimeSecsUntil(new TimeHandler());
        }
    }

    public void setTimeLastRead(String timeLastRead) {
        this.timeLastRead = timeLastRead;
    }

}
