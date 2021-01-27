package org.fudan.logProcess.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;

/**
 * @author Xu Rui
 * @date 2021/1/27 16:48
 */
@Service
@Slf4j
@Getter
public class LogProcessService {

    private static final String POLICY_FILE = "D:\\university\\blockchain\\logProcess\\logConsumer\\src\\main\\resources\\test.yml";

    private LogHandler logHandler;

    LogProcessService() throws FileNotFoundException {
        logHandler = new LogHandler(POLICY_FILE);
    }
}
