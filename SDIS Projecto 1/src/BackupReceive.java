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
import java.net.MulticastSocket;

public class BackupReceive extends Thread {

    static String msg_received;
    static String[] split_msg;
    static String msg_returned;
    final char CR = '\r';
    final char LF = '\n';
    char[] version = new char[3];
    char[] fileId = new char[256];
    //int replicationDeg;
    int chunkNo;
    MulticastMessageSender mcs;

    BackupReceive(MulticastMessageSender mcs) {
        this.mcs = mcs;
    }

    private static void parse_msg() {
        split_msg = msg_received.split("\\s+");

        for (int i = 0; i < split_msg.length; i++) {
            System.out.println("Split " + i + ": " + split_msg[i]);
        }
    }

    public void run() {

        //Receber PUTCHUNK do canal multicast.
        //msg_received = "PUTCHUNK 1.0 2012 9 \r\n \r\n bodyz";

        //Construir a string de armazenamento.
        parse_msg();

        //Random delay (100ms - 400ms)
        try {
            Thread.sleep(100 + (int) (Math.random() * ((400 - 100) + 1)));
        } catch (InterruptedException e) {
            System.out.println("Error sleeping.");
        }

        //Armazenar o chunk

        byte dataToWrite[] = null;
        try {
            dataToWrite = split_msg[4].getBytes("UTF-8");
        } catch (UnsupportedEncodingException ioe) {
            System.out.println("Error converting data.");
        }


        FileOutputStream out = null;

        try {
            out = new FileOutputStream(split_msg[2]);
        } catch (FileNotFoundException ioe) {
            System.out.println("Error while creating file " + ioe);
        }


        try {
            out.write(dataToWrite);
            out.close();
        } catch (IOException e) {
            System.out.println("Error outputting to file.");
        }


        msg_returned = answer();
    }

    private String answer() {
        String answer = "STORED " + version + " " + fileId + " " + chunkNo + CR + LF + " " + CR + LF;
        System.out.println("ANSWER: " + answer);

        return answer;
    }

    private void send_message(String answer) {

        mcs.send_message(msg_returned);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            System.out.println("?");
        }
    }

}
