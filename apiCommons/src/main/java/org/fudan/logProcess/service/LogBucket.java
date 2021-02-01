package org.fudan.logProcess.service;

import com.alibaba.fastjson.JSONObject;
import org.fudan.logProcess.logConfig.LogConfig;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Xu Rui
 * @date 2021/1/25 12:00
 */
public class LogBucket {
    private LogConfig logConfig;    //policy

    private HashSet<String> set;
    private HashMap<HashSet<String>, LogBucket> map;

    private JSONObject jsonObject;
    private ArrayList<ArrayList<String>> list;
    private int size;
    private int count;
    public final Object uploadLock = new Object();
    private boolean isUploaded = false;
    private Timer uploadTimer;
    private String keyNum;


    private ArrayList<String> filteredItem;


    LogIndexDataBaseService logIndexDataBaseService;

    LogReplyProducer logReplyProducer;

    public LogBucket(LogConfig logConfig, HashSet<String> set, HashMap<HashSet<String>, LogBucket> map, String keyNum, LogIndexDataBaseService logIndexDataBaseService, LogReplyProducer logReplyProducer){

        this.logIndexDataBaseService = logIndexDataBaseService;
        this.logReplyProducer = logReplyProducer;

        this.messages = new ArrayList<>();

        this.logConfig = logConfig;
        this.set = set;
        this.map = map;

        this.jsonObject = new JSONObject();
        if(logConfig.getHandler().getMergedItemRule().compareTo("single") == 0) {
            this.list = new ArrayList<>();
        } else if (logConfig.getHandler().getMergedItemRule().compareTo("multi") == 0) {
            this.list = new ArrayList<>();
            for(int i = 0; i < this.logConfig.getHandler().getMergedItemIndex().size(); i++) {
                this.list.add(new ArrayList<>());
            }
        }
        this.count = 0;
        this.size = 0;
        this.keyNum = keyNum;

        this.filteredItem = new ArrayList<>(logConfig.getHandler().getFilteredItem().size());

        if (logConfig.getSender().getTime() != 0 ){
            this.uploadTimer = new Timer("timer" + this.set.toString() );   //a timer that control the bucket uploading when time is over
            this.uploadTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("time is over!");
                    synchronized (uploadLock) { //this lock avoids the situation that time is over and at the same time bucket is full
                        upload();
                    }
                }
            }, logConfig.getSender().getTime());
        }
    }

    public String filteredItemHandler(String originalItem, String item, String type, String rule) throws Exception{
        //process the filtered item by the policy read by the log config
        switch (rule) {
            case "first":
                return originalItem;
            case "last":
                return item;
            case "sum":
                switch (type) {
                    case "Int":
                        return Integer.toString((Integer.parseInt(originalItem) + Integer.parseInt(item)));
                    case "Double":
                        return Double.toString((Double.parseDouble(originalItem) + Double.parseDouble(item)));
                    case "Float":
                        return Float.toString((Float.parseFloat(originalItem) + Float.parseFloat(item)));
                    default: //type.equals("Time")   long
                        return Long.toString((Long.parseLong(originalItem) * this.count + Long.parseLong(item)) / (this.count + 1));
                }
            case "average":
//                this.count
                //code
                //return (originalItem*this.count+item)/(++this.count)
                switch (type) {
                    case "Int":
                        return Integer.toString((Integer.parseInt(originalItem) * this.count + Integer.parseInt(item)) / (this.count + 1));
                    case "Double":
                        return Double.toString((Double.parseDouble(originalItem) * this.count + Double.parseDouble(item)) / (this.count + 1));
                    case "Float":
                        return Float.toString((Float.parseFloat(originalItem) * this.count + Float.parseFloat(item)) / (this.count + 1));
                    case "Date":  //(d1-d2)*this.count/(++this.count)+d2 与 (originalItem*this.count+item)/(++this.count)一样
                        long d1 = new SimpleDateFormat("yyyyMMdd").parse(originalItem).getTime();
                        long d2 = new SimpleDateFormat("yyyyMMdd").parse(item).getTime();
                        return new SimpleDateFormat("yyyyMMdd").format(new Date((d1 - d2) * this.count / (this.count + 1) + d2));
                    default: //type.equals("Time")   long
                        return Long.toString((Long.parseLong(originalItem) * this.count + Long.parseLong(item)) / (this.count + 1));
                }
                //break;
            case "max":
                //code
                if(type.equals("String") || type.equals("Date")) {
                    return originalItem.compareTo(item) > 0 ? originalItem : item;
                }
                else{
                    return Double.parseDouble(originalItem) > Double.parseDouble(item) ? originalItem : item;
                }
                //break;
            case "min":
                //code
                if(type.equals("String") || type.equals("Date")) {
                    return originalItem.compareTo(item) < 0 ? originalItem : item;
                }
                else{
                    return Double.parseDouble(originalItem) < Double.parseDouble(item) ? originalItem : item;
                }
                //break;
        }
        return "default";
    }

    public void changeFilteredItem(String[] logItem) throws Exception {
        List<String> type = logConfig.getHandler().getFilteredItemType();
        List<String> rule = logConfig.getHandler().getFilteredItemRule();
        List<Integer> filteredItemIndex = this.logConfig.getHandler().getFilteredItemIndex();

        for(int i = 0; i < filteredItemIndex.size(); i++) {
            if(this.filteredItem.size() < filteredItemIndex.size()) {   //fill directly first
                this.filteredItem.add(i, logItem[filteredItemIndex.get(i)]);
                continue;
            }
            //Follow up filling according to policy
            String item = filteredItemHandler(this.filteredItem.get(i),logItem[filteredItemIndex.get(i)], type.get(i), rule.get(i));
            this.filteredItem.set(i, item);
        }
        if(this.size == 0) {
            int temp = 0;
            for(int idx : logConfig.getHandler().getFilteredItemIndex()) {
                temp += logConfig.getHandler().getOriginalItem().get(idx).getBytes(StandardCharsets.UTF_8).length;
                temp += logItem[idx].getBytes(StandardCharsets.UTF_8).length;
            }
            for(int idx : logConfig.getHandler().getMergedItemIndex()) {
                temp += logConfig.getHandler().getOriginalItem().get(idx).getBytes(StandardCharsets.UTF_8).length;
            }
            temp += (filteredItem.size() + this.logConfig.getHandler().getMergedItem().size() * 2) * 6 + 20;
            this.size += temp;
        }
    }

    public void addMergedItem(Message msg , String[] logItem)  {
        //handle filteredItem
        try {
            changeFilteredItem(logItem);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                logReplyProducer.reply(msg, new CommonResult<>(BaseError.CONSUME_ERROR, e.getMessage()).toString());
            } catch (Exception ee){
                ee.printStackTrace();
            }
            return;
        }

        messages.add(msg);
        //handle list
        if(logConfig.getHandler().getMergedItemRule().compareTo("single") == 0) {
            ArrayList<String> arr = new ArrayList<>();
            for(int idx : this.logConfig.getHandler().getMergedItemIndex()) {
                arr.add(logItem[idx]);
                this.size += logItem[idx].getBytes(StandardCharsets.UTF_8).length + 3;
            }
            this.list.add(arr);
            this.count++;
            if(this.list.size() >= this.logConfig.getSender().getNum() && this.list.size() != 0) {   //the number of list is met num in the policy
                System.out.println("#################################################################");
                System.out.printf("bucket is full: %d\n", this.list.size());
                synchronized (uploadLock) {
                    upload();
                }
            }
            if(logConfig.getSender().getSize() * 1000 <= this.size && logConfig.getSender().getSize() != 0) {   //the size of list is met size in the policy
                System.out.println("#################################################################");
                System.out.printf("bucket Bytes is full: %d\n", this.size);
                synchronized (uploadLock) {
                    upload();
                }
            }
        } else if (logConfig.getHandler().getMergedItemRule().compareTo("multi") == 0) {
            List<Integer> mergedItemIndex = this.logConfig.getHandler().getMergedItemIndex();
            for(int i = 0; i < mergedItemIndex.size(); i++) {
                this.list.get(i).add(logItem[mergedItemIndex.get(i)]);
                this.count++;
                this.size += logItem[mergedItemIndex.get(i)].getBytes(StandardCharsets.UTF_8).length + 3;
            }
            if(this.list.get(0).size() >= this.logConfig.getSender().getNum() && this.list.size() != 0) {
                System.out.println("#################################################################");
                System.out.printf("bucket is full: %d\n", this.list.get(0).size());
                synchronized (uploadLock) {
                    upload();
                }
            }
            if(logConfig.getSender().getSize() * 1000 <= this.size && logConfig.getSender().getSize() != 0) {
                System.out.println("#################################################################");
                System.out.printf("bucket Bytes is full: %d\n", this.size);
                synchronized (uploadLock) {
                    upload();
                }
            }
        }

    }

    public void packageBucket() {
        //fill the filtered item into jsonObject
        List<String> originalItem = this.logConfig.getHandler().getOriginalItem();
        List<Integer> filteredItemIndex = this.logConfig.getHandler().getFilteredItemIndex();
        List<String> filteredItemField = this.logConfig.getHandler().getFilteredItemField();
        for(int i = 0; i < filteredItemIndex.size(); i++){
            this.jsonObject.put(filteredItemField.get(i), this.filteredItem.get(i)); //Add the required data fields and corresponding log data to jsonobject
        }

        //add list into jsonObject
        if(logConfig.getHandler().getMergedItemRule().compareTo("single") == 0) {
            this.jsonObject.put("count", this.list.size());
            this.jsonObject.put("list", this.list);
        }else if (logConfig.getHandler().getMergedItemRule().compareTo("multi") == 0) {
            List<Integer> mergedItemIndex = this.logConfig.getHandler().getMergedItemIndex();
            for(int i = 0; i < mergedItemIndex.size(); i++) {
                this.jsonObject.put("count" + originalItem.get(mergedItemIndex.get(i)), this.list.get(i).size());
                this.jsonObject.put(originalItem.get(mergedItemIndex.get(i)), this.list.get(i));
            }
        }

    }

    public String collectionPolicy() {
        //collection name policy
        List<String> collectionNamePrefix = this.logConfig.getHandler().getCollectionNamePrefix();
        List<String> collectionNameFields = this.logConfig.getHandler().getCollectionNameFields();
        ArrayList<String> collectionConcat = new ArrayList<>();
        for (String collectionNameField : collectionNameFields) {
            collectionConcat.add(this.jsonObject.get(collectionNameField).toString());
        }
        collectionConcat.sort(Comparator.naturalOrder());

        StringBuilder res = new StringBuilder();

        for(String str : collectionNamePrefix)
            res.append(str);

        for(String str : collectionConcat)
            res.append(str);

        return res.toString();
    }

    public String keyPolicy() {
        //key name policy
        String keyPolicyId = this.logConfig.getHandler().getKeyPolicyId();
        List<String> keyPolicyFields = this.logConfig.getHandler().getKeyPolicyFields();

        for(String str : keyPolicyFields) {
            keyPolicyId = keyPolicyId.concat(this.jsonObject.get(str).toString());
        }

        SimpleDateFormat formatter= new SimpleDateFormat("yyyyMMddHHmmss");     //time
        Date date = new Date(System.currentTimeMillis());
        keyPolicyId = keyPolicyId.concat(formatter.format(date));

        keyPolicyId = keyPolicyId.concat(this.keyNum);      //threadLocal num

        return keyPolicyId;
    }

    private void clear() {
        //clear the timer when the bucket is uploaded
        if(this.logConfig.getSender().getTime() != 0) this.uploadTimer.cancel();
    }
    public void upload(){
        try {
            if(isUploaded) return;
            else isUploaded = true;
            packageBucket();    //add item into jsonObject

            String[] para = new String[3];
            para[0] = collectionPolicy();  //collection
            para[1] = keyPolicy(); // key = keyPolicy + system time + treadLocal num
            para[2] = this.jsonObject.toJSONString();    //invoke json

//            LogHandler.getS().invoke("myChannel", "putPrivateData", para); //invoke
            System.out.println("invoke:");
            System.out.println("collection: " + para[0]);
            System.out.println("key: " + para[1]);
            System.out.println("value: " + para[2]);

            MergedIndexItem item = new MergedIndexItem(para[1]);

            for(int i = 0; i < list.size(); i++){
                item.add(list.get(i).get(0), i);
            }

            log.info(item.getDBMap().toString());

            logIndexDataBaseService.saveLog("1111", "2222", 3);

            logIndexDataBaseService.saveBatch(item.getDBMap());

            logReplyProducer.reply(messages, new CommonResult<>(BaseError.CONSUME_SUCCESS, messages).toString());

            System.out.println("#################################################################");
            this.map.remove(this.set);    //remove itself after upload
            this.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}