/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import com.eclipsesource.json.JsonArray;
import data.Transform;
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
        iGoal = -1;
        for (int i = 1; i < goals.length; i++) {
            this.add(goals[i]);
            iGoal = 1;
        }
        undefCurrentGoalStatus();
    }

    public Mission(String missionName, String goals[]) {
        super();
        setName(missionName);
        iGoal = -1;
        for (int i = 0; i < goals.length; i++) {
            iGoal = 0;
            this.add(goals[i]);
        }
        undefCurrentGoalStatus();
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
        if (iGoal < 0) {
            iGoal = 0;
        }
        this.add(goal);
        return this;
    }

    public void setCurrentGoal(String goal) {
        if (iGoal < 0) {
            this.addGoal(goal);
        }
        this.set(iGoal, goal);
    }

    public String getCurrentGoal() {
        if (this.isOver()) {
            return "";
        } else {
            if (getCurrentGoalStatus().length() == 0) {
                if (iGoal < 0) {
                    return "";
                } else {
                    return get(iGoal);
                }
            } else {
                return getCurrentGoalStatus();
            }
        }
    }

    public String[] getAllGoals() {
        return Transform.toArrayString(this);
    }

    public void nextGoal() {
        if (!isOver()) {
            iGoal++;
            goalStatus = "";
        }
    }

    public boolean isOver() {
        return iGoal >= size();
    }

    public String getCurrentGoalStatus() {
        return goalStatus;
    }

    public void defCurrentGoalStatus(String status) {
        goalStatus = status;
    }

    public void undefCurrentGoalStatus() {
        goalStatus = "";
    }
}
