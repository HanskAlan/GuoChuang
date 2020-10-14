package com.gc.sdn.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gc.sdn.constant.Constant;
import org.springframework.stereotype.Service;


@Service
public class MeterJson {

    public static JSONObject getMeterJson(int coflowId,int rate, int flowId, int p){
        JSONObject meterTable = new JSONObject()
                .fluentPut("meter-id",String.valueOf(Constant.hash(coflowId,flowId)))
                .fluentPut("meter-name","meter" + Constant.hash(coflowId,flowId) + p)
                .fluentPut("flags","meter-kbps")
                .fluentPut("meter-band-headers", new JSONObject()
                        .fluentPut("meter-band-header", new JSONArray()
                                .fluentAdd(new JSONObject()
                                        .fluentPut("band-id","0")
                                        .fluentPut("meter-band-types", new JSONObject()
                                                .fluentPut("flags","ofpmbt-drop")
                                        )
                                        .fluentPut("drop-burst-size",0)
                                        .fluentPut("drop-rate",rate)
                                )
                        )
                );
        return new JSONObject().fluentPut("input",meterTable);
    }
}
