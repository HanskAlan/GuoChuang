package com.gc.sdn.util;

import org.springframework.stereotype.Service;

/**
 * @Description:
 * @author: pan.wen
 * @date:2020/7/21 14:15
 * @Name: MeterXml
 */
@Service
public class MeterXml {

    public String getMeterXml(String rate,int flow_id){
        StringBuilder  meterXml = new StringBuilder();
        meterXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        meterXml.append("<meter xmlns=\"urn:opendaylight:flow:inventory\">");
        meterXml.append("<container-name>c4</container-name>");
        meterXml.append("<flags>meter-burst</flags>");
        meterXml.append("<meter-band-headers>");
        meterXml.append("<meter-band-header>");
        meterXml.append("<band-burst-size>444</band-burst-size>");
        meterXml.append("<band-id>0</band-id>");
        meterXml.append("<band-rate>"+rate+"</band-rate>");
        meterXml.append("<dscp-remark-burst-size>5</dscp-remark-burst-size>");
        meterXml.append("<dscp-remark-rate>"+rate+"</dscp-remark-rate>");
        meterXml.append("<prec_level>1</prec_level>");
        meterXml.append("<meter-band-types>");
        meterXml.append("<flags>ofpmbt-dscp-remark</flags>");
        meterXml.append("</meter-band-types>");
        meterXml.append("</meter-band-header>");
        meterXml.append("</meter-band-headers>");
        meterXml.append("<meter-id>"+flow_id+"</meter-id>");
        meterXml.append("<meter-name>Foo</meter-name>");
        meterXml.append("</meter>");

        String xml = meterXml.toString();

        return xml;
    }
}
