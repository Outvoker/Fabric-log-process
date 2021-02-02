package org.fudan.logProcess.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Xu Rui
 * @date 2021/1/25 15:52
 */
@Getter
@AllArgsConstructor
public enum BaseError {
    SUCCESS(20000, "Success"),

    //  Read
    READ_SUCCESS(20100, "Query success"),
    READ_ERROR(40100, "Query failed"),

    //  create
    CREATE_SUCCESS(20200, "Create success"),
    PUSH_LOG_SUCCESS(20201, "Push log success"),
    CREATE_ERROR(40200, "Create failed"),
    CREATE_ERROR_ALREADY_EXIST(40201, "Original key has already existed"),
    CREATE_ERROR_PART_ALREADY_EXIST(40201, "Part of original keys have already existed"),

    //  update
    UPDATE_SUCCESS(20300, "Update success"),
    UPDATE_ERROR(40300, "Update failed"),

    //  delete
    DELETE_SUCCESS(20400, "Delete success"),
    DELETE_ERROR(40400, "Delete failed"),

    //  param check
    PARAM_ERROR(40500, "Param error"),

    //  rocketMQ
    PRODUCE_SUCCESS(20600, "Produce success"),
    CONSUME_SUCCESS(20601, "Consume success"),
    PRODUCE_ERROR(40600, "Produce error"),
    PRODUCE_PARTLY_ERROR(40601, "Produce partly error"),
    PRODUCE_TIMEOUT_ERROR(40602, "Produce timeout error"),
    CONSUME_ERROR(40603, "consume error"),

    //  handle
    HANDLE_ERROR(40700, "handle error"),
    EXCEPTION_ERROR(40401, "exception error"),

    // fabric sdk
    BLOCKCHAIN_INVOKE_TIMEOUT_ERROR(40801, "invoke timeout"),
    BLOCKCHAIN_INVOKE_ERROR(40802, "invoke error"),
    BLOCKCHAIN_INVOKE_SUCCESS(20801, "invoke success"),
    BLOCKCHAIN_QUERY_TIMEOUT_ERROR(40803, "query timeout"),
    BLOCKCHAIN_QUERY_ERROR(40804, "query error"),
    BLOCKCHAIN_QUERY_SUCCESS(20802, "query success");

    private int code;
    private String msg;

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isError(){
        return code >= 40000;
    }
}
