/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import data.OleConfig;
import swing.OleApplication;

/**
 * @brief Bulk of objects to initialize each agent to link it to its parent
 * application
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class BootPayload {

    OleApplication parent;
    OleConfig olecfg;

    public BootPayload(OleApplication parent, OleConfig olecfg) {
        this.parent = parent;
        this.olecfg = olecfg;
    }

    public BootPayload() {
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

}
