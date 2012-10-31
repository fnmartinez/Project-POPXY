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
	private Map<String, Set<String>> headers;
	private Set<String> contents;
	private int size;
	private Set<String> structures;

	public DeletionConfiguration() {
		resetUserConfiguration();
	}

	public void resetUserConfiguration() {
		this.date = null;
		this.senders = new HashSet<String>();
		this.headers = new HashMap<String, Set<String>>();
		this.contents = new HashSet<String>();
		this.size = -1;
		this.structures = new HashSet<String>();
	}

	public Map<String, Set<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Set<String>> headers) {
		this.headers = headers;
	}

	// Reescribir el m�todo en caso de que se quiera iniciar con una config
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

	public void removeSender(String string) {
		this.senders.remove(string);
	}
	
	public Set<String> getContents() {
		return contents;
	}

	public void addContentType(String contentType) {
		this.contents.add(contentType);
	}

	public void removeContentType(String contentType) {
		this.contents.remove(contentType);
	}

	public Set<String> getStructure() {
		return structures;
	}

	public void addStructure(String structure) {
		this.structures.add(structure);
	}

	public void removeStructure(String structure) {
		this.structures.remove(structure);
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

	public boolean passDateRestriction(Mail mail) {
		if (mail.getDate() == null || this.date == null) {
			return true;
		} else {
			//No se pueden eliminar mails mas antiguos que el de la fecha de config
			if(this.date.compareTo(mail.getDate()) < 0){
				return true;
			}
			return false;
		}
	}

	public boolean hasStructureRestriction() {
		return !structures.isEmpty();
	}

	public boolean hasContentTypeRestriction() {
		return !contents.isEmpty();
	}

	public boolean hasHeaderRestriction() {
		return !headers.isEmpty();
	}

	public boolean hasSenderRestriction() {
		return !senders.isEmpty();
	}

	public boolean hasDeletionRestriction() {
		if (date == null && senders.isEmpty() && headers.isEmpty()
				&& contents.isEmpty() && size == -1 && structures.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public boolean passStructuresRestriction(Mail mail) {
		Set<String> fromMail = mail.getStructures();
		for (String s : fromMail) {
			if (structures.contains(s)) {
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
		for (String s : fromMail) {
			if (contents.contains(s)) {
				return false;
			}
		}
		return true;
	}

	public boolean passHeadersRestriction(Mail mail) {
		Map<String, String> fromMail = mail.getHeaders();
		for (Entry<String, Set<String>> entry : headers.entrySet()) {
			for (String s : entry.getValue()) {
				String value;
				if ((value = fromMail.get(entry.getKey())) != null) {
					if (value.contains(s)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean passSendersRestriction(Mail mail) {
		return !senders.contains(mail.getFrom());
	}

	public void addHeader(String headerName, String headerValue) {
		if (headers.containsKey(headerName)) {
			headers.get(headerName).add(headerValue);
		} else {
			Set<String> set = new HashSet<String>();
			set.add(headerValue);
			headers.put(headerName, set);
		}
	}

	public void removeHeader(String headerName, String headerValue) {
		if (headers.containsKey(headerName)) {
			headers.get(headerName).remove(headerValue);
			if (headers.get(headerName).isEmpty()) {
				headers.remove(headerName);
			}
		}
	}

}
