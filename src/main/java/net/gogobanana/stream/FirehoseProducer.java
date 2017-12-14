package net.gogobanana.stream;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import net.gogobanana.common.Logger4j;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by josephn on 12/9/2017.
 */
public class FirehoseProducer {

    public static void main(String[] args) throws Exception {

        net.gogobanana.common.Logger logger = new Logger4j(null);

        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
        BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(1000);

/** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
// Optional: set up some followings and track terms
        //List<Long> followings = Lists.newArrayList(1234L, 566788L);
        //List<String> terms = Lists.newArrayList("twitter", "api");
        List<Location> locations = Lists.newArrayList(
                new Location(new Location.Coordinate(-175.957031,14.944785),
                        new Location.Coordinate(-32.695313,75.320025)),
                new Location(new Location.Coordinate(77.233887,12.726084),
                        new Location.Coordinate(77.915039,13.293411)),
                new Location(new Location.Coordinate(-13.007813,48.341646),
                        new Location.Coordinate(4.042969,63.860036)));
        //hosebirdEndpoint.followings(followings);
        //hosebirdEndpoint.trackTerms(terms);


// These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1("FXAWMskArUtQaHgY2dWYSw74U",
                "Q0CQdBUMc0AzvSkSGPkQMbrbJe2I0tMIt5vOBdTlw5TSYGNDve",
                "753796896804581376-127xFEXLdy2lgBRyuoP5eVxiizPIMQ1",
                "4A1Mg39mBKtB2UDhwrVynWlftlQXxwhmYTzZ7GJTnSi3m");

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue))
                .eventMessageQueue(eventQueue);                          // optional: use this if you want to process client events

        Client hosebirdClient = builder.build();
// Attempts to establish a connection.
        hosebirdClient.connect();

        Thread t = new Thread() { // Create an anonymous inner class extends Thread
            @Override
            public void run() {
                while (!hosebirdClient.isDone()) {
                    try {
                        if (msgQueue.isEmpty() == false) {
                            String msg = msgQueue.take();
                            logger.log(msg);
                        }
                        else {
                            logger.log("No messages, waiting...");
                            Thread.sleep(5000L);
                        }
                    }catch (InterruptedException e){
                        logger.error(e,"error retrieving from queue");
                    }

                }
            }
        };
        t.start();




        System.err.println("CTRL^C to end service.");



        try {
            while (true)
                Thread.sleep(5000);
        }
        catch (Exception e) {
            logger.error(e,"Quitting");
        }
        hosebirdClient.stop();

        System.exit(0);
    }
}
