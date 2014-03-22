import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class BackupSend {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    byte[] file_id;
    File f;
    int rep_degree;
    ArrayList<String> store_messages_received;

    int current_rep_degree;

    MulticastMessageSender mcbs;

    BackupSend(MulticastMessageSender mcbs, byte[] file_id, File f, int rep_degree, ArrayList<String> store_messages_received) {
        this.mcbs = mcbs;
        this.f = f;
        this.file_id = file_id;
        this.rep_degree = rep_degree;
        this.store_messages_received = store_messages_received;
    }

    public static char[] getChars(byte[] hash) {
        char[] hexChars = new char[hash.length * 2];
        for (int j = 0; j < hash.length; j++) {
            int v = hash[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return hexChars;
    }

    private boolean wait_for_replies(long time_to_wait) {
        long t= System.currentTimeMillis();
        long end = t+time_to_wait;
        int current_index = 0;
        while(System.currentTimeMillis() < end) {
            if(store_messages_received.size() < current_index) {
                String[] split = store_messages_received.get(current_index).split(" ");
                if(split[1].getBytes() == file_id);
                    current_rep_degree++;
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
            if(split[1].getBytes() == file_id)
                store_messages_received.remove(i);
        }
    }

    public void breakFile() throws IOException {
        int chunkCount = 0;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(f.getAbsolutePath());
            byte[] buffer = new byte[64000];
            if (MulticastProcessor.LOG)
                System.out.println("[BackupSender] Copying file using streams");

            while ((fis.read(buffer)) != -1) {
                if (MulticastProcessor.LOG)
                    System.out.println("[BackupSender] Creating chunk: " + chunkCount);
                mcbs.send_message("PUTCHUNK " + MulticastProcessor.VERSION + " " + file_id + " " + buffer + "\r\n\r\n");
                boolean a = false;
                long time_to_wait = 500;
                while(!a && time_to_wait <= Math.pow(500,5)) {
                    a = wait_for_replies(time_to_wait);
                    time_to_wait = time_to_wait*2;
                }
                remove_messages_from_buffer(); //clears messages that weren't needed (> than rep degree) that belong to this session of backupsend
                //chunkCount++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while copying file " + ioe);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }

        char[] hexChars = getChars(file_id);

        System.out.print("HEX: ");
        System.out.println(hexChars);
    }

    private byte[] hash_file_id(byte[] file_id) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(file_id);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error while converting to SHA-256");
            e.printStackTrace();
        }
        return null;
    }


}




