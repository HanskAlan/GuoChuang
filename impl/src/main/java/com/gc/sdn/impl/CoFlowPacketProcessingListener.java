/*
 * Copyright © 2017 yy and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.gc.sdn.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gc.sdn.constant.Constant;
import com.gc.sdn.controller.GetAnswerFromRacController;
import com.gc.sdn.util.ParameterUtil;
import com.routineAlgorithm.controller.RAC;
import com.routineAlgorithm.controller.RACLog;
import com.routineAlgorithm.json.JsonFormatException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;


/**
 * @author: pan.wen
 */
public class CoFlowPacketProcessingListener implements PacketProcessingListener{

    private static int count = 1;
//    private static List listFlow = new ArrayList();
    private static Map<Integer,List> coflowMap = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(CoFlowPacketProcessingListener.class);

    public CoFlowPacketProcessingListener() {
        logger.info("[CoFlowPacketProcessingListener] CoFlowPacketProcessingListener Initiated. ");
    }

    @Override
    public void onPacketReceived(PacketReceived notification) {
        ParameterUtil parameterUtil = new ParameterUtil();
        GetAnswerFromRacController getAnswerFromRacController = new GetAnswerFromRacController();
        if(count == 1){
            JSONObject jsonObject = parameterUtil.initTopologyInfo(Constant.host,Constant.port,Constant.username,
                    Constant.password, Constant.containerName);
            if(jsonObject == null){
                return;
            }else if(jsonObject.size() == 0){
                return;
            }else{
                try {
                    RACLog.instance().setPath("D:\\EP3Mulcos_ODL\\target\\RACLog\\");
                    RAC.instance().INITIAL_RAC(jsonObject);
                    count++;
                    logger.info("Topology initialization completed successfully!");
                    return;
                } catch (JsonFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        // 从notification获取payload
        byte[] payload = notification.getPayload();
        // 获取payload的大小
        int packetSize = payload.length;
        // 解析MAC地址
        byte[] srcMacRaw = PacketParsing.extractSrcMac(payload);
        byte[] dstMacRaw = PacketParsing.extractDstMac(payload);
        String srcMac = PacketParsing.rawMacToString(srcMacRaw);
        String dstMac = PacketParsing.rawMacToString(dstMacRaw);

        // 解析端口地址
        byte[] srcPortRaw = PacketParsing.extractSrcPort(payload);
        byte[] dstPortRaw = PacketParsing.extractDstPort(payload);
        // 源端口
        int srcPort = PacketParsing.rawPortToInteger(srcPortRaw);
        // 目的端口
        int dstPort = PacketParsing.rawPortToInteger(dstPortRaw);

        // 解析IP地址
        byte[] srcIpRaw = PacketParsing.extractSrcIP(payload);
        byte[] dstIpRaw = PacketParsing.extractDstIP(payload);
        String srcIp = PacketParsing.rawIPToString(srcIpRaw);
        String dstIp = PacketParsing.rawIPToString(dstIpRaw);

        byte[] srcTypeRaw = PacketParsing.extractEtherType(payload);
        String srcType = PacketParsing.rawEthTypeToString(srcTypeRaw);
        byte[] srcProtocolRaw = PacketParsing.extractIPProtocol(payload);
        String srcProtocol = PacketParsing.rawIPProtoToString(srcProtocolRaw);

        logger.info("srcMac {}, dstMac {},srcIp {},dstIp {}, srcPort {}, dstPort {},srcType {},srcProtocol {}",
                srcMac, dstMac, srcIp, dstIp, srcPort, dstPort, srcType, srcProtocol);
        if(!ParameterUtil.hostMap.containsValue(srcIp)){
            return;
        }

        // 拼接流参数
        JSONObject jsonFlow = new JSONObject();
        jsonFlow.put("source",srcIp);
        jsonFlow.put("target",dstIp);


        int coFlowID = 0;
        int flowId = 0;
        int flowCount = 0;
        long dataSize = 0;
//        String lastPacketFlag = "";
//        long lastPacketSize = 0;
        String det_ip = "";
        try {
            byte[] strBuffer = PacketParsing.extractStrInfo(payload);
            String str = new String(strBuffer, "utf-8");
            str = str.replaceAll("[\u0000]","");
            if(str != null && str != ""){
                String[] s = str.split(";");
                String[] strCoFlowId = s[0].split("=");
                coFlowID = Integer.parseInt(strCoFlowId[1]);
                String[] strFlowCount = s[1].split("=");
                flowCount = Integer.parseInt(strFlowCount[1]);
                String[] strFLowId = s[2].split("=");
                flowId = Integer.parseInt(strFLowId[1]);
                String[] strDataSize = s[3].split("=");
                dataSize = Long.parseLong(strDataSize[1]);
//                String[] strIsLast = s[3].split("=");
//                lastPacketFlag = strIsLast[1];
                String[] strDetIp = s[5].split("=");
                det_ip = strDetIp[1];
                if(flowCount != 0){
                    jsonFlow.put("coflowSize",flowCount);
                }else{
                    return;
                }
                if(coFlowID != 0){
                    jsonFlow.put("coflowID",coFlowID);
                }else{
                    return;
                }
                if(flowId != 0){
                    jsonFlow.put("flowID",flowId);
                }else{
                    return;
                }
                if(dataSize != 0){
                    jsonFlow.put("size",dataSize);
                }else {
                    return;
                }
            }else{
                return;
            }
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }

        if(det_ip != "" && det_ip.equals(dstIp)){
            List listFlow = new ArrayList();
            if(!coflowMap.containsKey(coFlowID)) {
                coflowMap.put(coFlowID, listFlow);
            }
            listFlow = coflowMap.get(coFlowID);
            if(listFlow.contains(flowId)){
                return;
            }else {
                listFlow.add(flowId);
                try {
                    long timeArrive = System.currentTimeMillis();
                    RAC.instance().FLOW_ARRIVE(jsonFlow, timeArrive);
                    boolean solve = RAC.instance().TRY_SOLVE();
                    if (solve) {
                        JSONArray arrayGet = RAC.instance().GET_ANSWER_FAST_JSON();
                        if (arrayGet != null && arrayGet.size() > 0) {
                            getAnswerFromRacController.startRacPushFlow(arrayGet);
                        }
                    }
                } catch (JsonFormatException e) {
                    e.printStackTrace();
                }
            }
        }else if(det_ip != "" && !det_ip.equals(dstIp)){
            if(coflowMap.get(coFlowID).size() > 0){
                if(coflowMap.get(coFlowID).contains(flowId)){
                    try {
                        for (int i = 0; i < coflowMap.get(coFlowID).size(); i++) {
                            long timeComplete = System.currentTimeMillis();
                            jsonFlow.put("flowID", coflowMap.get(coFlowID).get(i));
                            RAC.instance().FLOW_COMPLETE(jsonFlow, timeComplete);
                        }
                    }catch (JsonFormatException e){
                        e.printStackTrace();
                    }finally {
                        coflowMap.remove(coFlowID);
                    }
                }else{
                    return;
                }
            }else{
                return;
            }
        }
//            if(!"lastPacket".equals(lastPacketFlag)){
//                if(dataSize != 0){
//                    long flowSizeCount = packetMap.get(dstPort);
//                    packetMap.put(dstPort,dataSize + flowSizeCount);
//                    lastFlowId = dstPort;
//                    return;
//                }
//                return;
//            }else if("lastPacket".equals(lastPacketFlag)){
//                long flowSizeCount = packetMap.get(dstPort);
//                packetMap.put(dstPort,dataSize + flowSizeCount);
//                lastFlowId = dstPort;
//                logger.info("packet send completed,please wait for the result to return!");
//                System.out.println("数据包发送完毕,请等待结果返回!");
//            }
//        }else{
//            if(!"lastPacket".equals(lastPacketFlag)){
//                if(dataSize != 0){
//                    long flowSizeCount = packetMap.get(dstPort);
//                    packetMap.put(dstPort,dataSize + flowSizeCount);
//                    lastFlowId = dstPort;
//                    return;
//                }
//                return;
//            }else if("lastPacket".equals(lastPacketFlag)){
//                long flowSizeCount = packetMap.get(dstPort);
//                packetMap.put(dstPort,dataSize + flowSizeCount);
//                lastFlowId = dstPort;
//                logger.info("packet send completed,please wait for the result to return!");
//                System.out.println("数据包发送完毕,请等待结果返回!");
//            }



        // 协流所有数据包到达后开始调用算法
//        try {
//            // 子流到达
//            if(flowCount != 0 && "lastPacket".equals(lastPacketFlag) ) {
//                // 遍历packetMap，取出子流id及对应数据包个数
//                for (Integer key : packetMap.keySet()) {
//                    jsonFlow.put("flowID", key);
//                    if(key != lastFlowId) {
//                        jsonFlow.put("size", packetMap.get(key));
//                    }else if(key == lastFlowId) {
//                        jsonFlow.put("size", packetMap.get(key) + lastPacketSize);
//                    }
//                    long timeArrive = System.currentTimeMillis();
//                    logger.info("call algorithm json {},arrived time {}", jsonFlow.toString(), timeArrive);
//                    RAC.instance().FLOW_ARRIVE(jsonFlow, timeArrive);
//                    logger.info("flow send end completed!");
//                    boolean solve = RAC.instance().TRY_SOLVE();
//                    if(solve){
//                        JSONArray arrayGet = RAC.instance().GET_ANSWER_FAST_JSON();
//                        if(arrayGet != null && arrayGet.size() > 0){
//                            try {
//                                getAnswerFromRacController.startRacPushFlow(arrayGet);
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }finally {
//                                long timeComplete = System.currentTimeMillis();
//                                for (Integer keyComplete : packetMap.keySet()){
//                                    jsonFlow.put("flowID",keyComplete);
//                                    RAC.instance().FLOW_COMPLETE(jsonFlow,timeComplete);
//                                }
//                            }
//                        }
//                        packetMap.clear();
//                        listFlow.clear();
//                        logger.info("coflow send completed!");
//                    }
//                }
//            }else{
//                logger.info("Serious packet loss,trying to resend!");
//                System.out.println("丢包严重，正在尝试重新发送！");
//                return;
//            }
//        } catch (JsonFormatException e) {
//            e.printStackTrace();
//        }
    }

}
