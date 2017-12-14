package net.gogobanana.stream;

import com.amazonaws.services.kinesisfirehose.model.PutRecordRequest;

/**
 * Created by josephn on 12/13/2017.
 */
public interface TwitterTransformLoad {

    PutRecordRequest TransformTweet(String tweet);

    void LoadTweet(PutRecordRequest transformedTweet);
}
