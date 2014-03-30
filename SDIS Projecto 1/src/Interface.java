import java.util.ArrayList;
import java.util.Scanner;

public class Interface extends Thread {
    Scanner s;
    private ArrayList<String> buffer;

    Interface(ArrayList<String> buffer) {
        this.buffer = buffer;
    }

    private void keyboard() {
        System.out.println("Welcome to the Distributed File System\n");
        System.out.println("Possible Commands:\nbackup <file_name> <number_of_copies>\nrestore <file_name>\ndelete file_name\nreclaim <n_bytes_to_free>\nexit");
        s = new Scanner(System.in);
        while (true) {
            String s1 = s.nextLine();
            buffer.add(s1.toLowerCase());
        }
    }

    public void run() {
        keyboard();
    }

    public void close() {
        s.close();
    }
}