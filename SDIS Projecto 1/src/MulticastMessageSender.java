import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastMessageSender extends Thread {
    private String message;
    DatagramPacket p;
    private MulticastSocket m_socket;
    String ip;
    int port;

    public MulticastMessageSender(MulticastSocket m_socket, String ip, int port) {
        this(m_socket, ip, port, false);
    }

    public MulticastMessageSender(MulticastSocket m_socket, String ip, int port, boolean LOG) {
        this.m_socket = m_socket;
        this.ip = ip;
        this.port = port;
        if(LOG)
            System.out.println("[MulticastMessageSender] Creating");
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
        create_datagram();
        if(MulticastProcessor.LOG)
            System.out.println("[MulticastMessageSender] Sending Message");
        m_socket.send(p);
    }

    public void send_message(String message)  {
        this.message = message;
        create_datagram();
        if(MulticastProcessor.LOG)
            System.out.println("[MulticastMessageSender] Sending Message");
        try {
            m_socket.send(p);
        } catch (IOException e) {
            System.err.println("Failed to send message.");
        }
    }

    public void run() {
        try {
            send_message();
        } catch (IOException e) {
            System.err.println("Failed to send message.");
        }
    }

}
