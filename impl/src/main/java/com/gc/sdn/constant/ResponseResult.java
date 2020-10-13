package com.gc.sdn.constant;

import java.io.Serializable;


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
