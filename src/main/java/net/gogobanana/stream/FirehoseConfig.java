package net.gogobanana.stream;

import com.amazonaws.services.kinesisfirehose.model.ElasticsearchBufferingHints;
import com.amazonaws.services.kinesisfirehose.model.ElasticsearchDestinationConfiguration;

/**
 * Created by josephn on 12/13/2017.
 */
public class FirehoseConfig {

    public static ElasticsearchDestinationConfiguration getElasticSearchDestinationConfiguration(String deliveryStreamName){
        ElasticsearchDestinationConfiguration config = new ElasticsearchDestinationConfiguration()
                .withBufferingHints(new ElasticsearchBufferingHints().withIntervalInSeconds(30).withSizeInMBs(5))
                .withDomainARN()
    }
}
