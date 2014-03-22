import org.omg.CORBA.INTERNAL;

import java.io.*;
import java.net.MulticastSocket;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.nio.file.Files;


public class BackupSend {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    FileInputStream fis = null;
    FileOutputStream fos = null;

    MulticastMessageSender mcs;

    BackupSend(MulticastMessageSender mcs) {
        this.mcs = mcs;
    }


    public void breakFile(String sourceFilePath) throws IOException {

        int chunkCount = 0;

        File f = new File(sourceFilePath);
        String fileName = f.getName();

        try {

            fis = new FileInputStream(sourceFilePath);

            byte[] buffer = new byte[64000];

            int noOfBytes;

            System.out.println("Copying file using streams");

            // read bytes from source file and write to destination file

            while ((noOfBytes = fis.read(buffer)) != -1) {
                String destFile =  fileName+chunkCount;
                fos = new FileOutputStream(destFile);
                fos.write(buffer, 0, noOfBytes);
                System.out.println("Creating chunk: " + chunkCount);
                chunkCount++;

                if (fos != null) {
                    fos.close();
                }
            }
        }

        catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        }

        catch (IOException ioe) {
            System.out.println("Exception while copying file " + ioe);
        }

        finally {
            // close the streams using close method
            try {
                if (fis != null) {
                    fis.close();
                }
            }
            catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }

        String fileId = createFileId(sourceFilePath);

        System.out.println("FileId " + fileId);

        MessageDigest digest;
        byte[] hash = null;

        try {
            digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(fileId.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error while converting to SHA-256");
            e.printStackTrace();
        }


        System.out.println("HASH: " + hash);

        char[] hexChars = getChars(hash);

        System.out.print("HEX: ");
        System.out.println(hexChars);
    }

    private void send_chunks(Byte[] b) {
        for (int i = 0; i < b.length; i++) {
            mcs.send_message("PUTCHUNK " + Interface.VERSION + " fileId" + b[i] + "\n\n ");
        }
    }

    private static String createFileId(String sourceFilePath) throws IOException {

        Path file = Paths.get(sourceFilePath);
        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);

        System.out.println("creationTime: " + attr.creationTime());
        System.out.println("lastAccessTime: " + attr.lastAccessTime());
        System.out.println("lastModifiedTime: " + attr.lastModifiedTime());

        String ret = new String("" + attr.creationTime() + attr.lastAccessTime() + attr.lastModifiedTime());

        return ret;
    }

    public static char[] getChars(byte[] hash) {

        char[] hexChars = new char[hash.length * 2];
        for ( int j = 0; j < hash.length; j++ ) {
            int v = hash[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return hexChars;
    }


}




