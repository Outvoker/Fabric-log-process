package org.fudan.webService.controller;

import lombok.extern.slf4j.Slf4j;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.fudan.webService.service.FabricSDKService;
import org.fudan.webService.service.LogProducerFeignService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RestController
@Slf4j
public class WebServiceController {
    @Resource
    private LogProducerFeignService logProducerFeignService;

    @Resource
    private FabricSDKService fabricSDKService;

    @PostMapping(value = "/log/push")
    public CommonResult<?> pushLog(@RequestBody String aLog){
        try {
            aLog = URLDecoder.decode(aLog, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        log.info("pushLog: {}", aLog);

        return new CommonResult<>(BaseError.PUSH_LOG_SUCCESS,logProducerFeignService.pushLog(aLog));
    }

    @PostMapping(value = "/log/pushAsync")
    public CommonResult<?> pushLogAsync(@RequestBody String aLog){
        try {
            aLog = URLDecoder.decode(aLog, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        log.info("pushLog: {}", aLog);

        return new CommonResult<>(BaseError.PUSH_LOG_SUCCESS,logProducerFeignService.pushLogAsync(aLog));
    }

    @GetMapping("/log/pull")
    CommonResult<?> pullLog(@RequestParam("key") String key){
        log.info("pullLog: {}", key);
        return new CommonResult<>(BaseError.PULL_LOG_SUCCESS, fabricSDKService.query(key));
    }
}
