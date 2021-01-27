package org.fudan.logProcess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Xu Rui
 * @date 2021/1/25 14:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Log {
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;
    private String originalKey;
    private String integratedKey;
    private Integer idx;
}
