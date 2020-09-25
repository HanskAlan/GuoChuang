package com.gc.sdn.constant;

import java.io.Serializable;

/**
 * @Description:
 * @author: pan.wen
 * @date:2020/7/16 14:38
 * @Name: ResponseResult
 */
public class ResponseResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private int resultCode;
    private String resultDesc;

    public ResponseResult(int resultCode, String resultDesc) {
        super();
        this.resultCode = resultCode;
        this.resultDesc = resultDesc;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultDesc() {
        return resultDesc;
    }

    public void setResultDesc(String resultDesc) {
        this.resultDesc = resultDesc;
    }
}
