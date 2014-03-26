import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class BackupReceive extends Thread {
    String[] split_msg;
    MulticastMessageSender mcs;
    byte[] dataToWrite;
    boolean not_enough_space;

    BackupReceive(MulticastMessageSender mcs, String msg_received, float remaining_space) {
        if(MulticastProcessor.LOG)
            System.out.println("[BackupReceive] Initializing");
        split_msg = msg_received.split(" ");
        this.mcs = mcs;
        StringBuilder s = new StringBuilder(split_msg[5].substring(4) + " "); //remove the \r\n\r\n
        for (int i = 6; i < split_msg.length; i++) { //every word past 5 is part of the message so group them together
            s.append(split_msg[i] + " ");
        }
        not_enough_space = false;
        dataToWrite = String.valueOf(s).getBytes();
        if(dataToWrite.length > remaining_space)
            not_enough_space = true;
    }

    public float getLength() {
        if(not_enough_space)
            return 0;
        return dataToWrite.length;
    }

    private void backup_chunk() {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(split_msg[2] + "-" + split_msg[3]);
        } catch (FileNotFoundException ioe) {
            System.out.println("Error while creating file " + ioe);
        }
        try {
            out.write(dataToWrite);
            out.close();
        } catch (IOException e) {
            System.out.println("Error outputting to file.");
        }
    }

    public void run() {
        if(not_enough_space)
            return;
        try {
            Thread.sleep(100 + (int) (Math.random() * ((400 - 100) + 1)));
        } catch (InterruptedException e) {
            System.out.println("Error sleeping.");
        }

        backup_chunk();
        send_message();
    }

    private void send_message() {
        String answer = "STORED " + MulticastProcessor.VERSION + " " + split_msg[2] + " " + split_msg[3] + "\r\n\r\n";
        if(MulticastProcessor.LOG)
            System.out.println("[BackupReceive] answer: " + answer);
        mcs.send_message(answer);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            System.out.println("?");
        }
    }
}
