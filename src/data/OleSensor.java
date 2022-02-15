/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.ArrayList;

/**
 *
 * @author lcv
 */
public class OleSensor extends Ole {

    public OleSensor() {
        super();
        setType(oletype.OLESENSOR.name());
        checkField("sensorname");
        checkField("reading");
    }

    public OleSensor(Ole o) {
        super(o);
        setType(oletype.OLESENSOR.name());
        checkField("sensorname");
        checkField("reading");
    }


    public OleSensor setAllReadings(ArrayList<Double> values) {
        setField("reading",new ArrayList(values));
        return this;
    }

    public ArrayList<Double> getAllReadings() {
        return getArray("reading");
    }

    public String getSensorName() {
        return getField("sensorname");
    }
    public OleSensor setSensorName(String name) {
        setField("sensorname", name);
        return this;
    }
}
