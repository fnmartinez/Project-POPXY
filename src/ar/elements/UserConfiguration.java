package ar.elements;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.ranges.Range;

import ar.POPXY;

public class UserConfiguration {

	private String serverAddress;
	private int port;
	private DeletionConfigurations deletionConfigurations;
	private int loginMax;
	private Boolean rotate;
	private Boolean leet;

	// TODO TIEMPO
	// In minutes of day, maximum 60*24=1440
	private List<Range<Integer>> scheduleList = new ArrayList<Range<Integer>>();

	public UserConfiguration() {
		resetUserConfiguration();
	}

	public void resetUserConfiguration() {
		this.serverAddress = null;
		this.port = -1;
		this.deletionConfigurations = new DeletionConfigurations();
		this.loginMax = -1;
		this.rotate = null;
		this.leet = null;
	}

	public void resetGlobalConfiguration() {
		User.setGlobalServerAddress(POPXY.getInstance()
				.getDefaultOriginServer());
		User.setGlobalServerPort(POPXY.getInstance()
				.getDefaultOriginServerPort());
		User.getGlobalConfiguration().getDeletionConfigurations()
				.resetGlobalConfiguration();
		User.setGlobalLoginMax(-1);
		User.setGlobalRotate(false);
		User.setGlobalLeet(false);
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

	public DeletionConfigurations getDeletionConfigurations() {
		return deletionConfigurations;
	}

	public void addScheduleRestriction(Range<Integer> r) {
		this.scheduleList.add(r);
	}

	public List<Range<Integer>> getScheduleList() {
		return scheduleList;
	}

}
