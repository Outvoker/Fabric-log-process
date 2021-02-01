package org.fudan.logProcess.service;

import lombok.extern.slf4j.Slf4j;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xu Rui
 * @date 2021/1/31 15:33
 */
@Slf4j
@Service
public class LogMultiTaskService {

    @Autowired
    LogSingleTaskService logSingleTaskService;

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
            DeferredResult<CommonResult<?>> deferredResult = new DeferredResult<>(3*10L);
            logSingleTaskService.request(deferredResult, tag, msg);
            deferredResult.onTimeout(() -> { //  return timeout
                log.info(Thread.currentThread().getName() + " onTimeout");
                deferredResult.setErrorResult(new CommonResult<>(BaseError.PRODUCE_TIMEOUT_ERROR));
            });
            deferredResultList.add((CommonResult<?>) deferredResult.getResult());
            deferredResult.onCompletion(() -> {
                log.info(Thread.currentThread().getName() + " onCompletion logSingleTaskService.request size = {}", deferredResultList.size());
                if(deferredResultList.size() == msgList.size()){
                    //  Judge whether each returned result is successful
                    for(CommonResult<?> result : deferredResultList){
                        if(result.isError()){
                            deferred.setResult(new CommonResult<>(BaseError.PRODUCE_PARTLY_ERROR, deferredResultList));
                            return;
                        }
                    }
                    deferred.setResult(new CommonResult<>(BaseError.PRODUCE_SUCCESS, deferredResultList));
                }
            });
        }
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
