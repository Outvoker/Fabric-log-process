package org.fudan.logProcess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.fudan.logProcess.entity.Log;
import org.fudan.logProcess.mapper.LogMapper;
import org.fudan.logProcess.service.ILogService;
import org.springframework.stereotype.Service;

/**
 * @author Xu Rui
 * @date 2021/1/25 14:23
 */
@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements ILogService {

    @Override
    public Log getByOriginalKey(String key) {
        QueryWrapper<Log> condition = new QueryWrapper<>();
        condition.eq("original_key", key).last("limit 1");
        return getOne(condition);
    }

    @Override
    public boolean exist(String key){
        QueryWrapper<Log> condition = new QueryWrapper<>();
        condition.eq("original_key", key).last("limit 1");
        return count(condition) != 0;
    }
}
