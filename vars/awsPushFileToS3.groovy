#!/usr/bin/groovy
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * Places a file on AWS S3.
 * To use this file, the Jenkins instance must have downloaded the AWS SDK for Java.
 * See https://aws.amazon.com/sdk-for-java/
 * 
 * For a more complete example see:
 * https://github.com/awslabs/aws-java-sample/blob/master/src/main/java/com/amazonaws/samples/S3Sample.java
 */
def call(String bucketName, String key, String filepath) {
  try {
    AmazonS3 s3 = new AmazonS3Client();
    Region usWest2 = Region.getRegion(Regions.US_WEST_2);
    s3.setRegion(usWest2);
    File file = new File(filepath);
    s3.putObject(new PutObjectRequest(bucketName, key, file));
  } catch (AmazonServiceException ase) {
    println("Caught an AmazonServiceException, which means your request made it "
        + "to Amazon S3, but was rejected with an error response for some reason.");
    println("Error Message:    " + ase.getMessage());
    println("HTTP Status Code: " + ase.getStatusCode());
    println("AWS Error Code:   " + ase.getErrorCode());
    println("Error Type:       " + ase.getErrorType());
    println("Request ID:       " + ase.getRequestId());
  } catch (AmazonClientException ace) {
    println("Caught an AmazonClientException, which means the client encountered "
        + "a serious internal problem while trying to communicate with S3, "
        + "such as not being able to access the network.");
    println("Error Message: " + ace.getMessage());
  }
  catch (err) {
    echo "ERROR  ${err}"
  }
}
