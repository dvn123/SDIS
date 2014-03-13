package multicastControl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastControlMessageSending implements Runnable {
    private String message;
    DatagramPacket p;
    private MulticastSocket m_socket;
    private boolean LOG;
    String multicast_control_ip;
    int multicast_control_port;


    public MulticastControlMessageSending(String message, MulticastSocket m_socket, String multicast_control_ip, int multicast_control_port, boolean LOG) {
        this.LOG = LOG;
        this.message = message;
        this.m_socket = m_socket;
        this.multicast_control_ip = multicast_control_ip;
        this.multicast_control_port = multicast_control_port;
        if(LOG)
            System.out.println("[MulticastControlMessageSending] Creating");
    }

    private void create_datagram() {
        try {
            InetAddress addr = InetAddress.getByName(multicast_control_ip);
            p = new DatagramPacket(message.getBytes(), message.getBytes().length, addr, multicast_control_port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private void send_message() throws IOException {
        if(LOG)
            System.out.println("[MulticastControlMessageSending] Sending Message");
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
