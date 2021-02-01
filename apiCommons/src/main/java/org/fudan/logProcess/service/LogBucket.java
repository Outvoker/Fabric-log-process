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

    private JSONObject jsonObject;
    private ArrayList<ArrayList<String>> list;
    private int size;
    private int count;
    public final Object uploadLock = new Object();
    public boolean isUploaded = false;
    private String keyNum;

    private String collectionName;
    private String keyName;


    private ArrayList<String> filteredItem;



    public LogBucket(LogConfig logConfig, String keyNum){

        this.messages = new ArrayList<>();

        this.logConfig = logConfig;

        this.jsonObject = new JSONObject();

        this.list = new ArrayList<>();

        this.count = 0;
        this.size = 0;
        this.keyNum = keyNum;

        this.filteredItem = new ArrayList<>(logConfig.getHandler().getFilteredItem().size());

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
    }

    public boolean addMergedItem(Message msg , String[] logItem)  {
        synchronized (this.uploadLock) {
            //handle filteredItem
            try {
                changeFilteredItem(logItem);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            messages.add(msg);
            //handle list
            ArrayList<String> arr = new ArrayList<>();
            for(int idx : this.logConfig.getHandler().getMergedItemIndex()) {
                arr.add(logItem[idx]);
            }
            this.list.add(arr);
            this.count++;
            if(this.list.size() >= this.logConfig.getSender().getNum() && this.list.size() != 0) {   //the number of list is met num in the policy
                System.out.println("#################################################################");
                System.out.printf("bucket is full: %d\n", this.list.size());
                return true;
            }
            return false;
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
        this.jsonObject.put("count", this.list.size());
        this.jsonObject.put("list", this.list);

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

        this.collectionName = res.toString();

        return this.collectionName;
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

        this.keyName = keyPolicyId;

        return keyPolicyId;
    }

    public List<String> getBlockchainParams(){
        packageBucket();    //add item into jsonObject
        List<String> params = new ArrayList<>();
        params.add(keyPolicy());
        params.add(this.jsonObject.toJSONString());
        return params;
    }

    public List<String> getBlockchainPDCParams(){
        packageBucket();    //add item into jsonObject
        List<String> params = new ArrayList<>();
        params.add(collectionPolicy());
        params.add(keyPolicy());
        params.add(this.jsonObject.toJSONString());
        return params;
    }

    public Map<String, Object> getLogIndexDBParam(){
        MergedIndexItem item = new MergedIndexItem(this.keyName);
        for(int i = 0; i < list.size(); i++){
            item.add(list.get(i).get(0), i);
        }
        return item.getDBMap();
    }

    public List<Message> getMessages(){
        return this.messages;
    }

}