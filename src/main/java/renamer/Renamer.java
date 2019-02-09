package renamer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

		} catch (JpegProcessingException e) {
			throw new RuntimeException(e);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		if (directory == null) {
			return null;
		}
		return directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
	}

	boolean matchFileName(String fileName) {
		return !fileName.matches("^\\d\\d\\d\\d\\d\\d_\\d\\d\\d\\d\\d\\d_.+")
				&& (fileName.endsWith(".jpg") || fileName.endsWith(".JPG"));
	}

	String[] getJpegFiles(File directory) {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File f, String s) {
				return matchFileName(s);
			}
		};
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

	boolean renameFile(File file) {
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
		file.renameTo(new File(newFileName));
		return true;
	}

	void renameAllFilesInDirectory(File directory) {
		String[] jpegFiles = getJpegFiles(directory);
		int counter = 0;
		for (String filename : jpegFiles) {
			if (renameFile(new File(directory.getPath() + "/" + filename))) {
				counter++;
			}
		}
		System.out.println(counter + " files renamed.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("1 argument expected (directory name)");
			return;
		}
		File directory = new File(args[0]);
		if (!directory.exists() || !directory.isDirectory()) {
			System.out.println(args[0] + " is not an existing directory");
			return;
		}

		new Renamer().renameAllFilesInDirectory(directory);

	}

}
