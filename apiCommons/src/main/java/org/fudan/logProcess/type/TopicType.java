package org.fudan.logProcess.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Xu Rui
 * @date 2021/1/25 11:43
 */
@Getter
@AllArgsConstructor
public enum TopicType {
    ALG_CONN_OBJ("AlgConnObj"),
    DATA_CONN_OBJ("DataConnObj"),
    TASK("Task");

    private final String type;
}
