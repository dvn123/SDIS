package multicastControlBackup;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

/**
 * Created by Diogo on 13-03-2014.
 */
public class MulticastControlBackupMessageSending {
    private String message;
    DatagramPacket p;
    private MulticastSocket m_socket;

    MulticastControlBackupMessageSending(String message, MulticastSocket m_socket) {
        this.message = message;
        this.m_socket = m_socket;
    }

    private void create_datagram() {
        p = new DatagramPacket(message.getBytes(), message.getBytes().length);
    }

    private void send_message() throws IOException {
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
