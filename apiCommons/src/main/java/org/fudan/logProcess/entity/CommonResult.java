package org.fudan.logProcess.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.*;
import org.fudan.logProcess.error.BaseError;

/**
 * @author Xu Rui
 * @date 2021/1/16 15:43
 * 通Common return type
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult<T> {
    private int status;
    private int code;
    private String message;
    private BaseError error;
    private T       data;

    /**
     * data constructors
     * @param error error type
     */
    public CommonResult(BaseError error){
        this(error.isError() ? -1 : 1, error.getCode(), error.getMsg(), error, null);
    }

    /**
     * data constructors
     * @param error error type
     * @param data  data
     */
    public CommonResult(BaseError error, T data){
        this(error.isError() ? -1 : 1, error.getCode(), error.getMsg(), error, data);
    }

    /**
     * Setting error
     * @param error error type
     */
    public void setError(BaseError error){
        this.error = error;
        this.status = error.isError() ? -1 : 1;
        setCode(error.getCode());
        setMessage(error.getMsg());
    }

    /**
     * Setup errors and data
     * @param error errors
     * @param data  data
     */
    public void set(BaseError error, T data){
        setError(error);
        setData(data);
    }

    /**
     * Appending error information
     * @param msg   error message
     * @return      CommonResult<T>
     */
    public CommonResult<T> addMsg(String msg){
        this.error.setMsg(this.error.getMsg() + " " + msg);
        return this;
    }

    /**
     * Return whether to fail or not
     * @return  true is fail，false is success
     */
    public boolean isError(){
        return error.isError();
    }

    /**
     * String transfer to CommonResult
     * @param str   str
     * @return      CommonResult
     */
    public static CommonResult<?> parse(String str){
        return JSONObject.parseObject(str, CommonResult.class);
    }

    /**
     * transfer to String
     * @return  String
     */
    @Override
    public String toString(){
        return JSONObject.toJSONString(this);
    }
}