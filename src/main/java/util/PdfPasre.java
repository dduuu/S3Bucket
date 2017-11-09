package util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ws.rs.core.NewCookie;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import com.google.common.base.Stopwatch;



public class PdfPasre {


	public void pdfPasre(String outfile) throws IOException, SAXException, TikaException{
		
		//		String filePath = "D:/Example.pdf";
		//		String outputFilePath = "E:/IM";
		//		File inputFile = new File(filePath);
		//		File outputFolder = new File(outputFilePath);
		//		String totalFileName = inputFile.getName();
		//		String fileName = totalFileName.substring(0,totalFileName.lastIndexOf("."));
		//		PDDocument doc = null;
		//		try {
		//			doc = PDDocument.load(inputFile);
		//			PDFRenderer pdfRenderer = new PDFRenderer(doc);
		//			
		//			int pageCounter = 0;
		//			//doc.getPage(pageCounter).setCropBox(pdfRenderer.);;
		//			//doc.getPage(pageCounter).getCropBox().setLowerLeftY(600);
		//			//doc.getPage(pageCounter).getCropBox().setUpperRightX(100);
		//			//doc.getPage(pageCounter).getCropBox().setUpperRightY(100);
		//			System.out.println(doc);
		//			for(PDPage page : doc.getPages())
		//				//doc.getPage(pageCounter).getCropBox().setLowerLeftX(200);
		//				//doc.getPage(pageCounter).getCropBox().setLowerLeftY(200);
		//				//doc.getPage(pageCounter).getCropBox().setUpperRightX(100);
		//				//doc.getPage(pageCounter).getCropBox().setUpperRightY(100);
		//			{
		//				BufferedImage bim = pdfRenderer.renderImageWithDPI(pageCounter, 300, ImageType.RGB);
		//				//BufferedImage bim = pdfRenderer.renderImage(pageCounter,2.0f);
		//				
		//				String imagename = outputFilePath + "\\" +fileName+ (pageCounter++) +".png";
		//				System.out.println(imagename);
		//				  ImageIOUtil.writeImage(bim,  imagename , 300);
		//				
		//				
		//				System.out.println("Png image is created " +imagename );
		//				ContentHandler handler = new BodyContentHandler();
		//				TesseractOCRConfig config =new TesseractOCRConfig();
		//				config.setTesseractPath("C:/Program Files (x86)/Tesseract-OCR");
		//				ParseContext parseContext = new ParseContext();
		//				parseContext.set(TesseractOCRConfig.class, config);
		//				TesseractOCRParser parser = new TesseractOCRParser();
		//				FileInputStream stream = new FileInputStream(imagename);
		//				System.out.println("Image is parse for scraping......." );
		//				Metadata metadata = new Metadata();
		//		        parser.parse(stream, handler, metadata, parseContext);
		//		        System.out.println(metadata);
		//		        String content = handler.toString();
		//		        
		//		        System.out.println(content);
		//		        System.out.println("scrapping successfully................................................!!");
		//		        System.out.println("#########################");
		//				
		//			}
		//			doc.close();
		//		} finally {
		//			if (doc != null) {
		//				doc.close();
		//			}
		//
		//		}



		//Parser parser = new AutoDetectParser();
		//BodyContentHandler handler = new BodyContentHandler();
		//TesseractOCRParser parser = new TesseractOCRParser();
		Logger logger = Logger.getLogger(PdfPasre.class.getName());
		//PDFRenderer pdfRenderer = new PDFRenderer(doc);
		/*
		HttpGet httpget = new HttpGet(in); 
	    HttpEntity entity = null;
	    HttpClient client = new DefaultHttpClient();
	    HttpResponse response = client.execute(httpget);
	    entity = response.getEntity();
	    if (entity != null) {
	    	InputStream instream = entity.getContent();
	    	*/
		
		PDDocument doc = PDDocument.load(new File(outfile));
		int count = doc.getNumberOfPages();
		logger.info("Total pages of current doc is  "+count);
		S3Test.pagecount = S3Test.pagecount+count;
//
		logger.info("Total no of Page is : "+S3Test.pagecount);
		Parser parser = new AutoDetectParser();
		//ContentHandler handler = new BodyContentHandler();
		BodyContentHandler handler = new BodyContentHandler(-1);
		TesseractOCRConfig config =new TesseractOCRConfig();
		//String tessdataPath = "C:/Program Files (x86)/Tesseract-OCR";
		//logger.info("pdf parsing");
		//System.out.println("pdf parse");
		config.setTesseractPath("C:/Program Files (x86)/Tesseract-OCR");
		config.setDensity(300);
		config.setDepth(8);
		config.setPageSegMode("12");
		PDFParserConfig pdfConfig = new PDFParserConfig();
		pdfConfig.setExtractInlineImages(true);
		pdfConfig.setExtractUniqueInlineImagesOnly(false);
		ParseContext parseContext = new ParseContext();
		parseContext.set(TesseractOCRConfig.class, config);	
		parseContext.set(PDFParserConfig.class, pdfConfig);
		parseContext.set(Parser.class, parser);
		//////////////////////////////////////////

		FileInputStream stream = new FileInputStream(outfile);
		Metadata metadata = new Metadata();
		//   BufferedImage image = ImageIO.read(new File("D:/destanition/data1.png"));
		
		parser.parse(stream, handler, metadata, parseContext);
		//  parser.parse(image, handler, metadata, parseContext);
		//logger.info("The metadata is : " +metadata  );
		// System.out.println(metadata);
		//String content = handler.toString();
		//  String value = content.replaceFirst("ï¬�", "fi");
		
		String normalisedText = Normalizer.normalize(handler.toString(), Normalizer.Form.NFD).trim();

		
		Charset UTF8_CHARSET = Charset.forName("UTF-8");
		byte[] b = normalisedText.getBytes();
		for (byte c: b) {
			 System.out.print("[" + c + "]");
		}
		String str = new String(b, UTF8_CHARSET);
		String adjusted = str.replaceAll("(?m)^[ \t]*\r?\n", "");
		String value = adjusted.replaceAll("[^\\p{ASCII}]", "");
		System.out.println(value);

		// System.out.println(content);
		System.out.println("Done");
		}
		
		//int totaltime =  (int) s.getTime();
		//logger.info("Timing of current documents "+count+ " page is: "+s.getTime());
		//S3Test.time = S3Test.time+totaltime;
		//logger.info("Total time of " +S3Test.pagecount+ " pages in milliseconds: " + S3Test.time);
	
	
}
