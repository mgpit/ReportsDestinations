//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.11.27 at 01:03:26 CET 
//


package de.mgpit.oracle.reports.plugin.destination.cdm.schema;


/**
 * Java content class for content complex type.
 *  <p>The following schema fragment specifies the expected content contained within this java content object.
 * <p>
 * <pre>
 * &lt;complexType name="content">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="length" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="properties" type="{}properties" minOccurs="0"/>
 *         &lt;element name="data" type="{}data"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface ContentType {


    java.lang.String getData();

    void setData(java.lang.String value);

    int getLength();

    void setLength(int value);

    de.mgpit.oracle.reports.plugin.destination.cdm.schema.PropertiesType getProperties();

    void setProperties(de.mgpit.oracle.reports.plugin.destination.cdm.schema.PropertiesType value);

}
