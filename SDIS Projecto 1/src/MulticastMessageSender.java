import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastMessageSender {
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

    private DatagramPacket create_datagram(String message) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return new DatagramPacket(message.getBytes(), message.getBytes().length, addr, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    private DatagramPacket create_datagram(byte[] message) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return new DatagramPacket(message, message.length, addr, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void send_message(String message)  {
        DatagramPacket p = create_datagram(message);
        if(MulticastProcessor.LOG)
            System.out.println("[MulticastMessageSender] Sending Message");
        try {
            m_socket.send(p);
        } catch (IOException e) {
            System.err.println("Failed to send message. " + e);
        }
    }

    public void send_message(byte[] message)  {
        DatagramPacket p = create_datagram(message);
        if(MulticastProcessor.LOG)
            System.out.println("[MulticastMessageSender] Sending Message");
        try {
            m_socket.send(p);
        } catch (IOException e) {
            System.err.println("Failed to send message. " + e);
        }
    }
}
