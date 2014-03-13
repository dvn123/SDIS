package multicastControl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastControl extends Thread {

    public static final boolean LOG = true;

    private String mc_ip;
    private int mc_port;
    private MulticastSocket m_socket;
    private int MAX_BUFFER_SIZE = 1024;

    public MulticastControl(String mc_ip, int mc_port) {
        if(LOG)
            System.out.println("[MulticastControl] Creating");
        this.mc_ip = mc_ip;
        this.mc_port = mc_port;

        initialize_multicast();
    }

    private void initialize_multicast() {
        if(LOG)
            System.out.println("[MulticastControl] Initializing Socket");
        try {
            m_socket = new MulticastSocket(mc_port);
            m_socket.setTimeToLive(1);
            InetAddress group = InetAddress.getByName(mc_ip);
            m_socket.joinGroup(group);
        } catch (IOException e) {
            System.err.println("Failed to initialize Multicast Control Socket. Exiting"); //TODO Error Control?
            System.exit(-1);
        }
    }

    public MulticastSocket getM_socket() {
        return m_socket;
    }

    private void listen() {
        if(LOG)
            System.out.println("[MulticastControl] Listening");
        byte[] buf = new byte[MAX_BUFFER_SIZE];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            m_socket.receive(recv);
        } catch (IOException e) {
            System.err.println("Failed trying to listen in Multicast Control channel. Exiting"); //TODO Error Control?
            System.exit(-1);
        }
        if(LOG)
            System.out.println("[MulticastControl] Processing - " + new String(recv.getData()).substring(0, recv.getLength()));
        MulticastControlDataProcessing mcdt = new MulticastControlDataProcessing(new String(recv.getData()).substring(0, recv.getLength()), LOG);
        mcdt.run();
    }

    public void run() {
        while(true) {
            listen();
        }
    }

}
