import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastMessageSending extends Thread {
    private String message;
    DatagramPacket p;
    private MulticastSocket m_socket;
    private boolean LOG;
    String ip;
    int port;


    public MulticastMessageSending(String message, MulticastSocket m_socket, String ip, int port, boolean LOG) {
        this.LOG = LOG;
        this.message = message;
        this.m_socket = m_socket;
        this.ip = ip;
        this.port = port;
        if(LOG)
            System.out.println("[MulticastMessageSending] Creating");
    }

    private void create_datagram() {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            p = new DatagramPacket(message.getBytes(), message.getBytes().length, addr, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private void send_message() throws IOException {
        if(LOG)
            System.out.println("[MulticastMessageSending] Sending Message");
        m_socket.send(p);
    }

    public void run() {
        create_datagram();
        try {
            send_message();
        } catch (IOException e) {
            System.err.println("Failed sending message.");
        }
    }

}
