package org.fudan.logProcess.service;

import java.util.List;

/**
 * @author Xu Rui
 * @date 2021/1/25 11:58
 */
public interface NewLineHandler {
    void handle(List<String> lines) throws Exception;
    void handle(String line) throws Exception;
}
