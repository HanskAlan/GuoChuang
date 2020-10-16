package com.gc.sdn.constant;


import com.routineAlgorithm.solver.*;

public interface Constant {
    String RESULT_SUCCESS_DESC = "成功";
    String RESULT_FAIL_DESC = "失败";

    Integer RESULT_SUCCESS_CODE = 200;
    Integer RESULT_FAIL_CODE = 401;
    Integer REQUEST_INVALID = 400;
    Integer NOT_FOUND = 404;
    Integer FORMAT_ERROR = 415;
    Integer INTEGER_SERVER_ERROR = 500;


    // ODL 相关参数
    String host = "127.0.0.1";
    Integer port = 6633;
    String username = "admin";
    String password = "admin";
    String containerName = "default";

    // RAC 参数
//    RASolver SOLVER = new OMCoflowSolver();
//    String SOLVER_NAME = "OMCoflow";

    RASolver SOLVER = new RapierSolver();
    String SOLVER_NAME = "Rapier";

    // RAC LOG 路径
    String RAC_LOG_PATH = "/home/aberror/Desktop/RACLog/";
//    String RAC_LOG_PATH = "/home/ftp/RACLog/";
//    String RAC_LOG_PATH = "D:/EP3Mulcos_ODL/target/RACLog/";


    // 流表相关的参数
    int priority = 102; // 注意，priority必须大于dropPriority
    int dropPriority = priority - 1; // 注意，priority必须大于dropPriority
    int idleTimeOut = 60;
    int hardTimeOut = 3600; // 1h
    static int hash(int coflowId,int flowId){
        return coflowId * 60 + flowId;
    }
}
