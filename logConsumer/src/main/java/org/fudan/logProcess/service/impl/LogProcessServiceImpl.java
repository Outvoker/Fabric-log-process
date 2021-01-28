package org.fudan.logProcess.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author Xu Rui
 * @date 2021/1/27 16:48
 */
@Service("logProcess")
@Slf4j
@Getter
public class LogProcessServiceImpl implements LogProcessService{

    private static final String POLICY_FILE = "D:\\university\\blockchain\\logProcess\\logConsumer\\src\\main\\resources\\test.yml";

    private LogHandler logHandler;

    LogProcessServiceImpl() throws FileNotFoundException {
        logHandler = new LogHandler(POLICY_FILE);
    }

    @Override
    public boolean handle(List<MessageExt> messageExts, ConsumeConcurrentlyContext context) {
        return false;
    }
}
