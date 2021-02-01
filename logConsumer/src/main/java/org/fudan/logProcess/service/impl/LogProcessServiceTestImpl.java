package org.fudan.logProcess.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.fudan.logProcess.jms.LogReplyProducer;
import org.fudan.logProcess.service.LogIndexDataBaseService;
import org.fudan.logProcess.service.LogProcessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Xu Rui
 * @date 2021/1/28 10:13
 */
@Service("test")
@Slf4j
public class LogProcessServiceTestImpl implements LogProcessService {

    @Resource
    LogIndexDataBaseService logIndexDataBase;

    @Resource
    LogReplyProducer logReplyProducer;

    @Override
    public boolean handle(List<MessageExt> messageExts, ConsumeConcurrentlyContext context) {
        for (Message msg : messageExts) {

            try {
                String body = new String(msg.getBody(), StandardCharsets.UTF_8);
                log.info("handle message: {}", body);

                log.info("handling...");

//                    logProcessService.getLogHandler().handle(body);

                if (body.compareTo("handle error") == 0) {
                    CommonResult<?> result = new CommonResult<>(BaseError.HANDLE_ERROR, body);
                    // create reply message with given util, do not create reply message by yourself
                    logReplyProducer.reply(msg, result.toString());
                    return true;

                } else if (body.compareTo("exception error") == 0) {
                    int a = 1 / 0;
                } else if (body.compareTo("timeout error") == 0) {
                    TimeUnit.MILLISECONDS.sleep(15);
                } else {
                    String[] str = body.split(",");
                    CommonResult<?> logIndexResult = logIndexDataBase.saveLog(str[0], str[1], Integer.parseInt(str[2]));

                    CommonResult<?> result = new CommonResult<>(BaseError.CONSUME_SUCCESS, logIndexResult);
                    // create reply message with given util, do not create reply message by yourself
                    logReplyProducer.reply(msg, result.toString());
                }

            } catch (Exception e) {
                e.printStackTrace();
                // create reply message with given util, do not create reply message by yourself
                try {
                    CommonResult<?> result = new CommonResult<>(BaseError.EXCEPTION_ERROR, e.getMessage());
                    logReplyProducer.reply(msg, result.toString());
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                return false;
            }
        }

        return true;
    }
}
