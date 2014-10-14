package com.adobe.ReprocessJobAVSCreation.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public abstract class Scene7APIClient {
    /** default log */
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /** default charset */
    protected static final String CHARSET = "UTF-8";
    
    /** default content-type (i.e. 'application/soap+xml'), defaults to 'text/xml' */
    protected static final String CONTENTTYPE = "text/xml";

    // Initiate DocumentBuilderFactory
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	protected String getRequestBody(String operation, String user, String password, String payload) {
		String requestBody = "<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
				  "<SOAP-ENV:Header>";
				  	if("submitJob" == operation ){
				  		requestBody = requestBody + "<authHeader xmlns='http://www.scene7.com/IpsApi/xsd/2013-08-29-beta'>" +
				  		"<user><![CDATA[" + user + "]]></user>" +
				  		"<password><![CDATA[" + password + "]]></password>"+
						"<locale>en-US</locale>" +
				      "<appName>Adobe.Scene7.SPS</appName>" +
				      "<appVersion>6.8-189168</appVersion>" +
				      "<faultHttpStatusCode>200</faultHttpStatusCode>" +
				    "</authHeader>" +
				  "</SOAP-ENV:Header>" +
				  "<SOAP-ENV:Body>" +
				    "<" + operation + "Param xmlns='http://www.scene7.com/IpsApi/xsd/2013-08-29-beta'>";
				  	}else{
				  		requestBody = requestBody + "<authHeader xmlns='http://www.scene7.com/IpsApi/xsd'>"+
						  		"<user><![CDATA[" + user + "]]></user>" +
						  		"<password><![CDATA[" + password + "]]></password>"+
								"<locale>en-US</locale>" +
						      "<appName>Adobe.Scene7.CQ</appName>" +
						      "<appVersion>5.5</appVersion>" +
						      "<faultHttpStatusCode>200</faultHttpStatusCode>" +
						    "</authHeader>" +
						  "</SOAP-ENV:Header>" +
						  "<SOAP-ENV:Body>" +
						    "<" + operation + "Param xmlns='http://www.scene7.com/IpsApi/xsd/2010-04-23-beta'>"; 
				  		
				  	}
				  	requestBody = requestBody + 
				    payload +
				    "</" + operation + "Param>" +
				  "</SOAP-ENV:Body>" +
				"</SOAP-ENV:Envelope>";
		return requestBody;
	}
	
	protected Document getResponseDOM(InputStream response) {
		Document document = null;

	    try {
			// Get DocumentBuilder
			DocumentBuilder builder = factory.newDocumentBuilder();
			// Parse and load into memory the Document
			document = builder.parse(response);
	    }catch(Exception e) {
	    	log.error("exception while parsing scene7 answer", e);
	    }
	    return document;
	}
}
