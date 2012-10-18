package ar.Filters;

import java.nio.ByteBuffer;

import ar.POPXY;
import ar.elements.User;

public class AppFilter {
	
	private POPXY proxy;
	private AccessFilter accessFilter;
	private TransactionFilter transactionFilter;
	
	public AppFilter(){
		this.proxy = POPXY.getInstance();
		this.accessFilter = new AccessFilter();
		this.transactionFilter = new TransactionFilter();
	}

	public boolean applyDeleteFilters(ByteBuffer[] buff, User user){
		return transactionFilter.applyDeleteFilters(buff,user);
	}
	
	public boolean applyAccessFilters(ByteBuffer[] buff){
		String user = buff[1].toString();
		return accessFilter.applyFilters(user);
	}
	
}
