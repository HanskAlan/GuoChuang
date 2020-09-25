package com.gc.sdn.constant;

/**
 * @Description:
 * @author: pan.wen
 * @date:2020/7/16 13:43
 * @Name: Constant
 */
public interface Constant {

    public static final String RESULT_SUCCESS_DESC = "成功";
    public static final String RESULT_FAIL_DESC = "失败";

    public static final Integer RESULT_SUCCESS_CODE = 200;
    public static final Integer RESULT_FAIL_CODE = 401;
    public static final Integer REQUEST_INVALID = 400;
    public static final Integer NOT_FOUND = 404;
    public static final Integer FORMAT_ERROR = 415;
    public static final Integer INTEGER_SERVER_ERROR = 500;

    public static final String host = "192.168.0.105";
    public static final Integer port = 6633;
    public static final String username = "admin";
    public static final String password = "admin";
    public static final String containerName = "default";


}
