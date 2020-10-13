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
import com.gc.sdn.controller.PushFlowController;
import com.gc.sdn.service.PushFlowService;
import com.gc.sdn.util.ParameterUtil;
import com.routineAlgorithm.controller.RAC;
import com.routineAlgorithm.controller.RACLog;
import com.routineAlgorithm.json.JsonFormatException;
import com.routineAlgorithm.solver.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;



public class CoFlowPacketProcessingListener implements PacketProcessingListener{

    private static int count = 1;
//    private static List listFlow = new ArrayList();
    private static final Map<Integer,ArrayList<Integer>> coflowMap = new HashMap<>();
    private static final Map<Integer,Map<Integer,Long>> timeMap = new HashMap<>(); // 记录上次结束的时间

    private static final Logger logger = LoggerFactory.getLogger(CoFlowPacketProcessingListener.class);

    public CoFlowPacketProcessingListener() {
        logger.info("[CoFlowPacketProcessingListener] CoFlowPacketProcessingListener Initiated. ");
    }

    @Override
    public void onPacketReceived(PacketReceived notification) {
        ParameterUtil parameterUtil = new ParameterUtil();
        if(count == 1){
            JSONObject jsonObject = parameterUtil.initTopologyInfo(Constant.host,Constant.port,Constant.username,
                    Constant.password, Constant.containerName);
            if(jsonObject == null){
                return;
            }else if(jsonObject.size() == 0){
                return;
            }else{
                try {
                    RACLog.instance().setPath(Constant.RAC_LOG_PATH);
                    RAC.instance().INITIAL_RAC(jsonObject);
                    System.out.println("Choose OMCoflow solver");
//                    RAC.instance().setSolver(new ECMPSolver());
                    RAC.instance().setSolver(new OMCoflowSolver()); // 这个是默认的
//                    RAC.instance().setSolver(new OMCoflowRSolver());
//                    RAC.instance().setSolver(new OMCoflowASolver());
//                    RAC.instance().setSolver(new RapierSolver());
//                    RAC.instance().setSolver(new RandomizeRapierSolver());
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
        String payloadDstIp = "";
        String payloadSrcIp = "";
        byte[] strBuffer = PacketParsing.extractStrInfo(payload);
        String str = new String(strBuffer, StandardCharsets.UTF_8);
        str = str.replaceAll("[\u0000]","");
        if(!Objects.equals(str, "")){
            String[] s = str.split(";");
            String[] strCoFlowId = s[0].split("=");
            coFlowID = Integer.parseInt(strCoFlowId[1]);
            String[] strFlowCount = s[1].split("=");
            flowCount = Integer.parseInt(strFlowCount[1]);
            String[] strFLowId = s[2].split("=");
            flowId = Integer.parseInt(strFLowId[1]);
            String[] strDataSize = s[3].split("=");
            dataSize = Long.parseLong(strDataSize[1]);
            String[] strSrcIp = s[4].split("=");
            payloadSrcIp = strSrcIp[1];
            String[] strDetIp = s[5].split("=");
            payloadDstIp = strDetIp[1];
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

        // 避免抖动，一条流传输结束一段时间内直接拒绝
        if(timeMap.containsKey(coFlowID) &&
                timeMap.get(coFlowID).containsKey(flowId) &&
                timeMap.get(coFlowID).get(flowId) + 5000 > System.currentTimeMillis()){
            timeMap.get(coFlowID).put(flowId,System.currentTimeMillis());
            return;
        }

        // 第一次接受正向数据包
        if(payloadDstIp.equals(dstIp)){
            // 在内存中记录该类数据包的消息
            if(!coflowMap.containsKey(coFlowID))
                coflowMap.put(coFlowID, new ArrayList<>());
            ArrayList<Integer> listFlow = coflowMap.get(coFlowID);
            if(listFlow.contains(flowId)) return;
            listFlow.add(flowId);

            // 添加Drop流表，抑制传输，以避免过流正式下发流表之前的传输
            PushFlowService.pushDropFlow(coFlowID,flowId,payloadSrcIp,payloadDstIp);
            try {
                // 假如RAC返回了一个方案，则安排传输
                if (RAC.instance().ARRIVE_AND_TRY(jsonFlow,System.currentTimeMillis())) {
                    JSONArray arrayGet = RAC.instance().GET_ANSWER_FAST_JSON();
                    if (arrayGet != null && arrayGet.size() > 0) {
                        PushFlowController.startRacPushFlow(arrayGet);
                    }
                }
            } catch (JsonFormatException e) {
                e.printStackTrace();
            }
        }else if(payloadSrcIp.equals(dstIp)){// 假如包向着payloadSrcIp发送过去
            // 假如coflow不存在或者flow不存在，就不是相应的ack信号，过滤
            if(!coflowMap.containsKey(coFlowID))return;
            ArrayList<Integer> listFlow = coflowMap.get(coFlowID);
            if(!listFlow.contains(flowId))return;

            // 接收到反向应答信号
            try {
                // 流传输完成，但是其他的还没完成（你之前把一个coflow的全部flow都删掉了）
                if (RAC.instance().COMPLETE_AND_TRY(jsonFlow, System.currentTimeMillis())) {
                    JSONArray arrayGet = RAC.instance().GET_ANSWER_FAST_JSON();
                    if (arrayGet != null && arrayGet.size() > 0) {
                        PushFlowController.startRacPushFlow(arrayGet);
                    }
                }
                listFlow.remove(Integer.valueOf(flowId));
                if(listFlow.size() == 0)coflowMap.remove(coFlowID);
                // 同时在timeMap中记录上次结束的时间，以避免统一体流的反复处理
                if(!timeMap.containsKey(coFlowID))timeMap.put(coFlowID,new HashMap<>());
                Map<Integer, Long> flowTimeMap = timeMap.get(coFlowID);
                flowTimeMap.put(flowId,System.currentTimeMillis());
            } catch (JsonFormatException e) {
                e.printStackTrace(); // 我也不知道这种情况怎么办
            }
        }
    }

}
