/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author lcv
 */
public class TimeHandler {

    public static final DateTimeFormatter inputdateformat = DateTimeFormatter.ofPattern("uuuu-MM-dd kk:mm:ss:SSS"),
            outputdateformat = inputdateformat, inputolddateformat = DateTimeFormatter.ofPattern("dd/MM/uuuu kk:mm:ss:SSS"),
            outputolddateformat = inputdateformat;
    public static final TimeHandler _baseTime=new TimeHandler("2020-01-01 00:00:00");
    public static String Now(){return new TimeHandler().toString();}

    protected LocalDateTime _theTime;

    public TimeHandler() {
        _theTime = LocalDateTime.now();
    }

    public TimeHandler(long l) {
        _theTime =_baseTime._theTime.plusSeconds(l);
    }
    
    public TimeHandler plusSeconds(long s) {
        _theTime = _theTime.plusSeconds(s);
        return this;
    }
    
    public TimeHandler minusSeconds(long s) {
        _theTime = _theTime.minusSeconds(s);
        return this;
    }
    
    public TimeHandler(String stime) {
        try {
            _theTime = LocalDateTime.parse(stime, inputdateformat);
        } catch (DateTimeParseException ex) {
            try {
                _theTime = LocalDateTime.parse(stime, inputolddateformat);
            } catch (DateTimeParseException ex2) {
                _theTime = new TimeHandler()._theTime;
            }
        }

    }

    public boolean isAfterEq(TimeHandler t) {
        return _theTime.isAfter(t._theTime) || _theTime.isEqual(t._theTime);
    }

    public boolean isBeforeEq(TimeHandler t) {
        return _theTime.isBefore(t._theTime) || _theTime.isEqual(t._theTime);
    }

    public boolean isEqual(TimeHandler t) {
        return _theTime.isEqual(t._theTime);
    }
    
    public long elapsedTimeSecs(TimeHandler other) {
        Duration res = Duration.between(_theTime, other._theTime);
        
        return res.getSeconds();
    }

    public long elapsedTimeSecs() {
        Duration res = Duration.between(_baseTime._theTime, _theTime);
        
        return res.getSeconds();
    }

    @Override
    public String toString() {
        return outputdateformat.format(_theTime);
    }
    
    public String toString(TimeHandler other) {
        String res;
        long secs = this.elapsedTimeSecs(other);
        if (secs <20 )
            res = ""+secs+" s"; //Less than one minute ago";
        else if (secs <60 )
            res = " < 1 min"; //Less than one minute ago";
        else if (secs <3600 )
            res = "< "+secs/60+" mins"; //Less than "+secs/60+" minutes ago";
        else if (secs <24*3600 )
            res = "< "+secs/3600+" hours";//"Less than "+secs/3600+" hours ago";
        else if (secs <24*3600*7 )
            res = "< "+secs/(3600*24)+" days";//"Less than "+secs/(3600*24)+" days ago";
        else 
            res = "< "+secs/(3600*24*7)+" weeks";
        return res;
    }
}
