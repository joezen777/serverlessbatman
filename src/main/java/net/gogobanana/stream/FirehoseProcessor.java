package net.gogobanana.stream;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehose;
import com.amazonaws.services.kinesisfirehose.model.*;
import net.gogobanana.common.Logger;

/**
 * Created by josephn on 12/13/2017.
 */
public class FirehoseProcessor {

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

    public FirehoseProcessor(String firehoseStream, String region, Logger logger,
                             ElasticsearchDestinationConfiguration elasticsearchDestinationConfiguration,
                             S3DestinationConfiguration s3DestinationConfiguration){
        this.firehoseStream = firehoseStream;
        this.region = region;
        this.logger = logger;
        this.elasticsearchDestinationConfiguration = elasticsearchDestinationConfiguration;
        this.s3DestinationConfiguration = s3DestinationConfiguration;
    }

    private void createFirehoseStreamIfNotExist(){

        AmazonKinesisFirehose amazonKinesisFirehose =
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
            this.deliveryStreamArn = describeDeliveryStreamResult.getDeliveryStreamDescription().getDeliveryStreamARN();
            logger.log("Delivery stream %s in region %s already exists with ARN %s",this.firehoseStream,
                    this.region, this.deliveryStreamArn);
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



            } catch (Exception e){
                logger.error(e,"Unable to create stream with configurations %s in region %s",this.firehoseStream,
                        this.region);
                throw e;
            }
        }


    }




}
