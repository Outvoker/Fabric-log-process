package org.fudan.logProcess.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.fudan.logProcess.entity.Log;

/**
 * @author Xu Rui
 * @date 2021/1/25 14:22
 */
@Mapper
public interface LogMapper extends BaseMapper<Log> {
}
