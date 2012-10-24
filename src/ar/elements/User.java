package ar.elements;

import java.net.InetAddress;
import java.util.Calendar;

import ar.POPXY;
import ar.protocols.ConfigurationProtocol;

public class User {
	
	// Configuraciones Generales
	private static UserConfiguration globalConfig = new UserConfiguration();
	private static Stats globalStats = new Stats();
	
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
		this.stats = new Stats();
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
	
	public void setServerAddress(String serverAddress){
		userConfig.setServerAddress(serverAddress);
	}

	public static void setGlobalServerAddress(String serverAddress){
		globalConfig.setServerAddress(serverAddress);
	}
	
	public String getServerAddress(){
		String server = userConfig.getServerAddress();
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
	
	public static String getGlobalServerAddress(){
		return globalConfig.getServerAddress();
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
		if(userConfig.hasTimeConfiguration()){
			return userConfig.isInIntervalSet();
		}else{
			return globalConfig.isInIntervalSet();
		}		
	}

	public boolean isBlocked() {
		// TODO Auto-generated method stub
		return false;
	}

	public void deleteLoginMax(){
		userConfig.setLoginMax(-1);
	}

	public static void deleteGlobalLoginMax() {
		globalConfig.setLoginMax(-1);
	}
	
	public void addInterval(int minFrom, int minTo){
		userConfig.addInterval(new IntervalTime(minFrom, minTo));
	}
	
	public static void addGlobalInterval(int minFrom, int minTo){
		globalConfig.addInterval(new IntervalTime(minFrom, minTo));
	}
	
	public void removeInterval(int minFrom, int minTo){
		userConfig.removeInterval(new IntervalTime(minFrom, minTo));
	}
	
	public static void removeGlobalInterval(int minFrom, int minTo){
		globalConfig.removeInterval(new IntervalTime(minFrom, minTo));
	}
	
	public static Stats getGlobalStats(){
		return globalStats;
	}
	
	public void addTransferedBytes(long transferedBytes){
		stats.addTransferedBytes(transferedBytes);
	}

	public void setApp(String app, boolean bool) {		
		if(app == "l33t"){
			this.setLeet(bool);	
		} else if(app == "rotate"){			
			this.setRotate(bool);		
		} else if(app == "anonymous"){
			//this.setAnonymous(bool);	
		} else {
			//this.setCustomApp(app, bool);
		}
		return;
	}
	
	public static void setGlobalApp(String app, boolean bool) {		
		if(app == "l33t"){
			User.setGlobalLeet(bool);	
		} else if(app == "rotate"){			
			User.setGlobalRotate(bool);		
		} else if(app == "anonymous"){
			//User.setGlobalAnonymous(bool);	
		} else {
			//User.setGlobalCustomApp(app, bool);
		}
		return;
	}
	
}
