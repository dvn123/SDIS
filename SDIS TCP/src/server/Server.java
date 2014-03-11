package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {
    private static final int PORT_POS = 0;
    private static final int MULTICAST_IP_POS = 1;
    private static final int MULTICAST_PORT_POS = 2;
    private static final boolean LOG = true;
    public static final int BACKLOG_SIZE = 10;

    private static final int OPER = 0;
    private static final int PLATE = 1;
    private static final int NAME = 2;

    int udp_port;
    String udp_ip = "localhost";
    ServerSocket socket;
    Socket socket2;

    DataOutputStream outToClient;

    private int multicast_port;
    private String multicast_ip;

    private InetAddress last_sender_ip;
    private int last_sender_port;
    private String[] last_data_received;

    Map<String, String> plates = new HashMap<String, String>();

    private void initialize_socket() throws IOException {
        if (LOG)
            System.out.println("Initializing Socket.");
        try {
            InetAddress addr = InetAddress.getByName(udp_ip);
            socket = new ServerSocket(udp_port, BACKLOG_SIZE, addr);

        } catch (SocketException e) {
            System.err.println("Failed to create socket. Exiting.");
            System.exit(-1);
        } catch (UnknownHostException e) {
            System.err.println("Failed to connect to address. Exiting.");
            System.exit(-1);
        }


        if (LOG)
            System.out.println("Initialized Socket.");
    }

    private void read_data() throws IOException {
        socket2 = socket.accept();
        if(LOG)
            System.out.println("Found connection");
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        String m = inFromClient.readLine();
        outToClient = new DataOutputStream(socket2.getOutputStream());
        last_data_received = m.split(" ");
        if(LOG)
            System.out.println("Received: " + m);
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
                udp_port = Integer.parseInt(port_str);
                if (udp_port < 9999 && udp_port > 10)
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
            System.out.println("The plate sent was not registered in the datebase.");
            outToClient.flush();
            outToClient.writeBytes("NOT_FOUND" + '\n');
            if (LOG)
                System.out.println("Sent NOT_FOUND");
            return -2;
        }

        System.out.println("Looking up plate " + last_data_received[PLATE] + ".");
        String name = plates.get(last_data_received[PLATE]);
        System.out.println("Plate is registered to " + name + "\n");
        outToClient.flush();
        outToClient.writeBytes(name + '\n');

        return 0;
    }

    private int process_register() throws IOException {
        if (LOG)
            System.out.println("Processing register");

        if (!(last_data_received[PLATE].matches("\\S{2,2}-\\S{2,2}-\\S{2,2}") && last_data_received[NAME].matches("\\w{3,256}")))   //obrigado isidro
            return -1;
        DatagramPacket sp = null;

        if (plates.containsKey(last_data_received[PLATE])) {
            System.out.println("Plate is already registered.");
            outToClient.flush();
            outToClient.writeBytes("-1" + '\n');
            if (LOG)
                System.out.println("Sent -1.");
            return -2;
        }

        String n_plates = String.valueOf(plates.size() + 1);
        outToClient.flush();
        outToClient.writeBytes(n_plates + '\n');

        System.out.println("Registering plate " + last_data_received[PLATE] + " to " + last_data_received[NAME] + ".\n");
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
            System.out.println("Packet has errors. Discarding.");
            outToClient.writeBytes("REJ");
        }
    }

    private void send_multicast() throws IOException {
        if(LOG)
            System.out.println("Starting Multicast");
        ServerMulticast sm = new ServerMulticast(multicast_ip, multicast_port, udp_ip, udp_port, false);
        sm.start();
    }

    public void receive_cycle() {
        System.out.println("Server is ready.");

        while (true) {
            try {
                read_data();
                process_data();
            } catch (IOException e) {
                System.out.println("Error transmiting datagrams. Exiting.");
                System.exit(-1);
            }
        }
    }

    public static void main(String[] args) {
        Server ser = new Server();

        if (args.length == 3) {
            ser.udp_port = Integer.parseInt(args[PORT_POS]);

            ser.multicast_ip = args[MULTICAST_IP_POS];
            ser.multicast_port = Integer.parseInt(args[MULTICAST_PORT_POS]);
        } else
            ser.request_arguments();


        try {
            ser.initialize_socket();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ser.send_multicast();
        } catch (IOException e) {
            System.out.println("Error in multicast socket. Exiting.");
            System.exit(-1);
        }

        ser.receive_cycle();
    }
}