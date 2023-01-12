/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
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
        }
        return res;
    }

    public static String getLocalIPAddress() {
        String res = "";
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            res = socket.getLocalAddress().getHostAddress();
        } catch (Exception ex) {
        }
        return res;
    }

    public static boolean isBehindRouter() {
        return !getExtIPAddress().equals(getLocalIPAddress());
    }

//    public static String getGeoBind(String ip) {
//        URL url;
//        String res="";
//        try {
//            url = new URL("http://freegeoip.net/csv/" + ip);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.connect();
//
//            InputStream is = connection.getInputStream();
//
//            int status = connection.getResponseCode();
//            if (status != 200) {
//                return null;
//            }
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//            for (String line; (line = reader.readLine()) != null;) {
//                //this API call will return something like:
////            "2.51.255.200" ,"AE","United Arab Emirates","03","Dubai","Dubai","","x-coord","y-coord","",""
//                // you can extract whatever you want from it
//            }
//        } catch (Exception ex) {
//        }
//        return res;
//    }
}
