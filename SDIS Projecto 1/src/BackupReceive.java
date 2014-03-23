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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class BackupReceive extends Thread {
    String[] split_msg;
    MulticastMessageSender mcs;

    BackupReceive(MulticastMessageSender mcs, String msg_received) {
        split_msg = msg_received.split(" ");
        this.mcs = mcs;
    }

    private void backup_chunk() {
        StringBuilder s = new StringBuilder(split_msg[5].substring(4) + " "); //remove the \r\n\r\n
        for (int i = 6; i < split_msg.length; i++) { //every word past 5 is part of the message so group them together
            s.append(split_msg[i] + " ");
        }
        byte[] dataToWrite = String.valueOf(s).getBytes();

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
