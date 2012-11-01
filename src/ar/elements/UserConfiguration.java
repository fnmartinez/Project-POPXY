package ar.elements;


import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class UserConfiguration {

	private String serverAddress;
	private int port;
	private DeletionConfiguration deletionConfiguration;
	private int loginMax;
	private Boolean rotate;
	private Boolean leet;
	private Boolean anonymous;
	private TimeConfiguration intervals;
	private Set<String[]> externalApps;

	public UserConfiguration() {
		resetUserConfiguration();
	}

	public void resetUserConfiguration() {
		this.serverAddress = null;
		this.port = -1;
		this.deletionConfiguration = new DeletionConfiguration();
		this.loginMax = -1;
		this.rotate = null;
		this.leet = null;
		this.anonymous = null;
		this.intervals = new TimeConfiguration();
		this.externalApps = new HashSet<String[]>();
	}

	public void resetGlobalConfiguration() {
		Properties properties = new Properties();

		String originServer = "pop3.alu.itba.edu.ar";
		int originPort = 110;
		int maxLogin = -1;
		boolean rotate = false;
		boolean l33t = false;
		boolean anonymous = false;
	
		try {
			properties.load(new FileInputStream("resources/user.properties"));
			originServer = properties.getProperty("OriginServer");
			originPort = Integer.parseInt(properties.getProperty("OriginPort"));
			maxLogin = Integer.parseInt(properties.getProperty("MaxLogin"));
			rotate = Boolean.parseBoolean(properties.getProperty("Rotate"));
			l33t = Boolean.parseBoolean(properties.getProperty("L33t"));
			anonymous = Boolean.parseBoolean(properties.getProperty("Anonymous"));
		} catch (Exception e) {
			System.out.println("No se pudo leer el archivo de conf de usuarios");
		}

		User.setGlobalServerAddress(originServer);
		User.setGlobalServerPort(originPort);
		User.setGlobalLoginMax(maxLogin);
		User.setGlobalRotate(rotate);
		User.setGlobalLeet(l33t);
		User.setGlobalAnonymous(anonymous);

		User.getGlobalConfiguration().getDeletionConfiguration().resetGlobalConfiguration();
		User.getGlobalConfiguration().getTimeConfiguration().resetGlobalTimeConfiguration();
	}

	public int getLoginMax() {
		return loginMax;
	}

	public void setLoginMax(int loginMax) {
		this.loginMax = loginMax;
	}

	public Boolean getRotate() {
		return rotate;
	}

	public void setRotate(Boolean rotate) {
		this.rotate = rotate;
	}

	public Boolean getLeet() {
		return leet;
	}

	public void setLeet(Boolean leet) {
		this.leet = leet;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public DeletionConfiguration getDeletionConfiguration() {
		return deletionConfiguration;
	}
	
	public TimeConfiguration getTimeConfiguration(){
		return intervals;
	}
	
	public boolean hasTimeConfiguration(){
		return intervals.hasInterval();
	}
	
	public void addInterval(IntervalTime interval){
		this.intervals.addInterval(interval);
	}
	
	public boolean isInIntervalSet(){
		return this.intervals.isInIntervalSet();
	}

	public void removeInterval(IntervalTime intervalTime) {
		intervals.removeInterval(intervalTime);		
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

	public Boolean getAnonymous() {
		return this.anonymous;
	}
	
	public boolean hasDeletionRestriction(){
		return deletionConfiguration.hasDeletionRestriction();
	}
	
	public void addExternalApp(String[] app){
		this.externalApps.add(app);
	}
	
	public void removeExternalApp(String[] app){
		this.externalApps.remove(app);
	}
	
	public Set<String[]> getExternalApps(){
		return this.externalApps;
	}
	
	public boolean hasExternalApps(){
		return !this.externalApps.isEmpty();
	}
}
