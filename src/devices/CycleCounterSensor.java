/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class CycleCounterSensor extends DeviceSensor{
    int counter;
    
    public CycleCounterSensor(String id) {
        super(id);
        setType(SensorType.CYCLE);
        setCounter(0);
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
    @Override
    public void setLastRead(String nextread) {
         super.setLastRead(nextread);
         setCounter(getCounter()+1);
   }

    @Override
    public String read() throws Exception {
        setLastRead(""+getCounter());
        return getLastRead();
    }
    @Override
   public boolean isWarnOn(String preRead, String nextRead){
        return false;
    }

    @Override
    public boolean isWarnOff(String preRead, String nextRead){
        return false;
    }    
}
