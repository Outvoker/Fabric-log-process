package org.fudan.logProcess.controller;

import lombok.extern.slf4j.Slf4j;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
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


    /**
     * Push one log.
     * @param aLog  log
     * @return      CommonResult
     */
    @PostMapping("/log/push")
    public CommonResult<?> pushLog(@RequestBody String aLog) {
        log.info("Main thread " + Thread.currentThread().getName() + ": Enter pushLog Method :{}", aLog);
        return logSingleTaskService.request(aLog);
    }

}
