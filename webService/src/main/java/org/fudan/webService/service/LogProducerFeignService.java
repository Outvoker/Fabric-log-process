package org.fudan.webService.service;

import org.fudan.logProcess.entity.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.async.DeferredResult;

@Component
@FeignClient(value = "LOG-PRODUCER-SERVICE")
public interface LogProducerFeignService {

    @PostMapping("/log/push")
    CommonResult<?> pushLog(@RequestBody String aLog);

    @PostMapping("/log/pushAsync")
    CommonResult<?> pushLogAsync(@RequestBody String aLog);

}
