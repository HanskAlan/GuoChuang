package com.gc.sdn.util;

import com.alibaba.fastjson.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


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
     * 下发流表 / meter表，安装rest接口
     * */
    public static String installConfig(String json, String uri){
        System.out.println(uri);
        System.out.println("json==" + json);
        Map<String,String >headers = new HashMap<>();
        headers.put("Content-type","application/json");
        try {
            return HttpRequestToOdl.send(uri,headers,json, "PUT");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String installConfig(JSONObject json, String uri){
        return installConfig(json.toJSONString(),uri);
    }

    public static void installRPC(String json, String uri){
//        URL: /restconf/operations/sal-flow:add-flow
//        Method: POST
//        Headers:
    //        Content-type: application/json
//            Accept: application/json
//            Authentication: admin:admin
        long startTime = System.currentTimeMillis();
        Map<String,String >headers = new HashMap<>();
        headers.put("Content-type","application/json");
        headers.put("Accept","application/json");
        try {
            HttpRequestToOdl.send(uri, headers, json, "POST");
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println(
                uri + "\n" + json + "\n" +
                "--- start time " + startTime +
                " spend time " + (System.currentTimeMillis() - startTime)
        );
    }


    private String getBasicAuthStr(String name,String password){
        return "Basic " + Base64.getEncoder().encodeToString((name + ":" + password).getBytes());
    }

    /**
     * 获取相对时间
     */
    public static long getTime(){
        if (beginTime == -1) beginTime = System.currentTimeMillis();
        return System.currentTimeMillis() - beginTime;
    }
    private static long beginTime = -1;
}




