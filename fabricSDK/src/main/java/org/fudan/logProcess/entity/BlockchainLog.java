package org.fudan.logProcess.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Program: Fabric-log-process
 * @Description: the information that saved information into fabric network
 * @Author: HouHao Ye
 * @Create: 2021-01-29 15:09
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockchainLog {
    private String key;
    private String value;
}
