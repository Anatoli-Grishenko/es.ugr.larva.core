/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class TradeFederation extends LARVAFirstAgent {
//
//    enum Status {
//        CHECKIN, CHECKOUT, IDLE, HASSESSION, UPDATE, EXIT
//    }
//    Status myStatus;
//    LARVAFrame myFrame;
//    JTextArea taCommitments, taSightings, taCaptures;
//    JLabel lCommitments, lSightings, lCaptures;
//    protected String sessionKey="";
//    int width=300, height;
//
//    @Override
//    public void setup() {
//        super.setup();
//        logger.offEcho();
//        logger.onTabular();
//        myStatus = Status.CHECKIN;
//        Info("Setting Trade Federation");
//        myFrame = new LARVAFrame(null);
//        myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        myFrame.setResizable(false);
//
//
//        exit = false;
//    }
//
//    @Override
//    public void Execute() {
//        Info("Status: " + myStatus.name());
//        switch (myStatus) {
//            case CHECKIN:
//                myStatus = MyCheckin();
//                break;
//            case IDLE:
//                myStatus = myIdle();
//                break;
//            case EXIT:
//            default:
//                exit = true;
//                break;
//        }
//
//    }
//
//    @Override
//    public void takeDown() {
//        MyCheckout();
//        Info("Taking down and deleting agent");
//        super.takeDown();
//    }
//
//    public Status MyCheckin() {
//        Info("Loading passport and checking-in to LARVA");
//        if (!loadMyPassport("passport/MyPassport.passport")) {
//            Error("Unable to load passport file");
//            return Status.EXIT;
//        }
//        if (!doLARVACheckin()) {
//            Error("Unable to checkin");
//            return Status.EXIT;
//        }
//        this.DFSetMyServices(new String[]{"DEATHSTAR " + userID});
//        this.setTitle();
//        return Status.IDLE;
//    }
//
//    public Status MyCheckout() {
//        this.doLARVACheckout();
//        return Status.EXIT;
//    }
//
//    public Status myIdle() {
//        inbox = this.LARVAblockingReceive();
//        Info("Received: "+ACLMessageTools.fancyWriteACLM(inbox, false));
//        if (inbox.getContent().contains("filedata")) {
//            this.sessionKey = inbox.getConversationId();
//            Ole ocontent = new Ole().set(inbox.getContent());
//            OleFile ofile = new OleFile(ocontent.getOle("surface"));
//            int maxlevel = ocontent.getInt("maxflight");
//            TheMap.clear();
//            TheMap.setWorldMap(ofile.toString(), maxlevel, ocontent.getField("palette"));            
//            this.setTitle();
//        }
//        if (inbox.getContent().contains("perceptions")) {
//            TheMap.feedPerception(inbox.getContent());            
//        }
//        return Status.IDLE;
//    }
//    
//    
//    protected void setTitle() {
//        this.TheMap.setTitle("| DEATH STAR |"+userName+" | "+sessionKey+" |");
//    }
}
