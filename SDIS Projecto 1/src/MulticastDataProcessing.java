import java.io.File;
import java.io.FileNotFoundException;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Diogo on 12-03-2014.
 */
public class MulticastDataProcessing extends Thread {
    private ArrayList<String> buffer;
    private float space;
    private static final boolean LOG = true;

    /*
    String multicast_control_ip;
    int multicast_control_port;
    String multicast_data_backup_ip;
    int multicast_data_backup_port;
    String multicast_data_restore_ip;
    int multicast_data_restore_port;

    private MulticastSocket mc_socket;
    private MulticastSocket mcb_socket;
    private MulticastSocket mcr_socket;
    */

    MulticastMessageSender mcs;
    MulticastMessageSender mcbs;
    MulticastMessageSender mcrs;

    private MulticastChannel mc;
    private MulticastChannel mcb;
    private MulticastChannel mcr;

    MulticastDataProcessing(String multicast_control_ip, int multicast_control_port, String multicast_data_backup_ip, int multicast_data_backup_port, String multicast_data_restore_ip, int multicast_data_restore_port) {

        initialize_multicast_channels(multicast_control_ip, multicast_control_port, multicast_data_backup_ip, multicast_data_backup_port, multicast_data_restore_ip, multicast_data_restore_port);
        read_file();
    }

    private void initialize_multicast_channels(String multicast_control_ip, int multicast_control_port, String multicast_data_backup_ip, int multicast_data_backup_port, String multicast_data_restore_ip, int multicast_data_restore_port) {
        buffer = new ArrayList<String>();
        mc = new MulticastChannel(multicast_control_ip, multicast_control_port, buffer, "MulticastControl");
        MulticastSocket mc_socket = mc.getM_socket();
        mc.start();
        mcs = new MulticastMessageSender(mc_socket, multicast_control_ip, multicast_control_port);
        if (LOG)
            System.out.println("[Interface] Created Multicast Control");

        mcb = new MulticastChannel(multicast_data_backup_ip, multicast_data_backup_port, buffer, "MulticastDataBackup");
        MulticastSocket mcb_socket = mcb.getM_socket();
        mcb.start();
        mcbs = new MulticastMessageSender(mcb_socket, multicast_data_backup_ip, multicast_data_backup_port);
        if (LOG)
            System.out.println("[Interface] Created Multicast Data BackupReceive");

        mcr = new MulticastChannel(multicast_data_restore_ip, multicast_data_restore_port, buffer, "MulticastDataRestore");
        MulticastSocket mcr_socket = mcr.getM_socket();
        mcr.start();
        mcrs = new MulticastMessageSender(mcr_socket, multicast_data_restore_ip, multicast_data_restore_port);
        if (LOG)
            System.out.println("[Interface] Created Multicast Data Restore");
    }

    public void read_file() {
        final File file = new File("conf");

        final Scanner scanner;
        try {
            scanner = new Scanner(file);
            Pattern p = Pattern.compile("SPACE: (.*)");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Matcher m = p.matcher(line);
                if (m.find()) {
                    space = Float.parseFloat(m.group(1));
                    if (LOG)
                        System.out.println("[Interface] Space - " + space);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Configuration file not found. Using default space of 128MB");
        }
    }

    private int process_message(String message) {
        String[] msg = message.split(" ");

        if (msg[0].equals("PUTCHUNK")) {
            BackupReceive b = new BackupReceive(mcs);
            b.start();
            return 0;
        } else if (msg[0].equals("RESTORE")) {

            return 0;
        } else if (msg[0].equals("STORED")) {

            return 0;
        } else if (msg[0].equals("CHUNK")) {

            return 0;
        } else if (msg[0].equals("DELETE")) {
            Delete d = new Delete(msg[1]);
            space -= d.delete_files();
            return 0;
        } else if (msg[0].equals("REMOVE")) {

            return 0;
        }
        return -1;
    }

    public void run() {
        if (LOG)
            System.out.println("[MulticastDataProcessing] Initializing.");

        //mcs.send_message("asd");

        while (true) {
            if (!buffer.isEmpty()) {
                String line = buffer.get(0);
                if(LOG)
                    System.out.println("[MulticastDataProcessing] Processing " + buffer.get(0));
                buffer.remove(0);
                process_message(line);
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
