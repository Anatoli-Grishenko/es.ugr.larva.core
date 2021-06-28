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
public class TokenList {

    //
    // Old milestones    
    //
    public static boolean findToken(String tokenlist, String token) {
        return tokenlist.contains(token);
    }

    public static int countTokens(String tokenlist) {
        if (tokenlist.contains(" ")) {
            return tokenlist.split(" ").length;
        } else {
            return tokenlist.length() / 7;
        }
    }

    public static String getToken(String tokenlist, int index) {
        String res = "";
        if (0 <= index && index < countTokens(tokenlist)) {
            if (tokenlist.contains(" ")) {
                return tokenlist.split(" ")[index];
            } else {
                res = tokenlist.substring(7 * index, Math.min(7 * (index + 1), tokenlist.length()));
            }
        }
        return res;
    }

    public static String addToken(String tokenlist, String token) {
        if (!findToken(tokenlist, token)) {
            if (tokenlist.contains(" ")) {
                return tokenlist + " " + token;
            } else {
                return tokenlist + token;
            }
        } else {
            return tokenlist;
        }
    }

    public static String removeToken(String tokenlist, String token) {
        if (!findToken(tokenlist, token)) {
            return tokenlist;
        } else {
            if (tokenlist.contains(" ")) {
                return tokenlist.replace(token+" ", "");
            }
            else {
             return tokenlist.replace(token, "");
                
            }
        }
    }
    public static String missingTokens(String partial, String total) {
        String res ="";
        for (int i=0; i< countTokens(total); i++) {
            if (!findToken(partial, getToken(total, i))) {
                res = res + getToken(total, i);
            }                
        }
        return res;
    }
}
