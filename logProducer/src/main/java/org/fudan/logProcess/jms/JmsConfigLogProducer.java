package org.fudan.logProcess.jms;


/**
 * @Description: producer config
 * @author Xu Rui
 */
public class JmsConfigLogProducer {

    /**
     * Name Server addr, if it is a cluster deployment, use semicolons to separate it
     */
    public static final String NAME_SERVER = "10.176.25.51:9876";

    /**
     * topic name
     */
    public static final String TOPIC = "topic1";

    /**
     * a producer group name
     */
    public static final String PRODUCER_GROUP = "test_producer";

    /**
     * timeout (ms)
     */
    public static long TIMEOUT = 30 * 1000L;

}
