/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package profiling;

import agents.LARVABaseAgent;
import static crypto.Keygen.getHexaKey;
import data.Ole;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.function.Consumer;
import java.util.function.Supplier;
import messaging.ACLMessageTools;
import swing.OleApplication;
import tools.MonitorBox;
import tools.MonitorRecord;
import tools.NetworkCookie;
import tools.NetworkCookie;
import tools.TimeHandler;
import tools.TimeHandler;
import zip.ZipTools;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public interface Profiling {
//
//    public static enum MonitorPL {
//        MONITORSEND, MONITORRECEIVE, MONITORACCESSPOINT, MONITORCOOKIE, MONITORMARK
//    };
//    public static final int MAXSIZE_BYTES = 1024 * 1024, INITIALSIZE_BYTES = 512, SERIESIZE = 10;
//
////    public static void start(ACLMessage incoming, MonitorBox mb) {
////        plainPing(incoming, mb);
////    }
//    public static String fakeData(int size) {
//        String choices[] = new String[]{
//            "OMDRUNHOQDQRMEMLGENKTUVSMFZMOSBWMPZXOVXYPZWVUJPZUJDPWTLECOJDDERYSMIROJUJEVFRVBNFMOORROXPMPCYQPOIKGUQLKHQYBFTXFTIHHEVQYTGDINGZFZLJPVRPVDHPUKSCITLHGHYUJCMJIXKKDJHXNJUHLUEZPVVTTMHTNGCPLECWNVENRXTKRIOWTQOXOONXPPCVPKMOPREXSCUMOLDFNKWUGKTZZWPJHVLWMECMKQVLOPUJJYN",
//            "FLSQUOWBPZDEZVNVOTTSQKWQEOENOEJGFDHEEFIXNGPFBMOLDOUDMOQPMELNWBNRVIVIQJJYIHUHNYLLOMLTLMPOQFFRZVJFPVDGISPHGWSMCSOQQIKGWNXTHZGUIZKLDNEJWORVMPTRYVPTMLSQTOUZESROSFJNPYWHYBZOOFLTHQGRYROPUNYGEWPOWMMIXWEKZUCQIWTRRFDSZNOYPLPFOTUEVMHSHIPKYJGFXPQZTHWNKQHFSMUZFOVDWYLS",
//            "XPLGZMEHXJOOONRLOMOYTCNNXVMJSODZKCNGGLEUGBCFLVWCWTQUILPPVDDNBPOKLLWLGWGXLLFNUBFLBHTGKMOVBGQLTFEJBWFHFUVTTNMXTRDPRWMVWFLKTYOGJQPQTJULWINMIVPDKOLMKCUBRZZKKWIPRCBJIRRDONXUFJUQNGFDVEPMILPYGUQQQZYYFOCGTGFCIILOVCQSHKZORFMOYJTGPTZEYOUERDOODSFGFUQRBUEFJKWRYVDQJFRH",
//            "JNKPYWPGVPVSYOKBRUDOSVITBNUIJBYIYJDUZJJYUWIEWNYXCGOZPZLXPTFYYKIIBNIRHOOLXJPLFPUPTVIQFSVTSURWHQTTJNDVRJJMTWMDTJUMSRNDBQGPRPUNVGTHUYKEGSWOOYDTRQXJHBJPINUERVMLYIIORYOCBKISNMVIYWEHGJZDNCZMXNSBWSOWRZOVOFPIPQQQQUMDJQTZOUJFTOGHSNOFUNOKSVFREGKWZGOYYEINVKNSEZQZDFDD",
//            "TNTZOTIKHDNFCJLGCUBJFXCEPMVYLQZVMRGOSCLHYDFLMZULGRXDYVSDVKLQHDOKXFYVGXKDWSFWOOVWIEPSQUMTKXOUBOOZCPWISGXOFRQEYGDCUHOKVVUOTCFWRDJBKEMTXKMWNFOKLFEQDWGFKZXNLOBBGUMSFZGVCKHOQVMOJEGJMMTWFVMOQIMZTSYSMBPZNFMIRNPNKILPPRZXVOQGWZJMHHOFGVBKCGWVJTERPGBYCNGQXEBNYBPWPOJW",
//            "HUORNLQWUCOECEGCNPOXEWHGHPHVLJTWGPCJYDGCFVOOOBUNRUURCYOEKZLNUXCBDVSROKUIDHCCNRFSWXHGTPRZKKOZSOUGLYKWKVTMPOTJOECVPYGHDFDXMLUMBXJIONTPYGNUGPTUJISHPCHOBQJJRTNOPUJVWQJIFQORHOPMMZCFWREJZKTMPBRCRGERONWMPYIPRNLVOUNOHPSEYZKBCSHKPUOCFOYLVFOWPWIJTWRZZUIELRYBGXFBODRQ",
//            "OKWBLMOOTLDBKFOIQBBDUPPBNNSPYNHGDOVZLGCNQCJUPTTZOBRGVQKSSNXSNHEIHPIDCIZUDJIDOPHGNTUXSKCCWSOKIRYFHBNBYSQNOZRYMJCUQXRHWGOZDMVQESEZPNQCSNRJHDBZKLQQEXFJMBSVGZPGHVDOQSOKGRYSZGYEOSSHFTGEBQEGQNVVIVTQDDMINSCLOBDFBWNTCOYODZXMUDDIGIFKMQPBNCRISWRRJMCUXNXPOHLWLKMHYBRE",
//            "ECSMOYLKGBEHODRDRVPWLKOGMSVYVXYJWOQMCPVEENGBIPZYGRHOOOFZCRLGSWMUSPMCSMSCCIXGXBXZWCRUXRTJQMRZVFMEEJGGWYSJQEEIQIDOZHTDGBQPMZKSOXQQFJQQSXZDBLJXSKKVWOTSGCYYNTWHPWOCYWOCHHIMZUIQWWSNBNEYNZGHJOOXLMRZRHROLWIKNFQLRLOQOCPDTVEHGOYVUGEGPXHRPLEDZYYRMTIUWEKTIPXEVZNNWWNR",
//            "EOYPICCMWORODWHKNNODQRMYZODRCPTFUONWBRSFJXYVPJHEOXBBGSKTLBMITXLRQSXQQDFCWEWUFZMIJDKUZOKQFXEWSROBOBFZVSPJBDXWPWOINSFNQWZFGKOFBDFIDDEYPLBFQDOSRJUMNXOPWYOGVFUYICNDCOOOMNLOEUKFXLCNQOWNYBFPYIKBCOORDGNZNHWVGBBKSPVIBBQVSCLOGGUPJJRIPBGBYHGYOCWHMRZYPWYKLPNNOGJVPLSE",
//            "OJOYTMTHEHQEIULMWOYYGNXBOECSXYBIBSMHPGOJLPHZOBUZFQMURLTKIXQEISMUTOHYHMYLEXEDCWRXZXFZTVYKTDWELGHIDFGBTRJJZVRGIHZTTZTJRLFTPZZOLJNRESNNKGUKWOSMWRJURSJDIHOSZLXUEZYDWUKRJWUQEOVELPFOZXOTXTPZMHSZOJYHBEHDHXEQLKEYMCRGJWWUVNOTIFPGXVNUKOTRMVSBGCDMWQFUOVXKNQQKWIWGSWIZ"};
////        String res = getHexaKey(size);
//        String res = choices[(int) (Math.random() * choices.length)];
//        while (res.length() < size) {
//            res = res + choices[(int) (Math.random() * choices.length)];
//        }
//        return res;
//    }
//
//    public static boolean isMonitor(ACLMessage msg) {
//        if (msg == null) {
//            return false;
//        } else {
//            return msg.getAllUserDefinedParameters().get(MonitorPL.MONITORMARK.name()) != null;
//        }
//
//    }
//
//    public static ACLMessage markMonitor(ACLMessage msg) {
//        msg.addUserDefinedParameter(MonitorPL.MONITORMARK.name(), "TRUE");
//        return msg;
//    }
//
//    public static ACLMessage hideSubscribe(ACLMessage msg, NetworkData nap) {
//        Ole oAccessPoint = Ole.objectToOle(nap);
//        msg.addUserDefinedParameter(MonitorPL.MONITORACCESSPOINT.name(), oAccessPoint.toPlainJson().toString());
//        msg = markMonitor(msg);
//        return msg;
//    }
//
//    public static void plainSubscribe(LARVABaseAgent ba, NetworkData nap, MonitorBox mb) {
//        String netmon = ba.DFGetAllProvidersOf("NETMON").get(0);
//        ACLMessage outbox = new ACLMessage(ACLMessage.SUBSCRIBE);
//        outbox.setSender(ba.getAID());
//        outbox.addReceiver(new AID(netmon, AID.ISLOCALNAME));
//        outbox.setContent("");
//        outbox = hideSubscribe(outbox, nap);
//        mb.getDoSend().accept(outbox);
//    }
//
//    public static ACLMessage hidePong(ACLMessage incoming, ACLMessage outgoing, NetworkData nap) {
//        return hidePong(incoming, outgoing, nap, "");
//    }
//
//    public static ACLMessage hidePong(ACLMessage incoming, ACLMessage outgoing, NetworkData nap, String description) {
//        String th = TimeHandler.Now();
//        NetworkCookie nc = extractCookie(incoming);
//        if (incoming == null || nc == null) {
//            return outgoing;
//        } else {
//            nc.settReceive(TimeHandler.Now());
//            nc.setOwner(outgoing.getSender().getLocalName());
//            nc.setAccessPoint(Ole.objectToOle(nap).toPlainJson().toString());
//            nc.setDescription(description);
//        }
//        injectCookie(outgoing, nc);
//        return outgoing;
//    }
//
//    public static ACLMessage hidePing(ACLMessage outgoing) {
//        outgoing = injectCookie(outgoing, null);
//        return outgoing;
//    }
//
//    public static MonitorRecord checkPong(ACLMessage incoming) {
//        NetworkCookie nc = extractCookie(incoming);
//        MonitorRecord mr = null;
//        NetworkData nap = new NetworkData();
//        try {
//            if (nc != null) {
//                if (nc.gettReceive().length() > 0 && nc.getAccessPoint().length() > 0) {
//                    Ole oNap = new Ole(nc.getAccessPoint());
//                    Ole.oleToObject(oNap, nap, NetworkData.class);
//                    mr = new MonitorRecord();
//                    mr.setDate(nc.gettSend());
//                    mr.setDistrict(nap.getMyDistrict());
//                    mr.setTown(nap.getMunicipio());
//                    mr.setId(nc.getID() + "," + nc.getDescription());
//                    mr.setOwner(nc.getOwner());
//                    mr.setIp((nap.getExtIP()));
//                    mr.setgMapOwner(nap.getMyGMaps());
//                    mr.setSerie(nc.getSerie());
//                    mr.setSize(nc.getSize());
//                    mr.setRealSize(nc.getRealSize());
//                    mr.setLatency((int) nc.getLatency());
//                    mr.setZipped(nc.isZipped());
//                }
//            }
//        } catch (Exception ex) {
//        };
//        return mr;
//    }
//
//    public static ACLMessage injectCookie(ACLMessage outgoing, NetworkCookie mync) {
//        if (extractCookie(outgoing) != null) {
//            outgoing.removeUserDefinedParameter(MonitorPL.MONITORCOOKIE.name());
//        }
//        NetworkCookie nc;
//        if (mync == null) {
//            nc = new NetworkCookie();
//            nc.setID(getHexaKey(16));
//            nc.setOwner("");
//            nc.setPayload("");
//            nc.setSize(outgoing.getContent().length());
//            nc.setSerie(0);
//            nc.setScale(1);
//            nc.setRealSize(INITIALSIZE_BYTES * nc.getScale());
//            nc.setAccessPoint("");
//            nc.setZipped(ACLMessageTools.isZipped(outgoing));
//            nc.settSend(TimeHandler.Now());
//        } else {
//            nc = mync;
//            nc.setZipped(ACLMessageTools.isZipped(outgoing));
//        }
//        outgoing = markMonitor(outgoing);
//        outgoing.addUserDefinedParameter(MonitorPL.MONITORCOOKIE.name(), Ole.objectToOle(nc).toPlainJson().toString());
//        return outgoing;
//    }
//
//    public static NetworkCookie extractCookie(ACLMessage incoming) {
//        NetworkCookie nc = new NetworkCookie();
//        Ole oCookie;
//        if (isMonitor(incoming)) {
//            String hiddencookie = incoming.getUserDefinedParameter(MonitorPL.MONITORCOOKIE.name());
//            if (hiddencookie != null) {
//                oCookie = new Ole(hiddencookie);
//                try {
//                    Ole.oleToObject(oCookie, nc, NetworkCookie.class);
//                    nc.setZipped(ACLMessageTools.isZipped(incoming));
//                } catch (Exception ex) {
//                    return null;
//                }
//                return nc;
//            } else {
//                return null;
//            }
//        } else {
//            return null;
//        }
//    }

}

//    public static void plainPong(ACLMessage incoming, MonitorBox mb) {
//        String th = TimeHandler.Now();
//        ACLMessage outbox;
//        outbox = incoming.createReply();
//        NetworkCookie nc = extractCookie(incoming);
//        if (nc != null) {
//            outbox.setPerformative(ACLMessage.INFORM);
//            nc.setPayload("");
//        }
//        if (nc != null) {
//            String sContent = Ole.objectToOle(nc).toPlainJson().toString();
//            if (nc.isZipped()) {
//                outbox.setContent("ZIPDATA" + ZipTools.zipToString(sContent));
//            } else {
//                outbox.setContent(sContent);
//            }
//            outbox.setReplyWith(TimeHandler.Now());
//            mb.getDoSend().accept(outbox);
//        }
//    }
//
//    public static void plainPing(ACLMessage incoming, MonitorBox mb) {
//        ACLMessage outbox = incoming.createReply();
//        String chars;
//        outbox.setPerformative(ACLMessage.QUERY_REF);
//        NetworkCookie nc = null;
//        if (incoming.getPerformative() == ACLMessage.SUBSCRIBE) {
//            nc = new NetworkCookie();
//            nc.setID(getHexaKey(16));
//            nc.setOwner(incoming.getSender().getLocalName());
//            nc.setSerie(0);
//            nc.setZipped(false);
//            chars = fakeData(INITIALSIZE_BYTES);
//            nc.setPayload(chars);
//            nc.setSize(nc.getPayload().length());
//        } else if (incoming.getPerformative() == ACLMessage.INFORM) {
//            NetworkCookie mync = extractCookie(incoming);
//            NetworkData nap = new NetworkData();
////            Ole.oleToObject(new Ole(mync.getAccessPoint()), nap, NetworkData.class);
//            int latency = (int) new TimeHandler(incoming.getInReplyTo()).elapsedTimeMilisecsUntil(new TimeHandler(incoming.getReplyWith()));
//            System.out.println(mync.getID() + "\t"
//                    + mync.getSerie() + "\t"
//                    + mync.getSize() + "\t"
//                    + latency + "\t"
//                    + mync.getOwner() + "\t"
//            );
//            if (mync.getSize() < MAXSIZE_BYTES) {
//                nc = new NetworkCookie();
//                nc.setID(getHexaKey(16));
//                nc.setOwner(incoming.getSender().getLocalName());
//                nc.setZipped(mync.isZipped());
//                nc.setSerie(mync.getSerie() + 1);
//
//                if (mync.getSerie() < SERIESIZE) {
//                    nc.setPayload(fakeData(mync.getSize()));
//                    nc.setSize(nc.getPayload().length());
//                } else {
//                    nc.setSerie(0);
//                    nc.setPayload(fakeData(2 * mync.getSize()));
//                    nc.setSize(nc.getPayload().length());
//                }
//            }
//
//        }
//        if (nc != null) {
//            String sContent = Ole.objectToOle(nc).toPlainJson().toString();
//            if (nc.isZipped()) {
//                outbox.setContent("ZIPDATA" + ZipTools.zipToString(sContent));
//            } else {
//                outbox.setContent(sContent);
//            }
//            outbox.setReplyWith(TimeHandler.Now());
//            mb.getDoSend().accept(outbox);
//        } else {
//            outbox.setPerformative(ACLMessage.CANCEL);
//            mb.getDoSend().accept(outbox);
//        }
//
//    }
//
//    public static ACLMessage injectMark(ACLMessage incoming) {
//        if (!isMonitor(incoming)) {
//            incoming.addUserDefinedParameter(MonitorPL.MONITORMARK.name(), "TRUE");
//        }
//        return incoming;
//    }
//    public static Ole getPackage(ACLMessage incoming, MonitorBox mb) {
//        Ole oRes = new Ole();
//        if (isMonitor(incoming)) {
//            String content = incoming.getContent();
//            try {
//                if (content.startsWith("ZIPDATA")) {
//                    content = ZipTools.unzipString(content.replace("ZIPDATA", ""));
//                }
//                oRes = new Ole(content);
//            } catch (Exception ex) {
//                doException(ex, mb);
//            }
//        }
//        return oRes;
//    }
//    public static void doException(Exception ex, MonitorBox mb) {
//        String message = "Unexpected exception:\n" + ex.toString();
//        if (mb.getDoApplication() == null) {
//            System.err.println(message);
//        } else {
//            mb.getDoApplication().get().Warning(message);
//        }
//        mb.getDoExit().accept(null);
//    }

