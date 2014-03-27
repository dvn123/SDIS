import java.io.File;
import java.util.Vector;

public class Remove {
    static Vector<String> deletedFiles;

    public static long freeSpace(long sizeToDelete) {

        File dir = new File(MulticastProcessor.homeDir);

        if(!dir.isDirectory())
        {
            System.out.println("Specified dir is not a directory");
            System.exit(1);
        }

        File[]  dirFiles = dir.listFiles();
        long deletedSize = 0;
        deletedFiles = new Vector<String>();

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

        if(deletedSize < sizeToDelete)
        {
            System.out.println("Couldn't delete any more files.");
        }

        return deletedSize;
    }

}
