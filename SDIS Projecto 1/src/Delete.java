import java.net.MulticastSocket;

/**
 * Created by Diogo on 18-03-2014.
 */
public class Delete extends Thread {
    String file_id;
    int rep_degree;
    int deleted;

    MulticastSocket mc_socket;
    String multicast_control_ip;
    int multicast_control_port;

    Delete(String file_id, int rep_degree, MulticastSocket mc_socket, String multicast_control_ip, int multicast_control_port) {
        this.file_id = file_id;
        this.rep_degree = rep_degree;
        this.mc_socket = mc_socket;
        this.multicast_control_ip = multicast_control_ip;
        this.multicast_control_port = multicast_control_port;
        deleted = 0;
    }

    public void increment_deleted() {
        deleted++;
        System.out.println("Deleted - " + deleted);
    }

    private void send_message() {
        while(deleted != rep_degree) {
            MulticastMessageSending m = new MulticastMessageSending("asd", mc_socket, multicast_control_ip, multicast_control_port, false);
            m.start();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                System.out.println("?");
            }
        }
    }

    public void run() {
        send_message();
    }


}
