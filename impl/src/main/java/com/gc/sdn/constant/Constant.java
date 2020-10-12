package com.gc.sdn.constant;


/**
 * @author pan.wen
 * @date 2020/7/16 13:43
 */
public interface Constant {
    String RESULT_SUCCESS_DESC = "成功";
    String RESULT_FAIL_DESC = "失败";

    Integer RESULT_SUCCESS_CODE = 200;
    Integer RESULT_FAIL_CODE = 401;
    Integer REQUEST_INVALID = 400;
    Integer NOT_FOUND = 404;
    Integer FORMAT_ERROR = 415;
    Integer INTEGER_SERVER_ERROR = 500;

    String host = "127.0.0.1";
//    String host = "192.168.2.119";
//    String host = "30.0.1.3";

    Integer port = 6633;
    String username = "admin";
    String password = "admin";
    String containerName = "default";

//    String RAC_LOG_PATH = "/home/ftp/RACLog/";
    String RAC_LOG_PATH = "D:/EP3Mulcos_ODL/target/RACLog/";

    static int hash(int coflowId,int flowId){
        return coflowId * 60 + flowId;
    }



    // 流表相关的参数
    int priority = 102; // 注意，priority必须大于dropPriority
    int dropPriority = priority - 1;
    int idleTimeOut = 10000;
    int hardTimeOut = 10000;
}
