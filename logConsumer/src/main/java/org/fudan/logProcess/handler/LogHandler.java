package org.fudan.logProcess.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.error.BaseError;
import org.fudan.logProcess.jms.LogReplyProducer;
import org.fudan.logProcess.logConfig.LogConfig;
import org.fudan.logProcess.service.LogIndexDataBaseService;
import org.fudan.logProcess.service.LogProcessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service("logHandler")
@Slf4j
public class LogHandler implements LogProcessService{

    private static final String policyPath = "D:\\university\\blockchain\\logProcess\\logConsumer\\src\\main\\resources\\test.yml";

    private static Integer times = 0;

    @Resource
    LogIndexDataBaseService logIndexDataBaseService;

    @Resource
    LogReplyProducer logReplyProducer;

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

    public final Object createBucketLock = new Object();

    private HashMap<HashSet<String>, LogBucket> map;

//    public static SdkDemo getS() {
//        return s;
//    }

    public LogConfig getLogConfig() {
        return logConfig;
    }

    /**
     * upload the bucket that match condition to upload
     * @param bucket    bucket
     * @param set       set
     */
    public void upload(LogBucket bucket, HashSet<String> set){
        synchronized (bucket.uploadLock) { //this lock avoids the situation that time is over and at the same time bucket is full
            if(bucket.isUploaded) return;
            bucket.isUploaded = true;

            //  write into blockchain
            log.info("getBlockchainParams = {}", bucket.getBlockchainParams());
            //  write into index DB
            log.info("getLogIndexDBParam = {}", bucket.getLogIndexDBParam());
            synchronized (times){
                times += ((Map)bucket.getLogIndexDBParam().get("originalKey")).size();
                log.info("upload times = {}", times);
            }
            CommonResult<?> result = logIndexDataBaseService.saveBatch(bucket.getLogIndexDBParam());
            //  reply the /log producer
            log.info("getMessages = {}", bucket.getMessages());
            try {
                logReplyProducer.reply(bucket.getMessages(), new CommonResult<>(BaseError.CONSUME_SUCCESS, result).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            //  destroy itself
            map.remove(set);
        }
    }


    public void  logProcess(Message msg)  {   //process the log

        String aLog = new String(msg.getBody(), StandardCharsets.UTF_8);

        String[] datas = aLog.split(this.logConfig.getInfo().getSeparator());
        HashSet<String> set = new HashSet<>();
        for(int idx : this.logConfig.getHandler().getMergedDependenceIndex()){
            set.add(datas[idx]);
        }

        synchronized (createBucketLock){
            if(!map.containsKey(set)){  //add a new merging item
                log.info("create new bucket");
                LogBucket bucket = new LogBucket(this.logConfig, Thread.currentThread().getName() + getKeyNum());
                map.put(set, bucket);

                //  set time to upload
                if(logConfig.getSender().getTime() != 0 ){
                    Timer uploadTimer = new Timer("timer" + set.toString());
                    uploadTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            log.info("######### time is over!");
                            upload(bucket, set);
                        }
                    }, logConfig.getSender().getTime());
                }
            }
        }

        LogBucket bucket = map.get(set);

        synchronized (bucket.uploadLock){
            if(bucket.isUploaded) {
                logProcess(msg);
                return;
            }
            log.info("write into bucket = {}", set);
            boolean flag = bucket.addMergedItem(msg, datas);    //  if bucket is full;
            if(flag) {
                log.info("######### bucket is full!");
                upload(bucket, set);
                log.info("map size = {}", map.size());
            }
        }

    }

    public LogHandler(LogIndexDataBaseService logIndexDataBaseService, LogReplyProducer logReplyProducer) throws FileNotFoundException {
        this.logIndexDataBaseService = logIndexDataBaseService;
        this.logReplyProducer = logReplyProducer;

        this.logConfig = new LogConfig(policyPath); //load policy

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
    public Boolean handle(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
        System.out.println("receive data:" + messages.size());
        totalNum += messages.size();

        for(Message msg : messages) {   //process the log in lines
            logProcess(msg);
        }
        System.out.println(Thread.currentThread().getName() + "totalNum is : " + totalNum);
        return true;
    }

}
