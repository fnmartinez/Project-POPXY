package ar.Filters;

import ar.POPXY;
import ar.elements.User;

public class AccessFilter {

	private POPXY proxy;
	
	public AccessFilter() {
		this.proxy = POPXY.getInstance();
	}

	public boolean applyFilters(String userName) {
		User user = proxy.getUser(userName);
		if(user == null){
			return true;
		}
		return loginTimesFilter(user) && loginIntervalFilter(user);
	}

	private boolean loginIntervalFilter(User user) {
		return user.haveLoginsLeft();
	}

	private boolean loginTimesFilter(User user) {
		return user.isInInterval();
	}

}
