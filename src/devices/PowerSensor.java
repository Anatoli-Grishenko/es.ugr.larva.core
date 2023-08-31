package devices;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import data.OleFile;
import devices.DeviceSensor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class PowerSensor extends DeviceSensor {

    public PowerSensor(String name) {
        super(name);
        setType(SensorType.POWER);
    }

    @Override
    public void setLastRead(String nextread) {
        super.setLastRead(nextread);
        PrintStream ps;
        try {
            ps = new PrintStream(new File(folder + getId() + ".txt"));
            ps.println(nextread);
        } catch (FileNotFoundException ex) {
        }
    }

    @Override
    public String read() throws Exception {
        Process proc;
        try {
            proc = Runtime.getRuntime().exec("upower -i /org/freedesktop/UPower/devices/battery_BAT0");

            BufferedReader stdInput = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()));

            String s, msg;
            while ((s = stdInput.readLine()) != null) {
                if (s.contains("state")) {
                    if (s.contains("discharging") || s.contains("pending")) {
                        if (!warn) {
                            warn = true;
                            setTimeLastRead(TimeHandler.Now());
                        }
                        setLastRead("OFF");
                    } else if (s.contains("charging") || s.contains("charged")){
                        if (warn) {
                            warn = false;
                            setTimeLastRead(TimeHandler.Now());
                        }
                        setLastRead("ON");
                    }
                }
            }
        } catch (Exception ex) {
//            new HandlerE
        }

        return getLastRead();
    }

    @Override
    public boolean isWarnOn(String preRead, String nextRead) {
        return preRead.equals("ON") && nextRead.equals("OFF");
    }

    @Override
    public boolean isWarnOff(String preRead, String nextRead) {
        return preRead.equals("OFF") && nextRead.equals("ON");
    }

}
