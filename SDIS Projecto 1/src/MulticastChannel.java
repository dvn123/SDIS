import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MulticastChannel extends Thread {
    byte[] data;
    private String ip;
    private String id;
    private int port;
    private MulticastSocket m_socket;
    private int MAX_BUFFER_SIZE = 64200;
    private ArrayList<DatagramPacket> buffer;

    public MulticastChannel(String ip, int port, ArrayList<DatagramPacket> buffer, String id) {
        if (MulticastProcessor.LOG)
            System.out.println("[" + id + "] Creating IP - " + ip + " Port - " + port);
        this.ip = ip;
        this.port = port;
        this.id = id;

        initialize_multicast();
        this.buffer = buffer;
    }

    private void initialize_multicast() {
        if (MulticastProcessor.LOG)
            System.out.println("[" + id + "] Initializing Socket");
        try {
            m_socket = new MulticastSocket(port);
            m_socket.setTimeToLive(1);
            InetAddress group = InetAddress.getByName(ip);
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
        if (MulticastProcessor.LOG)
            System.out.println("[" + id + "] Listening");
        byte[] buf = new byte[MAX_BUFFER_SIZE];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            m_socket.receive(recv);
            data = new byte[recv.getLength()];
            System.arraycopy(recv.getData(), 0, data, 0, recv.getLength());
        } catch (IOException e) {
            System.err.println("Failed trying to listen in channel. Exiting"); //TODO Error Control?
            //System.exit(-1);
        }
        if (MulticastProcessor.LOG)
            System.out.println("[" + id + "] Processing - " + new String(recv.getData()));
        if (!MulticastProcessor.ACCEPT_SAME_MACHINE_PACKETS) {
            try {
                if(MulticastProcessor.LOG)
                    System.out.println("[MulticastProcessor] Comparing - " + recv.getAddress().getHostAddress() + " - " + InetAddress.getLocalHost().getHostAddress());
                if (!recv.getAddress().getHostAddress().equals(InetAddress.getLocalHost().getHostAddress())) {
                    buffer.add(recv);
                } else {
                    if (MulticastProcessor.LOG)
                        System.out.println("[MulticastChannel] Rejecting packet because it was sent from the same machine");
                }
            } catch (UnknownHostException e) {
                System.err.println("Can't find own ip, accepting all packets.");
                buffer.add(recv);
            }
        } else {
            buffer.add(recv);
        }
    }

    public void run() {
        while (true) {
            listen();
        }
    }

    public void close() {
        m_socket.close();
        System.exit(0);
    }
}
