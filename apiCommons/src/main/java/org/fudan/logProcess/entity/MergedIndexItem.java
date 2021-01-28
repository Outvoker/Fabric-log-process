package org.fudan.logProcess.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Xu Rui
 * @date 2021/1/28 13:29
 */
@Data
public class MergedIndexItem {
    private static final String key1 = "integratedKey";
    private static final String key2 = "originalKeyIndex";

    private static Map<String, Integer> originalKeyIndexMap = new HashMap<>();
    private static Map<String, Object> DBParamMap = new HashMap<String, Object>(){{
        put(key2, originalKeyIndexMap);
    }};

    public MergedIndexItem(String integratedKey){
        DBParamMap.put(key1, integratedKey);
    }

    public void add(String originalKey, Integer index){
        originalKeyIndexMap.put(originalKey, index);
    }

    public Map<String, Object> getDBMap(){
        return DBParamMap;
    }
}
