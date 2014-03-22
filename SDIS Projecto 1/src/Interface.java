import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MulticastSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Scanner;

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
    private MulticastSocket mc_socket;
    private MulticastSocket mcb_socket;
    //private MulticastSocket mcr_socket;
    private MulticastChannel mc;
    private MulticastChannel mcb;
    //private MulticastChannel mcr;

    HashMap<String,byte[]> file_ids = new HashMap<String,byte[]>();

    Interface(String multicast_control_ip, int multicast_control_port, String multicast_data_backup_ip, int multicast_data_backup_port, String multicast_data_restore_ip, int multicast_data_restore_port) {
        MulticastDataProcessing mdp = new MulticastDataProcessing(multicast_control_ip, multicast_control_port, multicast_data_backup_ip, multicast_data_backup_port, multicast_data_restore_ip, multicast_data_restore_port);
        mdp.start();
        this.multicast_control_ip = multicast_control_ip;
        this.multicast_control_port = multicast_control_port;
        this.multicast_data_backup_ip = multicast_data_backup_ip;
        this.multicast_data_backup_port = multicast_data_backup_port;
        //this.multicast_data_restore_ip = multicast_data_restore_ip;
        //this.multicast_data_restore_port = multicast_data_restore_port;
    }

    private void create_file_id(File file) {
        Path p = Paths.get(file.getAbsolutePath());
        BasicFileAttributes view = null;
        try {
            view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileTime fileTime=view.lastAccessTime();

        String s = file.getAbsolutePath() + fileTime.toString();

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            try {
                byte[] hash = digest.digest(s.getBytes("UTF-8"));
                file_ids.put(file.getName(), hash);
            } catch (UnsupportedEncodingException e) {
                System.err.println("This computer doesn't support UTF-8");
            }
        } catch (NoSuchAlgorithmException e) {
            System.err.println("This computer doesn't support SHA-256");
        }
    }

    private void process_command(String cmd) {
        String commands[] = cmd.toLowerCase().split(" ");
        if (commands[0].equals("backup")) {
            backup(commands);
            return;
        } else if (commands[0].equals("restore")) {

            return;
        } else if (commands[0].equals("delete")) {
            //get file identifier
            //TODO
            return;
        } else if (commands[0].equals("free")) {

            return;
        }
        System.err.println("Invalid command, try again.");
    }

    private void backup(String[] commands) {
        //create_file_id(commands[1]);
    }

    private void keyboard() {
        System.out.println("Welcome to the Distributed File BackupReceive System\n");
        System.out.println("Possible Commands:\nbackup <file_name> <number_of_copies>\nrestore <file_name>\ndelete file_name\nfree <n_bytes_to_free>");
        Scanner s = new Scanner(System.in);
        while (true) {
            String s1 = s.nextLine();
            process_command(s1);
        }
    }

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Usage: main <multicast_control_ip> <multicast_control_port> <multicast_data_backup_ip> <multicast_data_backup_port> <multicast_data_restore_ip> <multicast_data_restore_port>");
            System.exit(-1);
        }

        //TODO verify arguments;

        Interface i = new Interface(args[MULTICAST_CONTROL_IP_POS], Integer.parseInt(args[MULTICAST_CONTROL_PORT_POS]), args[MULTICAST_BACKUP_IP_POS], Integer.parseInt(args[MULTICAST_BACKUP_PORT_POS]), args[MULTICAST_RESTORE_IP_POS], Integer.parseInt(args[MULTICAST_RESTORE_PORT_POS]));
        i.keyboard();
    }
}