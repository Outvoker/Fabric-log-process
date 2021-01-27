package org.fudan.logProcess.rocketMQ;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.springframework.stereotype.Component;

/**
 * @Description: 生产者消息
 * @author Xu Rui
 * @date 2021/1/26 10:15
 */
@Slf4j
//@Component
public class LogProducer {

    private String producerGroup = "test_producer";

    private DefaultMQProducer producer;

    public LogProducer(){
        //示例生产者
        producer = new DefaultMQProducer(producerGroup);
        //不开启vip通道 开通口端口会减2
        producer.setVipChannelEnabled(false);
        //绑定name server
        producer.setNamesrvAddr(JmsConfig.NAME_SERVER);
        start();
    }

    /**
     * 对象在使用之前必须要调用一次，只能初始化一次
     */
    public void start(){
        try {
            this.producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }

    public CommonResult<?> send(String tag, String msg){
        SendResult sendResult;
        try {
            sendResult = producer.send(new Message(JmsConfig.TOPIC, tag, msg.getBytes()));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(BaseError.PRODUCE_ERROR);
        }
        return new CommonResult<>(BaseError.PRODUCE_SUCCESS, sendResult);
    }

    public CommonResult<?> send(String msg){
        SendResult sendResult;
        try {
            sendResult = producer.send(new Message(JmsConfig.TOPIC, "", msg.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(BaseError.PRODUCE_ERROR);
        }
        return new CommonResult<>(BaseError.PRODUCE_SUCCESS, sendResult);
    }

    public DefaultMQProducer getProducer(){
        return this.producer;
    }

    /**
     * 一般在应用上下文，使用上下文监听器，进行关闭
     */
    public void shutdown(){
        this.producer.shutdown();
    }

}
