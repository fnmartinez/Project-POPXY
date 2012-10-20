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
	
	public DeletionConfiguration(){
		resetUserConfiguration();
	}
	
	public void resetUserConfiguration(){
		this.senders = new ArrayList<String>();
		this.headerPatterns = new ArrayList<String>();
		this.contentTypes = new ArrayList<String>();
		this.structure = new String("");
	}
	
	//Reescribir el método en caso de que se quiera iniciar con una config global d borrado default
	public void resetGlobalConfiguration() {
		resetUserConfiguration();
	}
	
	

}
