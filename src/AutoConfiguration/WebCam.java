/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AutoConfiguration;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import data.OleFile;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import map2D.Map2DColor;
import tools.ExceptionHandler;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class WebCam extends Sensor {

    protected Webcam sarxoswc;
    protected Map2DColor lastCapture;
    boolean init = false;

    public WebCam(String name) {
        super(name);
        setType(SensorType.PICTURE);
    }

    @Override
    public String read() {
        if (init) {
            if (Capture(false)) {
                return getLastRead();
            }
        } else {
            setLastRead("");

        }
        return getLastRead();

    }

    @Override
    public String getLastRead() {
        lastRead = lastCapture.toString();
        return lastRead;
    }

    public ArrayList<String> getWebCamNames() {
        ArrayList<String> names = new ArrayList();
        for (Webcam w : Webcam.getWebcams()) {
            names.add(w.getName());
        }
        return names;
    }

    public synchronized boolean init(String which) {
        if (sarxoswc == null) {
            if (which == null || which.length() == 0) {
                sarxoswc = Webcam.getDefault();
            } else {
                sarxoswc = Webcam.getWebcamByName(which);
            }
        }
        init = true;
        return true;
    }

    public synchronized boolean Capture(boolean HD) {
        init(getName());
        try {
            try {
                Dimension res;
                if (HD) {
                    res = WebcamResolution.HD.getSize();
                } else {
                    res = WebcamResolution.VGA.getSize();
                }
                sarxoswc.setViewSize(res);
            } catch (Exception ex) {
                new ExceptionHandler(ex);
            }
            System.out.println(getName() + sarxoswc.getViewSize().toString());
            sarxoswc.open();
            BufferedImage im = sarxoswc.getImage();
            sarxoswc.close();
            lastCapture = new Map2DColor(im);
            lastCapture.saveMap("./readings/" + getName() +"_last.png");
            lastCapture.saveMap("./readings/" + getName() + "_"+TimeHandler.Now() + ".png");
            return true;
        } catch (Exception ex) {
            new ExceptionHandler(ex);
            return false;
        }
    }

    public synchronized Map2DColor getCapture() {
        return lastCapture;
    }

}
