/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package world;

import world.Thing.PROPERTY;
import JsonObject.JsonObject;

/**
 *
 * @author lcv
 */

/*
PERCEPTION

ATTACHMENT          SELECTION           PROPERTY             TYPE            POSITION        RANGE           OPERATION
-----------------------------------------------------------------------------------------------------------------------------------------------
ZENITAL             ALL                 POSITION            TYPE            X,Y,Z           W X H           QUERY
FRONTAL             FIRST               ORIENTATION                                                         DISTANCE
LEFT                CLOSEST             SURFACE                                                             ANGLE
RIGHT               INTERN              PRESENCE
                                        REPORT
                                        ENERGY
                                        STATUS
                                        CHANNEL1-10


COMBOS
ATTACHMENT          SELECTION           PROPERTY             TYPE            POSITION        RANGE           OPERATION                   SENSOR
--------------------------------------------------------------------------------------------------------------------------             ----------
*                   ALL                 REPORT              TYPE            -               ALL             QUERY                       AWACS

ZENITAL             ALL                 SURFACE             ENVIRONMENT     XYZ             WxH             QUERY                       RADAR
FRONTAL

ZENITAL             ALL                SURFACE             ENVIRONMENT     XYZ             WxH             DISTANCE                    ELEVATION
FRONTAL

ZENITAL             ALL                 PRESENCE            TYPE            XYZ             WxH             QUERY                       MAGNETIC/DETECTOR
FRONTAL

*                    CLOSEST             POSITION            TYPE           -               -               DISTANCE                    L-GONIO


FRONTAL             CLOSEST             POSITION            TYPE            -               -               ANGLE                       A-GONIO
ZENITAL

*                   ALL                ORIENTATION         ENVIRONMENT     -               -               ANGLE                       COMPASS

*                   ALL                SURFACE             ENVIRONMENT     XYZ             -               DISTANCE                    ALTITUDE


*                   ALL                POSITION            ENVIRONMENT     XYZ             -               DISTANCE                    GPS


*                   INTERN              ENERGY              -               -               -               QUERY                       BATTERY

*                   INTERN              

 */
public class Perceptor {

    public static enum ATTACH {
        ZENITAL, FRONTAL, LEFT, RIGHT, BOTTOM
    }

    public static enum OPERATION {
        QUERY, DISTANCE, ANGLE
    }

    public static enum SELECTION {
        ALL, CLOSEST, INTERN
    }
    public static final int NULLREAD = Integer.MIN_VALUE;
    public static final int RANGE=11, RANGEHQ=21, RENGEDLX=41;

    PROPERTY _property;
    String _type;
    OPERATION _operation;
    SELECTION _selection;
    ATTACH _attachment;
    Thing _refOwner;
    String _name;
    int _range;
    double _sensitivity;

    public Perceptor(String name, Thing w) {
        _name = name;
        _refOwner = w;
        _range = 1;
        _sensitivity = Double.MAX_VALUE;
        _type=null;
    }

    public String getName() {
        return _name;
    }

    public Thing getOwner() {
        return _refOwner;
    }

    public PROPERTY getProperty() {
        return _property;
    }

    public String getType() {
        return _type;
    }

    public OPERATION getOperation() {
        return _operation;
    }

    public ATTACH getAttachment() {
        return _attachment;
    }

    public SELECTION getSelection() {
        return _selection;
    }

    public int getRange() {
        return _range;
    }

    public double getSensitivity() {
        return _sensitivity;
    }
//    public Perceptor(Thing w, PROPERTY c, ACCESS x, OPERATION o, ATTACH a) {
//        _refOwner = w;
//        _property = c;
//        _access = x;
//        _operation = o;
//        _attachment = a;
//    }

    public Perceptor setWhatPerceives(PROPERTY property, String type, SELECTION select) {
        _property = property;
        this.setType(type);
        _selection = select;
        return this;
    }

    public Perceptor setHowPerceives(OPERATION operation, int range) {
        _operation = operation;
        _range = range;
        return this;
    }

    public Perceptor setAttacment(ATTACH attachment) {
        _attachment = attachment;
        return this;
    }

    public Perceptor setSensitivity(double s) {
        _sensitivity = s;
        return this;
    }

    public Perceptor setRange(int r) {
        _range = r;
        return this;
    }

    public Perceptor setType(String type) {
        if (this.getOwner().getWorld().getOntology().isType(type)) {
            _type = type;
        } else {
            _type = this.getOwner().getWorld().getOntology().getRootType();
        }
        return this;
    }

    public JsonObject getReading() {
        JsonObject res = new JsonObject();
        res = this.getOwner().getWorld().getPerception(this);
        return res; //new JsonObject().add("sensor", getName()).merge(res);
    }
}
