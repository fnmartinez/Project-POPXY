package ar.elements;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;


public class TimeConfiguration {

	private Set<IntervalTime> intervalSet;
	
	public TimeConfiguration(){
		this.intervalSet = new HashSet<IntervalTime>();
	}
	
	public boolean hasInterval(){
		return !intervalSet.isEmpty();
	}

	public Set<IntervalTime> getIntervals(){
		return intervalSet;
	}
	
	public void setIntervalSet(Set<IntervalTime> intervalSet){
		this.intervalSet = intervalSet;
	}
	
	public void addInterval(int from, int to){
		IntervalTime interval = new IntervalTime(from,to);
		addInterval(interval);
	}
	
	public void addInterval(IntervalTime interval){
		this.intervalSet.add(interval);
	}
	
	//Reescribir el método en caso de que se quiera iniciar con una config global d tiempo default
	public void resetGlobalTimeConfiguration() {
		User.getGlobalConfiguration().getTimeConfiguration().setIntervalSet(new HashSet<IntervalTime>());
		User.getGlobalConfiguration().getTimeConfiguration().addInterval(0, 1439);
	}
	
	public boolean isInIntervalSet(){		
		for(IntervalTime interval: intervalSet){
			if(interval.isInInterval()){
				return true;
			}
		}
		return false;
	}

	public void removeInterval(IntervalTime intervalTime) {
		intervalSet.remove(intervalTime);		
	}
	
}
