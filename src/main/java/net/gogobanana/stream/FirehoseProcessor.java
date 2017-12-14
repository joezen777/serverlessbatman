package net.gogobanana.stream;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehose;
import com.amazonaws.services.kinesisfirehose.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.gogobanana.common.Logger;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by josephn on 12/13/2017.
 */
public class FirehoseProcessor implements TwitterTransformLoad {

    public String getFirehoseStream() {
        return firehoseStream;
    }

    public String getRegion() {
        return region;
    }

    private final String firehoseStream;
    private final String region;
    private final Logger logger;
    private ElasticsearchDestinationConfiguration elasticsearchDestinationConfiguration;
    private S3DestinationConfiguration s3DestinationConfiguration;
    private String deliveryStreamArn;
    AmazonKinesisFirehose amazonKinesisFirehose;
    private final static ObjectMapper JSON = new ObjectMapper();

    private AtomicBoolean initialized = new AtomicBoolean();

    public FirehoseProcessor(String firehoseStream, String region, Logger logger,
                             ElasticsearchDestinationConfiguration elasticsearchDestinationConfiguration,
                             S3DestinationConfiguration s3DestinationConfiguration){
        this.firehoseStream = firehoseStream;
        this.region = region;
        this.logger = logger;
        this.elasticsearchDestinationConfiguration = elasticsearchDestinationConfiguration;
        this.s3DestinationConfiguration = s3DestinationConfiguration;
        this.initialized.set(false);
    }



    private void createFirehoseStreamIfNotExist(){

        this.amazonKinesisFirehose =
                com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseClientBuilder.standard()
                        .withRegion(this.region).build();

        boolean deliveryStreamExists = true;
        try {
            DescribeDeliveryStreamResult describeDeliveryStreamResult =
                    amazonKinesisFirehose.describeDeliveryStream(
                            new DescribeDeliveryStreamRequest().withDeliveryStreamName(this.firehoseStream));

            if (describeDeliveryStreamResult.getDeliveryStreamDescription() == null ||
                    describeDeliveryStreamResult.getDeliveryStreamDescription().getDeliveryStreamARN() == null) {
                deliveryStreamExists = false;
            }
            else {
                this.deliveryStreamArn = describeDeliveryStreamResult.getDeliveryStreamDescription().getDeliveryStreamARN();
                logger.log("Delivery stream %s in region %s already exists with ARN %s", this.firehoseStream,
                        this.region, this.deliveryStreamArn);
                this.initialized.set(true);
            }
        }
        catch (Exception e){
            logger.error(e,"Error describing existing firehost client %s in region %s",this.firehoseStream,this.region);
            deliveryStreamExists = false;
        }

        if (deliveryStreamExists == false){
            try {
                CreateDeliveryStreamResult createDeliveryStreamResult =
                amazonKinesisFirehose.createDeliveryStream(new CreateDeliveryStreamRequest()
                    .withDeliveryStreamName(this.firehoseStream)
                    .withElasticsearchDestinationConfiguration(this.elasticsearchDestinationConfiguration)
                        .withS3DestinationConfiguration(this.s3DestinationConfiguration));
                this.deliveryStreamArn = createDeliveryStreamResult.getDeliveryStreamARN();
                logger.log("Created new stream %s in region %s with ARN %s",this.firehoseStream,this.region,
                        this.deliveryStreamArn );

                DescribeDeliveryStreamResult describeDeliveryStreamResult =
                        amazonKinesisFirehose.describeDeliveryStream(
                                new DescribeDeliveryStreamRequest().withDeliveryStreamName(this.firehoseStream));

                int count = 1;

                while (describeDeliveryStreamResult != null &&
                        describeDeliveryStreamResult.getDeliveryStreamDescription() != null &&
                        (describeDeliveryStreamResult.getDeliveryStreamDescription().getDeliveryStreamStatus() == null ||
                        !describeDeliveryStreamResult.getDeliveryStreamDescription().getDeliveryStreamStatus().equalsIgnoreCase("ACTIVE")) ){
                    try {
                        Thread.sleep(2000L * ((2 ^ count++) / 2));
                    }catch (InterruptedException e){
                        logger.error(e,"Sleep interrupted");
                    }
                    describeDeliveryStreamResult =
                            amazonKinesisFirehose.describeDeliveryStream(
                                    new DescribeDeliveryStreamRequest().withDeliveryStreamName(this.firehoseStream));
                    if (count > 10){
                        logger.error("Service not ready and in status of %s after 10 retries",
                                describeDeliveryStreamResult.getDeliveryStreamDescription().getDeliveryStreamStatus());
                        break;
                    }
                }

                this.initialized.set(true);

            } catch (Exception e){
                logger.error(e,"Unable to create stream with configurations %s in region %s",this.firehoseStream,
                        this.region);
                throw e;
            }
        }


    }

    public byte[] toJsonAsBytes(String obj) {
        try {
            return JSON.writeValueAsBytes(obj);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Record TransformTweet(String tweet) {

        logger.log("Putting tweet: %s" ,tweet);
        byte[] bytes = toJsonAsBytes(tweet);
        // The bytes could be null if there is an issue with the JSON serialization by the Jackson JSON library.
        if (bytes == null) {
            logger.error("Could not get JSON bytes for tweet");
            return null;
        }


        Record putRecord = new Record()
                .withData(ByteBuffer.wrap(bytes));

        return putRecord;
    }

    @Override
    public void LoadTweet(Record transformedTweet) {
        if (!this.initialized.get()){
            createFirehoseStreamIfNotExist();
        }

        try {
            PutRecordRequest putRecordRequest = new PutRecordRequest()
                    .withRecord(transformedTweet)
                    .withDeliveryStreamName(firehoseStream);

            amazonKinesisFirehose.putRecord(putRecordRequest);
        } catch (Exception ex){
            logger.error(ex,"Error sending message to amazon kinesis firehose");
        }
    }
}
