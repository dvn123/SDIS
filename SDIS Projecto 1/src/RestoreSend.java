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
            //System.out.println(" SIZE - " + chunk_messages_received.size() + " - CurrentIndex - " + current_index);
            if (chunk_messages_received.size() > current_index) {
                String[] split = chunk_messages_received.get(current_index).split("\\s+");
                //System.out.println(split[2].equals(new String(file_id)));
                //System.out.println("split[3] - " + split[3] + " current_chunk " + (char) current_chunk_n);
                //System.out.println(Integer.parseInt(split[3]) == (current_chunk_n));
                if (split[2].equals(new String(file_id)) && Integer.parseInt(split[3]) == (current_chunk_n)) {
                    if (MulticastProcessor.LOG)
                        System.out.println("[BackupSend] Found a CHUNK message related to this RestoreSend instance");
                    try {
                        String data_to_write = chunk_messages_received.get(current_index).substring(chunk_messages_received.get(current_index).indexOf("\r\n\r\n") + 4);
                        System.out.println("Writing - " + data_to_write);
                        fos.write(data_to_write.getBytes()); //get message after "\r\n\r\n"
                        chunk_messages_received.remove(current_index);
                        current_index--;
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

                float size = -1;
                long time_to_wait = INITIAL_TIME_TO_WAIT;
                while (size == -1 && time_to_wait <= MAX_LIMIT_TIME_TO_WAIT) {
                    if (MulticastProcessor.LOG)
                        System.out.println("[RestoreSend] Waiting for answers, time_to_wait - " + time_to_wait);
                    size = wait_for_replies(time_to_wait);
                    time_to_wait = time_to_wait * 2;
                }
                remove_messages_from_buffer(); //clears messages that weren't needed (> than rep degree) that belong to this session of backupsend
                if (size < MulticastProcessor.MAX_CHUNK_SIZE && size != -1) {
                    //if size isn't max this is the last chunk
                    System.out.println("[RestoreSend] Found last chunk - SIZE - " + size);
                    break;
                }

                current_chunk_n++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                System.out.println("Restore Finished");
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




