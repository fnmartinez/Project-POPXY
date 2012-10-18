package ar.Filters;

import java.nio.ByteBuffer;

import ar.POPXY;
import ar.elements.User;

public class LoginTimesFilter extends AppFilter{

	private POPXY proxy;
	
	public LoginTimesFilter(){
		this.proxy = POPXY.getInstance();
	}
	
	public boolean filter(ByteBuffer buff, User user) {
		if(user != null){
			return true;
		}		
		
		//Todo parsear buffer
		
		
		User u = proxy.getUser("LO Q HAY EN BUFFER");
		// Caso en el que el usuario no existe en el proxy.
		if(u == null){
			return true;
		}
		if(u.haveLoginsLeft()){
			return true;
		}	
		return false;
	}

	
	
}
