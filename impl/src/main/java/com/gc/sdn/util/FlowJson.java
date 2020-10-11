package com.gc.sdn.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gc.sdn.constant.Constant;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @author: pan.wen
 * @date: 2020/7/17 16:08
 * @Name: FlowJson
 */
@Service
public class FlowJson {

    public JSONObject getFlowJson(int co_flow_id,int flow_id, String target, String in_port, String output,int p){

        JSONObject jsonObjectAll = new JSONObject();
        JSONArray jsonArrayInventory = new JSONArray();

        JSONObject jsonObjectInventory = new JSONObject();
        jsonObjectInventory.put("id",Constant.hash(co_flow_id,flow_id));
        jsonObjectInventory.put("table_id",0);
        jsonObjectInventory.put("flow-name","");
        jsonObjectInventory.put("cookie",256);
        jsonObjectInventory.put("priority",Constant.priority);
        jsonObjectInventory.put("hard-timeout",Constant.hardTimeOut);
        jsonObjectInventory.put("idle-timeout", Constant.idleTimeOut);


        /* 下面需要的是这种形式
         * <instructions>
         *     <instruction>
         *         <order>0</order>
     *             <meter>
     *                 <meter-id>1</meter-id>
     *             </meter>
         *     </instruction>
         *     <instruction>
         *         <order>1</order>
         *         <apply-actions>
         *             <action>
             *             <order>1</order>
             *             <output-action>
             *                 <output-node-connector>2</output-node-connector>
             *             </output-action>
         *             </action>
         *         </apply-actions>
         *     </instruction>
         * </instructions>
         */

//        "instructions": {
//            "instruction": [
//                {
//                    "order": 0,
//                    "apply-actions": {
//                        "action": [
//                            {
//                                "order": 0,
//                                "dec-nw-ttl": {}
//                            }
//                        ]
//                    }
//                }
//            ]
//        }
//
        // 设置整体状态
        JSONObject meterInstruction = new JSONObject();
        JSONObject actionsInstruction = new JSONObject();
        jsonObjectInventory.put("instructions", new JSONObject()
                .fluentPut("instruction", new JSONArray()
                        .fluentAdd(meterInstruction)
                        .fluentAdd(actionsInstruction)
                )
        );
        // meter 的 instruction
        meterInstruction
                .fluentPut("order",0)
                .fluentPut("meter",new JSONObject()
                        .fluentPut("meter-id",String.valueOf(Constant.hash(co_flow_id,flow_id)))
                );

        // apply-action 的 instruction
        actionsInstruction
                .fluentPut("order",1)
                .fluentPut("apply-actions",new JSONObject()
                        .fluentPut("action",new JSONArray()
                                .fluentAdd(new JSONObject()
                                        .fluentPut("order",1)
                                        .fluentPut("output-action",new JSONObject()
                                                .fluentPut("output-node-connector",output)
                                        )
                                )
                        )
                );





        // 匹配项
        jsonObjectInventory.put("match", new JSONObject()
                .fluentPut("ipv4-destination", target + "/32") // 额外的过滤项
                // 讲一讲下面这一项，如果要正确使用的话应该是以e7-eth1 或者c1-eth10的形式使用，虽然处理这个东西比较麻烦
                .fluentPut("in-port", in_port) // 额外的过滤项
                .fluentPut("udp-source-port",65535 - Constant.hash(co_flow_id,flow_id)) // 这个是核心
                .fluentPut("udp-destination-port",5001) // 这个也是
                .fluentPut("ethernet-match", new JSONObject() // 这个是必须的
                        .fluentPut("ethernet-type",new JSONObject()
                                .fluentPut("type",2048)
                        )
                )
                .fluentPut("ip-match",new JSONObject() // 这个也是必须的，不知道为什么
                        .fluentPut("ip-protocol",17)
//                        .fluentPut("ip-dscp",8)
//                        .fluentPut("ip-ecn",3)
                )
        );
        jsonArrayInventory.add(jsonObjectInventory);

        jsonObjectAll.put("flow-node-inventory:flow",jsonArrayInventory);

        return jsonObjectAll;
    }
}
