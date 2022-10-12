/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import com.eclipsesource.json.JsonArray;
import java.util.ArrayList;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class Mission extends ArrayList<String> {

    
    public final static String sepMissions = ";";
    protected String name, goalStatus;
    protected int iGoal;
    
    public Mission(String mission) {
        super();
        String goals[] = mission.split(sepMissions);
        setName(goals[0]);
        for (int i = 1; i < goals.length; i++) {
            this.add(goals[i]);
        }
        iGoal = 0;
        goalStatus="NEW";
    }

    public Mission(String missionName, String goals[]) {
        super();
        setName(missionName);
        for (int i = 0; i < goals.length; i++) {
            this.add(goals[i]);
        }
        iGoal = 0;
        goalStatus="NEW";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        String res = getName() + sepMissions;
        for (String sm : this) {
            res += sm + sepMissions;
        }
        return res;
    }

    public Mission addGoal(String goal) {
        this.add(goal);
        return this;
    }

    public String getCurrentGoal() {
        if (this.isOver()) {
            return "";
        } else {
            return get(iGoal);
        }
    }

    public void nextGoal() {
        if (!isOver()) {
            iGoal++;
            goalStatus="NEW";
        }
    }

    public boolean isOver() {
        return iGoal >= size();
    }
    
    public String getCurrentGoalStatus(){
        return goalStatus;
    }

    public void setCurrentGoalStatus(String status){
        goalStatus=status;
    }
}
