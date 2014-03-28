import java.util.ArrayList;
import java.util.Scanner;

public class Interface extends Thread {
    private MulticastMessageSender mcs;
    private MulticastMessageSender mcrs;
    private MulticastMessageSender mcbs;
    private ArrayList<byte[]> buffer;

    Interface(ArrayList<byte[]> buffer) {
        this.buffer = buffer;
    }

    private void keyboard() {
        System.out.println("Welcome to the Distributed File System\n");
        System.out.println("Possible Commands:\nbackup <file_name> <number_of_copies>\nrestore <file_name>\ndelete file_name\nfree <n_bytes_to_free>");
        Scanner s = new Scanner(System.in);
        while (true) {
            String s1 = s.nextLine();
            buffer.add(s1.toLowerCase().getBytes());
        }
    }

    public void run() {
        keyboard();
    }
}