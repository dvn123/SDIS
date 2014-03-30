import java.io.File;
import java.util.Vector;

public class Remove {

    MulticastMessageSender mcs;

    public Remove(MulticastMessageSender mcs) {
        this.mcs = mcs;
    }


    public long freeSpace(long sizeToDelete) {

        File dir = new File(MulticastProcessor.homeDir);

        if(!dir.isDirectory())
        {
            System.out.println("Specified dir is not a directory");
            System.exit(1);
        }

        File[]  dirFiles = dir.listFiles();
        long deletedSize = 0;

        Vector<String> deletedFiles = new Vector<String>();

        for (File file : dirFiles) {

            if(deletedSize >= sizeToDelete)
            {
                System.out.println("Freedom of Space.");
                break;
            }

            if (file.isFile())
            {
                deletedFiles.add(file.getName());
                deletedSize += file.length();
                file.delete();
            }
        }

        for(int i = 0; i < deletedFiles.size(); i++)
        {
            String[] split = deletedFiles.elementAt(i).split("-");
            String return_msg = "REMOVED " + MulticastProcessor.VERSION + " " + split[0] + " " + split[1] + " \r\n\r\n";

            mcs.send_message(return_msg);
        }

        if(deletedSize < sizeToDelete)
        {
            System.out.println("Couldn't delete any more files.");
        }

        return deletedSize;
    }

}
