/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import data.Transform;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import java.util.ArrayList;
import java.util.HashMap;
import tools.ReportableObject;
import tools.TimeHandler;

/**
 *
 * @author lcv
 */
public class BehaviourControl implements ReportableObject{
    protected HashMap <String, Behaviour> _register;
    protected HashMap <String, Integer> _counter;
    protected HashMap <String, String> _start;
    protected HashMap <String, String> _end;
    protected HashMap <String, String> _description;
    protected Agent _owner;
    
    public BehaviourControl(Agent a) {
        _register = new HashMap();
        _counter = new HashMap();
        _end = new HashMap();
        _start = new HashMap();
        _description = new HashMap();
        _owner=a;
    }
    
    public ArrayList<String> getAllBehaviourNames() {
        return new ArrayList(_counter.keySet());
    }
    
    public Behaviour getBehaviour(String name) {
        return _register.get(name);        
    }
    
    public String registerBehaviour(Behaviour b, String name, String description) {
        String res="";
        b.setBehaviourName(name);
        _register.put(b.getBehaviourName(), b);
        _counter.put(b.getBehaviourName(), 0);
        _end.put(b.getBehaviourName(), "");
        _description.put(b.getBehaviourName(), description);
        _owner.addBehaviour(b);
        res = "Registering behaviour "+name+" ("+description+")";
        return res;
    }
    
    public String unregisterBehaviour(Behaviour b) {
        String res="";
        res = "Unregistering behaviour "+b.getBehaviourName()+" ("+_description.get(b.getBehaviourName())+")";
        _register.remove(b.getBehaviourName(), b);
//        _counter.remove(b.getBehaviourName(), 0);
//        _end.remove(b.getBehaviourName(), "");
        _owner.removeBehaviour(b);
        return res;
    }

    public String unregisterBehaviour(String name) {
        Behaviour b = _register.get(name);
        if (b != null)
            return unregisterBehaviour(b);
        else
            return "Behaviour "+name+" not found";
    }
    
    public int size() {
        return _register.keySet().size();
    }
    
    public boolean isEmpty() {
        return size() == 0;
    }
    
    public long getLatency(String behaviourname){
        TimeHandler start=new TimeHandler(_end.get(behaviourname)), end=new TimeHandler(_end.get(behaviourname)), now =new TimeHandler();
        if (start.isBeforeEq(end)) {
            return end.elapsedTimeSecs(start);
        } else {
            return now.elapsedTimeSecs(start);            
        }
        
    }
    public boolean isOK(String behaviourname) {
        return getLatency(behaviourname)<5;
    }
    
    public void startBehaviour(Behaviour b) {
        _start.put(b.getBehaviourName(), TimeHandler.Now());
    }
    public void endBehaviour(Behaviour b) {
        _counter.put(b.getBehaviourName(),_counter.get(b.getBehaviourName())+1);
        _end.put(b.getBehaviourName(), TimeHandler.Now());
    }
//    public void tickBehaviour(Behaviour b) {
//        if (_counter.get(b.getBehaviourName()) == null) {
//            registerBehaviour(b, b.getBehaviourName(),"");
//        }
//        endBehaviour(b);
//    }
//
//    public void tickBehaviour(String name) {
//        Behaviour b = _register.get(name);
//        if (b!= null)
//            tickBehaviour(b);
//    }

    @Override
    public String defReportType() {
        return "Behaviour Report";
    }

    @Override
    public String[] defReportableObjectList() {
        return Transform.toArrayString(this.getAllBehaviourNames());
    }

    @Override
    public String reportObjectStatus(String objectid) {
        String res=" count "+_counter.get(objectid)+" last "+new TimeHandler(_end.get(objectid)).toString(new TimeHandler());
            res += " duration "+this.getLatency(objectid);            
        return res;
    }
    
    
}
