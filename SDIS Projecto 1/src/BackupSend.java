import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class BackupSend extends Thread {
    public static final int INITIAL_TIME_TO_WAIT = 500;
    public static final int MAX_LIMIT_TIME_TO_WAIT = 500 * 10;

    char[] file_id;
    File f;
    int rep_degree;
    ArrayList<String> store_messages_received;

    int current_rep_degree;

    MulticastMessageSender mcbs;

    BackupSend(MulticastMessageSender mcbs, char[] file_id, File f, int rep_degree, ArrayList<String> store_messages_received) {
        if (MulticastProcessor.LOG)
            System.out.println("[BackupSend] Initializing");
        this.mcbs = mcbs;
        this.f = f;
        this.file_id = file_id;
        this.rep_degree = rep_degree;
        this.store_messages_received = store_messages_received;
    }

    private boolean wait_for_replies(long time_to_wait) {
        long t = System.currentTimeMillis();
        long end = t + time_to_wait;
        int current_index = 0;
        while (System.currentTimeMillis() < end) {
            if (store_messages_received.size() > current_index) {
                String[] split = store_messages_received.get(current_index).split(" ");
                if (split[2].equals(new String(file_id))) {
                    if (MulticastProcessor.LOG)
                        System.out.println("[BackupSend] Found a STORED message related to this BackupSend instance");
                    current_rep_degree++;
                    store_messages_received.remove(current_index);
                }
                current_index++;
            } else {
                current_index = 0;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.err.println("Interrupted sleep in Backup Send");
            }
        }
        return current_rep_degree >= rep_degree;
    }

    private void remove_messages_from_buffer() {
        for (int i = 0; i < store_messages_received.size(); i++) {
            String[] split = store_messages_received.get(i).split(" ");
            if (split[1].equals(new String(file_id)))
                store_messages_received.remove(i);
        }
    }

    public void breakFile() throws IOException {
        int chunkCount = 0;
        FileInputStream fis = null;

        if (MulticastProcessor.LOG)
            System.out.println("[BackupSend] Breaking File");

        try {
            fis = new FileInputStream(f.getAbsolutePath());
            byte[] buffer = new byte[MulticastProcessor.MAX_CHUNK_SIZE];
            if (MulticastProcessor.LOG)
                System.out.println("[BackupSend] Copying file using streams");
            int read = 0;


            while ((read = fis.read(buffer)) != -1) {
                //String message = "PUTCHUNK " + MulticastProcessor.VERSION + " " + new String(file_id) + " " + (chunkCount) + " " + rep_degree + " " + "\r\n\r\n" + new String(buffer, "UTF-8").substring(0,read);
                byte[] one = ("PUTCHUNK " + MulticastProcessor.VERSION + " " + new String(file_id) + " " + (chunkCount) + " " + rep_degree + " " + "\r\n\r\n").getBytes();
                byte[] combined = new byte[one.length + read];

                System.arraycopy(one,0,combined,0,one.length);
                System.arraycopy(buffer,0,combined,one.length,read);
                if (MulticastProcessor.LOG)
                    System.out.println("[BackupSend] Message Length - " + combined.length + " Read - " + read + " Reading file - " + new String(combined));

                mcbs.send_message(combined);
                boolean a = false;
                long time_to_wait = INITIAL_TIME_TO_WAIT;
                while (!a && time_to_wait <= MAX_LIMIT_TIME_TO_WAIT) {
                    if (MulticastProcessor.LOG)
                        System.out.println("[BackupSend] Waiting for answers, time_to_wait - " + time_to_wait);
                    a = wait_for_replies(time_to_wait);
                    time_to_wait = time_to_wait * 2;
                }
                remove_messages_from_buffer(); //clears messages that weren't needed (> than rep degree) that belong to this session of backupsend
                current_rep_degree = 0;
                chunkCount++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while copying file " + ioe);
        } finally {
            System.out.println("Backup Finished");
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
    }

    public void run() {
        try {
            breakFile();
        } catch (IOException e) {
            System.err.println("File not found");
        }
    }
}




