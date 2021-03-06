package org.fudan.logProcess.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.fudan.logProcess.entity.CommonResult;
import org.fudan.logProcess.entity.Log;
import org.fudan.logProcess.error.BaseError;
import org.fudan.logProcess.service.FabricServiceInterface;
import org.fudan.logProcess.entity.BlockchainLog;
import org.fudan.logProcess.service.LogIndexDataBaseService;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Program: Fabric-log-process
 * @Description: interact with fabric
 * @Author: HouHao Ye
 * @Create: 2021-01-29 15:09
 **/
@Slf4j
@Service("fabricService")
public class FabricServiceInterfaceImpl implements FabricServiceInterface {

    @Resource
    LogIndexDataBaseService logIndexDataBaseService;

    // K8S
    // connection.json: the connection information of fabric
    //private static final Path NETWORK_CONFIG_PATH = Paths.get("E:\\JAVA CODE\\SpringWorksapce\\Fabric-log-process\\fabricSDK\\src\\main\\resources\\connection-kubernetes.json");
    // the information of fabric network
    //private static final Path credentialPath = Paths.get("E:\\JAVA CODE\\SpringWorksapce\\Fabric-log-process\\fabricSDK\\src\\main\\resources\\crypto-config\\organizations\\peerOrganizations\\org1-example-com\\users\\Admin@org1-example-com\\msp");


    // docker
    private static final Path NETWORK_CONFIG_PATH = Paths.get("D:\\university\\blockchain\\logProcess\\fabricSDK\\src\\main\\resources\\connection.json");
    private static final Path credentialPath = Paths.get("D:\\university\\blockchain\\logProcess\\fabricSDK\\src\\main\\resources\\crypto-config\\peerOrganizations\\org1.example.com\\users\\Admin@org1.example.com\\msp");

    private X509Certificate certificate;
    private PrivateKey privateKey;
    private Wallet wallet;
    private Gateway gateway;
    private Gateway.Builder builder;
    private Network network;
    private Channel channel;
    private Contract contract;
    private Collection<Peer> peerSet;

    private static X509Certificate readX509Certificate(final Path certificatePath) throws IOException, CertificateException {
        try (Reader certificateReader = Files.newBufferedReader(certificatePath, StandardCharsets.UTF_8)) {
            return Identities.readX509Certificate(certificateReader);
        }
    }

    private static PrivateKey getPrivateKey(final Path privateKeyPath) throws IOException, InvalidKeyException {
        try (Reader privateKeyReader = Files.newBufferedReader(privateKeyPath, StandardCharsets.UTF_8)) {
            return Identities.readPrivateKey(privateKeyReader);
        }
    }

    public FabricServiceInterfaceImpl() {
        try {
            this.certificate = readX509Certificate(credentialPath.resolve(Paths.get("signcerts", "Admin@org1.example.com-cert.pem")));
            this.privateKey = getPrivateKey(credentialPath.resolve(Paths.get("keystore", "priv_sk")));

            // load identities into wallet
            this.wallet = Wallets.newInMemoryWallet();
            this.wallet.put("user", Identities.newX509Identity("Org1MSP", certificate, privateKey));
        } catch (Exception e) {
            log.info("reading certificate error");
            e.printStackTrace();
        }

        try {
            // set connection information
            this.builder = Gateway.createBuilder().identity(this.wallet, "user").networkConfig(NETWORK_CONFIG_PATH);

            // connect to fabric network
            this.gateway = builder.connect();
            // connect to channel
            this.network = gateway.getNetwork("mychannel");

            this.channel = network.getChannel();
            this.contract = network.getContract("mycc");
            this.peerSet = channel.getPeers();
        } catch (Exception e) {
            log.info("connect error");
            e.printStackTrace();
        }
    }

    public Log queryFromIndexDB(String key) {
        CommonResult<?> logIndexResult = logIndexDataBaseService.getLogByOriginalKey(key);
        // TODO: if there is no related log in the db, return error
        if(logIndexResult.isError()) return null;

        log.info("****************************query merged logID from indexDB {}", logIndexResult.getData());

        return JSON.parseObject(JSON.toJSONString(logIndexResult.getData()), Log.class);
    }

    public String queryFromBlockchain(String key) {
        byte[] queryResult = new byte[]{};
        try {
            queryResult = contract.evaluateTransaction("getData", key);
        } catch (ContractException e) {
            e.printStackTrace();
        }
        log.info("****************************query from fabric{}", new String(queryResult, StandardCharsets.UTF_8));
        return new String(queryResult, StandardCharsets.UTF_8);
    }

    /**
     * according to original_key to search mergedLog in the blockchain
     * @param deferred
     * @param key the original key
     * @return the merged log in the blockchain
     */
    @Async
    @Override
    public BlockchainLog query(DeferredResult<CommonResult<?>> deferred, String key) {

        //try {
        //    //Thread.sleep(6 * 1000L);
        //    TimeUnit.MILLISECONDS.sleep(10 * 1000L);
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}


        // TODO: 处理一下查询index db 失败的情况
        Log logIndex = queryFromIndexDB(key);
        if(logIndex == null) return null; // query from index db is error

        // TODO: 根据从处理一下从区块链中查询出来的结果
        String mergeLogString = queryFromBlockchain(logIndex.getIntegratedKey());
        Map<String, Object> mergedLogMap = JSONObject.parseObject(mergeLogString);

        // TODO: 对合并后的日志进行分解，拿到最初的日志
        mergedLogMap.remove("count");
        JSONArray mergedItems = (JSONArray) mergedLogMap.get("list");
        //log.info("list class = {}", mergedLogMap.get("list").getClass());
        Map<String, Object> itsOwnItems = JSON.parseObject(JSON.toJSONString(mergedItems.get(logIndex.getIdx())));
        for(Map.Entry<String, Object> items : itsOwnItems.entrySet()) {
            mergedLogMap.put(items.getKey(), items.getValue());
        }
        mergedLogMap.remove("list");


        // TODO: 这里怎么不初始化，利用Spring的机制
        BlockchainLog queryResult = new BlockchainLog();
        queryResult.setKey(logIndex.getIntegratedKey());
        try {
            queryResult.setValue(JSON.toJSONString(mergedLogMap));
            log.info("************************The key of {} is: {}", queryResult.getKey(), queryResult.getValue());
            deferred.setResult(new CommonResult<>(BaseError.BLOCKCHAIN_QUERY_SUCCESS, queryResult));
        } catch (Exception e) {
            log.info("************************queryByPeer Error");
            deferred.setErrorResult(new CommonResult<>(BaseError.BLOCKCHAIN_QUERY_ERROR, queryResult));
            e.printStackTrace();
        }
        return queryResult;
    }

    @Async
    @Override
    public Boolean invoke(DeferredResult<CommonResult<?>> deferred, BlockchainLog blockchainLog) {

        try {
            // TODO: 修改相应的智能合约
            Transaction transaction = contract.createTransaction("putData");
            transaction.setEndorsingPeers(channel.getPeers());
            transaction.submit(blockchainLog.getKey(), blockchainLog.getValue());

            log.info("invoke in blockchain");
            deferred.setResult(new CommonResult<>(BaseError.BLOCKCHAIN_INVOKE_SUCCESS, blockchainLog));
        } catch (Exception e) {
            log.info("Invoke Error");
            deferred.setErrorResult(new CommonResult<>(BaseError.BLOCKCHAIN_INVOKE_ERROR, blockchainLog));
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
