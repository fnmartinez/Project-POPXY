package ar.Filters;

import java.nio.ByteBuffer;

import ar.POPXY;
import ar.elements.User;

public class TransactionFilter {

	private POPXY proxy;
	
	public TransactionFilter() {
		this.proxy = POPXY.getInstance();
	}

	public boolean applyDeleteFilters(ByteBuffer[] buff, User user) {
		//TODO
		return true;
	}

	
}
