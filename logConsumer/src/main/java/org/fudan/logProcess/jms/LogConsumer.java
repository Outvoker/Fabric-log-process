package org.fudan.logProcess.jms;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.fudan.logProcess.service.LogProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Xu Rui
 * @date 2021/1/27 10:32
 */
@Slf4j
@Component
public class LogConsumer {

    @Resource(name="logHandler")
    private LogProcessService logProcessService;

    /**
     * consumer object
     */
    private DefaultMQPushConsumer consumer;

    /**
     *  Construction method, instantiate object
     */
    public LogConsumer() throws MQClientException {

        // Instantiate with specified consumer group name
        consumer = new DefaultMQPushConsumer(LogJmsConfig.CONSUMER_GROUP);
        // Specify name server addresses
        consumer.setNamesrvAddr(LogJmsConfig.NAME_SERVER);
        // Consumption mode: a new subscription group starts consumption for the first time from the last position of the queue, then starts consumption again, and then starts consumption according to the progress of the last consumption
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        // Subscribe one or more topics and tags for finding those messages need to be consumed
        consumer.subscribe(LogJmsConfig.TOPIC, "*");

        // Register callback to execute on arrival of messages fetched from brokers
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {

            if(logProcessService.handle(msgs, context)) return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;   //  consume success
            else return ConsumeConcurrentlyStatus.RECONSUME_LATER;  //  consume failed, reconsume later


        });

        consumer.start();
        log.info("consumer start!" + this.getClass());
    }


}
