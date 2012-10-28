package ar.Filters;

import ar.POPXY;
import ar.elements.User;

public class AccessFilter {

	public static boolean applyFilters(String userName) {
		User user = POPXY.getInstance().getUser(userName);
		return loginTimesFilter(user) && loginIntervalFilter(user);
	}

	private static boolean loginIntervalFilter(User user) {
		return user.isInInterval();
	}

	private static boolean loginTimesFilter(User user) {
		return user.haveLoginsLeft();
	}

}
