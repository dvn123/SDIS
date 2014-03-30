import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class BackupReceive extends Thread {
    String[] split_msg;
    MulticastMessageSender mcs;
    byte[] dataToWrite;
    String test;
    boolean not_enough_space;
    String path;

    BackupReceive(MulticastMessageSender mcs, byte[] msg_received, float remaining_space, String path) {
        if (MulticastProcessor.LOG)
            System.out.println("[BackupReceive] Initializing");

        this.mcs = mcs;
        this.path = path;

        String message_1 = new String(msg_received).substring(0, new String(msg_received).indexOf("\r\n\r\n"));
        System.out.println("[BackupReceive] Message 1 - " + message_1);
        split_msg = message_1.split(" ");

        /*StringBuilder s = new StringBuilder(split_msg[5].substring(4) + " "); //remove the \r\n\r\n
        for (int i = 6; i < split_msg.length; i++) { //every word past 5 is part of the message so group them together
            s.append(split_msg[i] + " ");
        }*/
        not_enough_space = false;
        dataToWrite = Arrays.copyOfRange(msg_received, message_1.length() + 4, msg_received.length);
        test = new String(dataToWrite);
        if (MulticastProcessor.LOG)
            System.out.println("[BackupReceive] dataToWrite - " + test);
        if (dataToWrite.length > remaining_space) {
            not_enough_space = true;
            System.err.println("[BackupReceive] Not enough space to backup");
        }
    }

    public float getLength() {
        if (not_enough_space)
            return 0;
        return dataToWrite.length;
    }

    private void backup_chunk() {
        File f2 = new File(path);
        f2.mkdirs();
        File f = new File(path + "/" + split_msg[2] + "-" + split_msg[3]);
        if(f.exists()) {
            if(MulticastProcessor.LOG)
                System.out.println("[BackupReceive] File already exists, not backing up.");
            send_message();
            return;
        }

        try {
            FileOutputStream out = new FileOutputStream(path + "/" + split_msg[2] + "-" + split_msg[3]);
            System.out.println("WRITING - " + test.trim() + " - a " + test.trim().length());
            out.write(dataToWrite);
            out.flush();
            out.close();
            send_message();
        } catch (IOException e) {
            System.out.println("Error outputting to file.");
        }
    }

    public void run() {
        if (not_enough_space)
            return;
        try {
            System.out.println("");
            Thread.sleep(100 + (int) (Math.random() * ((400 - 100) + 1)));
        } catch (InterruptedException e) {
            System.out.println("Error sleeping.");
        }

        backup_chunk();
    }

    private void send_message() {
        String answer = "STORED " + MulticastProcessor.VERSION + " " + split_msg[2] + " " + split_msg[3] + "\r\n\r\n";
        if (MulticastProcessor.LOG)
            System.out.println("[BackupReceive] answer: " + answer);
        mcs.send_message(answer);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            System.out.println("?");
        }
    }
}
