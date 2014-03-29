import java.util.ArrayList;
import java.util.Scanner;

public class Interface extends Thread {
    private ArrayList<byte[]> buffer;
    Scanner s;

    Interface(ArrayList<byte[]> buffer) {
        this.buffer = buffer;
    }

    private void keyboard() {
        System.out.println("Welcome to the Distributed File System\n");
        System.out.println("Possible Commands:\nbackup <file_name> <number_of_copies>\nrestore <file_name>\ndelete file_name\nfree <n_bytes_to_free>");
        s = new Scanner(System.in);
        while (true) {
            String s1 = s.nextLine();
            buffer.add(s1.toLowerCase().getBytes());
        }
    }

    public void run() {
        keyboard();
    }

    public void close() {
        s.close();
        System.exit(0);
    }
}