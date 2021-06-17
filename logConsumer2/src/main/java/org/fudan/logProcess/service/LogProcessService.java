package org.fudan.logProcess.service;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @author Xu Rui
 * @date 2021/1/27 16:48
 */
public interface LogProcessService {
    Boolean handle(List<MessageExt> messageExts, ConsumeConcurrentlyContext context);
}
