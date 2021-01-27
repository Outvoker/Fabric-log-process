package org.fudan.logProcess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.fudan.logProcess.entity.Log;

/**
 * @author Xu Rui
 * @date 2021/1/25 14:23
 */
public interface ILogService extends IService<Log> {
    Log getByOriginalKey(String key);
    boolean exist(String key);
}
