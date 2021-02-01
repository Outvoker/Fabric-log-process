<<<<<<< HEAD
package org.fudan.logProcess.service;
=======
package org.fudan.logProcess.service.impl;
>>>>>>> parent of dada66b... Merge branch 'main' of https://github.com/Outvoker/Fabric-log-process into main

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
<<<<<<< HEAD
import org.springframework.stereotype.Service;

=======
import org.fudan.logProcess.handler.LogHandler;
import org.fudan.logProcess.jms.LogReplyProducer;
import org.fudan.logProcess.service.LogIndexDataBaseService;
import org.fudan.logProcess.service.LogProcessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
>>>>>>> parent of dada66b... Merge branch 'main' of https://github.com/Outvoker/Fabric-log-process into main
import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author Xu Rui
 * @date 2021/1/27 16:48
 */
@Service("logProcess")
@Slf4j
@Getter
<<<<<<< HEAD
public class LogProcessServiceImpl implements LogProcessService{

    private static final String POLICY_FILE = "E:\\JAVA CODE\\SpringWorksapce\\Fabric-log-process\\logConsumer\\src\\main\\resources\\test.yml";

    private LogHandler logHandler;

    LogProcessServiceImpl() throws FileNotFoundException {
        logHandler = new LogHandler(POLICY_FILE);
=======
public class LogProcessServiceImpl implements LogProcessService {

    private static final String POLICY_FILE = "D:\\university\\blockchain\\logProcess\\logConsumer\\src\\main\\resources\\test.yml";

    LogHandler logHandler;

    @Resource
    LogIndexDataBaseService logIndexDataBase;

    @Resource
    LogReplyProducer logReplyProducer;

    LogProcessServiceImpl() throws FileNotFoundException {
        logHandler = new LogHandler(POLICY_FILE, logIndexDataBase, logReplyProducer);
>>>>>>> parent of dada66b... Merge branch 'main' of https://github.com/Outvoker/Fabric-log-process into main
    }

    @Override
    public boolean handle(List<MessageExt> messageExts, ConsumeConcurrentlyContext context) {
<<<<<<< HEAD
=======
        logHandler.handle(messageExts);
>>>>>>> parent of dada66b... Merge branch 'main' of https://github.com/Outvoker/Fabric-log-process into main
        return false;
    }
}
