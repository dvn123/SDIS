import java.io.File;

public class Delete {
    final String fileId;

    /* DELETE <FileId> <CRLF> <CRLF> */

    Delete(String fileIdd) {
        this.fileId = fileIdd;
    }

    public long delete_files() {
        File dir = new File(MulticastProcessor.homeDir);
        File[] dirFiles = dir.listFiles();
        int chunksDeleted = 0;
        long deletedSize = 0;

        for (File file : dirFiles) {

            String fileName = file.getName();

            if(fileName.startsWith(fileId))
            {
                deletedSize += file.length();
                file.delete();
                chunksDeleted++;
            }
        }

        return deletedSize;
    }

}
