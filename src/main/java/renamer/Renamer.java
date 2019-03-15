package renamer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

/**
 * Rename JPEG files in a given directory by prefixing with date picture taken.
 * 
 * @author Oyvind
 * 
 */
public class Renamer {

    Date extract(File jpegFile) {
        ExifSubIFDDirectory directory;
        try {
            Metadata metadata = JpegMetadataReader.readMetadata(jpegFile);
            directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        } catch (JpegProcessingException | IOException e) {
            throw new RuntimeException(e);
        }
        if (directory == null) {
            return null;
        }
        return directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
    }

    static boolean matchFileName(String fileName) {
        return !fileName.matches("^\\d\\d\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d_.+")
                && (fileName.endsWith(".jpg") || fileName.endsWith(".JPG"));
    }

    String[] getJpegFiles(File directory) {
        FilenameFilter filter = (f, s) -> matchFileName(s);
        return directory.list(filter);
    }

    String datePrefix(File file) {
        Date dateTaken = extract(file);
        if (dateTaken == null) {
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss");
        return dateFormat.format(dateTaken);
    }

    private boolean renameFile(File file) {
        String datePrefix = datePrefix(file);
        if (datePrefix == null) {
            System.out.println(String.format(
                    "Cannot determine new filename for %s", file.getName()));
            return false;
        }
        String newFileName = file.getParent() + "/" + datePrefix + "_"
                + file.getName();
        System.out.println(String.format("Renaming %s to %s", file.getName(),
                newFileName));
        //noinspection ResultOfMethodCallIgnored
        file.renameTo(new File(newFileName));
        return true;
    }

    private void renameAllFilesInDirectory(File directory) {
        String[] jpegFiles = getJpegFiles(directory);
        int counter = 0;
        for (String filename : jpegFiles) {
            if (renameFile(new File(directory.getPath() + "/" + filename))) {
                counter++;
            }
        }
        System.out.println(counter + " files renamed.");
    }


    private static void watchDirectory(File directory) {
        Path path = directory.toPath();
        WatchService watcher;
        Renamer renamer = new Renamer();
        try {
            watcher = path.getFileSystem().newWatchService();
            path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        System.out.println(String.format("Monitoring directory %s for changes...", directory.toString()));
        for (;;) {
            WatchKey watchKey;
            try {
                watchKey = watcher.take();
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            }
            List<WatchEvent<?>> events = watchKey.pollEvents();
            for (WatchEvent event : events) {
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    String fileName = event.context().toString();
                    if (Renamer.matchFileName(fileName)) {
                        renamer.renameFile(new File(directory.getPath() + "/" + fileName));
                    }
                }
            }
            // the directory is inaccessible so exit the loop.
            boolean valid = watchKey.reset();
            if (!valid) {
                System.out.println("Directory no longer accessible. Exiting...");
                break;
            }
        }
	}


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("1 argument expected (directory name)");
            return;
        }
        String dirName = args[0];
        File directory = new File(dirName);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println(args[0] + " is not an existing directory");
            return;
        }
        watchDirectory(directory);
        new Renamer().renameAllFilesInDirectory(directory);

    }

}
