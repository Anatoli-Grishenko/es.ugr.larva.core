/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lcv
 */
public class Internet {

    public static String getExtIPAddress() {
        String res = "";
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

            res = in.readLine(); //you get the IP as a String
        } catch (Exception ex) {
            Logger.getLogger(Internet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    public static String getLocalIPAddress() {
        String res="";
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            res = socket.getLocalAddress().getHostAddress();
        } catch (Exception ex) {
            Logger.getLogger(Internet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    
    public static boolean isBehindRouter() {
        return !getExtIPAddress().equals(getLocalIPAddress());
    }
}
