/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import data.OleConfig;
import data.OlePassport;
import java.awt.Component;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import swing.OleApplication;

/**
 * @brief Bulk of objects to initialize each agent to link it to its parent
 * application
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class LARVAPayload {

    OleApplication parent;
    OleConfig olecfg;
    OlePassport oPassport;
    HashMap <String , Component> guiComponents;
    AgentReport myReport;
    String sessionALias;

    public LARVAPayload(OleApplication parent, OleConfig olecfg) {
        this.parent = parent;
        this.olecfg = olecfg;
    }

    public LARVAPayload() {
    }

    public OleApplication getParent() {
        return parent;
    }

    public void setParent(OleApplication parent) {
        this.parent = parent;
    }

    public OleConfig getOlecfg() {
        return olecfg;
    }

    public void setOlecfg(OleConfig olecfg) {
        this.olecfg = olecfg;
    }

    public OlePassport getoPassport() {
        return oPassport;
    }

    public void setoPassport(OlePassport oPassport) {
        this.oPassport = oPassport;
    }

    public AgentReport getMyReport() {
        return myReport;
    }

    public void setMyReport(AgentReport myReport) {
        this.myReport = myReport;
    }

    public HashMap<String, Component> getGuiComponents() {
        return guiComponents;
    }

    public void setGuiComponents(HashMap<String, Component> guiComponents) {
        this.guiComponents = guiComponents;
    }

    public String getSessionALias() {
        return sessionALias;
    }

    public void setSessionALias(String sessionALias) {
        this.sessionALias = sessionALias;
    }


}
