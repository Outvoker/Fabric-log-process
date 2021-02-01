package org.fudan.logProcess.service;

import lombok.extern.slf4j.Slf4j;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.fudan.logProcess.jms.LogProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description log process task service
 * @author Xu Rui
 */
@Service
@Slf4j
public class LogTaskService {

    @Autowired
    LogProducer logProducer;

    /**
     * Asynchronously call request method to produce message to MQ, and wait for the result of consuming.
     * @param deferred  DeferredResult
     * @param tag       tag
     * @param msg       message
     */
    @Async
    public void request(DeferredResult<CommonResult<?>> deferred, String tag, String msg){
        log.info(Thread.currentThread().getName() + "enter taskService.request");
        try {
            CommonResult<?> result =  logProducer.request(tag, msg);
            log.info(Thread.currentThread().getName() + "request success: {}", result);
            deferred.setResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            deferred.setResult(new CommonResult<>(BaseError.PRODUCE_ERROR));
        }
    }

    /**
     * Same to {@link #request(DeferredResult, String, String)} with no tag.
     * @param deferred  DeferredResult
     * @param msg       message
     */
    @Async
    public void request(DeferredResult<CommonResult<?>> deferred, String msg){
        this.request(deferred, "", msg);
    }

    /**
     * Asynchronously call request method to produce messages to MQ, and wait for all the results of consuming.
     * @param deferred  DeferredResult
     * @param tag       tag
     * @param msgList   List of messages
     */
    @Async
    public void request(DeferredResult<CommonResult<?>> deferred, String tag, List<String> msgList){
        log.info(Thread.currentThread().getName() + "Enter taskService.request msgList");
        List<CommonResult<?>> deferredResultList = new ArrayList<>();
        for(String msg : msgList){
            DeferredResult<CommonResult<?>> deferredResult = new DeferredResult<>(3*1000L);
            this.request(deferredResult, tag, msg);
            deferredResult.onTimeout(() -> { //  return timeout
                log.info(Thread.currentThread().getName() + " onTimeout");
                deferredResult.setErrorResult(new CommonResult<>(BaseError.PRODUCE_TIMEOUT_ERROR));
            });

            deferredResultList.add((CommonResult<?>) deferredResult.getResult());
        }

        //  Judge whether each returned result is successful
        for(CommonResult<?> result : deferredResultList){
            if(result.isError()){
                deferred.setResult(new CommonResult<>(BaseError.PRODUCE_PARTLY_ERROR, deferredResultList));
                return;
            }
        }
        deferred.setResult(new CommonResult<>(BaseError.PRODUCE_SUCCESS, deferredResultList));
    }

    /**
     * Same to {@link #request(DeferredResult, String, List)} with no tag.
     * @param deferred  DeferredResult
     * @param msgList   List of messages
     */
    @Async
    public void request(DeferredResult<CommonResult<?>> deferred, List<String> msgList){
        this.request(deferred, "", msgList);
    }

}
