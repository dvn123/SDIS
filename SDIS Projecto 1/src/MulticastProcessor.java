import java.io.*;
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
import java.util.Iterator;
import java.util.Map;

public class MulticastProcessor {
    public static final boolean LOG = true;
    public static final boolean ACCEPT_SAME_MACHINE_PACKETS = true;
    public static final String VERSION = "0.5";

    public static final int MULTICAST_CONTROL_IP_POS = 0;
    public static final int MULTICAST_CONTROL_PORT_POS = 1;
    public static final int MULTICAST_BACKUP_IP_POS = 2;
    public static final int MULTICAST_BACKUP_PORT_POS = 3;
    public static final int MULTICAST_RESTORE_IP_POS = 4;
    public static final int MULTICAST_RESTORE_PORT_POS = 5;
    public static final int SPACE_POS = 6;
    public static final int PATH_POS = 7;

    public static final int MAX_CHUNK_SIZE = 64000;
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    ArrayList<byte[]> buffer;
    ArrayList<String> stored_messages;
    ArrayList<String> putchunk_messages;
    ArrayList<byte[]> chunk_messages;

    float space;
    String path;
    public static String homeDir;
    MulticastMessageSender mcs;
    MulticastMessageSender mcbs;
    MulticastMessageSender mcrs;
    HashMap<String, char[]> file_ids = new HashMap<String, char[]>();
    HashMap<String, Integer> file_rep_degree = new HashMap<String, Integer>();
    HashMap<String, Integer> chunk_stored_degree = new HashMap<String, Integer>();
    private MulticastChannel mc;
    private MulticastChannel mcb;
    private MulticastChannel mcr;
    Interface i;

    MulticastProcessor(String multicast_control_ip, int multicast_control_port, String multicast_data_backup_ip, int multicast_data_backup_port, String multicast_data_restore_ip, int multicast_data_restore_port, int space, String path) {
        if (LOG)
            System.out.println("[MulticastProcessor] Initializing.");

        buffer = new ArrayList<byte[]>();
        stored_messages = new ArrayList<String>();
        chunk_messages = new ArrayList<byte[]>();
        this.space = space;
        this.path = path;

        initialize_multicast_channels(multicast_control_ip, multicast_control_port, multicast_data_backup_ip, multicast_data_backup_port, multicast_data_restore_ip, multicast_data_restore_port);
        i = new Interface(buffer);
        i.start();
        read_map();
        read_chunk_stored_degree();
        read_file_rep_degree();
    }

    public static void main(String[] args) {
        if (args.length != 8) {
            System.out.println("Usage: main <multicast_control_ip> <multicast_control_port> <multicast_data_backup_ip> <multicast_data_backup_port> <multicast_data_restore_ip> <multicast_data_restore_port> <space> <backup_path>");
            System.exit(-1);
        }
        MulticastProcessor mdt = new MulticastProcessor(args[MULTICAST_CONTROL_IP_POS], Integer.parseInt(args[MULTICAST_CONTROL_PORT_POS]), args[MULTICAST_BACKUP_IP_POS], Integer.parseInt(args[MULTICAST_BACKUP_PORT_POS]), args[MULTICAST_RESTORE_IP_POS], Integer.parseInt(args[MULTICAST_RESTORE_PORT_POS]), Integer.parseInt(args[SPACE_POS]), args[PATH_POS]);
        mdt.process_commands();
    }

    private void initialize_multicast_channels(String multicast_control_ip, int multicast_control_port, String multicast_data_backup_ip, int multicast_data_backup_port, String multicast_data_restore_ip, int multicast_data_restore_port) {
        mc = new MulticastChannel(multicast_control_ip, multicast_control_port, buffer, "MulticastControl");
        MulticastSocket mc_socket = mc.getM_socket();
        mc.start();
        mcs = new MulticastMessageSender(mc_socket, multicast_control_ip, multicast_control_port);
        if (LOG)
            System.out.println("[MulticastProcessor] Created Multicast Control");
        mcb = new MulticastChannel(multicast_data_backup_ip, multicast_data_backup_port, buffer, "MulticastDataBackup");
        MulticastSocket mcb_socket = mcb.getM_socket();
        mcb.start();
        mcbs = new MulticastMessageSender(mcb_socket, multicast_data_backup_ip, multicast_data_backup_port);
        if (LOG)
            System.out.println("[MulticastProcessor] Created Multicast Data BackupReceive");

        mcr = new MulticastChannel(multicast_data_restore_ip, multicast_data_restore_port, buffer, "MulticastDataRestore");
        MulticastSocket mcr_socket = mcr.getM_socket();
        mcr.start();
        mcrs = new MulticastMessageSender(mcr_socket, multicast_data_restore_ip, multicast_data_restore_port);
        if (LOG)
            System.out.println("[MulticastProcessor] Created Multicast Data RestoreReceive");
    }

    private void read_map() {
        try {
            File file = new File("map");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                file_ids.put(line.substring(0,line.indexOf(":")), line.substring(line.indexOf(":") + 1).toCharArray());
            }
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void read_file_rep_degree() {
        try {
            File file = new File("file_rep_degree");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                file_rep_degree.put(line.substring(0,line.indexOf(":")), Integer.parseInt(line.substring(line.indexOf(":") + 1)));
            }
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void read_chunk_stored_degree() {
        try {
            File file = new File("chunk_stored_degree");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            Iterator it = chunk_stored_degree.entrySet().iterator();
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                chunk_stored_degree.put(line.substring(0,line.indexOf(":")), Integer.parseInt(line.substring(line.indexOf(":") + 1)));
            }
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void write_file_rep_degree() {
        System.out.println("[MulticastProcessor] Writing to file_rep_degree");
        try {
            FileOutputStream fos = new FileOutputStream("file_rep_degree");
            Iterator it = file_rep_degree.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                System.out.println("[MulticastProcessor] Writing to file_rep_degree 1- " + pairs.getKey() + " 2- " + pairs.getValue());
                fos.write((pairs.getKey().toString() + ":" + pairs.getValue() + "\n").getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write_chunk_stored_degree() {
        System.out.println("[MulticastProcessor] Writing to chunk_stored_degree");
        try {
            FileOutputStream fos = new FileOutputStream("chunk_stored_degree");
            Iterator it = chunk_stored_degree.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                System.out.println("[MulticastProcessor] Writing to chunk_stored_degree 1- " + pairs.getKey() + " 2- " + pairs.getValue());
                fos.write((pairs.getKey().toString() + ":" + pairs.getValue() + "\n").getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write_map() {
        System.out.println("[MulticastProcessor] Writing to map");
        try {
            FileOutputStream fos = new FileOutputStream("map");
            Iterator it = file_ids.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                System.out.println("[MulticastProcessor] Writing to map 1- " + pairs.getKey() + " 2- " + new String((char[]) pairs.getValue()));
                fos.write((pairs.getKey().toString() + ":" + new String((char[]) pairs.getValue()) + "\n").getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private char[] create_file_id(File file) {
        Path p = Paths.get(file.getAbsolutePath());
        BasicFileAttributes view = null;
        try {
            view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
        } catch (Exception e) {
            System.err.println("No such file");
            return null;
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


        char[] hexChars = new char[id.length * 2];
        for (int j = 0; j < id.length; j++) {
            int v = id[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        file_ids.put(file.getName(), hexChars); //guardar file_ids num map
        return hexChars;
    }

    private int process_message(byte[] message) {
        if (new String(message).toLowerCase().equals(new String(message))) { //keyboard commands are always lower case
            process_keyboard_command(new String(message));
            return 0;
        }

        String message_1 = new String(message).substring(0, new String(message).lastIndexOf("\r\n\r\n"));

        String[] msg = message_1.split(" ");

        if (!msg[1].equals(VERSION)) {
            System.out.println("Protocol versions do not match. Command aborted.");
            return -1;
        }

        if (msg[0].equals("PUTCHUNK")) {
            putchunk_messages.add(message_1);
            if(space > 0) {
                BackupReceive b = new BackupReceive(mcs, message, space, path);
                space -= b.getLength();
                b.start();
            }
            return 0;
        } else if (msg[0].equals("GETCHUNK")) {
            RestoreReceive rr = new RestoreReceive(mcrs, message_1, chunk_messages, path);
            rr.start();
            return 0;
        } else if (msg[0].equals("STORED")) {
            if(chunk_stored_degree.containsKey(msg[2] + "-" + msg[3])) {
                chunk_stored_degree.replace(msg[2] + "-" + msg[3], chunk_stored_degree.get(msg[2] + "-" + msg[3]) + 1);
            } else {
                chunk_stored_degree.put(msg[2] + "-" + msg[3], 1);
            }
            stored_messages.add(message_1); //stored messages are handled by the running backupsend processes, this buffer is passed on to them
            return 0;
        } else if (msg[0].equals("CHUNK")) {
            chunk_messages.add(message); //chunk messages are handled by RestoreReceive and RestoreSend
            return 0;
        } else if (msg[0].equals("DELETE")) {
            Delete d = new Delete(msg[1]);
            space -= d.delete_files();
            return 0;
        } else if (msg[0].equals("REMOVED")) {
            chunk_stored_degree.replace(msg[2] + "-" + msg[3], chunk_stored_degree.get(msg[2] + "-" + msg[3]) - 1);
            if(chunk_stored_degree.get(msg[2] + "-" + msg[3]) < file_rep_degree.get(msg[2])) { //if chunk < rep_degree
                try {
                    Thread.sleep(100 + (int) (Math.random() * ((400 - 100) + 1)));
                } catch (InterruptedException e) { }
                boolean a = false;

                for (int j = 0; j < putchunk_messages.size(); j++) {
                    String[] m = putchunk_messages.get(j).split(" ");
                    if(m[2].equals(msg[2]) && m[3].equals(msg[3]))
                        a =true;
                }
                putchunk_messages.clear();
                if(!a) {
                    File f = new File(path + "/" + msg[2] + "-" + msg[3]);
                    BackupSend bs = new BackupSend(mcbs, (msg[2]).toCharArray(), f, file_rep_degree.get(msg[2]) - chunk_stored_degree.get(msg[2] + "-" + msg[3]), stored_messages);
                    bs.start();
                }
            }
            //Remove r = new Remove();
            //space -= r.freeSpace(10000);
            //Return Msg Protocol Thingy
            return 0;
        }
        return -1;
    }

    private void process_keyboard_command(String cmd) {
        String commands[] = cmd.toLowerCase().split(" ");
        if (commands[0].equals("backup")) {
            if (commands.length != 3) {
                System.err.println("Invalid command, try again.");
                return;
            }

            File f = new File(commands[1]);
            if(f.exists()) {
                char[] id = create_file_id(f);
                if (id != null) {
                    file_rep_degree.put(new String(id), Integer.valueOf(commands[2]));
                    BackupSend bs = new BackupSend(mcbs, id, f, Integer.parseInt(commands[2]), stored_messages);
                    bs.start();
                }
            }
            return;
        } else if (commands[0].equals("restore")) {
            if (commands.length != 2) {
                System.err.println("Invalid command, try again.");
                return;
            }
            if(file_ids.get(commands[1]) != null) {
                RestoreSend rs = new RestoreSend(mcs, commands[1], file_ids.get(commands[1]), chunk_messages);
                rs.start();
            } else {
                System.err.println("File wasn't stored in this system. Unable to restore.");
            }
            return;
        } else if (commands[0].equals("delete")) {
            if (commands.length != 2) {
                System.err.println("Invalid command, try again.");
                return;
            }
            //get file identifier
            //TODO
            return;
        } else if (commands[0].equals("free")) {
            if (commands.length != 2) {
                System.err.println("Invalid command, try again.");
                return;
            }

            return;
        } else if (commands[0].equals("exit")) {
            write_map();
            write_chunk_stored_degree();
            write_file_rep_degree();
            mc.close();
            mcb.close();
            mcr.close();
            i.close();
            mcs.close();
            mcbs.close();
            mcrs.close();
            System.exit(0);
            return;
        }
        System.err.println("Invalid command, try again.");
    }

    public void process_commands() {
        while (true) {
            if (!buffer.isEmpty()) {
                byte[] line = buffer.get(0);
                if (LOG)
                    System.out.println("[MulticastProcessor] Processing " + new String(buffer.get(0)));
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
