package org.fudan.logProcess.jms;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Description: producer
 * @author Xu Rui
 */
@Slf4j
@Component
public class LogProducer {

    /**
     * DefaultMQProducer
     */
    private DefaultMQProducer producer;

    /**
     *  Construction method
     */
    public LogProducer(){
        producer = new DefaultMQProducer(JmsConfigLogProducer.PRODUCER_GROUP);
        //  If the VIP channel is not opened, the open port will be reduced by 2
        producer.setVipChannelEnabled(false);
        //  Bind name server
        producer.setNamesrvAddr(JmsConfigLogProducer.NAME_SERVER);
        start();
    }

    /**
     * Object must be called once before use, and can only be initialized once
     */
    public void start(){
        try {
            this.producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send request message in synchronous mode. This method returns only when the consumer consume the request message and reply a message.
     * @param tag   tag
     * @param msg   msg
     * @return      CommonResult
     */
    public CommonResult<?> request(String tag, String msg){
        Message requestResult;
        try {
            requestResult = producer.request(new Message(JmsConfigLogProducer.TOPIC, tag, msg.getBytes()), JmsConfigLogProducer.TIMEOUT);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(BaseError.PRODUCE_ERROR);
        }

        String body = new String(requestResult.getBody(), StandardCharsets.UTF_8);
        log.info(body);
        CommonResult<?> consumeResult = CommonResult.parse(body);
        if(consumeResult.isError()) return new CommonResult<>(BaseError.CONSUME_ERROR, consumeResult);

        return new CommonResult<>(BaseError.PRODUCE_SUCCESS, CommonResult.parse(body));
    }

    /**
     * Same to {@link #request(String, String)} with no tag;
     * @param msg   msg
     * @return      CommonResult
     */
    public CommonResult<?> request(String msg){
        return this.request("", msg);
    }

    /**
     * Send message in synchronous mode.
     * @param tag   tag
     * @param msg   message
     * @return      CommonResult
     */
    public CommonResult<?> send(String tag, String msg){
        SendResult sendResult;
        try {
            sendResult = producer.send(new Message(JmsConfigLogProducer.TOPIC, tag, msg.getBytes()));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(BaseError.PRODUCE_ERROR);
        }
        return new CommonResult<>(BaseError.PRODUCE_SUCCESS, sendResult);
    }

    /**
     * Same to {@link #send(String, String)} with tag is null.
     * @param msg   message
     * @return      CommonResult
     */
    public CommonResult<?> send(String msg){
        return this.send("", msg);
    }

    /**
     * Send messages in synchronous mode.
     * @param tag       tag
     * @param msgList   List of messages
     * @return          CommonResult
     */
    public CommonResult<?> send(String tag, List<String> msgList){
        SendResult sendResult;
        try {
            Collection<Message> messages = new ArrayList<>();
            for(String msg : msgList) messages.add(new Message(JmsConfigLogProducer.TOPIC, tag, msg.getBytes()));
            sendResult = producer.send(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(BaseError.PRODUCE_ERROR);
        }
        return new CommonResult<>(BaseError.PRODUCE_SUCCESS, sendResult);
    }

    /**
     * Same to {@link #send(String, List<String>)} with tag is null.
     * @param msgList   List of messages
     * @return          CommonResult
     */
    public CommonResult<?> send(List<String> msgList){
        return this.send("", msgList);
    }

    /**
     * get DefaultMQProducer
     * @return  DefaultMQProducer
     */
    public DefaultMQProducer getProducer(){
        return this.producer;
    }

    /**
     * in the application context, the context listener is used to close
     */
    public void shutdown(){
        this.producer.shutdown();
    }

}
