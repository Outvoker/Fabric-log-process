package org.fudan.logProcess.service;

import org.fudan.logProcess.entity.BlockchainLog;
import org.fudan.logProcess.entity.CommonResult;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * @Program: Fabric-log-process
 * @Description: define the interface that interact with fabric
 * @Author: HouHao Ye
 * @Create: 2021-01-29 15:09
 **/
public interface FabricServiceInterface {
    Boolean invoke(BlockchainLog blockchainLog);
    BlockchainLog query(String key);
    String queryOne(String key);
}
