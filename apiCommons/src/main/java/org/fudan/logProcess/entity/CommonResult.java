package org.fudan.logProcess.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.*;
import org.fudan.logProcess.error.BaseError;

/**
 * @author Xu Rui
 * @date 2021/1/16 15:43
 * 通用返回类型
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
     * 无数据构造器
     * @param error 错误类型
     */
    public CommonResult(BaseError error){
        this(error.isError() ? -1 : 1, error.getCode(), error.getMsg(), error, null);
    }

    /**
     * 有数据构造器
     * @param error 错误类型
     * @param data  数据
     */
    public CommonResult(BaseError error, T data){
        this(error.isError() ? -1 : 1, error.getCode(), error.getMsg(), error, data);
    }

    /**
     * 设置错误
     * @param error 错误类型
     */
    public void setError(BaseError error){
        this.error = error;
        this.status = error.isError() ? -1 : 1;
        setCode(error.getCode());
        setMessage(error.getMsg());
    }

    /**
     * 设置错误和数据
     * @param error 错误类型
     * @param data  数据
     */
    public void set(BaseError error, T data){
        setError(error);
        setData(data);
    }

    /**
     * 追加错误信息
     * @param msg   添加的信息
     * @return      返回通用结果
     */
    public CommonResult<T> addMsg(String msg){
        this.error.setMsg(this.error.getMsg() + " " + msg);
        return this;
    }

    /**
     * 返回是否失败
     * @return  true为失败，false为成功
     */
    public boolean isError(){
        return error.isError();
    }

    /**
     * String 转换为 CommonResult
     * @param str   str
     * @return      CommonResult
     */
    public static CommonResult<?> parse(String str){
        return JSONObject.parseObject(str, CommonResult.class);
    }

    /**
     * 转换为String
     * @return  String
     */
    @Override
    public String toString(){
        return JSONObject.toJSONString(this);
    }
}