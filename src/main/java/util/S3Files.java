package util;

import java.awt.List;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieStore;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.HttpHeaders;
import javax.xml.ws.spi.http.HttpHandler;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringUtils;

public class S3Files {
	Logger logger = Logger.getLogger(TesseractOCR.class.getName());
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

	public S3Files(){
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
				String fileurl = "https://imstage.s3.amazonaws.com/"+objectSummary.getKey();
				URL url = new URL(fileurl);
				String disposition = null;
				String fileName = null;
				InputStream inputStream = null;
				try {
					HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
					if (httpconn instanceof HttpsURLConnection) {
						//httpconn.disconnect();
						HttpResponse response = connectWithSSL(fileurl);
						int status = response.getStatusLine().getStatusCode();
						if (status == HttpStatus.SC_OK) {
							HttpEntity entity = response.getEntity();
							if (entity != null) {
								if(response.getFirstHeader(HttpHeaders.CONTENT_DISPOSITION)!=null){
									disposition = 	response.getFirstHeader(HttpHeaders.CONTENT_DISPOSITION).getValue();
								}
								fileName = getFileNameFromDisposition(disposition, fileurl);
								inputStream = entity.getContent();
							}

						}

					}  else{
						int responseCode = httpconn.getResponseCode();
						if (responseCode == HttpURLConnection.HTTP_OK) {
							try {
								inputStream = httpconn.getInputStream();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}  

				String tempfile ="D:/tesseract/"+fileName;
				FileInputStream fis = null;
				File file = null;
				try{
					file= new File(tempfile);
					FileUtils.copyInputStreamToFile(inputStream, file);
				}catch (Exception e) {
					e.printStackTrace();
				}













				// URL url = new URL(fileurl);
				//				Parser parser = new AutoDetectParser();
				//				ContentHandler handler = new BodyContentHandler();
				//				TesseractOCRConfig config =new TesseractOCRConfig();
				//				logger.info("pdf parsing");
				//				config.setDensity(300);
				//				config.setDepth(8);
				//				config.setPageSegMode("11");
				//				PDFParserConfig pdfConfig = new PDFParserConfig();
				//				pdfConfig.setExtractInlineImages(true);
				//				pdfConfig.setExtractUniqueInlineImagesOnly(false);
				//				ParseContext parseContext = new ParseContext();
				//				parseContext.set(TesseractOCRConfig.class, config);	
				//				parseContext.set(PDFParserConfig.class, pdfConfig);
				//				parseContext.set(Parser.class, parser);
				//				
				//				try {
				//					//DataInputStream in = new DataInputStream(url.openStream());
				//					FileInputStream stream = new FileInputStream("https://imstage.s3.amazonaws.com/"+objectSummary.getKey());
				//					System.out.println(stream);
				//					Metadata metadata = new Metadata();
				//					try {
				//						parser.parse(stream, handler, metadata, parseContext);
				//					} catch (IOException e) {
				//						// TODO Auto-generated catch block
				//						e.printStackTrace();
				//					} catch (SAXException e) {
				//						// TODO Auto-generated catch block
				//						e.printStackTrace();
				//					} catch (TikaException e) {
				//						// TODO Auto-generated catch block
				//						e.printStackTrace();
				//					}
				//					logger.info("The metadata is : " +metadata  );
				//					String content = handler.toString();
				//					Charset UTF8_CHARSET = Charset.forName("UTF-8");
				//					byte[] b = content.getBytes();
				//					String str = new String(b, UTF8_CHARSET);
				//					System.out.println(str);
				//
				//				} catch (FileNotFoundException e) {
				//					// TODO Auto-generated catch block
				//					e.printStackTrace();
				//				}

			}
			object = files.getS3().listNextBatchOfObjects(object);
		} while (object.isTruncated());
	}

	public String getFileNameFromDisposition(String disposition, String fileurl) {
		// TODO Auto-generated method stub
		String fileName = "";
		if (disposition != null && !StringUtils.isNullOrEmpty(disposition)) {
			int index = disposition.indexOf("filename=\"");
			if (index >= 0) {
				fileName = disposition.substring(index + 10,
						disposition.length() -1 );
			}
			else {
				index = disposition.indexOf("filename=");
				if (index >= 0) {
					fileName = disposition.substring(index + 9,
							disposition.length());
				}
			}

		} else {
			// extracts file name from URL
			fileName = fileurl.substring(fileurl.lastIndexOf("/") + 1,
					fileurl.length());
		}

		try {
			fileName = URLDecoder.decode(fileName, CharEncoding.UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return fileName;
	}

	public HttpResponse connectWithSSL(String fileurl) {
		// TODO Auto-generated method stub
		int CONNECTION_TIMEOUT = 80000;
		SSLContext sslContext = null;
		try{
			sslContext = SSLContexts.custom()
					.loadTrustMaterial(null, (chain, authType) -> true).build();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
		org.apache.http.client.CookieStore cookieStore = (org.apache.http.client.CookieStore) new BasicCookieStore();
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		RequestConfig requestConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.DEFAULT)
				.setConnectionRequestTimeout(CONNECTION_TIMEOUT)
				.setConnectTimeout(CONNECTION_TIMEOUT)
				.setSocketTimeout(CONNECTION_TIMEOUT)
				.build();


		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.setDefaultCookieStore(cookieStore)
				.disableContentCompression()
				.setSSLSocketFactory(sslsf)
				.build();

		HttpGet httpGet = new HttpGet(fileurl);
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpGet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}






}