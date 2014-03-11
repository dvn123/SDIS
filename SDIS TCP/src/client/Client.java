package client;

import sun.text.normalizer.Trie;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class Client {
    private static final boolean LOG = true;
    private static final int MULTICAST_IP_POS = 0;
    private static final int MULTICAST_PORT_POS = 1;
    private static final int OPERATION_POS = 2;
    private static final int STARTING_OPND_POS = 3;
    private static final int MAX_PACKET_SIZE = 1024;

    private int port;
    private String host_name;
    private String oper;
    private String opnd;

    private int multicast_port;
    private String multicast_ip;

    Socket socket;
    BufferedReader inFromServer;

    private void wait_for_reply() throws IOException {
        String response = inFromServer.readLine();
        if(LOG) {
            if(response == "-1" || response == "NOT_FOUND" || response == "REJ") {
                System.out.println(opnd + ":" + "ERROR");
            } else
                System.out.println(opnd + ":" + response);
        }

        socket.close();
    }

    private void parse(String[] args) {
        multicast_ip = args[MULTICAST_IP_POS];
        multicast_port = Integer.parseInt(args[MULTICAST_PORT_POS]);

        oper = args[OPERATION_POS];

        opnd = oper;
        for (int i = STARTING_OPND_POS; i < args.length; i++) {
            opnd = opnd.concat(" ");
            opnd = opnd.concat(args[i]);
        }
    }

    private void send() {
        if(LOG)
            System.out.println("Sending - " + opnd);

        try {
            InetAddress addr = InetAddress.getByName(host_name);
            try {
                socket = new Socket(host_name, port);
                DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
                inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outToServer.flush();
                outToServer.writeBytes(opnd + '\n');
                wait_for_reply();
            } catch (IOException e) {
                System.out.println("Error creating socket.");
            }
        } catch (UnknownHostException e1) {
            System.out.println("Unknown host.");
        }
    }

    public void set_message(String msg) {
        oper = msg.substring(0,msg.indexOf(" "));
        opnd = msg;
    }

    public static void main(String[] args) {
        Client c = new Client();
        c.parse(args);

        try {
            c.get_from_broadcast();
        } catch (IOException e) {
            e.printStackTrace();
        }

        c.send();
        c.set_message("lookup as-87-de");
        c.send();
    }

    private void get_from_broadcast() throws IOException {
        MulticastSocket msocket = new MulticastSocket(8888);
        msocket.setTimeToLive(1);
        InetAddress i = InetAddress.getByName(multicast_ip);
        msocket.joinGroup(i);
        String ms;
        while(true) {
            byte[] rd = new byte[MAX_PACKET_SIZE];
            DatagramPacket rp = new DatagramPacket(rd, rd.length);
            msocket.receive(rp);
            System.out.println("Received: " + new String(rp.getData()));
            ms =  new String(rp.getData()).substring(0, rp.getLength());   //remove unfilled buffer whitespace and split each word
            break;
        }
        String[] split_ms = ms.split(":");

        host_name = split_ms[0];
        port = Integer.parseInt(split_ms[1]);

        if(LOG) {
            System.out.println("multicast: " + multicast_ip + " " + multicast_port + ":" + host_name + " " + port);
        }

        msocket.leaveGroup(i);
        msocket.close();
    }
}