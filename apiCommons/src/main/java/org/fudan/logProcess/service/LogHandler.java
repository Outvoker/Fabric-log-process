package org.fudan.logProcess.service;

import org.fudan.logProcess.logConfig.LogConfig;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author Xu Rui
 * @date 2021/1/25 11:59
 */
public class LogHandler implements NewLineHandler{
//    private static SdkDemo s;
    private static int totalNum = 0;

    private LogConfig logConfig;

    private ThreadLocal<Integer> keyNum = new ThreadLocal(){
        @Override
        protected Object initialValue() {
            return 999;
        }
    };  //a variable protected with lock

    public String getKeyNum() { //get the key number which is increasing order
        this.keyNum.set(keyNum.get()+1);
        return String.valueOf(keyNum.get());
    }

    private HashMap<HashSet<String>, LogBucket> map;

//    public static SdkDemo getS() {
//        return s;
//    }

    public LogConfig getLogConfig() {
        return logConfig;
    }

    public void logProcess(String log) throws Exception {   //process the log
        String[] datas = log.split(this.logConfig.getInfo().getSeparator());
        HashSet<String> set = new HashSet<>();
        for(int idx : this.logConfig.getHandler().getMergedDependenceIndex()){
            set.add(datas[idx]);
        }


        if(!map.containsKey(set)){  //add a new merging item
            LogBucket bucket = new LogBucket(this.logConfig, set, map, Thread.currentThread().getName() + getKeyNum());
            map.put(set, bucket);
            bucket.addMergedItem(datas);
        } else {    //if there is an item already
            synchronized (map.get(set).uploadLock) {
                map.get(set).addMergedItem(datas);
            }
        }
    }

    public LogHandler(String path) throws FileNotFoundException {
        this.logConfig = new LogConfig(path); //load policy

//        s = new SdkDemo();  //new fabric java sdk demo
//        try {
//            s.checkConfig();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        s.setup();
        System.out.println("Init sdkDemo success!");

        map = new HashMap<>();
    }

    @Override
    public void handle(List<String> lines) throws Exception {
        System.out.println("receive data:" + lines.size());
        totalNum += lines.size();

        for(String log : lines) {   //process the log in lines
            logProcess(log);
        }
        System.out.println(Thread.currentThread().getName() + "totalNum is : " + totalNum);

    }

    @Override
    public void handle(String line) throws Exception {
        logProcess(line);
    }
}
