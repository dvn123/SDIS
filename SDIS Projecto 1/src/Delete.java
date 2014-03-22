import java.io.File;
import java.io.FilenameFilter;

public class Delete {
    String file_id;
    File[] paths;

    Delete(String file_id) {
        this.file_id = file_id;
    }

    private void find_files() {
        FilenameFilter fileNameFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.matches("file_id");
            }
        };

        File dir = new File("./data");
        paths = dir.listFiles(fileNameFilter);
    }

    public int delete_files() {
        find_files();
        for (int i = 0; i < paths.length; i++) {
            paths[i].delete();
        }
        return 64000*paths.length;
    }
}
