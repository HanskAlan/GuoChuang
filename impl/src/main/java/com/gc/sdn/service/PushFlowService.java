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

    public static void pushFlowAndMeter(int coflowId, int flowId, Double rate, String inPort, String outPort, String nodeName, String dstIp, int p){
        // 初始化odl工具类用于登陆的权限验证 初始话的时候运行了一些东西，不知道有什么用
        @SuppressWarnings("unused") OdlUtil odlUtil = new OdlUtil(Constant.host,Constant.port,Constant.username,Constant.password,Constant.containerName);
        int bandwidth = new Double(rate * 1000).intValue();
        // 调用odl接口下发meter表
        JSONObject jsonObjectMeter = MeterJson.getMeterJson(coflowId,bandwidth,flowId,nodeName,p);
        OdlUtil.installRPC(
                jsonObjectMeter.toJSONString(),
                METER_URI
        );

        // 调用odl接口下发流
        OdlUtil.installRPC(
                new FlowJson(coflowId,flowId,dstIp,nodeName).transmitAction(outPort).getRPCFlowTable(),
                FLOW_URI
        );
    }

    public static void pushDropFlow(int co_flow_id, int flow_id, String srcIp, String dstIp){
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
                FLOW_URI
        );
    }
}
