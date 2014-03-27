/*
To backup a chunk, the initiator-peer sends to the MDB multicast data channel a message whose body is the
contents of that chunk. This message includes also the chunk id and the desired replication degree:

PUTCHUNK <Version> <FileId> <ChunkNo> <ReplicationDeg> <CRLF> <CRLF> <Body>

A peer that stores the chunk upon receiving the PUTCHUNK message, should reply by sending on the
multicast control channel (MC) a confirmation message with the following format:

STORED <Version> <FileId> <ChunkNo> <CRLF> <CRLF>

after a random delay uniformly distributed between 0 and 400 ms.

Version - ASCII
FileId - ASCII
ChunkNo - Int encoded como ASCII;
ReplicationDeg- Char (ASCII)
CRLF
Body . Max Size 64KByte
  
  - Receber PUTCHUNK do canal multicast.
  - Construir a string de armazenamento.
  - Armazenar o Chunk.
  - Random delay (100ms - 400ms)
  - Responder com mensagem.

  int to char:
  int yourInt = 33;
  char ch = (char) yourInt;

  char to int:
  int value = (int)c.charValue();
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class RestoreReceive extends Thread {
    String[] split_msg;
    MulticastMessageSender mcrs;
    byte[] data;
    String path;

    ArrayList<String> chunk_messages_received;

    RestoreReceive(MulticastMessageSender mcrs, String msg_received, ArrayList<String> chunk_messages_received, String path) {
        split_msg = msg_received.split(" ");
        this.mcrs = mcrs;
        this.path = path;
        this.chunk_messages_received = chunk_messages_received;
    }

    private void read_file() {
        try {
            File file = new File(path + "/" + split_msg[2] + "-" + split_msg[3]);
            FileInputStream fis = new FileInputStream(file);
            data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
        } catch (FileNotFoundException e) {
            System.exit(0); //if the chunk wasn't backed up on this pc
        } catch (IOException e) {
            System.exit(0); //if the chunk wasn't backed up on this pc
        }
    }

    private void send_message() {
        String answer = "CHUNK " + MulticastProcessor.VERSION + " " + split_msg[2] + " " + split_msg[3] + "\r\n\r\n" + new String(data);
        if (MulticastProcessor.LOG)
            System.out.println("[RestoreReceive] answer: " + answer);
        mcrs.send_message(answer);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            System.out.println("?");
        }
    }

    private int check_chunk_count() {
        for (int i = 0; i < chunk_messages_received.size(); i++) {
            String[] split = chunk_messages_received.get(i).split(" ");
            if (split[2].equals(split_msg[2]) && split[3].equals(split_msg[3])) {
                return -1;
            }
        }
        return 0;
    }

    public void run() {
        try {
            Thread.sleep(100 + (int) (Math.random() * ((400 - 100) + 1)));
        } catch (InterruptedException e) {
            System.out.println("Error sleeping.");
        }

        if (check_chunk_count() != 0)
            return;
        read_file();
        send_message();
    }
}
