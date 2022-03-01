/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import data.OleList;
import static data.TokenList.countTokens;

/**
 *
 * @author lcv
 */
// https://apps.timwhitlock.info/emoji/tables/unicode
public class emojis {

    public static final String ROBOT = "🤖",
            KEYBOARD = "⌨️",
            STOP = "🛑",
            ERROR = "🚫",
            REPLY = "↩️",
            MEDICAL = "🆘",
            COMPETENCE = "📐",
            SKULL = "\ud83d\udc80",
            WARNING = "\u26a0",
            OK = "\u2705",
            NOOK = "\u274C",
            ENVELOPE = "\u2709",
            SPARK = "\u2733",
            MAN = "\ud83D\uDEB9",
            PASSPORT = "\ud83d\udec2",
            CHEQFLAG = "\ud83c\udfc1",
            SPEAKER = "\ud83d\udce2",
            KEY = "\ud83d\udd11",
            DIRECTHIT = "\ud83c\udfaf",
            INFO = "\u2139",
            LOCKED = "\ud83d\udd12",
            UNLOCKED = "\ud83d\udd13",
            BABY = "\ud83d\udc25",
            SOS = "\ud83c\udd98",
            NOENTRY = "\ud83d\udeab",
            REDSQUARE = "\ud83d\udfe5",
            ORANGESQUARE = "\ud83d\udfe7",
            YELLOWSQUARE = "\ud83d\udfe8",
            GREENSQUARE = "\ud83d\udfe9",
            BLUESQUARE = "\ud83d\udfe6",
            PURPLESQUARE = "\ud83d\udfeA",
            BLACKSQUARE = "\u25fc",
            WHITESQUARE = "⬜",
            REDCIRCLE = "\ud83d\udd34",
            ORANGECIRCLE = "\ud83d\udfe0",
            YELLOWCIRCLE = "\ud83d\udfe1",
            GREENCIRCLE = "\ud83d\udfe2",
            BLUECIRCLE = "\ud83d\udd35",
            PURPLECIRCLE = "\ud83d\udfe3",
            BLACKCIRCLE = "\u26ab",
            WHITECIRCLE = "\u26aa",
            RADIOACTIVE = "\u2622\uFe0f",
            UPRIGHTARROW = "\u2197\uFE0F",
            DOWNRIGHTARROW = "\u2198\uFE0F",
            HAMMERWRENCH = "\uD83D\uDEE0\uFE0F",
            ANTENNABARS = "\ud83d\udcf6",
            BLACKHEART = "\ud83d\udda4",
            REDHEART = "\u2764",
            GREENHEART = "\ud83d\uDC9A",
            BROKENHEART = "\ud83d\uDC94",
            SPARKLINGHEART = "\ud83d\uDC96",
            THUMBUP = "\ud83d\udc4d",
            THUMNDOWN = "\ud83d\udc4e",
            GLOWINGSTAR = "\ud83c\udf1f",
            CLASS="🔲",
            PACKAGE="📦",
            MAGNIFFIER="🔍",
            FOLDER = "📁",
            GEAR = "⚙️",
            MYSELFF = "👦",
            MYGROUP = "👨‍👩‍👧‍👦",
            MYCLASS = emojis.SPEAKER,
            RIGHTARROW = "➡️",
            LEFTARROW = "⬅️",
            CFP = "✋",
            AGREE = "🤝",
            INFORMREF = "👁️",
            MOVE = "🔃",
            CAPTURE = "🚹",
            CANCEL="🚫",
            REQUEST="👉",
            LINEUP="🏁",
            JOIN="👏";
    public static final String AGENT = PASSPORT,
            DEAD = REDCIRCLE,
            ALIVE = GREENCIRCLE,
            LOADING = YELLOWCIRCLE,
            MESSAGE = ENVELOPE,
            MILESTONE = "🏆",
            GOAL = CHEQFLAG,
            BROADCAST = SPEAKER,
            LOGIN = KEY,
            PROBLEM = GEAR,
            SOLVED = OK,
            CALENDAR = "📅",
            ASSIGNMENT = "📝",
            LUDWIG = "\uD83D\udc68";

    public static String showProgress(OleList total, OleList done) {
        String res = "";
        int idone = 0, itotal = 0;
        idone = done.size();
        itotal = total.size();
        res += String.format("%2d/%2d", idone, itotal);
        return res;

    }
//    public static String showProgress(String done, String total) {
//        String res = "";
//        int idone = 0, itotal = 0;
//        idone = countTokens(done);
//        itotal = countTokens(total);
//        res += String.format("%2d/%2d", idone, itotal);
//        return res;
//
//    }

    public static String showProgressBar(int width, OleList total, OleList done, String color) {
        String res = "";
        int idone = 0, itotal = 0;
        idone = done.size();
        itotal = total.size();
        if (itotal <= width) {
            width = itotal;
        }
        for (int k = 0; k < width; k++) {
            if (k < idone * width / itotal) {
                switch (color.toUpperCase()) {
                    case "GREEN":
                        res += emojis.GREENSQUARE;
                        break;
                    case "ORANGE":
                        res += emojis.ORANGESQUARE;
                        break;
                    case "BLUE":
                    default:
                        res += emojis.BLUESQUARE;
                }
            } else {
                res += emojis.WHITESQUARE;

            }
        }
        res += showProgress(total, done);
        return res;
    }
//    public static String showProgressBar(int width, String done, String total) {
//        String res = "";
//        int idone = 0, itotal = 0;
//        idone += countTokens(done);
//        itotal += countTokens(total);
//        if (itotal <= width) {
//            width = itotal;
//        }
//        for (int k = 0; k < width; k++) {
//            if (k < idone *width / itotal) {
//                res += emojis.BLUESQUARE;
//            } else {
//                res += emojis.WHITESQUARE;
//
//            }
//        }
//        res += showProgress(done,total);
//        return res;
//    }

}
