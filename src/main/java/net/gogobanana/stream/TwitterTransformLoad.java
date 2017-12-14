package net.gogobanana.stream;

/**
 * Created by josephn on 12/13/2017.
 */
public interface TwitterTransformLoad {

    Object TransformTweet(String tweet);

    void LoadTweet(Object transformedTweet);
}
