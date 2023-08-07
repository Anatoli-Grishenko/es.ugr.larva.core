/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import JsonObject.JsonObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import swing.SwingTools;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class ExceptionHandler {

    public ExceptionHandler(Exception ex) {
        StringWriter sexc = new StringWriter();
        PrintWriter psexc = new PrintWriter(sexc);
        ex.printStackTrace(psexc);
        System.err.println("uncaught-exception\n"+ex.toString()+
                "\n"+"info"+ sexc.toString());
        if (SwingTools.Confirm("uncaught-exception\n"+ex.toString()+
                "\n"+"info"+ sexc.toString()+ 
                "\n\n\nDo you want to close program right now?")) {
            System.exit(1);
        }
    }
}
