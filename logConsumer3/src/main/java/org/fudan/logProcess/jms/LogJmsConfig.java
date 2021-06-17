package org.fudan.logProcess.jms;

/**
 * @author Xu Rui
 * @Description: consumer config
 */
public class LogJmsConfig {
    /**
     * Name server address, because it is a cluster deployment, so there are multiple semicolons separated
     */
    public static final String NAME_SERVER = "10.176.25.51:9876";

    /**
     * Topic name topic is usually set by the server and cannot be created in the code (if it is not created properly, the producer will report that the topic cannot be found when sending a message to the topic)
     */
    public static final String TOPIC = "topic1";
    public static final String CONSUMER_GROUP = "test_consumer";
    public static final long REPLY_TIMEOUT = 3000L;
}
