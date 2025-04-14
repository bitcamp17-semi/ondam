package config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class S3ClientConfig {

    private final NaverConfig naverConfig;

    @Bean
    public AmazonS3 naverS3Client() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(
                naverConfig.getAccessKey(),
                naverConfig.getSecretKey()
        );

        AwsClientBuilder.EndpointConfiguration endpointConfig =
                new AwsClientBuilder.EndpointConfiguration(
                        naverConfig.getEndPoint(),
                        naverConfig.getRegionName()
                );

        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(endpointConfig)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }
}
