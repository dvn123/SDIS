import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MulticastSocket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interface {
    public static final int MULTICAST_CONTROL_IP_POS = 0;
    public static final int MULTICAST_CONTROL_PORT_POS = 1;
    public static final int MULTICAST_BACKUP_IP_POS = 2;
    public static final int MULTICAST_BACKUP_PORT_POS = 3;
    public static final int MULTICAST_RESTORE_IP_POS = 4;
    public static final int MULTICAST_RESTORE_PORT_POS = 5;
    public static final boolean LOG = true;
    public static final String VERSION = "0.5";

    String multicast_control_ip;
    int multicast_control_port;
    String multicast_data_backup_ip;
    int multicast_data_backup_port;
    String multicast_data_restore_ip;
    int multicast_data_restore_port;

    float space;

    private MulticastSocket mc_socket;
    private MulticastSocket mcb_socket;
    private MulticastSocket mcr_socket;

    Interface(String multicast_control_ip, int multicast_control_port, String multicast_data_backup_ip, int multicast_data_backup_port, String multicast_data_restore_ip, int multicast_data_restore_port) {
        this.multicast_control_ip = multicast_control_ip;
        this.multicast_control_port = multicast_control_port;
        this.multicast_data_backup_ip = multicast_data_backup_ip;
        this.multicast_data_backup_port = multicast_data_backup_port;
        this.multicast_data_restore_ip = multicast_data_restore_ip;
        this.multicast_data_restore_port = multicast_data_restore_port;
    }

    private void initialize_multicast_channels() {
        MulticastChannel mc = new MulticastChannel(multicast_control_ip, multicast_control_port, "MulticastControl");
        mc_socket = mc.getM_socket();
        mc.start();

        if (LOG)
            System.out.println("[Interface] Created Multicast Control");

        MulticastChannel mcb = new MulticastChannel(multicast_data_backup_ip, multicast_data_backup_port, "MulticastDataBackup");
        mcb_socket = mcb.getM_socket();
        mcb.start();

        if (LOG)
            System.out.println("[Interface] Created Multicast Data Backup");

        MulticastChannel mcr = new MulticastChannel(multicast_data_restore_ip, multicast_data_restore_port, "MulticastDataRestore");
        mcr_socket = mcr.getM_socket();
        mcr.start();

        if (LOG)
            System.out.println("[Interface] Created Multicast Data Restore");
    }

    private void process_command(String cmd) {
        String commands[] = cmd.toLowerCase().split(" ");
        if (commands[0] == "backup") {

        } else if (commands[0] == "restore") {

        } else if (commands[0] == "delete") {

        } else if (commands[0] == "free") {

        }
    }

    private void keyboard() {
        Scanner s = new Scanner(System.in);
        while (true) {
            System.out.println("Welcome to the Distributed File Backup System");
            System.out.println("Possible Commands - \nbackup <file_name> <number_of_copies>\n restore <file_name>\ndelete file_name\nfree <n_bytes_to_free>");
            String s1 = s.nextLine();

            process_command(s1);
        }
    }

    public void read_file() {
        final File file = new File("conf");

        final Scanner scanner;
        try {
            scanner = new Scanner(file);
            Pattern p = Pattern.compile( "SPACE: (.*)" );
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Matcher m = p.matcher(line);
                if (m.find()) {
                    space = Float.parseFloat(m.group(1));
                    if(LOG)
                        System.out.println("[Interface] Space - " + space);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Configuration file not found. Using default space of 128MB");
        }

    }

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Usage: main <multicast_control_ip> <multicast_control_port> <multicast_data_backup_ip> <multicast_data_backup_port> <multicast_data_restore_ip> <multicast_data_restore_port>");
            System.exit(-1);
        }

        //TODO verify arguments;

        Interface i = new Interface(args[MULTICAST_CONTROL_IP_POS], Integer.parseInt(args[MULTICAST_CONTROL_PORT_POS]), args[MULTICAST_BACKUP_IP_POS], Integer.parseInt(args[MULTICAST_BACKUP_PORT_POS]), args[MULTICAST_RESTORE_IP_POS], Integer.parseInt(args[MULTICAST_RESTORE_PORT_POS]));
        i.initialize_multicast_channels();
        i.read_file();

        //MulticastMessageSending m = new MulticastMessageSending("asd", i.mc_socket, i.multicast_control_ip, i.multicast_control_port, LOG);
        //m.start();

        Delete d = new Delete("asd", 3, i.mc_socket, i.multicast_control_ip, i.multicast_control_port);
        d.start();


        d.increment_deleted();
    }
}
