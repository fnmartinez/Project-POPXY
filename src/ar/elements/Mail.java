package ar.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

public class Mail {
	
	private DateTime date;
	private String from;
	private Map<String,String> headers;
	private Set<String> contents;
	private int size;
	// Extensiones o subtipos (ej. application/pdf image/png text/html text/plain
	private Set<String> structures;
	
	public Mail(){
		this.headers = new HashMap<String, String>();
		this.contents = new HashSet<String>();
		this.structures = new HashSet<String>();	
	}
	
	
	public void addHeaderValue(String headerName, String headerValue) {
		headers.put(headerName,headerValue);		
	}
	
	public void addContent(String content){
		contents.add(content.toUpperCase());
	}
	
	public void addStructure(String structures){
		this.structures.add(structures);
	}
	
	public void setFrom(String from){
		this.from = from;
	}


	public Map<String, String> getHeaders() {
		return headers;
	}


	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}


	public Set<String> getContents() {
		return contents;
	}


	public void setContents(Set<String> contents) {
		this.contents = contents;
	}


	public Set<String> getStructures() {
		return structures;
	}


	public void setExtensions(Set<String> extensions) {
		this.structures = extensions;
	}


	public int getSize() {
		return size;
	}


	public void setSize(int size) {
		this.size = size;
	}


	public DateTime getDate() {
		return date;
	}


	public void setDate(DateTime date) {
		this.date = date;
	}


	public String getFrom() {
		return from;
	}

	
}
