package ar.elements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;

public class DeletionConfiguration {

	private DateTime date;
	private Set<String> senders;
	private Map<String,String> headers;
	private Set<String> contentTypes;
	private int size;
	private Set<String> structures;

	public DeletionConfiguration() {
		resetUserConfiguration();
	}

	public void resetUserConfiguration() {
		this.date = null;
		this.senders = new HashSet<String>();
		this.headers = new HashMap<String,String>();
		this.contentTypes = new HashSet<String>();
		this.size = -1;
		this.structures = new HashSet<String>();
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	// Reescribir el método en caso de que se quiera iniciar con una config
	// global d borrado default
	public void resetGlobalConfiguration() {
		resetUserConfiguration();
	}

	public Set<String> getSenders() {
		return senders;
	}

	public void addSender(String sender) {
		this.senders.add(sender);
	}

	public Set<String> getContentTypes() {
		return contentTypes;
	}

	public void addContentHeader(String contentHeader) {
		this.contentTypes.add(contentHeader);
	}

	public Set<String> getStructure() {
		return structures;
	}

	public void setStructure(Set<String> structure) {
		this.structures = structure;
	}

	public boolean hasSizeRestriction() {
		return size != -1;
	}

	public void setSizeRestriction(int size) {
		this.size = size;
	}

	public DateTime getDateRestriction() {
		return date;
	}

	public boolean hasDateRestriction() {
		return date != null;
	}

	public void setDateRestriction(DateTime date) {
		this.date = date;
	}

	public boolean hasStructureRestriction(){
		return !structures.isEmpty();
	}
	
	public boolean hasContentTypeRestriction(){
		return !contentTypes.isEmpty();
	}
	
	public boolean hasHeaderRestriction(){
		return !headers.isEmpty();
	}
	
	public boolean hasSenderRestriction(){
		return !senders.isEmpty();
	}
	
	public boolean hasDeletionRestriction() {
		if (date == null && senders.isEmpty() && headers.isEmpty()
				&& contentTypes.isEmpty() && size == -1 && structures.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public boolean passStructuresRestriction(Mail mail) {
		Set<String> fromMail = mail.getStructures();
		for(String s: fromMail){
			if(structures.contains(s)){
				return false;
			}
		}
		return true;
	}

	public boolean passSizeRestriction(Mail mail) {
		return mail.getSize() <= this.size;		
	}
	
	public boolean passContentTypesRestriction(Mail mail) {
		Set<String> fromMail = mail.getContents();
		for(String s: fromMail){
			if(contentTypes.contains(s)){
				return false;
			}
		}
		return true;
	}
	
	public boolean passHeadersRestriction(Mail mail) {
		
		Map<String,String> fromMail = mail.getHeaders();
		for(Entry<String,String> s: fromMail.entrySet()){
			if(headers.containsKey(s.getKey())){
				if(headers.get(s.getKey()).contains(s.getValue())){
					return false;
				}
			}
		}
		return true;
	}		
	
	public boolean passSendersRestriction(Mail mail) {
		return !senders.contains(mail.getFrom());
	}
	
}
