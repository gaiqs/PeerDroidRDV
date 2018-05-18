package it.peerdroid.rdv;

import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PeerDroid_RDV implements RendezvousListener {
    private RendezVousService netpgRendezvous;
    private NetworkManager manager;
    private PeerGroup netPeerGroup;

    private String peerID = "urn:jxta:uuid-DAB714D252DF42F3A761AC4E5F0D75894CA3CE4A636B45348EB59270529D840503";
    private String netPeerGroupID = "";
    //the name of your private network top peer group 
    //networkConfigurator.setInfrastructureName("STI tsc++ Net Peer Group") must be changed in TSKernel SpaceManagerLayer createNetPeerGroup() 
    //DO NOT USE "STI tsc++ Net Peer Group" as infrastructure name
    private String netPeerGroupName = "DeustoTech Net Peer Group";


    //before starting the rendezvous peer make sure to provide a seeds.txt textfile containing ip and port where your rendezvous peer will be reachable on some server
    //connection URI to Relay/Rendezvous server property in TSKernel jxta.properties must both point to the seeds.txt RELAY_SEEDING_URI=http://locOfText/seeds.txt
    public PeerDroid_RDV() throws NoSuchAlgorithmException {
        PeerGroupID groupID = null;
        //the id of your private network top peer group
        //IdFactory.createInfrastructurePeerGroupID("tsc++netpeergroup").toString() must be changed in TSKernel SpaceManagerLayer createNetPeerGroup()
        //DO NOT USE "tsc++netpeergroup" as id name
        byte[] seed = MessageDigest.getInstance("MD5").digest("ismednetpeergroup".getBytes());
        groupID = IDFactory.newPeerGroupID(seed);

        netPeerGroupID = groupID.toString();
        clearCache(new File(new File(new File(".cache"), "rdvME"), "cm"));
    }

    public void configureJXTA() throws IOException, PeerGroupException {
        File home = new File(new File(".cache"), "rdvME");
        manager = new NetworkManager(NetworkManager.ConfigMode.SUPER, "JxtaRDVME", home.toURI());
        NetworkConfigurator nc = manager.getConfigurator();

        nc.setPeerId(peerID);
        nc.setName("RDV peer");
        //This infraestructure id is neccesary for the tsc++
//        nc.setInfrastructureID(netPeerGroupID);
        nc.setInfrastructureName(netPeerGroupName);
        nc.setDescription("Private Rendezvous");
        nc.setUseMulticast(false);

        nc.setMode(NetworkConfigurator.RDV_SERVER + NetworkConfigurator.RELAY_SERVER);

        nc.setUseOnlyRelaySeeds(true);
        nc.setUseOnlyRendezvousSeeds(true);
        nc.setTcpEnabled(true);
        nc.setTcpOutgoing(true);
        nc.setTcpIncoming(true);
        //the server is per default reachable on tcp port 9701
        //nc.setTcpInterfaceAddress("20.0.0.4");
        //nc.setTcpPort(9701);

        nc.save();
    }

    private void startJXTA() throws PeerGroupException, IOException {
        manager.startNetwork();
        netPeerGroup = manager.getNetPeerGroup();
        System.out.println(netPeerGroup);
        netpgRendezvous = netPeerGroup.getRendezVousService();
        netpgRendezvous.addListener(this);
        netpgRendezvous.startRendezVous();
    }

    public void connect() throws PeerGroupException, IOException {
        configureJXTA();
        startJXTA();
    }

    synchronized public void waitForQuit() {
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void clearCache(final File rootDir) {
        try {
            if (rootDir.exists()) {
                File[] list = rootDir.listFiles();
                for (File aList : list) {
                    if (aList.isDirectory()) {
                        clearCache(aList);
                    } else {
                        aList.delete();
                    }
                }
            }
            rootDir.delete();
            System.out.println("Cache component " + rootDir.toString() + " cleared.");
        } catch (Throwable t) {
        }
    }

    public void rendezvousEvent(RendezvousEvent event) {
        String eventDescription;
        int eventType;

        eventType = event.getType();
        switch (eventType) {
            case RendezvousEvent.RDVCONNECT:
                eventDescription = "RDVCONNECT";
                break;
            case RendezvousEvent.RDVRECONNECT:
                eventDescription = "RDVRECONNECT";
                break;
            case RendezvousEvent.RDVDISCONNECT:
                eventDescription = "RDVDISCONNECT";
                break;
            case RendezvousEvent.RDVFAILED:
                eventDescription = "RDVFAILED";
                break;
            case RendezvousEvent.CLIENTCONNECT:
                eventDescription = "CLIENTCONNECT";
                break;
            case RendezvousEvent.CLIENTRECONNECT:
                eventDescription = "CLIENTRECONNECT";
                break;
            case RendezvousEvent.CLIENTDISCONNECT:
                eventDescription = "CLIENTDISCONNECT";
                break;
            case RendezvousEvent.CLIENTFAILED:
                eventDescription = "CLIENTFAILED";
                break;
            case RendezvousEvent.BECAMERDV:
                eventDescription = "BECAMERDV";
                break;
            case RendezvousEvent.BECAMEEDGE:
                eventDescription = "BECAMEEDGE";
                break;
            default:
                eventDescription = "UNKNOWN RENDEZVOUS EVENT";
        }
        System.out.println(eventDescription);
    }


    public static void main(String[] arg) throws PeerGroupException, IOException, NoSuchAlgorithmException {
        Logger jxtaLogger = Logger.getLogger("net.jxta");
        jxtaLogger.setLevel(Level.ALL);
        PeerDroid_RDV simple = new PeerDroid_RDV();
        simple.connect();
        simple.waitForQuit();
    }
} 
