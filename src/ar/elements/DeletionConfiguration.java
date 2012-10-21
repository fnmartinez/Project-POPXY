package ar.elements;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

public class DeletionConfiguration {

	private DateTime date;
	private List<String> senders;
	private List<String> headerPatterns;
	private List<String> contentTypes;
	private int size;
	private String structure;

	public DeletionConfiguration() {
		resetUserConfiguration();
	}

	public void resetUserConfiguration() {
		this.date = null;
		this.senders = new ArrayList<String>();
		this.headerPatterns = new ArrayList<String>();
		this.contentTypes = new ArrayList<String>();
		this.size = -1;
		this.structure = new String("");
	}

	// Reescribir el método en caso de que se quiera iniciar con una config
	// global d borrado default
	public void resetGlobalConfiguration() {
		resetUserConfiguration();
	}

	public List<String> getSenders() {
		return senders;
	}

	public void addSender(String sender) {
		this.senders.add(sender);
	}

	public List<String> getContentTypes() {
		return contentTypes;
	}

	public void addContentHeader(String contentHeader) {
		this.contentTypes.add(contentHeader);
	}

	public List<String> getHeaderPattern() {
		return headerPatterns;
	}

	public void addHeaderPattern(String headerPattern) {
		this.headerPatterns.add(headerPattern);
	}

	public String getStructure() {
		return structure;
	}

	public void setStructure(String structure) {
		this.structure = structure;
	}

	public int getSizeRestriction(){
		return size;
	}
	
	public boolean hasSizeRestriction(){
		return size != -1;
	}
	
	public void setSizeRestriction(int size){
		this.size = size;
	}
	
	public DateTime getTimeRestriction(){
		return date;
	}
	
	public boolean hasTimeRestriction(){
		return date != null;
	}

	public void setTimeRestriction(DateTime date){
		this.date = date;
	}
}
