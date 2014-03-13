import multicastControl.MulticastControl;
import multicastControl.MulticastControlMessageSending;
import multicastControlBackup.MulticastControlBackup;
import multicastControlRestore.MulticastControlRestore;

import java.net.MulticastSocket;

public class Interface {
    public static final int MULTICAST_CONTROL_IP_POS = 0;
    public static final int MULTICAST_CONTROL_PORT_POS = 1;
    public static final int MULTICAST_BACKUP_IP_POS = 2;
    public static final int MULTICAST_BACKUP_PORT_POS = 3;
    public static final int MULTICAST_RESTORE_IP_POS = 4;
    public static final int MULTICAST_RESTORE_PORT_POS = 5;
    public static final boolean LOG = true;

    String multicast_control_ip;
    int multicast_control_port;
    String multicast_backup_ip;
    int multicast_backup_port;
    String multicast_restore_ip;
    int multicast_restore_port;

    private MulticastSocket mc_socket;
    private MulticastSocket mcb_socket;
    private MulticastSocket mcr_socket;

    Interface(String multicast_control_ip, int multicast_control_port, String multicast_backup_ip, int multicast_backup_port, String multicast_restore_ip, int multicast_restore_port) {
        this.multicast_control_ip = multicast_control_ip;
        this.multicast_control_port = multicast_control_port;
        this.multicast_backup_ip = multicast_backup_ip;
        this.multicast_backup_port = multicast_backup_port;
        this.multicast_restore_ip = multicast_restore_ip;
        this.multicast_restore_port = multicast_restore_port;
    }

    private void initialize_mc() {
        MulticastControl mc = new MulticastControl(multicast_control_ip, multicast_control_port);
        mc_socket = mc.getM_socket();
        mc.start();
    }

    private void initialize_mcb() {
        MulticastControlBackup mcb = new MulticastControlBackup(multicast_control_ip, multicast_control_port);
        mcb.run();
    }

    private void initialize_mcr() {
        MulticastControlRestore mcr = new MulticastControlRestore(multicast_control_ip, multicast_control_port);
        mcr.run();
    }


    public static void main(String[] args) {
        if(args.length != 6) {
            System.out.println("Usage: main <multicast_control_ip> <multicast_control_port> <multicast_backup_ip> <multicast_backup_port> <multicast_restore_ip> <multicast_restore_port>");
            System.exit(-1);
        }

        //TODO verify arguments;

        Interface i = new Interface(args[MULTICAST_CONTROL_IP_POS], Integer.parseInt(args[MULTICAST_CONTROL_PORT_POS]), args[MULTICAST_BACKUP_IP_POS], Integer.parseInt(args[MULTICAST_BACKUP_PORT_POS]), args[MULTICAST_RESTORE_IP_POS], Integer.parseInt(args[MULTICAST_RESTORE_PORT_POS]));
        i.initialize_mc();

        MulticastControlMessageSending m = new MulticastControlMessageSending("asd", i.mc_socket, i.multicast_control_ip, i.multicast_control_port, LOG);
        m.run();
        //i.initialize_mcb();
        //i.initialize_mcr();
    }
}
