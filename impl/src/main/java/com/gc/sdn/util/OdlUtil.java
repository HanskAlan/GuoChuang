package com.gc.sdn.util;

import com.alibaba.fastjson.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @author: pan.wen
 * @date:2020/7/16 16:10
 * @Name: OdlUtil
 */
public class OdlUtil {
    private String url;
    private String username;
    private String password;
    private String containerName;

    /**
     * 构造函数
     * @param host OpenDayLight的北向接口的主机名称
     * @param port OpenDayLight的北向接口的端口
     * @param username OpenDayLight的管理员的用户名
     * @param password OpenDayLight的管理员的密码
     * @param containerName OpenDayLight的容器名称
     * */
    public OdlUtil(String host,int port,String username,String password, String containerName){
        this.url = "http://" + host + ":" + port;
        this.username = username;
        this.password = password;
        this.containerName = containerName;
        HttpRequestToOdl.setBasicAuth(getBasicAuthStr(username,password));
    }


    /**
     * 获取拓扑信息
     * */
    public String getTopology(){

        String str = HttpRequestToOdl.sendGet(url + "/controller/nb/v2/topology/" + containerName,"");
        System.out.print(str);
        return str;
    }



    /**
     * 获取主机信息
     * */
    public String getHosts(){

        ///controller/nb/v2/topology/' + str(container_name)
        String str = HttpRequestToOdl.sendGet(url + "/controller/nb/v2/topology/" + containerName,"");
        System.out.println(str);
        return str;
    }

    /**
    * @Author: pan.wen
    * @Description:
    * @Date: 2020/8/3  20:36
    * @params: []
    * @return: java.lang.String
    **/
    public String getTopologyInfo(String uri){
        String str = null;
        try {
            str = HttpRequestToOdl.sendGet(uri,"");
        }catch (Exception e){
            e.printStackTrace();
        }
        return str;
    }

    /**
    * @Author: pan.wen
    * @Description: 下发meter表
    * @Date: 2020/7/21  14:55
    * @params: [meterXml, uri]
    * @return: java.lang.String
    **/
    public static String installMeter(JSONObject meterJson, String uri){
        String json = meterJson.toJSONString();
        System.out.println("json=="+meterJson);
        Map<String,String >headers = new HashMap<>();
        headers.put("Content-type","application/json");
        try {
            return HttpRequestToOdl.sendPut(uri,headers,json);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 下发流表
     * */
    public static String installFlow(JSONObject flowJson, String uri){
        String json = flowJson.toJSONString();
        System.out.println("json=="+json);
        Map<String,String >headers = new HashMap<>();
        headers.put("Content-type","application/json");
        try {
            return HttpRequestToOdl.sendPut(uri,headers,json);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    private String getBasicAuthStr(String name,String password){
        return "Basic " + Base64.getEncoder().encodeToString((name + ":" + password).getBytes());
    }
}
