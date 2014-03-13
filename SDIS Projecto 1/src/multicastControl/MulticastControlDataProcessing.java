package multicastControl;

/**
 * Created by Diogo on 12-03-2014.
 */
public class MulticastControlDataProcessing implements Runnable {
    private String message;
    private boolean log;

    private void check_message() {

    }

    MulticastControlDataProcessing(String message, boolean log) {
        this.message = message;
        this.log = log;
    }

    public void run() {
        if(log)
            System.out.println("[MulticastControlDataProcessing] Processing data.");

        return;
    }
}
