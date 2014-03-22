import java.io.File;
import java.io.FileNotFoundException;
import java.net.MulticastSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MulticastProcessor {
    public static final boolean LOG = true;
    public static final String VERSION = "0.5";

    public static final int MULTICAST_CONTROL_IP_POS = 0;
    public static final int MULTICAST_CONTROL_PORT_POS = 1;
    public static final int MULTICAST_BACKUP_IP_POS = 2;
    public static final int MULTICAST_BACKUP_PORT_POS = 3;
    public static final int MULTICAST_RESTORE_IP_POS = 4;
    public static final int MULTICAST_RESTORE_PORT_POS = 5;

    ArrayList<String> buffer;
    ArrayList<String> stored_messages;
    float space;

    MulticastMessageSender mcs;
    MulticastMessageSender mcbs;
    MulticastMessageSender mcrs;
    HashMap<String, byte[]> file_ids = new HashMap<String, byte[]>();
    private MulticastChannel mc;
    private MulticastChannel mcb;
    private MulticastChannel mcr;

    MulticastProcessor(String multicast_control_ip, int multicast_control_port, String multicast_data_backup_ip, int multicast_data_backup_port, String multicast_data_restore_ip, int multicast_data_restore_port) {
        if (LOG)
            System.out.println("[MulticastProcessor] Initializing.");
        initialize_multicast_channels(multicast_control_ip, multicast_control_port, multicast_data_backup_ip, multicast_data_backup_port, multicast_data_restore_ip, multicast_data_restore_port);
        read_file();
        Interface i = new Interface(buffer);
        i.start();
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

    private byte[] create_file_id(File file) {
        Path p = Paths.get(file.getAbsolutePath());
        BasicFileAttributes view = null;
        try {
            view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileTime fileTime = view.lastAccessTime();
        byte[] id = (file.getAbsolutePath() + fileTime.toString()).getBytes();

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            id = digest.digest(id);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error while converting to SHA-256");
            e.printStackTrace();
        }

        file_ids.put(file.getName(), id); //guardar file_ids num map
        return id;
    }

    private int process_message(String message) {
        if (message.toLowerCase().equals(message)) { //keyboard commands are always lower case
            process_keyboard_command(message);
            return 0;
        }

        String[] msg = message.split(" ");

        if (msg[0].equals("PUTCHUNK")) {
            BackupReceive b = new BackupReceive(mcs);
            b.start();
            return 0;
        } else if (msg[0].equals("RESTORE")) {

            return 0;
        } else if (msg[0].equals("STORED")) {
            stored_messages.add(message); //stored messages are handled by the running backupsend processes, this buffer is passed on to them
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

    private void process_keyboard_command(String cmd) {
        String commands[] = cmd.toLowerCase().split(" ");
        if (commands[0].equals("backup")) {
            File f = new File(commands[1]);
            byte[] id = create_file_id(f);
            BackupSend bs = new BackupSend(mcbs, id, f, Integer.parseInt(commands[2]), stored_messages);
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

    public void process_commands() {
        //mcs.send_message("asd");

        while (true) {
            if (!buffer.isEmpty()) {
                String line = buffer.get(0);
                if (LOG)
                    System.out.println("[MulticastProcessor] Processing " + buffer.get(0));
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

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Usage: main <multicast_control_ip> <multicast_control_port> <multicast_data_backup_ip> <multicast_data_backup_port> <multicast_data_restore_ip> <multicast_data_restore_port>");
            System.exit(-1);
        }
        //TODO verify arguments;

        MulticastProcessor mdt = new MulticastProcessor(args[MULTICAST_CONTROL_IP_POS], Integer.parseInt(args[MULTICAST_CONTROL_PORT_POS]), args[MULTICAST_BACKUP_IP_POS], Integer.parseInt(args[MULTICAST_BACKUP_PORT_POS]), args[MULTICAST_RESTORE_IP_POS], Integer.parseInt(args[MULTICAST_RESTORE_PORT_POS]));
        mdt.process_commands();
    }
}
