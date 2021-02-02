package org.fudan.logProcess.controller;

import lombok.extern.slf4j.Slf4j;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.fudan.logProcess.service.LogMultiTaskService;
import org.fudan.logProcess.service.LogSingleTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

/**
 * @Description Responding to HTTP requests.
 * @author Xu Rui
 */
@RestController
@Slf4j
public class LogProducerController {

    @Autowired
    LogSingleTaskService logSingleTaskService;

    @Autowired
    LogMultiTaskService logMultiTaskService;

    /**
     * Push one log.
     * @param aLog  log
     * @return      CommonResult
     */
    @PostMapping("/log/push")
    public DeferredResult<CommonResult<?>> pushLog(@RequestBody String aLog) {
        log.info("Main thread " + Thread.currentThread().getName() + ": Enter pushLog Method");

        // timeout with 3 * 1000 ms
        DeferredResult<CommonResult<?>> deferredResult = new DeferredResult<>(30*1000L);
        //  Asynchronously call
        logSingleTaskService.request(deferredResult, aLog);

        // timeout callback method
        deferredResult.onTimeout(() -> {
            log.info(Thread.currentThread().getName() + " onTimeout");
            // return timeout
            deferredResult.setErrorResult(new CommonResult<>(BaseError.PRODUCE_TIMEOUT_ERROR, aLog));
        });

        // When the callback method is completed, whether it is timeout or successful, it will enter this callback method
        deferredResult.onCompletion(() -> log.info(Thread.currentThread().getName() + " onCompletion"));

        return deferredResult;
    }

    @PostMapping("/log/pushBatch")
    public DeferredResult<CommonResult<?>> pushLogs(@RequestBody List<String> logs) {
        log.info("Main thread " + Thread.currentThread().getName() + ": Enter pushLogs Method");

        //  timeout with 20 * 1000 ms
        DeferredResult<CommonResult<?>> deferredResult = new DeferredResult<>(30*1000L);

        //  Asynchronously call
        logMultiTaskService.request(deferredResult, logs);

        // timeout callback method
        deferredResult.onTimeout(() -> {
            log.info(Thread.currentThread().getName() + " onTimeout");
            deferredResult.setErrorResult(new CommonResult<>(BaseError.PRODUCE_TIMEOUT_ERROR, logs));
        });

        return deferredResult;
    }
}
