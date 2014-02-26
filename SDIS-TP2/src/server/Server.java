package server;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {
    private static final int PORT_POS = 0;
    private static final boolean LOG = false;

    private static final int OPER = 0;
    private static final int PLATE = 1;
    private static final int NAME = 2;

    int port;
    DatagramSocket socket;

    InetAddress last_sender_ip;
    int last_sender_port;
    String[] last_data_received;

    Map<String, String> plates = new HashMap<String, String>();

    private void initialize_socket() {
        if (LOG)
            System.out.println("Initializing Socket.");
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.err.println("Failed to create socket. Exiting.");
            System.exit(-1);
        }
    }

    private void read_data() throws IOException {
        byte[] rd = new byte[1024];
        DatagramPacket rp = new DatagramPacket(rd, rd.length);
        socket.receive(rp);
        if (LOG) {
            System.out.println("Received: " + new String(rp.getData()));
            System.out.println("Length " + rp.getLength());
        }
        last_sender_ip = rp.getAddress();
        last_sender_port = rp.getPort();
        last_data_received = new String(rp.getData()).substring(0, rp.getLength()).split(" ");   //remove unfilled buffer whitespace and split each word
    }

    private void request_arguments() {
        System.out.println("Port number?");

        boolean valid_input = false;
        boolean first = true;
        Scanner sc = new Scanner(System.in);
        while (!valid_input) {
            if (!first)
                System.out.println("Invalid input, try again.");
            else
                first = false;
            String port_str = sc.nextLine();

            if (port_str.matches("\\d{1,4}")) {
                port = Integer.parseInt(port_str);
                if (port < 9999 && port > 10)
                    valid_input = true;
            }
        }
    }

    private int process_lookup() throws IOException {
        if (LOG)
            System.out.println("Processing lookup");

        if (!(last_data_received[PLATE].matches("\\S{2,2}-\\S{2,2}-\\S{2,2}")))   //obrigado isidro
            return -1;

        if (!(plates.containsKey(last_data_received[PLATE]))) {
            DatagramPacket sp = new DatagramPacket("NOT_FOUND".getBytes(), "NOT_FOUND".length(), last_sender_ip, last_sender_port);
            socket.send(sp);
            return -2;
        }
        String name = plates.get(last_data_received[PLATE]);
        DatagramPacket sp = new DatagramPacket(name.getBytes(), name.length(), last_sender_ip, last_sender_port);
        socket.send(sp);

        return 0;
    }

    private int process_register() throws IOException {
        if (LOG)
            System.out.println("Processing register");

        if (!(last_data_received[PLATE].matches("\\S{2,2}-\\S{2,2}-\\S{2,2}") && last_data_received[NAME].matches("\\w{3,256}")))   //obrigado isidro
            return -1;
        DatagramPacket sp = null;

        if (plates.containsKey(last_data_received[PLATE])) {
            sp = new DatagramPacket("-1".getBytes(), "-1".length(), last_sender_ip, last_sender_port);
            socket.send(sp);
            return -2;
        }

        String n_plates = String.valueOf(plates.size() + 1);
        sp = new DatagramPacket(n_plates.getBytes(), n_plates.length(), last_sender_ip, last_sender_port);
        socket.send(sp);

        plates.put(last_data_received[PLATE], last_data_received[NAME]);
        return 0;
    }

    private void process_data() throws IOException {
        boolean valid_oper = false;
        int valid_packet = -1;
        if (last_data_received[OPER].toUpperCase().equals("REGISTER")) {
            valid_packet = process_register();
            valid_oper = true;
        } else if (last_data_received[OPER].toUpperCase().equals("LOOKUP")) {
            valid_packet = process_lookup();
            valid_oper = true;
        }

        if (!valid_oper || valid_packet == -1) {
            if (LOG)
                System.out.println("Packet has errors. Discarding.");
            DatagramPacket sp = new DatagramPacket("REJ".getBytes(), "REJ".length(), last_sender_ip, last_sender_port);
            socket.send(sp);
        }
    }

    private void send_multicast() throws IOException {
        ServerMulticast a = new ServerMulticast(port);
        a.start();
    }

    public void receive_cycle() {
        if (LOG)
            System.out.println("In Cycle");

        MulticastSocket msocket = null;
        DatagramPacket d = null;
        try {
            msocket = new MulticastSocket(8888);
            msocket.setTimeToLive(1);
            InetAddress i = InetAddress.getByName("224.2.3.3");
            String multicast_message = "localhost" + ":" + port;
            d = new DatagramPacket(multicast_message.getBytes(), multicast_message.getBytes().length, i, 8888);
        } catch (IOException e) {
            e.printStackTrace();
        }


        while (true) {
            try {
                //msocket.send(d);
                read_data();
                process_data();
            } catch (IOException e) {
                System.out.println("Error transmiting datagrams.");
                System.exit(-1);
            }
        }
    }

    public static void main(String[] args) {
        Server ser = new Server();

        //parse
        if (args.length == 1)
            ser.port = Integer.parseInt(args[PORT_POS]);
        else
            ser.request_arguments();


        ser.initialize_socket();

        try {
            ser.send_multicast();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("saiu");

        ser.receive_cycle();

    }
}