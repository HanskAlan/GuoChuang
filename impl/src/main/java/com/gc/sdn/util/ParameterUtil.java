package com.gc.sdn.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gc.sdn.constant.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Description:
 * @author: pan.wen
 * @date:2020/8/3 16:43
 * @Name: ParameterUtil
 */
@Service
public class ParameterUtil {

    public static Map<String, String> hostMap = new HashMap<>();
    public static Map<String, String> nodeMap = new HashMap<>();
    public static Map<String, String> linkMap = new HashMap<>();
    public static Map<String, String> portMap = new HashMap<>();


    /**
    * @Author: pan.wen
    * @Description: 获取出端口
    * @Date: 2020/8/3  16:44
    * @params: []
    * @return: java.lang.String
    **/
    public String getPort(){
        return "";
    }

    /**
    * @Author: pan.wen
    * @Description: 获取设备id
    * @Date: 2020/8/3  16:45
    * @params: []
    * @return: java.lang.String
    **/
    public String getNode(){
        return "";
    }

    /**
    * @Author: pan.wen
    * @Description: 获取topo信息
    * @Date: 2020/8/3  16:47
    * @params: []
    * @return: java.lang.String
    **/
    public Map getPortInfo(){

        Map hashMap = null;
        try {
            OdlUtil odlUtil = new OdlUtil(Constant.host,Constant.port,Constant.username,Constant.password,Constant.containerName);
            String uri ="";
            String str = odlUtil.getTopologyInfo(uri);
            JSONObject jsonObject= JSONObject.parseObject(str);
            JSONObject jsonObjectOutput = jsonObject.getJSONObject("output");
            JSONArray jsonArrayPorts = jsonObjectOutput.getJSONArray("ports-info");
            hashMap = new HashMap();
            for(int i = 0; i < jsonArrayPorts.size(); i++){
                JSONObject jsonObjectPorts = jsonArrayPorts.getJSONObject(i);
                String node = jsonObjectPorts.getString("device-id");
                String mac = jsonObjectPorts.getString("hardware-address");
                String portName = jsonObjectPorts.getString("port-name");
                String portNumber = jsonObjectPorts.getString("port-number");
                hashMap.put(mac,portNumber+"-"+node);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return hashMap;
    }

    /**
    * @Author: pan.wen
    * @Description: 获取链路信息
    * @Date: 2020/8/3  21:36
    * @params: []
    * @return: java.util.Map
    **/
    public Map getLinkInfo(){

        Map hashMap = null;
        try {
            OdlUtil odlUtil = new OdlUtil(Constant.host,Constant.port,Constant.username,Constant.password,Constant.containerName);
            String uri = "";
            String str = odlUtil.getTopologyInfo(uri);
            JSONObject jsonObject= JSONObject.parseObject(str);
            JSONObject jsonObjectOutput = jsonObject.getJSONObject("output");
            JSONArray jsonArrayLinks = jsonObjectOutput.getJSONArray("links-info");
            hashMap = new HashMap();
            for(int i = 0; i < jsonArrayLinks.size(); i++){
                JSONObject jsonObjectLinks = jsonArrayLinks.getJSONObject(i);
                String link_id = jsonObjectLinks.getString("link-id");
                String des_device = jsonObjectLinks.getString("dst-device");
                int des_port = jsonObjectLinks.getIntValue("dst-port");
                String src_device = jsonObjectLinks.getString("src-device");
                hashMap.put(src_device,des_port);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return hashMap;
    }

    /**
    * @Author: pan.wen
    * @Description: 初始化topo信息
    * @Date: 2020/9/1  11:36
    * @params: []
    * @return: com.alibaba.fastjson.JSONObject
    **/
    public JSONObject initTopologyInfo(String host,Integer port,String username,String password,String containerName){
        JSONObject json = new JSONObject();
        try{
            OdlUtil odlUtil = new OdlUtil(host,port,username,password,containerName);
            String uri = "http://"+Constant.host+":8181/restconf/operational/network-topology:network-topology";
            String str = odlUtil.getTopologyInfo(uri);

            JSONObject jsonObject= JSONObject.parseObject(str);
            JSONObject jsonNet = jsonObject.getJSONObject("network-topology");
            JSONArray arrayTopo = jsonNet.getJSONArray("topology");
            JSONObject jsonTopo = arrayTopo.getJSONObject(0);

            // 获取交换机、主机节点信息，为获取到主机则返回
            JSONArray arrayNode = jsonTopo.getJSONArray("node");
            if(arrayNode == null){
                return null;
            }
            if(!arrayNode.toJSONString().contains("host") || arrayNode.size() < 18){
                return null;
            }
            JSONArray arrayVertex = new JSONArray();
            for(int i=0; i<arrayNode.size(); i++){
                JSONObject jsonNode = arrayNode.getJSONObject(i);
                String nodeId = jsonNode.getString("node-id");
                if(nodeId.contains("host")){
                    JSONArray arrayHost = jsonNode.getJSONArray("host-tracker-service:addresses");
                    JSONObject jsonAdd = arrayHost.getJSONObject(0);
                    String ip = jsonAdd.getString("ip");
                    JSONObject jsonHost = new JSONObject();
                    jsonHost.put("TAG",ip);
                    jsonHost.put("IsHost",true);
                    arrayVertex.add(jsonHost);
                    hostMap.put(nodeId,ip);
                    nodeMap.put(ip,nodeId);
                }else {
                    JSONObject jsonSwitch = new JSONObject();
                    jsonSwitch.put("TAG",nodeId);
                    jsonSwitch.put("IsHost",false);
                    arrayVertex.add(jsonSwitch);
                }
            }
            // 获取链路连接信息
            JSONArray arrayLink = jsonTopo.getJSONArray("link");
            if(arrayLink == null){
                return null;
            }
            JSONArray arrayEdges = new JSONArray();
            Set<String> setStr = new HashSet<String>();
            for(int j=0; j<arrayLink.size(); j++){
                JSONObject jsonLink = arrayLink.getJSONObject(j);
                String linkID = jsonLink.getString("link-id");
                JSONObject jsonSource = jsonLink.getJSONObject("source");
                String source = jsonSource.getString("source-node");
                String sourPort = jsonSource.getString("source-tp");
                JSONObject jsonDest = jsonLink.getJSONObject("destination");
                String dest = jsonDest.getString("dest-node");
                String destPort = jsonDest.getString("dest-tp");
                linkMap.put(source+"-"+dest,linkID);
                portMap.put(source+"-"+dest,sourPort+"-"+destPort);
                JSONObject jsonEdges = new JSONObject();
                if(setStr.contains(dest+"-"+source)){
                    continue;
                }
                setStr.add(source+"-"+dest);
                if(source.contains("host") || dest.contains("host")){
                    if(source.contains("host")){
                        source = hostMap.get(source);
                    }
                    if(dest.contains("host")){
                        dest = hostMap.get(dest);
                    }
                    jsonEdges.put("R",10);
                }else{
                    jsonEdges.put("R",1);
                }
                jsonEdges.put("A",source);
                jsonEdges.put("B",dest);
                arrayEdges.add(jsonEdges);
            }
            json.put("vertices",arrayVertex);
            json.put("edges",arrayEdges);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

}
