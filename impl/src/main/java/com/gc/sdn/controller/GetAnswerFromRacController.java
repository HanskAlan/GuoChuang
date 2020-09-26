package com.gc.sdn.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.gc.sdn.constant.Constant;
import com.gc.sdn.constant.ResponseResult;
import com.gc.sdn.service.GetAnswerFromRacService;
//import javax.annotation.Resource;

import static com.gc.sdn.util.ParameterUtil.*;

/**
 * @Description:
 * @author: pan.wen
 * @date:2020/9/8 10:54
 * @Name: GetAnswerFromRac
 */
public class GetAnswerFromRacController {

    GetAnswerFromRacService getAnswerFromRacService = new GetAnswerFromRacService();

    public ResponseResult startRacPushFlow(JSONArray jsonArray){
        try {
            //解析开始
            for(int i=0; i<jsonArray.size(); i++){
                //协流id
                int coflow_id = jsonArray.getJSONObject(i).getIntValue("coflowID");
                //协流属性
                JSONArray jsonArrayFlow = jsonArray.getJSONObject(i).getJSONArray("flowAssignments");
                    for(int j=0; j<jsonArrayFlow.size(); j++){
                    JSONObject jsonObjectFlow = jsonArrayFlow.getJSONObject(j);
                    //子流id
                    int flow_id = jsonObjectFlow.getIntValue("flowID");
                    //源节点
                    String source = jsonObjectFlow.getString("source");
                    //目标节点
                    String target = jsonObjectFlow.getString("target");
                    //流大小
                    Long size = jsonObjectFlow.getLong("size");
                    //传输速率
                    Double rate = jsonObjectFlow.getDouble("rate");
                    //路径长度
                    int path_length = jsonObjectFlow.getIntValue("path-length");
                    //路径过程属性
                    JSONArray jsonArrayPathProcess = jsonObjectFlow.getJSONArray("pathProcess");
                    //迭代
                    if(jsonArrayPathProcess.size() > 3) {
                        for (int p = 0; p < jsonArrayPathProcess.size() - 1; p++) {
                            String sourMac = "";
                            String in_port = "";
                            String out_port = "";
                            //目的交换机
                            String desSwitchIn = jsonArrayPathProcess.getString(p + 1);
                            String desSwitchOut = jsonArrayPathProcess.getString(p + 2);
                            if (p == 0) {
                                String linkIdIn = linkMap.get(nodeMap.get(source) + "-" + desSwitchIn);
                                String[] portIn = linkIdIn.split("/");
                                for (int k = 0; k < portIn.length; k++) {
                                    if (portIn[k].contains("openflow")) {
                                        String[] strPort = portIn[k].split(":");
                                        in_port = strPort[2];
                                    }
                                }
                                String linkIdOut = portMap.get(desSwitchIn + "-" + desSwitchOut);
                                String[] portOut = linkIdOut.split("-");
                                String[] strPort = portOut[0].split(":");
                                out_port = strPort[2];
                                getAnswerFromRacService.startRacPushFlow(coflow_id,flow_id, rate, in_port, out_port, desSwitchIn, target, p);
                            } else if (p == jsonArrayPathProcess.size() - 3) {
                                sourMac = jsonArrayPathProcess.getString(p);
                                String linkIdIn = portMap.get(sourMac + "-" + desSwitchIn);
                                String[] portIn = linkIdIn.split("-");
                                String[] strPortIn = portIn[1].split(":");
                                in_port = strPortIn[2];
                                String linkIdOut = linkMap.get(desSwitchIn + "-" + nodeMap.get(target));
                                String[] portOut = linkIdOut.split("/");
                                for (int k = 0; k < portOut.length; k++) {
                                    if (portOut[k].contains("openflow")) {
                                        String[] strPort = portOut[k].split(":");
                                        out_port = strPort[2];
                                    }
                                }
                                getAnswerFromRacService.startRacPushFlow(coflow_id,flow_id, rate, in_port, out_port, desSwitchIn, target, p);
                            } else{
                                sourMac = jsonArrayPathProcess.getString(p);
                                String linkIdIn = portMap.get(sourMac + "-" + desSwitchIn);
                                String[] portIn = linkIdIn.split("-");
                                String[] strPort = portIn[1].split(":");
                                in_port = strPort[2];
                                String linkIdOut = linkMap.get(desSwitchIn + "-" + desSwitchOut);
                                String[] portOut = linkIdOut.split(":");
                                out_port = portOut[2];
                                getAnswerFromRacService.startRacPushFlow(coflow_id,flow_id, rate, in_port, out_port, desSwitchIn, target, p);
                            }
                        }
                    }else{
                        String desSwitchIn = jsonArrayPathProcess.getString( 1);
                        String desSwitchOut = jsonArrayPathProcess.getString( 2);
                        String in_port = "";
                        String out_port = "";
                        String linkIdIn = linkMap.get(nodeMap.get(source) + "-" + desSwitchIn);
                        String[] portIn = linkIdIn.split("/");
                        for (int k = 0; k < portIn.length; k++) {
                            if (portIn[k].contains("openflow")) {
                                String[] strPort = portIn[k].split(":");
                                in_port = strPort[2];
                            }
                        }
                        String linkIdOut = linkMap.get(desSwitchIn + "-" + desSwitchOut);
                        String[] portOut = linkIdOut.split("/");
                        for (int k = 0; k < portOut.length; k++) {
                            if (portOut[k].contains("openflow")) {
                                String[] strPort = portOut[k].split(":");
                                out_port = strPort[2];
                            }
                        }
                        getAnswerFromRacService.startRacPushFlow(coflow_id,flow_id, rate, in_port, out_port, desSwitchIn, target, 1);
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

    public ResponseResult endRacPushFLow(JSONArray jsonArray){
        try {
            //解析开始
            for(int i=0; i<jsonArray.size(); i++){
                //协流id
                int coflow_id = jsonArray.getJSONObject(i).getIntValue("coflowID");
                //协流属性
                JSONArray jsonArrayFlow = jsonArray.getJSONObject(i).getJSONArray("flowAssignments");
                for(int j=0; j<jsonArrayFlow.size(); j++){
                    JSONObject jsonObjectFlow = jsonArrayFlow.getJSONObject(j);
                    //子流id
                    int flow_id = jsonObjectFlow.getIntValue("flowID");
                    //源节点
                    String source = jsonObjectFlow.getString("source");
                    //目标节点
                    String target = jsonObjectFlow.getString("target");
                    //流大小
                    Long size = jsonObjectFlow.getLong("size");
                    //传输速率
                    Double rate = jsonObjectFlow.getDouble("rate");
                    //路径长度
                    int path_length = jsonObjectFlow.getIntValue("path-length");
                    //路径过程属性
                    JSONArray jsonArrayPathProcess = jsonObjectFlow.getJSONArray("pathProcess");
                    //迭代
                    if(jsonArrayPathProcess.size() > 3) {
                        for (int p = jsonArrayPathProcess.size()-1; p > 1; p--) {
                            String sourMac = "";
                            String in_port = "";
                            String out_port = "";
                            //目的交换机
                            String desSwitchIn = jsonArrayPathProcess.getString(p -1);
                            String desSwitchOut = jsonArrayPathProcess.getString(p -2);
                            if (p == jsonArrayPathProcess.size()-1) {
                                String linkIdIn = linkMap.get(nodeMap.get(source) + "-" + desSwitchIn);
                                String[] portIn = linkIdIn.split("/");
                                for (int k = 0; k < portIn.length; k++) {
                                    if (portIn[k].contains("openflow")) {
                                        String[] strPort = portIn[k].split(":");
                                        in_port = strPort[2];
                                    }
                                }
                                String linkIdOut = portMap.get(desSwitchIn + "-" + desSwitchOut);
                                String[] portOut = linkIdOut.split("-");
                                String[] strPort = portOut[0].split(":");
                                out_port = strPort[2];
                                getAnswerFromRacService.startRacPushFlow(coflow_id,flow_id, rate, in_port, out_port, desSwitchIn, target, p);
                            } else if (p == 2) {
                                sourMac = jsonArrayPathProcess.getString(p);
                                String linkIdIn = portMap.get(sourMac + "-" + desSwitchIn);
                                String[] portIn = linkIdIn.split("-");
                                String[] strPortIn = portIn[1].split(":");
                                in_port = strPortIn[2];
                                String linkIdOut = linkMap.get(desSwitchIn + "-" + nodeMap.get(target));
                                String[] portOut = linkIdOut.split("/");
                                for (int k = 0; k < portOut.length; k++) {
                                    if (portOut[k].contains("openflow")) {
                                        String[] strPort = portOut[k].split(":");
                                        out_port = strPort[2];
                                    }
                                }
                                getAnswerFromRacService.startRacPushFlow(coflow_id,flow_id, rate, in_port, out_port, desSwitchIn, target, p);
                            } else{
                                sourMac = jsonArrayPathProcess.getString(p);
                                String linkIdIn = portMap.get(sourMac + "-" + desSwitchIn);
                                String[] portIn = linkIdIn.split("-");
                                String[] strPort = portIn[1].split(":");
                                in_port = strPort[2];
                                String linkIdOut = linkMap.get(desSwitchIn + "-" + desSwitchOut);
                                String[] portOut = linkIdOut.split(":");
                                out_port = portOut[2];
                                getAnswerFromRacService.startRacPushFlow(coflow_id,flow_id, rate, in_port, out_port, desSwitchIn, target, p);
                            }
                        }
                    }else{
                        String desSwitchIn = jsonArrayPathProcess.getString( 2);
                        String desSwitchOut = jsonArrayPathProcess.getString( 1);
                        String in_port = "";
                        String out_port = "";
                        String linkIdIn = linkMap.get(nodeMap.get(source) + "-" + desSwitchIn);
                        String[] portIn = linkIdIn.split("/");
                        for (int k = 0; k < portIn.length; k++) {
                            if (portIn[k].contains("openflow")) {
                                String[] strPort = portIn[k].split(":");
                                in_port = strPort[2];
                            }
                        }
                        String linkIdOut = linkMap.get(desSwitchIn + "-" + desSwitchOut);
                        String[] portOut = linkIdOut.split("/");
                        for (int k = 0; k < portOut.length; k++) {
                            if (portOut[k].contains("openflow")) {
                                String[] strPort = portOut[k].split(":");
                                out_port = strPort[2];
                            }
                        }
                        getAnswerFromRacService.startRacPushFlow(coflow_id,flow_id, rate, in_port, out_port, desSwitchIn, target, 1);
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
