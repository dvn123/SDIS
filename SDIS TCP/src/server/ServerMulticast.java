package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ServerMulticast extends Thread {

    private static final int SLEEP_TIME = 500;
    private static final int TTL = 1;

    private int multicast_port;
    private String multicast_ip;

    private int udp_port;
    private String udp_ip;

    private static boolean LOG;

    ServerMulticast(String multicast_ip, int multicast_port, String udp_ip, int udp_port, boolean LOG) {
        this.multicast_port = multicast_port;
        this.multicast_ip = multicast_ip;

        this.udp_port = udp_port;
        this.udp_ip = udp_ip;

        this.LOG = LOG;
    }

    public void run() {
        try {
            MulticastSocket msocket = new MulticastSocket(multicast_port);
            msocket.setTimeToLive(TTL);

            InetAddress addr = InetAddress.getByName(multicast_ip);
            String multicast_message = udp_ip + ":" + udp_port;
            DatagramPacket d = new DatagramPacket(multicast_message.getBytes(), multicast_message.getBytes().length, addr, multicast_port);
            while (true) {
                if (LOG)
                    System.out.println("multicast: " + multicast_ip + " " +  multicast_port + ":" +  udp_ip + " " + udp_port);

                msocket.send(d);
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException a) { }
            }
        } catch (IOException e) {
            System.out.println("Error sending multicast message");
            System.exit(-1);
        }
    }
}
