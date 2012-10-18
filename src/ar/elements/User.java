package ar.elements;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.Interval;



public class User {
	
	//Configuraciones genrales
	private static int globalLoginMax = -1;
	private static Interval globalLoginInterval;
	//TODO variables globales para eliminacion de mails
	
	private String user;
	private InetAddress serverAddress;
	private int port;
	private int loginMax;
	private int loginCant;
	private Calendar lastConnection;
	private Interval loginInterval;
	
	public User(String user, InetAddress serverAddress, int port){
		this.user = user;
		this.serverAddress = serverAddress;
		this.port = port;
		this.loginMax = -1;
		this.loginCant = 0;
		//TODO crear un intervalo que dure el dia 
	}
	
	
	public int getServerPort() {
		
		return this.port;
	}

	public InetAddress getServerAddress() {
		return this.serverAddress;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + port;
		result = prime * result
				+ ((serverAddress == null) ? 0 : serverAddress.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof User)) {
			return false;
		}
		User other = (User) obj;

		if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!user.equals(other.user)) {
			return false;
		}
		return true;
	}
	
	public void login(){
		Calendar now = Calendar.getInstance();
		if(lastConnection == null || lastConnection.DAY_OF_YEAR != now.DAY_OF_YEAR){
			lastConnection = now;
			loginCant = 0;
		}
			loginCant++;	
	}

	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	public int getLoginMax() {
		return loginMax;
	}


	public void setLoginMax(int loginMax) {
		this.loginMax = loginMax;
	}


	public int getLoginCant() {
		return loginCant;
	}


	public void setLoginCant(int loginCant) {
		this.loginCant = loginCant;
	}


	public Calendar getLastConnection() {
		return lastConnection;
	}


	public void setLastConnection(Calendar lastConnection) {
		this.lastConnection = lastConnection;
	}


	public Interval getLoginInterval() {
		return loginInterval;
	}


	public void setLoginInterval(Interval loginInterval) {
		this.loginInterval = loginInterval;
	}


	public void setServerAddress(InetAddress serverAddress) {
		this.serverAddress = serverAddress;
	}


	public boolean haveLoginsLeft() {
		if(loginMax == -1){
			return true;
		}else if(loginCant < loginMax){
			return true;
		}
		return false;
	}
	
	public boolean isInInterval(){
		//TODO
		return true;
	}
	
	public static void setGlobalLoginMax(int globalLoginMax){
		User.globalLoginMax = globalLoginMax; 
	}

	public static void setGlobalLoginInterval(Interval globalLoginInterval){
		User.globalLoginInterval = globalLoginInterval; 
	}
	
}
