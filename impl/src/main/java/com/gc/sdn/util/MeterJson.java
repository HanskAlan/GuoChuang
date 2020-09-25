package com.gc.sdn.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @author: pan.wen
 * @date:2020/8/3 15:26
 * @Name: MeterJson
 */
@Service
public class MeterJson {

    public JSONObject getMeterJson(int co_flow_id,int rate, int meter_id, int p){
        JSONObject jsonObjectMeter = new JSONObject();
        jsonObjectMeter.put("meter-id",String.valueOf(co_flow_id)+meter_id);
        jsonObjectMeter.put("meter-name","meter"+meter_id+p);
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
