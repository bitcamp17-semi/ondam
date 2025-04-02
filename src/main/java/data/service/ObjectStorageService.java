package data.service;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import config.NaverConfig;

@Service
public class ObjectStorageService {
    private String bucketName;
    private String endPoint;
    private String optimizerEndPoint;
    private AmazonS3 s3Client;

    public ObjectStorageService(NaverConfig naverConfig) {
        this.bucketName = naverConfig.getBucketName();
        this.endPoint = naverConfig.getEndPoint();
        this.optimizerEndPoint = naverConfig.getOptimizerEndPoint();
        s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(naverConfig.getEndPoint(), naverConfig.getRegionName()))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(naverConfig.getAccessKey(), naverConfig.getSecretKey())))
                .build();

        List<CORSRule.AllowedMethods> methodRule = new ArrayList<CORSRule.AllowedMethods>();
        methodRule.add(CORSRule.AllowedMethods.PUT);
        methodRule.add(CORSRule.AllowedMethods.GET);
        methodRule.add(CORSRule.AllowedMethods.POST);
        CORSRule rule = new CORSRule().withId("CORSRule")
                .withAllowedMethods(methodRule)
                .withAllowedHeaders(Arrays.asList(new String[] { "*" }))
                .withAllowedOrigins(Arrays.asList(new String[] { "*" }))
                .withMaxAgeSeconds(3600);

        List<CORSRule> rules = new ArrayList<CORSRule>();
        rules.add(rule);

        // Add rules to new CORS configuration.
        BucketCrossOriginConfiguration configuration = new BucketCrossOriginConfiguration();
        configuration.setRules(rules);

        // Set the rules to CORS configuration.
        s3Client.setBucketCrossOriginConfiguration(bucketName, configuration);

        // Get the rules for CORS configuration.
        configuration = s3Client.getBucketCrossOriginConfiguration(bucketName);


//		if (configuration == null) {
//		    System.out.println("Configuration is null.");
//		} else {
//		    System.out.println("Configuration has " + configuration.getRules().size() + " rules\n");
//
//		    for (CORSRule getRule : configuration.getRules()) {
//		        System.out.println("Rule ID: " + getRule.getId());
//		        System.out.println("MaxAgeSeconds: " + getRule.getMaxAgeSeconds());
//		        System.out.println("AllowedMethod: " + getRule.getAllowedMethods());
//		        System.out.println("AllowedOrigins: " + getRule.getAllowedOrigins());
//		        System.out.println("AllowedHeaders: " + getRule.getAllowedHeaders());
//		        System.out.println("ExposeHeader: " + getRule.getExposedHeaders());
//		        System.out.println();
//		    }
//		}
    }

    public String uploadFile(String bucketName, String directoryPath, MultipartFile file) {
//		System.out.println("uploadFile=" + file.getOriginalFilename());

        if (file.isEmpty()) {
            return null;
        }

        try (InputStream fileIn = file.getInputStream()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_");
            String filename = sdf.format(new Date()) + UUID.randomUUID().toString();

            // 확장자 처리 추가 (확장자가 없거나 비어 있으면 기본값 .file 추가)
            String originalFilename = file.getOriginalFilename();
            String[] splitFileName = originalFilename != null ? originalFilename.split("\\.") : null;
            if (splitFileName != null && splitFileName.length > 1) {
                filename += "." + splitFileName[splitFileName.length - 1];
            } else {
                filename += ".file"; // 기본 확장자
            }

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            PutObjectRequest objectRequest = new PutObjectRequest(bucketName, directoryPath + "/" + filename, fileIn, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);

            s3Client.putObject(objectRequest);

            return filename;

        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 오류", e);
        }
    }

    public String generateFileURL(String bucketName, String filePath) {
        // NCloud Object Storage 엔드포인트
        String endpoint = "https://kr.object.ncloudstorage.com";
        // NCloud URL 형식 반환
        return endpoint + "/" + bucketName + "/" + filePath; // 버킷 이름과 파일 경로 조합
    }

    public void deleteFile(String bucketName, String directoryPath, String fileName) {
        String path = directoryPath + "/" + fileName;
        boolean isfind = s3Client.doesObjectExist(bucketName, path);
        if (isfind) {
            s3Client.deleteObject(bucketName, path);
//			System.out.println(path + ":삭제완료");
        }
    }

    public String generatePresignedURL(String bucketName, String filePath, String fileName, int expiration) {
        if (bucketName == null || filePath == null || bucketName.isEmpty() || filePath.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 버킷 이름 또는 파일 경로입니다.");
        }

        // 파일 경로
        String fullPath = "dataroom/" + filePath;

        // Ncloud EndPoint
        String endpoint = "https://kr.object.ncloudstorage.com";

        // Content-Disposition 헤더 설정
        String contentDisposition;
        try {
            // `filename`은 원문 그대로, `filename*`은 UTF-8로 한 번만 인코딩
            contentDisposition = String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s",
                    fileName,
                    java.net.URLEncoder.encode(fileName, "UTF-8")
            );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("파일 이름 UTF-8 인코딩 오류", e);
        }

        // 최종 URL 생성
        return String.format("%s/%s/%s?response-content-disposition=%s&response-content-type=%s",
                endpoint,
                bucketName,
                fullPath,
                encodeURIComponent(contentDisposition), // Content-Disposition 전체를 안전하게 보장
                encodeURIComponent("application/octet-stream") // MIME type 설정
        );
    }

    // URL-safe 처리 메서드
    private String encodeURIComponent(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8")
                    .replaceAll("\\+", "%20") // 공백 공간 처리
                    .replaceAll("%21", "!")
                    .replaceAll("%27", "'")
                    .replaceAll("%28", "(")
                    .replaceAll("%29", ")")
                    .replaceAll("%7E", "~");
        } catch (Exception e) {
            throw new RuntimeException("URL-safe 인코딩 오류", e);
        }
    }

    // 파일 이름 추출 메서드
    private String getOriginalFilename(String filePath) {
        if (filePath.contains("/")) {
            return filePath.substring(filePath.lastIndexOf("/") + 1);
        }
        return filePath;
    }




    public String getBucketName() {
        return bucketName;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public String getOptimizerEndPoint() {
        return optimizerEndPoint;
    }
}
