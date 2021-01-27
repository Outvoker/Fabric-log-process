package org.fudan.logProcess.logConfig;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Elegenthus on 2020/7/17.
 */
public class LogConfig {
    private  String name;
    private int version;

    private Info info;
    private Sender sender;
    private Handler handler;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public static class Info{
        private String separator;
        private String filePath;

        public String getSeparator() {
            return separator;
        }

        public void setSeparator(String separator) {
            this.separator = separator;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }

    public static class Sender{
        private int time;
        private int size;
        private int num;

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }
    }

    public static class Handler{
        private String mergedItemRule;
        private List<String> mergedDependence;
        private List<String> originalItem;
        private List<Map> filteredItem;
        private List<String> filteredItemField;
        private List<String> filteredItemName;
        private List<String> filteredItemType;
        private List<String> filteredItemRule;
        private List<String> mergedItem;
        private Map collectionName;
        private Map keyPolicy;

        private List<Integer> mergedDependenceIndex;
        private List<Integer> filteredItemIndex;
        private List<Integer> mergedItemIndex;

        private List<String> collectionNamePrefix;
        private List<String> collectionNameFields;
        private List<String> keyPolicyFields;
        private String keyPolicyId;


        public String getMergedItemRule(){ return mergedItemRule;}

        public void setMergedItemRule(String mergedItemRule){ this.mergedItemRule = mergedItemRule; }

        public List<Integer> getMergedDependenceIndex(){ return mergedDependenceIndex; }

        public void setMergedDependenceIndex(List<String> mergedDependence){
            this.mergedDependenceIndex = new ArrayList<>();
            for(int i = 0; i < mergedDependence.size(); i++){
                this.mergedDependenceIndex.add(originalItem.indexOf(mergedDependence.get(i)));
            }
        }

        public List<Integer> getFilteredItemIndex(){ return filteredItemIndex; }

        public void setFilteredItemIndex(List<Map> filteredItem){
            this.filteredItemIndex = new ArrayList<>();
            for(int i = 0; i < filteredItem.size(); i++){
                Map m = filteredItem.get(i);
                this.filteredItemIndex.add(originalItem.indexOf(m.get("item")));
            }
        }

        public List<Integer> getMergedItemIndex(){ return mergedItemIndex; }

        public void setMergedItemIndex(List<String> mergedItem){
            this.mergedItemIndex = new ArrayList<>();
            for(int i = 0; i < mergedItem.size(); i++){
                this.mergedItemIndex.add(originalItem.indexOf(mergedItem.get(i)));
            }
        }

        public List<String> getCollectionNamePrefix(){ return collectionNamePrefix; }

        public void setCollectionNamePrefix(Map collectionName ){
            this.collectionNamePrefix = (List<String>) collectionName.get("prefix");
        }

        public List<String> getCollectionNameFields() { return collectionNameFields; }

        public void setCollectionNameFields(Map collectionName){
            this.collectionNameFields = (List<String>) collectionName.get("fields");
        }

        public List<String> getKeyPolicyFields() { return keyPolicyFields; }

        public void setKeyPolicyFields(Map keyPolicy) {
            this.keyPolicyFields = (List<String>) keyPolicy.get("fields");

            System.out.println("fields: " + keyPolicy.get("fields"));
        }

        public String getKeyPolicyId() { return keyPolicyId; }

        public void setKeyPolicyId(Map keyPolicy){
            this.keyPolicyId = (String) keyPolicy.get("id");
        }

        public Map getCollectionName() { return collectionName; }

        public void setCollectionName(Map collectionName){ this.collectionName = collectionName; }

        public Map getKeyPolicy() { return keyPolicy; }

        public void setKeyPolicy(Map keyPolicy) { this.keyPolicy = keyPolicy; }

        public List<String> getMergedDependence() {
            return mergedDependence;
        }

        public void setMergedDependence(List<String> mergedDependence) {
            this.mergedDependence = mergedDependence;
        }

        public List<String> getOriginalItem() {
            return originalItem;
        }

        public void setOriginalItem(List<String> originalItem) {
            this.originalItem = originalItem;
        }

        public List<Map> getFilteredItem() { return filteredItem; }

        public void setFilteredItem(List<Map> filteredItem) {
            this.filteredItem = filteredItem;
        }

        public List<String> getFilteredItemField() { return filteredItemField; }

        public void setFilteredItemField(List<Map> filteredItem) {
            this.filteredItemField = new ArrayList<>();
            for (int i = 0; i <filteredItem.size(); i++) {
                this.filteredItemField.add((String) filteredItem.get(i).get("item"));
            }
        }

        public List<String> getFilteredItemName() { return filteredItemName; }

        public void setFilteredItemName(List<Map> filteredItem){
            this.filteredItemName = new ArrayList<>();
            for(int i = 0; i < filteredItem.size(); i++){
                this.filteredItemName.add((String) filteredItem.get(i).get("item"));
            }
        }

        public List<String> getFilteredItemType() { return filteredItemType; }

        public void setFilteredItemType(List<Map> filteredItem) {
            this.filteredItemType = new ArrayList<>();
            for(int i = 0; i < filteredItem.size(); i++){
                this.filteredItemType.add((String) filteredItem.get(i).get("type"));
            }
        }

        public List<String> getFilteredItemRule() { return filteredItemRule; }

        public void setFilteredItemRule(List<Map> filteredItem) {
            this.filteredItemRule = new ArrayList<>();
            for(int i = 0; i < filteredItem.size(); i++){
                this.filteredItemRule.add((String)filteredItem.get(i).get("rule"));
            }
        }

        public List<String> getMergedItem() {
            return mergedItem;
        }

        public void setMergedItem(List<String> mergedItem) {
            this.mergedItem = mergedItem;
        }



    }
    public LogConfig(String filePath) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        File f = new File(filePath);

        Map map = yaml.load(new FileInputStream(f));
        Info info = new Info();
        Sender sender = new Sender();
        Handler handler = new Handler();

        this.setName((String) map.get("name"));
        this.setVersion((Integer) map.get("version"));


        Map mapInfo = (Map)map.get("info");
        Map mapSender = (Map)map.get("sender");
        Map mapHandler = (Map)map.get("handler");

        info.setSeparator((String) mapInfo.get("separator"));
        info.setFilePath((String) mapInfo.get("filePath"));

        sender.setTime((Integer) mapSender.get("time"));
        sender.setSize((Integer) mapSender.get("size"));
        sender.setNum((Integer) mapSender.get("num"));

        handler.setMergedItemRule((String) mapHandler.get("mergedItemRule"));
        handler.setMergedDependence((List<String>) mapHandler.get("mergedDependence"));
        handler.setOriginalItem((List<String>) mapHandler.get("originalItem"));
        handler.setFilteredItem((List<Map>) mapHandler.get("filteredItem"));
        handler.setFilteredItemField((List<Map>) mapHandler.get("filteredItem"));
        handler.setFilteredItemName((List<Map>) mapHandler.get("filteredItem"));
        handler.setFilteredItemType((List<Map>) mapHandler.get("filteredItem"));
        handler.setFilteredItemRule((List<Map>) mapHandler.get("filteredItem"));
        handler.setMergedItem((List<String>) mapHandler.get("mergedItem"));
        handler.setCollectionName((Map) mapHandler.get("collectionName"));
        handler.setCollectionNamePrefix((Map) mapHandler.get("collectionName"));
        handler.setCollectionNameFields((Map) mapHandler.get("collectionName"));
        handler.setKeyPolicy((Map) mapHandler.get("keyPolicy"));
        handler.setKeyPolicyId((Map) mapHandler.get("keyPolicy"));
        handler.setKeyPolicyFields((Map) mapHandler.get("keyPolicy"));
        handler.setMergedDependenceIndex((List<String>) mapHandler.get("mergedDependence"));
        handler.setFilteredItemIndex((List<Map>) mapHandler.get("filteredItem"));
        handler.setMergedItemIndex((List<String>) mapHandler.get("mergedItem"));

        this.setInfo(info);
        this.setSender(sender);
        this.setHandler(handler);

    }

}
