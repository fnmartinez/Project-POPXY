package ar.Filters;

import ar.POPXY;
import ar.elements.Mail;
import ar.elements.User;

public class TransactionFilter {

	public static boolean applyDeleteFilters(Mail mail, User user) {
		return user.passDeletionFilters(mail);
	}	
}
