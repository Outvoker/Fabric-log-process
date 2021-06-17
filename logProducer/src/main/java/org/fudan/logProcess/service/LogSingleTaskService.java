package org.fudan.logProcess.service;

import lombok.extern.slf4j.Slf4j;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.fudan.logProcess.jms.LogProducer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description log process task service
 * @author Xu Rui
 */
@Service
@Slf4j
public class LogSingleTaskService {

    @Resource
    LogProducer logProducer;

    /**
     * Asynchronously call request method to produce message to MQ, and wait for the result of consuming.
     * @param tag       tag
     * @param msg       message
     */
    public CommonResult<?> request(String tag, String msg){
        log.info(Thread.currentThread().getName() + "enter taskService.request");
        try {
            CommonResult<?> result =  logProducer.request(tag, msg);
            log.info(Thread.currentThread().getName() + "request success: {}", result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(BaseError.PRODUCE_ERROR);
        }
    }

    /**
     * Same to {@link #request(String, String)} with no tag.
     * @param msg       message
     */
    public CommonResult<?> request(String msg){
        return this.request("", msg);
    }


    public CommonResult<?> send(String msg){
        return logProducer.send(msg);
    }


}
