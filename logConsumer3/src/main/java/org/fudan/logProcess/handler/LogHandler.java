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
import org.fudan.logProcess.service.FabricSDKService;
import org.fudan.logProcess.service.LogIndexDataBaseService;
import org.fudan.logProcess.service.LogProcessService;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service("logHandler")
@Slf4j
public class LogHandler implements LogProcessService {

    private static final String policyPath = "D:\\university\\blockchain\\logProcess\\logConsumer\\src\\main\\resources\\test.yml";

    private static Integer times = 0;

    private static AtomicInteger invokeTimes = new AtomicInteger();
//    private static AtomicInteger timeOverUpload = new AtomicInteger();
    private static AtomicInteger bucketFullUpload = new AtomicInteger();
    private static volatile int total = 0;

    @Resource
    LogIndexDataBaseService logIndexDataBaseService;

    @Resource
    LogReplyProducer logReplyProducer;

    @Resource
    FabricSDKService fabricSDKService;

    //    private static SdkDemo s;
    private static AtomicInteger totalNum = new AtomicInteger();

    private LogConfig logConfig;

    private ThreadLocal<Integer> keyNum = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            return 999;
        }
    };  //a variable protected with lock

    public String getKeyNum() { //get the key number which is increasing order
        this.keyNum.set(keyNum.get() + 1);
        return String.valueOf(keyNum.get());
    }

    public final Object createBucketLock = new Object();

    private HashMap<String, LogBucket> map;


    public LogConfig getLogConfig() {
        return logConfig;
    }

    /**
     * upload the bucket that match condition to upload
     *
     * @param bucket bucket
     * @param key    key
     */
    public boolean upload(LogBucket bucket, String key) {
        synchronized (bucket.uploadLock) { //this lock avoids the situation that time is over and at the same time bucket is full
            if (bucket.isUploaded) return false;
            bucket.isUploaded = true;

            CommonResult<?> fabricResult;
            CommonResult<?> indexDBResult = null;

            //  write into blockchain
            List<String> params = bucket.getBlockchainParams();
//            log.info("getBlockchainParams = {}", params);
            log.info("invoke times: {}", invokeTimes.incrementAndGet());
            fabricResult = fabricSDKService.invoke(params.get(0), params.get(1));
            fabricResult = new CommonResult<>(BaseError.BLOCKCHAIN_INVOKE_SUCCESS,"ok");
            log.info("invoke result = {}", fabricResult);
            if (fabricResult == null || fabricResult.isError()) {
                log.info("invoke into blockchain failed {}", fabricResult);
            } else {
                //  write into index DB
                Map<String, Object> logIndexDBParam = bucket.getLogIndexDBParam();
                log.info("total invoke: {}, time: {}", totalNum.addAndGet(((Map) logIndexDBParam.get("originalKeyIndex")).size()), System.currentTimeMillis());
//                log.info("getLogIndexDBParam = {}", logIndexDBParam);
//                synchronized (times) {
//                    times += ((Map) logIndexDBParam.get("originalKeyIndex")).size();
//                    log.info("upload times = {}", times);
//                }
//                indexDBResult = logIndexDataBaseService.saveBatch(logIndexDBParam);
//                logIndexDataBaseService.saveBatch(logIndexDBParam);
            }

            //  reply the /log producer
//            log.info("reply all the messages in the bucket = {}", bucket.getMessages().size());
//            try {
//                CommonResult<?> result;
//                if (fabricResult == null || fabricResult.isError())
//                    result = new CommonResult<>(BaseError.CONSUME_ERROR, fabricResult);
//                else if (indexDBResult == null || indexDBResult.isError())
//                    result = new CommonResult<>(BaseError.CONSUME_ERROR, indexDBResult);
//                else result = new CommonResult<>(BaseError.CONSUME_SUCCESS, new Object[]{fabricResult, indexDBResult});
//                logReplyProducer.reply(bucket.getMessages(), result.toString());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            //  destroy itself
            map.remove(key);
            return true;
        }
    }

    /**
     * 日志处理函数
     * @param msg   一条日志到来
     */
    public void logProcess(Message msg) {   //process the log

        String aLog = new String(msg.getBody(), StandardCharsets.UTF_8);

        String[] datas = aLog.split(this.logConfig.getInfo().getSeparator());
        String key = datas[6];
//        HashSet<String> set = new HashSet<>();
//        for (int idx : this.logConfig.getHandler().getMergedDependenceIndex()) {
//            set.add(datas[idx]);
//        }


        if (!map.containsKey(key)) {  //add a new merging item
            synchronized (createBucketLock) {
                if(!map.containsKey(key)){
//                    log.info("create new bucket");
                    LogBucket bucket = new LogBucket(this.logConfig, Thread.currentThread().getName() + getKeyNum());
                    map.put(key, bucket);

                    //  set time to upload
                    if (logConfig.getSender().getTime() != 0) {
                        Timer uploadTimer = new Timer("timer" + key);
                        uploadTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                boolean upload = upload(bucket, key);
//                                if(upload) log.info("time over upload: {}", timeOverUpload.incrementAndGet());
                            }
                        }, logConfig.getSender().getTime());
                    }
                }
            }
        }

        LogBucket bucket = map.get(key);

        synchronized (bucket.uploadLock) {
            if (bucket.isUploaded) {
                logProcess(msg);
                return;
            }
//            log.info("write into bucket = {}", set);
            boolean flag = bucket.addMergedItem(msg, datas);    //  if bucket is full;
            if (flag) {
//                log.info("######### bucket is full!");
                boolean upload = upload(bucket, key);
                if(upload) log.info("bucket full upload: {}", bucketFullUpload.incrementAndGet());
//                log.info("map size = {}", map.size());
            }
        }

    }

    public LogHandler(LogIndexDataBaseService logIndexDataBaseService, LogReplyProducer logReplyProducer) throws FileNotFoundException {
        this.logIndexDataBaseService = logIndexDataBaseService;
        this.logReplyProducer = logReplyProducer;

        this.logConfig = new LogConfig(policyPath); //load policy

        map = new HashMap<>();
    }

    @Override
    public Boolean handle(List<MessageExt> messages, ConsumeConcurrentlyContext context) {

        for (Message msg : messages) {   //process the log in lines
            logProcess(msg);
        }
//        log.info("{} totalNum is : {}", Thread.currentThread().getName(), totalNum.addAndGet(messages.size()));
//        total += messages.size();
//        log.info("{} totalNum is : {}", Thread.currentThread().getName(), total);
        return true;
    }

}
