package com.lindar.wellrested.xml;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXB;
import java.io.StringReader;
import java.io.StringWriter;

@Slf4j
@UtilityClass
public class WellRestedXMLUtil {

    public static <T> String fromObjectToString(T xmlObject) {
        StringWriter sw = new StringWriter();
        JAXB.marshal(xmlObject, sw);
        return sw.toString();
    }

    public static <T> T fromStringToObject(String xmlString, Class<T> xmlObjClass) {
        return JAXB.unmarshal(new StringReader(xmlString), xmlObjClass);
    }
}
