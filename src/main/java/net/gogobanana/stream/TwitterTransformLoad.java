package net.gogobanana.stream;

import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest;
import com.amazonaws.services.kinesisfirehose.model.Record;

/**
 * Created by josephn on 12/13/2017.
 */
public interface TwitterTransformLoad {

    Record TransformTweet(String tweet);

    void LoadTweet(Record transformedTweet);
}
