package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Diogo on 25-02-2014.
 */
public class ServerMulticast extends Thread {
    private int port1;

    ServerMulticast(int port) {
        port1 = port;
    }

    public void run() {
        try {
            MulticastSocket msocket = new MulticastSocket(8888);

            msocket.setTimeToLive(1);

            InetAddress i = InetAddress.getByName("224.2.3.3");
            String multicast_message = "localhost" + ":" + port1;
            DatagramPacket d = new DatagramPacket(multicast_message.getBytes(), multicast_message.getBytes().length, i, 8888);
            while (true) {
                if (true)
                    System.out.println("Sending multicast");

                msocket.send(d);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException a) {
                    System.out.println("asd");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
