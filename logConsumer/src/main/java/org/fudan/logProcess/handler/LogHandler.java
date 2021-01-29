package org.fudan.logProcess.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.fudan.logProcess.logConfig.LogConfig;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Slf4j
public class LogHandler{
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

    public void logProcess(Message msg)  {   //process the log

        String log = new String(msg.getBody(), StandardCharsets.UTF_8);

        String[] datas = log.split(this.logConfig.getInfo().getSeparator());
        HashSet<String> set = new HashSet<>();
        for(int idx : this.logConfig.getHandler().getMergedDependenceIndex()){
            set.add(datas[idx]);
        }


        if(!map.containsKey(set)){  //add a new merging item
            LogBucket bucket = new LogBucket(this.logConfig, set, map, Thread.currentThread().getName() + getKeyNum());
            map.put(set, bucket);
            bucket.addMergedItem(msg, datas);
        } else {    //if there is an item already
            synchronized (map.get(set).uploadLock) {
                map.get(set).addMergedItem(msg, datas);
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

    public void handle(List<MessageExt> messages) {
        System.out.println("receive data:" + messages.size());
        totalNum += messages.size();

        for(Message msg : messages) {   //process the log in lines
            logProcess(msg);
        }
        System.out.println(Thread.currentThread().getName() + "totalNum is : " + totalNum);

    }

}
