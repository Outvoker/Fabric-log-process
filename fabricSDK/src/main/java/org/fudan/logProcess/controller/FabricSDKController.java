package org.fudan.logProcess.controller;

import lombok.extern.slf4j.Slf4j;
import org.fudan.logProcess.entity.BlockchainLog;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.fudan.logProcess.service.FabricServiceInterface;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;


/**
 * @Program: Fabric-log-process
 * @Description: interact with information
 * @Author: HouHao Ye
 * @Create: 2021-01-29 15:09
 **/
@RestController
@Slf4j
public class FabricSDKController {

    @Resource(description = "fabricService")
    FabricServiceInterface fabricServiceInterface;

    @PostMapping("/fabric/invoke")
    public DeferredResult<CommonResult<?>> invoke(@RequestParam("key") String key, @RequestParam("value") String value) {
        log.info("receive a request that invokes key = {}, value = {}", key, value);
        // get the information that will be saved into blockchain
        BlockchainLog blockchainLog = new BlockchainLog(key, value);

        // set the waiting period
        DeferredResult<CommonResult<?>> deferredResult = new DeferredResult<>(10 * 1000L);
        // Asynchronously call fabric to invoke
        Boolean flag = fabricServiceInterface.invoke(deferredResult, blockchainLog);

        // when timeout this callback method
        deferredResult.onTimeout(() -> {
            log.info(Thread.currentThread().getName() + "onTimeout");
            deferredResult.setErrorResult(new CommonResult<>(BaseError.BLOCKCHAIN_INVOKE_TIMEOUT_ERROR, blockchainLog));
        });

        // when callback method is completed, whether it is timeout or successful, it will enter this callback method
        deferredResult.onCompletion(() -> {
            log.info(Thread.currentThread().getName() + " onCompletion");
            // Asynchronously save into blockchain
            if(flag) {
                deferredResult.setErrorResult(new CommonResult<>(BaseError.BLOCKCHAIN_INVOKE_SUCCESS, blockchainLog));
            } else {
                deferredResult.setErrorResult(new CommonResult<>(BaseError.BLOCKCHAIN_INVOKE_ERROR, blockchainLog));
            }
        });
        return deferredResult;
    }

    @GetMapping("/fabric/query")
    public DeferredResult<CommonResult<?>> query(@RequestParam("key") String key) {
        log.info("receive a request that queries key = {}", key);

        // set the waiting period
        DeferredResult<CommonResult<?>> deferredResult = new DeferredResult<>( 10L);
        // get the information that queried from blockchain
        BlockchainLog blockchainLog = fabricServiceInterface.query(deferredResult, key);

        // when timeout this callback method
        deferredResult.onTimeout(() -> {
            log.info(Thread.currentThread().getName() + "onTimeout");
            deferredResult.setErrorResult(new CommonResult<>(BaseError.BLOCKCHAIN_INVOKE_TIMEOUT_ERROR, blockchainLog));
        });

        // when callback method is completed, whether it is timeout or successful, it will enter this callback method
        deferredResult.onCompletion(() -> {
            log.info(Thread.currentThread().getName() + " onCompletion");
            // check if queried successfully
            if (blockchainLog != null) {
                deferredResult.setErrorResult(new CommonResult<>(BaseError.BLOCKCHAIN_QUERY_SUCCESS, blockchainLog));
            } else {
                deferredResult.setErrorResult(new CommonResult<>(BaseError.BLOCKCHAIN_QUERY_ERROR));
            }
        });

        return deferredResult;
    }
}
