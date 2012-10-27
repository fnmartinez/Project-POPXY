package ar.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

public class Mail {

	private Map<String,List<String>> headers;
	private Set<String> contents;
	// Extensiones o subtipos (ej. application/pdf image/png text/html text/plain
	private Set<String> extensions;
	private int size;
	private DateTime date;
	private String from;
	
	public Mail(){
		this.headers = new HashMap<String, List<String>>();
		this.contents = new HashSet<String>();
		this.extensions = new HashSet<String>();	
	}
	
	
	public void addHeaderValue(String headerName, String headerValue) {
		List<String> headerValues = headers.get(headerName);
		if (headerValues == null) {
			headerValues = new ArrayList<String>();
			headers.put(headerName, headerValues);
		}
		headerValues.add(headerValue.toString());		
	}
	
	public void addContent(String content){
		contents.add(content.toUpperCase());
	}
	
	public void addExtension(String extension){
		this.extensions.add(extension);
	}
	
	public void setFrom(String from){
		this.from = from;
	}
	
}
