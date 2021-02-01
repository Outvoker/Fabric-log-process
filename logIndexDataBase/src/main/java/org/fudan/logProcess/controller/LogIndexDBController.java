package org.fudan.logProcess.controller;

import lombok.extern.slf4j.Slf4j;
import org.fudan.logProcess.entity.Log;
import org.fudan.logProcess.service.ILogService;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Xu Rui
 * @date 2021/1/25 14:19
 */
@RestController
@Slf4j
public class LogIndexDBController {

    @Resource
    ILogService logService;

    @GetMapping("/log/get")
    public CommonResult<?> getLogByOriginalKey(@RequestParam("key") String key){
        log.info("getLogByOriginalKey: key={}", key);

        return new CommonResult<>(BaseError.READ_SUCCESS, logService.getByOriginalKey(key));
    }

    @PostMapping("/log/save")
    public CommonResult<?> saveLog(@RequestParam("originalKey") String originalKey,
                                   @RequestParam("integratedKey") String integratedKey,
                                   @RequestParam("index") Integer index){
        log.info("saveLog: integratedKey={}, originalKey={}, index={}", integratedKey, originalKey, index);

        if(logService.exist(originalKey)) return new CommonResult<>(BaseError.CREATE_ERROR_ALREADY_EXIST);

        Log logObject = new Log();
        logObject.setOriginalKey(originalKey);
        logObject.setIntegratedKey(integratedKey);
        logObject.setIdx(index);

        if(!logService.save(logObject)) return new CommonResult<>(BaseError.CREATE_ERROR, logObject);

        return new CommonResult<>(BaseError.CREATE_SUCCESS, logObject);
    }

    @PostMapping("/log/saveBatch")
    public CommonResult<?> saveBatch(@RequestBody Map<String, Object> map){
        log.info("saveBatch: map={}", map);

        //  param check
        if(!map.containsKey("integratedKey") || !map.containsKey("originalKeyIndex"))
            return new CommonResult<>(BaseError.PARAM_ERROR);

        Object object = map.get("originalKeyIndex");

        if(!(object instanceof Map<?, ?>))
            return new CommonResult<>(BaseError.PARAM_ERROR);

        String integratedKey = String.valueOf(map.get("integratedKey"));

        Map<?, ?> originalKeyIndex= (Map<?, ?>) object;

        Collection<Log> logBatch = new ArrayList<>();

        //  record the failed logs
        List<Log> failedLogs = new ArrayList<>();

        for(Map.Entry<?, ?> entry: originalKeyIndex.entrySet()){
            String key = String.valueOf(entry.getKey());
            int value;
            try {
                value = Integer.parseInt(String.valueOf(entry.getValue()));
            }catch (NumberFormatException e){
                return new CommonResult<>(BaseError.PARAM_ERROR);
            }

            Log logObject = new Log();
            logObject.setOriginalKey(key);
            logObject.setIntegratedKey(integratedKey);
            logObject.setIdx(value);
//            if(logService.exist(key)) failedLogs.add(logObject);
//            else logBatch.add(logObject);
            logBatch.add(logObject);
        }

        if(!logService.saveBatch(logBatch)) return new CommonResult<>(BaseError.CREATE_ERROR, logBatch);

        if(!failedLogs.isEmpty()) return new CommonResult<>(BaseError.CREATE_ERROR_PART_ALREADY_EXIST, failedLogs);

        return new CommonResult<>(BaseError.CREATE_SUCCESS, logBatch);
    }
}
