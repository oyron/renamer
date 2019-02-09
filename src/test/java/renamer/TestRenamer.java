package renamer;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class TestRenamer {

	@Test
	public void testExtract() throws Exception {
		URL jpegFileURL = this.getClass().getResource("testimage.jpg");
		File jpegFile = new File(jpegFileURL.toURI());
		Date datePictureTaken = new Renamer().extract(jpegFile);
		Date expectedDate = new GregorianCalendar(2009, 2, 22, 13, 50, 34).getTime();
		assertEquals(expectedDate, datePictureTaken);
	}

	@Test
	public void testExtractNoMetadata() throws Exception {
		URL jpegFileURL = this.getClass().getResource("testimage2.jpg");
		File jpegFile = new File(jpegFileURL.toURI());
		Date datePictureTaken = new Renamer().extract(jpegFile);
		assertNull(datePictureTaken);
	}

	@Test
	public void testGetJpegFiles() throws Exception {
		URL jpegFileURL = this.getClass().getResource("testimage.jpg");
		File jpegFile = new File(jpegFileURL.toURI());
		File directory = new File(jpegFile.getParent());
		String[] jpegFiles = new Renamer().getJpegFiles(directory);
		assertTrue(jpegFiles.length == 2);
	}

	@Test
	public void testDatePrefix() throws Exception {
		URL jpegFileURL = this.getClass().getResource("testimage.jpg");
		File jpegFile = new File(jpegFileURL.toURI());
		String datePrefix = new Renamer().datePrefix(jpegFile);
		assertEquals("090322_135034", datePrefix);
	}

	@Test
	public void testMatchFileName() {
		String notToMatch = "010101_010101_abc.jpg";
		String notToMatch2 = "010101_010101_abc.JPG";
		String toMatch = "a010101_010101_abc.jpg";
		String toMatch2 = "010101010101_abc.jpg";
		Renamer renamer = new Renamer();
		assertFalse(renamer.matchFileName(notToMatch));
		assertFalse(renamer.matchFileName(notToMatch2));
		assertTrue(renamer.matchFileName(toMatch));
		assertTrue(renamer.matchFileName(toMatch2));		
	}
	
}
