/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import crypto.Keygen;
import tools.TimeHandler;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class ACLMPayload  {
    public static enum Payload{PAYLOADCLASS,PAYLOAD,ACLMID,DATE}
    String ACLMID;
    String date;
    
    public ACLMPayload() {
        super();
        setDate(TimeHandler.Now());
        setACLMID(Keygen.getHexaKey(32));
    }

    public String getACLMID() {
        return ACLMID;
    }

    public void setACLMID(String ACLMID) {
        this.ACLMID = ACLMID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String TimeHandler() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
