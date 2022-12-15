/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import console.Console;
import static console.Console.black;
import static console.Console.blue;
import static console.Console.cyan;
import static console.Console.green;
import static console.Console.lightblue;
import static console.Console.lightgreen;
import static console.Console.lightred;
import static console.Console.red;
import static console.Console.white;
import java.util.HashMap;
import java.util.logging.Logger;


/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class SchedulerViewer {
    
    protected TimeTable myTimeTable;
    protected Console myConsole;
    
    public SchedulerViewer(TimeTable myTimeTable) {
        this.myTimeTable = myTimeTable;
    }
    
    public TimeTable getMyTimeTable() {
        return myTimeTable;
    }
    
    public void setMyTimeTable(TimeTable myTimeTable) {
        this.myTimeTable = myTimeTable;
        View();
    }
    
    public void Open() {
        this.myConsole = new Console("SCHEDULE VIEWER", 220, 50, -10);
        myConsole.captureStdInOut().setText(white).setBackground(black).clearScreen();
    }
    
    public void Close() {
        myConsole.waitToClose();
        myConsole.close();
    }
    
    public void View() {
        HashMap<String, Integer> Colors = new HashMap();
        Colors.put("MOVEIN", cyan);
        Colors.put("CAPTURE", lightgreen);
        Colors.put("TRANSFER", Console.lightmagenta);
        Colors.put("STARTING", black);
        int width = 150, makespan = myTimeTable.getMakespan();
        double ratio = width * 1.0 / makespan;
        myConsole.setBackground(white).setText(black).clearScreen();
        int y = 5, x = 1, n = 1, w, left = 7;
        myConsole.printHRulerTop(left, 2, width, 100, makespan);
        for (String sa : myTimeTable.getAllAgents()) {
            myConsole.setBackground(white).setText(black).setCursorXY(1, y).print(sa);
            for (Assignment as : myTimeTable.getAllOCurrences(sa)) {
                int xx = left + (int) (as.getTini() * ratio),
                        ww = (int) (Math.round((as.getTend() - as.getTini()) * ratio));
                String a = as.getAction().split(" ")[0], c = as.getAction().replace(a, "");
                if (as.getAction().startsWith("REQUEST")) {
                    myConsole.setBackground(white).setText(black).setCursorXY(xx, y-1).print(">" + c);
                } else if (as.getAction().startsWith("CANCEL")) {
                    myConsole.setBackground(white).setText(black).setCursorXY(xx, y + 3).print("<" + c);
                } else {
                    if (a.startsWith("MTT")) {
                        myConsole.setBackground(blue).setText(black).
                                doRectangleFrame(xx+2, y, ww-2, 3);
                    } else {
                        myConsole.setBackground(Colors.get(a)).setText(black).
                                doRectangleFrame(xx, y, ww, 3);
                    }
                    if (a.equals("MOVEIN")) {
                        myConsole.setBackground(Colors.get(a)).setText(white).setCursorXY(xx + 1, y).print(c);
                    } else {
                        myConsole.setBackground(Colors.get(a)).setText(black).setCursorXY(xx, y).print("" + as.getCargo());
                    }
                }
            }
            n++;
            y += 5;
        }
        myConsole.println("");
        myConsole.setText(red).setBackground(white).
                setCursorXY(1, 1).println("MAKESPAN: "+myTimeTable.getMakespan()
                        +" (t.u.)      CAPTURES: "+myTimeTable.getNCaptures());
        
    }
    
    public void KeyToCOntinue() {
        myConsole.doPressReturn("Press RETURN to coontinue");
    }
    
    public Console getConsole() {
        return myConsole;
        
    }
}
