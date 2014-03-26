import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MulticastChannel extends Thread {
    private String ip;
    private String id;
    private int port;
    private MulticastSocket m_socket;
    private int MAX_BUFFER_SIZE = 1024;
    private ArrayList<String> buffer;

    public MulticastChannel(String ip, int port, ArrayList<String> buffer, String id) {
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
        } catch (IOException e) {
            System.err.println("Failed trying to listen in Multicast Control channel. Exiting"); //TODO Error Control?
            System.exit(-1);
        }
        if (MulticastProcessor.LOG)
            System.out.println("[" + id + "] Processing - " + new String(recv.getData()).substring(0, recv.getLength()));
        if(!MulticastProcessor.ACCEPT_SAME_MACHINE_PACKETS)  {
            try {
                if(!recv.getAddress().getHostAddress().equals(InetAddress.getLocalHost().getHostAddress()))  {
                    buffer.add(new String(recv.getData()).substring(0, recv.getLength()));
                } else {
                    if(MulticastProcessor.LOG)
                        System.out.println("[MulticastChannel] Rejecting packet because it was sent from the same machine");
                }
            } catch (UnknownHostException e) {
                System.err.println("Can't find own ip, accepting all packets.");
                buffer.add(new String(recv.getData()).substring(0, recv.getLength()));
            }
        } else
            buffer.add(new String(recv.getData()).substring(0, recv.getLength()));
    }

    public void run() {
        while (true) {
            listen();
        }
    }
}
