package com.gc.sdn.util;


import com.gc.sdn.constant.Constant;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


public class HttpRequestToOdl {
    private static String basicAuth = null;

    public static void setBasicAuth(String str){
        HttpRequestToOdl.basicAuth = str;
    }

    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        return sendGet(url, param, "utf-8");
    }


    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @param charSet
     *          网页编码
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param,String charSet) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            if(basicAuth !=null){
                connection.setRequestProperty("Authorization",
                        basicAuth);
            }
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                //System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(),charSet));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
            return new String(Constant.INTEGER_SERVER_ERROR.toString().getBytes());
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param){
        return sendPost(url, param, "utf-8");
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @param charSet
     *          网页编码
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param,String charSet) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("Content-type","application/json");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            if(basicAuth !=null){
                conn.setRequestProperty("Authorization",
                        basicAuth);
            }
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(),"utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
            return new String(Constant.INTEGER_SERVER_ERROR.toString().getBytes());
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 通过REST接口进行数据发送
     * @param url 发送的url
     * @param headers 发送用的头部
     * @param entity 发送的内容，以json格式
     * @param requestMethod PUT DELETE or POST
     */
    public static String send(String url, Map<String, String> headers, String entity, String requestMethod) throws Exception {
        headers.put("Authorization",basicAuth);
        URL postURL = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) postURL.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestMethod(requestMethod); // PUT DELETE POST
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setInstanceFollowRedirects(true);

        //json格式上传的模式
        StringBuilder sbStr = new StringBuilder();
        if(headers != null) {
            for(String pKey : headers.keySet()) {
                httpURLConnection.setRequestProperty(pKey, headers.get(pKey));
            }
        }
        if(entity != null) {
            try {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(httpURLConnection.getOutputStream(), "utf-8"));
                out.println(entity);
                out.close();
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    sbStr.append(inputLine);
                }
                in.close();
            }catch (Exception e){
                return new String(Constant.FORMAT_ERROR.toString().getBytes());
            }
        }
        httpURLConnection.disconnect();
        return new String(sbStr.toString().getBytes(), StandardCharsets.UTF_8);
    }
    public static String sendPut(String url, Map<String, String> headers, String entity, String requestMethod) throws Exception {
        return send(url,headers,entity,"PUT");
    }
}
