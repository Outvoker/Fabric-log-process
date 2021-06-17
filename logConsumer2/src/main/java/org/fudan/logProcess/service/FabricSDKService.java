package org.fudan.logProcess.service;

import org.fudan.logProcess.entity.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * @author Xu Rui
 * @date 2021/2/2 13:09
 */
@FeignClient(value = "FABRIC-SDK-SERVICE")
public interface FabricSDKService {
    @PostMapping("/fabric/invoke")
    CommonResult<?> invoke(@RequestParam("key") String key, @RequestParam("value") String value);
}
