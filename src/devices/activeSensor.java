/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.BooleanSupplier;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class activeSensor extends DeviceSensor {

    BooleanSupplier bs;

    public activeSensor(String id, BooleanSupplier extBool) {
        super(id);
        setType(SensorType.ACTIVE);
        bs = extBool;
    }

    @Override
    public String read() throws Exception {
        setLastRead("" + bs.getAsBoolean());
        return getLastRead();
    }

}
