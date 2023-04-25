/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

/**
 *
 * @author lcv
 */
public class TimeHandler {
    public static final String DateTemplate="uuuu-MM-dd kk:mm:ss:SSS";
    public static final DateTimeFormatter inputdateformat = DateTimeFormatter.ofPattern(DateTemplate),
            outputdateformat = inputdateformat, inputolddateformat = DateTimeFormatter.ofPattern("uuuu/MM/dd kk:mm:ss"),
            outputolddateformat = inputdateformat;
    public static final TimeHandler _baseTime = new TimeHandler("2010-01-01 00:00:00:000", DateTemplate);
    public static int timeSlack = Integer.MAX_VALUE;
    public static double secsRatio=3600.0;
    protected LocalDateTime _theTime;

    public static int getTimeSlack() {
        if (!isSynchro()) {
            Synchro();
        }
        return timeSlack;
    }

    public static boolean isSynchro() {
        return timeSlack != Integer.MAX_VALUE;
    }

    public static String Now() {
        return new TimeHandler().toString();
    }

    public static String Now(String DateFormat) {
        return new TimeHandler().toString(DateFormat);
    }

    public static String NetNow() {
        if (!isSynchro()) {
            Synchro();
        }
        return new TimeHandler(getTimeSlack()).toString();
    }

    public static TimeHandler getNetworkTime() {
        String TIME_SERVER = "time-a.nist.gov";
        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress;
        TimeHandler th1, th2;
        try {
            inetAddress = InetAddress.getByName(TIME_SERVER);
            TimeInfo timeInfo = timeClient.getTime(inetAddress);
            long returnTime = timeInfo.getReturnTime();
            Date time = new Date(returnTime);
            th1 = new TimeHandler(time);
            return th1;
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean Synchro() {
        TimeHandler th1, th2;
        if (!isSynchro()) {
            try {
                th1 = getNetworkTime();
                th2 = new TimeHandler();
                timeSlack = (int) th1.elapsedTimeMilisecsUntil(th2);
            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }

    public static TimeHandler nextSecs(long secs) {
        return new TimeHandler().plusSeconds(secs);
    }


    public TimeHandler() {
        _theTime = LocalDateTime.now();
    }

    public TimeHandler(int slackms) {
        _theTime = LocalDateTime.now().minusNanos(slackms * 1000000);
    }

    public TimeHandler plusSeconds(long s) {
        _theTime = _theTime.plusSeconds(s);
        return this;
    }

    public TimeHandler minusSeconds(long s) {
        _theTime = _theTime.minusSeconds(s);
        return this;
    }

    public TimeHandler plusMiliSeconds(long ms) {
        _theTime = _theTime.plusNanos(ms * 1000000);
        return this;
    }

    public TimeHandler minusMiliSeconds(long ms) {
        _theTime = _theTime.minusSeconds(ms * 1000000);
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

    public TimeHandler(String stime, String format) {
        DateTimeFormatter dt = DateTimeFormatter.ofPattern(format);
        try {
            _theTime = LocalDateTime.parse(stime, dt);
        } catch (DateTimeParseException ex) {
            _theTime = new TimeHandler()._theTime;
        }

    }

    public TimeHandler(double ddate) {
        _theTime = new TimeHandler().fromNumber(ddate)._theTime;
    }

    public static TimeHandler getBaseTime() {
        return _baseTime;
    }

//    public static void setBaseTime(TimeHandler _baseTime) {
//        TimeHandler._baseTime = _baseTime;
//    }
//    
    

    public TimeHandler(Date d) {
        try {
            fromDate(d);
        } catch (DateTimeParseException ex) {
        }
    }

    public double toNumber() {
        return getBaseTime().elapsedTimeSecsUntil(this)/secsRatio;
    }
    
    public TimeHandler fromNumber(double ddate) {
        _theTime = _baseTime._theTime.plusSeconds((long)(ddate*secsRatio));
        return this;
    }
    
    public Date toDate() {
        return this.asDate(_theTime);
    }

    public TimeHandler fromDate(Date d) {
        if (d != null) {
            _theTime = this.asLocalDateTime(d);
            return this;
        } else {
            return null;
        }
    }

    public boolean isAfterEq(TimeHandler t) {
        return this.elapsedTimeSecsUntil(t) >= 0;
//        return _theTime.isAfter(t._theTime) || _theTime.isEqual(t._theTime);
    }

    public boolean isBeforeEq(TimeHandler t) {
        return this.elapsedTimeSecsUntil(t) <= 0;
//        return _theTime.isBefore(t._theTime) || _theTime.isEqual(t._theTime);
    }

    public boolean isEqual(TimeHandler t) {
        return _theTime.isEqual(t._theTime);
    }

    public long elapsedTimeSecsUntil(TimeHandler other) {
        Duration res = Duration.between(_theTime, other._theTime);

        return res.getSeconds();
    }

    public long elapsedTimeMilisecsUntil(TimeHandler other) {
        Duration res = Duration.between(_theTime, other._theTime);

        return res.getSeconds() * 1000 + res.getNano() / 1000000;
    }

    public long elapsedTimeSecs() {
        Duration res = Duration.between(_baseTime._theTime, _theTime);

        return res.getSeconds();
    }

    @Override
    public String toString() {
        return outputdateformat.format(_theTime);
    }

    public String toString(String format) {
        DateTimeFormatter dt = DateTimeFormatter.ofPattern(format);
        return dt.format(_theTime);
    }

    public String toString(TimeHandler other) {
        String res;
        long secs = this.elapsedTimeSecsUntil(other);
        if (secs < 20) {
            res = "" + secs + " s"; //Less than one minute ago";
        } else if (secs < 60) {
            res = " < 1 min"; //Less than one minute ago";
        } else if (secs < 3600) {
            res = "< " + secs / 60 + " mins"; //Less than "+secs/60+" minutes ago";
        } else if (secs < 24 * 3600) {
            res = "< " + secs / 3600 + " hours";//"Less than "+secs/3600+" hours ago";
        } else if (secs < 24 * 3600 * 7) {
            res = "< " + secs / (3600 * 24) + " days";//"Less than "+secs/(3600*24)+" days ago";
        } else {
            res = "< " + secs / (3600 * 24 * 7) + " weeks";
        }
        return res;
    }

    protected Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    protected Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    protected LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    protected LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
