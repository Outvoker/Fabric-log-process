package org.fudan.logProcess.service;

import org.fudan.logProcess.entity.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Xu Rui
 * @date 2021/1/27 16:05
 */
@FeignClient(value = "LOGINDEXDATABASE")
public interface LogIndexDataBase {

    @GetMapping("/log/get")
    CommonResult<?> getLogByOriginalKey(@RequestParam("key") String key);

    @PostMapping("/log/save")
    CommonResult<?> saveLog(@RequestParam("originalKey") String originalKey,
                            @RequestParam("integratedKey") String integratedKey,
                            @RequestParam("index") Integer index);

    @PostMapping("/log/saveBatch")
    CommonResult<?> saveBatch(@RequestBody Map<String, Object> map);

}
