/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import data.Ole;
import data.Transform;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class AgentReport {

    TimeHandler timeStamp;
    String agentName, className, ownerName;
    Class agentClass;
    int bufferSize;
    int latency;
    int lastCycle;
    int inBox;
    int outBox;
    ArrayList<Integer> inBoxes;
    ArrayList<Integer> outBoxes;
    ArrayList<Integer> lastCycles;

    public AgentReport(String name, Class c, int bufferSize) {
        this.agentName = name;
        this.agentClass = c;
        this.className = c.getSimpleName();
        this.bufferSize = bufferSize;
        timeStamp= new TimeHandler();
        inBoxes = new ArrayList();
        outBoxes = new ArrayList();
        lastCycles = new ArrayList();
        latency = 10000;
    }

    public void clearData() {
        setLastCycle(0);
        setInBox(0);
        setOutBox(0);
    }
    public void pushData(ArrayList<Integer> buffer, int data) {
        if (buffer.size() == getBufferSize()) {
            buffer.remove(0);
        }
        buffer.add(data);
    }

    public void tick() {
        TimeHandler now = new TimeHandler();      
        pushData(inBoxes, inBox);
        pushData(outBoxes, outBox);
        pushData(lastCycles, lastCycle);
        setLastCycle((int) Math.abs(timeStamp.elapsedTimeMilisecsUntil(now))+getLastCycle());
        setTimeStamp(now);
//        System.out.println("\n"+agentName+"-|-"+this.getLastCycle()+" "+getInBox()+"-"+getOutBox());

    }

    public Ole toOle() {
        Ole res = new Ole();
        Class c = this.getClass();
        ArrayList<Field> myFields,
                fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
        myFields = fullFields;
        for (Field f : myFields) {
            String getterName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
            try {
                Method getter = c.getDeclaredMethod(getterName);
                res.setFieldGeneric(f.getName(), getter.invoke(this));
            } catch (Exception ex) {
            }
        }
        return res;
    }

    public AgentReport fromOle(Ole o) {
        Class c = this.getClass();
        ArrayList<Field> fullFields = new ArrayList(Transform.toArrayList(c.getDeclaredFields()));
        for (Field f : fullFields) {
            String setterName = "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
            Method setter;
            try {
                if (f.getType() == boolean.class) {
                    setter = c.getDeclaredMethod(setterName, boolean.class);
                    setter.invoke(this, o.getBoolean(f.getName()));
                } else if (f.getType() == double.class) {
                    setter = c.getDeclaredMethod(setterName, double.class);
                    setter.invoke(this, o.getDouble(f.getName()));
                } else if (f.getType() == int.class) {
                    setter = c.getDeclaredMethod(setterName, int.class);
                    setter.invoke(this, o.getInt(f.getName()));
                } else if (f.getType().isInstance("")) {
                    setter = c.getDeclaredMethod(setterName, String.class);
                    setter.invoke(this, o.getField(f.getName()));
                }else if (f.getType().isInstance(new ArrayList<Integer>())){
                    setter = c.getDeclaredMethod(setterName, String.class);
                    setter.invoke(this, o.getArray(f.getName()));
                }
            } catch (Exception ex) {
            }
        }
        return this;
    }

//    public Ole toOle() {
//        Ole res = new Ole();
//        res.setField("timeStamp",getTimeStamp().toString());
//        res.setField("agentName",getAgentName());
//        res.setField("agentClass",getAgentClass());
//        res.setField("agentClass",getAgentClass());
//        return res;
//    }
    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int buferSize) {
        this.bufferSize = buferSize;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public int getLastCycle() {
        return lastCycle;
    }

    public void setLastCycle(int lastCycle) {
        this.lastCycle = lastCycle;
    }

    public int getInBox() {
        return inBox;
    }

    public void setInBox(int inBox) {
        this.inBox = inBox;
    }

    public int getOutBox() {
        return outBox;
    }

    public void setOutBox(int outBox) {
        this.outBox = outBox;
    }

    public ArrayList<Integer> getInBoxes() {
        return inBoxes;
    }

    public void setInBoxes(ArrayList<Integer> inBoxes) {
        this.inBoxes = inBoxes;
    }

    public ArrayList<Integer> getOutBoxes() {
        return outBoxes;
    }

    public void setOutBoxes(ArrayList<Integer> outBoxes) {
        this.outBoxes = outBoxes;
    }

    public ArrayList<Integer> getLastCycles() {
        return lastCycles;
    }

    public void setLastCycles(ArrayList<Integer> lastCycles) {
        this.lastCycles = lastCycles;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

   
    public TimeHandler getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(TimeHandler timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Class getAgentClass() {
        return agentClass;
    }

    public void setAgentClass(Class agentClass) {
        this.agentClass = agentClass;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

}
