import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Diogo on 11-03-2014.
 */
public class MulticastControl implements Runnable {

    private String mc_ip;
    private int mc_port;
    private MulticastSocket m_socket;
    private int MAX_BUFFER_SIZE;

    MulticastControl(String mc_ip, int mc_port) {
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
        MulticastControlDataProcessing mcdt = new MulticastControlDataProcessing();
        mcdt.run();
    }

    public void run() {

    }

}
