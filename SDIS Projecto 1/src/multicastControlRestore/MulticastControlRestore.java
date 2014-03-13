package multicastControlRestore;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Diogo on 11-03-2014.
 */
public class MulticastControlRestore implements Runnable {

    private String mc_ip;
    private int mc_port;
    private MulticastSocket m_socket;
    private int MAX_BUFFER_SIZE = 1024;

    public MulticastControlRestore(String mc_ip, int mc_port) {
        this.mc_ip = mc_ip;
        this.mc_port = mc_port;
    }

    private void initialize_multicast() {
        try {
            m_socket = new MulticastSocket(mc_port);
            InetAddress group = InetAddress.getByName(mc_ip);
            m_socket.joinGroup(group);
        } catch (IOException e) {
            System.err.println("Failed to initialize Multicast Control Socket. Exiting"); //TODO Error Control?
            System.exit(-1);
        }
    }

    private void listen() {
        byte[] buf = new byte[MAX_BUFFER_SIZE];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            m_socket.receive(recv);
        } catch (IOException e) {
            System.err.println("Failed trying to listen in Multicast Control channel. Exiting"); //TODO Error Control?
            System.exit(-1);
        }
        MulticastControlRestoreDataProcessing mcdt = new MulticastControlRestoreDataProcessing(new String(recv.getData()).substring(0, recv.getLength()));
        mcdt.run();
    }

    public void run() {
        initialize_multicast();
        while(true) {
            listen();
        }
    }

}
