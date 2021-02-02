package org.fudan.webService.controller;

import lombok.extern.slf4j.Slf4j;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.fudan.webService.service.LogProducerFeignService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;

@RestController
@Slf4j
public class WebServiceController {
    @Resource
    private LogProducerFeignService logProducerFeignService;
    @PostMapping("/log/push")
    public CommonResult<?> pushLog(@RequestBody String aLog){
        log.info("pushLog: {}", aLog);
        return new CommonResult<>(BaseError.PUSH_LOG_SUCCESS,logProducerFeignService.pushLog(aLog));
    }
}
