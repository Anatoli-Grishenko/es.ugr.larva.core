/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram;

import static database.OleDataBase.BADRECORD;
import java.util.ArrayList;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 *
 * @author lcv
 */
public class TelegramChat {

    private int userID, groupID, courseID;
    private String username, groupname;
    private boolean teacher, subscribed;
    private ArrayList<Update> pendingUpdates;

    public TelegramChat() {
        resetChat();
    }
    
    public void resetChat() {
        pendingUpdates = new ArrayList<>();
        userID = groupID = BADRECORD;
        teacher=subscribed=false;
    }
    public boolean isValidChat() {
        return true; //(userID != BADRECORD);
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public void setTeacher(boolean teacher) {
        this.teacher = teacher;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public boolean isTeacher() {
        return teacher;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setPendingUpdates(ArrayList<Update> pendingUpdates) {
        this.pendingUpdates = pendingUpdates;
    }

    public int getUserID() {
        return userID;
    }

    public int getGroupID() {
        return groupID;
    }

    public int getCourseID() {
        return courseID;
    }

    public String getUsername() {
        return username;
    }

    public String getGroupname() {
        return groupname;
    }


    public ArrayList<Update> getPendingUpdates() {
        return pendingUpdates;
    }
}
