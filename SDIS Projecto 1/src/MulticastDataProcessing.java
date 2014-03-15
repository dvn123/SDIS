/**
 * Created by Diogo on 12-03-2014.
 */
public class MulticastDataProcessing extends Thread {
    private String message;
    private boolean LOG;

    private int process_message() {
        String[] msg = message.split(" ");

        if (msg[0] == "PUTCHUNK") {

        } else if (msg[0] == "RESTORE") {

            return 0;
        } else if (msg[0] == "STORED") {

            return 0;
        } else if (msg[0] == "CHUNK") {

            return 0;
        } else if (msg[0] == "DELETE") {

            return 0;
        } else if (msg[0] == "REMOVE") {

            return 0;
        }
        return -1;
    }

    MulticastDataProcessing(String message, boolean LOG) {
        this.message = message;
        this.LOG = LOG;
    }

    public void run() {
        if (LOG)
            System.out.println("[MulticastDataProcessing] Processing data.");
        if (process_message() != 0)
            System.err.println("Message not recognized. Aborting.");
        return;
    }
}
