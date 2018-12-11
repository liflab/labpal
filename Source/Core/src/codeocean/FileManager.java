package ca.uqac.lif.codeocean;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;

import ca.uqac.lif.labpal.config.Config;
import ca.uqac.lif.labpal.config.Config.ENV;

public class FileManager {

	public static enum FileType {
		PNG, DUMB, PDF, HTML
	};

	public static StringBuilder getFileContent(String st) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(st), Charset.defaultCharset());
		StringBuilder sb = new StringBuilder();
		for (String c : lines) {
			sb.append(c);
		}
		return sb;
	}

	public static void mkdir(String strPath) {

		File dir = new File(strPath);

		if (!dir.exists()) {

			dir.mkdir();
		}
	}

	/**
	 * 
	 * @param pathAbsolute
	 * @param fileName
	 * @param content
	 * @throws IOException
	 */

	public static void writeFile(String pathAbsolute, String fileName, String format, String content)
			throws IOException {
		// "C:\\Users\\chafik.meniar\\Documents\\test\\" + filename + "." + ext

		String strPath = pathAbsolute;
		File dir = new File(strPath);

		if (!dir.exists()) {

			dir.mkdir();
		}

		try (PrintWriter out = new PrintWriter(pathAbsolute + fileName + format)) {
			out.println(content);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeFile(String pathAbsolute, String fileName, String format, byte[] content)
			throws IOException {
		// "C:\\Users\\chafik.meniar\\Documents\\test\\" + filename + "." + ext

		if (FileType.PNG.toString().toLowerCase().equals(format.substring(1))) {
			saveImage(pathAbsolute, fileName, content);
		} else if (FileType.PDF.toString().toLowerCase().equals(format.substring(1))) {
			savePdf(pathAbsolute, fileName, content);
		} else {
			/*
			 * File f = new File(pathAbsolute+fileName+format); ZipOutputStream out = new
			 * ZipOutputStream(new FileOutputStream(f)); ZipEntry e = new ZipEntry();
			 * out.putNextEntry(e); out.write(content); out.closeEntry();
			 * 
			 * out.close();
			 */
		}

	}

	static void saveImage(String pathAbsolute, String fileName, byte[] byte_array) throws IOException {
		String strPath = pathAbsolute;
		File dir = new File(strPath);

		if (!dir.exists()) {

			dir.mkdir();
		}

		ByteArrayInputStream input_stream = new ByteArrayInputStream(byte_array);
		BufferedImage final_buffered_image = ImageIO.read(input_stream);
		ImageIO.write(final_buffered_image, "png", new File(pathAbsolute + fileName + ".png"));

	}

	static void savePdf(String pathAbsolute, String fileName, byte[] bytes) throws IOException {
		String strPath = pathAbsolute;
		File dir = new File(strPath);

		if (!dir.exists()) {

			dir.mkdir();
		}
		// below is the different part
		File someFile = new File(pathAbsolute + fileName + ".pdf");
		FileOutputStream fos = new FileOutputStream(someFile);
		fos.write(bytes);
		fos.flush();
		fos.close();

	}

	public static StringBuilder readFile(String fileName, String format, String path) throws IOException {
		FileInputStream inputStream = null;
		StringBuilder st = new StringBuilder();

		if (ENV.WEB.equals(Config.env)) {
			try {
				String root_path = "ca/uqac/lif/codeocean/";
				String folder = "resource/";
				String chemin = root_path + folder + fileName;
				// Getting ClassLoader obj
				ClassLoader classLoader = FileManager.class.getClassLoader();
				// Getting resource(File) from class loader
				File configFile = new File(classLoader.getResource(chemin).getFile());
				inputStream = new FileInputStream(configFile);
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				while ((line = reader.readLine()) != null) {
					st.append(line);
				}

				reader.close();

			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			} finally {
				inputStream.close();
			}

			return st;
		} else {

			String cheminTemplate = path + fileName + format;
			return getFileContent(cheminTemplate);
		}

	}

	@SuppressWarnings("deprecation")
	public static byte[]   mergePdF(String dest, String... paths) {
		try {
			ByteArrayOutputStream out = null;	
			List<File> lst = new ArrayList<File>();
			List<PDDocument> lstPDD = new ArrayList<>();
			for (String path : paths) {
				File file1 = new File(path);

				lstPDD.add(PDDocument.load(file1));
				lst.add(file1);
			}
			PDFMergerUtility PDFmerger = new PDFMergerUtility();

			// Setting the destination file
			PDFmerger.setDestinationFileName(dest);
			for (File file : lst) {
				// adding the source files
				PDFmerger.addSource(file);

			}
			// Merging the two documents
			PDFmerger.mergeDocuments();
            
			System.out.println("Documents merged");
			
			for(PDDocument pdd:lstPDD)
				pdd.close();
					
			PDDocument pdf=	PDDocument.load( new File(dest));

			 out = new ByteArrayOutputStream();

			 pdf.save(out);
		     byte[] data = out.toByteArray();
			 pdf.close();
			return data;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      return null;
	}

}
