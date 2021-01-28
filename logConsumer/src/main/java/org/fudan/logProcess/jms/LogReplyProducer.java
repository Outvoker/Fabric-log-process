package org.fudan.logProcess.jms;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.utils.MessageUtil;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description reply that the message has been consumed
 * @author Xu Rui
 * @date 2021/1/27 17:45
 */
@Slf4j
@Component
public class LogReplyProducer {

    private DefaultMQProducer replyProducer;

    LogReplyProducer() throws MQClientException {
        // create a producer to send reply message
        replyProducer = new DefaultMQProducer(LogJmsConfig.CONSUMER_GROUP + "_reply");
        replyProducer.setNamesrvAddr(LogJmsConfig.NAME_SERVER);
        replyProducer.start();
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
        SendResult replyResult = replyProducer.send(replyMessage, LogJmsConfig.REPLY_TIMEOUT);
        return replyResult;
    }

    /**
     * Reply to producer that the messages batch have been handled
     * @param msgBatch      message batch
     * @param replyContent  reply content
     * @return              SendResult
     */
    public List<SendResult> reply(List<Message> msgBatch, String replyContent) throws MQClientException, RemotingException, InterruptedException, MQBrokerException {
        List<SendResult> res = new ArrayList<>();
        for(Message msg : msgBatch)
            res.add(this.reply(msg, replyContent));
        return res;
    }
}
