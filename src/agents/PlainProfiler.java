/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import crypto.Keygen;
import data.Ole;
import data.OleTools;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import tools.Internet;
import tools.MonitorBox;
import profiling.NetworkData;
import tools.NetworkCookie;
import zip.ZipTools;
import profiling.Profiling;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class PlainProfiler extends LARVAFirstAgent implements Profiling {
    
//    NetworkData nap;
//    NetworkCookie nc;
//    String netMon = "", myGMap = "https://www.google.com/maps/@37.1441925,-3.5711564,14z";
//    MonitorBox mb;
//    
//    @Override
//    public void setup() {
//        super.setup();
//        nap = new NetworkData("./client/");
//        if (!OleTools.isConfig(nap)) {
//            if (Confirm("¿Das tu permiso para leer tu dirección IP?")) {
//                nap.setExtIP(Internet.getExtIPAddress());
//                nap.setLocalIP(Internet.getLocalIPAddress());
//                if (myGMap != null) {
//                    nap.setMyGMaps(myGMap);
//                }
//            }
//        }
//        OleTools.bootConfig(nap);
//        logger.offEcho();
//        String netmon = DFGetAllProvidersOf("PLAINNETMON").get(0);
//        outbox = new ACLMessage(ACLMessage.SUBSCRIBE);
//        outbox.setSender(getAID());
//        outbox.addReceiver(new AID(netmon, AID.ISLOCALNAME));
//        outbox.setContent("");
//        LARVAsend(outbox);
//    }
//    
//    @Override
//    public void Execute() {
//        inbox = LARVAblockingReceive();
//        switch (inbox.getPerformative()) {
//            case ACLMessage.QUERY_REF:
//                String a =getPackage(inbox);
//                outbox = LARVAcreateReply(inbox);
//                outbox.setPerformative(ACLMessage.INFORM);
//                outbox.setContent(Keygen.getHexaKey(10));
//                outbox = Profiling.hidePong(inbox, outbox, nap);
//                LARVAsend(outbox);
//                break;
//            case ACLMessage.CANCEL:
//                doExit();
//                break;
//        }
//    }
//
//    public String getPackage(ACLMessage incoming) {
//        Ole oRes = new Ole();
//        String content = incoming.getContent();
//        try {
//            if (content.startsWith("ZIPDATA")) {
//                content = ZipTools.unzipString(content.replace("ZIPDATA", ""));
//            }
//        } catch (Exception ex) {
//
//        }
//        return content;
//    }
}