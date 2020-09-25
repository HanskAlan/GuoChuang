package com.gc.sdn.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @author: pan.wen
 * @date:2020/7/17 16:08
 * @Name: FlowJson
 */
@Service
public class FlowJson {

    public JSONObject getFlowJson(int co_flow_id,int flow_id, String target, String in_port, String output,int p){

        JSONObject jsonObjectAll = new JSONObject();
        JSONArray jsonArrayInventory = new JSONArray();

        JSONObject jsonObjectInventory = new JSONObject();
        jsonObjectInventory.put("id",flow_id);
        jsonObjectInventory.put("flow-name","");
        jsonObjectInventory.put("cookie",256);

        JSONObject jsonObjectOutputAction = new JSONObject();
        jsonObjectOutputAction.put("output-node-connector",output);

        JSONObject jsonObjectAction = new JSONObject();
        jsonObjectAction.put("order",0);
        jsonObjectAction.put("output-action",jsonObjectOutputAction);

        JSONArray jsonArrayAction = new JSONArray();
        jsonArrayAction.add(jsonObjectAction);

        JSONObject jsonObjectApplyActions = new JSONObject();
        jsonObjectApplyActions.put("action",jsonArrayAction);

        JSONObject jsonObjectInstruction = new JSONObject();
        jsonObjectInstruction.put("order",0);
        jsonObjectInstruction.put("apply-actions",jsonObjectApplyActions);

        JSONObject jsonMeterId = new JSONObject();
        jsonMeterId.put("meter-id",String.valueOf(co_flow_id)+flow_id);

        JSONObject jsonMeter = new JSONObject();
        jsonMeter.put("order",0);
        jsonMeter.put("meter",jsonMeterId);

        JSONArray jsonArrayInstruction = new JSONArray();
        jsonArrayInstruction.add(jsonObjectInstruction);
        jsonArrayInstruction.add(jsonMeter);

        JSONObject jsonObjectInstructions = new JSONObject();
        jsonObjectInstructions.put("instruction",jsonArrayInstruction);

        jsonObjectInventory.put("instructions",jsonObjectInstructions);

        jsonObjectInventory.put("priority",666);
        jsonObjectInventory.put("table_id",0);

        JSONObject jsonObjectMatch = new JSONObject();
        jsonObjectMatch.put("ipv4-destination",target+"/32");
        jsonObjectMatch.put("in-port",in_port);

        JSONObject jsonObjectEthernetType = new JSONObject();
        jsonObjectEthernetType.put("type",2048);

        JSONObject jsonObjectEthernetMatch = new JSONObject();
        jsonObjectEthernetMatch.put("ethernet-type",jsonObjectEthernetType);

        jsonObjectMatch.put("ethernet-match",jsonObjectEthernetMatch);

        jsonObjectInventory.put("match",jsonObjectMatch);

        jsonArrayInventory.add(jsonObjectInventory);

        jsonObjectAll.put("flow-node-inventory:flow",jsonArrayInventory);

        return jsonObjectAll;
    }
}
