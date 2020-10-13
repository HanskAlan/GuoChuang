package com.gc.sdn.util;

import com.alibaba.fastjson.JSONObject;
import com.gc.sdn.constant.Constant;
import org.springframework.stereotype.Service;


@Service
public class MeterJson {

    public static JSONObject getMeterJson(int coflowId,int rate, int flowId, int p){
        JSONObject jsonObjectMeter = new JSONObject();
        jsonObjectMeter.put("meter-id",String.valueOf(Constant.hash(coflowId,flowId)));
        jsonObjectMeter.put("meter-name","meter" + Constant.hash(coflowId,flowId) + p);
        jsonObjectMeter.put("flags","meter-kbps");

        JSONObject jsonObjectHeader = new JSONObject();
        jsonObjectHeader.put("band-id","0");

        JSONObject jsonObjectTypes = new JSONObject();
        jsonObjectTypes.put("flags","ofpmbt-drop");
        jsonObjectHeader.put("meter-band-types",jsonObjectTypes);
        jsonObjectHeader.put("drop-burst-size",0);
        jsonObjectHeader.put("drop-rate",rate);

        JSONObject jsonObjectHeaders = new JSONObject();
        jsonObjectHeaders.put("meter-band-header",jsonObjectHeader);

        jsonObjectMeter.put("meter-band-headers",jsonObjectHeaders);

        JSONObject jsonMeter = new JSONObject();
        jsonMeter.put("meter",jsonObjectMeter);

        return jsonMeter;
    }
}
