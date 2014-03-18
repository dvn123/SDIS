import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastChannel extends Thread {
    public static final boolean LOG = true;



    private String ip;
    private String id;
    private int port;
    private MulticastSocket m_socket;
    private int MAX_BUFFER_SIZE = 1024;

    public MulticastChannel(String ip, int port, String id) {
        if (LOG)
            System.out.println("[" + id + "] Creating IP - " + ip + " Port - " + port);
        this.ip = ip;
        this.port = port;
        this.id = id;

        initialize_multicast();
    }

    private void initialize_multicast() {
        if (LOG)
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
        if (LOG)
            System.out.println("[" + id + "] Listening");
        byte[] buf = new byte[MAX_BUFFER_SIZE];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        try {
            m_socket.receive(recv);
        } catch (IOException e) {
            System.err.println("Failed trying to listen in Multicast Control channel. Exiting"); //TODO Error Control?
            System.exit(-1);
        }
        if (LOG)
            System.out.println("[" + id + "] Processing - " + new String(recv.getData()).substring(0, recv.getLength()));
        MulticastDataProcessing mcdt = new MulticastDataProcessing(new String(recv.getData()).substring(0, recv.getLength()), LOG);
        mcdt.start();
    }

    public void run() {
        while (true) {
            listen();
        }
    }
}
