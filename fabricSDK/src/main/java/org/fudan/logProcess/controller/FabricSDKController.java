package org.fudan.logProcess.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.fudan.logProcess.entity.BlockchainLog;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.entity.Log;
import org.fudan.logProcess.error.BaseError;
import org.fudan.logProcess.service.FabricServiceInterface;
import org.fudan.logProcess.service.LogIndexDataBaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;


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
    public CommonResult<?> invoke(@RequestParam("key") String key, @RequestParam("value") String value) {
        log.info("receive a request that invokes key = {}, value = {}", key, value);
        // get the information that will be saved into blockchain
        BlockchainLog blockchainLog = new BlockchainLog(key, value);

        // Asynchronously call fabric to invoke
        Boolean flag = fabricServiceInterface.invoke(blockchainLog);


        // when callback method is completed, whether it is timeout or successful, it will enter this callback method
        if(flag) {
            return new CommonResult<>(BaseError.BLOCKCHAIN_INVOKE_SUCCESS, blockchainLog);
        } else {
            return new CommonResult<>(BaseError.BLOCKCHAIN_INVOKE_ERROR, blockchainLog);
        }
    }

    @GetMapping("/fabric/query")
    public CommonResult<?> query(@RequestParam("key") String key) {
        log.info("receive a request that queries key = {}", key);

        // get the information that queried from blockchain
        BlockchainLog blockchainLog = fabricServiceInterface.query(key);


        if (blockchainLog != null) {
            return new CommonResult<>(BaseError.BLOCKCHAIN_QUERY_SUCCESS, blockchainLog);
        } else {
            return new CommonResult<>(BaseError.BLOCKCHAIN_QUERY_ERROR);
        }

    }
}
