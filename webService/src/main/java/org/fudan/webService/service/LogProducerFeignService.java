package org.fudan.webService.service;

import org.fudan.logProcess.entity.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.async.DeferredResult;

@Component
@FeignClient(value = "LOGPRODUCER")
public interface LogProducerFeignService {
    @PostMapping("/log/push")
    DeferredResult<CommonResult<?>> pushLog(@RequestBody String aLog);
}
