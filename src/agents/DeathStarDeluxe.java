/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import data.Ole;
import data.OleFile;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import messaging.ACLMessageTools;
import swing.LARVAAirTrafficControlTiles;
import swing.LARVAEmbeddedDash;
import swing.LARVAFrame;
import swing.LARVAMiniDash;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class DeathStarDeluxe extends LARVAFirstAgent {

    enum Status {
        CHECKIN, CHECKOUT, IDLE, HASSESSION, UPDATE, EXIT
    }
    Status myStatus;

    protected HashMap<String, LARVAMiniDash> AgentDash;
    protected LARVAAirTrafficControlTiles TheMap;
    protected String sessionKey = "";
    LARVAFrame Registry;
    JTextArea taRegistry;
    JScrollPane spRegistry;
    int nlines = 0, ncaptures = 0, nfound;
    int rwidth = 300, rheight = 600;

    @Override
    public void setup() {
        super.setup();
        logger.offEcho();
        logger.onTabular();
        myStatus = Status.CHECKIN;
        TheMap = new LARVAAirTrafficControlTiles();
        TheMap.setTitle("DEATH STAR");
        Info("Setting Death Star up");
        exit = false;
        taRegistry = new JTextArea();
        taRegistry.setSize(new Dimension(rwidth, rheight));
        taRegistry.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        spRegistry = new JScrollPane(taRegistry);
        spRegistry.setPreferredSize(new Dimension(rwidth, rheight));
        spRegistry.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
        spRegistry.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        spRegistry.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        Registry = new LARVAFrame(e -> this.defaultListener(e));
        Registry.setSize(new Dimension(rwidth + 10, rheight + 10));
        Registry.setLocation(TheMap.getWidth(), 0);
        Registry.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Registry.setVisible(true);
        Registry.add(spRegistry);
        Registry.pack();
        Registry.setTitle("Death Star DLX Registry +");
        Registry.setVisible(true);
        Registry.show();
    }

    @Override
    public void Execute() {
        Info("Status: " + myStatus.name());
        switch (myStatus) {
            case CHECKIN:
                myStatus = MyCheckin();
                break;
            case IDLE:
                myStatus = myIdle();
                break;
            case CHECKOUT:
                myStatus = MyCheckout();
                break;
            case EXIT:
            default:
                exit = true;
                break;
        }

    }

    @Override
    public void takeDown() {
        Info("Taking down and deleting agent");
        super.takeDown();
    }

    public Status MyCheckin() {
        Info("Loading passport and checking-in to LARVA");
        if (!loadMyPassport("passport/MyPassport.passport")) {
            Error("Unable to load passport file");
            return Status.EXIT;
        }
        if (!doLARVACheckin()) {
            Error("Unable to checkin");
            return Status.EXIT;
        }
        this.DFSetMyServices(new String[]{"DEATHSTAR " + userID});
        this.setTitle();
        TheMap.clear();
        nlines = 0;
        ncaptures = 0;
        nfound = 0;
        taRegistry.setText("");
        return Status.IDLE;
    }

    public Status MyCheckout() {
        this.doLARVACheckout();
        return Status.EXIT;
    }

    public Status myIdle() {
        inbox = this.LARVAblockingReceive();
        Info("Received: " + ACLMessageTools.fancyWriteACLM(inbox, false));
        if (inbox.getContent().contains("filedata")) {
            this.sessionKey = inbox.getConversationId();
            Ole ocontent = new Ole().set(inbox.getContent());
            OleFile ofile = new OleFile(ocontent.getOle("surface"));
            int maxlevel = ocontent.getInt("maxflight");
            TheMap.clear();
            nlines = 0;
            ncaptures = 0;
            nfound=0;
            taRegistry.setText("");
            TheMap.setWorldMap(ofile.toString(), maxlevel, ocontent.getField("palette"));
            this.setTitle();
        }
        if (inbox.getContent().contains("perceptions")) {
            TheMap.feedPerception(inbox.getContent());
            this.checkCommitments();
        }
        return Status.IDLE;
    }

    protected void checkCommitments() {
//        doSwingWait(() -> {
        for (LARVAEmbeddedDash ed : TheMap.getDashboards().values()) {
            String scomm, sreg = "";
            scomm = ed.getStatus();
            if (scomm.contains("Captured ")) {
                if (!taRegistry.getText().contains(scomm)) {
                    sreg = " 🚹x" + (++this.ncaptures) + "    " + ed.getName() + " " + scomm;
                    taRegistry.setText(taRegistry.getText() + sreg + "\n");
                    Registry.validate();
                    Registry.repaint();
                }
            }
            scomm = ed.getMyCommitment();
            if (!scomm.equals("")) {

                if (!taRegistry.getText().contains(scomm)) {
                    if (scomm.startsWith("MOVE")) {
                        sreg = " 🔄x" + (++nlines) + "    " + ed.getName() + " accepts " + scomm;
                    } else if (scomm.startsWith("FOUND")) {
                        sreg = " 👁x" + (++nfound) + "   " + ed.getName() + " " + scomm;
                    } else {
                        sreg = " 🔄x" + (++nlines) + "  " + ed.getName() + " accepts " + scomm;

                    }
                    taRegistry.setText(taRegistry.getText() + sreg + "\n");
                    Registry.validate();
                    Registry.repaint();
                }
            }
        }
//        });
    }

    protected void setTitle() {
        this.TheMap.setTitle("| DEATH STAR DLX |" + userName + " | " + sessionKey + " |");
    }

    public void defaultListener(ActionEvent e) {

    }
}
