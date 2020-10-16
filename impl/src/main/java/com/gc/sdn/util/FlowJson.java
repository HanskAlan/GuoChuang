package com.gc.sdn.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gc.sdn.constant.Constant;

public class FlowJson {
    JSONObject flowTable = new JSONObject();
    int coflowId,flowId;

    /**
     * 建立FlowJson框架
     */
    public FlowJson(int coflowId, int flowId, String target, String nodeName){
        this.coflowId = coflowId;
        this.flowId = flowId;

        flowTable.put("table_id",0);
        flowTable.put("priority",Constant.priority);
        flowTable.put("hard-timeout",Constant.hardTimeOut);
        flowTable.put("idle-timeout", Constant.idleTimeOut);
        flowTable.put("node",
                "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='" + nodeName + "']"
        ); // 这个是RPC方法需要的

        // 匹配项
        flowTable.put("match", new JSONObject()
                .fluentPut("ipv4-destination", target + "/32") // 额外的过滤项
                .fluentPut("udp-source-port",65535 - Constant.hash(coflowId,flowId)) // 这个是核心
                .fluentPut("udp-destination-port",5001) // 这个也是
                .fluentPut("ethernet-match", new JSONObject() // 这个是必须的
                        .fluentPut("ethernet-type",new JSONObject()
                                .fluentPut("type",2048)
                        )
                )
                .fluentPut("ip-match",new JSONObject() // 这个也是必须的，不知道为什么
                        .fluentPut("ip-protocol",17)
                )
        );

    }

    /**
     * 设置动作：丢包
     */
    public FlowJson dropAction(){
        flowTable.put("priority",Constant.dropPriority);
        JSONObject actionsInstruction = new JSONObject();
        flowTable.put("instructions", new JSONObject()
                .fluentPut("instruction", new JSONArray()
                        .fluentAdd(actionsInstruction)
                )
        );
        // apply-action 的 instruction
        actionsInstruction
                .fluentPut("order",0)
                .fluentPut("apply-actions",new JSONObject()
                        .fluentPut("action",new JSONArray()
                                .fluentAdd(new JSONObject()
                                        .fluentPut("order",0)
                                        .fluentPut("drop-action",new JSONObject())
                                )
                        )
                );

        return this;
    }


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

    /**
     * 设置动作：转发。json格式如上所示
     */
    public FlowJson transmitAction(String outPort){
        // 转发动作
        JSONObject meterInstruction = new JSONObject();
        JSONObject actionsInstruction = new JSONObject();
        flowTable.put("instructions", new JSONObject()
                .fluentPut("instruction", new JSONArray()
                        .fluentAdd(meterInstruction)
                        .fluentAdd(actionsInstruction)
                )
        );
        // meter 的 instruction
        meterInstruction
                .fluentPut("order",0)
                .fluentPut("meter",new JSONObject()
                        .fluentPut("meter-id",String.valueOf(Constant.hash(coflowId,flowId)))
                );

        // apply-action 的 instruction
        actionsInstruction
                .fluentPut("order",1)
                .fluentPut("apply-actions",new JSONObject()
                        .fluentPut("action",new JSONArray()
                                .fluentAdd(new JSONObject()
                                        .fluentPut("order",1)
                                        .fluentPut("output-action",new JSONObject()
                                                .fluentPut("output-node-connector",outPort)
                                        )
                                )
                        )
                );
        return this;
    }
    /**
     * @return 返回包装后的值
     */
    public String getRPCFlowTable() {
        return new JSONObject().fluentPut("input",flowTable).toString();
    }


}
