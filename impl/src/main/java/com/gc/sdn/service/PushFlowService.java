package com.gc.sdn.service;

import com.alibaba.fastjson.JSONObject;
import com.gc.sdn.constant.Constant;
import com.gc.sdn.util.FlowJson;
import com.gc.sdn.util.MeterJson;
import com.gc.sdn.util.OdlUtil;

/**
 * @Description:
 * @author: pan.wen
 * @date:2020/9/9 13:53
 * @Name: GetAnswerFromRacService
 */
public class PushFlowService {

    FlowJson flowJson = new FlowJson();

    MeterJson meterJson = new MeterJson();

    public void startRacPushFlow(int co_flow_id,int flow_id, Double rate,String in_port,String out_port,String nodeId, String target, int p){

        String apiUri="http://"+Constant.host+":8181/restconf/config/opendaylight-inventory:nodes/node/";

        // 初始化odl工具类用于登陆的权限验证
        OdlUtil odlUtil = new OdlUtil(Constant.host,Constant.port,Constant.username,Constant.password,Constant.containerName);

        int bandwidth = new Double(rate*1000).intValue();
        // 调用odl接口下发meter表
        String meterUri = apiUri+nodeId+"/meter/"+co_flow_id + flow_id;
        System.out.println(meterUri);
        JSONObject jsonObjectMeter = meterJson.getMeterJson(co_flow_id,bandwidth,flow_id,p);
        odlUtil.installMeter(jsonObjectMeter,meterUri);

        // 调用odl接口下发流
        String flowUri = apiUri+nodeId+"/flow-node-inventory:table/0/flow/"+flow_id;
        System.out.println(flowUri);
        JSONObject jsonObjectFlow = flowJson.getFlowJson(co_flow_id,flow_id,target,in_port,out_port,p);
        odlUtil.installFlow(jsonObjectFlow, flowUri);
    }
}
