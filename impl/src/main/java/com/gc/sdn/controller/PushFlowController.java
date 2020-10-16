package com.gc.sdn.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.gc.sdn.constant.Constant;
import com.gc.sdn.constant.ResponseResult;
import com.gc.sdn.service.PushFlowService;

import java.util.Arrays;
import java.util.HashMap;

import static com.gc.sdn.util.ParameterUtil.*;


public class PushFlowController {
    public static ResponseResult startRacPushFlow(JSONArray jsonArray){
        try {
            // 第一层，遍历协流的Assignment
            for(int i = 0; i < jsonArray.size(); i++){
                int coflowId = jsonArray.getJSONObject(i).getIntValue("coflowID");
                JSONArray flowAssignments = jsonArray.getJSONObject(i).getJSONArray("flowAssignments");

                // 第二层，遍历流Assignment
                for(int j = 0; j < flowAssignments.size(); j++){
                    try {
                        JSONObject jsonObjectFlow = flowAssignments.getJSONObject(j);
                        int flowId = jsonObjectFlow.getIntValue("flowID");
                        String dstName = jsonObjectFlow.getString("target");
                        Double rate = jsonObjectFlow.getDouble("rate");
                        JSONArray pathProcess = jsonObjectFlow.getJSONArray("pathProcess");

                        if(pathProcess.size() < 3){
                            throw new Exception("RAC给出的路径信息长度小于3，不是合法的路径信息");
                        }
                        String A = null;
                        String B = nodeMap.get(pathProcess.getString(0));
                        String C = pathProcess.getString(1);

                        for (int p = 0; p + 2 < pathProcess.size(); p++) {
                            // 以一个三元组（A，B，C）作为一次下发流表的输入，其中B是下发流表的交换机
                            A = B;B = C;C = pathProcess.getString(p + 2);
                            // 预处理C，C可能是主机
                            if(p + 3 == pathProcess.size()) C = nodeMap.get(C);

//                            String inPort = getInPort(A,B); // inPort_A -> outPort_B
                            String outPort = getOutPort(B,C); // inPort_B -> outPort_C

                            PushFlowService.pushFlowAndMeter(coflowId,flowId, rate, outPort, B, dstName, p);
                        }
                    } catch (Exception e){
                        // 我也不知道到底是哪里爆了异常
                        // 但是至少让一个flow的异常不要影响到另外一个flow
                        System.out.println("startRacPushFlow Exception0:");
                        System.out.println(flowAssignments.getJSONObject(j).toJSONString());
                        e.printStackTrace();
                    }
                }
            }
            return new ResponseResult(Constant.RESULT_SUCCESS_CODE, Constant.RESULT_SUCCESS_DESC);
        } catch (JSONException e){
            System.out.println("startRacPushFlow Exception1:");
            e.printStackTrace();
            return new ResponseResult(Constant.FORMAT_ERROR,Constant.RESULT_FAIL_DESC);
        }
        catch (Exception e) {
            System.out.println("startRacPushFlow Exception2:");
            e.printStackTrace();
            return new ResponseResult(Constant.RESULT_FAIL_CODE, Constant.RESULT_FAIL_DESC);
        }
    }

    private static final HashMap<String, HashMap<String,String[]>> quickPortMap = new HashMap<>();
    private static String[] getPort(String A,String B){
        if(quickPortMap.containsKey(A)){
            if(quickPortMap.get(A).containsKey(B))
                return quickPortMap.get(A).get(B);
        } else quickPortMap.put(A,new HashMap<>());

        String[] ports = {"initial","initial"};

        String[] splitBuff = null,temp;
        try{ // A:output -> B:input
            splitBuff = portMap.get(A + "-" + B).split("-");
            temp = splitBuff[0].split(":");
            if(temp.length == 3) ports[0] = temp[2]; // output

            temp = splitBuff[1].split(":");
            if(temp.length == 3) ports[1] = temp[2]; // input
        } catch (Exception e){
            try { // 如果没有，查看反向的情形有没有 B:input <- A:output
                splitBuff = portMap.get(B + "-" + A).split("-");
                temp = splitBuff[0].split(":");
                if(temp.length == 3) ports[1] = temp[2]; // input

                temp = splitBuff[1].split(":");
                if(temp.length == 3) ports[0] = temp[2]; // output
            } catch (Exception e1){
                ports[0] = ports[1] = "Error";
                System.out.println("getPort Exception: A = " + A + ", B = " + B );
                System.out.println(Arrays.toString(splitBuff));
                e.printStackTrace();
                e1.printStackTrace();
            }
        }

        quickPortMap.get(A).put(B,ports);
        return ports;
    }

    /**
     * 注意端口是从outPort -> inPort 的，返回的是A是outPort，B的inPort
     */
    public static String getInPort(String A,String B){
        return getPort(A,B)[1];
    }

    public static String getOutPort(String A,String B){
        return getPort(A,B)[0];
    }
}
