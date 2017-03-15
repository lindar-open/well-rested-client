package com.lindar.wellrested.xml;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXB;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WellRestedXMLUtil {

    public static <T> String fromObjectToString(T xmlObject) {
        StringWriter sw = new StringWriter();
        JAXB.marshal(xmlObject, sw);
        return sw.toString();
    }

    public static <T> T fromStringToObject(String xmlString, Class<T> xmlObjClass) {
        return JAXB.unmarshal(new StringReader(xmlString), xmlObjClass);
    }

    private WellRestedXMLUtil() {
    }
}
