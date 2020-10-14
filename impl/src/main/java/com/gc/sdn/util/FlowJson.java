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
    public FlowJson(int coflowId, int flowId, String target, String odlSwitch){
        this.coflowId = coflowId;
        this.flowId = flowId;

        flowTable.put("table_id",0);
        flowTable.put("priority",Constant.priority);
        flowTable.put("hard-timeout",Constant.hardTimeOut);
        flowTable.put("idle-timeout", Constant.idleTimeOut);

        // 这个是RPC方法需要的
        flowTable.put("node",
                "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='" + odlSwitch + "']"
        );
        System.out.println(flowTable.get("node"));

        // 下面三个是在使用config store的时候才用得上的东西
//        flowTable.put("flow-name","");
//        flowTable.put("cookie",256);
//        flowTable.put("id",Constant.hash(coflowId,flowId));

        // 匹配项
        flowTable.put("match", new JSONObject()
                .fluentPut("ipv4-destination", target + "/32") // 额外的过滤项
//                .fluentPut("in-port", in_port) // 额外的过滤项，已经弃用了
                .fluentPut("udp-source-port",65535 - Constant.hash(coflowId,flowId)) // 这个是核心
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

    }

    /**
     * 设置动作：丢包
     */
    public FlowJson dropAction(){
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

//    public static JSONObject getFlowJson(int coflowId, int flowId, String target, String odlSwitch, String outPort){
//        JSONObject flowTable = new JSONObject();
//        flowTable.put("table_id",0);
//        flowTable.put("priority",Constant.priority);
//        flowTable.put("hard-timeout",Constant.hardTimeOut);
//        flowTable.put("idle-timeout", Constant.idleTimeOut);
//
//        // 这个是RPC方法需要的
//        flowTable.put("node",
//                "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='" + odlSwitch + "']"
//        );
//        System.out.println(flowTable.get("node"));
//
//
//        // 下面三个是在使用config store的时候才用得上的东西
////        flowTable.put("flow-name","");
////        flowTable.put("cookie",256);
////        flowTable.put("id",Constant.hash(coflowId,flowId));
//
//
//        /* 下面需要的是这种形式
//         * <instructions>
//         *     <instruction>
//         *         <order>0</order>
//     *             <meter>
//     *                 <meter-id>1</meter-id>
//     *             </meter>
//         *     </instruction>
//         *     <instruction>
//         *         <order>1</order>
//         *         <apply-actions>
//         *             <action>
//             *             <order>1</order>
//             *             <output-action>
//             *                 <output-node-connector>2</output-node-connector>
//             *             </output-action>
//         *             </action>
//         *         </apply-actions>
//         *     </instruction>
//         * </instructions>
//         */
//
////        "instructions": {
////            "instruction": [
////                {
////                    "order": 0,
////                    "apply-actions": {
////                        "action": [
////                            {
////                                "order": 0,
////                                "dec-nw-ttl": {}
////                            }
////                        ]
////                    }
////                }
////            ]
////        }
////
//        // 设置整体状态
//        JSONObject meterInstruction = new JSONObject();
//        JSONObject actionsInstruction = new JSONObject();
//        flowTable.put("instructions", new JSONObject()
//                .fluentPut("instruction", new JSONArray()
//                        .fluentAdd(meterInstruction)
//                        .fluentAdd(actionsInstruction)
//                )
//        );
//        // meter 的 instruction
//        meterInstruction
//                .fluentPut("order",0)
//                .fluentPut("meter",new JSONObject()
//                        .fluentPut("meter-id",String.valueOf(Constant.hash(coflowId,flowId)))
//                );
//
//        // apply-action 的 instruction
//        actionsInstruction
//                .fluentPut("order",1)
//                .fluentPut("apply-actions",new JSONObject()
//                        .fluentPut("action",new JSONArray()
//                                .fluentAdd(new JSONObject()
//                                        .fluentPut("order",1)
//                                        .fluentPut("output-action",new JSONObject()
//                                                .fluentPut("output-node-connector",outPort)
//                                        )
//                                )
//                        )
//                );
//
//
//
//
//
//        // 匹配项
//        flowTable.put("match", new JSONObject()
//                .fluentPut("ipv4-destination", target + "/32") // 额外的过滤项
//                // 讲一讲下面这一项，如果要正确使用的话应该是以e7-eth1 或者c1-eth10的形式使用，虽然处理这个东西比较麻烦
////                .fluentPut("in-port", in_port) // 额外的过滤项
//                .fluentPut("udp-source-port",65535 - Constant.hash(coflowId,flowId)) // 这个是核心
//                .fluentPut("udp-destination-port",5001) // 这个也是
//                .fluentPut("ethernet-match", new JSONObject() // 这个是必须的
//                        .fluentPut("ethernet-type",new JSONObject()
//                                .fluentPut("type",2048)
//                        )
//                )
//                .fluentPut("ip-match",new JSONObject() // 这个也是必须的，不知道为什么
//                        .fluentPut("ip-protocol",17)
////                        .fluentPut("ip-dscp",8)
////                        .fluentPut("ip-ecn",3)
//                )
//        );
//
//        return new JSONObject().fluentPut("input",flowTable);
//    }

//    public static JSONObject getDropFlowJson(int co_flow_id,int flow_id, String dstIp,String odlSwitch){
//        JSONObject flowTable = new JSONObject();
//        flowTable.put("table_id",0);
//        flowTable.put("priority",Constant.dropPriority);
//        flowTable.put("hard-timeout",Constant.hardTimeOut);
//        flowTable.put("idle-timeout", Constant.idleTimeOut);
//
////        flowTable.put("id",Constant.hash(co_flow_id,flow_id));
////        flowTable.put("flow-name","");
////        flowTable.put("cookie",256);
//
//        // 这个是RPC方法需要的
//        flowTable.put("node",
//                "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='" + odlSwitch + "']"
//        );
//        System.out.println(flowTable.get("node"));
//

//
//
//
//
//
//        // 匹配项
//        flowTable.put("match", new JSONObject()
//                .fluentPut("ipv4-destination", dstIp + "/32") // 额外的过滤项
////                // 讲一讲下面这一项，如果要正确使用的话应该是以e7-eth1 或者c1-eth10的形式使用，虽然处理这个东西比较麻烦
////                .fluentPut("in-port", in_port) // 额外的过滤项
//                .fluentPut("udp-source-port",65535 - Constant.hash(co_flow_id,flow_id)) // 这个是核心
//                .fluentPut("udp-destination-port",5001) // 这个也是
//                .fluentPut("ethernet-match", new JSONObject() // 这个是必须的
//                        .fluentPut("ethernet-type",new JSONObject()
//                                .fluentPut("type",2048)
//                        )
//                )
//                .fluentPut("ip-match",new JSONObject() // 这个也是必须的，不知道为什么
//                                .fluentPut("ip-protocol",17)
//                )
//        );
//
//        return flowTable;
//    }
}
