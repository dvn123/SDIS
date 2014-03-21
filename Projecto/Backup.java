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

import java.io.*;

public class Backup extends Thread{

    final char CR = '\r';
    final char LF = '\n';
    char[] version = new char[3];
    char[] fileId = new char[256];
    int replicationDeg;
    int chunkNo;
    static String msg_received;
    static String[] split_msg;

    public static void main(String args[]) throws InterruptedException, IOException {

        //Receber PUTCHUNK do canal multicast.
        msg_received = "PUTCHUNK 1.0 2012 9 \r\n \r\n bodyz";

        //Construir a string de armazenamento.
        parse_msg();

        //Random delay (100ms - 400ms)
        Thread.sleep(100 + (int)(Math.random() * ((400 - 100) + 1)));

        //Armazenar o chunk
        byte dataToWrite[] = split_msg[4].getBytes("UTF-8");
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(split_msg[2]);
        }
        catch (FileNotFoundException ioe) {
            System.out.println("Error while creating file " + ioe);
        }

        out.write(dataToWrite);
        out.close();


        //Responder com mensagem.

    }

    public static void parse_msg() {
        split_msg = msg_received.split("\\s+");

        for(int i = 0; i < split_msg.length; i++)
        {
            System.out.println("Splitted " + i + ": " + split_msg[i]);
        }
    }

    public void answer()
    {
        String answer = "STORED " + version + " " +  fileId + " " + chunkNo + CR + LF + " " + CR + LF;
        System.out.println("ANSWER: " + answer);
    }

}
