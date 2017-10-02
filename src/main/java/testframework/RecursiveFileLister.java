package testframework;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RecursiveFileLister {

    private static Logger logger = LoggerFactory.getLogger(RecursiveFileLister.class);
    private List<File> fileList = new ArrayList<File>();

    RecursiveFileLister(String directory, FileFilter fileFilter) throws FileNotFoundException {
        File dir = new File(directory);
        if (!dir.exists()) {
            throw new FileNotFoundException(String.format("Directory %s not found", directory));
        }

        if (!dir.isDirectory()) {
            if (fileFilter.accept(dir)) {
                logger.debug("Add {} file", dir.getName());
                this.fileList.add(dir);
                return;
            } else {
                return;
            }
        }

        listFilesInDir(dir, fileFilter);

    }

    private void listFilesInDir(File dir, FileFilter fileFilter) {
        logger.debug("Start iterating {} directory", dir.getName());
        try {
            for (File f : dir.listFiles(fileFilter)) {
                if (!f.isDirectory()) {
                    logger.debug("Add {} file", f.getName());
                    this.fileList.add(f);
                } else {
                    listFilesInDir(f, fileFilter);
                }
            }
        } catch (NullPointerException e) {
            logger.error("No files in {} dir", dir.getName());
        }
    }

    List<File> getFilesList() {
        return this.fileList;
    }
}

class JsonFiles implements FileFilter {
    public boolean accept(File f) {
        return f.getName().endsWith(".json") & f.getName().startsWith("test") || f.isDirectory();
    }
}

class AllFiles implements FileFilter {
    public boolean accept(File f) {
        return true;
    }
}
