package org.fudan.logProcess.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Xu Rui
 * @date 2021/2/1 14:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Log {
    private Long id;
    private String originalKey;
    private String integratedKey;
    private Integer idx;
}
