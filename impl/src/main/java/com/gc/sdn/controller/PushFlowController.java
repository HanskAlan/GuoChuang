package com.gc.sdn.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.gc.sdn.constant.Constant;
import com.gc.sdn.constant.ResponseResult;
import com.gc.sdn.service.PushFlowService;

import static com.gc.sdn.util.ParameterUtil.*;

/**
 * @Description:
 * @author: pan.wen
 * @date: 2020/9/8 10:54
 * @Name: GetAnswerFromRac
 */
public class PushFlowController {
    public static ResponseResult startRacPushFlow(JSONArray jsonArray){
        try {
            //解析开始
            for(int i=0; i<jsonArray.size(); i++){
                //协流id
                int coflowId = jsonArray.getJSONObject(i).getIntValue("coflowID");
                //协流属性
                JSONArray jsonArrayFlow = jsonArray.getJSONObject(i).getJSONArray("flowAssignments");
                for(int j=0; j < jsonArrayFlow.size(); j++){
                    JSONObject jsonObjectFlow = jsonArrayFlow.getJSONObject(j);
                    // 子流id
                    int flowId = jsonObjectFlow.getIntValue("flowID");
//                    //源节点
//                    String srcName = jsonObjectFlow.getString("source");
                    //目标节点
                    String dstName = jsonObjectFlow.getString("target");
//                    //流大小
//                    Long size = jsonObjectFlow.getLong("size");
                    //传输速率
                    Double rate = jsonObjectFlow.getDouble("rate");
//                    //路径长度
//                    int path_length = jsonObjectFlow.getIntValue("path-length");
                    //路径过程属性
                    JSONArray jsonArrayPathProcess = jsonObjectFlow.getJSONArray("pathProcess");
                    //迭代
                    if(jsonArrayPathProcess.size() < 3){
                        throw new Exception("RAC给出的路径信息长度小于3，不是合法的路径信息");
                    }
                    for (int p = 0; p + 2 < jsonArrayPathProcess.size(); p++) {
                        // 以一个三元组（A，B，C）作为一次下发流表的输入，其中B是下发流表的交换机
                        // A和C有可能是主机，也有可能是交换机
                        String A = jsonArrayPathProcess.getString(p);
                        String B = jsonArrayPathProcess.getString(p + 1);
                        String C = jsonArrayPathProcess.getString(p + 2);
                        // 预处理A，A可能是主机
                        if(p == 0) A = nodeMap.get(A);
                        // 预处理C，C可能是主机
                        if(p + 3 == jsonArrayPathProcess.size()) C = nodeMap.get(C);

                        String[] splitBuff;
                        // 计算B的输入端口
                        splitBuff = portMap.get(A + "-" + B).split("-");
                        String inPort = splitBuff[1].split(":")[2];
//                        inPort = extend(B,inPort);

                        // 计算B的输出端口
                        splitBuff = portMap.get(B + "-" + C).split("-");
                        String outPort = splitBuff[0].split(":")[2];
//                        outPort = extend(B,outPort);

                        PushFlowService.pushFlowAndMeter(coflowId,flowId, rate, inPort, outPort, B, dstName, p);
                    }
                }
            }
            return new ResponseResult(Constant.RESULT_SUCCESS_CODE, Constant.RESULT_SUCCESS_DESC);
        } catch (JSONException e){
            return new ResponseResult(Constant.FORMAT_ERROR,Constant.RESULT_FAIL_DESC);
        } catch (Exception e) {
            return new ResponseResult(Constant.RESULT_FAIL_CODE, Constant.RESULT_FAIL_DESC);
        }
    }


}
