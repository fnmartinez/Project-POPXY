package ar.elements;


import ar.POPXY;

public class UserConfiguration {

	private String serverAddress;
	private int port;
	private DeletionConfiguration deletionConfiguration;
	private int loginMax;
	private Boolean rotate;
	private Boolean leet;
	private Boolean anonymous;
	private TimeConfiguration intervals;

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
	}

	public void resetGlobalConfiguration() {
		User.setGlobalServerAddress(POPXY.getInstance()
				.getDefaultOriginServer());
		User.setGlobalServerPort(POPXY.getInstance()
				.getDefaultOriginServerPort());
		User.getGlobalConfiguration().getDeletionConfiguration()
				.resetGlobalConfiguration();
		User.setGlobalLoginMax(-1);
		User.setGlobalRotate(false);
		User.setGlobalLeet(false);
		User.setGlobalAnonymous(false);
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
}
