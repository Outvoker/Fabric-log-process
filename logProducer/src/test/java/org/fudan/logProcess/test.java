package org.fudan.logProcess;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.junit.Test;

/**
 * @author Xu Rui
 * @date 2021/1/27 14:49
 */
@Slf4j
public class test {
    @Test
    public void testParseCommonResult(){
        CommonResult<?> result = new CommonResult<>(BaseError.CONSUME_ERROR, "error");
        String str = JSONObject.toJSONString(result);
        log.info(str);

        CommonResult<?> parse = CommonResult.parse(str);
        log.info(JSONObject.toJSONString(parse));
    }

    @Test
    public void testParseEnum(){
        String str = JSONObject.toJSONString(BaseError.CONSUME_ERROR);

        BaseError e = JSONObject.parseObject(str, BaseError.class);

        log.info(JSONObject.toJSONString(e));
    }
}
