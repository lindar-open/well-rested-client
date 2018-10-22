package com.lindar.wellrested;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "XmlEntry")
public class XMLEntry {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "body")
    private String body;

    @XmlAttribute(name = "id")
    private int id;

    public XMLEntry(){}

    public XMLEntry(String name, String title, String body, int id){
        this.name = name;
        this.title = title;
        this.body = body;
        this.id = id;
    }

    @Override
    public String toString(){
        return "XmlEntry{" +
                "\n name='" + name + "\'" +
                ",\n title='" + title + "\'" +
                ",\n body='" + body + "\'" +
                ",\n id='"  + id + "\n" +
                '}';
    }

}
