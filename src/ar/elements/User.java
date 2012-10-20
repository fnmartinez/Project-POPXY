package ar.elements;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.joda.time.Interval;

import ar.POPXY;

public class User {
	
	// Configuraciones Generales
	private static UserConfiguration globalConfig;
	private Stats globalStats;
	
	// Configuraciones propias de un usuario
	private String user;
	private int loginCant;
	private Calendar lastConnection;
	private UserConfiguration userConfig;
	private Stats stats;

	public User(String user) {
		this.user = user;
		this.loginCant = 0;
		this.lastConnection = Calendar.getInstance();  
		this.userConfig = new UserConfiguration();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public static UserConfiguration getGlobalConfiguration(){
		return globalConfig;
	}
	
	public static void setGlobalLoginMax(int loginMax) {
		globalConfig.setLoginMax(loginMax);
	}
	
	public void setLoginMax(int loginMax){
		userConfig.setLoginMax(loginMax);
	}
	
	public static void setGlobalRotate(Boolean rotate){
		globalConfig.setRotate(rotate);
	}
	
	public void setRotate(Boolean rotate){
		userConfig.setRotate(rotate);
	}
	
	public static void setGlobalLeet(Boolean leet){
		globalConfig.setLeet(leet);
	}
	
	public void setLeet(Boolean leet){
		userConfig.setLeet(leet);
	}
	
	public void setServerAddress(InetAddress serverAddress){
		userConfig.setServerAddress(serverAddress);
	}

	public static void setGlobalServerAddress(InetAddress serverAddress){
		globalConfig.setServerAddress(serverAddress);
	}
	
	public InetAddress getServerAddress(){
		InetAddress server = userConfig.getServerAddress();
		if(server != null){
			return server;
		}else{
			return globalConfig.getServerAddress();
		}
	}
	
	public void setServerPort(int port){
		userConfig.setPort(port);
	}
	
	public static void setGlobalServerPort(int port){
		globalConfig.setPort(port);
	}
	
	public int getServerPort(){
		int p = userConfig.getPort();
		if(p != -1){
			return p;
		}else{
			return globalConfig.getPort();
		}
	}

	public static int getGlobalServerPort(){
		return globalConfig.getPort();
	}
	
	public Boolean getLeet(){
		Boolean l = userConfig.getLeet();
		if(l != null){
			return l;
		}else{
			return globalConfig.getLeet();
		}
	}
	
	public Boolean getRotate(){
		Boolean r = userConfig.getRotate();
		if(r != null){
			return r;
		}else{
			return globalConfig.getRotate();
		}
	}
	
	public void login() {
		Calendar now = Calendar.getInstance();
		if (lastConnection == null
				|| lastConnection.DAY_OF_YEAR != now.DAY_OF_YEAR) {
			lastConnection = now;
			loginCant = 0;
		}
		loginCant++;
		globalStats.incrementLoginCant();
		stats.incrementLoginCant();
	}
	
	public boolean haveLoginsLeft() {
		
		if(userConfig.getLoginMax() == -1){
			return haveGlobalLoginsLeft();
		}else{
			return loginCant < userConfig.getLoginMax();
		}
	}
	
	public boolean haveGlobalLoginsLeft(){
		if(globalConfig.getLoginMax() == -1){
			return true;
		}else{
			return loginCant < globalConfig.getLoginMax();
		}
	}
	
	public static void initGlobalConfiguration(){
		resetGlobalConfiguration();
	}
	
	public static void resetGlobalConfiguration(){
		globalConfig.resetGlobalConfiguration();
	}
	
	public boolean isInInterval() {
		//TODO
		return true;
	}
	
}
