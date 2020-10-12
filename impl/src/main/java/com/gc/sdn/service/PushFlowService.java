package com.gc.sdn.service;

import com.alibaba.fastjson.JSONObject;
import com.gc.sdn.constant.Constant;
import com.gc.sdn.util.FlowJson;
import com.gc.sdn.util.MeterJson;
import com.gc.sdn.util.OdlUtil;
import static com.gc.sdn.util.ParameterUtil.*;

/**
 * @Description:
 * @author: pan.wen
 * @date:2020/9/9 13:53
 * @Name: GetAnswerFromRacService
 */
public class PushFlowService {

    public static void pushFlowAndMeter(int co_flow_id, int flow_id, Double rate, String in_port, String out_port, String nodeId, String target, int p){
        String apiUri="http://"+Constant.host+":8181/restconf/config/opendaylight-inventory:nodes/node/";

        // 初始化odl工具类用于登陆的权限验证 初始话的时候运行了一些东西，不知道有什么用
        @SuppressWarnings("unused") OdlUtil odlUtil = new OdlUtil(Constant.host,Constant.port,Constant.username,Constant.password,Constant.containerName);

        int bandwidth = new Double(rate*1000).intValue();
        // 调用odl接口下发meter表
        String meterUri = apiUri + nodeId + "/meter/" + Constant.hash(co_flow_id,flow_id);
        System.out.println(meterUri);
        JSONObject jsonObjectMeter = MeterJson.getMeterJson(co_flow_id,bandwidth,flow_id,p);
        OdlUtil.installMeter(jsonObjectMeter,meterUri);

        // 调用odl接口下发流
        String flowUri = apiUri + nodeId + "/flow-node-inventory:table/0/flow/" + Constant.hash(co_flow_id,flow_id);
        System.out.println(flowUri);
        JSONObject jsonObjectFlow = FlowJson.getFlowJson(co_flow_id,flow_id,target,in_port,out_port,p);
        OdlUtil.installFlow(jsonObjectFlow, flowUri);
    }

    public static void pushDropFlow(int co_flow_id, int flow_id, String srcIp, String dstIp){
        String apiUri="http://"+Constant.host+":8181/restconf/config/opendaylight-inventory:nodes/node/";
        String srcMac = nodeMap.get(srcIp),nodeId = null;
        for(String tmp : linkMap.keySet()){
            String[] splitTmp = tmp.split("-");
            if(splitTmp[0].equals(srcMac)){
                nodeId = splitTmp[1];
            }
        }
        assert nodeId != null;

        // 调用odl接口下发流
        String flowUri = apiUri + nodeId + "/flow-node-inventory:table/0/flow/" + Constant.hash(co_flow_id,flow_id);
        System.out.println(flowUri);
        OdlUtil.installFlow(
                FlowJson.getDropFlowJson(co_flow_id,flow_id,dstIp),
                flowUri
        );
    }
}
