package org.fudan.logProcess;

import org.fudan.logProcess.entity.BlockchainLog;
import org.fudan.logProcess.service.FabricServiceInterface;
import org.hyperledger.fabric.gateway.Transaction;
import org.junit.Test;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;

public class test {
    @Resource(description = "fabricService")
    FabricServiceInterface fabricServiceInterface;
    @Test
    public void test(){
        System.out.println(fabricServiceInterface.invoke(new BlockchainLog("1", "100")));
    }
}
