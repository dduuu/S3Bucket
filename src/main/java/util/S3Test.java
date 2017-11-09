package util;

import java.io.IOException;
import java.nio.file.Files;

//import org.apache.commons.lang.time.StopWatch;

import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringUtils;

public class S3Test {
	public static int pagecount=0;
	public static long time=0;
	public static void main(String[] args) throws IOException {
		//final int pagecount =0;
//    S3Files files =new S3Files();
//    files.init();
		//StopWatch watch = new StopWatch();
		//watch.start();
    TesseractOCR ocr =new TesseractOCR();
    ocr.init();
//    for(Bucket bucket :ocr.getS3().listBuckets()){
//    	System.out.println(bucket.getName());
//    }
  // String bucket = files.getS3().getBucketLocation("imstage");
 //  System.out.println(bucket);
//    for(Grant b1 : bucket.)
//    	System.out.println(b1);
//	}
//	
//   ObjectListing object = files.getS3().listObjects("imstage");
//  // System.out.println(object);
//   
//   do {
//       for (S3ObjectSummary objectSummary : object.getObjectSummaries()) {
//               System.out.println(objectSummary.getKey() + "\t" +
//                       objectSummary.getSize() + "\t" +
//                       StringUtils.fromDate(objectSummary.getLastModified()));
//       }
//       object = files.getS3().listNextBatchOfObjects(object);
//} while (object.isTruncated());
   ocr.listFile();
 //  watch.stop();
}
}
