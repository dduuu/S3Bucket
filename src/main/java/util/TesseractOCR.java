package util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.lang.time.StopWatch;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringUtils;
import com.google.common.base.Stopwatch;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.PDF;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.tika.parser.Parser;

	public class TesseractOCR {
		Logger logger = Logger.getLogger(TesseractOCR.class.getName());
	 //   private static final Logger logger = LogManager.getLogger(FileUtil.class);
		private AWSCredentials credentials;
		private AmazonS3 s3;
		//private static TransferManager tx;
		private static S3Files amazonS3Util;


		public AWSCredentials getCredentials() {
			return credentials;
		}

		public void setCredentials(AWSCredentials credentials) {
			this.credentials = credentials;
		}

		public AmazonS3 getS3() {
			return s3;
		}

		public void setS3(AmazonS3 s3) {
			this.s3 = s3;
		}


		public static synchronized S3Files getInstance(){
			if(amazonS3Util == null){
				amazonS3Util = new S3Files();
			}
			return amazonS3Util;
		}

		public TesseractOCR(){
			init();
		}

		public void init() {
			try {
				credentials = new BasicAWSCredentials("AKIAJ47W7OAWHKM3GDJQ","Ts527KHW4UNXYingY55bt50a/ifcAlKGWaJaeNJD");
			} catch (Exception e) {
				throw new AmazonClientException(
						"Cannot load the credentials from the profiles file." +
								"Please make sure that credentials are there in properties file",
								e);
			}

			this.s3 = AmazonS3ClientBuilder
					.standard()
					.withCredentials(new AWSStaticCredentialsProvider(credentials))
					.withRegion("us-west-1")
					.build();

			/*tx = TransferManagerBuilder
	                .standard()
	                .withS3Client(this.s3)
	                .build();*/

		}
		public void listFile() throws IOException{
			S3Files files = new S3Files();
			ObjectListing object = files.getS3().listObjects("imstage");

			do {
				for (S3ObjectSummary objectSummary : object.getObjectSummaries()) {
					
					System.out.println(objectSummary.getKey() + "\t" +
							objectSummary.getSize() + "\t" +
							StringUtils.fromDate(objectSummary.getLastModified()));
					String urls = "https://imstage.s3.amazonaws.com/" +objectSummary.getKey();
					String encodedURL=java.net.URLEncoder.encode(urls,"UTF-8");
					String g = urls.replace(" ","%20");
					try{
						
					  URL url = new URL(g);
					  byte[] fileChunk = new byte[10000];
					  
				        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				      
				        InputStream in = null;
				        String filename = url.getFile();
				        filename = filename.substring(filename.lastIndexOf('/') + 1);
				        String outfile = "D:/tesseract/" +  filename;
				        FileOutputStream out = new FileOutputStream(outfile);
				       // System.out.println(connection.getResponseCode());
				        in = connection.getInputStream();
				        int read = -1;
				        byte[] buffer = new byte[4096];
				        while((read = in.read(buffer)) != -1){
				            out.write(buffer, 0, read);
				           // System.out.println("[SYSTEM/INFO]: Downloading file...");
				        }
				        in.close();
				        out.close();
				        Stopwatch stopwatch = Stopwatch.createUnstarted();
						stopwatch.start();{
				        //scrapeData(outfile);
				        PdfPasre parse = new PdfPasre();
				        parse.pdfPasre(outfile);
				    	stopwatch.stop();
				    	logger.info("total time in sec "+stopwatch.elapsed(TimeUnit.SECONDS));
						}
				      //  scrapeData(outfile);
				        //PdfPasre(outfile);
				      
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
				object = files.getS3().listNextBatchOfObjects(object);
			
			} while (object.isTruncated());
		}

		

	/*
	    private void scrapeData(String outfile) throws IOException, SAXException, TikaException {
	    	StopWatch s =new StopWatch();
    	Parser parser = new AutoDetectParser();
			ContentHandler handler = new BodyContentHandler();
			TesseractOCRConfig config =new TesseractOCRConfig();
			//String tessdataPath = "C:/Program Files (x86)/Tesseract-OCR";
			logger.info("pdf parsing");
			//System.out.println("pdf parse");
			config.setTesseractPath("C:/Program Files (x86)/Tesseract-OCR");
			config.setDensity(300);
			config.setDepth(8);
			
			PDFParserConfig pdfConfig = new PDFParserConfig();
			pdfConfig.setExtractInlineImages(true);
			pdfConfig.setExtractUniqueInlineImagesOnly(false);
			ParseContext parseContext = new ParseContext();
			parseContext.set(TesseractOCRConfig.class, config);	
			parseContext.set(PDFParserConfig.class, pdfConfig);
			parseContext.set(Parser.class, parser);
//			//////////////////////////////////////////
//			
			FileInputStream stream = new FileInputStream(outfile);
	        Metadata metadata = new Metadata();
	     //   BufferedImage image = ImageIO.read(new File("D:/destanition/data1.png"));
	        
	        parser.parse(stream, handler, metadata, parseContext);
	      //  parser.parse(image, handler, metadata, parseContext);
	        logger.info("The metadata is : " +metadata  );
	       // System.out.println(metadata);
        String content = handler.toString();
	      //  String value = content.replaceFirst("ï¬�", "fi");
	       // System.out.println("#########################");
        
	        Charset UTF8_CHARSET = Charset.forName("UTF-8");
	        byte[] b = content.getBytes();
	        for (byte c: b) {
	            System.out.print("[" + c + "]");
	        }
	       String str = new String(b, UTF8_CHARSET);
	        System.out.println(str);
	      
	       // System.out.println(content);
	        System.out.println("Done");
			s.stop();
		   logger.info("Total time in milliseconds: " + s.getTime());
	       
//			// TODO Auto-generated method stub
//	    	
//	        
//			
	}
	*/

		private static URL verify(String url){
	        if(!url.toLowerCase().startsWith("http://")) {
	            return null;
	        }
	        URL verifyUrl = null;

	        try{
	            verifyUrl = new URL(url);
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	        return verifyUrl;
	    }
	}

