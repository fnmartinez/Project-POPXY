package ar.Filters;

import java.nio.ByteBuffer;

import ar.POPXY;
import ar.elements.User;

public class LoginIntervalFilter implements AppFilter{

	private POPXY proxy;
	
	public LoginIntervalFilter(){
		this.proxy = POPXY.getInstance();
	}
	
	public boolean filter(String username, User user) {
		if(user != null){
			return true;
		}	
		//Todo parsear buffer
		User u = proxy.getUser("LO Q HAY EN BUFFER");
		// Caso en el que el usuario no existe en el proxy.
		if(u == null){
			return true;
		}
		if(u.isInInterval()){
			return true;
		}	
		return false;

	}

}
