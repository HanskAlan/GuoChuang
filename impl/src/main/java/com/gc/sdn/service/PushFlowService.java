package com.gc.sdn.service;

import com.alibaba.fastjson.JSONObject;
import com.gc.sdn.constant.Constant;
import com.gc.sdn.util.FlowJson;
import com.gc.sdn.util.MeterJson;
import com.gc.sdn.util.OdlUtil;

import static com.gc.sdn.util.ParameterUtil.*;


public class PushFlowService {
    public static final String FLOW_URI = "http://" + Constant.host + ":8181/restconf/operations/sal-flow:add-flow";
    public static final String METER_URI = "http://" + Constant.host + ":8181/restconf/operations/sal-meter:add-meter";

    public static void pushFlowAndMeter(int coflowId, int flowId, Double rate, String outPort, String nodeName, String dstIp, int p){
        new pushFlowAndMeterThread(coflowId,flowId,rate,outPort,nodeName,dstIp,p).start();
    }

    public static void pushDropFlow(int co_flow_id, int flow_id, String srcIp, String dstIp){
        new PushDropFlowThread(co_flow_id, flow_id, srcIp, dstIp).start();
    }
}

class pushFlowAndMeterThread extends Thread{  // 继承Thread类，作为线程的实现类
    final int coflowId, flowId,p;
    final Double rate;
    final String outPort, nodeName, dstIp;

    public pushFlowAndMeterThread(int coflowId, int flowId, Double rate, String outPort, String nodeName, String dstIp, int p){
        this.coflowId = coflowId;
        this.flowId = flowId;
        this.rate = rate;
        this.outPort = outPort;
        this.nodeName = nodeName;
        this.dstIp = dstIp;
        this.p = p;
    }

    public void run(){  // 覆写run()方法，作为线程 的操作主体
        try{
            // 初始化odl工具类用于登陆的权限验证 构造函数里有一些东西，不知道有什么用
            @SuppressWarnings("unused") OdlUtil odlUtil = new OdlUtil(Constant.host,Constant.port,Constant.username,Constant.password,Constant.containerName);
            int bandwidth = new Double(rate * 8000).intValue();
            // 调用odl接口下发meter表
            JSONObject meter = MeterJson.getMeterJson(coflowId,bandwidth,flowId,nodeName,p);
            OdlUtil.installRPC(
                    meter.toJSONString(),
                    PushFlowService.METER_URI
            );

            // 调用odl接口下发流
            OdlUtil.installRPC(
                    new FlowJson(coflowId,flowId,dstIp,nodeName).transmitAction(outPort).getRPCFlowTable(),
                    PushFlowService.FLOW_URI
            );
        } catch (Exception e){
            System.out.println("pushFlowAndMeter Exception:");
            e.printStackTrace();
        }
    }
};


class PushDropFlowThread extends Thread{
    private final int co_flow_id;
    private final int flow_id;
    private final String srcIp;
    private final String dstIp;

    PushDropFlowThread(int co_flow_id, int flow_id, String srcIp, String dstIp){
        this.co_flow_id = co_flow_id;
        this.flow_id = flow_id;
        this.srcIp = srcIp;
        this.dstIp = dstIp;
    }
    public void run(){
        String srcMac = nodeMap.get(srcIp),nodeId = null;
        for(String tmp : linkMap.keySet()){
            String[] splitTmp = tmp.split("-");
            if(splitTmp[0].equals(srcMac)){
                nodeId = splitTmp[1];
            }
        }
        assert nodeId != null;

        OdlUtil.installRPC(
                new FlowJson(co_flow_id,flow_id,dstIp,nodeId).dropAction().getRPCFlowTable(),
                PushFlowService.FLOW_URI
        );
    }
}
