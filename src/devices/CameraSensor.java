/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package devices;

import devices.DeviceSensor;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import data.OleFile;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import map2D.Map2DColor;
import tools.ExceptionHandler;
import tools.StringTools;
import static tools.StringTools.cureAllFilename;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class CameraSensor extends DeviceSensor {

    protected Webcam sarxoswc;
    protected Map2DColor lastCapture;
    boolean init = false, grayscale = false;

    public CameraSensor(String id) {
        super(id);
        setType(SensorType.PICTURE);
    }

    @Override
    public String read() throws Exception {
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
        return lastRead;
    }

    public static ArrayList<String> getWebCamNames() {
        ArrayList<String> names = new ArrayList();
        for (Webcam w : Webcam.getWebcams()) {
            names.add(w.getName());
        }
        return names;
    }

    public synchronized boolean init() {
        if (sarxoswc == null) {
            sarxoswc = Webcam.getWebcamByName(getId());
        }
        init = true;
        return true;
    }

    public synchronized boolean Capture(boolean HD) throws Exception {
        if (!init()) {
            init();
        }
        Dimension res;
        if (HD) {
            res = WebcamResolution.HD.getSize();
        } else {
            res = WebcamResolution.VGA.getSize();
        }
        sarxoswc.setViewSize(res);

        System.out.println(getId() + sarxoswc.getViewSize().toString());
        sarxoswc.open();
        BufferedImage im = sarxoswc.getImage();
        sarxoswc.close();
        lastCapture = new Map2DColor(im);
        lastCapture.setPenColor(Color.GREEN);
        lastCapture.setFont(new Font("Mono", 12, BOLD));
//            lastCapture = lastCapture.resize(256, -1);
        lastCapture.drawText(20, 20, getName());
//        lastCapture.drawText(20, 40, TimeHandler.Now());
        String filecamname = folder + cureAllFilename(getName()) + "_last.png";
        lastCapture.saveMap(filecamname);
//            lastCapture.saveMap(folder + cureAllFilename(getName() + "_"+TimeHandler.Now()) + ".png");
        OleFile of = new OleFile();
        of.loadFile(filecamname);
        setLastRead(of.toPlainJson().toString());
        return true;
    }

    public synchronized Map2DColor getCapture() {
        return lastCapture;
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    @Override
    public String getLastSequence() throws Exception {
        Map2DColor frame;
        AnimatedGifEncoder gifwriter = new AnimatedGifEncoder();
        gifwriter.start("./readings/" + getName() + ".gif");
        gifwriter.setDelay(1000);
        gifwriter.setRepeat(0);
        int i = 0;
        for (String sseq : getReadHistory()) {
            frame = new Map2DColor();
            OleFile of = new OleFile();
            of.set(sseq);
            of.saveAsFile("./readings/", getName() + "-" + i, true);
            frame.loadMapRaw("./readings/" + getName() + "-" + i + ".png");
            gifwriter.addFrame(toBufferedImage(frame.getColorImage().getScaledInstance(320, 240, 5)));
        }
        gifwriter.finish();
        OleFile ogif = new OleFile();
        ogif.loadFile("./readings/" + getName() + ".gif");
        lastRead = ogif.toPlainJson().toString();
        return lastRead;
    }

//    private void configureRootMetadata(int delay, boolean loop) throws IIOInvalidTreeException {
//
//        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
//        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
//        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
//        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
//        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delay / 10));
//        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");
//
//        IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
//        commentsNode.setAttribute("CommentExtension", "Created by: https://memorynotfound.com");
//
//        IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
//        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
//        child.setAttribute("applicationID", "NETSCAPE");
//        child.setAttribute("authenticationCode", "2.0");
//
//        int loopContinuously = loop ? 0 : 1;
//        child.setUserObject(new byte[]{0x1, (byte) (loopContinuously & 0xFF), (byte) ((loopContinuously >> 8) & 0xFF)});
//        appExtensionsNode.appendChild(child);
//        metadata.setFromTree(metaFormatName, root);
//    }
}
