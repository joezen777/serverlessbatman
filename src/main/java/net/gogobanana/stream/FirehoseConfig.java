package net.gogobanana.stream;

import com.amazonaws.services.kinesisfirehose.model.*;

/**
 * Created by josephn on 12/13/2017.
 */
public final class FirehoseConfig {

    public static ElasticsearchDestinationConfiguration getElasticSearchDestinationConfiguration(){
        ElasticsearchDestinationConfiguration config = new ElasticsearchDestinationConfiguration()
                .withBufferingHints(new ElasticsearchBufferingHints().withIntervalInSeconds(30).withSizeInMBs(5))
                .withDomainARN("arn:aws:es:us-east-1:661744025157:domain/bananas")
                .withIndexName("bananas")
                .withIndexRotationPeriod("NoRotation")
                .withRetryOptions(new ElasticsearchRetryOptions().withDurationInSeconds(300))
                .withRoleARN("arn:aws:iam::661744025157:role/firehose_delivery_role");
        return config;
    }

    public static S3DestinationConfiguration getS3DestinationConfiguration() {
        S3DestinationConfiguration config = new S3DestinationConfiguration()
                .withBucketARN("arn:aws:s3:::serverlessbatmandatadump")
                .withRoleARN("arn:aws:iam::661744025157:role/firehose_delivery_role")
                .withBufferingHints(new BufferingHints().withIntervalInSeconds(300).withSizeInMBs(5))
                .withCompressionFormat("UNCOMPRESSED")
                .withPrefix("sb-twitter");
        return config;
    }
}
