package org.fudan.logProcess.jms;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.utils.MessageUtil;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.fudan.logProcess.service.LogIndexDataBase;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author Xu Rui
 * @date 2021/1/27 10:32
 */
@Slf4j
@Component
public class LogConsumerWithReply1 {

    @Resource
    LogIndexDataBase logIndexDataBase;

    /**
     * consumer object
     */
    private DefaultMQPushConsumer consumer;
    private DefaultMQProducer replyProducer;

    /**
     *  Construction method, instantiate object
     */
    public LogConsumerWithReply1() throws MQClientException {

        // Instantiate with specified consumer group name
        consumer = new DefaultMQPushConsumer(JmsConfigLogConsumer1.CONSUMER_GROUP);
        // Specify name server addresses
        consumer.setNamesrvAddr(JmsConfigLogConsumer1.NAME_SERVER);
        // Consumption mode: a new subscription group starts consumption for the first time from the last position of the queue, then starts consumption again, and then starts consumption according to the progress of the last consumption
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        // Subscribe one or more topics and tags for finding those messages need to be consumed
        consumer.subscribe(JmsConfigLogConsumer1.TOPIC, "*");

        // create a producer to send reply message
        replyProducer = new DefaultMQProducer(JmsConfigLogConsumer1.CONSUMER_GROUP + "_reply");
        replyProducer.setNamesrvAddr(JmsConfigLogConsumer1.NAME_SERVER);
        replyProducer.start();

        // Register callback to execute on arrival of messages fetched from brokers
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {

            for(Message msg:msgs) {

                try {
                    String body = new String(msg.getBody(), StandardCharsets.UTF_8);
                    log.info("handle message: {}", body);

                    log.info("handling...");

                    if(body.compareTo("handle error") == 0){
                        CommonResult<?> result = new CommonResult<>(BaseError.HANDLE_ERROR, body);
                        // create reply message with given util, do not create reply message by yourself
                        reply(msg, result.toString());
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

                    }else if (body.compareTo("exception error") == 0){
                        int a = 1 / 0;
                    }
                    else if (body.compareTo("timeout error") == 0){
                        TimeUnit.MILLISECONDS.sleep(15);
                    }
                    else{
                        String[] str = body.split(",");
                        CommonResult<?> logIndexResult =  logIndexDataBase.saveLog(str[0], str[1], Integer.parseInt(str[2]));

                        CommonResult<?> result = new CommonResult<>(BaseError.CONSUME_SUCCESS, logIndexResult);
                        // create reply message with given util, do not create reply message by yourself
                        reply(msg, result.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // create reply message with given util, do not create reply message by yourself
                    try {
                        CommonResult<?> result = new CommonResult<>(BaseError.EXCEPTION_ERROR, e.getMessage());
                        reply(msg, result.toString());
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        consumer.start();
        log.info("consumer start!" + this.getClass());
    }

    /**
     * Reply to producer that the message has been handled
     * @param msg           message
     * @param replyContent  reply content
     * @return              SendResult
     */
    public SendResult reply(Message msg, String replyContent) throws MQClientException, RemotingException, InterruptedException, MQBrokerException {
        // create reply message with given util, do not create reply message by yourself
        Message replyMessage = MessageUtil.createReplyMessage(msg, replyContent.getBytes());

        // send reply message with producer
        SendResult replyResult = replyProducer.send(replyMessage, JmsConfigLogConsumer1.REPLY_TIMEOUT);
        return replyResult;
    }
}
