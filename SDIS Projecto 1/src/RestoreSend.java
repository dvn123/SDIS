import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class RestoreSend extends Thread {
    public static final int INITIAL_TIME_TO_WAIT = 500;
    public static final int MAX_LIMIT_TIME_TO_WAIT = 500 * 10;

    MulticastMessageSender mcs;

    String file_name;
    char[] file_id;
    ArrayList<String> chunk_messages_received;
    FileOutputStream fos;
    int current_chunk_n;

    RestoreSend(MulticastMessageSender mcs, String file_name, char[] file_id, ArrayList<String> chunk_messages_received) {
        if (MulticastProcessor.LOG)
            System.out.println("[RestoreSend] Initializing");
        this.mcs = mcs;
        this.file_name = file_name;
        this.file_id = file_id;
        this.chunk_messages_received = chunk_messages_received;
        current_chunk_n = 0;
    }

    private int wait_for_replies(long time_to_wait) {
        long t = System.currentTimeMillis();
        long end = t + time_to_wait;
        int current_index = 0;
        while (System.currentTimeMillis() < end) {
            if (chunk_messages_received.size() > current_index) {
                String[] split = chunk_messages_received.get(current_index).split(" ");
                if (split[2].equals(new String(file_id)) && split[3].equals(String.valueOf(current_chunk_n))) {
                    if (MulticastProcessor.LOG)
                        System.out.println("[BackupSend] Found a CHUNK message related to this RestoreSend instance");
                    try {
                        String data_to_write = chunk_messages_received.get(current_index).substring(chunk_messages_received.get(current_index).indexOf("\r\n\r\n"));
                        fos.write(data_to_write.getBytes()); //get message after "\r\n\r\n"
                        return data_to_write.length();
                    } catch (IOException e) {
                        System.err.println("Error writing to file.");
                    }

                }
                current_index++;
            } else {
                current_index = 0;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.err.println("Interrupted sleep in Restore Send");
            }
        }
        return -1;
    }

    private void remove_messages_from_buffer() { //remove the other chunk messages that weren't needed
        for (int i = 0; i < chunk_messages_received.size(); i++) {
            String[] split = chunk_messages_received.get(i).split(" ");
            if (split[1].equals(new String(file_id)))
                chunk_messages_received.remove(i);
        }
    }

    public void restoreFile() throws IOException {
        if (MulticastProcessor.LOG)
            System.out.println("[RestoreSend] Restoring File");

        try {
            fos = new FileOutputStream(file_name);
            byte[] buffer = new byte[MulticastProcessor.MAX_CHUNK_SIZE];
            if (MulticastProcessor.LOG)
                System.out.println("[RestoreSend] Copying file using streams");
            while (true) {
                if (MulticastProcessor.LOG)
                    System.out.println("[RestoreSend] Creating chunk: " + current_chunk_n);
                mcs.send_message("GETCHUNK " + MulticastProcessor.VERSION + " " + new String(file_id) + " " + current_chunk_n + " " + "\r\n\r\n");
                int size = wait_for_replies(INITIAL_TIME_TO_WAIT);
                remove_messages_from_buffer(); //clears messages that weren't needed (> than rep degree) that belong to this session of backupsend
                if (size < MulticastProcessor.MAX_CHUNK_SIZE) //if size isn't max this is the last chunk
                    break;
                current_chunk_n++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
    }

    public void run() {
        try {
            restoreFile();
        } catch (IOException e) {
            System.err.println("File not found");
        }
    }
}




